package com.gmi.nordborglab.browser.client.mvp.genotype.snpviewer;

import com.gmi.nordborglab.browser.client.manager.SearchManager;
import com.gmi.nordborglab.browser.client.mvp.genotype.GenotypePresenter;
import com.gmi.nordborglab.browser.client.mvp.widgets.snps.SNPDetailPresenterWidget;
import com.gmi.nordborglab.browser.client.place.GoogleAnalyticsManager;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPInfoPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.Promise;
import com.google.gwt.query.client.plugins.deferred.PromiseFunction;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.gquery.PromiseRF;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.presenter.slots.SingleSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by uemit.seren on 3/3/15.
 */
public class SNPViewerPresenter extends Presenter<SNPViewerPresenter.MyView, SNPViewerPresenter.MyProxy> implements SNPViewerUiHandlers {

    public interface MyView extends View, HasUiHandlers<SNPViewerUiHandlers> {
        void setAvailableGenotypes(List<AlleleAssayProxy> alleleAssayList);

        void setGenotype(AlleleAssayProxy genotype);

        void setRegion(String region);

        void showRegionError();

        void clearRegionError();

        HasData<SNPInfoProxy> getSNPSDisplay();

        void setPhenotype(String phenotype);

        void showDefaultLoadingIndicator(boolean show);

        void showSNPDetail(boolean show);
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.snpviewer)
    public interface MyProxy extends ProxyPlace<SNPViewerPresenter> {

    }

    public static final SingleSlot<SNPDetailPresenterWidget> SLOT_SNP_DETAIL = new SingleSlot<>();
    private final CurrentUser currentUser;
    private final PlaceManager placeManager;

    protected Integer chr;
    protected Long position;
    protected Long alleleAssayId;
    protected String region;
    protected final CustomRequestFactory rf;
    protected final SNPDetailPresenterWidget snpDetailPresenter;
    protected PhenotypeProxy phenotype;
    protected final GoogleAnalyticsManager analyticsManager;
    protected final SearchManager searchManager;

    protected final AsyncDataProvider<SNPInfoProxy> snpsDataProvider = new AsyncDataProvider<SNPInfoProxy>() {
        @Override
        protected void onRangeChanged(HasData<SNPInfoProxy> display) {
            if (!isFilterSet()) {
                updateRowCount(0, false);
                return;
            }
            analyticsManager.startTimingEvent("SNPViewer", "Load");
            final Range range = display.getVisibleRange();
            rf.annotationDataRequest().getSNPInfosForFilter(alleleAssayId, region, range.getStart(), range.getLength(), getPassportIdsFromTraits()).fire(new Receiver<SNPInfoPageProxy>() {
                @Override
                public void onSuccess(SNPInfoPageProxy response) {
                    analyticsManager.endTimingEvent("SNPViewer", "Load", "SUCCESS");
                    updateRowCount((int) response.getTotalElements(), true);
                    updateRowData(range.getStart(), response.getContents());
                }
            });
        }
    };

    RegExp geneRegExp = RegExp.compile("^AT([1-5]{1})G\\d+");
    RegExp regionRegExp = RegExp.compile("^Chr([1-5]{1}):\\d+-\\d+");

    @Inject
    public SNPViewerPresenter(EventBus eventBus, MyView view, MyProxy proxy,
                              final CurrentUser currentUser,
                              final PlaceManager placeManager,
                              final CustomRequestFactory rf,
                              final SearchManager searchManager,
                              SNPDetailPresenterWidget snpDetailPresenter,
                              final GoogleAnalyticsManager analyticsManager) {
        super(eventBus, view, proxy, GenotypePresenter.SLOT_CONTENT);
        this.snpDetailPresenter = snpDetailPresenter;
        this.analyticsManager = analyticsManager;
        this.searchManager = searchManager;
        getView().setUiHandlers(this);
        this.currentUser = currentUser;
        this.rf = rf;
        this.placeManager = placeManager;
    }

    @Override
    public void onBind() {
        super.onBind();
        setInSlot(SLOT_SNP_DETAIL, snpDetailPresenter);
        getView().setAvailableGenotypes(currentUser.getAppData().getAlleleAssayList());
    }


    @Override
    public void onReset() {
        super.onReset();
        PlaceRequest place = placeManager.getCurrentPlaceRequest();
        // Set the genotype from the filter
        final boolean isViewerChanged = parseViewerURL();
        final boolean isDetailSNPChanged = parseSNPChromURL();
        getView().showDefaultLoadingIndicator(isFilterSet());
        getView().setGenotype(getGenotype());
        // set the region from the filter
        getView().setRegion(region);
        loadPhenotype(Longs.tryParse(place.getParameter("phenotype", "")))
                .done(new Function() {
                    @Override
                    public void f() {
                        phenotype = getArgument(0);
                        if (snpsDataProvider.getDataDisplays().contains(getView().getSNPSDisplay())) {
                            if (isViewerChanged)
                                getView().getSNPSDisplay().setVisibleRangeAndClearData(getView().getSNPSDisplay().getVisibleRange(), true);
                        } else {
                            snpsDataProvider.addDataDisplay(getView().getSNPSDisplay());
                        }
                        if (phenotype == null) {
                            getView().setPhenotype(null);
                        } else {
                            getView().setPhenotype(phenotype.getLocalTraitName() + " (" + phenotype.getNumberOfObsUnits() + ")");
                        }
                        if (isDetailSNPChanged || isViewerChanged)
                            setDetailData();

                    }
                })
                .fail(new Function() {
                    @Override
                    public void f() {
                        getView().setPhenotype(null);
                        placeManager.updateHistory(new PlaceRequest.Builder(placeManager.getCurrentPlaceRequest()).without("phenotype").build(), true);
                    }
                });
    }

    private Promise loadPhenotype(Long phenotypeId) {
        boolean isLoad = isPhenotypeChanged(phenotypeId);
        if (phenotypeId == null) {
            phenotype = null;
            isLoad = false;
        }
        if (!isLoad) {
            return new PromiseFunction() {
                @Override
                public void f(Deferred dfd) {
                    dfd.resolve(phenotype);
                }
            };
        }
        return new PromiseRF(rf.phenotypeRequest().findPhenotype(phenotypeId).with("traits.obsUnit.stock.passport.collection.locality"));
    }


    @Override
    public void onSearchPhenotype(final String request, final SearchManager.SearchCallback callback) {
        searchManager.searchByFilter(request, ConstEnums.FILTERS.PHENOTYPE, callback);
    }

    @Override
    public void onSelectRegion(String region) {
        PlaceRequest.Builder request = new PlaceRequest.Builder(placeManager.getCurrentPlaceRequest());
        getView().clearRegionError();
        if (region == null || region.isEmpty()) {
            request.without("region");
        } else {
            region = parseRegion(region);
            if (region == null)
                return;
            request.with("region", region);
        }
        placeManager.revealPlace(request.build());
    }

    @Override
    public void onSelectPhenotype(SearchItemProxy phenotype) {
        PlaceRequest.Builder request = new PlaceRequest.Builder(placeManager.getCurrentPlaceRequest());
        if (phenotype == null) {
            request.without("phenotype");
        } else {
            request.with("phenotype", phenotype.getId());
        }
        placeManager.revealPlace(request.build());
    }

    @Override
    public void onSelectAlleleAssay(AlleleAssayProxy alleleAssay) {
        PlaceRequest.Builder request = new PlaceRequest.Builder(placeManager.getCurrentPlaceRequest());
        if (alleleAssay == null) {
            request.without("genotype");
        } else {
            request.with("genotype", alleleAssay.getId().toString());
        }
        placeManager.revealPlace(request.build());
    }

    @Override
    public void onSelectSNP(SNPInfoProxy snp) {
        PlaceRequest.Builder rqBuilder = new PlaceRequest.Builder(placeManager.getCurrentPlaceRequest());
        if (snp == null) {
            rqBuilder.without("chr").without("position");
        } else {
            rqBuilder.with("chr", snp.getChr()).with("position", String.valueOf(snp.getPosition()));
        }
        PlaceRequest request = rqBuilder.build();
        if (!placeManager.getCurrentPlaceRequest().equals(request))
            placeManager.revealPlace(request);
    }


    private void setDetailData() {
        boolean isSNPSelected = isSNPSelected();
        if (isSNPSelected && isFilterSet()) {
            snpDetailPresenter.setData(chr, position.intValue(), alleleAssayId, phenotype != null ? phenotype.getTraits() : null);
        }
        getView().showSNPDetail(isSNPSelected);
    }

    private boolean isSNPSelected() {
        return chr != null && position != null;
    }

    private List<Long> getPassportIdsFromTraits() {
        if (phenotype == null || phenotype.getTraits() == null)
            return null;
        return FluentIterable.from(phenotype.getTraits())
                .transform(new com.google.common.base.Function<TraitProxy, Long>() {
                    @Nullable
                    @Override
                    public Long apply(TraitProxy input) {
                        if (input != null && input.getObsUnit() != null
                                && input.getObsUnit().getStock() != null && input.getObsUnit().getStock().getPassport() != null) {
                            return input.getObsUnit().getStock().getPassport().getId();
                        }
                        return null;
                    }
                }).filter(Predicates.notNull()).toList();
    }

    private String parseRegion(String region) {
        if (region == null || region.isEmpty())
            return null;
        MatchResult geneMatcher = geneRegExp.exec(region);
        MatchResult regionMatcher = regionRegExp.exec(region);
        if (geneMatcher == null && regionMatcher == null) {
            getView().showRegionError();
            return null;
        }
        return region;
    }

    private AlleleAssayProxy getGenotype() {
        if (alleleAssayId == null)
            return null;
        return FluentIterable.from(currentUser.getAppData().getAlleleAssayList())
                .firstMatch(new Predicate<AlleleAssayProxy>() {
                    @Override
                    public boolean apply(AlleleAssayProxy input) {
                        if (input == null)
                            return false;
                        return alleleAssayId.equals(input.getId());
                    }
                }).orNull();
    }

    private boolean isFilterSet() {
        return alleleAssayId != null && region != null;
    }


    private boolean parseViewerURL() {
        PlaceRequest place = placeManager.getCurrentPlaceRequest();
        Long alleleAssayIdToLoad = Longs.tryParse(place.getParameter("genotype", ""));
        String regionToLoad = parseRegion(place.getParameter("region", null));
        Long phenotypeToLoad = Longs.tryParse(place.getParameter("phenotype", ""));
        boolean isChanged = isPhenotypeChanged(phenotypeToLoad) || alleleAssayIdToLoad != alleleAssayId || regionToLoad != region;
        alleleAssayId = alleleAssayIdToLoad;
        region = regionToLoad;
        return isChanged;
    }

    private boolean isPhenotypeChanged(Long phenotypeId) {
        return (phenotype != null && phenotypeId == null) || (phenotype == null && phenotypeId != null) || (phenotype != null && phenotypeId != phenotype.getId());

    }

    private boolean parseSNPChromURL() {
        PlaceRequest place = placeManager.getCurrentPlaceRequest();
        Integer chrToLoad = Ints.tryParse(place.getParameter("chr", ""));
        Long positionToLoad = Longs.tryParse(place.getParameter("position", ""));
        boolean isChanged = chrToLoad != chr || positionToLoad != position;
        chr = chrToLoad;
        position = positionToLoad;
        return isChanged;
    }
}

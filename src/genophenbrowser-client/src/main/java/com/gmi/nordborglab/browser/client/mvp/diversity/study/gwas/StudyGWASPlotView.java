package com.gmi.nordborglab.browser.client.mvp.diversity.study.gwas;

import com.gmi.nordborglab.browser.client.events.SelectSNPEvent;
import com.gmi.nordborglab.browser.client.ui.SNPDetailPopup;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class StudyGWASPlotView extends ViewWithUiHandlers<StudyGWASPlotUiHandlers> implements
        StudyGWASPlotPresenter.MyView {

    private final Widget widget;

    public interface Binder extends UiBinder<Widget, StudyGWASPlotView> {

    }

    @UiField
    SimpleLayoutPanel gwasPlotContainer;
    private final SNPDetailPopup snpPopOver;

    @Inject
    public StudyGWASPlotView(final Binder binder, SNPDetailPopup snpPopOver) {
        this.snpPopOver = snpPopOver;
        widget = binder.createAndBindUi(this);
        bindSlot(StudyGWASPlotPresenter.SLOT_GWAS_PLOT, gwasPlotContainer);
        snpPopOver.setAnimationEnabled(true);
        snpPopOver.getSNPLdLink().addClickHandler((e) -> {
            getUiHandlers().showLdForRegion(snpPopOver.getChromosome().toString(), snpPopOver.getPosition());
            snpPopOver.hide();
        });
        snpPopOver.getSnpExactLdLink().addClickHandler((e) -> {
            getUiHandlers().showExactLdForRegion(snpPopOver.getChromosome().toString(), snpPopOver.getPosition());
            snpPopOver.hide();
        });
        snpPopOver.getSnpGlobalLdLink().addClickHandler((e) -> {
            getUiHandlers().showLdForSNP(snpPopOver.getChromosome().toString(), snpPopOver.getPosition());
            snpPopOver.hide();
        });
    }

    @Override
    public Widget asWidget() {
        return widget;
    }


    @Override
    public void showSNPPopUp(Long analysisId, SelectSNPEvent event) {
        snpPopOver.setDataPoint(analysisId, event.getChromosome(), event.getxVal());
        snpPopOver.setPopupPosition(event.getClientX(), event.getClientY() - 84 / 2);
        snpPopOver.show();
    }

    @Override
    public void setHasLdData(boolean hasLdData) {
        snpPopOver.setHasLdData(hasLdData);
    }
}

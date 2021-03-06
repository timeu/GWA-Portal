package com.gmi.nordborglab.browser.client.mvp.germplasm;

import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.TaxonomyProxy;
import com.google.common.collect.ImmutableList;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelCollapse;
import org.gwtbootstrap3.client.ui.PanelGroup;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.constants.Toggle;

import java.util.HashMap;
import java.util.Map;


public class GermplasmView extends ViewImpl implements
        GermplasmPresenter.MyView {

    private final Widget widget;

    public interface Binder extends UiBinder<Widget, GermplasmView> {
    }

    public interface MyStyle extends CssResource {
        String header_section();

        String header_section_active();

        String subitem_active();

        String icon();
    }

    @UiField
    SimpleLayoutPanel container;

    @UiField
    FlowPanel breadcrumbs;

    @UiField
    FlowPanel menuContainer;
    @UiField
    SimpleLayoutPanel searchContainer;

    @UiField
    MyStyle style;

    @UiField
    Label titleLabel;
    private boolean menuInitialized = false;
    private MainResources mainRes;

    private final PlaceManager placeManager;
    private PanelGroup accordion = null;
    private Map<Long, Panel> accordionGroups = new HashMap<>();
    private Map<Long, ListGroup> subMenuItems = new HashMap<Long, ListGroup>();

    private final static String PANEL_ID = "germplasmmenu";


    @Inject
    public GermplasmView(final Binder binder, final PlaceManager placeManager,
                         final MainResources mainRes) {
        this.placeManager = placeManager;
        this.mainRes = mainRes;
        widget = binder.createAndBindUi(this);
        bindSlot(GermplasmPresenter.SLOT_SEARCH, searchContainer);
        bindSlot(GermplasmPresenter.SLOT_CONTENT, container);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }


    private void setMainContent(IsWidget content) {
        if (content != null) {
            container.setWidget(content);
        }
    }

    @Override
    public void setTitle(String title) {
        if (title != null)
            titleLabel.setText(title);
    }

    @Override
    public void clearBreadcrumbs(int breadcrumbSize) {
        breadcrumbs.clear();
        if (breadcrumbSize > 0)
            breadcrumbs.add(new InlineHyperlink("Loading title...", ""));
        for (int i = 0; i < breadcrumbSize; ++i) {
            breadcrumbs.add(new InlineLabel(" > "));
            breadcrumbs.add(new InlineHyperlink("Loading title...", ""));
        }
    }

    @Override
    public void setBreadcrumbs(int index, String title, String historyToken) {
        InlineHyperlink hyperlink = null;
        if (index == 0)
            hyperlink = (InlineHyperlink) breadcrumbs.getWidget(0);
        else
            hyperlink = (InlineHyperlink) breadcrumbs.getWidget((index * 2));
        if (title == null) {
            hyperlink.setText("Unknown title");
        } else {
            hyperlink.setText(title);
        }
        hyperlink.setTargetHistoryToken(historyToken);
    }

    @Override
    public void initMenu(ImmutableList<TaxonomyProxy> taxonomies) {
        if (menuInitialized)
            return;

        accordionGroups.clear();
        subMenuItems.clear();
        InlineHyperlink subItem = null;
        ListGroupItem li = null;
        ListGroup ul = null;
        PlaceRequest.Builder request = new PlaceRequest.Builder().nameToken(NameTokens.taxonomy);
        PlaceRequest.Builder requestPassports = new PlaceRequest.Builder().nameToken(NameTokens.passports);
        accordion = new PanelGroup();
        accordion.setId(PANEL_ID);
        int i = 0;
        for (TaxonomyProxy taxonomy : taxonomies) {
            String COLLAPSE_ID = "taxonomy_" + taxonomy.getId();
            request.with("id", taxonomy.getId().toString());
            requestPassports.with("id", taxonomy.getId().toString());

            Panel menuItem = new Panel();
            accordionGroups.put(taxonomy.getId(), menuItem);

            PanelHeader panelHeader = new PanelHeader();
            panelHeader.setDataParent("#" + PANEL_ID);
            panelHeader.setDataTarget("#" + COLLAPSE_ID);
            panelHeader.setDataToggle(Toggle.COLLAPSE);
            menuItem.add(panelHeader);

            HTMLPanel header = new HTMLPanel("");
            header.setStylePrimaryName(style.header_section());
            Label icon = new Label();
            icon.addStyleName(mainRes.style().plant_icon());
            icon.addStyleName(style.icon());
            InlineLabel l = new InlineLabel();
            l.setText(taxonomy.getGenus() + " " + taxonomy.getSpecies());
            header.add(icon);
            header.add(l);
            panelHeader.add(header);

            PanelCollapse panelCollapse = new PanelCollapse();
            panelCollapse.setId(COLLAPSE_ID);
            if (i == 0) {
                panelCollapse.setIn(true);
            }
            menuItem.add(panelCollapse);
            ul = new ListGroup();
            subMenuItems.put(taxonomy.getId(), ul);
            subItem = new InlineHyperlink();
            subItem.setText("Overview");
            subItem.setTargetHistoryToken(placeManager
                    .buildHistoryToken(request.build()));
            li = new ListGroupItem();
            li.add(subItem);
            ul.add(li);

            subItem = new InlineHyperlink();
            subItem.setText("All Accessions");
            requestPassports.with("alleleAssayId", "0");
            subItem.setTargetHistoryToken(placeManager
                    .buildHistoryToken(requestPassports.build()));
            li = new ListGroupItem();
            li.getElement().setAttribute("alleleAssayId", "0");
            li.add(subItem);
            ul.add(li);

            if (taxonomy.getAlleleAssays() != null) {
                for (AlleleAssayProxy alleleAssay : taxonomy.getAlleleAssays()) {
                    li = new ListGroupItem();
                    li.getElement().setAttribute("alleleAssayId",
                            alleleAssay.getId().toString());
                    subItem = new InlineHyperlink();
                    subItem.setText(alleleAssay.getName());
                    subItem.setTargetHistoryToken(placeManager
                            .buildHistoryToken(requestPassports.with(
                                    "alleleAssayId", alleleAssay.getId()
                                            .toString()
                            ).build()));
                    requestPassports.with("alleleAssayId", alleleAssay.getId()
                            .toString());
                    li.add(subItem);
                    ul.add(li);
                }
            }
            panelCollapse.add(ul);
            accordion.add(menuItem);
            i = i + 1;
        }
        menuContainer.add(accordion);
        menuInitialized = true;
    }


    @Override
    public void setActiveMenuItem(Long selectedTaxonomyId, Long alleleAssayId) {
        for (Map.Entry<Long, Panel> group : accordionGroups.entrySet()) {
            Widget header = group.getValue().getWidget(0);
            header.removeStyleName(style.header_section_active());
            if (selectedTaxonomyId != null) {
                if (selectedTaxonomyId == group.getKey()) {
                    header.addStyleName(style.header_section_active());
                }
            }
        }

        for (Map.Entry<Long, ListGroup> listEntry : subMenuItems.entrySet()) {
            ListGroup list = listEntry.getValue();
            for (int i = 0; i < list.getWidgetCount(); i++) {
                ListGroupItem item = (ListGroupItem) list.getWidget(i);
                item.removeStyleName(style.subitem_active());
                if (selectedTaxonomyId != null
                        && selectedTaxonomyId == listEntry.getKey()) {
                    if (alleleAssayId == null && i == 0) {
                        item.addStyleName(style.subitem_active());
                    } else if (i > 0
                            && alleleAssayId != null
                            && alleleAssayId == Long.parseLong(item
                            .getElement()
                            .getAttribute("alleleAssayId"))) {
                        item.addStyleName(style.subitem_active());
                    }
                }
            }
        }
    }
}

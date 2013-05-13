package com.gmi.nordborglab.browser.client.mvp.view.diversity.ontology;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.editors.OntologyDisplayEditor;
import com.gmi.nordborglab.browser.client.mvp.handlers.OntologyUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.ontology.GraphOntologyTreeViewModel;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.ontology.TraitOntologyPresenter;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.PhenotypeListDataGridColumns;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.cells.GraphOntologyCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.GraphTerm2TermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.GraphTermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.Term2TermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.user.client.Command;
import static com.google.gwt.query.client.GQuery.$;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

import java.util.List;

public class TraitOntologyView extends ViewWithUiHandlers<OntologyUiHandlers> implements
		TraitOntologyPresenter.MyView{


	private final Widget widget;

    @UiField(provided=true)
    DataGrid<PhenotypeProxy> phenotypeDataGrid;
    @UiField
    CustomPager phenotypePager;
    @UiField
    ScrollPanel navTreeContainer;
    @UiField
    Label ontologyCategory;
    @UiField
    OntologyDisplayEditor ontologyEditor;
    @UiField
    TabLayoutPanel tabPanel;
    @UiField
    Label phenotypeTabHeader;


    private SingleSelectionModel<GraphTerm2TermProxy> selectionModel = new SingleSelectionModel<GraphTerm2TermProxy>();
    private GraphOntologyDataProvider dataProvider = new GraphOntologyDataProvider() {

        @Override
        public void refreshWithChildTerms(HasData<GraphTerm2TermProxy> display, GraphTerm2TermProxy term) {
            getUiHandlers().refreshWithChildTerms(display,term);
        }
    };
    private final CellTree.BasicResources cellTreeResources;
    private final OntologyDisplayDriver ontologyDisplayDriver;
    private final PlaceManager placeManager;
    private final GraphOntologyCell ontologyCell;
    private CellTree navTree;
    private TermProxy selectedTerm = null;

    public interface OntologyDisplayDriver extends RequestFactoryEditorDriver<TermProxy,OntologyDisplayEditor> {}

    public interface OntologyDataProvider  {
        public void refreshWithChildTerms(HasData<Term2TermProxy> display,Term2TermProxy term);
    }

    public interface GraphOntologyDataProvider  {
        public void refreshWithChildTerms(HasData<GraphTerm2TermProxy> display,GraphTerm2TermProxy term);
    }


    public interface Binder extends UiBinder<Widget, TraitOntologyView> {
	}

	@Inject
	public TraitOntologyView(final Binder binder, final CustomDataGridResources dataGridResources,
                             final CellTree.BasicResources cellTreeResources,final OntologyDisplayDriver ontologyDisplayDriver,
                             final PlaceManager placeManager,
                             final GraphOntologyCell ontologyCell) {
        this.cellTreeResources = cellTreeResources;
        this.ontologyCell = ontologyCell;
        this.ontologyDisplayDriver = ontologyDisplayDriver;
        this.placeManager = placeManager;
        phenotypeDataGrid = new DataGrid<PhenotypeProxy>(10,dataGridResources,new EntityProxyKeyProvider<PhenotypeProxy>());
        phenotypeDataGrid.setWidth("100%");
        widget = binder.createAndBindUi(this);
        this.ontologyDisplayDriver.initialize(ontologyEditor);
        phenotypePager.setDisplay(phenotypeDataGrid);
        initPhenotypeDataGrid();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                getUiHandlers().onSelectTerm(selectionModel.getSelectedObject());
            }
        });
	}

    private void initPhenotypeDataGrid() {
        PlaceRequest request = new ParameterizedPlaceRequest(
                NameTokens.phenotype);

        final PlaceRequest ontologyRequest = placeManager.getCurrentPlaceRequest();

        phenotypeDataGrid.setWidth("100%");
        phenotypeDataGrid.setEmptyTableWidget(new Label("No Records found"));

        phenotypeDataGrid.addColumn(new PhenotypeListDataGridColumns.NameColumn(
                placeManager, request), "Name");

        phenotypeDataGrid.addColumn(new PhenotypeListDataGridColumns.ExperimentColumn(), "Experiment");
        phenotypeDataGrid.addColumn(new Column<PhenotypeProxy,String[]>(new HyperlinkCell()){

            @Override
            public String[] getValue(PhenotypeProxy object) {
                String[] hyperlink = new String[2];
                String to = "";
                if (object.getTraitOntologyTerm() != null) {
                    to =  object.getTraitOntologyTerm().getName() + " (" + object.getTraitOntologyTerm().getAcc()+")";
                }
                hyperlink[HyperlinkCell.LINK_INDEX] = "#"+placeManager.buildHistoryToken(ontologyRequest.with("id", object.getToAccession()));
                hyperlink[HyperlinkCell.URL_INDEX] = to;
                return hyperlink;
            }
        },"Trait-Ontology");
        phenotypeDataGrid.addColumn(new PhenotypeListDataGridColumns.ProtocolColumn(),
                "Protocol");
        phenotypeDataGrid.setColumnWidth(0, 15, Style.Unit.PCT);
        phenotypeDataGrid.setColumnWidth(1, 15, Style.Unit.PCT);
        phenotypeDataGrid.setColumnWidth(2, 15, Style.Unit.PCT);
        phenotypeDataGrid.setColumnWidth(3, 55, Style.Unit.PCT);
    }

    @Override
	public Widget asWidget() {
		return widget;
	}

    @Override
    public void initNavigationTree() {
        navTreeContainer.clear();
        navTree = new CellTree(new GraphOntologyTreeViewModel(selectionModel,dataProvider,ontologyCell),null,cellTreeResources);
        navTreeContainer.add(navTree);

    }

    @Override
    public void setRootOntology(GraphTermProxy term) {
        ontologyCategory.setText(term.getName());
    }

    @Override
    public OntologyDisplayDriver getDisplayDriver() {
        return ontologyDisplayDriver;
    }

    @Override
    public HasData<PhenotypeProxy> getPhenotypeDisplay() {
        return phenotypeDataGrid;
    }


    @Override
    public void setPhenotypeCount(Integer count) {
        if (count == null) {
            phenotypeTabHeader.setText("Phenotypes");
        }
        else {
            phenotypeTabHeader.setText("Phenotypes ("+count+")");
        }
    }

    @Override
    public void openNavTreeAndSelectItem(final GraphTermProxy term) {
        if (term == null && selectionModel.getSelectedObject() != null) {
            selectionModel.setSelected(selectionModel.getSelectedObject(),false);
            return;
        }
        else if (term == null) {
            return;
        }
        Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
            public void execute() {
                TreeNode rootNode = navTree.getRootTreeNode();
                selectTreeItem(rootNode,0,term.getPathToRoot());
            }
        });
    }

    private void selectTreeItem(TreeNode node,int index,List<Long> path) {
        if (node == null)
            return;
        for (int i=0;i<node.getChildCount();i++) {
            GraphTerm2TermProxy term2Term = (GraphTerm2TermProxy) node.getChildValue(i);
            if (path.size() > index && term2Term.getNodeId().equals(path.get(index))) {
                index++;
                if (index == path.size()) {
                    selectionModel.setSelected(term2Term,true);
                    scrollIntoView();
                    return;
                }
                selectTreeItem(node.setChildOpen(i,true,true),index,path);
            }
        }
    }

    private void scrollIntoView() {
        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
                GQuery query = $("."+((CellTree.Style)cellTreeResources.cellTreeStyle()).cellTreeSelectedItem());
                query.scrollIntoView();
            }
        });

    }

}

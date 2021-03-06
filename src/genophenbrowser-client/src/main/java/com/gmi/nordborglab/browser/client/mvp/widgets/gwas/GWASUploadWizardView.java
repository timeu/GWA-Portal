package com.gmi.nordborglab.browser.client.mvp.widgets.gwas;

import com.gmi.nordborglab.browser.client.csv.DefaultFileChecker;
import com.gmi.nordborglab.browser.client.events.FileUploadCloseEvent;
import com.gmi.nordborglab.browser.client.events.FileUploadErrorEvent;
import com.gmi.nordborglab.browser.client.events.FileUploadFinishedEvent;
import com.gmi.nordborglab.browser.client.events.FileUploadStartEvent;
import com.gmi.nordborglab.browser.client.ui.fileupload.FileUploadWidget;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.gwtsupercsv.cellprocessor.CellProcessorAdaptor;
import org.gwtsupercsv.cellprocessor.Optional;
import org.gwtsupercsv.cellprocessor.ParseDouble;
import org.gwtsupercsv.cellprocessor.ParseInt;
import org.gwtsupercsv.cellprocessor.ParseLong;
import org.gwtsupercsv.cellprocessor.Trim;
import org.gwtsupercsv.cellprocessor.constraint.Equals;
import org.gwtsupercsv.cellprocessor.constraint.IsIncludedIn;
import org.gwtsupercsv.cellprocessor.ift.CellProcessor;
import org.gwtsupercsv.cellprocessor.ift.StringCellProcessor;
import org.gwtsupercsv.util.CsvContext;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 4:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWASUploadWizardView extends ViewWithUiHandlers<GWASUploadWizardUiHandlers> implements GWASUploadWizardPresenterWidget.MyView {

    interface Binder extends UiBinder<Widget, GWASUploadWizardView> {

    }

    private final Widget widget;

    @UiField
    FileUploadWidget fileUploadWidget;

    private final List<String> allowedExtensions = Lists.newArrayList("application/x-hdf");
    private static List<String> csvMimeTypes = Lists.newArrayList("text/csv", "application/csv", "application/excel", "application/vnd.ms-excel", "application/vnd.msexcel");
    private FileUploadWidget.FileChecker fileChecker;

    public static class ParseNAs extends CellProcessorAdaptor implements StringCellProcessor {

        public ParseNAs() {
        }

        public ParseNAs(CellProcessor next) {
            super(next);
        }

        @Override
        public Object execute(Object value, CsvContext context) {
            validateInputNotNull(value, context);
            if (value instanceof String && ((String) value).equalsIgnoreCase("NA")) {
                return null;
            }
            return next.execute(value, context);
        }
    }


    @Inject
    public GWASUploadWizardView(final Binder binder) {
        widget = binder.createAndBindUi(this);
        initFileUploaWidget();
    }

    private void initFileUploaWidget() {
        CellProcessor[] headerCellProcessors = new CellProcessor[]{
                new Trim(new Equals("chr")), new Trim(new Equals("pos")), new Trim(new IsIncludedIn(new String[]{"score", "pvalue"})),
                (new Trim(new Equals("maf"))),
                (new Trim(new Equals("mac"))),
                (new Trim(new Equals("GVE")))
        };
        CellProcessor[] contentCellProcesors = new CellProcessor[]{
                new Trim(new ParseInt()), new Trim(new ParseLong()), new Trim(new ParseDouble()),
                new Optional(new Trim(new ParseNAs(new ParseDouble()))),
                new Optional(new Trim(new ParseNAs(new ParseInt()))),
                new Optional(new Trim(new ParseNAs(new ParseDouble())))
        };
        List<String> defaultValues = Lists.newArrayList("1", "6083872", "0.023 | 5.1673", "0.3191", "30", "0.0016");
        fileChecker = new DefaultFileChecker(allowedExtensions, csvMimeTypes, headerCellProcessors, contentCellProcesors, defaultValues);
        fileUploadWidget.setFileChecker(fileChecker);
        fileUploadWidget.addHandler(new FileUploadCloseEvent.FileUploadCloseHandler() {
            @Override
            public void onFileUploadClose(FileUploadCloseEvent event) {
                getUiHandlers().onClose();
            }
        }, FileUploadCloseEvent.TYPE);

        fileUploadWidget.addHandler(new FileUploadStartEvent.FileUploadStartHandler() {

            @Override
            public void onFileUploadStart(FileUploadStartEvent event) {
                getUiHandlers().onUploadStart();
            }
        }, FileUploadStartEvent.TYPE);

        fileUploadWidget.addHandler(new FileUploadFinishedEvent.FileUploadFinishedHandler() {
            @Override
            public void onFileUploadFinished(FileUploadFinishedEvent event) {
                getUiHandlers().onUploadEnd();
            }
        }, FileUploadFinishedEvent.TYPE);

        fileUploadWidget.addHandler(new FileUploadErrorEvent.FileUploadErrorHandler() {
            @Override
            public void onFileUploadError(FileUploadErrorEvent event) {
                getUiHandlers().onUploadError(event.getResponseText());
            }
        }, FileUploadErrorEvent.TYPE);

    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setmultipleUpload(boolean multipleUpload) {
        fileUploadWidget.setMultiUpload(multipleUpload);
    }

    @Override
    public void setRestURL(String restURL) {
        fileUploadWidget.setRestURL(GWT.getHostPageBaseURL() + restURL);
    }


}
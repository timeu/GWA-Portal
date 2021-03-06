package com.gmi.nordborglab.browser.client.mvp.diversity;

import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.testutils.PresenterTestBase;
import com.gmi.nordborglab.browser.client.testutils.PresenterTestModule;
import com.gmi.nordborglab.browser.shared.proxy.BreadcrumbItemProxy;
import com.google.inject.Inject;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class DiversityPresenterTest extends PresenterTestBase {

    public static class Module extends PresenterTestModule {

        @Override
        protected void configurePresenterTest() {
        }
    }

    @Inject
    DiversityPresenter presenter;

    @Inject
    DiversityPresenter.MyView view;


    @Captor
    ArgumentCaptor<List<BreadcrumbItemProxy>> captor;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Ignore
    @Test
    public void testBreadcrumbs() {
        PlaceRequest.Builder request = new PlaceRequest.Builder().nameToken("phenotype").with("id", "1");
        given(placeManager.getCurrentPlaceRequest()).willReturn(request.build());
        presenter.onBind();
        presenter.onReset();
        verify(view).clearBreadcrumbs(3);
        verify(view).setTitle("Phenotype");
        verify(view).setBreadcrumbs(0, "ALL", placeManager.buildHistoryToken(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build()));
        verify(view).setBreadcrumbs(1, "Experiment", placeManager.buildHistoryToken(new PlaceRequest.Builder().nameToken(NameTokens.experiment).with("id", "1").build()));
        verify(view).setBreadcrumbs(2, "Phenotype", placeManager.buildHistoryToken(new PlaceRequest.Builder().nameToken("phenotype").with("id", "1").build()));
    }

    @Test
    public void testTitleRequestRequired() {
        assertFalse(presenter.titleUpdateRequired(null, null));
        assertFalse(presenter.titleUpdateRequired(null, 1L));
        assertFalse(presenter.titleUpdateRequired("experiments", null));
        presenter.titleType = "experiments";
        assertTrue(presenter.titleUpdateRequired("phenotype", 1L));
        presenter.titleType = "experiment";
        presenter.titleId = 1L;
        assertFalse(presenter.titleUpdateRequired("experiment", 1L));
        assertTrue(presenter.titleUpdateRequired("experiment", 2L));
        assertTrue(presenter.titleUpdateRequired("phenotype", 2L));
        assertTrue(presenter.titleUpdateRequired("phenotype", 1L));

    }


}

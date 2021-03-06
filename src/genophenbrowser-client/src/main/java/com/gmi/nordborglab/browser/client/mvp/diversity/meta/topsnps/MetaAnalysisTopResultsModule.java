package com.gmi.nordborglab.browser.client.mvp.diversity.meta.topsnps;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class MetaAnalysisTopResultsModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(MetaAnalysisTopResultsPresenter.class,
                MetaAnalysisTopResultsPresenter.MyView.class, MetaAnalysisTopResultsView.class,
                MetaAnalysisTopResultsPresenter.MyProxy.class);
    }
}

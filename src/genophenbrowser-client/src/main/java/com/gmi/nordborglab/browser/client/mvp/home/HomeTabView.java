package com.gmi.nordborglab.browser.client.mvp.home;

import com.gmi.nordborglab.browser.client.ui.BaseTabContainerView;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 24.06.13
 * Time: 14:26
 * To change this template use File | Settings | File Templates.
 */
public class HomeTabView extends BaseTabContainerView implements HomeTabPresenter.MyView {


    interface Binder extends UiBinder<Widget, HomeTabView> {

    }

    private final Widget widget;


    @Inject
    public HomeTabView(final Binder binder) {
        widget = binder.createAndBindUi(this);
        bindSlot(HomeTabPresenter.SLOT_CONTENT, tabContainer.getPanelContent());
    }

    @Override
    public Widget asWidget() {
        return widget;
    }


}
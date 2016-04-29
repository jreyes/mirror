package com.vaporwarecorp.mirror.feature;

import com.robopupu.api.feature.Feature;
import com.robopupu.api.mvp.Presenter;
import com.robopupu.api.plugin.PlugInterface;
import com.robopupu.api.util.Params;

@PlugInterface
public interface MainFeature extends Feature {
// -------------------------- OTHER METHODS --------------------------

    void displayView();

    void hideCurrentPresenter();

    void hideView();

    void onApplicationReady();

    void showPresenter(Class<? extends Presenter> presenterClass, Params... params);
}

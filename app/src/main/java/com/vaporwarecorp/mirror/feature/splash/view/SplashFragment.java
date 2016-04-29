package com.vaporwarecorp.mirror.feature.splash.view;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.feature.common.view.FullscreenFragment;
import com.vaporwarecorp.mirror.feature.splash.presenter.SplashPresenter;

@Plugin
public class SplashFragment extends FullscreenFragment<SplashPresenter> implements SplashView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    SplashPresenter mPresenter;

// --------------------------- CONSTRUCTORS ---------------------------

    @Provides(SplashView.class)
    public SplashFragment() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PresentedView ---------------------

    @Override
    public SplashPresenter getPresenter() {
        return mPresenter;
    }
}

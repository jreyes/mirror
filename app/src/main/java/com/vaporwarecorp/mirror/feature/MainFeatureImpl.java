package com.vaporwarecorp.mirror.feature;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.feature.AbstractFeature;
import com.robopupu.api.mvp.Presenter;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.util.Params;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.component.ForecastManager;
import com.vaporwarecorp.mirror.component.PluginFeatureManager;
import com.vaporwarecorp.mirror.component.ProximityManager;
import com.vaporwarecorp.mirror.feature.main.view.MainView;
import com.vaporwarecorp.mirror.feature.splash.presenter.SplashPresenter;

@Plugin
public class MainFeatureImpl extends AbstractFeature implements MainFeature {
// ------------------------------ FIELDS ------------------------------

    @Plug
    EventManager mEventManager;
    @Plug
    PluginFeatureManager mFeatureManager;
    @Plug
    ForecastManager mForecastManager;
    @Plug
    ProximityManager mProximityManager;
    @Plug
    MainView mView;

    private Class<? extends Presenter> mCurrentPresenterClass;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(MainFeature.class)
    public MainFeatureImpl() {
        super(MainScope.class, true);
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface MainFeature ---------------------

    @Override
    public void displayView() {
        hideCurrentPresenter();
        mView.displayView();
    }

    @Override
    public void hideCurrentPresenter() {
        if (mCurrentPresenterClass == null) {
            return;
        }

        hideView(mCurrentPresenterClass, false, null);
        mCurrentPresenterClass = null;
    }

    @Override
    public void hideView() {
        mView.hideView();
    }

    @Override
    public void onApplicationReady() {
        hideCurrentPresenter();
        mProximityManager.startProximityDetection();
        mForecastManager.startReceiver();
    }

    @Override
    public void showPresenter(Class<? extends Presenter> presenterClass, Params... params) {
        mCurrentPresenterClass = presenterClass;
        showView(mCurrentPresenterClass, false, params);
    }

    @Override
    protected void onStart() {
        super.onStart();
        showPresenter(SplashPresenter.class);
    }

    @Override
    protected void onStop() {
        mProximityManager.stopProximityDetection();
        super.onStop();
    }
}

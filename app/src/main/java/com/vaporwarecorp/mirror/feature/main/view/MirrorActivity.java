package com.vaporwarecorp.mirror.feature.main.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.View;
import android.view.WindowManager;
import com.hound.android.fd.Houndify;
import com.robopupu.api.feature.FeatureContainer;
import com.robopupu.api.mvp.PluginActivity;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.PluginFeatureManager;
import com.vaporwarecorp.mirror.component.forecast.ForecastView;
import com.vaporwarecorp.mirror.component.forecast.model.Forecast;
import com.vaporwarecorp.mirror.feature.MainFeature;
import com.vaporwarecorp.mirror.feature.MainScope;
import com.vaporwarecorp.mirror.feature.main.presenter.MainPresenter;
import com.vaporwarecorp.mirror.util.FullScreenActivityUtil;

@Plugin
public class MirrorActivity extends PluginActivity<MainPresenter> implements MainView {
// ------------------------------ FIELDS ------------------------------

    @Plug(MirrorAppScope.class)
    MainFeature mFeature;
    @Plug
    PluginFeatureManager mFeatureManager;
    @Plug(MainScope.class)
    MainPresenter mPresenter;

    private View mBackgroundContainer;
    private ForecastView mForecastView;
    private View mOverlayContainer;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface FeatureContainer ---------------------

    @Override
    @IdRes
    public int getContainerViewId() {
        return R.id.fragment_container;
    }

// --------------------- Interface MainView ---------------------

    @Override
    public void displayView() {
        mBackgroundContainer.setVisibility(View.VISIBLE);
        mOverlayContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public FeatureContainer getMainFeatureContainer() {
        return this;
    }

    @Override
    public void hideView() {
        mBackgroundContainer.setVisibility(View.INVISIBLE);
        mOverlayContainer.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setForecast(Forecast forecast) {
        mForecastView.setForecast(forecast);
    }

// --------------------- Interface PresentedView ---------------------

    @Override
    public MainPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Houndify.REQUEST_CODE) {
            mPresenter.processCommand(resultCode, data);
        }
    }

    @Override
    protected void onCreate(final Bundle inState) {
        super.onCreate(inState);
        setContentView(R.layout.activity_mirror);

        mBackgroundContainer = findViewById(R.id.background_container);
        mOverlayContainer = findViewById(R.id.overlay_container);
        mForecastView = (ForecastView) findViewById(R.id.forecast_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onResumeFullScreen();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!mFeature.isStarted()) {
            mFeatureManager.startFeature(this, mFeature);
        }
    }

    private void onResumeFullScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        FullScreenActivityUtil.onResume(this);
    }
}

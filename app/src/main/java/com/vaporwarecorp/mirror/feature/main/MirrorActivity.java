package com.vaporwarecorp.mirror.feature.main;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import com.robopupu.api.feature.FeatureContainer;
import com.robopupu.api.feature.FeatureView;
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
import com.vaporwarecorp.mirror.feature.common.view.MirrorView;
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

    private View mContentContainer;
    private ForecastView mForecastView;
    private View mFullscreenContainer;
    private View mHeaderContainer;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface FeatureContainer ---------------------

    @Override
    @IdRes
    public int getContainerViewId() {
        return R.id.fragment_container;
    }

// --------------------- Interface FeatureTransitionManager ---------------------

    @Override
    public void showView(final FeatureView featureView, final boolean addToBackStack, final String fragmentTag) {
        String tag = fragmentTag;

        if (fragmentTag == null) {
            tag = featureView.getViewTag();
        }

        final FragmentManager manager = getFragmentManager();
        if (manager.findFragmentByTag(tag) == null) {
            final FragmentTransaction transaction = manager.beginTransaction();

            if (featureView instanceof DialogFragment) {
                final DialogFragment dialogFragment = (DialogFragment) featureView;
                transaction.add(dialogFragment, tag);

                if (addToBackStack) {
                    transaction.addToBackStack(tag);
                }
                transaction.commitAllowingStateLoss();
            } else if (featureView instanceof Fragment) {
                final Fragment fragment = (Fragment) featureView;
                if (fragment instanceof MirrorView && ((MirrorView) fragment).isFullscreen()) {
                    transaction.replace(R.id.fullscreen_container, fragment, tag);
                    mFullscreenContainer.setVisibility(View.VISIBLE);
                } else {
                    transaction.replace(R.id.fragment_container, fragment, tag);
                    mFullscreenContainer.setVisibility(View.GONE);
                }
                if (addToBackStack) {
                    transaction.addToBackStack(tag);
                }
                transaction.commit();
            }
        }
    }

// --------------------- Interface MainView ---------------------

    @Override
    public Activity activity() {
        return this;
    }

    @Override
    public void displayView() {
        mHeaderContainer.setVisibility(View.VISIBLE);
        mContentContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public FeatureContainer getMainFeatureContainer() {
        return this;
    }

    @Override
    public void hideView() {
        mHeaderContainer.setVisibility(View.INVISIBLE);
        mContentContainer.setVisibility(View.INVISIBLE);
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

// -------------------------- OTHER METHODS --------------------------

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPresenter.verifyPermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.onViewResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(final Bundle inState) {
        super.onCreate(inState);
        setContentView(R.layout.activity_mirror);

        mHeaderContainer = findViewById(R.id.header_container);
        mContentContainer = findViewById(R.id.content_container);
        mFullscreenContainer = findViewById(R.id.fullscreen_container);
        mForecastView = (ForecastView) findViewById(R.id.forecast_view);
        //findViewById(R.id.spotify).setOnClickListener(v -> mPresenter.startSpotify());
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

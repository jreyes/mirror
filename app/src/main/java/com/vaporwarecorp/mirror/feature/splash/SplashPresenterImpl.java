package com.vaporwarecorp.mirror.feature.splash;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.feature.MainFeature;

import java.util.Random;

import static com.vaporwarecorp.mirror.util.RxUtil.delay;

@Plugin
public class SplashPresenterImpl extends AbstractFeaturePresenter<SplashView> implements SplashPresenter {
// ------------------------------ FIELDS ------------------------------

    private static final String[] SPLASH_COVERS = {
            "http://k39.kn3.net/taringa/2/0/4/3/3/4/63/piledro/A12.gif",
            "http://i.giphy.com/rR2AWZ3ip77r2.gif",
            "http://i.giphy.com/vncgdgPWLwGRi.gif",
            "http://m.popkey.co/76b5e7/NlZaX.gif",
            "http://i.giphy.com/3Ow6njmLYdchW.gif",
            "http://i.giphy.com/tptFQ8QAJYYvu.gif",
            "http://i.giphy.com/pDLNJlazF9ljG.gif"
    };

    @Plug
    AppManager mAppManager;
    @Plug
    MainFeature mMainFeature;
    @Plug
    SplashView mView;

// --------------------------- CONSTRUCTORS ---------------------------

    @Provides(SplashPresenter.class)
    public SplashPresenterImpl() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(SplashView.class);
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewResume(final View view) {
        super.onViewResume(view);
        setPictureUrl(SPLASH_COVERS);
        isApplicationReady();
    }

    @Override
    protected SplashView getViewPlug() {
        return mView;
    }

    private void isApplicationReady() {
        delay(l -> {
            if (mAppManager.isBluetoothAvailable() && mAppManager.isNetworkAvailable() &&
                    mAppManager.isLocationAvailable()) {
                mMainFeature.onApplicationReady();
            } else {
                isApplicationReady();
            }
        }, 20);
    }

    private void setPictureUrl(String[] pictureUrls) {
        mView.setPictureUrl(pictureUrls[new Random().nextInt(pictureUrls.length)]);
    }
}

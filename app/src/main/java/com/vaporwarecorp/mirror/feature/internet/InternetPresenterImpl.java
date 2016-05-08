package com.vaporwarecorp.mirror.feature.internet;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.ResetEvent;

@Plugin
public class InternetPresenterImpl extends AbstractFeaturePresenter<InternetView> implements InternetPresenter {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
    @Plug
    EventManager mEventManager;
    @Plug
    InternetView mView;

// --------------------------- CONSTRUCTORS ---------------------------

    @Provides(InternetPresenter.class)
    public InternetPresenterImpl() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(InternetView.class);
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewResume(View view) {
        super.onViewResume(view);
        mView.setVideo(mAppManager.getLocalAssetPath("videos/aol.mp4"), () -> mEventManager.post(new ResetEvent()));
    }

    @Override
    protected InternetView getViewPlug() {
        return mView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAppManager.refWatcher().watch(this);
    }
}

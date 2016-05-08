package com.vaporwarecorp.mirror.feature.watch;

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
public class WatchCBSPresenterImpl extends AbstractFeaturePresenter<WatchCBSView> implements WatchCBSPresenter {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
    @Plug
    EventManager mEventManager;
    @Plug
    WatchCBSView mView;

// --------------------------- CONSTRUCTORS ---------------------------

    @Provides(WatchCBSPresenter.class)
    public WatchCBSPresenterImpl() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(WatchCBSView.class);
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewResume(View view) {
        super.onViewResume(view);
        String cbsUrl = "http://cbsnewshd-lh.akamaihd.net/i/CBSNHD_7@199302/index_700_av-p.m3u8?sd=10&rebase=on";
        mView.setVideo(cbsUrl, () -> mEventManager.post(new ResetEvent()));
    }

    @Override
    protected WatchCBSView getViewPlug() {
        return mView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAppManager.refWatcher().watch(this);
    }
}

package com.vaporwarecorp.mirror.feature.spotify;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.component.SpotifyManager;
import com.vaporwarecorp.mirror.event.SpotifyPlaybackEvent;
import com.vaporwarecorp.mirror.event.SpotifyTrackEvent;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

@Plugin
public class SpotifyPresenterImpl extends AbstractFeaturePresenter<SpotifyView> implements SpotifyPresenter {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
    @Plug
    EventManager mEventManager;
    @Plug
    SpotifyManager mSpotifyManger;
    @Plug
    SpotifyView mView;

// --------------------------- CONSTRUCTORS ---------------------------

    @Provides(SpotifyPresenter.class)
    public SpotifyPresenterImpl() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(SpotifyView.class);
    }

// --------------------- Interface ViewObserver ---------------------

    @SuppressWarnings("unchecked")
    @Override
    public void onViewStart(View view) {
        super.onViewStart(view);

        List<String> trackIds = (List<String>) getParams().get(TRACK_IDS);
        mSpotifyManger.play(trackIds);
    }

// -------------------------- OTHER METHODS --------------------------

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(SpotifyTrackEvent event) {
        mView.updateMetadata(event.getTrack());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(SpotifyPlaybackEvent event) {
        mView.updateProgress(event.getEventType(), event.getLastPosition());
    }

    @Override
    protected SpotifyView getViewPlug() {
        return mView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAppManager.refWatcher().watch(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mEventManager.register(this);
    }

    @Override
    protected void onStop() {
        mSpotifyManger.stop();
        mEventManager.unregister(this);
        super.onStop();
    }
}

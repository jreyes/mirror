package com.vaporwarecorp.mirror.feature.spotify;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.SpotifyManager;

import java.util.List;

@Plugin
public class SpotifyPresenterImpl extends AbstractFeaturePresenter<SpotifyView> implements SpotifyPresenter {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
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
    public void onViewResume(View view) {
        super.onViewResume(view);

        List<String> trackIds = (List<String>) getParams().get(TRACK_IDS);
        mSpotifyManger.play(trackIds, tracks -> mView.updateQueue(tracks));
    }

    @Override
    public void onViewStop(View view) {
        mSpotifyManger.stop();
        super.onViewStop(view);
    }

    //mView.updateMetadata(event.getTrack());
    //mView.updateProgress(event.getEventType(), event.getLastPosition());
    @Override
    protected SpotifyView getViewPlug() {
        return mView;
    }
}

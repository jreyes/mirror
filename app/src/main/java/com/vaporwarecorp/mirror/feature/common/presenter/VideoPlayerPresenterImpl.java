package com.vaporwarecorp.mirror.feature.common.presenter;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.ResetEvent;
import com.vaporwarecorp.mirror.feature.common.view.VideoPlayerView;

@Plugin
public class VideoPlayerPresenterImpl
        extends AbstractFeaturePresenter<VideoPlayerView>
        implements VideoPlayerPresenter {
// ------------------------------ FIELDS ------------------------------

    @Plug
    EventManager mEventManager;
    @Plug
    VideoPlayerView mView;

// --------------------------- CONSTRUCTORS ---------------------------

    @Provides(VideoPlayerPresenter.class)
    public VideoPlayerPresenterImpl() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(VideoPlayerView.class);
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewResume(View view) {
        super.onViewResume(view);
        final String videoUrl = getParams().getString(VIDEO_URL);
        mView.setVideo(videoUrl, () -> mEventManager.post(new ResetEvent()));
    }

    @Override
    protected VideoPlayerView getViewPlug() {
        return mView;
    }
}

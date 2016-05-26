package com.vaporwarecorp.mirror.feature.common.presenter;

import com.robopupu.api.feature.FeaturePresenter;
import com.robopupu.api.plugin.PlugInterface;

@PlugInterface
public interface YoutubePresenter extends FeaturePresenter {
// ------------------------------ FIELDS ------------------------------

    String YOUTUBE_VIDEO_ID = "YOUTUBE_VIDEO_ID";

    interface Listener {
        void onCompleted();
    }
}

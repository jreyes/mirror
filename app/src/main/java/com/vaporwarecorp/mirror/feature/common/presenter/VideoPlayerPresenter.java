package com.vaporwarecorp.mirror.feature.common.presenter;

import com.robopupu.api.feature.FeaturePresenter;
import com.robopupu.api.plugin.PlugInterface;

@PlugInterface
public interface VideoPlayerPresenter extends FeaturePresenter {
// ------------------------------ FIELDS ------------------------------

    String VIDEO_URL = "VIDEO_URL";

    interface Listener {
        void onCompleted();
    }
}

package com.vaporwarecorp.mirror.feature.common.presenter;

import com.robopupu.api.feature.FeaturePresenter;

public interface VideoPlayerPresenter extends FeaturePresenter {
    interface Listener {
        void onCompleted();
    }
}

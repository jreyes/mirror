package com.vaporwarecorp.mirror.feature.common.presenter;

import com.robopupu.api.feature.FeaturePresenter;

public interface YoutubePresenter extends FeaturePresenter {
    interface Listener {
        void onCompleted();
    }
}

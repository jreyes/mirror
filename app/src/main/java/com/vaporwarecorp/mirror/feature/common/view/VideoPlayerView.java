package com.vaporwarecorp.mirror.feature.common.view;

import com.robopupu.api.mvp.View;
import com.vaporwarecorp.mirror.feature.common.presenter.VideoPlayerPresenter;

public interface VideoPlayerView extends View {
// -------------------------- OTHER METHODS --------------------------

    void setVideo(String videoPath, VideoPlayerPresenter.Listener listener);
}

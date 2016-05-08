package com.vaporwarecorp.mirror.feature.common.view;

import com.robopupu.api.mvp.View;
import com.vaporwarecorp.mirror.feature.common.presenter.YoutubePresenter.Listener;

public interface YoutubeView extends View {
// -------------------------- OTHER METHODS --------------------------

    void setYoutubeVideo(String youtubeVideoId, Listener listener);
}

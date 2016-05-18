package com.vaporwarecorp.mirror.feature.common.view;

import com.robopupu.api.mvp.View;

public interface FullscreenView extends View {
// -------------------------- OTHER METHODS --------------------------

    boolean isLandscape();

    void setPictureUrl(String pictureUrl);
}

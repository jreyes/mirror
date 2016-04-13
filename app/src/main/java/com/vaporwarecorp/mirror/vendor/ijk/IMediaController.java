package com.vaporwarecorp.mirror.vendor.ijk;

import android.view.View;
import android.widget.MediaController;

public interface IMediaController {
// -------------------------- OTHER METHODS --------------------------

    void hide();

    boolean isShowing();

    void setAnchorView(View view);

    void setEnabled(boolean enabled);

    void setMediaPlayer(MediaController.MediaPlayerControl player);

    void show();

    void show(int timeout);

    void showOnce(View view);
}

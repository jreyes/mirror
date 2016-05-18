package com.vaporwarecorp.mirror.feature.common.view;

import com.robopupu.api.mvp.View;
import com.spotify.sdk.android.player.PlayerNotificationCallback.EventType;

import java.util.List;

public interface PlaybackOverlayView extends View {
// -------------------------- OTHER METHODS --------------------------

    void updateMetadata(Object item);

    void updateProgress(EventType eventType, int lastPosition);

    void updateQueue(List items);
}

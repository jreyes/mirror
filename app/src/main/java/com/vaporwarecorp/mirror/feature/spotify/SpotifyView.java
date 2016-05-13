package com.vaporwarecorp.mirror.feature.spotify;

import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.PlugInterface;
import com.spotify.sdk.android.player.PlayerNotificationCallback.EventType;
import kaaes.spotify.webapi.android.models.Track;

import java.util.ArrayList;

@PlugInterface
public interface SpotifyView extends View {
// -------------------------- OTHER METHODS --------------------------

    void play(ArrayList<String> trackIds);

    void updateMetadata(Track track);

    void updateProgress(EventType eventType, int lastPosition);
}

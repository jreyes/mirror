package com.vaporwarecorp.mirror.component;

import android.app.Activity;
import android.content.Intent;
import com.robopupu.api.component.Manager;
import com.robopupu.api.plugin.PlugInterface;
import kaaes.spotify.webapi.android.models.Track;

import java.util.List;

@PlugInterface
public interface SpotifyManager extends Manager {
// ------------------------------ FIELDS ------------------------------

    String CLIENT_ID = "SpotifyClientId";
    String CLIENT_REDIRECT_URI = "SpotifyRedirectUri";
    int REQUEST_CODE = 1337;

// -------------------------- OTHER METHODS --------------------------

    void authenticate(Activity activity);

    void play(List<String> trackUris, Listener listener);

    void processAuthentication(int resultCode, Intent data);

    void stop();

    interface Listener {
        void onTracksLoaded(List<Track> tracks);
    }
}

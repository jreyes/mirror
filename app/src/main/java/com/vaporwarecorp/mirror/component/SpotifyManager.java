/*
 * Copyright 2016 Johann Reyes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

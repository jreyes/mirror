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
package com.vaporwarecorp.mirror.feature.spotify;

import com.robopupu.api.plugin.PlugInterface;
import com.vaporwarecorp.mirror.component.WebAuthentication;
import com.vaporwarecorp.mirror.feature.common.MirrorManager;
import kaaes.spotify.webapi.android.models.Track;

import java.util.List;

@PlugInterface
public interface SpotifyManager extends MirrorManager, WebAuthentication {
// ------------------------------ FIELDS ------------------------------

    int REQUEST_CODE = 1337;

// -------------------------- OTHER METHODS --------------------------

    void getNewReleases(NewReleasesCallback callback);

    void pausePlaying();

    void play(List<String> trackUris, Listener listener);

    void resumePlaying();

    interface Listener {
        void onTrackUpdate(Track track);

        void onPlaylistEnd();
    }

    interface NewReleasesCallback {
        void onComplete(List<String> trackUris);
    }
}

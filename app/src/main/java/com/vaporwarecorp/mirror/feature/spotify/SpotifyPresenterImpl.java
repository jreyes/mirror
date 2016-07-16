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

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.ResetEvent;
import kaaes.spotify.webapi.android.models.Track;

import java.util.List;

@Plugin
@Provides(SpotifyPresenter.class)
public class SpotifyPresenterImpl extends AbstractFeaturePresenter<SpotifyView> implements SpotifyPresenter {
// ------------------------------ FIELDS ------------------------------

    @Plug
    EventManager mEventManager;
    @Plug
    SpotifyManager mSpotifyManger;
    @Plug
    SpotifyView mView;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(SpotifyView.class);
    }

// --------------------- Interface SpotifyPresenter ---------------------

    @Override
    public void pausePlaying() {
        mSpotifyManger.pausePlaying();
    }

    @Override
    public void resumePlaying() {
        mSpotifyManger.resumePlaying();
    }

// --------------------- Interface ViewObserver ---------------------

    @SuppressWarnings("unchecked")
    @Override
    public void onViewResume(View view) {
        super.onViewResume(view);

        final List<String> trackIds = (List<String>) getParams().get(TRACK_IDS);
        mSpotifyManger.play(trackIds, new SpotifyManager.Listener() {
            @Override
            public void onTrackUpdate(Track track) {
                mView.updateTrack(track);
            }

            @Override
            public void onPlaylistEnd() {
                mEventManager.post(new ResetEvent(SpotifyPresenter.class));
            }
        });
    }

    @Override
    public void onViewStop(View view) {
        mSpotifyManger.onViewStop();
        super.onViewStop(view);
    }

    @Override
    protected SpotifyView getViewPlug() {
        return mView;
    }
}

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
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.SpotifyManager;

import java.util.List;

@Plugin
public class SpotifyPresenterImpl extends AbstractFeaturePresenter<SpotifyView> implements SpotifyPresenter {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
    @Plug
    SpotifyManager mSpotifyManger;
    @Plug
    SpotifyView mView;

// --------------------------- CONSTRUCTORS ---------------------------

    @Provides(SpotifyPresenter.class)
    public SpotifyPresenterImpl() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(SpotifyView.class);
    }

// --------------------- Interface ViewObserver ---------------------

    @SuppressWarnings("unchecked")
    @Override
    public void onViewResume(View view) {
        super.onViewResume(view);

        List<String> trackIds = (List<String>) getParams().get(TRACK_IDS);
        mSpotifyManger.play(trackIds, tracks -> mView.updateQueue(tracks));
    }

    @Override
    public void onViewStop(View view) {
        mSpotifyManger.stop();
        super.onViewStop(view);
    }

    //mView.updateMetadata(event.getTrack());
    //mView.updateProgress(event.getEventType(), event.getLastPosition());
    @Override
    protected SpotifyView getViewPlug() {
        return mView;
    }
}

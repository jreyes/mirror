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
package com.vaporwarecorp.mirror.feature.common.presenter;

import android.content.Intent;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.ResetEvent;
import com.vaporwarecorp.mirror.feature.common.view.YoutubeView;

import java.util.HashMap;
import java.util.Map;

@Plugin
@Provides(YoutubePresenter.class)
public class YoutubePresenterImpl extends AbstractFeaturePresenter<YoutubeView> implements YoutubePresenter {
// ------------------------------ FIELDS ------------------------------

    private static final String YOUTUBE_URL = "https://youtu.be/%s";

    @Plug
    EventManager mEventManager;
    @Plug
    YoutubeView mView;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(YoutubeView.class);
    }

// --------------------- Interface Shareable ---------------------

    @Override
    public Map<String, Object> content() {
        final String youtubeVideoId = getParams().getString(YOUTUBE_VIDEO_ID);
        final Map<String, Object> content = new HashMap<>();
        content.put(ACTION, Intent.ACTION_VIEW);
        content.put(URL, String.format(YOUTUBE_URL, youtubeVideoId));
        return content;
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewResume(View view) {
        super.onViewResume(view);
        final String youtubeVideoId = getParams().getString(YOUTUBE_VIDEO_ID);
        mView.setYoutubeVideo(youtubeVideoId, () -> mEventManager.post(new ResetEvent(YoutubePresenter.class)));
    }

    @Override
    protected YoutubeView getViewPlug() {
        return mView;
    }
}

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
package com.vaporwarecorp.mirror.feature.watch;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.ResetEvent;

@Plugin
@Provides(WatchCBSPresenter.class)
public class WatchCBSPresenterImpl extends AbstractFeaturePresenter<WatchCBSView> implements WatchCBSPresenter {
// ------------------------------ FIELDS ------------------------------

    private static final String VIDEO_URL =
            "http://cbsnewshd-lh.akamaihd.net/i/CBSNHD_7@199302/index_700_av-p.m3u8?sd=10&rebase=on";

    @Plug
    EventManager mEventManager;
    @Plug
    WatchCBSView mView;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(WatchCBSView.class);
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewResume(View view) {
        super.onViewResume(view);
        mView.setVideo(VIDEO_URL, () -> mEventManager.post(new ResetEvent(WatchCBSPresenter.class)));
    }

    @Override
    protected WatchCBSView getViewPlug() {
        return mView;
    }
}

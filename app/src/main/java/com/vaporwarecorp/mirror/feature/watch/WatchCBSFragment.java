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
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.feature.common.view.VideoPlayerFragment;

@Plugin
@Provides(WatchCBSView.class)
public class WatchCBSFragment extends VideoPlayerFragment<WatchCBSPresenter> implements WatchCBSView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    WatchCBSPresenter mPresenter;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface MirrorView ---------------------

    @Override
    public boolean isFullscreen() {
        return false;
    }

    @Override
    public void onCenterDisplay() {
    }

    @Override
    public void onSideDisplay() {
    }

    @Override
    public Class presenterClass() {
        return WatchCBSPresenter.class;
    }

// --------------------- Interface PresentedView ---------------------

    @Override
    public WatchCBSPresenter getPresenter() {
        return mPresenter;
    }
}

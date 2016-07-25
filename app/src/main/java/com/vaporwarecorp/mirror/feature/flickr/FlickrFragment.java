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
package com.vaporwarecorp.mirror.feature.flickr;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.mvp.Presenter;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.feature.common.view.BrowserFragment;
import timber.log.Timber;

@Plugin
@Provides(FlickrView.class)
public class FlickrFragment extends BrowserFragment<FlickrPresenter> implements FlickrView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    FlickrPresenter mPresenter;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface FlickrView ---------------------

    @Override
    public void loadUrl(String url) {
        Timber.d("loading %s", url);
        getWebView().loadUrl(url);
    }

// --------------------- Interface MirrorView ---------------------

    @Override
    public Class<? extends Presenter> presenterClass() {
        return FlickrPresenter.class;
    }

// --------------------- Interface PresentedView ---------------------

    @Override
    public FlickrPresenter getPresenter() {
        return mPresenter;
    }
}

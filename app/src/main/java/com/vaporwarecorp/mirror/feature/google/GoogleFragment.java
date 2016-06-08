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
package com.vaporwarecorp.mirror.feature.google;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.feature.common.view.BrowserFragment;
import timber.log.Timber;

@Plugin
@Provides(GoogleView.class)
public class GoogleFragment extends BrowserFragment<GooglePresenter> implements GoogleView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    GooglePresenter mPresenter;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PresentedView ---------------------

    @Override
    public GooglePresenter getPresenter() {
        return mPresenter;
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public void onResume() {
        super.onResume();

        WebView webView = getWebView();
        if (webView != null && webView.getOriginalUrl() == null) {
            Timber.d("Loading google");
            //webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl("http://google.com");
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
        }
    }
}

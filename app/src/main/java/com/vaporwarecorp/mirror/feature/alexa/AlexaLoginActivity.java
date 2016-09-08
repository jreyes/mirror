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
package com.vaporwarecorp.mirror.feature.alexa;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.vaporwarecorp.mirror.BuildConfig;
import com.vaporwarecorp.mirror.R;
import timber.log.Timber;

public class AlexaLoginActivity extends Activity {
// ------------------------------ FIELDS ------------------------------

    private static final int RESULT_LOGIN = 7887;

    private WebView mWebView;
    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (BuildConfig.DEBUG) {
                Timber.i(url);
            }
            if (url.startsWith("http") || url.startsWith("https")) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivityForResult(i, RESULT_LOGIN);

            return true;
        }
    };

// -------------------------- OTHER METHODS --------------------------

    @Override
    public void finish() {
        if (mWebView != null) {
            mWebView.clearHistory();
            mWebView.clearCache(true);
            mWebView.loadUrl("about:blank");
            mWebView.pauseTimers();
            mWebView = null;
        }
        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOGIN) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alexa_login);

        // Get the intent that started this activity
        Uri data = getIntent().getData();
        if (data != null) {
            mWebView = (WebView) findViewById(R.id.webview);
            mWebView.setWebViewClient(mWebViewClient);
            mWebView.loadUrl(data.toString());
        } else {
            finish();
        }
    }
}
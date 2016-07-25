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
package com.vaporwarecorp.mirror.feature.proxy;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.squareup.okhttp.Request;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.feature.common.AbstractMirrorManager;
import com.vaporwarecorp.mirror.util.JsonUtil;
import fi.iki.elonen.NanoHTTPD;
import timber.log.Timber;

import java.io.IOException;

import static fi.iki.elonen.NanoHTTPD.Response.Status.OK;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(ProxyManager.class)
public class ProxyManagerImpl extends AbstractMirrorManager implements ProxyManager {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;

    private ProxyServer mProxyServer;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface MirrorManager ---------------------

    @Override
    public void onFeaturePause() {
        Timber.d("Stopping the Proxy server");
        mProxyServer.stop();
    }

    @Override
    public void onFeatureResume() {
        try {
            Timber.d("Starting the Proxy server");
            mProxyServer.start();
        } catch (IOException e) {
            Timber.e(e, "Error starting the Proxy server");
        }
    }

    @Override
    public void onFeatureStart() {
        Timber.d("Creating the Proxy server");
        mProxyServer = new ProxyServer();
    }

    @Override
    public void onFeatureStop() {
        Timber.d("Destroying the Proxy server");
        mProxyServer = null;
    }

    private class ProxyServer extends NanoHTTPD {
        private static final String MIME_JSON = "application/json";

        ProxyServer() {
            super(34000);
        }

        @Override
        public Response serve(IHTTPSession session) {
            final String uri = session.getUri().substring(1);
            if (Method.GET.equals(session.getMethod()) && "proxy".equals(uri)) {
                return get(session.getParms().get("url"));
            }
            return newFixedLengthResponse(null);
        }

        private Response get(String url) {
            try {
                Timber.d("loading url %s", url);
                final Request request = new Request.Builder().url(url).build();
                final String content = mAppManager.okHttpClient().newCall(request).execute().body().string();
                final String jsonContent = JsonUtil.toString(JsonUtil.toJsonNode(content));
                final Response response = newFixedLengthResponse(OK, MIME_JSON, jsonContent);
                response.addHeader("Access-Control-Allow-Origin", "*");
                return response;
            } catch (IOException e) {
                Timber.e(e, "Error loading the url %s", url);
                return newFixedLengthResponse(null);
            }
        }
    }
}

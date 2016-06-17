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
package com.vaporwarecorp.mirror.component.configuration;

import android.content.Context;
import android.text.TextUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.vaporwarecorp.mirror.component.ConfigurationManager.Listener;
import fi.iki.elonen.NanoHTTPD;
import org.apache.commons.io.IOUtils;
import timber.log.Timber;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.vaporwarecorp.mirror.util.JsonUtil.toJsonNode;
import static fi.iki.elonen.NanoHTTPD.Response.Status.NOT_FOUND;
import static fi.iki.elonen.NanoHTTPD.Response.Status.OK;

public class WebServer extends NanoHTTPD {
// ------------------------------ FIELDS ------------------------------

    private final static String JAVASCRIPT_APP = "app.js";
    private final static String MODEL_PLACEHOLDER = ",\"model\":";
    private final static String MODULES_PLACEHOLDER = "/* {{modules}} */";

    private Map<String, String> mCache;
    private List<Configuration> mConfigurations;
    private Context mContext;
    private Listener mListener;

// --------------------------- CONSTRUCTORS ---------------------------

    public WebServer(Context context) {
        super(8080);
        mContext = context;
        mCache = new HashMap<>();
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public Response serve(IHTTPSession session) {
        final String uri = session.getUri().substring(1);
        if (Method.HEAD.equals(session.getMethod())) {
            if (mListener != null) {
                mListener.onExit();
            }
        } else if (Method.POST.equals(session.getMethod())) {
            for (Configuration configuration : mConfigurations) {
                if (configuration.getClass().getName().startsWith(uri)) {
                    return updateConfiguration(session, configuration);
                }
            }
            return newFixedLengthResponse(null);
        } else if (uri.endsWith(".ico")) {
            return getNotFoundResponse();
        } else if (uri.endsWith(".js")) {
            String template = read("configuration/" + uri);
            if (uri.endsWith(JAVASCRIPT_APP)) {
                final List<String> modules = new LinkedList<>();
                for (Configuration configuration : mConfigurations) {
                    final String jsonString = read(configuration.getJsonConfiguration());
                    modules.add(new StringBuilder(jsonString).insert(jsonString.lastIndexOf('}'),
                            MODEL_PLACEHOLDER + configuration.getJsonValues()).toString());
                }
                template = template.replace(MODULES_PLACEHOLDER, TextUtils.join(",", modules));
            }
            return newFixedLengthResponse(OK, "application/javascript", template);
        } else if (uri.endsWith(".css")) {
            return newFixedLengthResponse(OK, "text/css", read("configuration/" + uri));
        }
        return newFixedLengthResponse(read("configuration/index.html"));
    }

    public void start(List<Configuration> configurations, Listener listener) {
        try {
            mConfigurations = configurations;
            mListener = listener;
            super.start();
        } catch (IOException ignored) {
            mConfigurations.clear();
            mListener = null;
        }
    }

    public void stop() {
        mConfigurations.clear();
        mListener = null;
        super.stop();
    }

    private Response getNotFoundResponse() {
        return newFixedLengthResponse(NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.");
    }

    private String getPostData(IHTTPSession session) {
        try {
            final HashMap<String, String> json = new HashMap<>();
            session.parseBody(json);
            return json.get("postData");
        } catch (IOException | ResponseException ignored) {
        }
        return "";
    }

    private String read(String filePath) {
        if (mCache.containsKey(filePath)) {
            return mCache.get(filePath);
        }

        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(mContext.getAssets().open(filePath), writer, "UTF-8");

            final String content = writer.toString();
            mCache.put(filePath, content);
            return content;
        } catch (IOException e) {
            Timber.e(e, "Can't read file %s", filePath);
            return "";
        }
    }

    private Response updateConfiguration(IHTTPSession session, Configuration configuration) {
        final String json = getPostData(session);
        final JsonNode jsonNode = toJsonNode(json).findValue("formItems");
        configuration.updateConfiguration(jsonNode);
        return newFixedLengthResponse(null);
    }
}

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

import android.text.TextUtils;
import com.squareup.okhttp.Request;
import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.util.JsonUtil;
import fi.iki.elonen.NanoFileUpload;
import fi.iki.elonen.NanoHTTPD;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import timber.log.Timber;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.vaporwarecorp.mirror.util.JsonUtil.toJsonNode;
import static fi.iki.elonen.NanoHTTPD.Response.Status.*;

public class WebServer extends NanoHTTPD {
// ------------------------------ FIELDS ------------------------------

    private static final String JAVASCRIPT_APP = "app.js";
    private static final String MIME_JSON = "application/json";
    private static final String MODEL_PLACEHOLDER = ",\"model\":";
    private static final String MODULES_PLACEHOLDER = "/* {{modules}} */";

    private AppManager mAppManager;
    private Map<String, String> mCache;
    private List<Configuration> mConfigurations;
    private NanoFileUpload mUploader;

// --------------------------- CONSTRUCTORS ---------------------------

    public WebServer(AppManager appManager) {
        super(4000);
        mAppManager = appManager;
        mCache = new HashMap<>();
        mUploader = new NanoFileUpload(new DiskFileItemFactory());
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public Response serve(IHTTPSession session) {
        final String uri = session.getUri().substring(1);
        if (Method.GET.equals(session.getMethod()) && "proxy".equals(uri)) {
            return get(session.getParms().get("url"));
        } else if (NanoFileUpload.isMultipartContent(session)) {
            try {
                List<FileItem> files = mUploader.parseRequest(session);
                if (files != null && files.size() > 0) {
                    return newFixedLengthResponse(saveFileItemToDisk(files.get(0)));
                }
            } catch (FileUploadException e) {
                Timber.e(e, e.getMessage());
                return getInternalErrorResponse(e.getMessage());
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

    public void start(List<Configuration> configurations) {
        try {
            mConfigurations = configurations;
            super.start();
        } catch (IOException ignored) {
            mConfigurations.clear();
        }
    }

    public void stop() {
        mConfigurations.clear();
        super.stop();
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

    private File getFile(FileItem fileItem) {
        Storage storage;
        if (SimpleStorage.isExternalStorageWritable()) {
            storage = SimpleStorage.getExternalStorage();
        } else {
            storage = SimpleStorage.getInternalStorage(mAppManager.getAppContext());
        }
        storage.createDirectory("configuration/uploads");
        storage.createFile("configuration/uploads", fileItem.getName(), new byte[0]);
        return storage.getFile("configuration/uploads", fileItem.getName());
    }

    private Response getInternalErrorResponse(String message) {
        return newFixedLengthResponse(INTERNAL_ERROR, MIME_PLAINTEXT, "Internal error " + message);
    }

    private Response getNotFoundResponse() {
        return newFixedLengthResponse(NOT_FOUND, MIME_PLAINTEXT, "Error 404, file not found.");
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
            IOUtils.copy(mAppManager.getAppContext().getAssets().open(filePath), writer, "UTF-8");

            final String content = writer.toString();
            mCache.put(filePath, content);
            return content;
        } catch (IOException e) {
            Timber.e(e, "Can't read file %s", filePath);
            return "";
        }
    }

    private String saveFileItemToDisk(FileItem fileItem) {
        Timber.d("Received file %s", fileItem.getName());
        try {
            File file = getFile(fileItem);
            Timber.d("writing file %s", file.getAbsolutePath());
            fileItem.write(getFile(fileItem));
            return file.getAbsolutePath();
        } catch (Exception e) {
            Timber.e(e, e.getMessage());
            return "";
        }
    }

    private Response updateConfiguration(IHTTPSession session, Configuration configuration) {
        try {
            final String json = getPostData(session);
            configuration.updateConfiguration(toJsonNode(json));
            return newFixedLengthResponse(null);
        } catch (RuntimeException e) {
            Timber.e(e, e.getMessage());
            return newFixedLengthResponse(INTERNAL_ERROR, MIME_PLAINTEXT, e.getMessage());
        }
    }
}

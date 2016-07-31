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

import android.net.Uri;
import com.fasterxml.jackson.databind.JsonNode;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.ConfigurationManager;
import com.vaporwarecorp.mirror.feature.common.AbstractMirrorManager;

import static com.vaporwarecorp.mirror.util.JsonUtil.createTextNode;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(FlickrManager.class)
public class FlickrManagerImpl extends AbstractMirrorManager implements FlickrManager {
// ------------------------------ FIELDS ------------------------------

    private static final String PREF = FlickrManager.class.getName();
    private static final String PREF_ANIMATION_SPEED = PREF + ".PREF_ANIMATION_SPEED";
    private static final int PREF_ANIMATION_SPEED_DEFAULT = 1;
    private static final String PREF_FLICKR_ID = PREF + ".PREF_FLICKR_ID";
    private static final String PREF_FLICKR_ID_DEFAULT = "";
    private static final String PREF_UPDATE_INTERVAL = PREF + ".PREF_UPDATE_INTERVAL";
    private static final int PREF_UPDATE_INTERVAL_DEFAULT = 10;

    @Plug
    ConfigurationManager mConfigurationManager;

    private int mAnimationSpeed;
    private String mFlickrId;
    private int mUpdateInterval;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Configuration ---------------------

    @Override
    public String getJsonConfiguration() {
        return "configuration/json/flickr.json";
    }

    @Override
    public String getJsonValues() {
        return createTextNode("flickrId", mFlickrId)
                .put("animationSpeed", mAnimationSpeed)
                .put("updateInterval", mUpdateInterval)
                .toString();
    }

    @Override
    public void updateConfiguration(JsonNode jsonNode) {
        mConfigurationManager.updateString(PREF_FLICKR_ID, jsonNode, "flickrId");
        mConfigurationManager.updateInt(PREF_ANIMATION_SPEED, jsonNode, "animationSpeed");
        mConfigurationManager.updateInt(PREF_UPDATE_INTERVAL, jsonNode, "updateInterval");
        loadConfiguration();
    }

// --------------------- Interface FlickrManager ---------------------

    @Override
    public String getUrl() {
        return new Uri.Builder()
                .scheme("file")
                .authority("")
                .appendPath("android_asset")
                .appendPath("webview")
                .appendPath("flickr")
                .appendPath("flickr.html")
                .appendQueryParameter("flickrId", mFlickrId)
                .appendQueryParameter("animationSpeed", String.valueOf(mAnimationSpeed))
                .appendQueryParameter("updateInterval", String.valueOf(mUpdateInterval))
                .build()
                .toString();
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        loadConfiguration();
    }

    private void loadConfiguration() {
        mFlickrId = mConfigurationManager.getString(PREF_FLICKR_ID, PREF_FLICKR_ID_DEFAULT);
        mAnimationSpeed = mConfigurationManager.getInt(PREF_ANIMATION_SPEED, PREF_ANIMATION_SPEED_DEFAULT);
        mUpdateInterval = mConfigurationManager.getInt(PREF_UPDATE_INTERVAL, PREF_UPDATE_INTERVAL_DEFAULT);
    }
}

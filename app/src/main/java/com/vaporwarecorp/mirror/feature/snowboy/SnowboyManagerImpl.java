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
package com.vaporwarecorp.mirror.feature.snowboy;

import ai.kitt.snowboy.RecognitionListener;
import ai.kitt.snowboy.SnowboyRecognizer;
import com.fasterxml.jackson.databind.JsonNode;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.ConfigurationManager;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.HotWordEvent;
import com.vaporwarecorp.mirror.feature.common.AbstractMirrorManager;

import java.io.IOException;

import static com.vaporwarecorp.mirror.util.JsonUtil.createBooleanNode;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(SnowboyManager.class)
public class SnowboyManagerImpl extends AbstractMirrorManager implements SnowboyManager, RecognitionListener {
// ------------------------------ FIELDS ------------------------------

    private static final String PREF = SnowboyManager.class.getName();
    private static final String PREF_AUDIO_GAIN = PREF + ".PREF_AUDIO_GAIN";
    private static final float PREF_AUDIO_GAIN_DEFAULT = 2F;
    private static final String PREF_ENABLED = PREF + ".PREF_ENABLED";
    private static final boolean PREF_ENABLED_DEFAULT = false;
    private static final String PREF_MODEL_PATH = PREF + ".PREF_MODEL_PATH";
    private static final String PREF_MODEL_PATH_DEFAULT = "snowboy/computer.pmdl";
    private static final String PREF_SENSITIVITY = PREF + ".PREF_SENSITIVITY";
    private static final float PREF_SENSITIVITY_DEFAULT = 0.45F;
    private static final String RESOURCE_PATH = "snowboy/common.res";

    @Plug
    AppManager mAppManager;
    @Plug
    ConfigurationManager mConfigurationManager;
    @Plug
    EventManager mEventManager;

    private float mAudioGain;
    private boolean mEnabled;
    private String mModelPath;
    private SnowboyRecognizer mRecognizer;
    private float mSensitivity;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Configuration ---------------------

    @Override
    public String getJsonConfiguration() {
        return "configuration/json/snowboy.json";
    }

    @Override
    public String getJsonValues() {
        return createBooleanNode("enabled", mEnabled)
                .put("sensitivity", mSensitivity)
                .put("audioGain", mAudioGain)
                .toString();
    }

    @Override
    public void updateConfiguration(JsonNode jsonNode) {
        mConfigurationManager.updateString(PREF_MODEL_PATH, jsonNode, "pmdl");
        mConfigurationManager.updateFloat(PREF_SENSITIVITY, jsonNode, "sensitivity");
        mConfigurationManager.updateFloat(PREF_AUDIO_GAIN, jsonNode, "audioGain");
        mConfigurationManager.updateBoolean(PREF_ENABLED, jsonNode, "enabled");
        loadConfiguration();
    }

// --------------------- Interface MirrorManager ---------------------

    @Override
    public void onFeaturePause() {
        if (mRecognizer == null) {
            return;
        }
        mRecognizer.stop();
    }

    @Override
    public void onFeatureResume() {
        if (mRecognizer == null) {
            return;
        }
        if (mRecognizer.isListening()) {
            mRecognizer.stop();
        }
        mRecognizer.startListening();
    }

    @Override
    public void onFeatureStart() {
        if (!mEnabled) {
            return;
        }

        final String resourcePath = mAppManager.getLocalAssetPath(RESOURCE_PATH);
        try {
            mRecognizer = new SnowboyRecognizer(resourcePath, mModelPath);
            mRecognizer.setSensitivity(mSensitivity);
            mRecognizer.setAudioGain(mAudioGain);
            mRecognizer.addListener(this);
        } catch (IOException e) {
            onFeatureStop();
        }
    }

    @Override
    public void onFeatureStop() {
        if (mRecognizer == null) {
            return;
        }
        mRecognizer.stop();
        mRecognizer = null;
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        loadConfiguration();
    }

// --------------------- Interface RecognitionListener ---------------------

    @Override
    public void onError(Exception e) {
    }

    @Override
    public void onResult(int index) {
        if (index < 0) return;

        mRecognizer.stop();
        mEventManager.post(new HotWordEvent());
    }

    /**
     * Load the configuration of the component.
     */
    private void loadConfiguration() {
        mEnabled = mConfigurationManager.getBoolean(PREF_ENABLED, PREF_ENABLED_DEFAULT);
        mModelPath = mConfigurationManager.getString(PREF_MODEL_PATH, PREF_MODEL_PATH_DEFAULT);
        mSensitivity = mConfigurationManager.getFloat(PREF_SENSITIVITY, PREF_SENSITIVITY_DEFAULT);
        mAudioGain = mConfigurationManager.getFloat(PREF_AUDIO_GAIN, PREF_AUDIO_GAIN_DEFAULT);
    }
}

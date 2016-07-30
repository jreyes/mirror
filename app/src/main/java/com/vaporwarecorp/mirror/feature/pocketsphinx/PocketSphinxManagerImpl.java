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

package com.vaporwarecorp.mirror.feature.pocketsphinx;

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
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import timber.log.Timber;

import java.io.File;
import java.io.IOException;

import static com.vaporwarecorp.mirror.feature.pocketsphinx.SpeechRecognizerSetup.defaultSetup;
import static com.vaporwarecorp.mirror.util.JsonUtil.createTextNode;


@Plugin
@Scope(MirrorAppScope.class)
@Provides(PocketSphinxManager.class)
public class PocketSphinxManagerImpl extends AbstractMirrorManager implements PocketSphinxManager, RecognitionListener {
// ------------------------------ FIELDS ------------------------------

    private static final String KWS_SEARCH = "COMMAND";
    private static final String PREF = PocketSphinxManager.class.getName();
    private static final String PREF_ENABLED = PREF + ".PREF_ENABLED";
    private static final boolean PREF_ENABLED_DEFAULT = true;
    private static final String PREF_HOT_WORD = PREF + ".PREF_HOT_WORD";
    private static final String PREF_HOT_WORD_DEFAULT = "computer";

    @Plug
    AppManager mAppManager;
    @Plug
    ConfigurationManager mConfigurationManager;
    @Plug
    EventManager mEventManager;

    private boolean mEnabled;
    private String mHotWord;
    private SpeechRecognizer mRecognizer;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Configuration ---------------------

    @Override
    public String getJsonConfiguration() {
        return "configuration/json/pocketsphinx.json";
    }

    @Override
    public String getJsonValues() {
        return createTextNode("hotWord", mHotWord).put("enabled", mEnabled).toString();
    }

    @Override
    public void updateConfiguration(JsonNode jsonNode) {
        mConfigurationManager.updateString(PREF_HOT_WORD, jsonNode, "hotWord");
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
        mRecognizer.startListening(KWS_SEARCH);
    }

    @Override
    public void onFeatureStart() {
        if (!mEnabled) {
            return;
        }

        try {
            File assetsDir = new Assets(mAppManager.getAppContext()).syncAssets();
            mRecognizer = defaultSetup()
                    .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                    .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                    .setKeywordThreshold(1e-20f)
                    .setBoolean("-allphone_ci", true)
                    .getRecognizer();
            mRecognizer.addListener(this);
            mRecognizer.addKeyphraseSearch(KWS_SEARCH, mHotWord);
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
            disable();
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            Timber.e(e, e.getMessage());
            disable();
            throw e;
        }
    }

    @Override
    public void onFeatureStop() {
        if (mRecognizer == null) {
            return;
        }
        mRecognizer.cancel();
        mRecognizer.shutdown();
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
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null) return;

        String text = hypothesis.getHypstr().trim().toLowerCase();
        Timber.d("onPartialResult %s", text);
        if (text.equals(mHotWord)) {
            mRecognizer.stop();
            mEventManager.post(new HotWordEvent());
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis == null) return;

        String text = hypothesis.getHypstr().trim().toLowerCase();
        Timber.d("onResult %s", text);
    }

    @Override
    public void onError(Exception e) {
    }

    @Override
    public void onTimeout() {
    }

    private void disable() {
        mConfigurationManager.updateString(PREF_HOT_WORD, PREF_HOT_WORD_DEFAULT);
        mRecognizer = null;
    }

    /**
     * Load the configuration of the component.
     */
    private void loadConfiguration() {
        mHotWord = mConfigurationManager.getString(PREF_HOT_WORD, PREF_HOT_WORD_DEFAULT);
        mEnabled = mConfigurationManager.getBoolean(PREF_ENABLED, PREF_ENABLED_DEFAULT);
    }
}

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

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.component.SoundManager;
import com.vaporwarecorp.mirror.event.CommandEvent;
import com.vaporwarecorp.mirror.feature.alexa.AlexaCommandManager;
import com.vaporwarecorp.mirror.feature.common.AbstractMirrorManager;
import com.vaporwarecorp.mirror.feature.houndify.HoundifyCommandManager;
import com.vaporwarecorp.mirror.feature.speechtotext.SpeechToTextManager;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import timber.log.Timber;

import java.io.File;
import java.io.IOException;

import static com.vaporwarecorp.mirror.component.CommandManager.*;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(PocketSphinxManager.class)
public class PocketSphinxManagerImpl extends AbstractMirrorManager implements PocketSphinxManager, RecognitionListener {
// ------------------------------ FIELDS ------------------------------

    private static final String MENU_SEARCH = "MENU";

    @Plug
    AlexaCommandManager mAlexaCommandManager;
    @Plug
    AppManager mAppManager;
    @Plug
    EventManager mEventManager;
    @Plug
    HoundifyCommandManager mHoundifyCommandManager;
    @Plug
    SoundManager mSoundManager;
    @Plug
    SpeechToTextManager mSpeechToTextManager;

    private SpeechRecognizer mRecognizer;

// ------------------------ INTERFACE METHODS ------------------------


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

        mRecognizer.stop();
        mRecognizer.startListening(MENU_SEARCH);
    }

    @Override
    public void onFeatureStart() {
        try {
            final File assetsDir = new Assets(mAppManager.getAppContext()).syncAssets();
            mRecognizer = defaultSetup()
                    .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                    .setDictionary(new File(assetsDir, "6117.dic"))
                    .setKeywordThreshold(1e-40f)
                    .setBoolean("-allphone_ci", true)
                    .setFloat("-lw", 6.5)
                    .getRecognizer();
            mRecognizer.addListener(this);

            final File menuKeywords = new File(assetsDir, "menu.gram");
            mRecognizer.addKeywordSearch(MENU_SEARCH, menuKeywords);

            if (!PluginBus.isPlugged(AlexaCommandManager.class)) {
                PluginBus.plug(AlexaCommandManager.class);
            }
            if (!PluginBus.isPlugged(HoundifyCommandManager.class)) {
                PluginBus.plug(HoundifyCommandManager.class);
            }
            if (!PluginBus.isPlugged(SpeechToTextManager.class)) {
                PluginBus.plug(SpeechToTextManager.class);
            }

            mAlexaCommandManager.start();
            mHoundifyCommandManager.start();
            mSpeechToTextManager.start();
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            Timber.e(e, e.getMessage());
            throw e;
        }
    }

    @Override
    public void onFeatureStop() {
        mAlexaCommandManager.stop();
        mHoundifyCommandManager.stop();
        mSpeechToTextManager.stop();

        if (mRecognizer == null) {
            return;
        }
        mRecognizer.cancel();
        mRecognizer.shutdown();
        mRecognizer = null;
    }

// --------------------- Interface RecognitionListener ---------------------

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
        if (!mRecognizer.getSearchName().equals(MENU_SEARCH)) {
            mRecognizer.stop();
            mRecognizer.startListening(MENU_SEARCH);
        }
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null) return;

        final String text = hypothesis.getHypstr().trim().toLowerCase();
        if (KEYWORD_ALEXA.equals(text) || KEYWORD_HOUNDIFY.equals(text) || KEYWORD_GOOGLE.equals(text)) {
            mRecognizer.stop();
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis == null) return;

        final String text = hypothesis.getHypstr().trim().toLowerCase();
        if (KEYWORD_ALEXA.equals(text) && mAlexaCommandManager.isEnabled()) {
            mEventManager.post(new CommandEvent(CommandEvent.TYPE_COMMAND_START, KEYWORD_ALEXA));
            mAlexaCommandManager.voiceSearch();
        } else if (KEYWORD_HOUNDIFY.equals(text) && mHoundifyCommandManager.isEnabled()) {
            mEventManager.post(new CommandEvent(CommandEvent.TYPE_COMMAND_START, KEYWORD_HOUNDIFY));
            mHoundifyCommandManager.voiceSearch();
        } else if (KEYWORD_GOOGLE.equals(text)) {
            mEventManager.post(new CommandEvent(CommandEvent.TYPE_COMMAND_START, KEYWORD_GOOGLE));
            mSpeechToTextManager.voiceSearch();
        } else {
            mRecognizer.startListening(MENU_SEARCH);
        }
    }

    @Override
    public void onError(Exception e) {
        Timber.e(e, "onError");
        mRecognizer.startListening(MENU_SEARCH);
    }

    @Override
    public void onTimeout() {
        Timber.e("onTimeout");
        mRecognizer.startListening(MENU_SEARCH);
    }
}

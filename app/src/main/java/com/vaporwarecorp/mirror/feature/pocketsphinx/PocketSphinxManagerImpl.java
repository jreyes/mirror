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
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.component.SoundManager;
import com.vaporwarecorp.mirror.event.HotWordEvent;
import com.vaporwarecorp.mirror.feature.alexa.AlexaCommandManager;
import com.vaporwarecorp.mirror.feature.common.AbstractMirrorManager;
import com.vaporwarecorp.mirror.feature.houndify.HoundifyCommandManager;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import timber.log.Timber;

import java.io.File;
import java.io.IOException;

import static com.vaporwarecorp.mirror.event.HotWordEvent.*;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(PocketSphinxManager.class)
public class PocketSphinxManagerImpl extends AbstractMirrorManager implements PocketSphinxManager, RecognitionListener {
// ------------------------------ FIELDS ------------------------------

    private static final String KEYWORD_ALEXA = "alexa";
    private static final String KEYWORD_HOUNDIFY = "houndify";
    private static final String KEYWORD_POCKET_SPHINX = "computer";
    private static final String KWS_SEARCH = "COMMAND";
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

    private SpeechRecognizer mRecognizer;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface MirrorManager ---------------------

    @Override
    public void onFeaturePause() {
        if (mRecognizer == null) {
            return;
        }

        mRecognizer.stop();

        mAlexaCommandManager.stop();
        mHoundifyCommandManager.stop();
    }

    @Override
    public void onFeatureResume() {
        if (mRecognizer == null) {
            return;
        }

        mAlexaCommandManager.start();
        mHoundifyCommandManager.start();

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
                    .setKeywordThreshold(1e-10f)
                    .setBoolean("-allphone_ci", true)
                    .getRecognizer();
            mRecognizer.addListener(this);

            final File menuGrammar = new File(assetsDir, "menu.gram");
            mRecognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
            //mRecognizer.addKeyphraseSearch(KWS_SEARCH, mHotWord);
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
            Timber.d("onEndOfSpeech");
            switchSearch(MENU_SEARCH);
        }
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null) return;

        final String text = hypothesis.getHypstr().trim().toLowerCase();
        if (KEYWORD_ALEXA.equals(text) || KEYWORD_HOUNDIFY.equals(text)) {
            mRecognizer.stop();
        } else if (KEYWORD_POCKET_SPHINX.equals(text)) {
            mRecognizer.stop();
            mEventManager.post(new HotWordEvent(TYPE_POCKET_SPHINX));
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis == null) return;

        final String text = hypothesis.getHypstr().trim().toLowerCase();
        Timber.d("onResult %s", text);
        if (KEYWORD_ALEXA.equals(text) && mAlexaCommandManager.isEnabled()) {
            mEventManager.post(new HotWordEvent(TYPE_ALEXA));
            mSoundManager.acknowledge(() -> {
                mSoundManager.requestAudioFocus();
                mAlexaCommandManager.voiceSearch();
            });
        } else if (KEYWORD_HOUNDIFY.equals(text) && mHoundifyCommandManager.isEnabled()) {
            mEventManager.post(new HotWordEvent(TYPE_HOUNDIFY));
            mSoundManager.acknowledge(() -> {
                mSoundManager.requestAudioFocus();
                mHoundifyCommandManager.voiceSearch();
            });
        } else {
            switchSearch(MENU_SEARCH);
        }
    }

    @Override
    public void onError(Exception e) {
    }

    @Override
    public void onTimeout() {
    }

    private void switchSearch(String searchName) {
        mRecognizer.stop();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(MENU_SEARCH))
            mRecognizer.startListening(searchName);
        else
            mRecognizer.startListening(searchName, 10000);
    }
}

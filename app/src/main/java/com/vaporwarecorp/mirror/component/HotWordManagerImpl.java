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
package com.vaporwarecorp.mirror.component;

import com.robopupu.api.component.AbstractManager;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.hotword.SpeechRecognizer;
import com.vaporwarecorp.mirror.event.HotWordEvent;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import timber.log.Timber;

import java.io.File;
import java.io.IOException;

import static com.vaporwarecorp.mirror.component.hotword.SpeechRecognizerSetup.defaultSetup;


@Plugin
public class HotWordManagerImpl extends AbstractManager implements HotWordManager, RecognitionListener {
// ------------------------------ FIELDS ------------------------------

    private static final String KEYPHRASE = "HotWordKeyphrase";
    private static final String KWS_SEARCH = "COMMAND";

    @Plug
    AppManager mAppManager;
    @Plug
    EventManager mEventManager;

    private String mKeyphrase;
    private SpeechRecognizer mRecognizer;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(HotWordManager.class)
    public HotWordManagerImpl() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface HotWordManager ---------------------

    @Override
    public void destroy() {
        mRecognizer.cancel();
        mRecognizer.shutdown();
        mRecognizer = null;
    }

    @Override
    public void start() {
        try {
            File assetsDir = new Assets(mAppManager.getAppContext()).syncAssets();
            mRecognizer = defaultSetup()
                    .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                    .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                    .setKeywordThreshold(1e-10f)
                    .setBoolean("-allphone_ci", true)
                    .getRecognizer();

            mKeyphrase = mAppManager.getApplicationProperties().getProperty(KEYPHRASE);
            mRecognizer.addListener(this);
            mRecognizer.addKeyphraseSearch(KWS_SEARCH, mKeyphrase);
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
            mAppManager.exitApplication();
        }
    }

    @Override
    public void startListening() {
        mRecognizer.stop();
        mRecognizer.startListening(KWS_SEARCH);
    }

    @Override
    public void stopListening() {
        mRecognizer.stop();
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
        if (text.equals(mKeyphrase)) {
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
}

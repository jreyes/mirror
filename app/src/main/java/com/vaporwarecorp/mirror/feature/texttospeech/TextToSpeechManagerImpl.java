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

package com.vaporwarecorp.mirror.feature.texttospeech;

import android.speech.tts.TextToSpeech;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.feature.common.AbstractMirrorManager;

import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(TextToSpeechManager.class)
public class TextToSpeechManagerImpl
        extends AbstractMirrorManager
        implements TextToSpeechManager {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;

    private TextToSpeech mTextToSpeech;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface MirrorManager ---------------------

    @Override
    public void onFeaturePause() {
        if (mTextToSpeech == null) {
            return;
        }

        mTextToSpeech.stop();
        mTextToSpeech.shutdown();
        mTextToSpeech = null;
    }

    @Override
    public void onFeatureResume() {
        mTextToSpeech = new TextToSpeech(mAppManager.getAppContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                mTextToSpeech.setLanguage(Locale.US);
            } else {
                mTextToSpeech = null;
            }
        });
    }

// --------------------- Interface TextToSpeechManager ---------------------

    @Override
    public void speak(String textToSpeak) {
        if (mTextToSpeech == null || isEmpty(textToSpeak)) {
            return;
        }
        mTextToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(hashCode()));
    }
}

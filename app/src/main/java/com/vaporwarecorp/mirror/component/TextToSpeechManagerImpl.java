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

import android.speech.tts.TextToSpeech;
import com.robopupu.api.component.AbstractManager;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

@Plugin
public class TextToSpeechManagerImpl
        extends AbstractManager
        implements TextToSpeechManager, TextToSpeech.OnInitListener {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;

    private TextToSpeech mTextToSpeech;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(TextToSpeechManager.class)
    public TextToSpeechManagerImpl() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface OnInitListener ---------------------

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            mTextToSpeech.setLanguage(Locale.US);
        }
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        mTextToSpeech = new TextToSpeech(mAppManager.getAppContext(), this);
    }

// --------------------- Interface TextToSpeechManager ---------------------

    @Override
    public void speak(String textToSpeak) {
        if (StringUtils.isEmpty(textToSpeak)) {
            return;
        }
        mTextToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(hashCode()));
    }

    @Override
    public void stop() {
        mTextToSpeech.stop();
        mTextToSpeech.shutdown();
    }
}

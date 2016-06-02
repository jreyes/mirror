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
package com.vaporwarecorp.mirror.component.hotword;

import edu.cmu.pocketsphinx.Config;

import java.io.File;
import java.io.IOException;

import static edu.cmu.pocketsphinx.Decoder.defaultConfig;

public class SpeechRecognizerSetup {
// ------------------------------ FIELDS ------------------------------

    private final Config config;

// -------------------------- STATIC METHODS --------------------------

    static {
        System.loadLibrary("pocketsphinx_jni");
    }

    /**
     * Creates new speech recognizer builder with default configuration.
     */
    public static SpeechRecognizerSetup defaultSetup() {
        return new SpeechRecognizerSetup(defaultConfig());
    }

// --------------------------- CONSTRUCTORS ---------------------------

    private SpeechRecognizerSetup(Config config) {
        this.config = config;
    }

// -------------------------- OTHER METHODS --------------------------

    public SpeechRecognizer getRecognizer() throws IOException {
        return new SpeechRecognizer(config);
    }

    public SpeechRecognizerSetup setAcousticModel(File model) {
        return setString("-hmm", model.getPath());
    }

    public SpeechRecognizerSetup setBoolean(String key, boolean value) {
        config.setBoolean(key, value);
        return this;
    }

    public SpeechRecognizerSetup setDictionary(File dictionary) {
        return setString("-dict", dictionary.getPath());
    }

    public SpeechRecognizerSetup setKeywordThreshold(float threshold) {
        return setFloat("-kws_threshold", threshold);
    }

    private SpeechRecognizerSetup setFloat(String key, double value) {
        config.setFloat(key, value);
        return this;
    }

    private SpeechRecognizerSetup setString(String key, String value) {
        config.setString(key, value);
        return this;
    }
}

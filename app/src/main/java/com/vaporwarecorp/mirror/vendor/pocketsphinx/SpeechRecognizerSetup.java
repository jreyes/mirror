package com.vaporwarecorp.mirror.vendor.pocketsphinx;

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

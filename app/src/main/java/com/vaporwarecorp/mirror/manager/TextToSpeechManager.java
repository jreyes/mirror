package com.vaporwarecorp.mirror.manager;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class TextToSpeechManager implements TextToSpeech.OnInitListener {
// ------------------------------ FIELDS ------------------------------

    private TextToSpeech mTextToSpeech;

// --------------------------- CONSTRUCTORS ---------------------------

    public TextToSpeechManager(Context context) {
        mTextToSpeech = new TextToSpeech(context, this);
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface OnInitListener ---------------------

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            mTextToSpeech.setLanguage(Locale.US);
        }
    }

// -------------------------- OTHER METHODS --------------------------

    public void onDestroy() {
        mTextToSpeech.stop();
        mTextToSpeech.shutdown();
    }

    public void speak(String textToSpeak) {
        if (StringUtils.isEmpty(textToSpeak)) {
            return;
        }
        mTextToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(hashCode()));
    }
}

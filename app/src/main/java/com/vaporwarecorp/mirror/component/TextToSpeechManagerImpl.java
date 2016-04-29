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

// -------------------------- OTHER METHODS --------------------------

    public void destroy() {
        mTextToSpeech.stop();
        mTextToSpeech.shutdown();
    }
}

package com.vaporwarecorp.mirror.component;

import com.robopupu.api.component.Manager;
import com.robopupu.api.plugin.PlugInterface;

@PlugInterface
public interface TextToSpeechManager extends Manager {
// -------------------------- OTHER METHODS --------------------------

    void destroy();

    void speak(String textToSpeak);
}

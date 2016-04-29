package com.vaporwarecorp.mirror.feature.greet.view;

import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.PlugInterface;

@PlugInterface
public interface GreetView extends View {
// -------------------------- OTHER METHODS --------------------------

    void displayGreet(String greetName, boolean isWelcome);
}

package com.vaporwarecorp.mirror.feature.greet;

import com.robopupu.api.plugin.PlugInterface;
import com.vaporwarecorp.mirror.feature.common.view.MirrorView;

@PlugInterface
public interface GreetView extends MirrorView {
// -------------------------- OTHER METHODS --------------------------

    void displayGreet(String greetName, boolean isWelcome);
}

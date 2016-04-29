package com.vaporwarecorp.mirror.feature.greet.presenter;

import com.robopupu.api.feature.FeaturePresenter;
import com.robopupu.api.plugin.PlugInterface;

@PlugInterface
public interface GreetPresenter extends FeaturePresenter {
// ------------------------------ FIELDS ------------------------------

    String GREET_TYPE = "GREET_TYPE";

// -------------------------- OTHER METHODS --------------------------

    void onAnimationEnd();
}
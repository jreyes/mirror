package com.vaporwarecorp.mirror.feature.main.presenter;

import android.content.Intent;
import com.robopupu.api.feature.FeaturePresenter;
import com.robopupu.api.plugin.PlugInterface;

@PlugInterface
public interface MainPresenter extends FeaturePresenter {
// -------------------------- OTHER METHODS --------------------------

    void processCommand(int resultCode, Intent data);

    void startListening();

    void stopListening();
}
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
package com.vaporwarecorp.mirror.feature.snowboy;

import ai.kitt.snowboy.RecognitionListener;
import ai.kitt.snowboy.SnowboyRecognizer;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.HotWordEvent;
import com.vaporwarecorp.mirror.feature.common.AbstractMirrorManager;

import java.io.IOException;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(SnowboyManager.class)
public class SnowboyManagerImpl extends AbstractMirrorManager implements SnowboyManager, RecognitionListener {
// ------------------------------ FIELDS ------------------------------

    private static final String MODEL_PATH = "snowboy/computer.pmdl";
    private static final String RESOURCE_PATH = "snowboy/common.res";

    @Plug
    AppManager mAppManager;
    @Plug
    EventManager mEventManager;

    private SnowboyRecognizer mRecognizer;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface MirrorManager ---------------------

    @Override
    public void onFeaturePause() {
        if (mRecognizer == null) {
            return;
        }
        mRecognizer.stop();
    }

    @Override
    public void onFeatureResume() {
        if (mRecognizer == null) {
            return;
        }
        if (mRecognizer.isListening()) {
            mRecognizer.stop();
        }
        mRecognizer.startListening();
    }

    @Override
    public void onFeatureStart() {
        final String resourcePath = mAppManager.getLocalAssetPath(RESOURCE_PATH);
        final String modelPath = mAppManager.getLocalAssetPath(MODEL_PATH);

        try {
            mRecognizer = new SnowboyRecognizer(resourcePath, modelPath);
            mRecognizer.addListener(this);
        } catch (IOException e) {
            onFeatureStop();
        }
    }

    @Override
    public void onFeatureStop() {
        if (mRecognizer == null) {
            return;
        }
        mRecognizer.stop();
        mRecognizer = null;
    }

// --------------------- Interface RecognitionListener ---------------------

    @Override
    public void onError(Exception e) {
    }

    @Override
    public void onResult(int index) {
        if (index < 0) return;

        mRecognizer.stop();
        mEventManager.post(new HotWordEvent());
    }
}

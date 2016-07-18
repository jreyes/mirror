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

package com.vaporwarecorp.mirror.component;

import android.media.AudioAttributes;
import android.media.SoundPool;
import com.robopupu.api.component.AbstractManager;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.app.MirrorAppScope;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(SoundManager.class)
public class SoundManagerImpl extends AbstractManager implements SoundManager {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;

    private int mAcknowledgeId;
    private int mErrorId;
    private SoundPool mSoundPool;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);

        mSoundPool = new SoundPool.Builder().setAudioAttributes(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
        ).build();

        mAcknowledgeId = mSoundPool.load(mAppManager.getAppContext(), R.raw.acknowledge, 1);
        mErrorId = mSoundPool.load(mAppManager.getAppContext(), R.raw.error, 1);
    }

// --------------------- Interface SoundManager ---------------------

    @Override
    public void acknowledge() {
        mSoundPool.play(mAcknowledgeId, 1f, 1f, 1, 0, 1f);
    }

    @Override
    public void error() {
        mSoundPool.play(mErrorId, 1f, 1f, 1, 0, 1f);
    }
}

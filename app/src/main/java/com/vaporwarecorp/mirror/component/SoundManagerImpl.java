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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import com.robopupu.api.component.AbstractManager;
import com.robopupu.api.dependency.D;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import timber.log.Timber;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(SoundManager.class)
public class SoundManagerImpl extends AbstractManager implements SoundManager {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;

    private MediaPlayer mAcknowledgePlayer;
    private AudioManager mAudioManager;
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

        mAcknowledgePlayer = MediaPlayer.create(mAppManager.getAppContext(), R.raw.acknowledge);
        mAudioManager = D.get(AudioManager.class);
        //mAcknowledgeId = mSoundPool.load(mAppManager.getAppContext(), R.raw.acknowledge, 1);
        mErrorId = mSoundPool.load(mAppManager.getAppContext(), R.raw.error, 1);
    }

// --------------------- Interface SoundManager ---------------------

    @Override
    public void acknowledge(Listener listener) {
        mAcknowledgePlayer.setOnCompletionListener(mp -> listener.onCompleted());
        mAcknowledgePlayer.start();
    }

    @Override
    public void error() {
        mSoundPool.play(mErrorId, 1f, 1f, 1, 0, 1f);
    }


    @Override
    public void releaseAudioFocus() {
        mAudioManager.abandonAudioFocus(null);
    }

    @Override
    public boolean requestAudioFocus() {
        // Request audio focus for playback
        int result = mAudioManager.requestAudioFocus(null,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Timber.d("Audio focus received");
            return true;
        } else {
            Timber.d("Audio focus NOT received");
            return false;
        }
    }
}

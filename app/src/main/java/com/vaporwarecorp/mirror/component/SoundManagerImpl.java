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
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import java.util.concurrent.TimeUnit;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(SoundManager.class)
public class SoundManagerImpl extends AbstractManager implements SoundManager {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;

    private int mAcknowledgeId;
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

        mAudioManager = D.get(AudioManager.class);
        mAcknowledgeId = mSoundPool.load(mAppManager.getAppContext(), R.raw.acknowledge, 1);
        mErrorId = mSoundPool.load(mAppManager.getAppContext(), R.raw.error, 1);
    }

// --------------------- Interface SoundManager ---------------------

    @Override
    public Observable<Long> acknowledge() {
        mSoundPool.play(mAcknowledgeId, 1f, 1f, 1, 0, 1f);
        return Observable.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
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

    @Override
    public void volumeDown() {
        mAudioManager.adjustVolume(AudioManager.ADJUST_LOWER, 0);
    }

    @Override
    public void volumeUp() {
        mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
    }
}

package com.vaporwarecorp.mirror.manager;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import com.vaporwarecorp.mirror.R;

public class SoundManager {
// ------------------------------ FIELDS ------------------------------

    private int mAcknowledgeId;
    private int mErrorId;
    private SoundPool mSoundPool;

// --------------------------- CONSTRUCTORS ---------------------------

    public SoundManager(Context context) {
        mSoundPool = new SoundPool.Builder().setAudioAttributes(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
        ).build();
        mAcknowledgeId = mSoundPool.load(context, R.raw.acknowledge, 1);
        mErrorId = mSoundPool.load(context, R.raw.error, 1);
    }

// -------------------------- OTHER METHODS --------------------------

    public void acknowledge() {
        mSoundPool.play(mAcknowledgeId, 1f, 1f, 1, 0, 1f);
    }

    public void error() {
        mSoundPool.play(mErrorId, 1f, 1f, 1, 0, 1f);
    }
}

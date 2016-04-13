package com.vaporwarecorp.mirror.ui.fragment;

import android.os.Bundle;
import android.view.View;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.event.HandWaveClickEvent;
import com.vaporwarecorp.mirror.event.HandWaveRightEvent;
import com.vaporwarecorp.mirror.event.VideoCompletedEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.google.android.youtube.player.YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT;

public class YouTubeFragment
        extends YouTubePlayerFragment
        implements YouTubePlayer.OnInitializedListener, YouTubePlayer.PlayerStateChangeListener {
// ------------------------------ FIELDS ------------------------------

    private static final String YOUTUBE_VIDEO_ID = "YOUTUBE_VIDEO_ID";

    private YouTubePlayer mPlayer;

// -------------------------- STATIC METHODS --------------------------

    public static YouTubeFragment newInstance(String youtubeVideoId) {
        Bundle args = new Bundle();
        args.putString(YOUTUBE_VIDEO_ID, youtubeVideoId);

        YouTubeFragment fragment = new YouTubeFragment();
        fragment.setArguments(args);
        return fragment;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface OnInitializedListener ---------------------

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        mPlayer = player;
        mPlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        mPlayer.addFullscreenControlFlag(FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        mPlayer.setPlayerStateChangeListener(this);
        if (!wasRestored) {
            mPlayer.loadVideo(getArguments().getString(YOUTUBE_VIDEO_ID), 0);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult error) {
    }

// --------------------- Interface PlayerStateChangeListener ---------------------

    @Override
    public void onLoading() {
    }

    @Override
    public void onLoaded(String s) {
    }

    @Override
    public void onAdStarted() {
    }

    @Override
    public void onVideoStarted() {
    }

    @Override
    public void onVideoEnded() {
        EventBus.getDefault().post(new VideoCompletedEvent());
    }

    @Override
    public void onError(YouTubePlayer.ErrorReason errorReason) {
    }

// -------------------------- OTHER METHODS --------------------------

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(HandWaveClickEvent event) {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.play();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(HandWaveRightEvent event) {
        EventBus.getDefault().post(new VideoCompletedEvent());
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initialize(getString(R.string.google_maps_key), this);
    }
}

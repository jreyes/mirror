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
package com.vaporwarecorp.mirror.feature.common.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.widget.PLVideoTextureView;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.FeatureFragment;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.feature.common.presenter.VideoPlayerPresenter;
import com.vaporwarecorp.mirror.feature.common.presenter.VideoPlayerPresenter.Listener;

@Plugin
public class VideoPlayerFragment
        extends FeatureFragment<VideoPlayerPresenter>
        implements VideoPlayerView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    VideoPlayerPresenter mPresenter;

    private PLVideoTextureView mVideoView;

// --------------------------- CONSTRUCTORS ---------------------------

    @Provides(VideoPlayerView.class)
    public VideoPlayerFragment() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface MirrorView ---------------------

    @Override
    public boolean isFullscreen() {
        return false;
    }

    @Override
    public Class presenterClass() {
        return VideoPlayerPresenter.class;
    }

// --------------------- Interface PresentedView ---------------------

    @Override
    public VideoPlayerPresenter getPresenter() {
        return mPresenter;
    }

// --------------------- Interface VideoPlayerView ---------------------

    @Override
    public void setVideo(@NonNull String videoPath, @Nullable Listener listener) {
        AVOptions options = new AVOptions();
        if (isLiveStreaming(videoPath)) {
            options.setInteger(AVOptions.KEY_GET_AV_FRAME_TIMEOUT, 10 * 1000);
            options.setInteger(AVOptions.KEY_LIVE_STREAMING, 1);
        }
        options.setInteger(AVOptions.KEY_MEDIACODEC, 1);

        mVideoView.setAVOptions(options);
        mVideoView.setOnCompletionListener(mp -> {
            if (listener != null) {
                listener.onCompleted();
            }
        });
        // After setVideoPath, the play will start automatically
        mVideoView.setVideoPath(videoPath);
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle inState) {
        return inflater.inflate(R.layout.fragment_video_player, container, false);
    }

    @Override
    public void onDestroy() {
        mVideoView.stopPlayback();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        mVideoView.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mVideoView.start();
    }

    @Override
    protected void onCreateBindings() {
        super.onCreateBindings();

        mVideoView = getView(R.id.video_view);

        // Set some listeners
        /*
        mVideoView.setOnPreparedListener(mOnPreparedListener);
        mVideoView.setOnInfoListener(mOnInfoListener);
        mVideoView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mVideoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnSeekCompleteListener(mOnSeekCompleteListener);
        mVideoView.setOnErrorListener(mOnErrorListener);
        */
    }

    private boolean isLiveStreaming(String url) {
        return url.startsWith("rtmp://") ||
                (url.startsWith("http://") && url.endsWith(".m3u8")) ||
                (url.startsWith("http://") && url.endsWith(".flv"));
    }
}

package com.vaporwarecorp.mirror.feature.common.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.widget.PLVideoView;
import com.robopupu.api.feature.FeatureFragment;
import com.robopupu.api.feature.FeaturePresenter;
import com.vaporwarecorp.mirror.R;

public abstract class VideoPlayerFragment<T extends FeaturePresenter>
        extends FeatureFragment<T>
        implements VideoPlayerView {
// ------------------------------ FIELDS ------------------------------

    private PLVideoView mVideoView;

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

    public void setVideoPath(String videoPath) {
        AVOptions options = new AVOptions();
        if (isLiveStreaming(videoPath)) {
            options.setInteger(AVOptions.KEY_GET_AV_FRAME_TIMEOUT, 10 * 1000);
            options.setInteger(AVOptions.KEY_LIVE_STREAMING, 1);
        }
        options.setInteger(AVOptions.KEY_MEDIACODEC, 1);

        mVideoView.setAVOptions(options);

        // After setVideoPath, the play will start automatically
        // mVideoView.start() is not required
        mVideoView.setVideoPath(videoPath);
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

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
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.google.android.youtube.player.*;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.FeatureFragment;
import com.robopupu.api.mvp.Presenter;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.feature.common.presenter.YoutubePresenter;
import com.vaporwarecorp.mirror.feature.common.presenter.YoutubePresenter.Listener;
import timber.log.Timber;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.google.android.youtube.player.YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT;

@Plugin
@Provides(YoutubeView.class)
public class YoutubeFragment
        extends FeatureFragment<YoutubePresenter>
        implements YoutubeView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    YoutubePresenter mPresenter;

    private boolean mFragmentResumed;
    private Listener mListener;
    private YouTubePlayer mPlayer;
    private FrameLayout mPlayerView;
    private YouTubeThumbnailLoader mThumbnailLoader;
    private boolean mThumbnailLoading;
    private YouTubeThumbnailView mThumbnailView;
    private String mYoutubeVideoId;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface MirrorView ---------------------

    @Override
    public boolean isFullscreen() {
        return false;
    }

    @Override
    public void onMaximize() {
        if (mPlayer != null && !mPlayer.isPlaying()) {
            mPlayer.play();
        }
        mThumbnailView.setVisibility(View.GONE);
    }

    @Override
    public void onMinimize() {
        mThumbnailView.setVisibility(View.VISIBLE);
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    @Override
    public Class<? extends Presenter> presenterClass() {
        return YoutubePresenter.class;
    }

// --------------------- Interface PresentedView ---------------------

    @Override
    public YoutubePresenter getPresenter() {
        return mPresenter;
    }

// --------------------- Interface YoutubeView ---------------------

    @Override
    public void setYoutubeVideo(@NonNull String youtubeVideoId, @Nullable Listener listener) {
        mYoutubeVideoId = youtubeVideoId;
        mThumbnailLoading = false;
        mListener = listener;
        maybeStartVideo();
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        initializePlayerView();
        initializeThumbnailView();

        FrameLayout viewFrame = new FrameLayout(getActivity());
        viewFrame.addView(mPlayerView, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        viewFrame.addView(mThumbnailView, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        return viewFrame;
    }

    @Override
    public void onDestroy() {
        if (mThumbnailLoader != null) {
            mThumbnailLoader.release();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        mFragmentResumed = false;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFragmentResumed = true;
    }

    private void initializePlayerView() {
        mPlayerView = new FrameLayout(getActivity());
        mPlayerView.setId(R.id.youtube_player_view);

        YouTubePlayerFragment playerFragment = YouTubePlayerFragment.newInstance();
        getChildFragmentManager().beginTransaction().replace(R.id.youtube_player_view, playerFragment).commit();
        playerFragment.initialize(getString(R.string.google_maps_key), new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
                mPlayer = player;
                mPlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                mPlayer.addFullscreenControlFlag(FULLSCREEN_FLAG_CUSTOM_LAYOUT);
                mPlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                    @Override
                    public void onLoading() {
                        mThumbnailView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoaded(String s) {
                        if (isMaximized()) {
                            mPlayer.play();
                            mThumbnailView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onAdStarted() {
                    }

                    @Override
                    public void onVideoStarted() {
                    }

                    @Override
                    public void onVideoEnded() {
                        if (mListener != null) {
                            mListener.onCompleted();
                        }
                    }

                    @Override
                    public void onError(YouTubePlayer.ErrorReason errorReason) {
                        Timber.e(errorReason.toString());
                    }
                });
                maybeStartVideo();
            }

            @Override
            public void onInitializationFailure(Provider provider, YouTubeInitializationResult errorReason) {
                Timber.e(errorReason.toString());
            }
        });
    }

    private void initializeThumbnailView() {
        mThumbnailView = new YouTubeThumbnailView(getActivity());
        mThumbnailView.initialize(getString(R.string.google_maps_key), new YouTubeThumbnailView.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubeThumbnailView thumbnailView, YouTubeThumbnailLoader thumbnailLoader) {
                mThumbnailLoader = thumbnailLoader;
                mThumbnailLoader.setOnThumbnailLoadedListener(new YouTubeThumbnailLoader.OnThumbnailLoadedListener() {
                    @Override
                    public void onThumbnailLoaded(YouTubeThumbnailView thumbnailView, String videoId) {
                        if (mFragmentResumed) {
                            mPlayer.cueVideo(videoId);
                        }
                    }

                    @Override
                    public void onThumbnailError(YouTubeThumbnailView thumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {
                        Timber.e(errorReason.toString());
                    }
                });
                maybeStartVideo();
            }

            @Override
            public void onInitializationFailure(YouTubeThumbnailView thumbnailView, YouTubeInitializationResult errorReason) {
                Timber.e(errorReason.toString());
            }
        });
    }

    private boolean isMaximized() {
        return getView() != null && ((View) getView().getParent()).getScaleX() == 1f;
    }

    private void maybeStartVideo() {
        if (mFragmentResumed && mPlayer != null && mThumbnailLoader != null && !mThumbnailLoading) {
            mThumbnailLoading = true;
            mThumbnailLoader.setVideo(mYoutubeVideoId);
        }
    }
}

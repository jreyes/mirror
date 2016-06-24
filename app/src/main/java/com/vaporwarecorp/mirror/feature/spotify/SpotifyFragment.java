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
package com.vaporwarecorp.mirror.feature.spotify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import co.mobiwise.library.MusicPlayerView;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.FeatureFragment;
import com.robopupu.api.mvp.Presenter;
import com.robopupu.api.mvp.ViewCompatActivity;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.R;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

import java.util.List;

import static solid.stream.Stream.stream;

@Plugin
@Provides(SpotifyView.class)
public class SpotifyFragment
        extends FeatureFragment<SpotifyPresenter>
        implements SpotifyView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    SpotifyPresenter mPresenter;

    private Track mCurrentTrack;
    private MusicPlayerView mMusicPlayerView;
    private TextView mSingerTextView;
    private TextView mSongTextView;

// -------------------------- STATIC METHODS --------------------------

    private static String artistNames(List<ArtistSimple> artists) {
        return stream(artists).map(a -> a.name).reduce((u, t) -> u + ", " + t).get();
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface MirrorView ---------------------

    @Override
    public boolean isFullscreen() {
        return false;
    }

    @Override
    public void onCenterDisplay() {
    }

    @Override
    public void onSideDisplay() {
    }

    @Override
    public Class presenterClass() {
        return SpotifyPresenter.class;
    }

// --------------------- Interface PresentedView ---------------------

    /**
     * Gets the {@link Presenter} assigned for this {@link ViewCompatActivity}.
     *
     * @return A {@link Presenter}.
     */
    @Override
    public SpotifyPresenter getPresenter() {
        return mPresenter;
    }

// --------------------- Interface SpotifyView ---------------------

    @Override
    public void updateTrack(Track track) {
        if (mCurrentTrack == null || !mCurrentTrack.id.equals(track.id)) {
            mCurrentTrack = track;

            mSongTextView.setText(track.name);
            mSingerTextView.setText(artistNames(track.artists));
            mMusicPlayerView.setCoverURL(track.album.images.get(0).url);
            mMusicPlayerView.setProgress(0);
            mMusicPlayerView.setMax((int) (track.duration_ms / 1000));
            mMusicPlayerView.start();
        }
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle inState) {
        return inflater.inflate(R.layout.fragment_spotify, container, false);
    }

    @Override
    public void onPause() {
        mCurrentTrack = null;
        mSongTextView.setText(null);
        mSingerTextView.setText(null);
        mMusicPlayerView.stop();
        super.onPause();
    }

    @Override
    protected void onCreateBindings() {
        super.onCreateBindings();
        mMusicPlayerView = getView(R.id.music_player_view);
        mSongTextView = getView(R.id.song_text_view);
        mSingerTextView = getView(R.id.singer_text_view);
    }
}

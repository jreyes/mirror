package com.vaporwarecorp.mirror.command.music;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v17.leanback.app.PlaybackOverlayFragment;
import android.support.v17.leanback.widget.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.vaporwarecorp.mirror.MirrorApp;
import com.vaporwarecorp.mirror.event.*;
import com.vaporwarecorp.mirror.manager.SpotifyManager;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class MusicFragment extends PlaybackOverlayFragment {
// ------------------------------ FIELDS ------------------------------

    private static final int DEFAULT_UPDATE_PERIOD = 1000;
    private static final String SPOTIFY_TRACK_IDS = "SPOTIFY_TRACK_IDS";
    private static final int UPDATE_PERIOD = 16;

    private int mDuration;
    private Handler mHandler;
    private long mLastPosition;
    private long mLastPositionUpdateTime;
    private PlaybackControlsRow.PlayPauseAction mPlayPauseAction;
    private PlaybackControlsRow mPlaybackControlsRow;
    private ClassPresenterSelector mPresenterSelector;
    private ArrayObjectAdapter mRowsAdapter;
    private Runnable mRunnable;
    private SpotifyManager mSpotifyManager;

// -------------------------- STATIC METHODS --------------------------

    @SuppressWarnings("Convert2streamapi")
    private static String artistNames(List<ArtistSimple> artists) {
        List<String> artistNames = new ArrayList<>();
        for (ArtistSimple artist : artists) {
            artistNames.add(artist.name);
        }
        return StringUtils.join(artistNames, ", ");
    }

    public static MusicFragment newInstance(ArrayList<String> trackIds) {
        Bundle args = new Bundle();
        args.putStringArrayList(SPOTIFY_TRACK_IDS, trackIds);

        MusicFragment fragment = new MusicFragment();
        fragment.setArguments(args);
        return fragment;
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSpotifyManager = MirrorApp.spotify(getActivity());
        mHandler = new Handler();

        initDisplay();
        initMediaMetadata();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(SpotifyPlaybackEvent event) {
        if (mPlaybackControlsRow == null) {
            // We only update playback state after we get a valid metadata.
            return;
        }

        mLastPosition = event.getLastPosition();
        mLastPositionUpdateTime = SystemClock.elapsedRealtime();
        switch (event.getEventType()) {
            case PLAY:
                startProgressAutomation();
                mPlayPauseAction.setIndex(PlaybackControlsRow.PlayPauseAction.PAUSE);
                break;
            case PAUSE:
                stopProgressAutomation();
                mPlayPauseAction.setIndex(PlaybackControlsRow.PlayPauseAction.PLAY);
                break;
        }
        mRowsAdapter.notifyArrayItemRangeChanged(mRowsAdapter.indexOf(mPlaybackControlsRow), 1);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(HandWaveClickEvent event) {
        mSpotifyManager.togglePlay();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(HandWaveRightEvent event) {
        EventBus.getDefault().post(new VideoCompletedEvent());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(SpotifyTrackEvent event) {
        updateMetadata(event.getTrack());
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        mSpotifyManager.stop();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void addPlaybackControlsRow(Track track) {
        mPlaybackControlsRow = new PlaybackControlsRow(new MutableTrackHolder(track));
        mRowsAdapter.add(mPlaybackControlsRow);

        resetPlaybackRow();

        mPlayPauseAction = new PlaybackControlsRow.PlayPauseAction(getActivity());

        ArrayObjectAdapter primaryActionsAdapter = new ArrayObjectAdapter(new ControlButtonPresenterSelector());
        primaryActionsAdapter.add(mPlayPauseAction);
        mPlaybackControlsRow.setPrimaryActionsAdapter(primaryActionsAdapter);
    }

    private int getUpdatePeriod() {
        if (getView() == null || mPlaybackControlsRow.getTotalTime() <= 0) {
            return DEFAULT_UPDATE_PERIOD;
        }
        return Math.max(UPDATE_PERIOD, mPlaybackControlsRow.getTotalTime() / getView().getWidth());
    }

    private void initDisplay() {
        mPresenterSelector = new ClassPresenterSelector();
        mRowsAdapter = new ArrayObjectAdapter(mPresenterSelector);
        setBackgroundType(PlaybackOverlayFragment.BG_NONE);
        setFadingEnabled(false);
    }

    private void initMediaMetadata() {
        ArrayList<String> trackIds = getArguments().getStringArrayList(SPOTIFY_TRACK_IDS);
        mSpotifyManager.play(trackIds);
    }

    private void initializePlaybackControls(Track track) {
        setupRows();
        addPlaybackControlsRow(track);
        setAdapter(mRowsAdapter);
    }

    private void resetPlaybackRow() {
        mDuration = 0;
        mPlaybackControlsRow.setTotalTime(0);
        mPlaybackControlsRow.setCurrentTime(0);
        mRowsAdapter.notifyArrayItemRangeChanged(mRowsAdapter.indexOf(mPlaybackControlsRow), 1);
    }

    private void setupRows() {
        PlaybackControlsRowPresenter playbackControlsRowPresenter =
                new PlaybackControlsRowPresenter(new DescriptionPresenter());
        mPresenterSelector.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
    }

    private void startProgressAutomation() {
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
        mRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsedTime = SystemClock.elapsedRealtime() - mLastPositionUpdateTime;
                int currentPosition = Math.min(mDuration, (int) (mLastPosition + elapsedTime));
                mPlaybackControlsRow.setCurrentTime(currentPosition);
                mHandler.postDelayed(this, getUpdatePeriod());
            }
        };
        mHandler.postDelayed(mRunnable, getUpdatePeriod());
    }

    private void stopProgressAutomation() {
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    private void updateAlbumArt(String imageUrl) {
        Glide
                .with(getActivity())
                .load(imageUrl)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        Drawable artDrawable = new BitmapDrawable(MusicFragment.this.getResources(), bitmap);
                        mPlaybackControlsRow.setImageDrawable(artDrawable);
                        mRowsAdapter.notifyArrayItemRangeChanged(mRowsAdapter.indexOf(mPlaybackControlsRow), 1);
                    }
                });
    }

    private void updateMetadata(Track track) {
        if (mPlaybackControlsRow == null) {
            initializePlaybackControls(track);
        }
        mDuration = (int) track.duration_ms;
        mPlaybackControlsRow.setTotalTime(mDuration);
        ((MutableTrackHolder) mPlaybackControlsRow.getItem()).track = track;
        mRowsAdapter.notifyArrayItemRangeChanged(mRowsAdapter.indexOf(mPlaybackControlsRow), 1);
        if (!track.album.images.isEmpty()) {
            updateAlbumArt(track.album.images.get(0).url);
        }
    }

    private static final class DescriptionPresenter extends AbstractDetailsDescriptionPresenter {
        @Override
        protected void onBindDescription(ViewHolder viewHolder, Object item) {
            MutableTrackHolder data = ((MutableTrackHolder) item);
            viewHolder.getTitle().setText(data.track.name);
            viewHolder.getSubtitle().setText(artistNames(data.track.artists));
        }
    }

    private static final class MutableTrackHolder {
        Track track;

        MutableTrackHolder(Track track) {
            this.track = track;
        }
    }
}

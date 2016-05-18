package com.vaporwarecorp.mirror.feature.spotify;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter.ViewHolder;
import android.support.v17.leanback.widget.ImageCardView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.mvp.Presenter;
import com.robopupu.api.mvp.ViewCompatActivity;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.feature.common.view.PlaybackOverlayFragment;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

import java.util.List;

import static solid.stream.Stream.stream;

@Plugin
public class SpotifyFragment
        extends PlaybackOverlayFragment<SpotifyPresenter, Track>
        implements SpotifyView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    SpotifyPresenter mPresenter;

// -------------------------- STATIC METHODS --------------------------

    private static String artistNames(List<ArtistSimple> artists) {
        return stream(artists).map(a -> a.name).reduce((u, t) -> u + ", " + t).get();
    }

// --------------------------- CONSTRUCTORS ---------------------------

    @Provides(SpotifyView.class)
    public SpotifyFragment() {
    }

// ------------------------ INTERFACE METHODS ------------------------


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
    public void updateMetadata(Object item) {

    }

/*
    @Override
    public void updateMetadata(Track track) {
        if (mPlaybackControlsRow == null) {
            initializePlaybackControls(track);
        }
        mDuration = (int) track.duration_ms;
        mPlaybackControlsRow.setTotalTime(mDuration);
        ((MutableTrackHolder) mPlaybackControlsRow.getItem()).track = track;
        mRowsAdapter.notifyArrayItemRangeChanged(mRowsAdapter.indexOf(mPlaybackControlsRow), 1);
        if (!track.album.images.isEmpty()) {
            Glide
                    .with(getActivity())
                    .load(track.album.images.get(0).url)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                            Drawable artDrawable = new BitmapDrawable(SpotifyFragment.this.getResources(), bitmap);
                            mPlaybackControlsRow.setImageDrawable(artDrawable);
                            mRowsAdapter.notifyArrayItemRangeChanged(mRowsAdapter.indexOf(mPlaybackControlsRow), 1);
                        }
                    });
        }
    }*/

    @Override
    protected void bindCardPresenter(ImageCardView cardView, Track track) {
        cardView.setTitleText(track.name);
        cardView.setContentText(artistNames(track.artists));

        Glide.with(getActivity())
                .load(track.album.images.get(0).url)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(cardView.getMainImageView());
    }

    @Override
    protected void bindDescriptionPresenter(ViewHolder viewHolder, MutableDataHolder holder) {
        Track track = (Track) holder.data;
        viewHolder.getTitle().setText(track.name);
        viewHolder.getSubtitle().setText(artistNames(track.artists));
    }
}

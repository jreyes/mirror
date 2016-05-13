package com.vaporwarecorp.mirror.feature.spotify;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.PlaybackOverlayFragment;
import android.support.v17.leanback.widget.*;
import android.widget.AdapterView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.robopupu.api.binding.AdapterViewBinding;
import com.robopupu.api.binding.ViewBinder;
import com.robopupu.api.binding.ViewBinding;
import com.robopupu.api.dependency.*;
import com.robopupu.api.feature.Feature;
import com.robopupu.api.feature.FeatureView;
import com.robopupu.api.mvp.*;
import com.robopupu.api.mvp.Presenter;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.robopupu.api.util.Converter;
import com.spotify.sdk.android.player.PlayerNotificationCallback.EventType;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;
import org.apache.commons.lang3.StringUtils;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.List;

@Plugin
public class SpotifyFragment
        extends PlaybackOverlayFragment
        implements SpotifyView, FeatureView, PresentedView<SpotifyPresenter> {
// ------------------------------ FIELDS ------------------------------

    private static final int DEFAULT_UPDATE_PERIOD = 1000;
    private static final String SPOTIFY_TRACK_IDS = "SPOTIFY_TRACK_IDS";
    private static final int UPDATE_PERIOD = 16;

    @Plug
    SpotifyPresenter mPresenter;

    private final ViewBinder mBinder;
    private int mDuration;
    private Feature mFeature;
    private Handler mHandler;
    private long mLastPosition;
    private long mLastPositionUpdateTime;
    private PlaybackControlsRow.PlayPauseAction mPlayPauseAction;
    private PlaybackControlsRow mPlaybackControlsRow;
    private ClassPresenterSelector mPresenterSelector;
    private ArrayObjectAdapter mRowsAdapter;
    private Runnable mRunnable;
    private DependencyScope mScope;
    private final ViewState mState;

// -------------------------- STATIC METHODS --------------------------

    private static String artistNames(List<ArtistSimple> artists) {
        List<String> artistNames = new ArrayList<>();
        for (ArtistSimple artist : artists) {
            artistNames.add(artist.name);
        }
        return StringUtils.join(artistNames, ", ");
    }

// --------------------------- CONSTRUCTORS ---------------------------

    @Provides(SpotifyView.class)
    public SpotifyFragment() {
        mBinder = new ViewBinder(this);
        mState = new ViewState(this);
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface FeatureView ---------------------

    @Override
    public void setFeature(final Feature feature) {
        mFeature = feature;
    }

    @Override
    public boolean isDialog() {
        return false;
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

// --------------------- Interface Scopeable ---------------------

    @Override
    public DependencyScope getScope() {
        return mScope;
    }

    @Override
    public void setScope(final DependencyScope scope) {
        mScope = scope;
    }

// --------------------- Interface SpotifyView ---------------------

    @Override
    public void play(ArrayList<String> trackIds) {

    }

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
            updateAlbumArt(track.album.images.get(0).url);
        }
    }

    @Override
    public void updateProgress(EventType eventType, int lastPosition) {
        if (mPlaybackControlsRow == null) {
            // We only update playback state after we get a valid metadata.
            return;
        }

        mLastPosition = lastPosition;
        mLastPositionUpdateTime = SystemClock.elapsedRealtime();
        switch (eventType) {
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

// --------------------- Interface View ---------------------

    @NonNull
    @Override
    public ViewState getState() {
        return mState;
    }

    @Override
    public String getViewTag() {
        return getClass().getName();
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * Creates and binds a {@link ViewBinding} to a {@link android.view.View} specified by the given view id.
     *
     * @param viewId A view id used in a layout XML resource.
     * @param <T>    The parametrised type of the ViewDelagate.
     * @return The created {@link ViewBinding}.
     */
    @SuppressWarnings("unchecked")
    public <T extends ViewBinding<?>> T bind(@IdRes final int viewId) {
        return mBinder.bind(viewId);
    }

    /**
     * Binds the given {@link ViewBinding} to the specified {@link android.view.View}.
     *
     * @param viewId  A view id in a layout XML specifying the target {@link android.view.View}.
     * @param binding An {@link ViewBinding}.
     * @return The found and bound {@link android.view.View}.
     */
    @SuppressWarnings("unchecked")
    public <T extends android.view.View> T bind(@IdRes final int viewId, final ViewBinding<T> binding) {
        return mBinder.bind(viewId, binding);
    }

    /**
     * Binds the given {@link AdapterViewBinding} to the specified {@link AdapterView}.
     *
     * @param viewId  A view id in a layout XML specifying the target {@link AdapterView}.
     * @param binding An {@link AdapterViewBinding}.
     * @param adapter An {@link AdapterViewBinding.Adapter} that is assigned to {@link AdapterViewBinding}.
     * @return The found and bound {@link AdapterView}.
     */
    @SuppressWarnings("unchecked")
    public AdapterView bind(@IdRes final int viewId, final AdapterViewBinding<?> binding, final AdapterViewBinding.Adapter<?> adapter) {
        return mBinder.bind(viewId, binding, adapter);
    }

    /**
     * Looks up and returns a {@link android.view.View} with the given layout id.
     *
     * @param viewId A view id used in a layout XML resource.
     * @return The found {@link android.view.View}.
     */
    @SuppressWarnings("unchecked")
    public <T extends android.view.View> T getView(@IdRes final int viewId) {
        return (T) getActivity().findViewById(viewId);
    }

    @Override
    public void onActivityCreated(final Bundle inState) {
        super.onActivityCreated(inState);

        mBinder.setActivity(getActivity());
        onCreateBindings();

        if (inState != null) {
            onRestoreState(inState);

            final DependenciesCache cache = D.get(DependenciesCache.class);
            final DependencyMap dependencies = cache.getDependencies(this);

            if (dependencies != null) {
                final DependencyScope scope = dependencies.getDependency(KEY_DEPENDENCY_SCOPE);

                if (scope != null) {
                    mScope = scope;
                }
                onRestoreDependencies(dependencies);
            }
        }
    }

    @Override
    public void onDestroy() {
        Timber.d("onDestroy()");
        super.onDestroy();
        mState.onDestroy();

        mBinder.dispose();

        if (this instanceof DependencyScopeOwner) {
            // Cached DependencyScope is automatically disposed to avoid memory leaks

            final DependenciesCache cache = D.get(DependenciesCache.class);
            final DependencyScopeOwner owner = (DependencyScopeOwner) this;
            cache.removeDependencyScope(owner);
        }

        final SpotifyPresenter presenter = resolvePresenter();
        if (presenter != null) {
            presenter.onViewDestroy(this);
        }

        if (PluginBus.isPlugged(this)) {
            Timber.d("onDestroy() : Unplugged from PluginBus");
            PluginBus.unplug(this);
        }
    }

    @Override
    public void onPause() {
        Timber.d("onPause()");
        super.onPause();
        mState.onPause();

        final SpotifyPresenter presenter = resolvePresenter();
        if (presenter != null) {
            presenter.onViewPause(this);
        }

        if (mFeature != null) {
            mFeature.removeActiveView(this);
        }
    }

    @Override
    public void onResume() {
        Timber.d("onResume()");
        super.onResume();
        mState.onResume();

        final SpotifyPresenter presenter = resolvePresenter();
        if (presenter != null) {
            presenter.onViewResume(this);
        }

        final DependenciesCache cache = D.get(DependenciesCache.class);
        cache.removeDependencies(this);

        if (mFeature != null) {
            mFeature.addActiveView(this);
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        onSaveState(outState);

        final DependenciesCache cache = D.get(DependenciesCache.class);

        // Save a reference to the Presenter

        final DependencyMap dependencies = cache.getDependencies(this, true);
        dependencies.addDependency(KEY_DEPENDENCY_SCOPE, mScope);

        onSaveDependencies(dependencies);

        if (this instanceof DependencyScopeOwner) {
            // DependencyScope is automatically cached so that it can be restored when
            // and if the View resumes

            final DependencyScopeOwner owner = (DependencyScopeOwner) this;
            cache.saveDependencyScope(owner, owner.getOwnedScope());
        }
    }

    @Override
    public void onStart() {
        Timber.d("onStart()");
        super.onStart();
        mState.onStart();

        final SpotifyPresenter presenter = resolvePresenter();
        if (presenter != null) {
            presenter.onViewStart(this);
            mBinder.initialise();
        }
    }

    @Override
    public void onStop() {
        Timber.d("onStop()");
        super.onStop();
        mState.onStop();

        final SpotifyPresenter presenter = resolvePresenter();
        if (presenter != null) {
            presenter.onViewStop(this);
        }
    }

    @Override
    public void onViewCreated(final android.view.View view, final Bundle inState) {
        Timber.d("onViewCreated(...)");
        super.onViewCreated(view, inState);
        mState.onCreate();

        final SpotifyPresenter presenter = resolvePresenter();
        if (presenter != null) {
            presenter.onViewCreated(this, Converter.fromBundleToParams(inState));
        } else {
            Timber.d("onViewCreated(...) : Presenter == null");
        }

        mHandler = new Handler();
        initDisplay();
    }

    /**
     * Invoked to bind {@link ViewBinding}s to {@link View}s. This method has to be overridden in
     * classes extended from {@link ViewFragment}.
     */
    @CallSuper
    protected void onCreateBindings() {
        // Do nothing by default
    }

    /**
     * This method can be overridden to restore dependencies after the {@link ViewFragment} is
     * restored, for instance, after recreating it.
     *
     * @param dependencies A {@link DependencyMap} for restoring the dependencies.
     */
    protected void onRestoreDependencies(final DependencyMap dependencies) {
        // By default do nothing
    }

    /**
     * This method can be overridden to restore state of this {@link ViewFragment} from the given
     * {@link Bundle}.
     *
     * @param inState A {@link Bundle}.
     */
    protected void onRestoreState(final Bundle inState) {
        // By default do nothing
    }

    /**
     * This method can be overridden to save dependencies after the {@link ViewFragment} is
     * restored, for instance, after recreating it.
     *
     * @param dependencies A {@link DependencyMap} for saving the dependencies.
     */
    protected void onSaveDependencies(final DependencyMap dependencies) {
        // By default do nothing
    }

    /**
     * This method can be overridden to save state of this {@link ViewFragment} to the given
     * {@link Bundle}.
     *
     * @param outState A {@link Bundle}.
     */
    protected void onSaveState(final Bundle outState) {
        // By default do nothing
    }

    /**
     * Resolves the {@link Presenter} assigned for this {@link ViewCompatActivity}.
     *
     * @return A {@link Presenter}.
     */
    protected SpotifyPresenter resolvePresenter() {
        SpotifyPresenter presenter = getPresenter();

        if (presenter == null) {
            if (PluginBus.isPlugin(getClass())) {
                PluginBus.plug(this);
                presenter = getPresenter();
            }
        }
        return presenter;
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
                        Drawable artDrawable = new BitmapDrawable(SpotifyFragment.this.getResources(), bitmap);
                        mPlaybackControlsRow.setImageDrawable(artDrawable);
                        mRowsAdapter.notifyArrayItemRangeChanged(mRowsAdapter.indexOf(mPlaybackControlsRow), 1);
                    }
                });
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

package com.vaporwarecorp.mirror.feature.common.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v17.leanback.widget.*;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter.ViewHolder;
import android.support.v17.leanback.widget.PlaybackControlsRow.PlayPauseAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.SkipNextAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.SkipPreviousAction;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.robopupu.api.binding.AdapterViewBinding;
import com.robopupu.api.binding.ViewBinder;
import com.robopupu.api.binding.ViewBinding;
import com.robopupu.api.dependency.*;
import com.robopupu.api.feature.Feature;
import com.robopupu.api.feature.FeatureView;
import com.robopupu.api.mvp.PresentedView;
import com.robopupu.api.mvp.Presenter;
import com.robopupu.api.mvp.ViewState;
import com.robopupu.api.plugin.PluginBus;
import com.robopupu.api.util.Converter;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerNotificationCallback.EventType;
import com.vaporwarecorp.mirror.R;

import java.util.List;

public abstract class PlaybackOverlayFragment<T_Presenter extends Presenter, U extends Object>
        extends android.support.v17.leanback.app.PlaybackOverlayFragment
        implements PlaybackOverlayView, FeatureView, PresentedView<T_Presenter> {
// ------------------------------ FIELDS ------------------------------

    private static final int DEFAULT_UPDATE_PERIOD = 1000;
    private static final int UPDATE_PERIOD = 16;
    private final ViewBinder mBinder;
    private int mDuration;
    private Feature mFeature;
    private Handler mHandler;
    private long mLastPosition;
    private long mLastPositionUpdateTime;
    private PlayPauseAction mPlayPauseAction;
    private PlaybackControlsRow mPlaybackControlsRow;
    private ArrayObjectAdapter mPrimaryActionsAdapter;
    private ArrayObjectAdapter mRowsAdapter;
    private Runnable mRunnable;
    private DependencyScope mScope;
    private Object mSelectedItem;
    private SkipNextAction mSkipNextAction;
    private SkipPreviousAction mSkipPreviousAction;
    private final ViewState mState;

// --------------------------- CONSTRUCTORS ---------------------------

    protected PlaybackOverlayFragment() {
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

// --------------------- Interface PlaybackOverlayView ---------------------

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
                mPlayPauseAction.setIndex(PlayPauseAction.PAUSE);
                break;
            case PAUSE:
                stopProgressAutomation();
                mPlayPauseAction.setIndex(PlayPauseAction.PLAY);
                break;
        }
        mRowsAdapter.notifyArrayItemRangeChanged(mRowsAdapter.indexOf(mPlaybackControlsRow), 1);
    }

    @Override
    public void updateQueue(List items) {
        if (items.isEmpty()) {
            return;
        }

        mSelectedItem = items.get(0);

        addPlaybackControlsRow();

        /*
        final ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        listRowAdapter.addAll(0, items);

        final HeaderItem header = new HeaderItem(0, "Queue");
        mRowsAdapter.add(new ListRow(header, listRowAdapter));
        */

        setAdapter(mRowsAdapter);
    }

// --------------------- Interface PresentedView ---------------------

    /**
     * Gets the {@link Presenter} assigned for this {@link ViewCompatActivity}.
     *
     * @return A {@link Presenter}.
     */
    @Override
    public abstract T_Presenter getPresenter();

// --------------------- Interface Scopeable ---------------------

    @Override
    public DependencyScope getScope() {
        return mScope;
    }

    @Override
    public void setScope(final DependencyScope scope) {
        mScope = scope;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        setBackgroundType(PlaybackOverlayFragment.BG_NONE);
        setFadingEnabled(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mState.onDestroy();

        mBinder.dispose();

        if (this instanceof DependencyScopeOwner) {
            // Cached DependencyScope is automatically disposed to avoid memory leaks

            final DependenciesCache cache = D.get(DependenciesCache.class);
            final DependencyScopeOwner owner = (DependencyScopeOwner) this;
            cache.removeDependencyScope(owner);
        }

        final T_Presenter presenter = resolvePresenter();
        if (presenter != null) {
            presenter.onViewDestroy(this);
        }

        if (PluginBus.isPlugged(this)) {
            PluginBus.unplug(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mState.onPause();

        final T_Presenter presenter = resolvePresenter();
        if (presenter != null) {
            presenter.onViewPause(this);
        }

        if (mFeature != null) {
            mFeature.removeActiveView(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mState.onResume();

        final T_Presenter presenter = resolvePresenter();
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
        super.onStart();
        mState.onStart();

        final T_Presenter presenter = resolvePresenter();
        if (presenter != null) {
            presenter.onViewStart(this);
            mBinder.initialise();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mState.onStop();

        final T_Presenter presenter = resolvePresenter();
        if (presenter != null) {
            presenter.onViewStop(this);
        }
    }

    @Override
    public void onViewCreated(final android.view.View view, final Bundle inState) {
        super.onViewCreated(view, inState);
        mState.onCreate();

        final T_Presenter presenter = resolvePresenter();
        if (presenter != null) {
            presenter.onViewCreated(this, Converter.fromBundleToParams(inState));
        }
    }

    protected abstract void bindCardPresenter(ImageCardView cardView, U item);

    protected abstract void bindDescriptionPresenter(ViewHolder viewHolder, MutableDataHolder holder);

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
    protected T_Presenter resolvePresenter() {
        T_Presenter presenter = getPresenter();

        if (presenter == null) {
            if (PluginBus.isPlugin(getClass())) {
                PluginBus.plug(this);
                presenter = getPresenter();
            }
        }
        return presenter;
    }

    private void addPlaybackControlsRow() {
        PlaybackControlsRowPresenter playbackControlsRowPresenter = new PlaybackControlsRowPresenter(new DescriptionPresenter());
        playbackControlsRowPresenter.setSecondaryActionsHidden(true);

        ClassPresenterSelector classPresenterSelector = new ClassPresenterSelector();
        classPresenterSelector.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
        classPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());

        mRowsAdapter = new ArrayObjectAdapter(classPresenterSelector);

        mPlaybackControlsRow = new PlaybackControlsRow(new MutableDataHolder(mSelectedItem));
        mRowsAdapter.add(mPlaybackControlsRow);

        updatePlaybackRow();

        Activity activity = getActivity();
        mPlayPauseAction = new PlayPauseAction(activity);
        mSkipNextAction = new SkipNextAction(activity);
        mSkipPreviousAction = new SkipPreviousAction(activity);

        mPrimaryActionsAdapter = new ArrayObjectAdapter(new ControlButtonPresenterSelector());
        mPrimaryActionsAdapter.add(mSkipPreviousAction);
        mPrimaryActionsAdapter.add(mPlayPauseAction);
        mPrimaryActionsAdapter.add(mSkipNextAction);
        mPlaybackControlsRow.setPrimaryActionsAdapter(mPrimaryActionsAdapter);
    }

    /*
    private void addPlaybackControlsRow(U data) {
        mPlaybackControlsRow = new PlaybackControlsRow(new MutableDataHolder(data));
        mRowsAdapter.add(mPlaybackControlsRow);

        resetPlaybackRow();

        mPlayPauseAction = new PlayPauseAction(getActivity());

        ArrayObjectAdapter primaryActionsAdapter = new ArrayObjectAdapter(new ControlButtonPresenterSelector());
        primaryActionsAdapter.add(mPlayPauseAction);
        mPlaybackControlsRow.setPrimaryActionsAdapter(primaryActionsAdapter);
    }
    */

    private int getUpdatePeriod() {
        if (getView() == null || mPlaybackControlsRow.getTotalTime() <= 0) {
            return DEFAULT_UPDATE_PERIOD;
        }
        return Math.max(UPDATE_PERIOD, mPlaybackControlsRow.getTotalTime() / getView().getWidth());
    }

    /*
    private void initializePlaybackControls(U data) {
        setupRows();
        addPlaybackControlsRow(data);
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
    */

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

    private void updatePlaybackRow() {
        mPlaybackControlsRow.setCurrentTime(0);
        mPlaybackControlsRow.setBufferedProgress(0);
        mRowsAdapter.notifyArrayItemRangeChanged(0, 1);
    }

    protected class MutableDataHolder {
        public Object data;

        MutableDataHolder(Object data) {
            this.data = data;
        }
    }

    private class CardPresenter extends android.support.v17.leanback.widget.Presenter {
        private static final int CARD_WIDTH = 300;
        private static final int CARD_HEIGHT = 300;

        private int mSelectedBackgroundColor = -1;
        private int mDefaultBackgroundColor = -1;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            Context context = parent.getContext();
            mDefaultBackgroundColor = ContextCompat.getColor(context, R.color.primary);
            mSelectedBackgroundColor = ContextCompat.getColor(context, R.color.primary_dark);

            ImageCardView cardView = new ImageCardView(parent.getContext()) {
                @Override
                public void setSelected(boolean selected) {
                    updateCardBackgroundColor(this, selected);
                    super.setSelected(selected);
                }
            };

            cardView.setFocusable(true);
            cardView.setFocusableInTouchMode(true);
            updateCardBackgroundColor(cardView, false);
            return new ViewHolder(cardView);
        }

        private void updateCardBackgroundColor(ImageCardView view, boolean selected) {
            int color = selected ? mSelectedBackgroundColor : mDefaultBackgroundColor;
            view.setBackgroundColor(color);
            view.findViewById(R.id.info_field).setBackgroundColor(color);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ImageCardView cardView = (ImageCardView) viewHolder.view;
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
            bindCardPresenter(cardView, (U) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
            ImageCardView cardView = (ImageCardView) viewHolder.view;
            cardView.setBadgeImage(null);
            cardView.setMainImage(null);
        }
    }

    private class DescriptionPresenter extends AbstractDetailsDescriptionPresenter {
        @SuppressWarnings("unchecked")
        @Override
        protected void onBindDescription(ViewHolder viewHolder, Object item) {
            bindDescriptionPresenter(viewHolder, (MutableDataHolder) item);
        }
    }
}

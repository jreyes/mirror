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
package com.vaporwarecorp.mirror.feature.main;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import com.robopupu.api.feature.FeatureContainer;
import com.robopupu.api.feature.FeatureView;
import com.robopupu.api.mvp.PluginActivity;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.PluginFeatureManager;
import com.vaporwarecorp.mirror.component.dottedgrid.DottedGridView;
import com.vaporwarecorp.mirror.component.forecast.ForecastView;
import com.vaporwarecorp.mirror.component.forecast.model.Forecast;
import com.vaporwarecorp.mirror.feature.MainFeature;
import com.vaporwarecorp.mirror.feature.MainScope;
import com.vaporwarecorp.mirror.feature.common.view.MirrorView;
import com.vaporwarecorp.mirror.util.FullScreenActivityUtil;

@Plugin
public class MirrorActivity extends PluginActivity<MainPresenter> implements MainView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
    @Plug(MirrorAppScope.class)
    MainFeature mFeature;
    @Plug
    PluginFeatureManager mFeatureManager;
    @Plug(MainScope.class)
    MainPresenter mPresenter;

    private DottedGridView mContentContainer;
    private Integer mCurrentContainerId;
    private Class mCurrentPresenterClass;
    private ForecastView mForecastView;
    private View mFullscreenContainer;
    private View mHeaderContainer;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface FeatureContainer ---------------------

    @Override
    @IdRes
    public int getContainerViewId() {
        return R.id.fullscreen_container;
    }

// --------------------- Interface FeatureTransitionManager ---------------------

    /**
     * Displays the DialogFragment or MirrorView Fragment.
     */
    @Override
    public void showView(final FeatureView featureView, final boolean addToBackStack, final String fragmentTag) {
        String tag = fragmentTag;
        if (fragmentTag == null) {
            tag = featureView.getViewTag();
        }

        final FragmentManager manager = getFragmentManager();
        if (featureView instanceof DialogFragment) {
            showDialogFragment(manager, (DialogFragment) featureView, addToBackStack, tag);
        } else if (featureView instanceof MirrorView) {
            showMirrorView(manager, (MirrorView) featureView, addToBackStack, tag);
        } else {
            throw new IllegalArgumentException("View must be a DialogFragment or MirrorView");
        }
    }

    /**
     * Removes the DialogFragment or MirrorView Fragment.
     */
    @Override
    public void removeView(final FeatureView featureView, final boolean addedToBackStack, final String fragmentTag) {
        if (featureView instanceof DialogFragment) {
            hideDialogFragment((DialogFragment) featureView);
        } else if (featureView instanceof MirrorView) {
            String tag = (fragmentTag != null) ? fragmentTag : featureView.getViewTag();
            hideMirrorView((MirrorView) featureView, addedToBackStack, tag);
        } else {
            throw new IllegalArgumentException("View must be a DialogFragment or MirrorView");
        }
    }

// --------------------- Interface MainView ---------------------

    @Override
    public Activity activity() {
        return this;
    }

    @Override
    public void displayView() {
        mHeaderContainer.setVisibility(View.VISIBLE);
        mContentContainer.setVisibility(View.VISIBLE);
        mFullscreenContainer.setVisibility(View.GONE);
    }

    @Override
    public FeatureContainer getMainFeatureContainer() {
        return this;
    }

    @Override
    public void hideView() {
        mHeaderContainer.setVisibility(View.INVISIBLE);
        mContentContainer.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setForecast(Forecast forecast) {
        mForecastView.setForecast(forecast);
    }

// --------------------- Interface PresentedView ---------------------

    @Override
    public MainPresenter getPresenter() {
        return mPresenter;
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPresenter.verifyPermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.onViewResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(final Bundle inState) {
        super.onCreate(inState);
        setContentView(R.layout.activity_mirror);

        mHeaderContainer = findViewById(R.id.header_container);
        mContentContainer = (DottedGridView) findViewById(R.id.content_container);
        mFullscreenContainer = findViewById(R.id.fullscreen_container);
        mForecastView = (ForecastView) findViewById(R.id.forecast_view);
        findViewById(R.id.test1).setOnClickListener(v -> mPresenter.test1());
        findViewById(R.id.test2).setOnClickListener(v -> mPresenter.test2());
        findViewById(R.id.test3).setOnClickListener(v -> mPresenter.test3());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAppManager.refWatcher().watch(this);
    }

    @Override
    protected void onPause() {
        removeDottedGridListener();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onResumeFullScreen();
        addDottedGridListener();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!mFeature.isStarted()) {
            mFeatureManager.startFeature(this, mFeature);
        }
    }

    @SuppressWarnings("unchecked")
    private void addDottedGridListener() {
        mContentContainer.setListener(new DottedGridView.Listener() {
            @Override
            public void onClosedToRight(MirrorView mirrorView) {

            }

            @Override
            public void onViewOnCenter(int containerId) {
                MirrorView mirrorView = getMirrorViewByContainerId(containerId);
                if (mirrorView != null) {
                    if (mCurrentPresenterClass != null && !mCurrentPresenterClass.equals(mirrorView.presenterClass())) {
                        MirrorView currentMirrorView = getMirrorViewByContainerId(mCurrentContainerId);
                        if (currentMirrorView != null) {
                            mFeature.hidePresenter(currentMirrorView.presenterClass());
                        }
                    }
                    mCurrentPresenterClass = mirrorView.presenterClass();
                    mCurrentContainerId = containerId;
                }
            }

            @Override
            public void onViewOnLeft(int containerId) {
                if (mCurrentPresenterClass != null) {
                    final MirrorView mirrorView = getMirrorViewByContainerId(containerId);
                    if (mirrorView != null && mCurrentPresenterClass.equals(mirrorView.presenterClass())) {
                        mCurrentPresenterClass = null;
                        mCurrentContainerId = null;
                    }
                }
            }

            @Override
            public void onViewOnRight(int containerId) {
                if (mCurrentPresenterClass != null) {
                    final MirrorView mirrorView = getMirrorViewByContainerId(containerId);
                    if (mirrorView != null && mCurrentPresenterClass.equals(mirrorView.presenterClass())) {
                        mCurrentPresenterClass = null;
                        mCurrentContainerId = null;
                    }
                }
            }
        });
    }

    private MirrorView getMirrorViewByContainerId(int containerId) {
        Fragment fragment = getFragmentManager().findFragmentById(containerId);
        if (fragment != null && fragment instanceof MirrorView) {
            return (MirrorView) fragment;
        }
        return null;
    }

    private int getParentId(final Fragment fragment) {
        if (fragment.getView() != null && fragment.getView().getParent() != null) {
            return ((View) fragment.getView().getParent()).getId();
        }
        return 0;
    }

    private void hideDialogFragment(final DialogFragment dialogFragment) {
        dialogFragment.dismiss();
    }

    private void hideFragment(final Fragment fragment,
                              final boolean removeParentView,
                              final boolean addedToBackStack,
                              final String tag) {
        final FragmentManager manager = getFragmentManager();
        if (manager.findFragmentByTag(tag) != null) {
            final FragmentTransaction transaction = manager.beginTransaction();
            transaction.remove(fragment);
            transaction.commit();
            if (addedToBackStack) {
                manager.popBackStack();
            }

            final int viewId = getParentId(fragment);
            if (removeParentView && viewId != 0) {
                mContentContainer.removeBorderView(viewId);
            }
        }
    }

    private void hideMirrorView(final MirrorView mirrorView, final boolean addedToBackStack, final String tag) {
        hideFragment((Fragment) mirrorView, !mirrorView.isFullscreen(), addedToBackStack, tag);
    }

    private void onResumeFullScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        FullScreenActivityUtil.onResume(this);
    }

    private void removeDottedGridListener() {
        mContentContainer.setListener(null);
    }

    private void showDialogFragment(final FragmentManager fragmentManager,
                                    final DialogFragment dialogFragment,
                                    final boolean addToBackStack,
                                    final String tag) {
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(dialogFragment, tag);
        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }
        transaction.commitAllowingStateLoss();
    }

    private void showFragment(final FragmentManager fragmentManager,
                              final Fragment fragment,
                              final boolean addToBackStack,
                              final String tag) {
        mFullscreenContainer.setVisibility(View.GONE);

        if (fragmentManager.findFragmentByTag(tag) == null) {
            final int viewId = mContentContainer.addBorderView(this);
            updateCurrentPresenterClass(fragment, viewId);

            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(viewId, fragment, tag);
            if (addToBackStack) {
                transaction.addToBackStack(tag);
            }
            transaction.commitAllowingStateLoss();
        } else {
            fragmentManager
                    .beginTransaction()
                    .detach(fragment)
                    .attach(fragment)
                    .commitAllowingStateLoss();
        }
    }

    private void showMirrorView(final FragmentManager fragmentManager,
                                final MirrorView mirrorView,
                                final boolean addToBackStack,
                                final String tag) {
        if (mirrorView.isFullscreen()) {
            mFullscreenContainer.setVisibility(View.VISIBLE);

            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(getContainerViewId(), (Fragment) mirrorView, tag);
            if (addToBackStack) {
                transaction.addToBackStack(tag);
            }
            transaction.commitAllowingStateLoss();
        } else {
            showFragment(fragmentManager, (Fragment) mirrorView, addToBackStack, tag);
        }
    }

    @SuppressWarnings("unchecked")
    private void updateCurrentPresenterClass(Fragment fragment, int containerId) {
        if (mCurrentPresenterClass != null) {
            mFeature.hidePresenter(mCurrentPresenterClass);
            mCurrentPresenterClass = null;
        }

        if (fragment != null) {
            if (!(fragment instanceof MirrorView)) {
                throw new IllegalArgumentException("Fragment must implement MirrorView");
            }
            mCurrentPresenterClass = ((MirrorView) fragment).presenterClass();
            mCurrentContainerId = containerId;
        }
    }
}

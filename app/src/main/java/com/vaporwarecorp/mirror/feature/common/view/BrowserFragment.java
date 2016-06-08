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
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.webkit.WebViewFragment;
import android.widget.AdapterView;
import com.robopupu.api.binding.AdapterViewBinding;
import com.robopupu.api.binding.ViewBinder;
import com.robopupu.api.binding.ViewBinding;
import com.robopupu.api.dependency.*;
import com.robopupu.api.feature.Feature;
import com.robopupu.api.feature.FeatureContainerProvider;
import com.robopupu.api.feature.FeatureManager;
import com.robopupu.api.feature.FeatureView;
import com.robopupu.api.mvp.*;
import com.robopupu.api.plugin.PluginBus;
import com.robopupu.api.util.Converter;
import com.vaporwarecorp.mirror.feature.common.presenter.BrowserPresenter;

public abstract class BrowserFragment<T_Presenter extends BrowserPresenter>
        extends WebViewFragment
        implements BrowserView, PresentedView<T_Presenter>, FeatureView {
// ------------------------------ FIELDS ------------------------------

    private final ViewBinder mBinder;
    private Feature mFeature;
    private DependencyScope mScope;
    private final ViewState mState;

// --------------------------- CONSTRUCTORS ---------------------------

    protected BrowserFragment() {
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

// --------------------- Interface MirrorView ---------------------

    @Override
    public boolean isFullscreen() {
        return false;
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

        // If this ViewFragment is a FeatureContainerProvider, it needs to be registered to
        // FeatureManager
        if (this instanceof FeatureContainerProvider) {
            final FeatureContainerProvider provider = (FeatureContainerProvider) this;
            final FeatureManager featureManager = D.get(FeatureManager.class);
            featureManager.registerFeatureContainerProvider(provider);
        }

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
}
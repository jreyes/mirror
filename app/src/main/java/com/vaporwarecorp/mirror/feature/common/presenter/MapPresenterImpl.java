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
package com.vaporwarecorp.mirror.feature.common.presenter;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.feature.common.view.MapView;

@Plugin
public class MapPresenterImpl extends AbstractFeaturePresenter<MapView> implements MapPresenter {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
    @Plug
    MapView mView;

// --------------------------- CONSTRUCTORS ---------------------------

    @Provides(MapPresenter.class)
    public MapPresenterImpl() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(MapView.class);
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewResume(View view) {
        super.onViewResume(view);
        mView.displayMap(getFromMarkerOptions(), getToMarkerOptions());
    }

    @Override
    protected MapView getViewPlug() {
        return mView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAppManager.refWatcher().watch(this);
    }

    private Double getDouble(String key) {
        return (Double) getParams().get(key);
    }

    private MarkerOptions getFromMarkerOptions() {
        String title = getParams().getString(MAP_FROM_TITLE);
        LatLng latLng = new LatLng(
                getDouble(MAP_FROM_LATITUDE),
                getDouble(MAP_FROM_LONGITUDE)
        );
        return new MarkerOptions().position(latLng).title(title);
    }

    private MarkerOptions getToMarkerOptions() {
        String title = getParams().getString(MAP_TO_TITLE);
        if (title == null) {
            return null;
        }

        LatLng latLng = new LatLng(
                getDouble(MAP_TO_LATITUDE),
                getDouble(MAP_TO_LONGITUDE)
        );
        return new MarkerOptions().position(latLng).title(title);
    }
}


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

import android.content.Intent;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorApplication;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.feature.common.view.MapView;

import java.util.HashMap;
import java.util.Map;

@Plugin
public class MapPresenterImpl extends AbstractFeaturePresenter<MapView> implements MapPresenter {
// ------------------------------ FIELDS ------------------------------

    private static final String DIRECTIONS_URL = "http://maps.google.com/maps?saddr=%s,%s(%s)&daddr=%s,%s(%s)";
    private static final String MAP_URL = "https://maps.google.com/maps?q=loc:%s,%s(%s)";

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

// --------------------- Interface Shareable ---------------------

    @Override
    public Map<String, Object> content() {
        final String title = getParams().getString(MAP_FROM_TITLE);
        final double lat = getDouble(MAP_FROM_LATITUDE);
        final double lng = getDouble(MAP_FROM_LONGITUDE);
        final String toTitle = getParams().getString(MAP_TO_TITLE);
        final double toLat = getDouble(MAP_TO_LATITUDE);
        final double toLng = getDouble(MAP_TO_LONGITUDE);
        final Map<String, Object> content = new HashMap<>();
        content.put(ACTION, Intent.ACTION_VIEW);
        if (toTitle == null) {
            content.put(URL, String.format(MAP_URL, lat, lng, title));
        } else {
            content.put(URL, String.format(DIRECTIONS_URL, lat, lng, title, toLat, toLng, toTitle));
        }
        content.put(CLASS_NAME_KEY, "com.google.android.apps.maps");
        content.put(CLASS_NAME_VALUE, "com.google.android.maps.MapsActivity");
        return content;
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
        MirrorApplication.refWatcher(this);
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


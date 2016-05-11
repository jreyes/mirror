package com.vaporwarecorp.mirror.feature.showmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.AppManager;

@Plugin
public class ShowMapPresenterImpl extends AbstractFeaturePresenter<ShowMapView> implements ShowMapPresenter {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
    @Plug
    ShowMapView mView;

// --------------------------- CONSTRUCTORS ---------------------------

    @Provides(ShowMapPresenter.class)
    public ShowMapPresenterImpl() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(ShowMapView.class);
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewStart(final View view) {
        super.onViewStart(view);
        mView.displayMap(getFromMarkerOptions());
    }

    @Override
    protected ShowMapView getViewPlug() {
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
}

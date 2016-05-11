package com.vaporwarecorp.mirror.feature.showdirections;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.feature.showmap.ShowMapPresenter;

@Plugin
public class ShowDirectionsPresenterImpl extends AbstractFeaturePresenter<ShowDirectionsView> implements ShowMapPresenter {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
    @Plug
    ShowDirectionsView mView;

// --------------------------- CONSTRUCTORS ---------------------------

    @Provides(ShowMapPresenter.class)
    public ShowDirectionsPresenterImpl() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(ShowDirectionsView.class);
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewStart(final View view) {
        super.onViewStart(view);
        mView.displayMap(getFromMarkerOptions(), getToMarkerOptions());
    }

    @Override
    protected ShowDirectionsView getViewPlug() {
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
        LatLng latLng = new LatLng(
                getDouble(MAP_TO_LATITUDE),
                getDouble(MAP_TO_LONGITUDE)
        );
        return new MarkerOptions().position(latLng).title(title);
    }
}

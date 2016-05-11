package com.vaporwarecorp.mirror.feature.showmap;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.feature.common.view.MapFragment;

@Plugin
public class ShowMapFragment extends MapFragment<ShowMapPresenter> implements ShowMapView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    ShowMapPresenter mPresenter;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(ShowMapView.class)
    public ShowMapFragment() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PresentedView ---------------------

    @Override
    public ShowMapPresenter getPresenter() {
        return mPresenter;
    }
}

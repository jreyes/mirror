package com.vaporwarecorp.mirror.feature.showdirections;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.feature.common.view.MapFragment;

@Plugin
public class ShowDirectionsFragment extends MapFragment<ShowDirectionsPresenter> implements ShowDirectionsView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    ShowDirectionsPresenter mPresenter;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(ShowDirectionsView.class)
    public ShowDirectionsFragment() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PresentedView ---------------------

    @Override
    public ShowDirectionsPresenter getPresenter() {
        return mPresenter;
    }
}

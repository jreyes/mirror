package com.vaporwarecorp.mirror.feature.internet;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.feature.common.view.VideoPlayerFragment;

@Plugin
public class InternetFragment extends VideoPlayerFragment<InternetPresenter> implements InternetView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    InternetPresenter mPresenter;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(InternetView.class)
    public InternetFragment() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PresentedView ---------------------

    @Override
    public InternetPresenter getPresenter() {
        return mPresenter;
    }
}

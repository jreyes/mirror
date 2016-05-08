package com.vaporwarecorp.mirror.feature.miku;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.feature.common.view.YoutubeFragment;

@Plugin
public class MikuTimeFragment extends YoutubeFragment<MikuTimePresenter> implements MikuTimeView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    MikuTimePresenter mPresenter;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(MikuTimeView.class)
    public MikuTimeFragment() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PresentedView ---------------------

    @Override
    public MikuTimePresenter getPresenter() {
        return mPresenter;
    }
}

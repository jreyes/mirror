package com.vaporwarecorp.mirror.feature.watch;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.feature.common.view.VideoPlayerFragment;

@Plugin
public class WatchCBSFragment extends VideoPlayerFragment<WatchCBSPresenter> implements WatchCBSView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    WatchCBSPresenter mPresenter;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(WatchCBSView.class)
    public WatchCBSFragment() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PresentedView ---------------------

    @Override
    public WatchCBSPresenter getPresenter() {
        return mPresenter;
    }
}

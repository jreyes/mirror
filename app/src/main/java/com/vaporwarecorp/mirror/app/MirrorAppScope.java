package com.vaporwarecorp.mirror.app;

import com.robopupu.api.app.BaseAppScope;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;

@Scope
public class MirrorAppScope extends BaseAppScope<MirrorApplication> {
// --------------------------- CONSTRUCTORS ---------------------------

    public MirrorAppScope(final MirrorApplication app) {
        super(app);
    }

// -------------------------- OTHER METHODS --------------------------

    @Provides
    public MirrorApplication getMirrorApplication() {
        return getApplication();
    }
}
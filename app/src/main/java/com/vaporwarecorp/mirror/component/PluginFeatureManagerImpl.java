package com.vaporwarecorp.mirror.component;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.feature.AbstractFeatureManager;
import com.robopupu.api.feature.Feature;
import com.robopupu.api.feature.FeatureContainer;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.robopupu.api.util.Params;
import com.vaporwarecorp.mirror.app.MirrorAppScope;

@Plugin
public class PluginFeatureManagerImpl extends AbstractFeatureManager implements PluginFeatureManager {
// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(PluginFeatureManager.class)
    public PluginFeatureManagerImpl() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface FeatureManager ---------------------

    @Override
    public Feature startFeature(final FeatureContainer container, final Feature feature, final Params params) {
        PluginBus.plug(feature);
        return super.startFeature(container, feature, params);
    }
}
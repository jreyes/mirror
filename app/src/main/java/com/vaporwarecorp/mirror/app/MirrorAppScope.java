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
package com.vaporwarecorp.mirror.app;

import com.robopupu.api.app.Robopupu;
import com.robopupu.api.component.BitmapManager;
import com.robopupu.api.component.BitmapManagerImpl;
import com.robopupu.api.dependency.AppDependencyScope;
import com.robopupu.api.dependency.DependenciesCache;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.PluginBus;
import com.robopupu.api.util.PermissionRequestManager;

@Scope
public class MirrorAppScope extends AppDependencyScope<MirrorApplication> {
// --------------------------- CONSTRUCTORS ---------------------------

    public MirrorAppScope(final MirrorApplication app) {
        super(app);
    }

// -------------------------- OTHER METHODS --------------------------

    @Provides
    public MirrorApplication getMirrorApplication() {
        return getApplication();
    }

    @Override
    protected <T> T getDependency() {
        if (type(PluginBus.class)) {
            return dependency(PluginBus.getInstance());
        } else if (type(BitmapManager.class)) {
            return dependency(new BitmapManagerImpl());
        } else if (type(DependenciesCache.class)) {
            final Robopupu robopupu = Robopupu.getInstance();
            return dependency(robopupu.getDependenciesCache());
        } else if (type(PermissionRequestManager.class)) {
            return dependency(new PermissionRequestManager());
        }
        return super.getDependency();
    }
}
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
package com.vaporwarecorp.mirror.component;

import android.content.ContextWrapper;
import com.pixplicity.easyprefs.library.Prefs;
import com.robopupu.api.component.AbstractManager;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;

@Plugin
public class PreferenceManagerImpl extends AbstractManager implements PreferenceManager {
// ------------------------------ FIELDS ------------------------------

    private static final String PREFERENCE_USER_NAME = "USER_NAME";

    @Plug
    AppManager mAppManager;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(PreferenceManager.class)
    public PreferenceManagerImpl() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        new Prefs.Builder()
                .setContext(mAppManager.getAppContext())
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(mAppManager.getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
    }

// --------------------- Interface PreferenceManager ---------------------

    public String getUserName() {
        return Prefs.getString(PREFERENCE_USER_NAME, "User");
    }

    public void setUserName(String userName) {
        Prefs.putString(PREFERENCE_USER_NAME, userName);
    }
}

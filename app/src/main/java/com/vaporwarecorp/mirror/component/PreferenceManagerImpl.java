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

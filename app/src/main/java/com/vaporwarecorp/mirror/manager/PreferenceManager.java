package com.vaporwarecorp.mirror.manager;

import android.content.Context;
import android.content.ContextWrapper;
import com.pixplicity.easyprefs.library.Prefs;

public class PreferenceManager {
// ------------------------------ FIELDS ------------------------------

    private static final String PREFERENCE_USER_NAME = "USER_NAME";

// --------------------------- CONSTRUCTORS ---------------------------

    public PreferenceManager(Context context) {
        new Prefs.Builder()
                .setContext(context)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(context.getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
    }

// -------------------------- OTHER METHODS --------------------------

    public String getUserName() {
        return Prefs.getString(PREFERENCE_USER_NAME, "User");
    }

    public void setUserName(String userName) {
        Prefs.putString(PREFERENCE_USER_NAME, userName);
    }
}

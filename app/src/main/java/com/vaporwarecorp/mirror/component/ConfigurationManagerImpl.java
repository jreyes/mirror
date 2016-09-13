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

import com.fasterxml.jackson.databind.JsonNode;
import com.pixplicity.easyprefs.library.Prefs;
import com.robopupu.api.component.AbstractManager;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.mvp.Presenter;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import solid.functions.Action1;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.trimToNull;
import static solid.stream.Stream.stream;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(ConfigurationManager.class)
public class ConfigurationManagerImpl extends AbstractManager implements ConfigurationManager {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
    @Plug
    EventManager mEventManager;

    private Set<Class<? extends Presenter>> mDisabledPresenters;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface ConfigurationManager ---------------------

    @Override
    public void disablePresenter(Class<? extends Presenter> presenterClass) {
        if (!mDisabledPresenters.contains(presenterClass)) {
            mDisabledPresenters.add(presenterClass);
        }
    }

    @Override
    public void enablePresenter(Class<? extends Presenter> presenterClass) {
        mDisabledPresenters.remove(presenterClass);
    }

    @Override
    public boolean getBoolean(String preferenceKey, boolean defaultValue) {
        try {
            return Prefs.getBoolean(preferenceKey, defaultValue);
        } catch (ClassCastException e) {
            Timber.e(e, "Error getting the value for %s", preferenceKey);
            Prefs.remove(preferenceKey);
            return defaultValue;
        }
    }

    @Override
    public float getFloat(String preferenceKey, float defaultValue) {
        try {
            return Prefs.getFloat(preferenceKey, defaultValue);
        } catch (ClassCastException e) {
            Timber.e(e, "Error getting the value for %s", preferenceKey);
            Prefs.remove(preferenceKey);
            return defaultValue;
        }
    }

    @Override
    public int getInt(String preferenceKey, int defaultValue) {
        return Prefs.getInt(preferenceKey, defaultValue);
    }

    @Override
    public String getString(String preferenceKey, String defaultValue) {
        try {
            return Prefs.getString(preferenceKey, defaultValue);
        } catch (ClassCastException e) {
            Timber.e(e, "Error getting the value for %s", preferenceKey);
            Prefs.remove(preferenceKey);
            return defaultValue;
        }
    }

    @Override
    public ArrayList<String> getStringList(String preferenceKey, List<String> defaultValue) {
        return new ArrayList<>(Prefs.getStringSet(preferenceKey, new HashSet<>(defaultValue)));
    }

    @Override
    public boolean isPresenterEnabled(Class<? extends Presenter> presenterClass) {
        return !mDisabledPresenters.contains(presenterClass);
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void updateBoolean(String preferenceKey, JsonNode jsonNode, String jsonNodeKey) {
        stream(jsonNode.findValues(jsonNodeKey)).forEach((Action1<JsonNode>) j -> Prefs.putBoolean(preferenceKey, j.booleanValue()));
    }

    @Override
    public void updateFloat(String preferenceKey, JsonNode jsonNode, String jsonNodeKey) {
        stream(jsonNode.findValues(jsonNodeKey)).forEach((Action1<JsonNode>) j -> Prefs.putFloat(preferenceKey, j.floatValue()));
    }

    @Override
    public void updateInt(String preferenceKey, JsonNode jsonNode, String jsonNodeKey) {
        stream(jsonNode.findValues(jsonNodeKey)).forEach((Action1<JsonNode>) j -> Prefs.putInt(preferenceKey, j.intValue()));
    }

    @Override
    public void updateString(String preferenceKey, String preferenceValue) {
        final String value = trimToNull(preferenceValue);
        if (value != null) {
            Prefs.putString(preferenceKey, value);
        } else {
            Prefs.remove(preferenceKey);
        }
    }

    @Override
    public void updateString(String preferenceKey, JsonNode jsonNode, String jsonNodeKey) {
        stream(jsonNode.findValues(jsonNodeKey)).forEach((Action1<JsonNode>) j -> updateString(preferenceKey, j.textValue()));
    }

    @Override
    public void updateStringSet(String preferenceKey, JsonNode jsonNode, String jsonNodeKey) {
        final Set<String> values = new HashSet<>();
        stream(jsonNode.findValues(jsonNodeKey)).forEach((Action1<JsonNode>) j -> values.add(j.textValue()));
        Prefs.putStringSet(preferenceKey, values);
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        mDisabledPresenters = new HashSet<>();
    }

    @Override
    public void onUnplugged(PluginBus bus) {
        stop();
        mDisabledPresenters = null;
        super.onUnplugged(bus);
    }
}

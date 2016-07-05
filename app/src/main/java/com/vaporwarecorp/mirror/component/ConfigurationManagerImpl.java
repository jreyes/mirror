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
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.configuration.Configuration;
import com.vaporwarecorp.mirror.component.configuration.WebServer;
import com.vaporwarecorp.mirror.event.ResetEvent;
import com.vaporwarecorp.mirror.feature.configuration.ConfigurationPresenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static solid.stream.Stream.stream;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(ConfigurationManager.class)
public class ConfigurationManagerImpl extends AbstractManager implements ConfigurationManager {
// ------------------------------ FIELDS ------------------------------

    private static final String PREFERENCE_CONFIGURED = "CONFIGURED";

    @Plug
    AppManager mAppManager;
    @Plug
    EventManager mEventManager;

    private WebServer mServer;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface ConfigurationManager ---------------------

    @Override
    public String getString(String preferenceKey, String defaultValue) {
        return Prefs.getString(preferenceKey, defaultValue);
    }

    @Override
    public ArrayList<String> getStringList(String preferenceKey, List<String> defaultValue) {
        return new ArrayList<>(Prefs.getStringSet(preferenceKey, new HashSet<>(defaultValue)));
    }

    @Override
    public void hasBeenSetup() {
        Prefs.putBoolean(PREFERENCE_CONFIGURED, true);
    }

    @Override
    public boolean needsInitialSetup() {
        return !Prefs.getBoolean(PREFERENCE_CONFIGURED, false);
    }

    @Override
    public void start() {
        mServer.start(PluginBus.getPlugs(Configuration.class),
                () -> mEventManager.post(new ResetEvent(ConfigurationPresenter.class)));
    }

    @Override
    public void stop() {
        hasBeenSetup();
        if (mServer.isAlive()) {
            mServer.stop();
        }
    }

    @Override
    public void updateString(String preferenceKey, String preferenceValue) {
        Prefs.putString(preferenceKey, trimToEmpty(preferenceValue));
    }

    @Override
    public void updateString(String preferenceKey, JsonNode jsonNode, String jsonNodeKey) {
        stream(jsonNode.findValues(jsonNodeKey))
                .filter(j -> isNotEmpty(j.textValue()))
                .forEach((JsonNode j) -> updateString(preferenceKey, j.textValue()));
    }

    @SuppressWarnings("Convert2streamapi")
    @Override
    public void updateStringSet(String preferenceKey, JsonNode jsonNode, String jsonNodeKey) {
        final Set<String> values = new HashSet<>();
        for (JsonNode url : jsonNode.findValues(jsonNodeKey)) {
            values.add(url.textValue());
        }
        Prefs.putStringSet(preferenceKey, values);
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        mServer = new WebServer(mAppManager.getAppContext());
    }

    @Override
    public void onUnplugged(PluginBus bus) {
        stop();
        super.onUnplugged(bus);
    }
}

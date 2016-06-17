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
package com.vaporwarecorp.mirror.feature.greet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.ConfigurationManager;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.component.PreferenceManager;
import com.vaporwarecorp.mirror.event.Event;
import com.vaporwarecorp.mirror.event.GreetEvent;
import com.vaporwarecorp.mirror.util.RxUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.vaporwarecorp.mirror.event.GreetEvent.TYPE_GOODBYE;
import static com.vaporwarecorp.mirror.event.GreetEvent.TYPE_WELCOME;
import static com.vaporwarecorp.mirror.util.JsonUtil.*;
import static com.vaporwarecorp.mirror.util.RandomUtil.randomValue;
import static org.apache.commons.lang3.StringUtils.replace;

@Plugin
@Provides(GreetPresenter.class)
public class GreetPresenterImpl extends AbstractFeaturePresenter<GreetView> implements GreetPresenter {
// ------------------------------ FIELDS ------------------------------

    private static final String PREF = GreetPresenter.class.getName();
    private static final String PREF_GOODBYE = PREF + ".PREF_GOODBYE";
    private static final List<String> PREF_GOODBYE_DEFAULT = Collections.singletonList(
            "<b>Goodbye</b><br>{{user}}"
    );
    private static final String PREF_WELCOME = PREF + ".PREF_WELCOME";
    private static final List<String> PREF_WELCOME_DEFAULT = Collections.singletonList(
            "<b>Welcome</b><br>{{user}}"
    );
    private static final String TEMPLATE_USER = "{{user}}";

    @Plug
    AppManager mAppManager;
    @Plug
    ConfigurationManager mConfigurationManager;
    @Plug
    EventManager mEventManager;
    @Plug
    PreferenceManager mPreferenceManager;
    @Plug
    GreetView mView;

    private ArrayList<String> mGoodbyes;
    private ArrayList<String> mWelcomes;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Configuration ---------------------

    @Override
    public String getJsonConfiguration() {
        return "configuration/json/greet.json";
    }

    @Override
    public String getJsonValues() {
        final ArrayNode formItems = createArrayNode();
        for (int i = 0; i < mWelcomes.size(); i++) {
            formItems.add(createTextNode("welcome", mWelcomes.get(i)).put("goodbye", mGoodbyes.get(i)));
        }
        return createJsonNode("formItems", formItems).toString();
    }

    @Override
    public void updateConfiguration(JsonNode jsonNode) {
        mConfigurationManager.updateStringSet(PREF_WELCOME, jsonNode, "welcome");
        mConfigurationManager.updateStringSet(PREF_GOODBYE, jsonNode, "goodbye");
        loadConfiguration();
    }

// --------------------- Interface GreetPresenter ---------------------

    @Override
    public void onAnimationEnd() {
        RxUtil.delay(l -> mEventManager.post(getGreetEvent()), 5);
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(GreetView.class);
        loadConfiguration();
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewResume(final View view) {
        super.onViewResume(view);
        mView.displayGreet(getGreeting());
    }

    @Override
    protected GreetView getViewPlug() {
        return mView;
    }

    private Event getGreetEvent() {
        return new GreetEvent(isWelcome() ? TYPE_WELCOME : TYPE_GOODBYE);
    }

    private String getGreeting() {
        final String userName = mPreferenceManager.getUserName();
        return isWelcome() ? replace(randomValue(mWelcomes), TEMPLATE_USER, userName) :
                replace(randomValue(mGoodbyes), TEMPLATE_USER, userName);
    }

    private boolean isWelcome() {
        return getParams().containsValue(TYPE_WELCOME);
    }

    private void loadConfiguration() {
        mWelcomes = mConfigurationManager.getStringList(PREF_WELCOME, PREF_WELCOME_DEFAULT);
        mGoodbyes = mConfigurationManager.getStringList(PREF_GOODBYE, PREF_GOODBYE_DEFAULT);
    }
}

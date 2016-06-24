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
package com.vaporwarecorp.mirror.feature.splash;

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
import com.vaporwarecorp.mirror.feature.MainFeature;
import timber.log.Timber;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.vaporwarecorp.mirror.util.JsonUtil.*;
import static com.vaporwarecorp.mirror.util.RxUtil.delay;

@Plugin
@Provides(SplashPresenter.class)
public class SplashPresenterImpl extends AbstractFeaturePresenter<SplashView> implements SplashPresenter {
// ------------------------------ FIELDS ------------------------------

    private static final String PREF = SplashPresenter.class.getName();
    private static final String PREF_URLS = PREF + ".PREF_URLS";
    private static final List<String> PREF_URLS_DEFAULT = Arrays.asList(
            "http://k39.kn3.net/taringa/2/0/4/3/3/4/63/piledro/A12.gif",
            "http://i.giphy.com/rR2AWZ3ip77r2.gif",
            "http://i.giphy.com/vncgdgPWLwGRi.gif",
            "http://m.popkey.co/76b5e7/NlZaX.gif",
            "http://i.giphy.com/3Ow6njmLYdchW.gif",
            "http://i.giphy.com/tptFQ8QAJYYvu.gif",
            "http://i.giphy.com/pDLNJlazF9ljG.gif"
    );

    @Plug
    AppManager mAppManager;
    @Plug
    ConfigurationManager mConfigurationManager;
    @Plug
    MainFeature mMainFeature;
    @Plug
    SplashView mView;

    private List<String> mUrls;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Configuration ---------------------

    @Override
    public String getJsonConfiguration() {
        return "configuration/json/splash.json";
    }

    @Override
    public String getJsonValues() {
        final ArrayNode formItems = createArrayNode();
        for (String url : mUrls) {
            formItems.add(createTextNode("url", url));
        }
        return createJsonNode("formItems", formItems).toString();
    }

    @Override
    public void updateConfiguration(JsonNode jsonNode) {
        mConfigurationManager.updateStringSet(PREF_URLS, jsonNode, "url");
        loadConfiguration();
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(SplashView.class);
        loadConfiguration();
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewStart(final View view) {
        super.onViewStart(view);

        final String pictureUrl = mUrls.get(new Random().nextInt(mUrls.size()));
        Timber.d("Displaying %s", pictureUrl);
        mView.setPictureUrl(pictureUrl);

        isApplicationReady();
    }

    @Override
    protected SplashView getViewPlug() {
        return mView;
    }

    private void isApplicationReady() {
        delay(l -> {
            Timber.d("mAppManager.isBluetoothAvailable() %s", mAppManager.isBluetoothAvailable());
            Timber.d("mAppManager.isNetworkAvailable() %s", mAppManager.isNetworkAvailable());
            Timber.d("mAppManager.isLocationAvailable() %s", mAppManager.isLocationAvailable());
            if (mAppManager.isBluetoothAvailable() && mAppManager.isNetworkAvailable() &&
                    mAppManager.isLocationAvailable()) {
                mMainFeature.onApplicationReady();
            } else {
                isApplicationReady();
            }
        }, 20);
    }

    private void loadConfiguration() {
        mUrls = mConfigurationManager.getStringList(PREF_URLS, PREF_URLS_DEFAULT);
    }
}

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

package com.vaporwarecorp.mirror.feature.forecast;

import android.content.Intent;
import android.location.Location;
import com.fasterxml.jackson.databind.JsonNode;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.ConfigurationManager;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.ForecastEvent;
import com.vaporwarecorp.mirror.feature.common.AbstractMirrorManager;
import com.vaporwarecorp.mirror.feature.forecast.model.Forecast;
import com.vaporwarecorp.mirror.receiver.ForecastReceiver;
import com.vaporwarecorp.mirror.util.LocationUtil;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import java.util.Locale;

import static com.vaporwarecorp.mirror.util.JsonUtil.createTextNode;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(ForecastManager.class)
public class ForecastManagerImpl extends AbstractMirrorManager implements ForecastManager {
// ------------------------------ FIELDS ------------------------------

    private static final String PREF = ForecastManager.class.getName();
    private static final String PREF_API_KEY = PREF + ".PREF_API_KEY";

    @Plug
    AppManager mAppManager;
    @Plug
    ConfigurationManager mConfigurationManager;
    @Plug
    EventManager mEventManager;

    private Api mApi;
    private String mApiKey;
    private Location mLocation;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Configuration ---------------------

    @Override
    public String getJsonConfiguration() {
        return "configuration/json/forecastio.json";
    }

    @Override
    public String getJsonValues() {
        return createTextNode("apiKey", mApiKey).toString();
    }

    @Override
    public void updateConfiguration(JsonNode jsonNode) {
        mConfigurationManager.updateString(PREF_API_KEY, jsonNode, "apiKey");
        loadConfiguration();
    }

// --------------------- Interface ForecastManager ---------------------

    @SuppressWarnings("ResourceType")
    @Override
    public void retrieveForecast() {
        if (StringUtils.isEmpty(mApiKey)) {
            Timber.e("ForecastManager - No API key found");
            return;
        }

        if (mLocation == null) {
            Timber.e("ForecastManager - No last location found");
            return;
        }

        Timber.i("ForecastManager - refreshing forecast information");
        final String latitudeLongitude = String.format("%s,%s", mLocation.getLatitude(), mLocation.getLongitude());
        final String language = Locale.getDefault().getLanguage();
        final Subscription subscription = mApi
                .getForecast(latitudeLongitude, mApiKey, "us", language)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Forecast>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable t) {
                        Timber.e(t, "ForecastManager failed: %s", t.getMessage());
                    }

                    @Override
                    public void onNext(Forecast forecast) {
                        mEventManager.post(new ForecastEvent(forecast));
                    }
                });
        subscribe(subscription);
    }

// --------------------- Interface MirrorManager ---------------------

    @Override
    public void onFeaturePause() {
        mAppManager.cancelPendingIntent(new Intent(ForecastReceiver.FORECAST_UPDATE_INTENT));
    }

    @Override
    public void onFeatureResume() {
        if (mApiKey == null || mApi == null || mLocation == null) {
            return;
        }
        mAppManager.getAppContext().sendBroadcast(new Intent(ForecastReceiver.FORECAST_UPDATE_INTENT));
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        loadConfiguration();
    }

    /**
     * Load the configuration of the component.
     */
    private void loadConfiguration() {
        mApiKey = mConfigurationManager.getString(PREF_API_KEY, "");
        mApi = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .baseUrl("https://api.forecast.io/")
                .build()
                .create(Api.class);
        mLocation = LocationUtil.getLastKnownLocation(mAppManager.getAppContext());
        onFeatureStart();
    }

    private interface Api {
        @GET("forecast/{apiKey}/{latLon}")
        Observable<Forecast> getForecast(@Path(value = "latLon", encoded = true) String latLong, @Path("apiKey") String apiKey, @Query("units") String units, @Query("lang") String lang);
    }
}

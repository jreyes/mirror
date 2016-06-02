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

import android.content.Intent;
import android.location.Location;
import com.robopupu.api.component.AbstractManager;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.forecast.model.Forecast;
import com.vaporwarecorp.mirror.event.ForecastEvent;
import com.vaporwarecorp.mirror.receiver.ForecastReceiver;
import com.vaporwarecorp.mirror.util.LocationUtil;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import java.util.Locale;

@Plugin
public class ForecastManagerImpl extends AbstractManager implements ForecastManager {
// ------------------------------ FIELDS ------------------------------

    private static final String CLIENT_KEY = "ForecastIoKey";

    @Plug
    AppManager mAppManager;
    @Plug
    EventManager mEventManager;

    private Api mApi;
    private String mApiKey;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(ForecastManager.class)
    public ForecastManagerImpl() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface ForecastManager ---------------------

    @SuppressWarnings("ResourceType")
    @Override
    public void retrieveForecast() {
        Location location = LocationUtil.getLastKnownLocation(mAppManager.getAppContext());
        if (location == null) {
            Timber.e("ForecastManager - No last location found");
            return;
        }

        Timber.i("ForecastManager - refreshing forecast information");
        String latitudeLongitude = String.format("%s,%s", location.getLatitude(), location.getLongitude());
        String language = Locale.getDefault().getLanguage();
        mApi
                .getForecast(latitudeLongitude, mApiKey, "us", language)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Forecast>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable t) {
                        Timber.e(t, "ForecastManager failed: " + t.getMessage());
                    }

                    @Override
                    public void onNext(Forecast forecast) {
                        mEventManager.post(new ForecastEvent(forecast));
                    }
                });
    }

    @Override
    public void startReceiver() {
        mAppManager.getAppContext().sendBroadcast(new Intent(ForecastReceiver.FORECAST_UPDATE_INTENT));
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        initializeRetrofit();
    }

    private void initializeRetrofit() {
        mApiKey = mAppManager.getApplicationProperties().getProperty(CLIENT_KEY);
        mApi = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .baseUrl("https://api.forecast.io/")
                .build()
                .create(Api.class);
    }

    private interface Api {
        @GET("forecast/{apiKey}/{latLon}")
        Observable<Forecast> getForecast(@Path(value = "latLon", encoded = true) String latLong, @Path("apiKey") String apiKey, @Query("units") String units, @Query("lang") String lang);
    }
}

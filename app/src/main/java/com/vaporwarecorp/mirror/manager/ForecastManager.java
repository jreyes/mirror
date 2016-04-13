package com.vaporwarecorp.mirror.manager;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import com.vaporwarecorp.mirror.event.ForecastEvent;
import com.vaporwarecorp.mirror.receiver.ForecastReceiver;
import com.vaporwarecorp.mirror.vendor.forecast.model.Forecast;
import org.greenrobot.eventbus.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import timber.log.Timber;

import java.util.Locale;
import java.util.Properties;

public class ForecastManager {
// ------------------------------ FIELDS ------------------------------

    private static final String CLIENT_KEY = "ForecastIoKey";

    private Api mApi;
    private String mApiKey;
    private Context mContext;
    private LocationManager mLocationManager;

// --------------------------- CONSTRUCTORS ---------------------------

    public ForecastManager(Context context, Properties properties) {
        mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mApiKey = properties.getProperty(CLIENT_KEY);
        mApi = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.forecast.io/")
                .build()
                .create(Api.class);
    }

// -------------------------- OTHER METHODS --------------------------

    @SuppressWarnings("ResourceType")
    public void retrieveForecast() {
        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location == null) {
            Timber.e("ForecastManager - No last location found");
            return;
        }

        Timber.i("ForecastManager - refreshing forecast information");
        String latitudeLongitude = String.format("%s,%s", location.getLatitude(), location.getLongitude());
        String language = Locale.getDefault().getLanguage();
        mApi
                .getForecast(latitudeLongitude, mApiKey, "us", language)
                .enqueue(new Callback<Forecast>() {
                    @Override
                    public void onResponse(Call<Forecast> call, Response<Forecast> response) {
                        if (!response.isSuccessful()) {
                            Timber.e(call.request().url() + ": failed: " + response.code());
                            return;
                        }
                        EventBus.getDefault().post(new ForecastEvent(response.body()));
                    }

                    @Override
                    public void onFailure(Call<Forecast> call, Throwable t) {
                        Timber.e(t, call.request().url() + ": failed: " + t.getMessage());
                    }
                });
    }

    public void update() {
        mContext.sendBroadcast(new Intent(ForecastReceiver.FORECAST_UPDATE_INTENT));
    }

    private interface Api {
        @GET("forecast/{apiKey}/{latLon}")
        Call<Forecast> getForecast(@Path(value = "latLon", encoded = true) String latLong, @Path("apiKey") String apiKey, @Query("units") String units, @Query("lang") String lang);
    }
}

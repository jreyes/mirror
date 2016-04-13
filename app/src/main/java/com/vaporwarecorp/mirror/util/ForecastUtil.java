package com.vaporwarecorp.mirror.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import com.vaporwarecorp.mirror.R;

public class ForecastUtil {
// -------------------------- STATIC METHODS --------------------------

    public static String getIconResource(String icon, Context context) {
        switch (icon) {
            case "rain":
                return context.getString(R.string.rain);
            case "clear-day":
                return context.getString(R.string.day_sunny);
            case "clear-night":
                return context.getString(R.string.clear_night);
            case "snow":
                return context.getString(R.string.snow);
            case "sleet":
                return context.getString(R.string.sleet);
            case "wind":
                return context.getString(R.string.strong_wind);
            case "fog":
                return context.getString(R.string.fog);
            case "cloudy":
                return context.getString(R.string.cloudy);
            case "partly-cloudy-day":
                return context.getString(R.string.day_cloudy);
            case "partly-cloudy-night":
                return context.getString(R.string.night_cloudy);
            case "hail":
                return context.getString(R.string.day_hail);
            case "thunderstorm":
                return context.getString(R.string.thunderstorms);
            case "tornado":
                return context.getString(R.string.tornado);
            case "umbrella":
                return context.getString(R.string.umbrella);
            default:
                return context.getString(R.string.day_sunny);
        }
    }
}

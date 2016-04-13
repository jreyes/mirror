package com.vaporwarecorp.mirror.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.vaporwarecorp.mirror.MirrorApp;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class ForecastReceiver extends BroadcastReceiver {
// ------------------------------ FIELDS ------------------------------

    public static final String FORECAST_UPDATE_INTENT = "com.vaporwarecorp.mirror.FORECAST_UPDATE";
    private static final long FORECAST_UPDATE_INTERVAL = AlarmManager.INTERVAL_HOUR;

// -------------------------- OTHER METHODS --------------------------

    @Override
    public void onReceive(Context context, Intent intent) {
        MirrorApp.forecast(context).retrieveForecast();

        boolean alarmUp = (PendingIntent.getBroadcast(context, 0,
                new Intent(FORECAST_UPDATE_INTENT),
                PendingIntent.FLAG_NO_CREATE) != null);

        if (!alarmUp) {
            Intent updateIntent = new Intent(FORECAST_UPDATE_INTENT);
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    FORECAST_UPDATE_INTERVAL,
                    FORECAST_UPDATE_INTERVAL,
                    PendingIntent.getBroadcast(context, 0, updateIntent, FLAG_UPDATE_CURRENT)
            );
        }
    }
}

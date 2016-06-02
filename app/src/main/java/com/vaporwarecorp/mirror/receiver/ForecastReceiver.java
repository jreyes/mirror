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
package com.vaporwarecorp.mirror.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.robopupu.api.dependency.D;
import com.vaporwarecorp.mirror.component.ForecastManager;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class ForecastReceiver extends BroadcastReceiver {
// ------------------------------ FIELDS ------------------------------

    public static final String FORECAST_UPDATE_INTENT = "com.vaporwarecorp.mirror.FORECAST_UPDATE";
    private static final long FORECAST_UPDATE_INTERVAL = AlarmManager.INTERVAL_HOUR;

// -------------------------- OTHER METHODS --------------------------

    @Override
    public void onReceive(Context context, Intent intent) {
        // retrieve the forecast
        D.get(ForecastManager.class).retrieveForecast();

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

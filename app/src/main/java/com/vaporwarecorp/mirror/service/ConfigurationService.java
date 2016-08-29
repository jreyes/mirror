package com.vaporwarecorp.mirror.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.app.Constants;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.configuration.Configuration;
import com.vaporwarecorp.mirror.component.configuration.WebServer;
import timber.log.Timber;

public class ConfigurationService extends Service {
// ------------------------------ FIELDS ------------------------------

    private WebServer server;

// -------------------------- OTHER METHODS --------------------------

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.CONFIGURATION_SERVICE_START)) {
            Timber.i("Starting configuration service");

            final AppManager appManager = PluginBus.getPlug(AppManager.class);
            server = new WebServer(appManager.getAppContext());
            server.start(PluginBus.getPlugs(Configuration.class));

            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Mirror Configuration Service")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setOngoing(true)
                    .build();

            startForeground(Constants.NOTIFICATION_ID.CONFIGURATION_SERVICE, notification);
        } else if (intent.getAction().equals(Constants.ACTION.CONFIGURATION_SERVICE_STOP)) {
            Timber.i("Stopping configuration service");

            if (server != null) {
                if (server.isAlive()) {
                    server.stop();
                }
                server = null;
            }

            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }
}

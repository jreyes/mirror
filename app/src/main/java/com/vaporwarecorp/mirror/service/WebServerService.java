package com.vaporwarecorp.mirror.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.configuration.Configuration;
import com.vaporwarecorp.mirror.component.configuration.WebServer;
import timber.log.Timber;

import static com.vaporwarecorp.mirror.app.Constants.ACTION.WEB_SERVER_SERVICE_START;
import static com.vaporwarecorp.mirror.app.Constants.ACTION.WEB_SERVER_SERVICE_STOP;

public class WebServerService extends Service {
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
        if (intent.getAction().equals(WEB_SERVER_SERVICE_START) && server == null) {
            Timber.i("Starting web server service");
            final AppManager appManager = PluginBus.getPlug(AppManager.class);
            server = new WebServer(appManager.getAppContext());
            server.start(PluginBus.getPlugs(Configuration.class));
        } else if (intent.getAction().equals(WEB_SERVER_SERVICE_STOP)) {
            Timber.i("Stopping web server service");
            if (server != null) {
                if (server.isAlive()) {
                    server.stop();
                }
                server = null;
            }
            stopSelf();
        }
        return START_STICKY;
    }
}

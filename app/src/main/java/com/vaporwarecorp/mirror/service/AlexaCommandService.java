package com.vaporwarecorp.mirror.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Regions;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.app.Constants;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.AlexaCommandEvent;
import com.vaporwarecorp.mirror.feature.alexa.AlexaCommandManager;
import org.json.JSONObject;
import timber.log.Timber;

import java.util.UUID;

public class AlexaCommandService extends Service {
// ------------------------------ FIELDS ------------------------------

    private AWSIotMqttManager iotMqttManager;

// -------------------------- OTHER METHODS --------------------------

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.ALEXA_COMMAND_SERVICE_START)) {
            Timber.i("Starting Alexa command service");

            startManager(intent);

            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Mirror Alexa Command Service")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setOngoing(true)
                    .build();

            startForeground(Constants.NOTIFICATION_ID.ALEXA_COMMAND_SERVICE, notification);
        } else if (intent.getAction().equals(Constants.ACTION.ALEXA_COMMAND_SERVICE_STOP)) {
            Timber.i("Stopping Alexa command service");
            stopManager();
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private String getCommand(byte[] data) {
        try {
            final String message = new String(data, "UTF-8");
            return new JSONObject(message).getString("command");
        } catch (Exception e) {
            return "";
        }
    }

    private void startManager(Intent intent) {
        final String mCognitoPoolId = intent.getStringExtra(AlexaCommandManager.PREF_COGNITO_POOL_ID);
        final String mIotEndpoint = intent.getStringExtra(AlexaCommandManager.PREF_IOT_ENDPOINT);
        final String mIotTopic = intent.getStringExtra(AlexaCommandManager.PREF_IOT_TOPIC);

        final Regions region = Regions.US_EAST_1;
        final CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), mCognitoPoolId, region
        );
        final AWSIotMqttLastWillAndTestament lwt = new AWSIotMqttLastWillAndTestament(mIotTopic,
                "Android client lost connection", AWSIotMqttQos.QOS0);
        final String clientId = UUID.randomUUID().toString();
        final EventManager eventManager = PluginBus.getPlug(EventManager.class);

        iotMqttManager = new AWSIotMqttManager(clientId, mIotEndpoint);
        iotMqttManager.setKeepAlive(30);
        iotMqttManager.setMqttLastWillAndTestament(lwt);
        iotMqttManager.resetReconnect();
        try {
            iotMqttManager.connect(credentialsProvider, (status, throwable) -> {
                Timber.d("Status = %s", status);
                if (throwable != null) {
                    Timber.e(throwable, throwable.getMessage());
                }
                if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {
                    iotMqttManager.subscribeToTopic(mIotTopic, AWSIotMqttQos.QOS0, (topic, data) -> {
                        final String command = getCommand(data);
                        Timber.d("Command arrived %s", command);
                        eventManager.post(new AlexaCommandEvent(command));
                    });
                }
            });
        } catch (AmazonClientException e) {
            Timber.e(e, e.getMessage());
            throw e;
        }
    }

    private void stopManager() {
        if (iotMqttManager == null) {
            return;
        }

        iotMqttManager.disconnect();
        iotMqttManager = null;
    }
}

/*
 *   Copyright 2016 Johann Reyes
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.vaporwarecorp.mirror.feature.artik;

import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.ArtikEvent;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import cloud.artik.model.Acknowledgement;
import cloud.artik.model.ActionOut;
import cloud.artik.model.MessageIn;
import cloud.artik.model.MessageOut;
import cloud.artik.model.RegisterMessage;
import cloud.artik.model.WebSocketError;
import cloud.artik.websocket.ArtikCloudWebSocketCallback;
import cloud.artik.websocket.DeviceChannelWebSocket;
import cloud.artik.websocket.FirehoseWebSocket;
import rx.Observable;
import timber.log.Timber;

public class ArtikCloudManager {
// ------------------------------ FIELDS ------------------------------

    private String mAccessToken;
    private String mApplicationId;
    private String mControlDeviceId;
    private DeviceChannelWebSocket mDeviceChannelWS;
    private EventManager mEventManager;
    private FirehoseWebSocket mFirehoseWS;
    private String mSidekickDeviceId;

// --------------------------- CONSTRUCTORS ---------------------------

    public ArtikCloudManager(EventManager eventManager,
                             String applicationId,
                             String sidekickDeviceId,
                             String controlDeviceId,
                             String accessToken) {
        mEventManager = eventManager;
        mApplicationId = applicationId;
        mSidekickDeviceId = sidekickDeviceId;
        mControlDeviceId = controlDeviceId;
        mAccessToken = accessToken;
    }

// -------------------------- OTHER METHODS --------------------------

    public Observable<String> sendAction(Map<String, Object> parameters) {
        return Observable.create(subscriber -> {
            final MessageIn messageIn = new MessageIn();
            messageIn.setCid(mApplicationId);
            messageIn.setData(parameters);
            messageIn.setSdid(mSidekickDeviceId);
            messageIn.setTs(System.currentTimeMillis());
            try {
                mDeviceChannelWS.sendMessage(messageIn);
                Timber.d("DeviceChannelWebSocket sendAction: %s", messageIn.toString());
                subscriber.onNext(messageIn.toString());
            } catch (IOException e) {
                Timber.e(e, "::doInBackground run into Exception");
                subscriber.onError(e);
            } finally {
                subscriber.onCompleted();
            }
        });
    }

    public void startMessaging() {
        connectDeviceChannelWS();
        connectFirehoseWS();
    }

    public void stopMessaging() {
        disconnectFirehoseWS();
        disconnectDeviceChannelWS();
    }

    private void broadcastAction(MessageOut messageOut) {
        final Map<String, Object> data = messageOut.getData();
        if (data == null) {
            return;
        }

        final String action = String.valueOf(data.get("action"));
        final ArtikEvent event;
        switch (action) {
            case "down":
                event = new ArtikEvent(ArtikEvent.TYPE_ACTION_DOWN);
                break;
            case "home":
                event = new ArtikEvent(ArtikEvent.TYPE_ACTION_HOME);
                break;
            case "left":
                event = new ArtikEvent(ArtikEvent.TYPE_ACTION_LEFT);
                break;
            case "right":
                event = new ArtikEvent(ArtikEvent.TYPE_ACTION_RIGHT);
                break;
            case "up":
                event = new ArtikEvent(ArtikEvent.TYPE_ACTION_UP);
                break;
            default:
                event = null;
                break;
        }

        if (event != null) {
            mEventManager.post(event);
        }
    }

    private void connectDeviceChannelWS() {
        createDeviceChannelWebSockets();
        try {
            mDeviceChannelWS.connect();
        } catch (IOException e) {
            Timber.e(e, "Error connecting to the device channel WS");
        }
    }

    private void connectFirehoseWS() {
        createFirehoseWebsocket();
        try {
            mFirehoseWS.connect();
        } catch (IOException e) {
            Timber.e(e, "Error connecting to the firehose WS");
        }
    }

    private void createDeviceChannelWebSockets() {
        try {
            mDeviceChannelWS = new DeviceChannelWebSocket(true, new ArtikCloudWebSocketCallback() {
                @Override
                public void onOpen(int i, String s) {
                    Timber.d("DeviceChannelWebSocket::onOpen: registering %s", mSidekickDeviceId);

                    RegisterMessage registerMessage = new RegisterMessage();
                    registerMessage.setAuthorization("bearer " + mAccessToken);
                    registerMessage.setCid(mApplicationId);
                    registerMessage.setSdid(mSidekickDeviceId);

                    try {
                        mDeviceChannelWS.registerChannel(registerMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onMessage(MessageOut messageOut) {
                }

                @Override
                public void onAction(ActionOut actionOut) {
                }

                @Override
                public void onAck(Acknowledgement acknowledgement) {
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Timber.w("DeviceChannelWebSocket::onClose: code: %s; reason: %s", code, reason);
                    mDeviceChannelWS = null;
                }

                @Override
                public void onError(WebSocketError error) {
                    Timber.e("DeviceChannelWebSocket::onError: %s", error.getMessage());
                }

                @Override
                public void onPing(long timestamp) {
                }
            });
        } catch (URISyntaxException | IOException e) {
            Timber.e(e, e.getMessage());
        }
    }

    private void createFirehoseWebsocket() {
        try {
            mFirehoseWS = new FirehoseWebSocket(mAccessToken, mControlDeviceId, null, null, null, new ArtikCloudWebSocketCallback() {
                @Override
                public void onOpen(int i, String s) {
                    Timber.d("FirehoseWebSocket::onOpen: registering %s", mControlDeviceId);
                }

                @Override
                public void onMessage(MessageOut messageOut) {
                    Timber.d("FirehoseWebSocket::onMessage: %s", messageOut.toString());
                    broadcastAction(messageOut);
                }

                @Override
                public void onAction(ActionOut actionOut) {
                }

                @Override
                public void onAck(Acknowledgement acknowledgement) {
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Timber.w("FirehoseWebSocket::onClose: code: %s; reason: %s", code, reason);
                    mFirehoseWS = null;
                }

                @Override
                public void onError(WebSocketError error) {
                    Timber.e("FirehoseWebSocket::onError: %s", error.getMessage());
                }

                @Override
                public void onPing(long timestamp) {
                }
            });
        } catch (URISyntaxException | IOException e) {
            Timber.e(e, e.getMessage());
        }
    }

    private void disconnectDeviceChannelWS() {
        new Thread(() -> {
            try {
                if (mDeviceChannelWS != null) {
                    mDeviceChannelWS.close();
                }
                mDeviceChannelWS = null;
            } catch (IOException e) {
                Timber.e(e, "Error disconnecting from the device channel WS");
            }
        }).start();
    }

    private void disconnectFirehoseWS() {
        new Thread(() -> {
            try {
                if (mFirehoseWS != null) {
                    mFirehoseWS.close();
                }
                mFirehoseWS = null;
            } catch (IOException e) {
                Timber.e(e, "Error disconnecting from the firehose WS");
            }
        }).start();
    }
}

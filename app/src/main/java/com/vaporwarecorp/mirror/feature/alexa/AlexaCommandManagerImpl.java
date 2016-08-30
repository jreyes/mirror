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
package com.vaporwarecorp.mirror.feature.alexa;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.RelativeLayout;
import com.fasterxml.jackson.databind.JsonNode;
import com.hound.android.fd.view.SearchPanelView;
import com.robopupu.api.dependency.D;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.ConfigurationManager;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.component.PluginFeatureManager;
import com.vaporwarecorp.mirror.component.configuration.Configuration;
import com.vaporwarecorp.mirror.event.AlexaCommandEvent;
import com.vaporwarecorp.mirror.event.SpeechEvent;
import com.vaporwarecorp.mirror.feature.Command;
import com.vaporwarecorp.mirror.feature.common.AbstractMirrorManager;
import com.vaporwarecorp.mirror.feature.main.MainView;
import com.vaporwarecorp.mirror.service.AlexaCommandService;
import com.vaporwarecorp.mirror.util.DisplayMetricsUtil;
import com.willblaschko.android.alexa.AlexaManager;
import com.willblaschko.android.alexa.audioplayer.AlexaAudioPlayer;
import com.willblaschko.android.alexa.callbacks.AsyncCallback;
import com.willblaschko.android.alexa.callbacks.AuthorizationCallback;
import com.willblaschko.android.alexa.interfaces.AvsItem;
import com.willblaschko.android.alexa.interfaces.AvsResponse;
import com.willblaschko.android.alexa.interfaces.audioplayer.AvsPlayContentItem;
import com.willblaschko.android.alexa.interfaces.audioplayer.AvsPlayRemoteItem;
import com.willblaschko.android.alexa.interfaces.playbackcontrol.AvsReplaceAllItem;
import com.willblaschko.android.alexa.interfaces.playbackcontrol.AvsReplaceEnqueuedItem;
import com.willblaschko.android.alexa.interfaces.playbackcontrol.AvsStopItem;
import com.willblaschko.android.alexa.interfaces.speechrecognizer.AvsExpectSpeechItem;
import com.willblaschko.android.alexa.interfaces.speechsynthesizer.AvsSpeakItem;
import com.willblaschko.android.alexa.requestbody.DataRequestBody;
import ee.ioc.phon.android.speechutils.RawAudioRecorder;
import okio.BufferedSink;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import timber.log.Timber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.hound.android.fd.view.SearchPanelView.State.LISTENING;
import static com.hound.android.fd.view.SearchPanelView.State.SEARCHING;
import static com.vaporwarecorp.mirror.app.Constants.ACTION.ALEXA_COMMAND_SERVICE_START;
import static com.vaporwarecorp.mirror.util.JsonUtil.createTextNode;
import static solid.stream.Stream.stream;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(AlexaCommandManager.class)
public class AlexaCommandManagerImpl extends AbstractMirrorManager implements AlexaCommandManager {
// ------------------------------ FIELDS ------------------------------

    private static final int AUDIO_RATE = 16000;
    private final static int STATE_FINISHED = 0;
    private final static int STATE_LISTENING = 1;
    private final static int STATE_PROCESSING = 2;

    @Plug
    AppManager mAppManager;
    @Plug
    ConfigurationManager mConfigurationManager;
    @Plug
    EventManager mEventManager;
    @Plug
    PluginFeatureManager mFeatureManager;

    private AlexaManager mAlexaManager;
    private AlexaAudioPlayer mAudioPlayer;
    private AlexaAudioPlayer.Callback mAudioPlayerCallback = new AlexaAudioPlayer.Callback() {
        @Override
        public void playerPrepared(AvsItem pendingItem) {
        }

        @Override
        public void itemComplete(AvsItem completedItem) {
            mAvsQueue.remove(completedItem);
            checkQueue();
        }

        @Override
        public void playerProgress(AvsItem currentItem, long offsetInMilliseconds, float percent) {
        }

        @Override
        public boolean playerError(AvsItem item, int what, int extra) {
            removeVoiceView();
            return false;
        }

        @Override
        public void dataError(AvsItem item, Exception e) {
        }
    };
    private AsyncCallback<AvsResponse, Exception> mAudioRequestCallback = new AsyncCallback<AvsResponse, Exception>() {
        @Override
        public void start() {
            setState(STATE_PROCESSING);
            addVoiceView();
        }

        @Override
        public void success(AvsResponse result) {
            handleResponse(result);
        }

        @Override
        public void failure(Exception error) {
            Timber.e(error, "Voice Error");
            setState(STATE_FINISHED);
            removeVoiceView();
            mEventManager.post(new SpeechEvent(error.getMessage()));
        }

        @Override
        public void complete() {
            stopListening();
        }
    };
    private Deque<AvsItem> mAvsQueue;
    private String mCognitoPoolId;
    private Collection<AlexaCommand> mCommands;
    private String mIotEndpoint;
    private String mIotTopic;
    private RawAudioRecorder mRecorder;
    private DataRequestBody mRequestBody = new DataRequestBody() {
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            //while our recorder is not null and it is still recording, keep writing to POST data
            int count = 0;
            final long timeout = System.currentTimeMillis() + (90 * 1000);
            while (mRecorder != null && !mRecorder.isPausing()) {
                final float rmsdb = mRecorder.getRmsdb();
                Timber.d("Recorder rmsdb %s", rmsdb);
                mSearchPanelView.setVolume(Math.round(rmsdb));
                if (sink != null) {
                    sink.write(mRecorder.consumeRecording());
                }
                if (rmsdb > 25F) {
                    count = 0;
                }
                if (count > 400 || System.currentTimeMillis() > timeout) {
                    stopListening();
                }
                count++;

                //sleep and do it all over again
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            stopListening();
        }
    };
    private SearchPanelView mSearchPanelView;

// --------------------------- CONSTRUCTORS ---------------------------

    public AlexaCommandManagerImpl() {
        mCommands = new ArrayList<>();
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface CommandManager ---------------------

    @Override
    public void start() {
        if ("".equals(mCognitoPoolId) || "".equals(mIotEndpoint) || "".equals(mIotTopic)) {
            Timber.w("Tried to start Alexa commands but is hasn't been configured");
            return;
        }

        Timber.d("Starting Alexa commands");
        //get our AlexaManager instance for convenience
        mAlexaManager = AlexaManager.getInstance(mAppManager.getAppContext(), "MirrorApp");
        mAlexaManager.checkLoggedIn(new AsyncCallback<Boolean, Throwable>() {
            @Override
            public void start() {
            }

            @Override
            public void success(Boolean result) {
                initializeAlexa();
                initializePubSub();
                initializeCommands();
            }

            @Override
            public void failure(Throwable error) {
                mAlexaManager.logIn(new AuthorizationCallback() {
                    @Override
                    public void onCancel() {
                        stop();
                    }

                    @Override
                    public void onSuccess() {
                        initializeAlexa();
                        initializePubSub();
                        initializeCommands();
                    }

                    @Override
                    public void onError(Exception e) {
                        Timber.e(e, "Error trying to login to amazon");
                        stop();
                    }
                });
            }

            @Override
            public void complete() {
            }
        });

        createSearchPanelView();
    }

    @Override
    public void stop() {
        Timber.d("Stopping Alexa commands");
        stopPubSub();
        stopListening();
        if (mAudioPlayer != null) {
            mAudioPlayer.removeCallback(mAudioPlayerCallback);
            mAudioPlayer = null;
        }
        mCommands.clear();
        mAlexaManager = null;
    }

    @Override
    public void voiceSearch() {
        // if AlexaManager hasn't started, do nothing
        if (mAlexaManager == null) {
            return;
        }

        stopListening();

        mRecorder = new RawAudioRecorder(AUDIO_RATE);
        mRecorder.start();
        mAlexaManager.sendAudioRequest(mRequestBody, mAudioRequestCallback);
    }

// --------------------- Interface Configuration ---------------------

    @Override
    public String getJsonConfiguration() {
        return "configuration/json/alexa.json";
    }

    @Override
    public String getJsonValues() {
        return createTextNode("cognitoPoolId", mCognitoPoolId)
                .put("iotEndpoint", mIotEndpoint)
                .put("iotTopic", mIotTopic)
                .toString();
    }

    @Override
    public void updateConfiguration(JsonNode jsonNode) {
        mConfigurationManager.updateString(PREF_COGNITO_POOL_ID, jsonNode, "cognitoPoolId");
        mConfigurationManager.updateString(PREF_IOT_ENDPOINT, jsonNode, "iotEndpoint");
        mConfigurationManager.updateString(PREF_IOT_TOPIC, jsonNode, "iotTopic");
        loadConfiguration();
        start();
    }

// --------------------- Interface MirrorManager ---------------------

    @Override
    public void onFeatureResult(int requestCode, int resultCode, Intent data) {
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        loadConfiguration();
    }

// -------------------------- OTHER METHODS --------------------------

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(AlexaCommandEvent event) {
        stream(mCommands)
                .filter(c -> c.matches(event.getCommand()))
                .forEach(c -> c.executeCommand(event.getCommand()));
    }

    private void addVoiceView() {
        Activity activity = mFeatureManager.getForegroundActivity();
        if (activity instanceof MainView) {
            Timber.d("Adding pulse view");
            new Handler(Looper.getMainLooper())
                    .post(() -> {
                        if (mSearchPanelView.getParent() == null) {
                            ((MainView) activity).getRootView().addView(mSearchPanelView);
                        }
                        mSearchPanelView.changeState(LISTENING, true);
                    });
        }
    }

    private void checkQueue() {
        //if we're out of things, hang up the phone and move on
        if (mAvsQueue.size() == 0) {
            mEventManager.post(new SpeechEvent(""));
            removeVoiceView();
            return;
        }

        AvsItem current = mAvsQueue.removeFirst();
        Timber.d("Got %s", ToStringBuilder.reflectionToString(current, ToStringStyle.MULTI_LINE_STYLE));
        if (current instanceof AvsPlayRemoteItem) {
            removeVoiceView();
            //play a URL
            if (!mAudioPlayer.isPlaying()) {
                mAudioPlayer.playItem((AvsPlayRemoteItem) current);
            }
        } else if (current instanceof AvsPlayContentItem) {
            removeVoiceView();
            //play a URL
            if (!mAudioPlayer.isPlaying()) {
                mAudioPlayer.playItem((AvsPlayContentItem) current);
            }
        } else if (current instanceof AvsSpeakItem) {
            //play a sound file
            if (!mAudioPlayer.isPlaying()) {
                mAudioPlayer.playItem((AvsSpeakItem) current);
            }
        } else if (current instanceof AvsStopItem) {
            removeVoiceView();
            //stop our play
            mAudioPlayer.stop();
            mAvsQueue.remove(current);
        } else if (current instanceof AvsReplaceAllItem) {
            removeVoiceView();
            mAudioPlayer.stop();
            mAvsQueue.remove(current);
        } else if (current instanceof AvsReplaceEnqueuedItem) {
            removeVoiceView();
            mAvsQueue.remove(current);
        } else if (current instanceof AvsExpectSpeechItem) {
            //listen for user input
            mAudioPlayer.stop();
            voiceSearch();
        } else {
            removeVoiceView();
            mAudioPlayer.stop();
        }
    }

    private void createSearchPanelView() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(MATCH_PARENT, DisplayMetricsUtil.convertDpToPixel(171f, mAppManager.getAppContext()));
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.bottomMargin = 0;

        mSearchPanelView = new SearchPanelView(mAppManager.getAppContext());
        mSearchPanelView.setLayoutParams(params);
    }

    /**
     * Handle the response sent back from Alexa's parsing of the Intent, these can be any of the AvsItem types (play, speak, stop, clear, listen)
     *
     * @param response a List<AvsItem> returned from the mAlexaManager.sendTextRequest() call in sendVoiceToAlexa()
     */
    private void handleResponse(AvsResponse response) {
        new Handler(Looper.getMainLooper()).post(() -> mSearchPanelView.changeState(SEARCHING, true));

        if (response != null) {
            //if we have a clear queue item in the list, we need to clear the current queue before proceeding
            //iterate backwards to avoid changing our array positions and getting all the nasty errors that come
            //from doing that
            for (int i = response.size() - 1; i >= 0; i--) {
                if (response.get(i) instanceof AvsReplaceAllItem || response.get(i) instanceof AvsReplaceEnqueuedItem) {
                    //clear our queue
                    mAvsQueue.clear();
                    //remove item
                    response.remove(i);
                }
            }
            Timber.i("Adding %s items to our queue", response.size());
            mAvsQueue.addAll(response);
        }
        checkQueue();
    }

    private void initializeAlexa() {
        Timber.d("Initializing Alexa module");
        mAvsQueue = new LinkedBlockingDeque<>();

        //instantiate our audio player
        mAudioPlayer = AlexaAudioPlayer.getInstance(mAppManager.getAppContext());
        mAudioPlayer.addCallback(mAudioPlayerCallback);
    }

    private void initializeCommands() {
        if (mCommands.isEmpty()) {
            stream(D.getAll(Command.class))
                    .filter(c -> c instanceof AlexaCommand)
                    .forEach(c -> {
                        PluginBus.plug(c);
                        mCommands.add((AlexaCommand) c);
                        Timber.i("loaded %s alexa command", c.getClass().getCanonicalName());
                    });
        }
    }

    private void initializePubSub() {
        Timber.d("Initializing Pub/Sub module");

        mEventManager.register(this);

        Intent intent = new Intent(mAppManager.getAppContext(), AlexaCommandService.class)
                .setAction(ALEXA_COMMAND_SERVICE_START)
                .putExtra(PREF_COGNITO_POOL_ID, mCognitoPoolId)
                .putExtra(PREF_IOT_ENDPOINT, mIotEndpoint)
                .putExtra(PREF_IOT_TOPIC, mIotTopic);
        mAppManager.getAppContext().startService(intent);
    }

    /**
     * Load the configuration of the component.
     */
    private void loadConfiguration() {
        mCognitoPoolId = mConfigurationManager.getString(PREF_COGNITO_POOL_ID, "");
        mIotEndpoint = mConfigurationManager.getString(PREF_IOT_ENDPOINT, "");
        mIotTopic = mConfigurationManager.getString(PREF_IOT_TOPIC, "");
    }

    private void removeVoiceView() {
        Activity activity = mFeatureManager.getForegroundActivity();
        if (activity instanceof MainView) {
            Timber.d("Removing pulse view");
            new Handler(Looper.getMainLooper())
                    .post(() -> ((MainView) activity).getRootView().removeView(mSearchPanelView));
        }
    }

    private void setState(final int state) {
        switch (state) {
            case (STATE_LISTENING):
                voiceSearch();
                break;
            case STATE_FINISHED:
                stopListening();
                break;
        }
    }

    private void stopListening() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    private void stopPubSub() {
        mEventManager.unregister(this);

        Intent intent = new Intent(mAppManager.getAppContext(), AlexaCommandService.class)
                .setAction(ALEXA_COMMAND_SERVICE_START);
        mAppManager.getAppContext().startService(intent);
    }
}

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

package com.vaporwarecorp.mirror.feature.speechtotext;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.robopupu.api.dependency.D;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.CommandEvent;
import com.vaporwarecorp.mirror.event.SpeechEvent;
import com.vaporwarecorp.mirror.feature.Command;
import com.vaporwarecorp.mirror.feature.common.AbstractMirrorManager;

import java.util.Collection;
import java.util.List;

import solid.functions.Action1;
import timber.log.Timber;

import static solid.collectors.ToList.toList;
import static solid.stream.Stream.stream;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(SpeechToTextManager.class)
public class SpeechToTextManagerImpl extends AbstractMirrorManager implements SpeechToTextManager {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
    @Plug
    EventManager mEventManager;

    private List<SpeechToTextCommand> mCommands;
    private Intent mIntent;
    private RecognitionListener mRecognitionListener;
    private SpeechRecognizer mRecognizer;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface CommandManager ---------------------

    @Override
    public boolean isEnabled() {
        return mRecognizer != null && SpeechRecognizer.isRecognitionAvailable(mAppManager.getAppContext());
    }

    @Override
    public void start() {
        Timber.d("start");
        if (mIntent != null) {
            return;
        }

        initializeCommands();
        initializeIntent();
        initializeRecognitionListener();

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(mAppManager.getAppContext());
        mRecognizer.setRecognitionListener(mRecognitionListener);
    }

    @Override
    public void stop() {
        Timber.d("stop");
        removeVoiceView();

        mCommands.clear();
        mCommands = null;

        mRecognitionListener = null;

        mIntent = null;

        // check if there was a SpeechRecognizer
        if (mRecognizer == null) {
            return;
        }

        mRecognizer.setRecognitionListener(null);
        mRecognizer.destroy();
        mRecognizer = null;
    }

    public void voiceSearch() {
        Timber.d("voiceSearch");
        mRecognizer.startListening(mIntent);
    }

    private void initializeCommands() {
        mCommands = stream(D.getAll(Command.class))
                .filter(c -> c instanceof SpeechToTextCommand)
                .map(c -> {
                    if (!PluginBus.isPlugged(c)) {
                        PluginBus.plug(c);
                    }
                    Timber.i("loaded %s google command", c.getClass().getCanonicalName());
                    return (SpeechToTextCommand) c;
                })
                .collect(toList());
    }

    private void initializeIntent() {
        mIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mAppManager.getPackageName());
        mIntent.putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, true);
        mIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
    }

    private void initializeRecognitionListener() {
        mRecognitionListener = new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                final String volume = String.valueOf(Math.round(rmsdB) * 2);
                mEventManager.post(new CommandEvent(CommandEvent.TYPE_COMMAND_VOLUME, volume));
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
                mEventManager.post(new CommandEvent(CommandEvent.TYPE_COMMAND_SEARCHING, KEYWORD_GOOGLE));
                removeVoiceView();
            }

            @Override
            public void onError(int error) {
                Timber.d("onError");
                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        Timber.d("ERROR_AUDIO");
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        Timber.d("ERROR_CLIENT");
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        Timber.d("ERROR_RECOGNIZER_BUSY");
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        Timber.d("ERROR_INSUFFICIENT_PERMISSIONS");
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        Timber.d("ERROR_NETWORK_TIMEOUT");
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        Timber.d("ERROR_NETWORK");
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        Timber.d("ERROR_SERVER");
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        Timber.d("ERROR_NO_MATCH");
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        Timber.d("ERROR_SPEECH_TIMEOUT");
                        break;
                    default:
                        return;
                }
                mRecognizer.cancel();
                removeVoiceView();
            }

            @Override
            public void onResults(Bundle bundle) {
                mRecognizer.cancel();

                final Collection<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (results == null || results.isEmpty() || mCommands == null || mCommands.isEmpty()) {
                    return;
                }
                for (String result : results) {
                    if ("".equals(result)) {
                        continue;
                    }
                    Timber.d("onResults '%s'", result);
                    stream(mCommands).filter(c -> c.matches(result)).forEach((Action1<SpeechToTextCommand>) c -> {
                        c.executeCommand(result);
                    });
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        };
    }

    private void removeVoiceView() {
        Timber.d("removeVoiceView");
        mEventManager.post(new CommandEvent(CommandEvent.TYPE_COMMAND_STOP, KEYWORD_GOOGLE));
        mEventManager.post(new SpeechEvent(""));
    }
}

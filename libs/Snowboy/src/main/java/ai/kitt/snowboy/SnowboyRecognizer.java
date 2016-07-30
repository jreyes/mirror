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
package ai.kitt.snowboy;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

public class SnowboyRecognizer {
// ------------------------------ FIELDS ------------------------------

    private static final float BUFFER_SIZE_SECONDS = 0.4f;
    private static final String TAG = SnowboyRecognizer.class.getName();

    private int bufferSize;
    private final Collection<RecognitionListener> listeners;
    private final Handler mainHandler;
    private Thread recognizerThread;
    private AudioRecord recorder;
    private SnowboyDetect snowboyDetector;

// -------------------------- STATIC METHODS --------------------------

    static {
        System.loadLibrary("snowboy-detect-android");
    }

// --------------------------- CONSTRUCTORS ---------------------------

    public SnowboyRecognizer(String resourcePath, String modelPath) throws IOException {
        listeners = new HashSet<>();
        mainHandler = new Handler(Looper.getMainLooper());

        snowboyDetector = new SnowboyDetect(resourcePath, modelPath);
        snowboyDetector.SetSensitivity("0.45");
        snowboyDetector.SetAudioGain(2F);

        bufferSize = Math.round(snowboyDetector.SampleRate() * BUFFER_SIZE_SECONDS);

        recorder = new AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION, snowboyDetector.SampleRate(),
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize * 2);

        if (recorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
            recorder.release();
            throw new IOException("Failed to initialize recorder. Microphone might be already in use.");
        }
    }

// -------------------------- OTHER METHODS --------------------------

    public void addListener(RecognitionListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public boolean isListening() {
        return recognizerThread != null && recognizerThread.isAlive();
    }

    public boolean startListening() {
        if (recognizerThread != null) {
            return false;
        }

        Log.i(TAG, "Start recognition");
        recognizerThread = new RecognizerThread();
        recognizerThread.start();
        return true;
    }

    public boolean stop() {
        boolean result = stopRecognizerThread();
        if (result) {
            Log.i(TAG, "Stop recognition");
            mainHandler.post(new ResultEvent(-2));
        }
        if (recorder != null) {
            recorder.stop();
        }
        return result;
    }

    private boolean stopRecognizerThread() {
        if (null == recognizerThread)
            return false;

        try {
            recognizerThread.interrupt();
            recognizerThread.join();
        } catch (InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
        }

        recognizerThread = null;
        return true;
    }

    private class OnErrorEvent extends RecognitionEvent {
        private final Exception exception;

        OnErrorEvent(Exception exception) {
            this.exception = exception;
        }

        @Override
        protected void execute(RecognitionListener listener) {
            listener.onError(exception);
        }
    }

    private abstract class RecognitionEvent implements Runnable {
        public void run() {
            RecognitionListener[] emptyArray = new RecognitionListener[0];
            for (RecognitionListener listener : listeners.toArray(emptyArray))
                execute(listener);
        }

        protected abstract void execute(RecognitionListener listener);
    }

    private final class RecognizerThread extends Thread {
        @Override
        public void run() {
            recorder.startRecording();
            if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
                recorder.stop();
                IOException ioe = new IOException("Failed to start recording. Microphone might be already in use.");
                mainHandler.post(new OnErrorEvent(ioe));
                return;
            }

            Log.d(TAG, "Starting decoding");

            short[] buffer = new short[bufferSize];

            // Skip the first buffer, usually zeroes
            recorder.read(buffer, 0, buffer.length);
            while (!interrupted()) {
                int nread = recorder.read(buffer, 0, buffer.length);
                if (nread == -1) {
                    throw new RuntimeException("error reading audio buffer");
                } else if (nread > 0) {
                    final int index = snowboyDetector.RunDetection(buffer, nread);
                    if (index > 0) {
                        mainHandler.post(new ResultEvent(index));
                    }
                }
            }

            Log.d(TAG, "Stopping decoding");
            recorder.stop();
            snowboyDetector.Reset();

            // Remove all pending notifications.
            mainHandler.removeCallbacksAndMessages(null);
        }
    }

    private class ResultEvent extends RecognitionEvent {
        final int index;

        ResultEvent(int index) {
            this.index = index;
        }

        @Override
        protected void execute(RecognitionListener listener) {
            listener.onResult(index);
        }
    }
}

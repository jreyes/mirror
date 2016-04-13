package com.vaporwarecorp.mirror.vendor.houndify;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.hound.android.sdk.util.Beta;
import timber.log.Timber;


/**
 * <p>Utility class for creating and maintaining an instance of an {@link AudioRecord}.  The audio record comes ready to be used by
 * VoiceSearchFactory for voice searching.</p>
 * <p>Consider using a {@link} instead if you are dealing with multiple consumers of the audio data.</p>
 *
 */
@Beta
public class AudioRecordFactory {

    private static final String LOG_TAG = AudioRecordFactory.class.getSimpleName();
    private static final boolean LOG_DEBUG = false;

    private static int DESIRED_SAMPLE_RATE = 16000;
    private static final int BUFFER_SECONDS = 5;

    private static int bestAvailableSampleRate;

    private static AudioRecord instance;

    private static void initSamplingRate() {
        AudioRecord ar = null;

        try {
            ar = tryNewInstance(DESIRED_SAMPLE_RATE);
        }
        catch (final AudioRecordException e) {
            Log.i(LOG_TAG, e.getMessage());
        }

        if (ar != null) {
            bestAvailableSampleRate = ar.getSampleRate();
            ar.release();
            ar = null;

            // Hack to force Samsung phones to clean up this AudioRecord NOW.
            // Otherwise, the next AudioRecord created may not init properly
            // (there can be only one).
            //
            System.gc();
        }
        else {
            bestAvailableSampleRate = 0;
        }

        Log.i(LOG_TAG, "Best available sampling rate: " + bestAvailableSampleRate);
    }

    /**
     * Returns the best available sample rate for an AudioRecord created via
     * newInstance(), or 0 if newInstance will knowingly throw an exception.
     */

    public static synchronized int getBestAvailableSampleRate() {
        if (bestAvailableSampleRate == 0) {
            initSamplingRate();
        }

        // If this is still zero oh well we tried
        return bestAvailableSampleRate;
    }


    /**
     * Create a new AudioRecord object with the best available sample rate.
     * @return
     * @throws AudioRecordException
     *     if creation fails for some reason
     */

    public static synchronized AudioRecord getInstance() throws AudioRecordException {
        if (LOG_DEBUG) {
            Log.d(LOG_TAG, "getInstance");
        }

        if (instance == null) {
            instance = tryNewInstance(getBestAvailableSampleRate());
        }

        return instance;
    }

    /**
     * Convenience method for releasing and creating a new AudioRecord instance.  This
     * will release any previously held audio record object by this class before creating a new one.
     * @return
     * @throws AudioRecordException
     */
    public static synchronized AudioRecord createNewInstance() throws AudioRecordException {
        if (LOG_DEBUG) {
            Log.d(LOG_TAG, "createNewInstance");
        }

        release();
        return getInstance();
    }

    /**
     * Releases audio record instance held internally by this class.  This calls {@link AudioRecord#release()} thus any
     * use of the audio record else where will crash.
     */
    public static synchronized void release() {
        if (LOG_DEBUG) {
            Log.d(LOG_TAG, "release");
        }

        if (instance != null) {
            try {
                instance.release();
            }
            finally {
                instance = null;
                System.gc();
            }
        }
    }

    private static AudioRecord tryNewInstance(final int sample_rate) throws AudioRecordException {
        // Force release the old audio record instance
        release();

        if (sample_rate == 0) {
            throw new AudioRecordException("AudioRecord does not support a sample rate of 0hz");
        }

        int audio_buffer_size;
        audio_buffer_size = sample_rate /*samples/second*/ * BUFFER_SECONDS * 2 /*bytes/sample*/;

        final int min_buf_size = AudioRecord.getMinBufferSize(
                sample_rate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        if (min_buf_size == AudioRecord.ERROR_BAD_VALUE || min_buf_size == AudioRecord.ERROR) {
            throw new AudioRecordException("AudioRecord does not support sample rate " + sample_rate + ", buffer size = " + min_buf_size);
        }

        audio_buffer_size = Math.max(min_buf_size, audio_buffer_size);

        Timber.i("using custom Audio record *************************************************************");

        AudioRecord ar = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sample_rate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                audio_buffer_size
        );

        if (ar.getState() != AudioRecord.STATE_INITIALIZED) {
            throw new AudioRecordException("Can't create a new AudioRecord @ " + sample_rate + "Hz, state = "  + ar.getState());
        }

        return ar;
    }


    public static class AudioRecordException extends Exception {
        private static final long serialVersionUID = 1L;

        public AudioRecordException() {
            super();
        }

        public AudioRecordException(final String detailMessage, final Throwable throwable) {
            super(detailMessage, throwable);
        }

        public AudioRecordException(final String detailMessage) {
            super(detailMessage);
        }

        public AudioRecordException(final Throwable throwable) {
            super(throwable);
        }
    }

}
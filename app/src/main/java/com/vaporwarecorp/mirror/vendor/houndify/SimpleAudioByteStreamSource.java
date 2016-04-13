package com.vaporwarecorp.mirror.vendor.houndify;


import android.media.AudioRecord;

import java.io.IOException;
import java.io.InputStream;

public class SimpleAudioByteStreamSource extends InputStream {
// ------------------------------ FIELDS ------------------------------

    private AudioRecord audioRecord;
    private byte[] internalBuffer = new byte[1024];
    private State state = State.IDLE;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface AutoCloseable ---------------------

    @Override
    public void close() throws IOException {
        if (state == State.STARTED) {
            try {
                audioRecord.stop();
            } catch (IllegalStateException ex) {
                // Swallow it, main goal is to stop all recording.
            }
        }

        audioRecord = null;
        state = State.STOPPED;
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        if (state == State.IDLE) {
            try {
                audioRecord = AudioRecordFactory.getInstance();

                audioRecord.startRecording();
                state = State.STARTED;
            } catch (final AudioRecordFactory.AudioRecordException e) {
                return -1;
            }
        }

        final int bytesRead = audioRecord.read(internalBuffer, 0, Math.min(byteCount, internalBuffer.length));
        if (bytesRead > 0) {
            System.arraycopy(internalBuffer, 0, buffer, byteOffset, bytesRead);
            return bytesRead;
        } else {
            close();
            throw new IOException("Error reading from audio record.  Status = " + bytesRead);
        }
    }

// -------------------------- ENUMERATIONS --------------------------

    private enum State {
        IDLE, STARTED, STOPPED
    }
}

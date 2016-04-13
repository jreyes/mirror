package com.vaporwarecorp.mirror.vendor.ijk;

import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import tv.danmaku.ijk.media.player.IMediaPlayer;

public interface IRenderView {
// ------------------------------ FIELDS ------------------------------

    int AR_ASPECT_FIT_PARENT = 0; // without clip
    int AR_ASPECT_FILL_PARENT = 1; // may clip
    int AR_ASPECT_WRAP_CONTENT = 2;
    int AR_MATCH_PARENT = 3;
    int AR_16_9_FIT_PARENT = 4;
    int AR_4_3_FIT_PARENT = 5;

// -------------------------- OTHER METHODS --------------------------

    void addRenderCallback(@NonNull IRenderCallback callback);

    View getView();

    void removeRenderCallback(@NonNull IRenderCallback callback);

    void setAspectRatio(int aspectRatio);

    void setVideoRotation(int degree);

    void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen);

    void setVideoSize(int videoWidth, int videoHeight);

    boolean shouldWaitForResize();

// -------------------------- INNER CLASSES --------------------------

    interface ISurfaceHolder {
        void bindToMediaPlayer(IMediaPlayer mp);

        @NonNull
        IRenderView getRenderView();

        @Nullable
        SurfaceHolder getSurfaceHolder();

        @Nullable
        Surface openSurface();

        @Nullable
        SurfaceTexture getSurfaceTexture();
    }

    interface IRenderCallback {
        /**
         * @param holder
         * @param width  could be 0
         * @param height could be 0
         */
        void onSurfaceCreated(@NonNull ISurfaceHolder holder, int width, int height);

        /**
         * @param holder
         * @param format could be 0
         * @param width
         * @param height
         */
        void onSurfaceChanged(@NonNull ISurfaceHolder holder, int format, int width, int height);

        void onSurfaceDestroyed(@NonNull ISurfaceHolder holder);
    }
}
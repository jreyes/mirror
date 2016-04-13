package com.vaporwarecorp.mirror.vendor.ijk;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import timber.log.Timber;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.ISurfaceTextureHolder;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SurfaceRenderView extends SurfaceView implements IRenderView {
// ------------------------------ FIELDS ------------------------------

    private MeasureHelper mMeasureHelper;
    private SurfaceCallback mSurfaceCallback;

// --------------------------- CONSTRUCTORS ---------------------------

    public SurfaceRenderView(Context context) {
        super(context);
        initView();
    }

    public SurfaceRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SurfaceRenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface IRenderView ---------------------

    @Override
    public void addRenderCallback(@NonNull IRenderCallback callback) {
        mSurfaceCallback.addRenderCallback(callback);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void removeRenderCallback(@NonNull IRenderCallback callback) {
        mSurfaceCallback.removeRenderCallback(callback);
    }

    @Override
    public void setAspectRatio(int aspectRatio) {
        mMeasureHelper.setAspectRatio(aspectRatio);
        requestLayout();
    }

    @Override
    public void setVideoRotation(int degree) {
        Timber.e("SurfaceView doesn't support rotation (" + degree + ")!\n");
    }

    @Override
    public void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen) {
        if (videoSarNum > 0 && videoSarDen > 0) {
            mMeasureHelper.setVideoSampleAspectRatio(videoSarNum, videoSarDen);
            requestLayout();
        }
    }

    @Override
    public void setVideoSize(int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            mMeasureHelper.setVideoSize(videoWidth, videoHeight);
            getHolder().setFixedSize(videoWidth, videoHeight);
            requestLayout();
        }
    }

    @Override
    public boolean shouldWaitForResize() {
        return true;
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(SurfaceRenderView.class.getName());
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            info.setClassName(SurfaceRenderView.class.getName());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMeasureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mMeasureHelper.getMeasuredWidth(), mMeasureHelper.getMeasuredHeight());
    }

    private void initView() {
        mMeasureHelper = new MeasureHelper(this);
        mSurfaceCallback = new SurfaceCallback(this);
        getHolder().addCallback(mSurfaceCallback);
        //noinspection deprecation
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    }

// -------------------------- INNER CLASSES --------------------------

    //--------------------
    // SurfaceViewHolder
    //--------------------
    private static final class InternalSurfaceHolder implements IRenderView.ISurfaceHolder {
        private SurfaceRenderView mSurfaceView;
        private SurfaceHolder mSurfaceHolder;

        InternalSurfaceHolder(@NonNull SurfaceRenderView surfaceView,
                              @Nullable SurfaceHolder surfaceHolder) {
            mSurfaceView = surfaceView;
            mSurfaceHolder = surfaceHolder;
        }

        public void bindToMediaPlayer(IMediaPlayer mp) {
            if (mp != null) {
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) &&
                        (mp instanceof ISurfaceTextureHolder)) {
                    ISurfaceTextureHolder textureHolder = (ISurfaceTextureHolder) mp;
                    textureHolder.setSurfaceTexture(null);
                }
                mp.setDisplay(mSurfaceHolder);
            }
        }

        @NonNull
        @Override
        public IRenderView getRenderView() {
            return mSurfaceView;
        }

        @Nullable
        @Override
        public SurfaceHolder getSurfaceHolder() {
            return mSurfaceHolder;
        }

        @Nullable
        @Override
        public SurfaceTexture getSurfaceTexture() {
            return null;
        }

        @Nullable
        @Override
        public Surface openSurface() {
            if (mSurfaceHolder == null)
                return null;
            return mSurfaceHolder.getSurface();
        }
    }

    private static final class SurfaceCallback implements SurfaceHolder.Callback {
        private SurfaceHolder mSurfaceHolder;
        private boolean mIsFormatChanged;
        private int mFormat;
        private int mWidth;
        private int mHeight;
        private WeakReference<SurfaceRenderView> mWeakSurfaceView;
        private Map<IRenderCallback, Object> mRenderCallbackMap = new ConcurrentHashMap<>();

        SurfaceCallback(@NonNull SurfaceRenderView surfaceView) {
            mWeakSurfaceView = new WeakReference<>(surfaceView);
        }

        void addRenderCallback(@NonNull IRenderCallback callback) {
            mRenderCallbackMap.put(callback, callback);

            ISurfaceHolder surfaceHolder = null;
            if (mSurfaceHolder != null) {
                surfaceHolder = new InternalSurfaceHolder(mWeakSurfaceView.get(), mSurfaceHolder);
                callback.onSurfaceCreated(surfaceHolder, mWidth, mHeight);
            }
            if (mIsFormatChanged) {
                if (surfaceHolder == null)
                    surfaceHolder = new InternalSurfaceHolder(mWeakSurfaceView.get(), mSurfaceHolder);
                callback.onSurfaceChanged(surfaceHolder, mFormat, mWidth, mHeight);
            }
        }

        void removeRenderCallback(@NonNull IRenderCallback callback) {
            mRenderCallbackMap.remove(callback);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            mIsFormatChanged = false;
            mFormat = 0;
            mWidth = 0;
            mHeight = 0;

            ISurfaceHolder surfaceHolder = new InternalSurfaceHolder(mWeakSurfaceView.get(), mSurfaceHolder);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceCreated(surfaceHolder, 0, 0);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mSurfaceHolder = null;
            mIsFormatChanged = false;
            mFormat = 0;
            mWidth = 0;
            mHeight = 0;

            ISurfaceHolder surfaceHolder = new InternalSurfaceHolder(mWeakSurfaceView.get(), mSurfaceHolder);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceDestroyed(surfaceHolder);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format,
                                   int width, int height) {
            mSurfaceHolder = holder;
            mIsFormatChanged = true;
            mFormat = format;
            mWidth = width;
            mHeight = height;

            // mMeasureHelper.setVideoSize(width, height);

            ISurfaceHolder surfaceHolder = new InternalSurfaceHolder(mWeakSurfaceView.get(), mSurfaceHolder);
            for (IRenderView.IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceChanged(surfaceHolder, format, width, height);
            }
        }
    }
}
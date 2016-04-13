package com.vaporwarecorp.mirror.manager;

import android.content.Context;
import com.vaporwarecorp.mirror.event.HandWaveClickEvent;
import com.vaporwarecorp.mirror.event.HandWaveLeftEvent;
import com.vaporwarecorp.mirror.event.HandWaveRightEvent;
import edu.washington.cs.touchfreelibrary.sensors.CameraGestureSensor;
import edu.washington.cs.touchfreelibrary.sensors.ClickSensor;
import org.greenrobot.eventbus.EventBus;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import timber.log.Timber;

public class HandWaveManager implements CameraGestureSensor.Listener, ClickSensor.Listener {
// ------------------------------ FIELDS ------------------------------

    private Context mContext;
    private CameraGestureSensor mGestureSensor;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(mContext) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Timber.i("OpenCV loaded successfully");
                    CameraGestureSensor.loadLibrary();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

// --------------------------- CONSTRUCTORS ---------------------------

    public HandWaveManager(Context context) {
        mContext = context;
        mGestureSensor = new CameraGestureSensor(mContext);
        mGestureSensor.addGestureListener(HandWaveManager.this);
        mGestureSensor.addClickListener(HandWaveManager.this);
        initOpenCV();
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Listener ---------------------

    @Override
    public void onSensorClick(ClickSensor clickSensor) {
        Timber.d("onSensorClick");
        EventBus.getDefault().post(new HandWaveClickEvent());
    }

    @Override
    public void onGestureUp(CameraGestureSensor cameraGestureSensor, long l) {
        Timber.d("onGestureUp");
    }

    @Override
    public void onGestureDown(CameraGestureSensor cameraGestureSensor, long l) {
        Timber.d("onGestureDown");
    }

    @Override
    public void onGestureLeft(CameraGestureSensor cameraGestureSensor, long l) {
        Timber.d("onGestureLeft");
        EventBus.getDefault().post(new HandWaveLeftEvent());
    }

    @Override
    public void onGestureRight(CameraGestureSensor cameraGestureSensor, long l) {
        Timber.d("onGestureRight");
        EventBus.getDefault().post(new HandWaveRightEvent());
    }

// -------------------------- OTHER METHODS --------------------------

    public void onDestroy() {
        mGestureSensor.stop();
        mGestureSensor.removeClickListener(this);
        mGestureSensor.removeGestureListener(this);
        mGestureSensor = null;
    }

    public void startDetecting(JavaCameraView view) {
        mGestureSensor.start(view);
    }

    public void stopDetecting() {
        if (mGestureSensor.isRunning()) {
            mGestureSensor.stop();
        }
    }

    private void initOpenCV() {
        if (!OpenCVLoader.initDebug()) {
            Timber.d("Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, mContext, mLoaderCallback);
        } else {
            Timber.d("OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}

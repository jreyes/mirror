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
package com.vaporwarecorp.mirror.component.opencv;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import org.opencv.android.*;
import org.opencv.core.Mat;
import timber.log.Timber;

public class GestureView extends FrameLayout implements CameraBridgeViewBase.CvCameraViewListener {
// ------------------------------ FIELDS ------------------------------

    private JavaCameraView mCameraView;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getContext()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Timber.i("OpenCV loaded successfully");
                    mCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

// --------------------------- CONSTRUCTORS ---------------------------

    public GestureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeLayout();
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface CvCameraViewListener ---------------------

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        return null;
    }

// -------------------------- OTHER METHODS --------------------------

    public void onDestroy() {
        if (mCameraView != null) {
            mCameraView.disableView();
            mCameraView = null;
        }
    }

    public void onPause() {
        if (mCameraView != null) {
            mCameraView.disableView();
        }
    }

    public void onResume() {
        if (!OpenCVLoader.initDebug()) {
            Timber.d("Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, getContext(), mLoaderCallback);
        } else {
            Timber.d("OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private void initializeLayout() {
        mCameraView = new JavaCameraView(getContext(), 0);
        mCameraView.setVisibility(SurfaceView.VISIBLE);
        mCameraView.setCvCameraViewListener(this);
        addView(mCameraView);
    }
}

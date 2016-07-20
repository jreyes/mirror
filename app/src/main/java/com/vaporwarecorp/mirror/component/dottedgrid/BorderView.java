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
package com.vaporwarecorp.mirror.component.dottedgrid;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import com.nineoldandroids.view.ViewHelper;

import static com.vaporwarecorp.mirror.util.DisplayMetricsUtil.convertDpToPixel;

class BorderView extends FrameLayout {
// ------------------------------ FIELDS ------------------------------

    @ColorInt
    private int mColorRed;
    @ColorInt
    private int mColorTransparent;
    @ColorInt
    private int mColorWhite;
    private boolean mLeftAligned;
    private int mLeftRightMargin;
    private boolean mMaximized;
    private int mMaximizedHeight;
    private int mMaximizedWidth;
    private int mMinimizedHeight;
    private boolean mRightAligned;
    private float mScaleFactor;
    private int mTopBottomMargin;

// -------------------------- STATIC METHODS --------------------------

    static BorderView newInstance(Context context, int maximizedWidth, float scaleFactor) {
        BorderView borderView = new BorderView(context);
        borderView.initializeLayout(context, maximizedWidth, scaleFactor);
        return borderView;
    }

// --------------------------- CONSTRUCTORS ---------------------------

    private BorderView(Context context) {
        super(context, null, 0);
    }

// -------------------------- OTHER METHODS --------------------------

    public int getLeftRightMargin() {
        return mLeftRightMargin;
    }

    public int getMinimizedHeight() {
        return mMinimizedHeight;
    }

    public int getTopBottomMargin() {
        return mTopBottomMargin;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mMaximizedHeight != getHeight()) {
            mMaximizedHeight = getHeight();
            mMinimizedHeight = Math.round(mMaximizedHeight * mScaleFactor);
            mTopBottomMargin = Math.round((mMaximizedHeight - mMinimizedHeight) / 2);
            mLeftRightMargin = Math.round((mMaximizedWidth - (mMaximizedWidth * mScaleFactor)) / 2);
        }
    }

    void hideBorder() {
        setBackgroundColor(mColorTransparent);
    }

    boolean isLeftAligned() {
        return mLeftAligned;
    }

    boolean isMaximized() {
        return mMaximized;
    }

    boolean isRightAligned() {
        return mRightAligned;
    }

    void maximize() {
        mMaximized = true;

        mLeftAligned = false;
        mRightAligned = false;

        // set LayoutParams
        LayoutParams params = new LayoutParams(mMaximizedWidth, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        setLayoutParams(params);

        // set padding
        final int borderPadding = Math.round(convertDpToPixel(4, getContext()));
        setPadding(borderPadding, borderPadding, borderPadding, borderPadding);

        // scale
        ViewHelper.setScaleX(this, 1f);
        ViewHelper.setScaleY(this, 1f);
    }

    void minimize(boolean leftAligned, int left, int top) {
        mLeftAligned = leftAligned;
        mRightAligned = !leftAligned;

        // scale
        scale(left, top);
    }

    void scale(int left, int top) {
        mMaximized = false;

        // set LayoutParams
        LayoutParams params = new LayoutParams(getWidth(), getHeight());
        params.leftMargin = left;
        params.topMargin = top;
        setLayoutParams(params);

        // set padding
        final int borderPadding = Math.round(convertDpToPixel(8, getContext()));
        setPadding(borderPadding, borderPadding, borderPadding, borderPadding);

        // scale
        ViewHelper.setScaleX(this, mScaleFactor);
        ViewHelper.setScaleY(this, mScaleFactor);
    }

    void showBorder() {
        setBackgroundColor(mColorWhite);
    }

    void showBorderWarning() {
        setBackgroundColor(mColorRed);
    }

    private void initializeLayout(Context context, int maximizedWidth, float scaleFactor) {
        mMaximizedWidth = maximizedWidth;
        mScaleFactor = scaleFactor;

        mColorTransparent = ContextCompat.getColor(context, android.R.color.transparent);
        mColorWhite = ContextCompat.getColor(context, android.R.color.white);
        mColorRed = ContextCompat.getColor(context, android.R.color.holo_red_dark);

        // set ID
        setId(View.generateViewId());

        // hide the border
        hideBorder();

        // Maximize by default
        maximize();
    }
}

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
import android.support.annotation.Nullable;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.feature.common.view.MirrorView;

import java.util.HashMap;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.vaporwarecorp.mirror.util.DisplayMetricsUtil.convertDpToPixel;
import static solid.stream.Stream.stream;

public class DottedGridView extends PercentRelativeLayout {
// ------------------------------ FIELDS ------------------------------

    private static final int INVALID_POINTER = -1;
    private static final float SENSITIVITY = 1f;
    private static final float X_MIN_VELOCITY = 1500;
    private static final float Y_MIN_VELOCITY = 1000;

    ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            final int dropWidth = Math.round(releasedChild.getMeasuredWidth() / 2);
            final int dropHeight = Math.round(releasedChild.getMeasuredHeight() / 2);
            final int left = releasedChild.getLeft();
            final int right = getMeasuredWidth() - releasedChild.getLeft() - releasedChild.getMeasuredWidth();
            final int top = releasedChild.getTop();
            final int bottom = getMeasuredHeight() - releasedChild.getTop() - releasedChild.getMeasuredHeight();

            final int newLeft;
            final int newTop;
            if (right < dropWidth) {
                newLeft = getMeasuredWidth() - releasedChild.getMeasuredWidth();
                newTop = getNewTop(releasedChild, dropHeight, top, bottom, false);
                notifyUpdateViewOnRight((BorderView) releasedChild);
            } else if (left < dropWidth) {
                newLeft = 0;
                newTop = getNewTop(releasedChild, dropHeight, top, bottom, false);
                notifyUpdateViewOnLeft((BorderView) releasedChild);
            } else {
                newLeft = Math.round((getMeasuredWidth() - releasedChild.getMeasuredWidth()) / 2);
                newTop = getNewTop(releasedChild, dropHeight, top, bottom, true);
                notifyUpdateViewOnCenter((BorderView) releasedChild);
            }

            LayoutParams params = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            params.leftMargin = newLeft;
            params.topMargin = newTop;
            params.getPercentLayoutInfo().widthPercent = 0.3f;
            releasedChild.setLayoutParams(params);

            ViewCompat.postInvalidateOnAnimation(DottedGridView.this);
        }

        /**
         * Override method used to configure the horizontal drag. Restrict the motion of the dragged
         * child view along the horizontal axis.
         *
         * @param child child view being dragged.
         * @param left  attempted motion along the X axis.
         * @param dx    proposed change in position for left.
         * @return the new clamped position for left.
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            final int leftBound = getPaddingLeft() + mBorderPadding;
            final int rightBound = getWidth() - mDraggedView.getWidth() - mBorderPadding;
            return Math.min(Math.max(left, leftBound), rightBound);
        }

        /**
         * Override method used to configure the vertical drag. Restrict the motion of the dragged child
         * view along the vertical axis.
         *
         * @param child child view being dragged.
         * @param top   attempted motion along the Y axis.
         * @param dy    proposed change in position for top.
         * @return the new clamped position for top.
         */
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            final int topBound = getPaddingTop() + mBorderPadding;
            final int bottomBound = getHeight() - mDraggedView.getHeight() - mBorderPadding;
            return Math.min(Math.max(top, topBound), bottomBound);
        }

        /**
         * Override method used to configure which is going to be the dragged view.
         *
         * @param view      child the user is attempting to capture.
         * @param pointerId ID of the pointer attempting the capture,
         * @return true if capture should be allowed, false otherwise.
         */
        @Override
        public boolean tryCaptureView(View view, int pointerId) {
            return view instanceof BorderView;
        }
    };

    private int activePointerId = INVALID_POINTER;
    private FrameLayout mBackground;
    private int mBorderPadding;
    private BorderView mDraggedView;
    private Listener mListener;
    private ViewDragHelper mViewDragHelper;
    private HashMap<Integer, BorderView> mViews;

// --------------------------- CONSTRUCTORS ---------------------------

    public DottedGridView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeLayout(context);
        initializeViewDragHelper();
    }

// -------------------------- OTHER METHODS --------------------------

    public int addBorderView(final Context context) {
        final int viewId = View.generateViewId();

        final LayoutParams params = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.addRule(PercentRelativeLayout.CENTER_IN_PARENT);
        params.getPercentLayoutInfo().widthPercent = 0.3f;

        BorderView view = new BorderView(context);
        view.setId(viewId);
        view.setLayoutParams(params);
        mViews.put(viewId, view);
        addView(view);

        return viewId;
    }

    /**
     * To ensure the animation is going to work this method has been override to call
     * postInvalidateOnAnimation if the view is not settled yet.
     */
    @Override
    public void computeScroll() {
        if (!isInEditMode() && mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public int getDraggedViewHeightPlusMarginTop() {
        return mDraggedView.getHeight();
    }

    /**
     * Checks if the top view is closed at the right or left place.
     *
     * @return true if the view is closed.
     */
    public boolean isClosed() {
        return isClosedAtLeft() || isClosedAtRight();
    }

    /**
     * Checks if the top view is closed at the left place.
     *
     * @return true if the view is closed at left.
     */
    public boolean isClosedAtLeft() {
        return mDraggedView.getRight() <= 0;
    }

    /**
     * Checks if the top view closed at the right place.
     *
     * @return true if the view is closed at right.
     */
    public boolean isClosedAtRight() {
        return mDraggedView.getLeft() >= getWidth();
    }

    /**
     * Override method to intercept only touch events over the drag view and to cancel the drag when
     * the action associated to the MotionEvent is equals to ACTION_CANCEL or ACTION_UP.
     *
     * @param ev captured.
     * @return true if the view is going to process the touch event or false if not.
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }

        mDraggedView = null;
        stream(mViews.values())
                .filter(v -> isViewHit(v, (int) ev.getX(), (int) ev.getY()))
                .forEach((BorderView v) -> mDraggedView = v);

        switch (MotionEventCompat.getActionMasked(ev) & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mViewDragHelper.cancel();
                return false;
            case MotionEvent.ACTION_DOWN:
                int index = MotionEventCompat.getActionIndex(ev);
                activePointerId = MotionEventCompat.getPointerId(ev, index);
                if (activePointerId == INVALID_POINTER) {
                    return false;
                }
                break;
            default:
                break;
        }
        boolean interceptTap = mDraggedView == null ||
                mViewDragHelper.isViewUnder(mDraggedView, (int) ev.getX(), (int) ev.getY());
        return mViewDragHelper.shouldInterceptTouchEvent(ev) || interceptTap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int actionMasked = MotionEventCompat.getActionMasked(event);
        switch (actionMasked & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_CANCEL:
                return false;
            case MotionEvent.ACTION_UP:
                hideDraggedViewBorder();
                mBackground.setVisibility(INVISIBLE);
                break;
            case MotionEvent.ACTION_DOWN:
                showDraggedViewBorder();
                activePointerId = MotionEventCompat.getPointerId(event, actionMasked);
                mBackground.setVisibility(VISIBLE);
                break;
        }
        if (activePointerId == INVALID_POINTER) {
            return false;
        }
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    public void removeBorderView(final int viewId) {
        View view = mViews.get(viewId);
        if (view != null) {
            mViews.remove(viewId);
            removeView(view);
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private int getNewTop(View releasedChild, int dropHeight, int top, int bottom, boolean middleOnly) {
        if (!middleOnly && top < dropHeight) {
            return 0;
        } else if (!middleOnly && bottom < dropHeight) {
            return getMeasuredHeight() - releasedChild.getMeasuredHeight();
        } else {
            return Math.round((getMeasuredHeight() - releasedChild.getMeasuredHeight()) / 2);
        }
    }

    private void hideDraggedViewBorder() {
        if (mDraggedView != null) {
            mDraggedView.hideBorder();
        }
    }

    private void initializeLayout(Context context) {
        mBorderPadding = Math.round(convertDpToPixel(2, context));
        mViews = new HashMap<>();

        FrameLayout gridLayout = new FrameLayout(context);
        gridLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_dotted_grid));
        gridLayout.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));

        mBackground = new FrameLayout(context);
        mBackground.addView(gridLayout);
        mBackground.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_solid_border));
        mBackground.setBottom(0);
        mBackground.setLeft(0);
        mBackground.setRight(0);
        mBackground.setTop(0);
        mBackground.setVisibility(INVISIBLE);
        addView(mBackground);
    }

    /**
     * Initialize the viewDragHelper.
     */
    private void initializeViewDragHelper() {
        mViewDragHelper = ViewDragHelper.create(this, SENSITIVITY, mCallback);
    }

    /**
     * Calculate if one position is above any view.
     *
     * @param view to analyze.
     * @param x    position.
     * @param y    position.
     * @return true if x and y positions are below the view.
     */
    private boolean isViewHit(View view, int x, int y) {
        int[] viewLocation = new int[2];
        view.getLocationOnScreen(viewLocation);
        int[] parentLocation = new int[2];
        this.getLocationOnScreen(parentLocation);
        int screenX = parentLocation[0] + x;
        int screenY = parentLocation[1] + y;
        return screenX >= viewLocation[0]
                && screenX < viewLocation[0] + view.getWidth()
                && screenY >= viewLocation[1]
                && screenY < viewLocation[1] + view.getHeight();
    }

    /**
     * Notify te view is closed to the right to the Listener
     */
    private void notifyCloseToRightListener(MirrorView mirrorView) {
        if (mListener != null) {
            //mListener.onClosedToRight();
        }
    }

    private void notifyUpdateViewOnCenter(BorderView borderView) {
        if (mListener != null) {
            mListener.onViewOnCenter(borderView.getId());
        }
    }

    private void notifyUpdateViewOnLeft(BorderView borderView) {
        if (mListener != null) {
            mListener.onViewOnLeft(borderView.getId());
        }
    }

    private void notifyUpdateViewOnRight(BorderView borderView) {
        if (mListener != null) {
            mListener.onViewOnRight(borderView.getId());
        }
    }

    private void showDraggedViewBorder() {
        if (mDraggedView != null) {
            mDraggedView.bringToFront();
            mDraggedView.showBorder();
        }
    }

// -------------------------- INNER CLASSES --------------------------

    public interface Listener {
        /**
         * Called when the view is closed to the right.
         */
        void onClosedToRight(MirrorView mirrorView);

        /**
         * Called when the view is set in the center
         */
        void onViewOnCenter(int containerId);

        /**
         * Called when the view is set on the left
         */
        void onViewOnLeft(int containerId);

        /**
         * Called when the view is set on the right
         */
        void onViewOnRight(int containerId);
    }
}

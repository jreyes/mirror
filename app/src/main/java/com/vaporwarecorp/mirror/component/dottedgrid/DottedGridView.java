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
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.vaporwarecorp.mirror.R;

import java.util.HashMap;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static solid.stream.Stream.stream;

public class DottedGridView extends FrameLayout {
// ------------------------------ FIELDS ------------------------------

    private static final int INVALID_POINTER = -1;
    private static final int MAX_OVERFLOW = 150;
    private static final int MIN_OVERFLOW = -50;
    private static final float SENSITIVITY = 1f;

    @SuppressWarnings("WeakerAccess")
    ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            BorderView borderView = (BorderView) changedView;

            final int right = getMeasuredWidth() - left - borderView.getMeasuredWidth();
            if (left < MIN_OVERFLOW || right < MIN_OVERFLOW) {
                borderView.showBorderWarning();
            } else {
                borderView.showBorder();
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            BorderView borderView = (BorderView) releasedChild;

            final int left = borderView.getLeft();
            final int right = getMeasuredWidth() - borderView.getLeft() - borderView.getMeasuredWidth();
            if (right < mColumnSizeSide) {
                if (right < MIN_OVERFLOW) {
                    notifyUpdateCloseOnRight(borderView);
                } else {
                    rearrangeRightContainer(borderView);
                }
            } else if (left < mColumnSizeSide) {
                if (left < MIN_OVERFLOW) {
                    notifyUpdateCloseOnLeft(borderView);
                } else {
                    rearrangeLeftContainer(borderView);
                }
            } else {
                rearrangeCenterContainer(borderView);
            }
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
            final int leftBound = -MAX_OVERFLOW;
            final int rightBound = getWidth() + MAX_OVERFLOW;
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
            final int topBound = getPaddingTop();
            final int bottomBound = getHeight() - mDraggedView.getHeight();
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
    private int mColumnSizeCenter;
    private int mColumnSizeSide;
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
        // TODO find better place for this
        // column in center is 40% while side columns is 15% of layout
        // so distribution would be 17/17/32/17/17
        mColumnSizeCenter = Math.round((getMeasuredWidth() * 32) / 100);
        mColumnSizeSide = Math.round((getMeasuredWidth() * 17) / 100);

        final int viewId = View.generateViewId();

        final LayoutParams params = new LayoutParams(mColumnSizeCenter, WRAP_CONTENT);
        params.gravity = Gravity.CENTER;

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
        BorderView borderView = mViews.get(viewId);
        if (borderView != null) {
            // remove the view
            mViews.remove(viewId);
            removeView(borderView);

            // now rearrange the layout that this view was removed from
            if (borderView.isLeftAligned()) {
                rearrangeLeftContainer();
            }
            if (borderView.isRightAligned()) {
                rearrangeRightContainer();
            }
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private void hideDraggedViewBorder() {
        if (mDraggedView != null) {
            mDraggedView.hideBorder();
        }
    }

    private void initializeLayout(Context context) {
        mViews = new HashMap<>();

        FrameLayout gridLayout = new FrameLayout(context);
        gridLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_dotted_grid));
        gridLayout.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));

        mBackground = new FrameLayout(context);
        mBackground.addView(gridLayout);
        mBackground.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        mBackground.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_solid_border));
        mBackground.setVisibility(INVISIBLE);
        addView(mBackground);

        // now set the child clipping to false
        setClipChildren(false);
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

    private void notifyUpdateCloseOnLeft(BorderView borderView) {
        if (mListener != null) {
            mListener.onClosedToLeft(borderView.getId());
        }
    }

    private void notifyUpdateCloseOnRight(BorderView borderView) {
        if (mListener != null) {
            mListener.onClosedToRight(borderView.getId());
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

    private void rearrangeCenterContainer(BorderView borderView) {
        LayoutParams params = new LayoutParams(mColumnSizeCenter, WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        borderView.setLayoutParams(params);

        borderView.setCenterAligned();
        notifyUpdateViewOnCenter(borderView);

        rearrangeLeftContainer();
        rearrangeRightContainer();
    }

    private void rearrangeLeftContainer() {
        int newLeft = 0;
        int newTop = 0;

        for (BorderView borderView : mViews.values()) {
            if (borderView.isLeftAligned()) {
                LayoutParams params = (LayoutParams) borderView.getLayoutParams();
                if (params.leftMargin != newLeft || params.topMargin != newTop) {
                    params.leftMargin = newLeft;
                    params.topMargin = newTop;
                    if (mViewDragHelper.smoothSlideViewTo(borderView, newLeft, newTop)) {
                        ViewCompat.postInvalidateOnAnimation(this);
                    }
                }
                newTop += borderView.getMeasuredHeight();
            }
        }
    }

    private void rearrangeLeftContainer(BorderView borderView) {
        // if it is already right aligned, don't realigned it
        if (borderView.isLeftAligned()) {
            LayoutParams params = (LayoutParams) borderView.getLayoutParams();
            if (mViewDragHelper.smoothSlideViewTo(borderView, params.leftMargin, params.topMargin)) {
                ViewCompat.postInvalidateOnAnimation(this);
                return;
            }
        }

        // now let's calculate the new height of the BorderView object
        final float scaleFactor = (float) borderView.getMeasuredWidth() / mColumnSizeSide;
        final int newBorderViewHeight = Math.round(borderView.getMeasuredHeight() / scaleFactor);

        // now let's get the new left and top margin
        // Starting for the first column top position
        int newLeft = 0;
        int newTop = 0;

        // let's iterate the views
        for (BorderView view : mViews.values()) {
            if (borderView.getId() != view.getId() && view.isLeftAligned()) {
                newTop += view.getMeasuredHeight();
            }
            // if the newTop value plus the height of the current BorderView is
            // greater than the height of the container then go to the second column
            if (newLeft == 0 && newTop + newBorderViewHeight > getMeasuredHeight()) {
                newTop = 0;
                newLeft = mColumnSizeSide;
            }
        }

        // and let's set the LayoutParams
        LayoutParams params = new LayoutParams(mColumnSizeSide, newBorderViewHeight);
        params.leftMargin = newLeft;
        params.topMargin = newTop;
        borderView.setLayoutParams(params);

        // set BorderView to right aligned
        borderView.setLeftAligned();
        notifyUpdateViewOnLeft(borderView);

        // and rearrange the right side in case this view came from it
        rearrangeRightContainer();
    }

    private void rearrangeRightContainer() {
        int newLeft = getMeasuredWidth() - mColumnSizeSide;
        int newTop = 0;

        for (BorderView borderView : mViews.values()) {
            if (borderView.isRightAligned()) {
                LayoutParams params = (LayoutParams) borderView.getLayoutParams();
                if (params.leftMargin != newLeft || params.topMargin != newTop) {
                    params.leftMargin = newLeft;
                    params.topMargin = newTop;
                    if (mViewDragHelper.smoothSlideViewTo(borderView, newLeft, newTop)) {
                        ViewCompat.postInvalidateOnAnimation(this);
                    }
                }
                newTop += borderView.getMeasuredHeight();
            }
        }
    }

    private void rearrangeRightContainer(BorderView borderView) {
        // if it is already right aligned, don't realigned it
        if (borderView.isRightAligned()) {
            LayoutParams params = (LayoutParams) borderView.getLayoutParams();
            if (mViewDragHelper.smoothSlideViewTo(borderView, params.leftMargin, params.topMargin)) {
                ViewCompat.postInvalidateOnAnimation(this);
                return;
            }
        }

        // now let's get the new left and top margin
        // Starting for the first column top position
        int newLeft = getMeasuredWidth() - mColumnSizeSide;
        int newTop = 0;

        // let's iterate the views
        for (BorderView view : mViews.values()) {
            if (borderView.getId() != view.getId() && view.isRightAligned()) {
                newTop += view.getMeasuredHeight();
            }
        }

        // now let's calculate the new height of the BorderView object
        final float scaleFactor = (float) borderView.getMeasuredWidth() / mColumnSizeSide;
        final int newBorderViewHeight = Math.round(borderView.getMeasuredHeight() / scaleFactor);

        // and let's set the LayoutParams
        LayoutParams params = new LayoutParams(mColumnSizeSide, newBorderViewHeight);
        params.leftMargin = newLeft;
        params.topMargin = newTop;
        borderView.setLayoutParams(params);

        // set BorderView to right aligned
        borderView.setRightAligned();
        notifyUpdateViewOnRight(borderView);

        // and rearrange the left side in case this view came from it
        rearrangeLeftContainer();
    }

    private void showDraggedViewBorder() {
        if (mDraggedView != null) {
            mDraggedView.bringToFront();
            mDraggedView.showBorder();
        }
    }

// -------------------------- INNER CLASSES --------------------------

    @SuppressWarnings("unused")
    public interface Listener {
        /**
         * Called when the view is closed to the right.
         */
        void onClosedToRight(int containerId);

        /**
         * Called when the view is closed to the left.
         */
        void onClosedToLeft(int containerId);

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

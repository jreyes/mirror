/*
 * Copyright (C) 2014 Pedro Vicente Gómez Sánchez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vaporwarecorp.mirror.component.dottedgrid;

import android.support.v4.widget.ViewDragHelper;
import android.view.View;

/**
 * ViewDragHelper.Callback implementation used to work with DraggableView to perform the scale
 * effect and other animation when the view is released.
 *
 * @author Pedro Vicente Gómez Sánchez.
 */
class DraggableViewCallback extends ViewDragHelper.Callback {
// ------------------------------ FIELDS ------------------------------

    private static final int MINIMUM_DX_FOR_HORIZONTAL_DRAG = 5;
    private static final int MINIMUM_DY_FOR_VERTICAL_DRAG = 15;
    private static final float X_MIN_VELOCITY = 1500;
    private static final float Y_MIN_VELOCITY = 1000;

    private DottedGridView draggableView;
    private View draggedView;

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Main constructor.
     *
     * @param draggableView instance used to apply some animations or visual effects.
     */
    public DraggableViewCallback(DottedGridView draggableView, View draggedView) {
        this.draggableView = draggableView;
        this.draggedView = draggedView;
    }

// -------------------------- OTHER METHODS --------------------------

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
        int newLeft = draggedView.getLeft();
        if (Math.abs(dx) > MINIMUM_DX_FOR_HORIZONTAL_DRAG) {
            newLeft = left;
        }
        return newLeft;
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
        int newTop = draggableView.getHeight() - draggableView.getDraggedViewHeightPlusMarginTop();
        if (Math.abs(dy) >= MINIMUM_DY_FOR_VERTICAL_DRAG) {
            final int topBound = draggableView.getPaddingTop();
            final int bottomBound = draggableView.getHeight()
                    - draggableView.getDraggedViewHeightPlusMarginTop()
                    - draggedView.getPaddingBottom();

            newTop = Math.min(Math.max(top, topBound), bottomBound);
        }
        return newTop;
    }

    /**
     * Override method used to apply different scale and alpha effects while the view is being
     * dragged.
     *
     * @param left position.
     * @param top  position.
     * @param dx   change in X position from the last call.
     * @param dy   change in Y position from the last call.
     */
    @Override
    public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
        draggableView.changeDragViewPosition();
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
        return view.equals(draggedView);
    }
}
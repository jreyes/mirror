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

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.*;

import static java.lang.Math.*;
import static org.opencv.imgproc.Imgproc.*;

public class Gesture {
// ------------------------------ FIELDS ------------------------------

    private static final float HAND_DELAY_AFTER_SLIDE = 0.5F;
    private static final int HAND_MAX_HEIGHT = 350;
    private static final float HAND_MAX_TIME_HANDLE_FOR_SLIDE = 0.5F;
    private static final int HAND_MAX_WIDTH = 320;
    private static final float HAND_MAX_X_DISTANCE_BETWEEN_DEFECT_FOR_PALM_IN_HAND_RATIO = 0.08F;
    private static final float HAND_MAX_Y_DISTANCE_BETWEEN_DEFECT_FOR_PALM_IN_HAND_RATIO = 0.2F;
    private static final int HAND_MIN_HEIGHT = 80;
    private static final int HAND_MIN_MOVE_HAND_FOR_SLIDE = 150;
    private static final int HAND_MIN_WIDTH = 80;
    private static final float HAND_THUMBS_DETECT_MIN_HEIGHT_RATIO = 0.12F;
    public int centerX;
    public int centerY;

    public Properties properties;
    private MatOfInt4 mDefects;
    private List<PositionAndTime> mDownPositionsFromTime;
    private boolean mFoundHand;
    private MatOfPoint mHandContour;
    private List<PositionAndTime> mLeftPositionsFromTime;
    private boolean mPalm;
    private List<Point[]> mPalmDefects;
    private int mRadius;
    private int mRecH;
    private int mRecW;
    private int mRecX;
    private int mRecY;
    private List<PositionAndTime> mRightPositionsFromTime;
    private boolean mSlideDown;
    private boolean mSlideLeft;
    private boolean mSlideRight;
    private boolean mSlideUp;
    private boolean mThumbsDown;
    private boolean mThumbsUp;
    private long mTimeLastHandSlide;
    private List<PositionAndTime> mUpPositionsFromTime;

// --------------------------- CONSTRUCTORS ---------------------------

    public Gesture() {
        mPalmDefects = new LinkedList<>();
        mLeftPositionsFromTime = new LinkedList<>();
        mRightPositionsFromTime = new LinkedList<>();
        mDownPositionsFromTime = new LinkedList<>();
        mUpPositionsFromTime = new LinkedList<>();
        properties = new Properties();
    }

// -------------------------- OTHER METHODS --------------------------

    public boolean initFromMaskAndPosition(Mat search_hand_mask, int nearestFromX, int nearestfromY) {
        // Check each contours
        List<MatOfPoint> contours = new LinkedList<>();
        findContours(search_hand_mask, contours, new Mat(), RETR_TREE, CHAIN_APPROX_SIMPLE);

        List<Integer> distanceContours = new LinkedList<>();

        if (contours.isEmpty()) {
            return false;
        }

        for (MatOfPoint contour : contours) {
            List<MatOfPoint> contourList = Collections.singletonList(contour);
            drawContours(Motion.currentFrame, contourList, 0, new Scalar(0, 0, 250), 2);
            Moments moments = moments(contour);
            centerX = (int) round(moments.get_m10() / moments.get_m00());
            centerY = (int) round(moments.get_m01() / moments.get_m00());
            int distance = abs(centerX - nearestFromX) + abs(centerY - nearestfromY);
            distanceContours.add(distance);
        }

        for (int i = 0; i < contours.size(); i++) {
            int idxNearestContour = min(i, distanceContours.get(i));
            if (!initFromContour(contours.get(idxNearestContour))) {
                contours.remove(idxNearestContour);
                distanceContours.remove(idxNearestContour);
                continue;
            }
            return true;
        }

        return false;
    }

    public boolean searchPalmFromMask(Mat searchHandMask) {
        return initFromMask(searchHandMask) && checkForPalm();
    }

    private boolean checkForPalm() {
        int[] defects = mDefects.toArray();
        Point[] data = mHandContour.toArray();
        for (int i = 0; i < defects.length; i = i + 4) {
            Point start = data[defects[i]];
            Point end = data[defects[i + 1]];
            Point far = data[defects[i + 2]];

            // Check if defect is in the proper place
            if (!inCircle(centerX, centerY, round(mRadius / 1.5F), far.x, far.y)) {
                continue;
            }
            if (inCircle(centerX, centerY, round(mRadius / 3.2F), far.x, far.y)) {
                continue;
            }

            // Filter defect by angle
            double a = sqrt(pow(end.x - start.x, 2) + pow(end.y - start.y, 2));
            double b = sqrt(pow(far.x - start.x, 2) + pow(far.y - start.y, 2));
            double c = sqrt(pow(end.x - far.x, 2) + pow(end.y - far.y, 2));

            int angle = (int) acos(pow(b, 2) + pow(c, 2) - pow(a, 2) / (2 * b * c)) * 57;
            if (angle <= 120) {
                mPalmDefects.add(new Point[]{start, end, far});
                if (palmDefectsValid()) {
                    mPalm = true;
                    return true;
                }
            }
        }
        return false;
    }

    private void checkForSliding() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - mTimeLastHandSlide < HAND_DELAY_AFTER_SLIDE) {
            return;
        }

        // Check each direction
        mSlideLeft = checkSlidingFromPositions(mLeftPositionsFromTime, mRecX, true, currentTime);
        mSlideRight = checkSlidingFromPositions(mRightPositionsFromTime, mRecX + mRecW, false, currentTime);
        mSlideDown = checkSlidingFromPositions(mUpPositionsFromTime, mRecY, true, currentTime);
        mSlideUp = checkSlidingFromPositions(mDownPositionsFromTime, mRecY + mRecH, false, currentTime);
    }

    private void checkForThumbs() {
        float spaceOutsideOfCenter = (mRecH - (mRadius * 2)) / mRecH;
        if (spaceOutsideOfCenter < HAND_THUMBS_DETECT_MIN_HEIGHT_RATIO) {
            return;
        }
        if (((centerY - mRadius - mRecY) / mRecH) > HAND_THUMBS_DETECT_MIN_HEIGHT_RATIO) {
            mThumbsUp = true;
        } else if ((mRecY + mRecH - centerY + mRadius) / mRecH > HAND_THUMBS_DETECT_MIN_HEIGHT_RATIO) {
            mThumbsDown = true;
        }
    }

    private boolean checkSlidingFromPositions(List<PositionAndTime> positionsFromTime, int newPosition, boolean newPositionMustBeGreater, long currentTime) {
        // Clean timed out position
        boolean sliding = false;

        List<PositionAndTime> newPositionsFromTime = new LinkedList<>();
        for (PositionAndTime positionAndTime : positionsFromTime) {
            if (currentTime - positionAndTime.time < HAND_MAX_TIME_HANDLE_FOR_SLIDE) {
                newPositionsFromTime.add(positionAndTime);
            }
        }
        positionsFromTime = newPositionsFromTime;

        // Clean if current contradict revert from last position
        if (!positionsFromTime.isEmpty() &&
                ((!newPositionMustBeGreater && getLastPosition(positionsFromTime) < newPosition) ||
                        (newPositionMustBeGreater && getLastPosition(positionsFromTime) > newPosition))) {
            positionsFromTime.clear();
        }

        positionsFromTime.add(new PositionAndTime(newPosition, currentTime));

        long positionHandMove = abs(positionsFromTime.get(0).position - getLastPosition(positionsFromTime));

        if (positionsFromTime.size() > 1 && positionHandMove >= HAND_MIN_MOVE_HAND_FOR_SLIDE) {
            sliding = true;
            mLeftPositionsFromTime.clear();
            mRightPositionsFromTime.clear();
            mUpPositionsFromTime.clear();
            mDownPositionsFromTime.clear();
            mTimeLastHandSlide = currentTime;
        }

        return sliding;
    }

    private long getLastPosition(List<PositionAndTime> positionAndTimes) {
        return positionAndTimes.get(positionAndTimes.size() - 1).position;
    }

    private boolean inCircle(int center_x, int center_y, int radius, double x, double y) {
        double dist = sqrt(pow((center_x - x), 2) + pow(center_y - y, 2));
        return dist <= radius;
    }

    private boolean initFromContour(MatOfPoint contour) {
        final Rect bounds = boundingRect(contour);
        mRecH = bounds.height;
        mRecW = bounds.width;
        mRecY = bounds.y;
        mRecX = bounds.x;
        if (mRecH < HAND_MIN_HEIGHT || mRecH > HAND_MAX_HEIGHT || mRecW < HAND_MIN_WIDTH || mRecW > HAND_MAX_WIDTH) {
            return false;
        }

        mHandContour = contour;
        mFoundHand = true;

        Moments moments = moments(contour);

        MatOfInt hull = new MatOfInt();
        convexHull(mHandContour, hull, false);

        mDefects = new MatOfInt4();
        convexityDefects(mHandContour, hull, mDefects);

        float[] radius = new float[0];
        minEnclosingCircle(new MatOfPoint2f(mHandContour.toArray()), new Point(), radius);

        mRadius = round(radius[0] / 1.2F);
        centerX = (int) round(moments.get_m10() / moments.get_m00());
        centerY = (int) round(moments.get_m01() / moments.get_m00());

        initGestures();

        return true;
    }

    private boolean initFromMask(Mat searchHandMask) {
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(searchHandMask, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        for (MatOfPoint contour : contours) {
            if (initFromContour(contour)) {
                return true;
            }
        }
        return false;
    }

    private void initGestures() {
        checkForPalm();
        if (!mPalm) {
            checkForThumbs();
        }
        checkForSliding();
    }

    private boolean palmDefectsValid() {
        if (mPalmDefects.size() < 4) {
            return false;
        }

        int nb_defect_above_line = 0;
        List<Integer> positions_y = new LinkedList<>();
        List<Integer> positions_x = new LinkedList<>();

        for (Point[] defect : mPalmDefects) {
            if (defect[2].y < centerY) {
                nb_defect_above_line += 1;
                positions_x.add((int) defect[2].x);
                positions_y.add((int) defect[2].y);
            }
        }

        if (nb_defect_above_line < 3) {
            return false;
        }

        float avg_y = sum(positions_y) / positions_y.size();
        for (double position : positions_y) {
            if (abs(position - avg_y) / mRecH > HAND_MAX_Y_DISTANCE_BETWEEN_DEFECT_FOR_PALM_IN_HAND_RATIO) {
                return false;
            }
        }

        float avg_x = sum(positions_x) / positions_x.size();
        for (double position : positions_x) {
            if (abs(position - avg_x) / mRecW > HAND_MAX_X_DISTANCE_BETWEEN_DEFECT_FOR_PALM_IN_HAND_RATIO) {
                return false;
            }
        }

        return true;
    }

    private int sum(List<Integer> list) {
        int sum = 0;
        for (int i : list) {
            sum += i;
        }
        return sum;
    }
}

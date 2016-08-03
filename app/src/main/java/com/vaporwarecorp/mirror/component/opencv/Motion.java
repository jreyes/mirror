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

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Properties;

import static org.opencv.core.Core.absdiff;
import static org.opencv.core.Core.inRange;
import static org.opencv.imgproc.Imgproc.*;

public class Motion {
// ------------------------------ FIELDS ------------------------------

    private static final int HAND_TIME_TO_KEEP_SEARCHING_HAND_WHEN_LOST_TRACKING = 0;

    private Mat currentFrame;
    private Gesture currentGesture;
    private boolean foundHand;
    private Scalar handPointHSV;
    private Gesture handTracked;
    private double movementRatio;
    private Mat previousFrame;
    private Properties previousGestureProperties;
    private long timeLastMotion;
    private long timeSinceFoundHandTracked;
    private long timeSinceLastDifferentGesture;

// --------------------------- CONSTRUCTORS ---------------------------

    public Motion() {
    }

    private double addValueToColor(double value, double color) {
        double result = color + value;
        if (result > 255) {
            color = 255;
        } else if (result < 0) {
            color = 0;
        } else {
            color = result;
        }
        return color;
    }

    private Scalar addValueToColorArray(Scalar values, Scalar colors) {
        for (int i = 0; i <= 3; i++) {
            colors.val[i] = addValueToColor(values.val[i], colors.val[i]);
        }
        return colors;
    }

    private void findHandFromTrack() {
        // Get brightness from tracked hand
        Mat kernel = getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));

        Mat blurred = new Mat();
        Imgproc.filter2D(currentFrame.clone(), blurred, -1, kernel);

        Mat hsv = new Mat();
        Imgproc.cvtColor(blurred, hsv, COLOR_BGR2HSV);

        Scalar hand_lower_blue = addValueToColorArray(new Scalar(255, 55, 32), handPointHSV.clone());
        Scalar hand_upper_blue = addValueToColorArray(new Scalar(255, 255, 255), handPointHSV.clone());

        Mat mask = new Mat();
        inRange(hsv, hand_lower_blue, hand_upper_blue, mask);

        Mat mask_refined = new Mat();
        Imgproc.morphologyEx(mask, mask_refined, MORPH_OPEN, kernel);

        Mat search_hand_mask = mask_refined.clone();
        Gesture search_hand = new Gesture();
        if (!search_hand.initFromMaskAndPosition(search_hand_mask, handTracked.centerX, handTracked.centerY)) {
            foundHand = false;
        } else {
            timeSinceFoundHandTracked = System.currentTimeMillis();
            handTracked = search_hand;
            // self.handPointHSV = hsv[self.handTracked.centerY][self.handTracked.centerX]
            handPointHSV = new Scalar(hsv.get(handTracked.centerY, handTracked.centerX));
            foundHand = true;
        }
    }

    private boolean foundMovement() {
        return movementRatio > 1;
    }

    private Gesture getGesture() {
        // Retry a few times before initing with palm again
        long timeElapsedSinceLastFoundHand = System.currentTimeMillis() - timeSinceFoundHandTracked;

        boolean stillTringToFindHandFromTrack = timeElapsedSinceLastFoundHand < HAND_TIME_TO_KEEP_SEARCHING_HAND_WHEN_LOST_TRACKING;
        if (!stillTringToFindHandFromTrack) {
            handTracked = null;
        }

        if (handTracked != null || stillTringToFindHandFromTrack) {
            findHandFromTrack();
        } else {
            tryToTrackHand();
        }

        currentGesture = handTracked;
        if (!foundHand) {
            currentGesture = new Gesture();
            currentGesture.properties.put("needInitPalm", !stillTringToFindHandFromTrack);
        }

        setTimeElapsedSinceSameGesture();
        return currentGesture;
    }

    /*
def GetInformationOnNextFrame(self):

    #
    cntGray = 0
    for rowGray in self.frameDifference:
        for gray in rowGray:
            cntGray += gray
    self.movementRatio = cntGray / self.frameDifference.size

 */

    private void getInformationOnNextFrame(Mat frame) {
        // Store previous frame
        previousFrame = currentFrame;
        currentFrame = frame;

        if (previousFrame == null) {
            return;
        }

        // Get frame difference to avoid doing things when there is no movement
        Mat currentFrameMat = new Mat();
        cvtColor(currentFrame, currentFrameMat, COLOR_RGB2GRAY);

        Mat previousFrameMat = new Mat();
        cvtColor(previousFrame, previousFrameMat, COLOR_BGR2GRAY);

        Mat frameDifference = new Mat();
        absdiff(currentFrameMat, previousFrameMat, frameDifference);

        int cntGray = 0;
        for (frameDifference.row) {
        }
        movementRatio = (cntGray / frameDifference.elemSize());


        if (!foundMovement()) {
            return;
        }

        // Keep track of last motion
        timeLastMotion = System.currentTimeMillis();
    }

    private void setTimeElapsedSinceSameGesture() {
        if (previousGestureProperties == null) {
            previousGestureProperties = (Properties) currentGesture.properties.clone();
        }
        if (currentGesture.properties.get("palm").equals(previousGestureProperties.get("palm")) &&
                currentGesture.properties.get("thumbsUp").equals(previousGestureProperties.get("thumbsUp")) &&
                currentGesture.properties.get("thumbsDown").equals(previousGestureProperties.get("thumbsDown")) &&
                currentGesture.properties.get("slideRight").equals(previousGestureProperties.get("slideRight")) &&
                currentGesture.properties.get("slideLeft").equals(previousGestureProperties.get("slideLeft")) &&
                currentGesture.properties.get("slideDown").equals(previousGestureProperties.get("slideDown")) &&
                currentGesture.properties.get("slideUp").equals(previousGestureProperties.get("slideUp"))) {
            currentGesture.properties.put("elapsedTimeWithSameGesture", System.currentTimeMillis() - timeSinceLastDifferentGesture);
        } else {
            timeSinceLastDifferentGesture = System.currentTimeMillis();
            currentGesture.properties.put("elapsedTimeWithSameGesture", 0);
        }
        previousGestureProperties = (Properties) currentGesture.properties.clone();
    }

    private long timeElapsedSinceLastMotion() {
        return System.currentTimeMillis() - timeLastMotion;
    }

    private void tryToTrackHand() {
        int lower_blue_brightness = 255;
        Gesture search_hand = new Gesture();
        while (lower_blue_brightness > 15) {
            // define range of blue color in HSV
            Scalar lower_blue = new Scalar(255, 55, lower_blue_brightness);
            Scalar upper_blue = new Scalar(255, 255, 255);

            Mat kernel = getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));

            Mat blurred = new Mat();
            Imgproc.filter2D(currentFrame.clone(), blurred, -1, kernel);

            Mat hsv = new Mat();
            Imgproc.cvtColor(blurred, hsv, COLOR_BGR2HSV);

            // Threshold the HSV image to get only blue colors
            Mat mask = new Mat();
            inRange(hsv, lower_blue, upper_blue, mask);

            Mat mask_refined = new Mat();
            morphologyEx(mask, mask_refined, MORPH_OPEN, kernel);

            if (search_hand.searchPalmFromMask(mask_refined.clone())) {
                // Set infos from tracked hand
                handTracked = search_hand;
                timeSinceFoundHandTracked = System.currentTimeMillis();
                handPointHSV = new Scalar(hsv.get(handTracked.centerY, handTracked.centerX));
                foundHand = true;
                return;
            }

            lower_blue_brightness -= 10;
        }
        foundHand = false;
    }
}

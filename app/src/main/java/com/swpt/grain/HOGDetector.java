package com.swpt.grain;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.HOGDescriptor;

public class HOGDetector {
    private static final int HIT_THRESHOLD = 0;

    private HOGDescriptor hog;
    private MatOfRect rects = new MatOfRect();
    private MatOfDouble weights = new MatOfDouble();

    public HOGDetector() {
        hog = new HOGDescriptor();
        boolean loaded = hog.load("hog");
    }

    public int detectObject(Mat img) {
        hog.detectMultiScale(img, rects, weights, HIT_THRESHOLD);
        Rect[] rectArray = rects.toArray();
    }
}

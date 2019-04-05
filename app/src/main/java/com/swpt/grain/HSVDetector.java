package com.swpt.grain;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class HSVDetector {
    Scalar lowerBound = new Scalar(0, 0, 0);
    Scalar upperBound = new Scalar(255, 255, 255);
    Mat bounds;
    Mat hsv;


    public void setBound1(float lowerB, float higherB) {
        setBound(0, lowerB, higherB);
    }

    public void setBound2(float lowerB, float higherB) {
        setBound(1, lowerB, higherB);
    }

    public void setBound3(float lowerB, float higherB) {
        setBound(2, lowerB, higherB);
    }

    private void setBound(int index, float lowerB, float higherB) {
        double[] lowDouble = lowerBound.val;
        lowDouble[index] = lowerB;
        double[] highDouble = upperBound.val;
        highDouble[index] = higherB;
        lowerBound.set(lowDouble);
        upperBound.set(highDouble);
        System.out.println(lowerBound);
        System.out.println(upperBound);
    }


    public void detectColor(Mat img) {
        if(bounds == null && hsv == null) {
            bounds = new Mat(img.cols(), img.rows(), CvType.CV_8U);
            hsv = new Mat(img.cols(), img.rows(), img.type());
        }
        Imgproc.cvtColor(img, hsv, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(hsv, hsv, Imgproc.COLOR_RGB2HSV);
        Core.inRange(hsv, lowerBound, upperBound, bounds);
        bounds.copyTo(img, bounds);
    }
}

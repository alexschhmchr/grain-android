package com.swpt.grain;

import android.content.res.AssetManager;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.io.IOException;

public class HOGDetector {
    private static final double HIT_THRESHOLD = 2.5;

    private Mat gray = new Mat();
    private HOGDescriptor hog;
    private MatOfRect rects = new MatOfRect();
    private MatOfPoint points = new MatOfPoint();
    private MatOfDouble weights = new MatOfDouble();
    private double weightTreshold;

    public HOGDetector(double weightThreshold, String filesPath) {
        hog = new HOGDescriptor();
        System.out.println(filesPath);
        boolean loaded = hog.load(filesPath + "/hog.yml");
        System.out.println("loaded: " + loaded);
        this.weightTreshold = weightThreshold;
    }

    public void setWeightTreshold(double weightTreshold) {
        this.weightTreshold = weightTreshold;
    }

    public int detectObject(Mat img) {
        System.out.println(img.size());
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
        hog.detect(gray, points, weights, weightTreshold);
        //hog.detectMultiScale(gray, rects, weights, HIT_THRESHOLD);
        //Rect[] rectArray = rects.toArray();
        Point[] pointArray = points.toArray();
        double[] weightArray = null;
        if(pointArray.length > 0) {
            weightArray = weights.toArray();
        }
        System.out.println(pointArray.length);
        for(int i = 0; i < pointArray.length; i++) {
            System.out.println("weight: " + weightArray[i]);
            Point p2 = pointArray[i].clone();
            p2.x += 440;
            p2.y += 440;
            Imgproc.rectangle(img, pointArray[i], p2, new Scalar(255, 0, 0), 2);
        }
        return pointArray.length;
    }
}

package com.swpt.grain;

import android.content.res.AssetManager;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.io.IOException;

public class HOGDetector {
    private static final int HIT_THRESHOLD = 0;

    private HOGDescriptor hog;
    private MatOfRect rects = new MatOfRect();
    private MatOfDouble weights = new MatOfDouble();
    private double weightTreshold;

    public HOGDetector(double weightThreshold, AssetManager assetManager) {
        hog = new HOGDescriptor();
        String fileName = "file:///android_asset/hog.yml";
        try {
            String[] files = assetManager.list("");
            for (String file :
                    files) {
                System.out.println(file);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean loaded = hog.load(fileName);
        System.out.println("loaded: " + loaded);
        this.weightTreshold = weightThreshold;
    }

    public int detectObject(Mat img) {
        hog.detectMultiScale(img, rects, weights, HIT_THRESHOLD);
        Rect[] rectArray = rects.toArray();
        double[] weightArray = weights.toArray();
        for(int i = 0; i < rectArray.length; i++) {
            Imgproc.rectangle(img, rectArray[i], new Scalar(255, 0, 0), 2);
        }
        return rectArray.length;
    }
}

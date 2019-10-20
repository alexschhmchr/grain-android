package com.swpt.grain;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.SVM;
import org.opencv.objdetect.HOGDescriptor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class HOGDetector {
    private static final double HIT_THRESHOLD = 2.5;

    private Mat gray = new Mat();
    private HOGDescriptor hog;
    private SVM svm;
    private int winWidth;
    private int winHeight;
    private MatOfRect rects = new MatOfRect();
    private MatOfPoint points = new MatOfPoint();
    private MatOfDouble weights = new MatOfDouble();
    private int winStrideX;
    private int winStrideY;


    public HOGDetector(String filesPath, int winStrideX, int winStrideY) {
        hog = new HOGDescriptor();
        svm = SVM.load(filesPath + "/hog_all_svm");
        System.out.println("var count: " + svm.getVarCount());
        System.out.println(filesPath);
        boolean loaded = hog.load(filesPath + "/hog_all_params");
        Size winSize = hog.get_winSize();
        winWidth = (int) winSize.width;
        winHeight = (int) winSize.height;
        System.out.println("loaded: " + loaded);
        this.winStrideX = winStrideX;
        this.winStrideY = winStrideY;
    }

    private ArrayList<Rect> slideWindow(Mat img) {
        ArrayList<Rect> windowList = new ArrayList<>();
        Rect window = new Rect(0, 0, winWidth, winHeight);
        while (window.y + window.height < img.height()) {
            windowList.add(window.clone());
            window.x += winStrideX;
            if (window.x + window.width > img.width()) {
                window.x = 0;
                window.y += winStrideY;
            }
        }
        return windowList;
    }

    public int detect(Mat img) {
        ArrayList<Rect> windows = slideWindow(img);
        MatOfFloat descriptors = new MatOfFloat();
        for(Rect window : windows) {
            Mat roi = new Mat(img, window);
            MatOfFloat descriptor = new MatOfFloat();
            hog.compute(roi, descriptor);
            descriptors.push_back(descriptor);
            roi.release();
        }
        Mat resultMat = new Mat(new Size(1, windows.size()), CvType.CV_32F);
        Mat descriptorsMat = descriptors.reshape(1, windows.size());
        svm.predict(descriptorsMat, resultMat);
        MatOfFloat resultFloatMat = new MatOfFloat(resultMat);
        float[] resultArray = resultFloatMat.toArray();
        int counter = 0;
        for(int i = 0; i < windows.size(); i++) {
            if(resultArray[i] != -1) {
                Rect window = windows.get(i);
                Imgproc.rectangle(img, window, new Scalar(255, 0, 0), 2);
                Imgproc.putText(img, idToName(resultArray[i]), window.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 255, 0), 2);
                counter++;
            }
        }
        return counter;
    }

    private String idToName(float id) {
        switch ((int) id) {
            case 1: return "Mais";
            case 2: return "Roggen";
            case 3: return "Triticale";
            case 4: return "Weizen";
        }
        return "Mehl";
    }

    /*public int detectObject(Mat img) {
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
    }*/
}

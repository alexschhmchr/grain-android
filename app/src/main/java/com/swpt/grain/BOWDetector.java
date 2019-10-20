package com.swpt.grain;

import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.AKAZE;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.Feature2D;
import org.opencv.features2d.KAZE;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.SVM;

import java.util.ArrayList;

public class BOWDetector {
    private static final int WINDOW_WIDTH = 128;
    private static final int WINDOW_HEIGHT = 128;

    private Feature2D ftExtractor;
    private BFMatcher matcher;
    private SVM svm;
    private Mat tempDes = new Mat();
    private MatOfKeyPoint kps = new MatOfKeyPoint();
    private Mat empty = new Mat();
    private MatOfDMatch matches = new MatOfDMatch();
    private MatOfFloat descriptor = new MatOfFloat();
    private int clusters;
    private int winStrideX;
    private int winStrideY;


    public BOWDetector(String filesPath, int winStrideX, int winStrideY) {
        ftExtractor = KAZE.create();
        svm = SVM.load(filesPath + "/bow_svm");
        matcher = BFMatcher.load(filesPath + "/bow_params");
        System.out.println("Train descriptors: " + matcher.getTrainDescriptors().size());
        clusters = matcher.getTrainDescriptors().get(0).height();
        this.winStrideX = winStrideX;
        this.winStrideY = winStrideY;
    }

    private ArrayList<Rect> slideWindow(Mat img) {
        ArrayList<Rect> windowList = new ArrayList<>();
        Rect window = new Rect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
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

    private void computeHistogram(Mat roi, MatOfFloat descriptor) {
        ftExtractor.detectAndCompute(roi, empty, kps, tempDes);
        matcher.match(tempDes, matches);
        descriptor.fromArray(toHistgramm(matches));
    }

    public int detect(Mat img) {
        ArrayList<Rect> windows = slideWindow(img);
        MatOfFloat descriptors = new MatOfFloat();

        System.out.println("windows:" + windows.size());
        for(Rect window : windows) {
            Mat roi = new Mat(img, window);
            computeHistogram(roi, descriptor);
            descriptors.push_back(descriptor);
            roi.release();
            System.out.println("scanned window");
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

    private float[] toHistgramm(MatOfDMatch matches) {
        float[] histogramm = new float[clusters];
        for(DMatch match : matches.toArray()) {
            histogramm[match.trainIdx] += 1;
        }
        return histogramm;
    }

    /*public void detectInWindow(Rect window, Mat img) {
        Mat roi = new Mat(img, window);
        ftExtractor.detectAndCompute(roi, null, null, des);
        matcher.match(des, matches);
        roi.release();
    }



    private float predictObject(float[] histogramm) {
        MatOfFloat histoMat = new MatOfFloat(histogramm);
        float prediction = classifier.predict(histoMat);
        return prediction;
    }

    private void drawObject(Mat img) {

    }

    @Override
    public int detectObject(Mat img) {
        int objCounter = 0;
        Rect roi = new Rect();
        roi.width = 150;
        roi.height = 150;
        roi.x = 0;
        roi.y = 0;
        int slideInX = (img.width() - roi.width) / SLIDE_SIZE;
        int slideInY = (img.height() - roi.height) / SLIDE_SIZE;
        for(int i = 0; i < slideInY; i++) {
            for(int k = 0; k < slideInX; k++) {
                detectInWindow(roi, img);
                float[] histogramm = toHistgramm(matches);
                float prediction = predictObject(histogramm);
                if(prediction > PREDICTION_THRESHOLD) {
                    drawObject(img);
                    objCounter++;
                }
                roi.x += SLIDE_SIZE;
            }
            roi.y += SLIDE_SIZE;
        }
        return objCounter;
    }*/

}

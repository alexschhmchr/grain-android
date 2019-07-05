package com.swpt.grain;

import org.opencv.core.Algorithm;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.features2d.AKAZE;
import org.opencv.features2d.Feature2D;
import org.opencv.features2d.FlannBasedMatcher;
import org.opencv.ml.SVM;

public class BOWDetector implements Detector{
    private static final int SLIDE_SIZE = 50;
    private static final float PREDICTION_THRESHOLD = 0.8f;

    private Feature2D ftExtractor;
    private FlannBasedMatcher matcher;
    private SVM classifier;
    private Mat des = new Mat();
    private MatOfDMatch matches = new MatOfDMatch();
    private int clusters;

    public BOWDetector(String ftFilePath, String matcherFilePath, String svmFilePath) {
        ftExtractor = AKAZE.create();
        ftExtractor.read(ftFilePath);
        matcher = FlannBasedMatcher.create();
        matcher.read(matcherFilePath);
        clusters = matcher.getTrainDescriptors().get(0).height();
        classifier = SVM.load(svmFilePath);
    }

    public void detectInWindow(Rect window, Mat img) {
        Mat roi = new Mat(img, window);
        ftExtractor.detectAndCompute(roi, null, null, des);
        matcher.match(des, matches);
        roi.release();
    }

    private float[] toHistgramm(MatOfDMatch matches) {
        float[] histogramm = new float[clusters];
        for(DMatch match : matches.toArray()) {
            histogramm[match.trainIdx] += 1;
        }
        return histogramm;
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
    }

}

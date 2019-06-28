package com.swpt.grain;

import org.opencv.core.Algorithm;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.features2d.AKAZE;
import org.opencv.features2d.Feature2D;
import org.opencv.ml.SVM;

public class BOWDetector implements Detector{
    private Feature2D ftExtractor;
    private SVM classifier;

    public BOWDetector(String svmFilePath, String ftFilePath) {
        ftExtractor = AKAZE.create();
        ftExtractor.read(ftFilePath);
        classifier = SVM.load(svmFilePath);
    }

    public void detectInWindow(Rect window, Mat img) {

    }

    @Override
    public int detectObject(Mat img) {

        return 0;
    }

}

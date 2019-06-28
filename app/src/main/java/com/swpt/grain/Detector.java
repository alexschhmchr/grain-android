package com.swpt.grain;

import org.opencv.core.Mat;

public interface Detector {
    int detectObject(Mat img);
}

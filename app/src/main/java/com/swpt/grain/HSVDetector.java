package com.swpt.grain;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class HSVDetector {
    private boolean showInRangePoints = false;
    private Scalar lowerBound = new Scalar(0, 0, 0);
    private Scalar upperBound = new Scalar(255, 255, 255);
    //private Mat hsvImg = new Mat();
    //private Mat threshImg = new Mat();
    //private Mat nonZero = new Mat();
    private Mat tempMat = new Mat();
    private Mat pyrMat = new Mat(360, 640, CvType.CV_8U);
    private DBScan dbScanner = new DBScan(3, 150);


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


    public ArrayList<Point> getInRangePoints(Mat img, boolean showThreshImg) {
        Mat targetMat;
        if(showThreshImg) {
            targetMat = img;
        } else {
            targetMat = tempMat;
        }
        Imgproc.cvtColor(img, targetMat, Imgproc.COLOR_RGB2HSV);
        Core.inRange(targetMat, lowerBound, upperBound, targetMat);
        Imgproc.pyrDown(img, pyrMat);
        Core.findNonZero(pyrMat, pyrMat);
        int[] data = new int[(int) pyrMat.total() * pyrMat.channels()];
        System.out.println(pyrMat.height());
        ArrayList<Point> pointList = new ArrayList<>();
        if(pyrMat.height() > 0) {
            pyrMat.get(0, 0, data);
            for (int i = 0; i < data.length;) {
                int x = data[i++];
                int y = data[i++];
                pointList.add(new Point(x, y));
            }
        }
        pyrMat.release();
        return pointList;
    }


    public int detectObject(Mat img, boolean showThreshImg) {
        ArrayList<Point> inRangePointList = getInRangePoints(img, showThreshImg);
        ArrayList<DBScan.Cluster> clusters = dbScanner.findClustersNR(inRangePointList);
        for(DBScan.Cluster cluster : clusters) {
            Rect rect = cluster.getClusterRect();
            rect.width *= 2;
            rect.height *= 2;
            rect.x *= 2;
            rect.y *= 2;
            Imgproc.rectangle(img, cluster.getClusterRect(), new Scalar(255), 2);
        }
        return clusters.size();
    }

}

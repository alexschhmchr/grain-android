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
    private static final int MAX_POINTS = 7000;
    private Scalar lowerBound = new Scalar(0, 0, 0);
    private Scalar upperBound = new Scalar(255, 255, 255);
    private Mat tempMat = null;
    private Mat pyrMat = new Mat(360, 640, CvType.CV_8U);
    private DBScan dbScanner = new DBScan(3, 50);


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
        if(tempMat == null) {
            tempMat = new Mat(img.size(), img.type());
        }
        Mat targetMat;
        if(showThreshImg) {
            targetMat = img;
        } else {
            targetMat = tempMat;
        }
        Imgproc.cvtColor(img, targetMat, Imgproc.COLOR_RGB2HSV);
        Core.inRange(targetMat, lowerBound, upperBound, targetMat);
        Imgproc.pyrDown(targetMat, pyrMat);
        Imgproc.pyrDown(pyrMat, pyrMat);
        Imgproc.pyrDown(pyrMat, pyrMat);
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
        if(inRangePointList.size() < MAX_POINTS && !showThreshImg) {
            ArrayList<DBScan.Cluster> clusters = dbScanner.findClustersNR(inRangePointList);
            for(DBScan.Cluster cluster : clusters) {
                Rect rect = cluster.getClusterRect();
                rect.width *= 8;
                rect.height *= 8;
                rect.x *= 8;
                rect.y *= 8;
                Imgproc.rectangle(img, cluster.getClusterRect(), new Scalar(255), 2);
            }
            return clusters.size();
        } else {
            return inRangePointList.size();
        }

    }

}

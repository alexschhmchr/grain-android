package com.swpt.grain;

import org.opencv.core.*;
import java.util.ArrayList;

public class DBScan {
    private double maxDistance;
    private int minPoints;

    public DBScan(double maxDistance, int minPoints) {
        this.maxDistance = maxDistance;
        this.minPoints = minPoints;
    }


    public ArrayList<Cluster> findClusters(ArrayList<Point> pointList) {
        ArrayList<Cluster> clusterList = new ArrayList<>();
        while(!pointList.isEmpty()) {
            ArrayList<Point> clusterPoints = new ArrayList<>();
            Point center = pointList.remove(0);
            findNeighbours(center, pointList, clusterPoints);
            if(clusterPoints.size() > minPoints) {
                Cluster cluster = new Cluster(clusterPoints);
                clusterList.add(cluster);
            }
        }
        System.out.println(clusterList.size());
        return clusterList;
    }

    public ArrayList<Cluster> findClustersNR(ArrayList<Point> pointList) {
        ArrayList<Cluster> clusters = new ArrayList<>();
        while(!pointList.isEmpty()) {
            Point center = pointList.remove(0);
            ArrayList<Point> clusterPoints = findNeighboursNR(center, pointList);
            if(clusterPoints.size() > minPoints) {
                Cluster cluster = new Cluster(clusterPoints);
                clusters.add(cluster);
            }
        }
        return clusters;
    }

    private void findNeighbours(Point center, ArrayList<Point> pointList, ArrayList<Point> clusterPoints) {
        for(int i = 0; i < pointList.size(); i++) {
            Point p = pointList.get(i);
            double distance = Math.abs(getNorm(center, p));
            if(distance < maxDistance) {
                clusterPoints.add(p);
                pointList.remove(i--);
                findNeighbours(p, pointList, clusterPoints);
            }
        }
    }

    private ArrayList<Point> findNeighboursNR(Point center, ArrayList<Point> pointList) {
        ArrayList<Point> neighbourPoints = new ArrayList<>();
        ArrayList<Point> outerPoints = new ArrayList<>();
        outerPoints.add(center);
        while(!outerPoints.isEmpty()) {
            ArrayList<Point> newOuterPoints = new ArrayList<>();
            for(Point refPoint : outerPoints) {
                for(int i = 0; i < pointList.size(); i++) {
                    Point p = pointList.get(i);
                    double distance = Math.abs(getNorm(refPoint, p));
                    if(distance < maxDistance) {
                        neighbourPoints.add(p);
                        newOuterPoints.add(p);
                        pointList.remove(i--);
                    }
                }
            }
            outerPoints = newOuterPoints;
        }
        return neighbourPoints;
    }

    private double getNorm(Point point1, Point point2) {
        return getNorm((int) point1.x, (int) point1.y, (int) point2.x, (int) point2.y);
    }

    private double getNorm(int x1, int y1, int x2, int y2) {
        int x = x1 - x2;
        int y = y1 - y2;
        double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        return distance;
    }

    public static class Cluster {
        private ArrayList<Point> clusterPoints;
        private Rect clusterRect;

        private Cluster(ArrayList<Point> clusterPoints) {
            this.clusterPoints = clusterPoints;
        }

        public Rect getClusterRect() {
            if(clusterRect == null) {
                clusterRect = calculateClusterRect();
            }
            return clusterRect;
        }

        private Rect calculateClusterRect() {
            double leftPoint = clusterPoints.get(0).x;
            double rightPoint = 0;
            double topPoint = clusterPoints.get(0).y;
            double bottomPoint = 0;
            for(Point p : clusterPoints) {
                if(p.x < leftPoint) {
                    leftPoint = p.x;
                } else if(p.x > rightPoint) {
                    rightPoint = p.x;
                }
                if(p.y < topPoint) {
                    topPoint = p.y;
                } else if(p.y > bottomPoint) {
                    bottomPoint = p.y;
                }
            }
            int x = (int) Math.round(leftPoint);
            int y = (int) Math.round(topPoint);
            int width = (int) (rightPoint - leftPoint);
            int height = (int) (bottomPoint - topPoint);
            return new Rect(x, y, width, height);
        }
    }
}

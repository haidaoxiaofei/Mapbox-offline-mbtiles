package com.mapbox.mapboxsdk.android.testapp;

/**
 * Created by rui on 14-9-14.
 */
public class Segment {
    public int areaId;
    public Point sPoint;
    public Point ePoint;
    private static Point tmpPoint1 = new Point();
    private static Point tmpPoint2 = new Point();
    private static Segment tmpSegment = new Segment(tmpPoint1, tmpPoint2);

    public Segment(Point startPoint, Point endPoint) {
        super();
        this.sPoint = startPoint;
        this.ePoint = endPoint;
    }
    public Segment(float x1, float y1, float x2, float y2, int areaId) {
        this(new Point(x1, y1), new Point(x2, y2), areaId);
    }
    public Segment(Point startPoint, Point endPoint, int areaId) {
        super();
        this.sPoint = startPoint;
        this.ePoint = endPoint;
    }
    public Segment(Segment s) {
        this(s.sPoint.x, s.sPoint.y, s.ePoint.x, s.ePoint.y, s.areaId);
    }

    public Point getsPoint() {
        return sPoint;
    }

    public void setsPoint(Point sPoint) {
        this.sPoint = sPoint;
    }

    public Point getePoint() {
        return ePoint;
    }

    public void setePoint(Point ePoint) {
        this.ePoint = ePoint;
    }

    public Segment(float x1, float y1, float x2, float y2) {
        this(new Point(x1, y1), new Point(x2, y2));
    }

    @Override
    public String toString() {
        return sPoint.getString() + " " + ePoint.getString();
    }

    double cross(Point p1, Point p2, Point p0) {
        return ((p1.x - p0.x) * (p2.y - p0.y) - (p2.x - p0.x) * (p1.y - p0.y));
    }

    public Point projectPoint(Point p) {
        Point pP = new Point();
        float xd = sPoint.x - ePoint.x;
        float yd = sPoint.y - ePoint.y;
        if (xd == 0) {
            pP.x = sPoint.x;
            pP.y = p.y;
        } else if (yd == 0) {
            pP.y = sPoint.y;
            pP.x = p.x;
        } else {
            double k = yd / xd;
            pP.x = (float) ((k * sPoint.x + p.x / k + p.y - sPoint.y) / (1 / k + k));
            pP.y = (float) (-1 / k * (pP.x - p.x) + p.y);
        }

        return pP;
    }

    public float distanceLine(Point p) // a和b是线段的两个端点， c是检测点
    {
        Point ab = new Point(ePoint.x - sPoint.x, ePoint.y - sPoint.y);
        Point ac = new Point(p.x - sPoint.x, p.y - sPoint.y);
        float f = ab.x * ac.x + ab.y * ac.y;
        if (f <= 0) {
            return (float) sPoint.distance(p);
        }
        float d = ab.x * ab.x + ab.y * ab.y;
        if (f >= d) {
            return (float) ePoint.distance(p);
        }
        f = f / d;
        Point D = new Point(sPoint.x + f * ab.x, sPoint.y + f * ab.y);   // c在ab线段上的投影点
        return (float) p.distance(D);
    }

    public boolean isChained(Segment s) {
        return this.sPoint.isNearTo(s.sPoint, Point.RADIUS)
                || this.sPoint.isNearTo(s.ePoint, Point.RADIUS)
                || this.ePoint.isNearTo(s.sPoint, Point.RADIUS)
                || this.ePoint.isNearTo(s.ePoint, Point.RADIUS);
    }

    public boolean isIntersected(float x1, float y1, float x2, float y2) {
        tmpPoint1.x = x1;
        tmpPoint1.y = y1;
        tmpPoint2.x = x2;
        tmpPoint2.y = y2;
        return isIntersected(tmpPoint1, tmpPoint2);
    }

    public boolean isIntersected(Point p1, Point p2) {
        tmpSegment.setsPoint(p1);
        tmpSegment.setePoint(p2);
        return isIntersected(tmpSegment);
    }

    public boolean isIntersected(Segment w) {
        return ((Math.max(this.sPoint.x, this.ePoint.x) >= Math.min(w.sPoint.x, w.ePoint.x))
                && (Math.max(w.sPoint.x, w.ePoint.x) >= Math.min(this.sPoint.x, this.ePoint.x))
                && (Math.max(this.sPoint.y, this.ePoint.y) >= Math.min(w.sPoint.y, w.ePoint.y))
                && (Math.max(w.sPoint.y, w.ePoint.y) >= Math.min(this.sPoint.y, this.ePoint.y))
                && (cross(w.sPoint, this.ePoint, this.sPoint) * cross(this.ePoint, w.ePoint, this.sPoint) >= 0)
                && (cross(this.sPoint, w.ePoint, w.sPoint) * cross(w.ePoint, this.ePoint, w.sPoint) >= 0));
    }

    public Point intersectPoint(Segment s) {
        if (!this.isIntersected(s)) {
            return null;
        }
        double deita = (ePoint.x - sPoint.x) * (s.sPoint.y - s.ePoint.y) - (s.ePoint.x - s.sPoint.x) * (sPoint.y - ePoint.y);
        float x = (s.sPoint.y * s.ePoint.x - s.sPoint.x * s.ePoint.y) * (ePoint.x - sPoint.x)
                - (sPoint.y * ePoint.x - sPoint.x * ePoint.y) * (s.ePoint.x - s.sPoint.x);
        x /= deita;
        float y = (sPoint.y * ePoint.x - sPoint.x * ePoint.y) * (s.sPoint.y - s.ePoint.y)
                - (s.sPoint.y * s.ePoint.x - s.sPoint.x * s.ePoint.y) * (sPoint.y - ePoint.y);
        y /= deita;

        return new Point(x, y);
    }
    public double length(){
        return Math.sqrt((sPoint.x - ePoint.getX()) * (sPoint.x - ePoint.getX())
                + (sPoint.y - ePoint.getY()) * (sPoint.y - ePoint.getY()));
    }

}
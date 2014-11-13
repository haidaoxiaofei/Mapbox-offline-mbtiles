package com.mapbox.mapboxsdk.android.testapp;


public class Point implements Comparable<Point> {

	public double x;
	public double y;
	private double weight = Double.MAX_VALUE;
	public static final int RADIUS = 16;
	private static int GREATER = 1;
	private static int LESS = -1;
	private static int EQUAL = 0;
	private static SortMethod sortMethod = SortMethod.DICTIONARY;

	public Point() {
		this(0, 0);

	}

	public Point(Point p) {
		this(p.x, p.y);
	}
	
	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }


	public Point(String x, String y) {
		this.x = Double.valueOf(x);
		this.y = Double.valueOf(y);
	}

	public void setWeight(double weigth) {
		this.weight = weigth;
	}

	public double getWeight() {
		return weight;
	}

	public static void setSortMethod(SortMethod sortMethod) {
		Point.sortMethod = sortMethod;
	}

	public double distance(Point q) {
		return Math.sqrt((this.x - q.getX()) * (this.x - q.getX())
				+ (this.y - q.getY()) * (this.y - q.getY()));
	}

	@Override
	public boolean equals(Object obj) {
		return this.x == ((Point) obj).x && this.y == ((Point) obj).y;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 11
				* hash
				+ (int) (Double.doubleToLongBits(this.x) ^ (Double
						.doubleToLongBits(this.x) >>> 32));
		hash = 11
				* hash
				+ (int) (Double.doubleToLongBits(this.y) ^ (Double
						.doubleToLongBits(this.y) >>> 32));
		return hash;
	}

	@Override
	public String toString() {
		return "[x=" + x + ", y=" + y + "]";
	}

	public String getString() {
		return "" + (int) x + " " + (int) y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public boolean isNearTo(Point p, float radius) {
		return radius > this.distance(p);
	}

	@Override
	public int compareTo(Point o) {
		switch (sortMethod) {
		case DICTIONARY:
			if (this.x > o.x) {
				return GREATER;
			} else if (this.x < o.x) {
				return LESS;
			} else {
				if (this.y > o.y) {
					return GREATER;
				} else if (this.y < o.y) {
					return LESS;
				} else {
					return EQUAL;
				}
			}
		case X_BASED:
			if (this.x > o.x) {
				return GREATER;
			} else if (this.x < o.x) {
				return LESS;
			} else {
				return EQUAL;
			}

		case Y_BASED:
			if (this.y > o.y) {
				return GREATER;
			} else if (this.y < o.y) {
				return LESS;
			} else {
				return EQUAL;
			}
		case WEIGHT:
			if (this.weight > o.getWeight()) {
				return GREATER;
			} else if (this.weight > o.getWeight()) {
				return LESS;
			} else {
				return EQUAL;
			}
		default:
			throw new AssertionError();
		}

	}

	public enum SortMethod {
		X_BASED, Y_BASED, DICTIONARY, WEIGHT;
	}

	public enum PointType {
		START, END, MARK, CHOSEMARKER, CHOSESEGMENT, EMPTY;
	}
}

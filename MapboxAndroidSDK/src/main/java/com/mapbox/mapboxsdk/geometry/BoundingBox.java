package com.mapbox.mapboxsdk.geometry;

import android.os.Parcel;
import android.os.Parcelable;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.views.util.constants.MapViewConstants;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A rectangular geographical area defined in latitude and longitude units.
 */
public final class BoundingBox implements Parcelable, Serializable, MapViewConstants {

    static final long serialVersionUID = 2L;

    private final double mLatNorth;
    private final double mLatSouth;
    private final double mLonEast;
    private final double mLonWest;

    /**
     * Construct a new bounding box based on its corners, given in NESW
     * order.
     *
     * @param north
     * @param east
     * @param south
     * @param west
     */
    public BoundingBox(double north,
                       double east,
                       double south,
                       double west) {

        if (north == south) {
            //boundingbox full view
            north = 90;
            south = -90;
        }
        if (east == west) {
            //boundingbox full view
            east = 180;
            west = -180;
        }
        this.mLatNorth = north;
        this.mLonEast = east;
        this.mLatSouth = south;
        this.mLonWest = west;
    }

    /**
     * Create a bounding box from another bounding box
     *
     * @param other the other bounding box
     */
    public BoundingBox(BoundingBox other) {
        this.mLatNorth = other.getLatNorth();
        this.mLonEast = other.getLonEast();
        this.mLatSouth = other.getLatSouth();
        this.mLonWest = other.getLonWest();
    }

    /**
     * Create a new BoundingBox with no size centered at 0, 0, also known as null island
     */
    public BoundingBox() {
        this(0, 0, 0, 0);
    }

    /**
     * Calculates the centerpoint of this bounding box by simple interpolation and returns
     * it as a point. This is a non-geodesic calculation which is not the geographic center.
     *
     * @return LatLng center of this BoundingBox
     */
    public LatLng getCenter() {
        return new LatLng((this.mLatNorth + this.mLatSouth) / 2,
                (this.mLonEast + this.mLonWest) / 2);
    }

    public double getLatNorth() {
        return this.mLatNorth;
    }

    public double getLatSouth() {
        return this.mLatSouth;
    }

    public double getLonEast() {
        return this.mLonEast;
    }

    public double getLonWest() {
        return this.mLonWest;
    }

    /**
     * Get the absolute distance, in degrees, between the north and
     * south boundaries of this bounding box
     *
     * @return
     */
    public double getLatitudeSpan() {
        return Math.abs(this.mLatNorth - this.mLatSouth);
    }

    /**
     * Get the absolute distance, in degrees, between the west and
     * east boundaries of this bounding box
     *
     * @return
     */
    public double getLongitudeSpan() {
        return Math.abs(this.mLonEast - this.mLonWest);
    }

    @Override
    public String toString() {
        return new StringBuffer().append("N:")
                .append(this.mLatNorth).append("; E:")
                .append(this.mLonEast).append("; S:")
                .append(this.mLatSouth).append("; W:")
                .append(this.mLonWest).toString();
    }

    /**
     * Constructs a bounding box that contains all of a list of LatLng
     * objects. Empty lists will yield invalid bounding boxes.
     *
     * @param latLngs
     * @return
     */
    public static BoundingBox fromLatLngs(final ArrayList<? extends LatLng> latLngs) {
        double minLat = 90,
                minLon = 180,
                maxLat = -90,
                maxLon = -180;

        for (final LatLng gp : latLngs) {
            final double latitude = gp.getLatitude();
            final double longitude = gp.getLongitude();

            minLat = Math.min(minLat, latitude);
            minLon = Math.min(minLon, longitude);
            maxLat = Math.max(maxLat, latitude);
            maxLon = Math.max(maxLon, longitude);
        }

        if (minLon == maxLon) {
            minLon -= 0.05;
            maxLon += 0.05;
        }
        if (minLat == maxLat) {
            minLat -= 0.05;
            maxLat += 0.05;
        }
        return new BoundingBox(maxLat, maxLon, minLat, minLon);
    }

    /**
     * Determines whether this bounding box matches another one exactly.
     *
     * @param other another bounding box
     * @return a boolean indicating whether the bounding boxes are equal
     */
    public boolean equals(final BoundingBox other) {
        return mLatNorth == other.getLatNorth()
                && mLatSouth == other.getLatSouth()
                && mLonEast == other.getLonEast()
                && mLonWest == other.getLonWest();
    }

    /**
     * Determines whether this bounding box contains a point and the point
     * does not touch its boundary.
     *
     * @param pGeoPoint the point which may be contained
     * @return true, if the point is contained within the box.
     */
    public boolean contains(final ILatLng pGeoPoint) {
        final double latitude = pGeoPoint.getLatitude();
        final double longitude = pGeoPoint.getLongitude();
        return ((latitude < this.mLatNorth) && (latitude > this.mLatSouth))
                && ((longitude < this.mLonEast) && (longitude > this.mLonWest));
    }


    /**
     * Returns a new BoundingBox that stretches to contain both this and another BoundingBox.
     *
     * @param box
     * @return
     */
    public BoundingBox union(BoundingBox box) {
        return (box != null)?union(box.getLatNorth(), box.getLatSouth(), box.getLonEast(), box.getLonWest()):null;
    }

    /**
     * Returns a new BoundingBox that stretches to include another bounding box,
     * given by corner points.
     *
     * @param pLatNorth
     * @param pLatSouth
     * @param pLonEast
     * @param pLonWest
     * @return
     */
    public BoundingBox union(final double pLatNorth, final double pLatSouth,
                             final double pLonEast,
                             final double pLonWest) {
        if ((pLonWest < pLonEast) && (pLatNorth > pLatSouth)) {
            if ((this.mLonWest < this.mLonEast) && (this.mLatNorth > this.mLatSouth)) {
                return new BoundingBox((this.mLatNorth < pLatNorth) ? pLatNorth : this.mLatNorth,
                        (this.mLonEast < pLonEast) ? pLonEast : this.mLonEast,
                        (this.mLatSouth > pLatSouth) ? pLatSouth : this.mLatSouth,
                        (this.mLonWest > pLonWest) ? pLonWest : this.mLonWest);
            } else {
                return new BoundingBox(pLatNorth, pLonEast, pLatSouth, pLonWest);
            }
        } else {
            return new BoundingBox(this);
        }
    }
    
    /**
     * Returns a new BoundingBox that is the intersection of this with another box
     *
     * @param box
     * @return
     */
    public BoundingBox intersect(BoundingBox box) {
        return (box != null)?intersect(box.getLatNorth(), box.getLatSouth(), box.getLonEast(), box.getLonWest()):null;
    }
    
    /**
     * Returns a new BoundingBox that is the intersection of this with another box
     *
     * @param pLatNorth
     * @param pLatSouth
     * @param pLonEast
     * @param pLonWest
     * @return
     */
    public BoundingBox intersect(final double pLatNorth, final double pLatSouth,
                             final double pLonEast,
                             final double pLonWest) {
        if ((pLonWest < pLonEast) && (pLatNorth > pLatSouth)) {
            if ((this.mLonWest < this.mLonEast) && (this.mLatNorth > this.mLatSouth)) {
            	double maxLonWest = Math.max(this.mLonWest, pLonWest);
            	double minLonEast = Math.min(this.mLonEast, pLonEast);
            	double maxLatNorth = Math.max(this.mLatNorth, pLatNorth);
            	double minLatSouth = Math.min(this.mLatSouth, pLatSouth);
            	if (maxLonWest < minLonEast && maxLatNorth < minLatSouth) {
            		return new BoundingBox(maxLatNorth, minLonEast, minLatSouth, maxLonWest);
            	}
                return null;
            } else {
                return new BoundingBox(pLatNorth, pLonEast, pLatSouth, pLonWest);
            }
        } else {
            return new BoundingBox(this);
        }
    }

    public static final Parcelable.Creator<BoundingBox> CREATOR = new Parcelable.Creator<BoundingBox>() {
        @Override
        public BoundingBox createFromParcel(final Parcel in) {
            return readFromParcel(in);
        }

        @Override
        public BoundingBox[] newArray(final int size) {
            return new BoundingBox[size];
        }
    };

    @Override
    public int hashCode() {
        return (int) (
                (mLatNorth + 90)
                        + ((mLatSouth + 90) * 1000)
                        + ((mLonEast + 180) * 1000000)
                        + ((mLonEast + 180) * 1000000000));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel out, final int arg1) {
        out.writeDouble(this.mLatNorth);
        out.writeDouble(this.mLonEast);
        out.writeDouble(this.mLatSouth);
        out.writeDouble(this.mLonWest);
    }

    private static BoundingBox readFromParcel(final Parcel in) {
        final double latNorth = in.readDouble();
        final double lonEast = in.readDouble();
        final double latSouth = in.readDouble();
        final double lonWest = in.readDouble();
        return new BoundingBox(latNorth, lonEast, latSouth, lonWest);
    }
}

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

    private double mLatNorth;
    private double mLatSouth;
    private double mLonEast;
    private double mLonWest;

    /**
     * Construct a new bounding box based on its corners, given in NESW
     * order.
     *
     * @param north
     * @param east
     * @param south
     * @param west
     */
    public BoundingBox(final double north,
                       final double east,
                       final double south,
                       final double west) {
        this.mLatNorth = north;
        this.mLonEast = east;
        this.mLatSouth = south;
        this.mLonWest = west;
    }

    public BoundingBox() {
        this(0,0,0,0);
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

    public BoundingBox union(BoundingBox box) {
        if (box == null) return this;
        return union(box.getLatNorth(), box.getLatSouth(), box.getLonEast(), box.getLonWest());
    }

    public BoundingBox union(final double pLatNorth, final double pLatSouth,
            final double pLonEast,
            final double pLonWest) {
        if ((pLonWest < pLonEast) && (pLatNorth < pLatSouth)) {
            if ((this.mLonWest < this.mLonEast) && (this.mLatNorth < this.mLatSouth)) {
                if (this.mLonWest > pLonWest) this.mLonWest = pLonWest;
                if (this.mLatNorth < pLatNorth) this.mLatNorth = pLatNorth;
                if (this.mLonEast < pLonEast) this.mLonEast = pLonEast;
                if (this.mLatSouth > pLatSouth) this.mLatSouth = pLatSouth;
            } else {
                this.mLonWest = pLonWest;
                this.mLatNorth = pLatNorth;
                this.mLonEast = pLonEast;
                this.mLatSouth = pLatSouth;
            }
        }
        return this;
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

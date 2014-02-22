package com.mapbox.mapboxsdk.geometry;

import java.io.Serializable;
import java.util.ArrayList;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.views.util.constants.MapViewConstants;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A rectangular geographical area defined in latitude and longitude units.
 */
public final class BoundingBox implements Parcelable, Serializable, MapViewConstants {

    static final long serialVersionUID = 2L;

    protected final double mLatNorth;
    protected final double mLatSouth;
    protected final double mLonEast;
    protected final double mLonWest;

    /**
     * Construct a new bounding box based on its corners, given in NESW
     * order.
     * @param north
     * @param east
     * @param south
     * @param west
     */
    public BoundingBox(final double north, final double east, final double south,
                       final double west) {
        this.mLatNorth = north;
        this.mLonEast = east;
        this.mLatSouth = south;
        this.mLonWest = west;
    }

    /**
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
     * @return
     */
    public double getLatitudeSpan() {
        return Math.abs(this.mLatNorth - this.mLatSouth);
    }

    /**
     * Get the absolute distance, in degrees, between the west and
     * east boundaries of this bounding box
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

    public static BoundingBox fromGeoPoints(final ArrayList<? extends LatLng> partialPolyLine) {
        double minLat = Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double maxLon = Double.MIN_VALUE;
        for (final LatLng gp : partialPolyLine) {
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
     * Determine whether this bounding box contains a point and the point
     * does not touch its boundary.
     * @param pGeoPoint the point which may be contained
     * @return true, if the point is contained within the box.
     */
    public boolean contains(final ILatLng pGeoPoint) {
        final double latitude = pGeoPoint.getLatitude();
        final double longitude = pGeoPoint.getLongitude();
        return ((latitude < this.mLatNorth) && (latitude > this.mLatSouth))
                && ((longitude < this.mLonEast) && (longitude > this.mLonWest));
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

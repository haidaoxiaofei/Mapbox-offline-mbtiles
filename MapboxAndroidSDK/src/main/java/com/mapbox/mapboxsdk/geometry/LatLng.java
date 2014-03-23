package com.mapbox.mapboxsdk.geometry;

import java.io.Serializable;
import com.mapbox.mapboxsdk.api.ILatLng;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * An immutable latitude, longitude, and optionally altitude point.
 * Coordinates are stored as WGS84 degrees.
 */
public final class LatLng implements ILatLng, Parcelable, Serializable {

    private double longitude;
    private double latitude;
    private double altitude = 0f;

    /**
     * Construct a new latitude, longitude point given float arguments
     * @param aLatitude
     * @param aLongitude
     */
    public LatLng(final double aLatitude, final double aLongitude) {
        this.latitude = aLatitude;
        this.longitude = aLongitude;
    }

    /**
     * Construct a new latitude, longitude, altitude point given float arguments
     * @param aLatitude
     * @param aLongitude
     */
    public LatLng(final double aLatitude, final double aLongitude, final double aAltitude) {
        this.latitude = aLatitude;
        this.longitude = aLongitude;
        this.altitude = aAltitude;
    }

    /**
     * Transform a Location into a LatLng point
     * @param aLocation
     */
    public LatLng(final Location aLocation) {
        this(aLocation.getLatitude(), aLocation.getLongitude(), aLocation.getAltitude());
    }

    /**
     * Clone an existing latitude longitude point
     * @param aLatLng
     */
    public LatLng(final LatLng aLatLng) {
        this.latitude = aLatLng.latitude;
        this.longitude = aLatLng.longitude;
        this.altitude = aLatLng.altitude;
    }

    /**
     * Returns the longitude value of this point
     * @return the longitude value in decimal degrees
     */
    public double getLongitude() {
        return this.longitude;
    }

    /**
     * Returns the latitude value of this point
     * @return the latitude value in decimal degrees
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     * Returns the altitude value of this point. The encoding
     * of altitude is unspecified.
     *
     * @return the altitude value
     */
    public double getAltitude() {
        return this.altitude;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(this.latitude)
                .append(",")
                .append(this.longitude)
                .append(",")
                .append(this.altitude)
                .toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final LatLng rhs = (LatLng) obj;
        return rhs.latitude == this.latitude && rhs.longitude == this.longitude && rhs.altitude == this.altitude;
    }

    @Override
    public int hashCode() {
        return (int) (37.0 * (17.0 * latitude * 1E6d + longitude * 1E6d) + altitude);
    }

    /**
     * Write LatLng to parcel.
     * @param in
     */
    private LatLng(final Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.altitude = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeDouble(altitude);
    }

    public static final Parcelable.Creator<LatLng> CREATOR = new Parcelable.Creator<LatLng>() {
        @Override
        public LatLng createFromParcel(final Parcel in) {
            return new LatLng(in);
        }

        @Override
        public LatLng[] newArray(final int size) {
            return new LatLng[size];
        }
    };
}

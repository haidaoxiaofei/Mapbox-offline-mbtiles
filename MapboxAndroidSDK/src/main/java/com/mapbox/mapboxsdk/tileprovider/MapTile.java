package com.mapbox.mapboxsdk.tileprovider;

import com.mapbox.mapboxsdk.overlay.TilesOverlay;

/**
 * A map tile is distributed using the observer pattern. The tile is delivered by a tile provider
 * (i.e. a descendant of {@link com.mapbox.mapboxsdk.tileprovider.modules.MapTileModuleLayerBase} or
 * {@link MapTileLayerBase} to a consumer of tiles (e.g. descendant of
 * {@link TilesOverlay}). Tiles are typically images (e.g. png or jpeg).
 */
public class MapTile {

    public static final int MAPTILE_SUCCESS_ID = 0;
    public static final int MAPTILE_FAIL_ID = MAPTILE_SUCCESS_ID + 1;

    // This class must be immutable because it's used as the key in the cache hash map
    // (ie all the fields are final).
    private final int x;
    private final int y;
    private final int z;
    private final String path;
    private String cacheKey;
    private final int code;

    public MapTile(final int az, final int ax, final int ay) {
        this.z = az;
        this.x = ax;
        this.y = ay;

        this.path = this.cacheKey = "/" + z + "/" + x + "/" + y;
        this.code = ((17 * (37 + z)) * (37 * x)) * (37 + y);
    }

    public MapTile(final String cacheKey, final int az, final int ax, final int ay) {
        this.z = az;
        this.x = ax;
        this.y = ay;

        this.path = "/" + z + "/" + x + "/" + y;
        this.cacheKey = cacheKey + this.path;
        this.code = ((17 * (37 + z)) * (37 * x)) * (37 + y);
    }

    public int getZ() {
        return z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getCacheKey() { return cacheKey; }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MapTile)) {
            return false;
        }
        final MapTile rhs = (MapTile) obj;
        return z == rhs.z && x == rhs.x && y == rhs.y;
    }

    @Override
    public int hashCode() {
        return this.code;
    }
}

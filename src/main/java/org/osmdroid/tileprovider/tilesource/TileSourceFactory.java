package org.osmdroid.tileprovider.tilesource;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;

public class TileSourceFactory {

    // private static final Logger logger = LoggerFactory.getLogger(TileSourceFactory.class);

    /**
     * Get the tile source with the specified name.
     *
     * @param aName the tile source name
     * @return the tile source
     * @throws IllegalArgumentException if tile source not found
     */
    public static ITileSource getTileSource(final String aName) throws IllegalArgumentException {
        for (final ITileSource tileSource : mTileSources) {
            if (tileSource.name().equals(aName)) {
                return tileSource;
            }
        }
        throw new IllegalArgumentException("No such tile source: " + aName);
    }

    public static boolean containsTileSource(final String aName) {
        for (final ITileSource tileSource : mTileSources) {
            if (tileSource.name().equals(aName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the tile source at the specified position.
     *
     * @param aOrdinal
     * @return the tile source
     * @throws IllegalArgumentException if tile source not found
     */
    public static ITileSource getTileSource(final int aOrdinal) throws IllegalArgumentException {
        for (final ITileSource tileSource : mTileSources) {
            if (tileSource.ordinal() == aOrdinal) {
                return tileSource;
            }
        }
        throw new IllegalArgumentException("No tile source at position: " + aOrdinal);
    }

    public static ArrayList<ITileSource> getTileSources() {
        return mTileSources;
    }

    public static void addTileSource(final ITileSource mTileSource) {
        mTileSources.add(mTileSource);
    }

    public static final OnlineTileSourceBase MAPNIK = new XYTileSource("Mapnik",
            ResourceProxy.string.mapnik, 0, 18, 256, ".png", "http://tile.openstreetmap.org/");


    public static final OnlineTileSourceBase DEFAULT_TILE_SOURCE = MAPNIK;

    private static ArrayList<ITileSource> mTileSources;

    static {
        mTileSources = new ArrayList<ITileSource>();
        mTileSources.add(MAPNIK);
    }
}

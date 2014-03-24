package com.mapbox.mapboxsdk.tileprovider.modules;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

public class MBTilesFileArchive implements IArchiveFile {

    private final SQLiteDatabase mDatabase;

    //	TABLE tiles (zoom_level INTEGER, tile_column INTEGER, tile_row INTEGER, tile_data BLOB);
    public final static String TABLE_TILES = "tiles";
    public final static String COL_TILES_ZOOM_LEVEL = "zoom_level";
    public final static String COL_TILES_TILE_COLUMN = "tile_column";
    public final static String COL_TILES_TILE_ROW = "tile_row";
    public final static String COL_TILES_TILE_DATA = "tile_data";

    private MBTilesFileArchive(final SQLiteDatabase pDatabase) {
        mDatabase = pDatabase;
    }

    public static MBTilesFileArchive getDatabaseFileArchive(final File pFile) throws SQLiteException {
        return new MBTilesFileArchive(
                SQLiteDatabase.openDatabase(
                        pFile.getAbsolutePath(),
                        null,
                        SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY));
    }

    @Override
    public InputStream getInputStream(final ITileLayer pTileSource, final MapTile pTile) {

        try {
            InputStream ret = null;
            final String[] tile = {COL_TILES_TILE_DATA};
            final String[] xyz = {
                    Integer.toString(pTile.getX())
                    , Double.toString(Math.pow(2, pTile.getZ()) - pTile.getY() - 1)
                    , Integer.toString(pTile.getZ())
            };

            final Cursor cur = mDatabase.query(TABLE_TILES, tile, "tile_column=? and tile_row=? and zoom_level=?", xyz, null, null, null);

            if (cur.getCount() != 0) {
                cur.moveToFirst();
                ret = new ByteArrayInputStream(cur.getBlob(0));
            }
            cur.close();
            if (ret != null) {
                return ret;
            }
        } catch (final Throwable e) {
            Log.w(TAG, "Error getting db stream: " + pTile, e);
        }

        return null;
    }

    @Override
    public String toString() {
        return "MBTiles [mDatabase=" + mDatabase.getPath() + "]";
    }

    public int getMinZoomLevel() {
        Cursor cursor = mDatabase.rawQuery("SELECT MIN(zoom_level) FROM tiles", null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public int getMaxZoomLevel() {
        Cursor cursor = mDatabase.rawQuery("SELECT MAX(zoom_level) FROM tiles", null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public String getName() {
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM metadata WHERE name = 'name'", null);
        cursor.moveToFirst();
        return cursor.getString(1);
    }

    public String getType() {
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM metadata WHERE name = 'template'", null);
        cursor.moveToFirst();
        return cursor.getString(1);
    }

    public String getVersion() {
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM metadata WHERE name = 'version'", null);
        cursor.moveToFirst();
        return cursor.getString(1);
    }

    public String getDescription() {
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM metadata WHERE name = 'description'", null);
        cursor.moveToFirst();
        return cursor.getString(1);
    }

    public String getAttribution() {
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM metadata WHERE name = 'attribution'", null);
        cursor.moveToFirst();
        return cursor.getString(1);
    }

    public BoundingBox getBounds() {
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM metadata WHERE name = 'bounds'", null);
        cursor.moveToFirst();
        String boundsString = cursor.getString(1);
        String[] boundsArray = boundsString.split(",");
        return new BoundingBox(Double.parseDouble(boundsArray[0]),
                Double.parseDouble(boundsArray[1]),
                Double.parseDouble(boundsArray[2]),
                Double.parseDouble(boundsArray[3]));
    }


    public void close() {
        if (mDatabase != null) {
            mDatabase.close();
        }
    }
    private static final String TAG = "MBTilesFileArchive";

}

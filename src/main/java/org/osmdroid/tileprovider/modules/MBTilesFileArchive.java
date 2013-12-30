package org.osmdroid.tileprovider.modules;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

public class MBTilesFileArchive implements IArchiveFile {

    private static final Logger logger = LoggerFactory.getLogger(MBTilesFileArchive.class);

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
    public InputStream getInputStream(final ITileSource pTileSource, final MapTile pTile) {
        try {
            InputStream ret = null;
            final String[] tile = {COL_TILES_TILE_DATA};
            final String[] xyz = {
                    Integer.toString(pTile.getX())
                    , Double.toString(Math.pow(2, pTile.getZoomLevel()) - pTile.getY() - 1)  // Use Google Tiling Spec
                    , Integer.toString(pTile.getZoomLevel())
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
            logger.warn("Error getting db stream: " + pTile, e);
        }

        return null;
    }

    @Override
    public String toString() {
        return "DatabaseFileArchive [mDatabase=" + mDatabase.getPath() + "]";
    }
    public int getMinZoomLevel(){
        Cursor cursor = mDatabase.rawQuery("SELECT MIN(zoom_level) FROM tiles", null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }
    public int getMaxZoomLevel(){
        Cursor cursor = mDatabase.rawQuery("SELECT MAX(zoom_level) FROM tiles", null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public String getName(){
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM metadata ORDER BY ROWID ASC LIMIT 1", null);
        cursor.moveToFirst();
        return cursor.getString(0);
    }
    public String getType(){
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM metadata ORDER BY ROWID ASC LIMIT 1", null);
        cursor.moveToFirst();
        return cursor.getString(1);
    }
    public String getVersion(){
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM metadata ORDER BY ROWID ASC LIMIT 1", null);
        cursor.moveToFirst();
        return cursor.getString(2);
    }
    public String getDescription(){
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM metadata ORDER BY ROWID ASC LIMIT 1", null);
        cursor.moveToFirst();
        return cursor.getString(3);
    }
    public String getFormat(){
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM metadata ORDER BY ROWID ASC LIMIT 1", null);
        cursor.moveToFirst();
        return cursor.getString(4);
    }

}

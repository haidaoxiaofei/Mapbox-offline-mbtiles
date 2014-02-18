package com.mapbox.mapboxsdk.tileprovider.modules;

import java.io.File;

import android.util.Log;

import android.database.sqlite.SQLiteException;

public class ArchiveFileFactory {

    /**
     * Return an implementation of {@link IArchiveFile} for the specified file.
     *
     * @return an implementation, or null if there's no suitable implementation
     */
    public static IArchiveFile getArchiveFile(final File pFile) {

        if (pFile.getName().endsWith(".mbtiles")) {
            try {
                return MBTilesFileArchive.getDatabaseFileArchive(pFile);
            } catch (final SQLiteException e) {
                Log.e(TAG, "Error opening MBTiles SQLite file", e);
            }
        }

        return null;
    }

    private static final String TAG = "ArchiveFileFactory";

}

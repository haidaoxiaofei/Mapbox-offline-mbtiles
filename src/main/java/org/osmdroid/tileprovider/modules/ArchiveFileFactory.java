package org.osmdroid.tileprovider.modules;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.database.sqlite.SQLiteException;

public class ArchiveFileFactory {

    private static final Logger logger = LoggerFactory.getLogger(ArchiveFileFactory.class);

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
                logger.error("Error opening MBTiles SQLite file", e);
            }
        }

        return null;
    }

}

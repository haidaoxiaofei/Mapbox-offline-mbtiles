package com.mapbox.mapboxsdk.offline;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.geometry.BoundingBox;

public class OfflineMapDatabase implements MapboxConstants {

    private static final String TAG = "OfflineMapDatabase";

    private Context context;

    private String uniqueID;
    private String mapID;
    private boolean includesMetadata;
    private boolean includesMarkers;
    private RasterImageQuality imageQuality;
    private BoundingBox mapRegion;
    private Integer minimumZ;
    private Integer maximumZ;
    private String path;
    private boolean invalid;
    private boolean initializedProperly;

    /**
     * Default Constructor
     * @param context Context of Android app
     */
    public OfflineMapDatabase(Context context) {
        super();
        this.context = context;
    }

    public String sqliteMetadataForName (String name)
    {
        String query = "SELECT " + OfflineDatabaseHandler.FIELD_METADATA_VALUE + " FROM " + OfflineDatabaseHandler.TABLE_METADATA +  " WHERE " + OfflineDatabaseHandler.FIELD_METADATA_NAME + "='" + name + "';";
        SQLiteDatabase db = OfflineDatabaseHandler.getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        return cursor.getString(cursor.getColumnIndex(OfflineDatabaseHandler.FIELD_METADATA_VALUE));
    }


    public byte[] sqliteDataForURL(String url)
    {
        SQLiteDatabase db = OfflineDatabaseHandler.getInstance(context).getReadableDatabase();
        String query = "SELECT " + OfflineDatabaseHandler.FIELD_DATA_VALUE + " FROM " + OfflineDatabaseHandler.TABLE_DATA +  " WHERE " + OfflineDatabaseHandler.FIELD_DATA_ID + "= (SELECT " + OfflineDatabaseHandler.FIELD_RESOURCES_ID + " from " + OfflineDatabaseHandler.TABLE_RESOURCES + " where " + OfflineDatabaseHandler.FIELD_RESOURCES_URL + " = '" + url + "');";
        Cursor cursor = db.rawQuery(query, null);
        return cursor.getBlob(cursor.getColumnIndex(OfflineDatabaseHandler.FIELD_DATA_VALUE));
    }
}

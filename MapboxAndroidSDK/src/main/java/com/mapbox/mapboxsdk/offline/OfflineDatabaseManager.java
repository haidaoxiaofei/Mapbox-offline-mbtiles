package com.mapbox.mapboxsdk.offline;

import android.content.Context;

import java.util.Hashtable;

public class OfflineDatabaseManager {

    private static OfflineDatabaseManager offlineDatabaseManager = null;

    private Hashtable<String, OfflineDatabaseHandler> databaseHandlers = null;

    private static Context context = null;

    private OfflineDatabaseManager() {
        super();
        databaseHandlers = new Hashtable<String, OfflineDatabaseHandler>();
    }

    public static OfflineDatabaseManager getOfflineDatabaseManager(Context ctx) {
        if (offlineDatabaseManager == null) {
            offlineDatabaseManager = new OfflineDatabaseManager();
        }
        context = ctx;
        return offlineDatabaseManager;
    }

    public OfflineDatabaseHandler getOfflineDatabaseHandlerForMapId(String mapId) {
        if (databaseHandlers.containsKey(mapId)) {
            return databaseHandlers.get(mapId);
        }

        OfflineDatabaseHandler dbh = new OfflineDatabaseHandler(context, mapId.toLowerCase() + "-PARTIAL");
        databaseHandlers.put(mapId.toLowerCase(), dbh);
        return dbh;
    }
}

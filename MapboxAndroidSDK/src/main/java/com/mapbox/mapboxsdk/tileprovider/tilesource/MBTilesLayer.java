package com.mapbox.mapboxsdk.tileprovider.tilesource;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.modules.MBTilesFileArchive;
import com.mapbox.mapboxsdk.tileprovider.modules.MapTileDownloader;
import com.mapbox.mapboxsdk.views.util.constants.MapViewConstants;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MBTilesLayer extends TileLayer implements MapViewConstants,
        MapboxConstants {
    private static final String TAG = "MBTilesLayer";
    MBTilesFileArchive mbTilesFileArchive;
    private Resources mResources;
    /**
     * Initialize a new tile layer, represented by a MBTiles file.
     *
     * @param url
     *            path to a MBTiles file
     */
    public MBTilesLayer(final String url) {
        super(url);
        initialize(url, null);
    }

    public MBTilesLayer(final Context context, final String assetUrl) {
        super(assetUrl);
        initialize(assetUrl, context);
    }

    private static File createFileFromInputStream(InputStream inputStream, String URL) {
        try {
            File f = new File(URL);
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while((length=inputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        } catch (IOException e) {
        }
        return null;
    }

    private void initialize(String url, final Context context) {
        mResources = context.getResources();

        File file = null;
        if (context != null) {
            //we assume asset here
            AssetManager am = context.getAssets();
            InputStream inputStream;
            try{
                inputStream = am.open(url);
                file = createFileFromInputStream(inputStream, Environment.getExternalStorageDirectory() + File.separator + url);
            }
            catch (IOException e) {
                Log.e(TAG, "MBTiles file not found in assets: " + e.toString());
            }
        }
        if (file == null) {
            try {
                file = new File(url);
            }
            catch (Exception e){
                Log.e(TAG, "can't load MBTiles: " + e.toString());
            }
        }
        if (file != null) {
            mbTilesFileArchive = MBTilesFileArchive.getDatabaseFileArchive(file);
        }

        if (mbTilesFileArchive != null) {
            mMaximumZoomLevel = mbTilesFileArchive.getMaxZoomLevel();
            mMinimumZoomLevel = mbTilesFileArchive.getMinZoomLevel();
            mName = mbTilesFileArchive.getName();
            mDescription = mbTilesFileArchive.getDescription();
            mShortAttribution = mbTilesFileArchive.getAttribution();
            mBoundingBox = mbTilesFileArchive.getBounds();
            mCenter = mbTilesFileArchive.getCenter();
        }
    }

    @Override
    public void detach()
    {
        if (mbTilesFileArchive != null) {
            mbTilesFileArchive.close();
            mbTilesFileArchive = null;
        }
    }

    @Override
    public Drawable getDrawableFromTile(final MapTileDownloader downloader, final MapTile aTile, boolean hdpi) {
        if (mbTilesFileArchive != null) {
            InputStream stream = mbTilesFileArchive.getInputStream(this, aTile);
            if (stream != null) {
                //TODO: needs to be change CacheableBitmapDrawable
                return new BitmapDrawable(mResources, stream);
            }

        }
        return null;
    }
}

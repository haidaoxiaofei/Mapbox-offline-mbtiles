package com.mapbox.mapboxsdk;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Environment;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import java.io.*;

public class MapViewFactory {
    /**
     * Generates a new MapView from the filename of an MBTiles file stored in assets.
     * @param context the app context
     * @param URL the file name within assets
     * @return the MapView
     */
    public static MapView fromMBTiles(Activity context, String URL){
        DefaultResourceProxyImpl mResourceProxy = new DefaultResourceProxyImpl(context);
        SimpleRegisterReceiver simpleReceiver = new SimpleRegisterReceiver(context);
        AssetManager am = context.getAssets();
        InputStream inputStream;
        try{
            inputStream = am.open(URL);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("MBTiles file not found in assets");
        }
        if(inputStream==null){
            throw new IllegalArgumentException("InputStream is null");
        }
        File file = createFileFromInputStream(inputStream, Environment.getExternalStorageDirectory() + File.separator + URL);
        if(file==null){
            throw new IllegalArgumentException("File is null");
        }
        MBTilesFileArchive mbTilesFileArchive = MBTilesFileArchive.getDatabaseFileArchive(file);
        IArchiveFile[] files = {mbTilesFileArchive};
        XYTileSource MBTILESRENDER = new XYTileSource(
                URL,
                ResourceProxy.string.offline_mode,
                mbTilesFileArchive.getMinZoomLevel(),mbTilesFileArchive.getMaxZoomLevel(),
                256, ".png", "https://laksjdflkjasdf.com/");
        MapTileModuleProviderBase moduleProvider = new MapTileFileArchiveProvider(simpleReceiver, MBTILESRENDER, files);
        MapTileProviderArray mProvider = new MapTileProviderArray(MBTILESRENDER, null,
                new MapTileModuleProviderBase[]{moduleProvider}
        );
        return new MapView(context, 256, mResourceProxy, mProvider);
    }
    private static File createFileFromInputStream(InputStream inputStream, String URL) {
        try{
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
        }
        catch (IOException e) {
        }
        return null;
    }
}

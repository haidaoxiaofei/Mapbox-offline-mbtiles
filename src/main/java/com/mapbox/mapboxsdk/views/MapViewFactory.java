package com.mapbox.mapboxsdk.views;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Environment;
import com.mapbox.mapboxsdk.DefaultResourceProxyImpl;
import com.mapbox.mapboxsdk.ResourceProxy;
import com.mapbox.mapboxsdk.tileprovider.MapTileProviderArray;
import com.mapbox.mapboxsdk.tileprovider.modules.IArchiveFile;
import com.mapbox.mapboxsdk.tileprovider.modules.MBTilesFileArchive;
import com.mapbox.mapboxsdk.tileprovider.modules.MapTileModuleProviderBase;
import com.mapbox.mapboxsdk.tileprovider.tilesource.XYTileSource;
import com.mapbox.mapboxsdk.tileprovider.util.SimpleRegisterReceiver;

import java.io.*;

public class MapViewFactory {
    private static File createFileFromInputStream(InputStream inputStream, String URL) {
        try {
            File f = new File(URL);
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        } catch (IOException e) {
        }
        return null;
    }
}

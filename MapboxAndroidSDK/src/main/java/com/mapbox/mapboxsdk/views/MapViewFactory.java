package com.mapbox.mapboxsdk.views;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

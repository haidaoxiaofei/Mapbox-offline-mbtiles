package com.mapbox.mapboxsdk.overlay;

import com.google.common.base.Strings;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.format.GeoJSON;
import android.os.AsyncTask;
import android.util.Log;
import com.squareup.okhttp.OkHttpClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class GeoJSONLayer {

    private MapView mapView;

    public GeoJSONLayer(final MapView mapView) {
        this.mapView = mapView;
    }

    public void loadURL(final String url) {
		if (Strings.isNullOrEmpty(url)) {
			Log.w(TAG, "url is Null or empty");
			return;
		}
        new Getter().execute(url);
    }

    /**
     * Class that generates markers from formats such as GeoJSON
     */
    private class Getter extends AsyncTask<String, Void, ArrayList<Object>> {
        @Override
        protected ArrayList<Object> doInBackground(String... params) {
            InputStream is;
            String jsonText = null;
            ArrayList<Object> uiObjects = new ArrayList<Object>();
			try {
				OkHttpClient okHttpClient = new OkHttpClient();
				URL.setURLStreamHandlerFactory(okHttpClient);
                is = new URL(params[0]).openStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                jsonText = readAll(rd);

				uiObjects.addAll(GeoJSON.parseString(jsonText, mapView));
            } catch (Exception e) {
                Log.e(TAG, "Error loading / parsing GeoJSON: " + e.toString());
				e.printStackTrace();
            }
            return uiObjects;
        }

		@Override
		protected void onPostExecute(ArrayList<Object> objects)
		{
			// Back on the Main Thread so add new UI Objects and refresh map
			for (Object obj: objects)
			{
				if (obj instanceof Marker)
				{
					mapView.addMarker((Marker)obj);
				}
				else if (obj instanceof PathOverlay)
				{
					mapView.getOverlays().add((PathOverlay)obj);
				}
			}
			if (objects.size() > 0)
			{
				mapView.invalidate();
			}
		}

		private String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }
    }

    static final String TAG = "GeoJSONLayer";
}

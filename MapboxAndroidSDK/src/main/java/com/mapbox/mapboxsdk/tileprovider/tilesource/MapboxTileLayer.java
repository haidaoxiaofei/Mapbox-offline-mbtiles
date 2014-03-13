package com.mapbox.mapboxsdk.tileprovider.tilesource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.views.util.constants.MapViewConstants;
import com.squareup.okhttp.OkHttpClient;

/**
 * A convenience class to initialize tile layers that use Mapbox.
 */
public class MapboxTileLayer extends TileLayer implements MapViewConstants,
		MapboxConstants {
	private static final String TAG = "MapboxTileLayer";
	private JSONObject infoJSON;

	/**
	 * Initialize a new tile layer, directed at a hosted Mapbox tilesource.
	 * 
	 * @param id
	 *            a valid mapid, of the form account.map
	 */
	public MapboxTileLayer(String id) {
		super(id);
		initialize(id, false);
	}

	public MapboxTileLayer(String id, boolean enableSSL) {
		super(id);
		initialize(id, enableSSL);
	}

	private void initialize(String id, boolean enableSSL) {
		if (!id.contains("http://") && !id.contains("https://")
				&& id.contains("")) {
			this.setURL(MAPBOX_BASE_URL + id + "/{z}/{x}/{y}{2x}.png");
		}
		Log.d(TAG, "initialize " + id);
		String jsonURL = getReferenceURL(id, enableSSL);
		if (jsonURL != null) {
			initWithTileJSON(getBrandedJSON(jsonURL));
		}
	}

	private void initWithTileJSON(JSONObject tileJSON) {
		infoJSON = (tileJSON != null) ? tileJSON : new JSONObject();
		if (infoJSON != null) {
			if (infoJSON.has("tiles")) {
				try {
					this.setURL(infoJSON.getJSONArray("tiles").getString(0).replace(".png", "{2x}.png"));
				} catch (JSONException e) {
				}
			}
			if (infoJSON.has("minzoom")) {
				try {
					mMinimumZoomLevel = infoJSON.getInt("minzoom");
				} catch (JSONException e) {
				}
			}
			if (infoJSON.has("maxzoom")) {
				try {
					mMaximumZoomLevel = infoJSON.getInt("maxzoom");
				} catch (JSONException e) {
				}
			}
			if (infoJSON.has("bounds")) {
				try {
					boolean valid = false;
					double[] bounds = new double[4];
					Object value = infoJSON.get("bounds");
					if (value instanceof JSONArray) {
						JSONArray array = ((JSONArray)value);
						if (array.length() == 4) {
							for (int i = 0; i < array.length(); i++) {
								bounds[i] = array.getDouble(i);
							}
							valid = true;
						}
					}
					else {
						String[] array = infoJSON.getString("bounds").split(",");
						if (array.length == 4) {
							for (int i = 0; i < array.length; i++) {
								bounds[i] = Double.parseDouble(array[i]);
							}
							valid = true;
						}
					}
					if (valid) {
						mBoundingBox = new BoundingBox(bounds[3], bounds[2], bounds[1], bounds[0]);
					}
				} catch (JSONException e) {
				}
			}
		}
		Log.d(TAG, "infoJSON " + infoJSON.toString());
	}

	byte[] readFully(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		for (int count; (count = in.read(buffer)) != -1;) {
			out.write(buffer, 0, count);
		}
		return out.toByteArray();
	}

	class RetreiveJSONTask extends AsyncTask<String, Void, JSONObject> {

		protected JSONObject doInBackground(String... urls) {
			OkHttpClient client = new OkHttpClient();
			client.setResponseCache(null);
			InputStream in = null;
			try {
				URL url = new URL(urls[0]);
				HttpURLConnection connection = client.open(url);
				in = connection.getInputStream();
				byte[] response = readFully(in);
				String result = new String(response, "UTF-8");
				return new JSONObject(result);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	private JSONObject getBrandedJSON(String url) {
		try {
			return new RetreiveJSONTask().execute(url).get(10000,
					TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private String getReferenceURL(String id, boolean enableSSL) {
		return String.format("http%s://api.tiles.mapbox.com/v3/%s.json%s",
				(enableSSL ? "s" : ""), id, (enableSSL ? "?secure" : ""));
	}
}
package com.cortxt.app.mmcutility.Reporters.WebReporter;

import com.cortxt.app.mmcutility.Utils.MMCLogger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

public class StatsRequest extends HttpGet {
	public static final String TAG = StatsRequest.class.getSimpleName();
	private static final String END_POINT = "/api/stats";
	
	private static final String KEY_NETWORKS = "networks[]";
	private static final String KEY_CENTER = "center[]";
	private static final String KEY_RADIUS = "radius";
	
	/**
	 * Constructs a StatsRequest object with the given parameters
	 * @param host
	 * @param apiKey
	 * @param startTime
	 * @param endTime
	 * @param latitude
	 * @param longitude
	 * @param radius (in meters)
	 * @param networkIds
	 */
	public StatsRequest(String host, String apiKey, long startTime, long endTime,
			double latitude, double longitude, float radius, String... networkIds) {
		LinkedList<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair(WebReporter.JSON_API_KEY, apiKey));
		
		if (networkIds != null)
			for(String networkId : networkIds) {
				params.add(new BasicNameValuePair(KEY_NETWORKS, networkId));
			}
		
		if(latitude != Double.MAX_VALUE && longitude != Double.MAX_VALUE && radius != Float.MAX_VALUE) {
			params.add(new BasicNameValuePair(KEY_CENTER, Double.toString(latitude)));
			params.add(new BasicNameValuePair(KEY_CENTER, Double.toString(longitude)));
			params.add(new BasicNameValuePair(KEY_RADIUS, Float.toString(radius)));
		}
		
		String paramsString = URLEncodedUtils.format(params, "utf-8");
		
		try {
			setURI(new URI(host + END_POINT + "?" + paramsString));
		} catch (URISyntaxException e) {
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "constructor", "invalid uri", e);
			throw new RuntimeException(e);
		}
	}
}

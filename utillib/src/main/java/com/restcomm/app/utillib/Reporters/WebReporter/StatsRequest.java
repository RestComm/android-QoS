package com.restcomm.app.utillib.Reporters.WebReporter;

import android.util.Pair;

import com.restcomm.app.utillib.Utils.LoggerUtil;

import java.net.URL;
import java.util.LinkedList;


public class StatsRequest  {
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
	//public StatsRequest(String host, String apiKey, long startTime, long endTime,
	//		double latitude, double longitude, float radius, String... networkIds) {

		public static URL getURL (String host, String apiKey, long startTime, long endTime,
								  		double latitude, double longitude, float radius, String... networkIds) {

		LinkedList<Pair> params = new LinkedList<Pair>();
		params.add(new Pair(WebReporter.JSON_API_KEY, apiKey));

		if (networkIds != null)
			for(String networkId : networkIds) {
				params.add(new Pair(KEY_NETWORKS, networkId));
			}
		
		if(latitude != Double.MAX_VALUE && longitude != Double.MAX_VALUE && radius != Float.MAX_VALUE) {
			params.add(new Pair(KEY_CENTER, Double.toString(latitude)));
			params.add(new Pair(KEY_CENTER, Double.toString(longitude)));
			params.add(new Pair(KEY_RADIUS, Float.toString(radius)));
		}

		String paramsString = WebReporter.URLEncodedFormat(params);

		try {
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "StatsRequest ", paramsString);
			return new URL(host + END_POINT + "?" + paramsString);

		} catch (Exception e) {
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "StatsRequest", "exception", e);
			throw new RuntimeException(e);
		}

	}
}

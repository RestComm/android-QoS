package com.cortxt.app.utillib.Reporters.WebReporter;

import android.util.Pair;

import com.cortxt.app.utillib.Utils.LoggerUtil;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class RequestServerGet {
	public static final String TAG = StatsRequest.class.getSimpleName();
	private static  String END_POINT = "/api/";
	
	/**
	 * Constructs a StatsRequest object with the given parameters\
	 */
	public static URL getURL (String host, String apiKey, String email, String type, HashMap<String, String> query)
	{
		END_POINT = "/api/user";

		LinkedList<Pair> params = new LinkedList<Pair>();
		params.add(new Pair(WebReporter.JSON_API_KEY, apiKey));
		if (email != null) {
			params.add(new Pair("emails[]", email));
			if (query != null) {
				// add each name value pair in the query hash map
				for (Map.Entry<String, String> entry : query.entrySet()) {
					params.add(new Pair(entry.getKey(), entry.getValue()));
				}
			}
		}
		if (query == null && email == null)
			params.add(new Pair("chkauth", "1"));

		String paramsString = WebReporter.URLEncodedFormat(params);
		
		try {
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "RequestServerGet " + type, paramsString);
			return new URL(host + END_POINT + "?" + paramsString);

		} catch (Exception e) {
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "RequestServerGet", "exception", e);
			throw new RuntimeException(e);
		}
	}
}

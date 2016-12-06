package com.restcomm.app.utillib.Reporters.WebReporter;

import android.util.Pair;

import com.restcomm.app.utillib.Utils.LoggerUtil;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class NetworkRequest {
	public static final String TAG = NetworkRequest.class.getSimpleName();
	private static final String END_POINT = "/api/operator";
	
	//private static final String KEY_MCCS = "mccs[]";
	//private static final String KEY_MNCS = "mncs[]";
	//private static final String KEY_CARRIERS = "carriers[]";
	//private static final String KEY_SIDS = "sids[]";
	
	private static final String KEY_MCC = "mcc";
	private static final String KEY_MNC = "mnc";
	private static final String KEY_CARRIER = "carrier";
	private static final String KEY_SID = "sid";
	
	public static URL getURL(String host, String apiKey, HashMap<String, String> carrierProperties) {
		
		
		LinkedList<Pair> params = new LinkedList<Pair>();
		params.add(new Pair(WebReporter.JSON_API_KEY, apiKey));
		if (carrierProperties != null)
		{
			// add each name value pair in the query hash map
			for(Map.Entry<String, String> entry : carrierProperties.entrySet()) {
				params.add(new Pair(entry.getKey(), entry.getValue()));
			}
		}
		String paramsString = WebReporter.URLEncodedFormat(params);
		try {
			//URI requestUri = new URI(host + END_POINT + "?" + paramsString);
			//setURI(requestUri);
			return new URL(host + END_POINT + "?" + paramsString);
		}
		catch (Exception e) {
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "constructor", "invalid uri", e);
			throw new RuntimeException(e); 
		}
	}
	
	public static URL getURL(String host, String apiKey, String mcc) {
		LinkedList<Pair> params = new LinkedList<Pair>();
		params.add(new Pair(WebReporter.JSON_API_KEY, apiKey));
		params.add(new Pair("mcc", mcc));
		params.add(new Pair("limit", "5"));

		String paramsString = WebReporter.URLEncodedFormat(params);
		
		try {
			return new URL(host + END_POINT + "?" + paramsString);
		}
		catch (Exception e) {
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "constructor", "invalid uri", e);
			throw new RuntimeException(e);
		}
	}
}

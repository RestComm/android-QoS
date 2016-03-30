package com.cortxt.app.mmcutility.Reporters.WebReporter;

import android.util.Pair;

import com.cortxt.app.mmcutility.Utils.MMCLogger;

import java.net.URL;
import java.util.LinkedList;

public class AreaStatsRequest
{
	public static final String TAG = AreaStatsRequest.class.getSimpleName();
	
	public static URL getURL(String path, String apiKey, double latitude, double longitude, int radius, int months, String ops)
	{
		LinkedList<Pair> params = new LinkedList<Pair>();
		
		params.add(new Pair(WebReporter.JSON_API_KEY, apiKey));
		
		params.add(new Pair("lat", Double.toString(latitude)));
		params.add(new Pair("lng", Double.toString(longitude)));
		params.add(new Pair("radius", Integer.toString(radius)));
		params.add(new Pair("months", Integer.toString(months)));
		params.add(new Pair("criteria", "operators"));
		params.add(new Pair("values", ops));

		String paramsString = WebReporter.URLEncodedFormat(params);

		try 
		{
			return new URL(path + "?" + paramsString);
		} 
		catch (Exception e)
		{
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "constructor", "invalid uri", e);
			throw new RuntimeException(e);
		}
	}	
	
}

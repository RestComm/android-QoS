package com.cortxt.app.mmcutility.Reporters.WebReporter;

import android.util.Pair;

import com.cortxt.app.mmcutility.DataObjects.MMCDevice;
import com.cortxt.app.mmcutility.Utils.MMCLogger;

import java.net.URL;
import java.util.LinkedList;

public class DidYouKnowRequest  {

	public static final String TAG = TopOperatorsRequest.class.getSimpleName();
	
	public static URL getURL (String path, String apiKey, String opid)
	{
		LinkedList<Pair> params = new LinkedList<Pair>();
		
		params.add(new Pair(WebReporter.JSON_API_KEY, apiKey));
		params.add(new Pair("opid", opid));
		params.add(new Pair("ms", Long.toString(System.currentTimeMillis())));
		params.add(new Pair("version", "2"));
		//params.add(new BasicNameValuePair("tag", "samples"));
		params.add(new Pair("lang", MMCDevice.getLanguageCode()));
		String paramsString = WebReporter.URLEncodedFormat(params);
		
		try 
		{
			return (new URL(path + "?" + paramsString));
		} 
		catch (Exception e)
		{
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "constructor", "invalid uri", e);
			throw new RuntimeException(e);
		}
	}
}

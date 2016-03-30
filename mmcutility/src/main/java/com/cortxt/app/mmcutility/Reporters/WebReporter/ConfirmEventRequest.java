package com.cortxt.app.mmcutility.Reporters.WebReporter;

import android.util.Pair;

import com.cortxt.app.mmcutility.Utils.MMCLogger;

import java.net.URL;
import java.util.LinkedList;

public class ConfirmEventRequest
{

	public static final String TAG = TopOperatorsRequest.class.getSimpleName();
	
	public static URL getURL (String path, String apiKey, long ltime, int evttype, int newtype, int rating, int userid)
	{
		LinkedList<Pair> params = new LinkedList<Pair>();
		
		params.add(new Pair(WebReporter.JSON_API_KEY, apiKey));
		params.add(new Pair("evttype", Integer.toString(evttype)));
		params.add(new Pair("newtype", Integer.toString(newtype)));
		params.add(new Pair("rating", Integer.toString(rating)));
		params.add(new Pair("userid", Integer.toString(userid)));
		params.add(new Pair("ltime", Long.toString(ltime)));
		
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

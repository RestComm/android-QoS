package com.cortxt.app.mmcutility.Reporters.WebReporter;

import com.cortxt.app.mmcutility.DataObjects.MMCGSMDevice;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;


public class ServerUpdateRequest {
	private static final String TAG = ServerUpdateRequest.class.getSimpleName();
	private static final String END_POINT = "/api/";
	public static final String DEVICE = "devices";
	public static final String USER = "user";
	
	public static final String KEY_SHARE_WITH_CARRIER= "share";
	public static final String KEY_EMAIL_SETTING = "email";
	public static final String KEY_TWITTER_SETTING = "twitter";
    public static final String KEY_GCM_REG_ID = "gcmid";
	protected JSONObject mBody;
    protected String mHost;

	public static HttpURLConnection PUTSettingChangeRequest(String host, String type, String apiKey, String key, Object value, HashMap<String, String> carrier) throws Exception
	{
		JSONObject body = renderSettingChangeRequest (apiKey, key, value);
		URL url = new URL(host + END_POINT + type);
		return putRequest (url, body);
	}

	public static HttpURLConnection PUTSimChangeRequest(String host, String type, String apiKey, MMCGSMDevice device) throws Exception
	{
		JSONObject body = renderSimChangeRequest(apiKey, device);
		URL url = new URL(host + END_POINT + type);
		return putRequest (url, body);
	}

    private static HttpURLConnection putRequest (URL url, JSONObject body) throws Exception
	{
		String message = body.toString();
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000);
		conn.setConnectTimeout(15000);
		conn.setRequestMethod("PUT");
		conn.setDoInput(true);
		conn.setDoOutput(true);

		conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

		//open
		conn.connect();
		OutputStream os = new BufferedOutputStream(conn.getOutputStream());
		os.write(message.getBytes());
		//clean up
		os.flush();
		return conn;
	}

	public static JSONObject renderSimChangeRequest(String apiKey, MMCGSMDevice device) throws JSONException {
		JSONObject body = new JSONObject();
		body.put(WebReporter.JSON_API_KEY, apiKey);

		body.put(MMCGSMDevice.KEY_IMSI, device.getIMSI());
		body.put(MMCGSMDevice.KEY_PHONE_NUMBER, device.getPhoneNumber());
		return body;
	}
	
	// Update a setting on my user, such as email or twitter name
	public static JSONObject renderSettingChangeRequest(String apiKey, String key, Object value) throws JSONException {
		JSONObject body = new JSONObject();
		body.put(WebReporter.JSON_API_KEY, apiKey);
		body.put(key, value);  // The actual key value to be updated
		return body;
	}

	private static String addId (String objectid)
	{
		if (objectid != null && objectid.length() > 1)
			return "/" + objectid;
		return "";
	}

}

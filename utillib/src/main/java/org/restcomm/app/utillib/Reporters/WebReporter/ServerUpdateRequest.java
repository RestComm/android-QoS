/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * For questions related to commercial use licensing, please contact sales@telestax.com.
 *
 */

package org.restcomm.app.utillib.Reporters.WebReporter;

import org.restcomm.app.utillib.DataObjects.GSMDevice;

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
	public static final String KEY_SETTINGS_SETTING = "settings";
	protected JSONObject mBody;
    protected String mHost;

	public static HttpURLConnection PUTSettingChangeRequest(String host, String type, String apiKey, String key, Object value, HashMap<String, String> carrier) throws Exception
	{
		JSONObject body = renderSettingChangeRequest (apiKey, key, value);
		URL url = new URL(host + END_POINT + type);
		return putRequest (url, body);
	}

	public static HttpURLConnection PUTSimChangeRequest(String host, String type, String apiKey, GSMDevice device) throws Exception
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

	public static JSONObject renderSimChangeRequest(String apiKey, GSMDevice device) throws JSONException {
		JSONObject body = new JSONObject();
		body.put(WebReporter.JSON_API_KEY, apiKey);

		body.put(GSMDevice.KEY_IMSI, device.getIMSI());
		body.put(GSMDevice.KEY_PHONE_NUMBER, device.getPhoneNumber());
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

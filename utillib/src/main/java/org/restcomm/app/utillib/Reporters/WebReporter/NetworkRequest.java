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

import android.util.Pair;

import org.restcomm.app.utillib.Utils.LoggerUtil;

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

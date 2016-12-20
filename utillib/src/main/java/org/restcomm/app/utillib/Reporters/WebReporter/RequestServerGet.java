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

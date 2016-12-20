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

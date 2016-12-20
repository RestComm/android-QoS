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

package org.restcomm.app.utillib.Utils;

import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.preference.PreferenceManager;

import org.restcomm.app.utillib.DataObjects.Carrier;

public class CoverageRequest {
	private static final String TAG = CoverageRequest.class.getSimpleName();
	
	private static final String END_POINT_WIN = "CoverageImg.aspx";
	private static final String END_POINT_LIN = "/api/overlays";

	private static final String END_POINT = "/api/overlays";

	private static final String KEY_TYPE = "type";
	private static final String KEY_SW = "sw[]";
	private static final String KEY_NE = "ne[]";

	public static final String TYPE_RSSI = "rssi";

	public double Lat0, Lat1, Lng0, Lng1;
	protected URL url;
	/**
	 * Constructs a CoverageRequest object
	 */
	public CoverageRequest(Context context, String type, Carrier car, double lat0, double lat1, double lng0, double lng1, String mapmethod, int floor) {
		//super (context,type, car,  sw,  ne);
		Lat0 = lat0;
		Lat1 = lat1;
		Lng0 = lng0;
		Lng1 = lng1;
		
		int min = 600, max = 800;
		int userCovOnly = PreferenceManager.getDefaultSharedPreferences(context).getInt(PreferenceKeys.Miscellaneous.USER_COV_ONLY, 0);

		String paramsString = "lat0=" + lat0 + "&lng0=" + lng0 + "&lat1=" + lat1 + "&lng1=" + lng1 + "&ver=1&max=" + max + "&min=" + min 
				+"&fill=5&outline=1&fade=70&layer1=LTE/3g/2g signal";
		
		if(mapmethod.equals("survey") || mapmethod.equals("transit")) {
			
			paramsString = "lat0=" + (lat0) + "&lng0=" + lng0 + "&lat1=" + (lat1) + "&lng1=" + lng1
					+ "&ver=1"
					+ "&max=800"
					+ "&min=600"
					+ "&fade=70"
					+ "&layer1=LTE/3g/2g signal"
					+ "&mapMethod=" + mapmethod
					+ "&nocache=1"
					+ "&web=1";
			if(mapmethod.equals("survey"))
			{
				if (floor > -999)
					paramsString += "&minFloor=" + floor;
				paramsString += "&fill=1";
				paramsString += "&outline=0";
			}
			else
			{
				paramsString += "&fill=5";
				paramsString += "&outline=1";
			}
			
		}
		else if (userCovOnly == 1) {
			int userid = Global.getUserID(context);
			long stoptime = System.currentTimeMillis()/1000;
			long starttime = stoptime - 90 * 24 * 3600;  // 90 day time period
			paramsString += "&userid=" + userid + "&minDate=" + starttime + "&maxDate=" + stoptime;
		}
		
		if (car != null)
			paramsString += "&opid=" + car.OperatorId;
		String apiKey = Global.getApiKey(context); // PreferenceManager.getDefaultSharedPreferences(context).getString("API_KEY_PREFERENCE", null);
		
		paramsString += "&apiKey=" + apiKey;
		paramsString = paramsString.replace(" ", "%20");
		try {
			String request = Global.getApiUrl(context) + END_POINT_LIN + "?" + paramsString;
			url = new URL(request);

//			System.out.println(request.toString());
		}
		catch (MalformedURLException e) {
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "constructor", "invalid uri", e);
			throw new RuntimeException(e);
		}
	}

//	public GeoPoint getSW() {
//		return mSW;
//	}
//
//	public GeoPoint getNE() {
//		return mNE;
//	}

	public URL getURL () {return url;}
}

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
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "constructor", "invalid uri", e);
			throw new RuntimeException(e);
		}
	}	
	
}

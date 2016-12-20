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
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "constructor", "invalid uri", e);
			throw new RuntimeException(e);
		}
	}
}

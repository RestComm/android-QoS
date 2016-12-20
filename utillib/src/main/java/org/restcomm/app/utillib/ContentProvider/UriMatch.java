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

package org.restcomm.app.utillib.ContentProvider;

import java.util.HashMap;
import java.util.Map;

import android.net.Uri;

/**
 * This enum lists all the match codes that the uri matcher could use for all 
 * the possible uri cases.
 * @author Abhin
 *
 */
public enum UriMatch {
	SIGNAL_STRENGTHS(
			TablesEnum.SIGNAL_STRENGTHS,
			0, 
			TablesEnum.SIGNAL_STRENGTHS.RelativeUri
		),
		
	SIGNAL_STRENGTH_ID(
			TablesEnum.SIGNAL_STRENGTHS,
			1, 
			TablesEnum.SIGNAL_STRENGTHS.RelativeUri + "/#"
		),
		
	BASE_STATIONS(
		TablesEnum.BASE_STATIONS,
		2, 
		TablesEnum.BASE_STATIONS.RelativeUri
	),
	
	BASE_STATION_ID(
		TablesEnum.BASE_STATIONS,
		3, 
		TablesEnum.BASE_STATIONS.RelativeUri + "/#"
	),
	
	LOCATIONS(
		TablesEnum.LOCATIONS,
		4, 
		TablesEnum.LOCATIONS.RelativeUri
	),
	
	LOCATION_ID(
		TablesEnum.LOCATIONS,
		5, 
		TablesEnum.LOCATIONS.RelativeUri + "/#"
	);

	public final TablesEnum Table;
	public final int Code;
	public final String Path;
	
	// create a map for quick reverse lookup
	private static final Map<Integer, UriMatch> lookup = new HashMap<Integer, UriMatch>();
	static {
		for (UriMatch match : UriMatch.values())
			lookup.put(match.Code, match);
	}
	
	UriMatch(TablesEnum table, int code, String path){
		this.Table = table;
		this.Code = code;
		this.Path = path;
	}
	
	/**
	 * This method does a reverse lookup on the enum using the code and
	 * returns the appropriate enum.
	 * @param code
	 * @return
	 */
	public static UriMatch get(int code){
		return lookup.get(code);
	}
	
	public Uri getContentUri(){
		return Uri.parse("content://" + Tables.AUTHORITY + "/" + Table.RelativeUri);
	}
}

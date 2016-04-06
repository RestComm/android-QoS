package com.cortxt.app.utillib.ContentProvider;

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

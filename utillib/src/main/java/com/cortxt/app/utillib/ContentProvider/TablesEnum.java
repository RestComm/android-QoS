package com.cortxt.app.utillib.ContentProvider;

import android.net.Uri;

/**
 * This enum lists the various tables that can be used in the database along with
 * a link to the class that has more details on the enum.
 * 
 * TODO might want to move the data of the Tables.java class into this enum.
 * @author Abhin
 *
 */
public enum TablesEnum {
	SIGNAL_STRENGTHS(
		"signalStrengths", 
		Tables.SignalStrengths.class,
		"signalStrengths", 
		"vnd.android.cursor.dir/vnd.com.cortxt.app.MMC.SignalStrength",
		"vnd.android.cursor.item/vnd.com.cortxt.app.MMC.SignalStrength"
	),
	BASE_STATIONS(
		"baseStations", 
		Tables.BaseStations.class,
		"baseStations",
		"vnd.android.cursor.dir/vnd.com.cortxt.app.MMC.BaseStation",
		"vnd.android.cursor.item/vnd.com.cortxt.app.MMC.BaseStation"
	),
	LOCATIONS(
		"locations", 
		Tables.Locations.class,
		"locations",
		"vnd.android.cursor.dir/vnd.com.cortxt.app.MMC.Location",
		"vnd.android.cursor.item/vnd.com.cortxt.app.MMC.Location"
	);


	public final String Name;
	public final Class Template;
	public final String RelativeUri;
	public final String ContentType;
	public final String ContentItemType;
	
	TablesEnum(String name, Class template, String relativeUri, String contentType, String contentItemType){
		this.Name = name;
		this.Template = template;
		this.RelativeUri = relativeUri;
		this.ContentType = contentType;
		this.ContentItemType = contentItemType;
	}
	
	public Uri getContentUri(){
		return Uri.parse("content://" + Tables.AUTHORITY + "/" + this.RelativeUri);
	}
}

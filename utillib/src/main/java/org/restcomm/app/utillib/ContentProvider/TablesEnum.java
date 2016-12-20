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

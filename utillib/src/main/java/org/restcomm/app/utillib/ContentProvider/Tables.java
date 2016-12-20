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

/**
 * Convenience definitions for the Provider.java class.
 * This class contains classes for all the tables in the SQLite database.
 * Details regarding the structure of the various tables can be found on the 
 * {@linkplain https://github.com/CORTxT/MMC_Android/issues/1} account for this application.
 * @author Abhin
 *
 */
public final class Tables {
	public static final String AUTHORITY = "com.cortxt.app.mmcutility.ContentProvider.Tables";
	
	/**
	 * A lot of the tables have a timestamp column and this is where they get the
	 * name of that column from.
	 */
	public static final String TIMESTAMP_COLUMN_NAME = "timestamp";
	
	/**
	 * A lot of the tables have an eventId column and this is where they get the
	 * name of that column from.
	 */
	public static final String EVENT_ID_COLUMN_NAME = "eventId";

	//This class cannot be instantiated
	private Tables() {}
	
	public static final class SignalStrengths  {
		// This class cannot be instantiated
		private SignalStrengths() {}
		
		public static final String TIMESTAMP = TIMESTAMP_COLUMN_NAME;
		
		public static final String EVENT_ID = EVENT_ID_COLUMN_NAME;
		public static final String _ID = "_id";
		public static final String SIGNAL = "signal";
		public static final String SIGNALBARS = "bars";
		public static final String ECI0 = "eci0";
		public static final String ECN0 = "ecn0";
		public static final String SNR = "snr";
		public static final String BER = "ber";
		public static final String RSCP = "rscp";
		public static final String SIGNAL2G = "sig2G";
		public static final String LTE_SIGNAL = "lteRssi";
		public static final String LTE_RSRP = "lteRsrp";
		public static final String LTE_RSRQ = "lteRsrq";
		public static final String LTE_CQI = "lteCqi";
		public static final String LTE_SNR = "lteSnr";
		public static final String WIFISIGNAL = "wifisig";
		public static final String COVERAGE = "coverage";
		
		private static final String[] columns = {SIGNAL, ECI0, SNR, BER, RSCP, SIGNAL2G, LTE_SIGNAL, LTE_RSRP, LTE_RSRQ, LTE_SNR, LTE_CQI, SIGNALBARS, ECN0, WIFISIGNAL, COVERAGE};
		public static String getName(int col) {return columns[col];}
	}
	
	public static final class BaseStations{
		// This class cannot be instantiated
		private BaseStations() {}
		
		public static final String TIMESTAMP = TIMESTAMP_COLUMN_NAME;
		
		public static final String EVENT_ID = EVENT_ID_COLUMN_NAME;

		public static final String _ID = "_id";

		public static final String NET_TYPE = "netType";
					
		public static final String BS_LOW = "bsLow";
		
		public static final String BS_MID = "bsMid";
		
		public static final String BS_HIGH = "bsHigh";
		
		public static final String BS_CODE = "bsCode";

		public static final String BS_BAND = "bsBand";

		public static final String BS_CHAN = "bsChan";
//		}
	
	} 
	
	public static final class Locations {
		private Locations() {}
		public static final String _ID = "_id";
		public static final String EVENT_ID = EVENT_ID_COLUMN_NAME;
		
		public static final String TIMESTAMP = TIMESTAMP_COLUMN_NAME;
		
		public static final String ALTITUDE = "altitude";
		
		public static final String ACCURACY = "accuracy";
		
		public static final String BEARING = "bearing";
		
		public static final String LATITUDE = "latitude";
		
		public static final String LONGITUDE = "longitude";
		
		public static final String SPEED = "speed";
		
		public static final String SATELLITES = "satellites";
		
		public static final String PROVIDER = "provider";
	}

}

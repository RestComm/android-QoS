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

package org.restcomm.app.utillib.DataObjects;

import java.util.HashMap;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.cdma.CdmaCellLocation;

/**
 * This class contains getters for device properties of CDMA phones.
 * @author nasrullah
 *
 */
public class CDMADevice extends DeviceInfo {

	public static final String KEY_MEID = "meid";
	public static final String KEY_ESN = "esn";
	public static final String KEY_SID = "sid";
	
	public CDMADevice(Context context) {
		super(context);
	}
	
	/**
	 * @return The phone's esn, or an empty string if it is unknown
	 */
	public String getESN() {
		// TODO : determine if getDeviceID() actually returned ESN before returning it
		return mTelephonyManager.getDeviceId();
	}
	
	/**
	 * @return The phone's meid, or an empty string if it is unknown
	 */
	public String getMEID() {
		// TODO : determine if getDeviceID() actually returned MEID before returning it
		return mTelephonyManager.getDeviceId();
	}
	
	/**
	 * @return The System id, or -1 if it is unknown
	 */
	public int getSid() {
		CellLocation cellLoc = mTelephonyManager.getCellLocation();
		if(cellLoc != null && cellLoc instanceof CdmaCellLocation) {
			return ((CdmaCellLocation) cellLoc).getSystemId();
		}
		else {
			return -1;
		}
	}
	
	@Override
	public HashMap<String, String> getProperties() {
		HashMap<String, String> properties = super.getProperties();
		
		if(getESN().length() > 0) {
			properties.put(KEY_ESN, getESN());
		}
		else if(getMEID().length() > 0) {
			properties.put(KEY_MEID, getMEID());
		}
		
		return properties;
	}
	
	@Override
	public HashMap<String, String> getCarrierProperties() {
		HashMap<String, String> carrierProperties = super.getCarrierProperties();
		
		if(getSid() != -1) {
			carrierProperties.put(KEY_SID, Integer.toString(getSid()));
		}
		
		return carrierProperties;
	}

	
}

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

/**
 * This class contains getters for device properties of GSM phones.
 * @author nasrullah
 *
 */
public class GSMDevice extends DeviceInfo {
	
	public static final String KEY_IMEI = "imei";
	

	public GSMDevice(Context context) {
		super(context);
	}

	/**
	 * @return The phone's imei, or an empty string if it is unknown
	 */
	public String getIMEI() {
		return super.mTelephonyManager.getDeviceId() != null ? super.mTelephonyManager.getDeviceId() : "";
	}
	
	
	@Override
	public HashMap<String, String> getProperties() {
		HashMap<String, String> properties = super.getProperties();
		
		if(getIMEI().length() > 0)
			properties.put(KEY_IMEI, getIMEI());
		
		return properties;
	}

	
}

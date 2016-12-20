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

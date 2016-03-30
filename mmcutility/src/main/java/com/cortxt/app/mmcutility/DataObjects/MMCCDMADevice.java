package com.cortxt.app.mmcutility.DataObjects;

import java.util.HashMap;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.cdma.CdmaCellLocation;

/**
 * This class contains getters for device properties of CDMA phones.
 * @author nasrullah
 *
 */
public class MMCCDMADevice extends MMCDevice {

	public static final String KEY_MEID = "meid";
	public static final String KEY_ESN = "esn";
	public static final String KEY_SID = "sid";
	
	public MMCCDMADevice(Context context) {
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

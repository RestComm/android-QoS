package com.cortxt.app.utillib.DataObjects;

import java.lang.reflect.Field;

import android.provider.ContactsContract;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cortxt.app.utillib.ContentProvider.Tables;

/**
 * This class contains the information about the signal of the phone that
 * will be stored in a rolling buffer.
 * @author abhin
 *
 */ 
public class SignalEx {
	protected SignalStrength signalStrength;
	protected long timestamp;
	public static final String TAG = SignalEx.class.getSimpleName();
	/*
	 * Getters and setters
	 */
	public Object getSignalStrength(){
		if (signalStrength != null)
			return signalStrength;
		return null;
	}
	public void setSignalStrength(Object signalStrength){
		this.signalStrength = (SignalStrength)signalStrength;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/*
	 * Constructor
	 */
	public SignalEx(){ // null signal, means signal unknown
		this.timestamp = System.currentTimeMillis(); //new Time(Time.getCurrentTimezone());
		
	}
	public SignalEx(SignalStrength signalStrength){
		this.signalStrength = signalStrength;
		this.timestamp = System.currentTimeMillis(); //new Time(Time.getCurrentTimezone());
		//this.timestamp.setToNow();
	}

	/*
	 * Returns signal in dB
	 * if signal is unknown, or in network outage, returns null
	 */
	public Integer getDbmValue(int networkType, int phoneType) 
	{
		if (this.signalStrength == null)
			return null;
		if (networkType == PhoneState.NETWORK_NEWTYPE_LTE || networkType == PhoneState.NETWORK_NEWTYPE_IWLAN)
		{
			Integer lteRssi = this.getLayer3("mLteRssi");
			if (lteRssi == null)
				lteRssi = this.getLayer3("mLteSignalStrength");
			if (lteRssi != null) {
				if (lteRssi >= 0 && lteRssi < 32) {
					if (lteRssi == 0)
						lteRssi = -119;  // officially 0 means -113dB or less, but since lowest possible signal on Blackberry = -120, call it -120 for consistency
					else if (lteRssi == 1)
						lteRssi = -111;  // officially 1 = -111 dB
					else if (lteRssi > 1 && lteRssi <= 31)
						lteRssi = (lteRssi - 2) * 2 + -109;
				}

				if (lteRssi > -130)
					return lteRssi;
			}
		}
		// Easy way to get GSM DB
		if (phoneType == TelephonyManager.PHONE_TYPE_GSM)
		{
			int rssi = this.getGsmSignalStrength();
			// according to the 3GPP specs: http://m10.home.xs4all.nl/mac/downloads/3GPP-27007-630.pdf
			if (rssi == 0)
				return -120;  // officially 0 means -113dB or less, but since lowest possible signal on Blackberry = -120, call it -120 for consistency
			else if (rssi == 1)
				return -111;  // officially 1 = -111 dB
			else if (rssi > 1 && rssi <= 31)  
				return (rssi - 2) * 2 + -109;
			else if (rssi < -30 && rssi >= -130)
				return rssi;
			else if (rssi == 99) {

				return null;
			}
			else
				return null; // shouldn't be possible
			
		}
		else if (phoneType == TelephonyManager.PHONE_TYPE_CDMA)
		{
			boolean isEvdo = true; //signal.getSignalStrength().getEvdoDbm() < signal.getSignalStrength().getCdmaDbm() ? true : false;
			if (networkType == TelephonyManager.NETWORK_TYPE_CDMA || networkType == TelephonyManager.NETWORK_TYPE_1xRTT)
				isEvdo = false;
			
			if (isEvdo)
			{
				int evdoDbm = this.getEvdoDbm();
				// If there is no EVDO signal but there is CDMA signal, then use CDMA signal
				if (evdoDbm <= -120 || evdoDbm >= -1)  
				{
					int cdmaDbm = this.getCdmaDbm();
					if (cdmaDbm <= -120 || cdmaDbm >= -1)  
						return evdoDbm;  // no cdma signal either, so send the evdo signal afterall
					else
						return cdmaDbm;
				}
			}
			else
				return this.getCdmaDbm();
		}
		
		return null;
		
	}
	
	/**
	 * This method uses reflection to get the PSC of the network if the API level of the phone
	 * supports that method. This round-about method has to be used because the minimum SDK level
	 * of this application is 7 and the Primary Scrambling Code can only be acquired for API level 9
	 * onwards.
	 * @return
	 */
	public Integer getLayer3(String fieldname){
		Integer returnValue = null;
		if (signalStrength == null)
			return null;
		Field[] fields = null;
		//fields = signalStrength.getClass().getDeclaredFields();
		
		
		Field getFieldPointer = null;
		try {
			getFieldPointer = signalStrength.getClass().getDeclaredField(fieldname); //NoSuchFieldException 
			getFieldPointer.setAccessible(true); 
		} catch (SecurityException e) {
			//Log.d(TAG, "Not enough permissions to access " + fieldname);
		} catch (NoSuchFieldException e) {
			//Log.d(TAG, "Field does not exist - " + fieldname);
		} catch (Exception e) {
			//Log.d(TAG, "Field does not exist - " + fieldname);
		}
		
		if (getFieldPointer != null){
			//now we're in business!
			try {
				
				returnValue = (Integer) getFieldPointer.getInt(signalStrength);
			} catch (Exception e) {
				Log.d(TAG, "Could not get the Primary Scrambling Code", e);
			}
		}
		
		return returnValue;
	}
	
	public boolean isUnknown ()
	{
		if (signalStrength == null)
			return true;
		return false;
	}
	/**
     * Get the GSM Signal Strength, valid values are (0-31, 99) as defined in TS
     * 27.007 8.5
     */
    public int getGsmSignalStrength() {
    	if (signalStrength != null)
    		return signalStrength.getGsmSignalStrength();
    	else
    		return 0;
    }

    /**
     * Get the GSM bit error rate (0-7, 99) as defined in TS 27.007 8.5
     */
    public int getGsmBitErrorRate() {
    	if (signalStrength != null)
    		return signalStrength.getGsmBitErrorRate();
    	else
    		return 0;
    }

    /**
     * Get the CDMA RSSI value in dBm
     */
    public int getCdmaDbm() {
    	if (signalStrength != null)
    		return signalStrength.getCdmaDbm();
    	else
    		return 0;
    }

    /**
     * Get the CDMA Ec/Io value in dB*10
     */
    public int getCdmaEcio() {
    	if (signalStrength != null)
    		return signalStrength.getCdmaEcio();
    	else
    		return 0;
    }

    /**
     * Get the EVDO RSSI value in dBm
     */
    public int getEvdoDbm() {
    	if (signalStrength != null)
    		return signalStrength.getEvdoDbm();
    	else
    		return 0;
    }

    /**
     * Get the EVDO Ec/Io value in dB*10
     */
    public int getEvdoEcio() {
    	if (signalStrength != null)
    		return signalStrength.getEvdoEcio();
    	else
    		return 0;
    }

    /**
     * Get the signal to noise ratio. Valid values are 0-8. 8 is the highest.
     */
    public int getEvdoSnr() {
    	if (signalStrength != null)
    		return signalStrength.getEvdoSnr();
    	else
    		return 0;
    }
    
    @Override 
    public String toString ()
    {
    	if (signalStrength != null)
    		return signalStrength.toString();
    	else
    		return "";
    }
}

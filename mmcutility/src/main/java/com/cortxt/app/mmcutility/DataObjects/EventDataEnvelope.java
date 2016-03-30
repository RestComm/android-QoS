package com.cortxt.app.mmcutility.DataObjects;

import java.util.List;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.cortxt.app.mmcutility.R;
import com.cortxt.app.mmcutility.Reporters.ReportManager;
import com.cortxt.app.mmcutility.Utils.Global;

public class EventDataEnvelope {
	private OCallData oCallData;	//the name of this variable has to match the target JSON packet's root variable
	public static final String TAG = EventDataEnvelope.class.getSimpleName();
	
	public OCallData getOCallData() {
		return oCallData;
	}
	
	public EventDataEnvelope(){
		this.oCallData = new OCallData();
	}
	
	
	
	public EventDataEnvelope(int qOSRating, int iConnectTime, int v, int bDropped,
			int dataSpeed, int iUser, String strCarrier, String handset,
			long lStartTime, int iHandsetId, int iCarrierId,
			String txtPhoneNumber, int bat, String oS, int phoneType, int networkType, boolean isRoaming,
			String ipv4, int mcc, int mnc, String imsi, String apikey,
			List<EventData> oEventData){
		this.oCallData = new OCallData(
				qOSRating, iConnectTime, v, bDropped, dataSpeed, iUser, 
				strCarrier, handset, lStartTime, iHandsetId, iCarrierId, 
				txtPhoneNumber, bat, oS, phoneType, networkType, isRoaming, 
				ipv4, mcc, mnc, imsi, apikey, oEventData
				);
        Context context = ReportManager.getContext();
        if (context != null)
        {
            String altLabel = context.getString(R.string.alt_app_label);
            if (altLabel != null && !altLabel.equals(""))
                this.oCallData.appName = altLabel;
            else
                this.oCallData.appName = Global.getAppName(null);
        }

	}
	
	
	public int getQOSRating() {
		return oCallData.QOSRating;
	}
	public void setQOSRating(int qOSRating) {
		oCallData.QOSRating = qOSRating;
	}
	public int getiConnectTime() {
		return oCallData.iConnectTime;
	}
	public void setiConnectTime(int iConnectTime) {
		oCallData.iConnectTime = iConnectTime;
	}
	public int getV() {
		return oCallData.V;
	}
	public void setV(int v) {
		oCallData.V = v;
	}
	public int getbDropped() {
		return oCallData.bDropped;
	}
	public void setbDropped(int bDropped) {
		oCallData.bDropped = bDropped;
	}
	public int getiUser() {
		return oCallData.iUser;
	}
	public void setiUser(int iUser) {
		oCallData.iUser = iUser;
	}
	public String getStrCarrier() {
		return oCallData.strCarrier;
	}
	public void setStrCarrier(String strCarrier) {
		oCallData.strCarrier = strCarrier;
	}
	public String getHandset() {
		return oCallData.handset;
	}
	public void setHandset(String handset) {
		oCallData.handset = handset;
	}
	
	public long getlStartTime() {
		return oCallData.lStartTime;
	}
	public void setlStartTime(long lStartTime) {
		oCallData.lStartTime = lStartTime;
	}

	public String getOS() {
		return oCallData.OS;
	}
	public void setOS(String oS) {
		oCallData.OS = oS;
	}
	public List<EventData> getoEventData() {
		return oCallData.oEventData;
	}
	public void setoEventData(List<EventData> oEventData) {
		oCallData.oEventData = oEventData;
	}
	
	public void setAllowSpeedTest (int allow)
	{
		oCallData.dataSpeed = allow;
	}
}

/**
 * The JSON packet that has to be created by the EventDataEnvelope class has to have a root element
 * called oCallData. Therefore, the EventDataEnvelope class has a oCallData object inside of it so
 * that GSON would marshall it correctly. The members of the OCallData are public but that does not
 * pose any threat because the OCallData class is not public and its members cannot be accessed 
 * outside of this class. In addition, the object of this class stored inside EventDataEnvelope
 * is private and thus does not allow direct access to the object. The setters and getters in
 * the EventDataEnvelope make the fields of the OCallData object easily accessible to the outside
 * world.
 * @author abhin
 *
 */
class OCallData{
	/**
	 * The quality of serice rating. An integer from -1 to 5.
	 * -1 stands for unknown which should be used during an average call or 
	 * when not prompting for voice quality.
	 * 
	 * 0 means dropped call and 1-5 is when the user supplies a star rating.
	 */
	@Deprecated
	public int QOSRating;
	/**
	 * The original intention was to calculate the milliseconds between the attempt to start
	 * a call and the actual 'ring'. But currently on the blackberry the value sent is 0.
	 */
	@Deprecated
	public int iConnectTime;
	/**
	 * This is the version number of the Android app.
	 */
	public int V;
	/**
	 * 0 if the call was not-dropped and 1 otherwise.
	 */
	@Deprecated
	public int bDropped;
	/**
	 * 0 if a data speed test is not allowed and 1 otherwise.
	 * 
	 * This is for the client to be able to limit the number of data speed tests.
	 */
	public int dataSpeed;
	/**
	 * The user ID.
	 */
	public int iUser;
	/**
	 * A string that describes the carrier.
	 */
	public String strCarrier;
	/*
	 * The MCC and MNC of the carrier
	 */
	/**
	 * "Android" + <the model name of the phone>
	 */
	
	public String getMCC() {
		return Integer.toString(mcc);
	}
	public void setMCC(int mCC) {
		mcc = mCC;
	}
	public int getMNC() {
		return mnc;
	}
	public void setMNC(int mNC) {
		mnc = mNC;
	}
	public String handset;
	/**
	 * The timestamp (in milliseconds) of the first event (in UTC). This has to use System.currentTimeMillis().
	 */
	public long lStartTime;
	/**
	 * whether the device is roaming
	 */
	public int roam;
	/**
	 * description of network type: GPRS,UMTS,CDMA
	 */
	public String ntype;
	@Deprecated
	public int iHandsetId;
	/**
	 * 
	 */
	@Deprecated
	public int iCarrierId;
	/**
	 * The phone number that you called.
	 */
	@Deprecated
	public String txtPhoneNumber;
	/**
	 * The battery level. On a scale of 0-100.
	 */
	public int bat;
	/**
	 * The OS version number not including the word "Android". eg. "2.1.1"
	 */
	public String OS;
	
	public String ipv4;
	public String IMSI;
	public String apiKey;
	public int mcc, mnc;
	public String appName;
	
	/**
	 * List of events.
	 */
	public List<EventData> oEventData;
	
	public OCallData(int qOSRating, int iConnectTime, int v, int bDropped,
			int dataSpeed, int iUser, String strCarrier, String handset,
			long lStartTime, int iHandsetId, int iCarrierId,
			String txtPhoneNumber, int bat, String oS, int phoneType, int networkType, boolean isRoaming,
			String _ipv4, int _mcc, int _mnc, String _imsi, String _apikey,
			List<EventData> oEventData) {
		V = v;
		this.iUser = iUser;
		this.strCarrier = strCarrier;
		this.handset = handset;
		this.lStartTime = lStartTime;
		this.bat = bat;
		OS = oS;
		this.oEventData = oEventData;

		this.setNetworkType(phoneType, networkType);
		if (isRoaming)
			this.roam = 1;
		else
			this.roam = 0;
		this.ipv4 = _ipv4;
		this.mcc = _mcc;
		this.mnc = _mnc;
		this.IMSI = _imsi;
		this.apiKey = _apikey;
		
		
	}
	
	public OCallData() {
		
	}
	
	public void setNetworkType (int phoneType, int networkType)
    {
        if (phoneType == TelephonyManager.PHONE_TYPE_CDMA)
        	ntype = "cdma";
        else if (phoneType == TelephonyManager.PHONE_TYPE_GSM)
        {
	        if ( networkType == TelephonyManager.NETWORK_TYPE_EDGE || networkType == TelephonyManager.NETWORK_TYPE_GPRS)
	    		ntype = "gsm";
	    	else
	    		ntype = "umts";
        }
    }
	
}

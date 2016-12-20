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

import java.lang.reflect.Field;

import android.content.ContentValues;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

//import com.cortxt.app.MMC.ServicesOld.MMCPhoneStateListenerOld;
import org.restcomm.app.utillib.DataObjects.CellLocationEx;
import org.restcomm.app.utillib.DataObjects.SignalEx;
import org.restcomm.app.utillib.DataObjects.PhoneState;
import org.restcomm.app.utillib.Utils.Global;
import org.restcomm.app.utillib.Utils.LoggerUtil;
import org.restcomm.app.utillib.Utils.PreferenceKeys;
//import com.cortxt.app.MMC.WebServicesOld.JSON.SpeedTestResultsOld;

import org.json.JSONObject;


public class ContentValuesGenerator {
	
	private static Context service;

	
	public ContentValuesGenerator(Context context){
		this.service = context;      
	}

	/**
	 * Creates a ContentValues object with keys taken from {@link Tables.Locations}
	 * and values taken from the location parameters
	 * @param location
	 * @return
	 */
	public static ContentValues generateFromEventLocation(double latitude, double longitude, long lTimestamp, int iUncertainty, int iAltitude, int iSpeed, int iHeading, String provider, long eventId)
	{
		/*
		 * Note:- A lot of the getters of the location object return 0.0f when the 
		 * appropriate data doesn't exist. We replace these by null for the sqlite database.
		 */
		ContentValues values = new ContentValues();
		values.put(Tables.Locations.ACCURACY, iUncertainty == 0.0f ? null : iUncertainty);
		values.put(Tables.Locations.ALTITUDE, iAltitude == 0.0f ? null : iAltitude);
		values.put(Tables.Locations.BEARING, iHeading == 0.0f ? null : iHeading);
		values.put(Tables.Locations.LATITUDE,latitude);
		values.put(Tables.Locations.LONGITUDE, longitude);
		values.put(Tables.Locations.PROVIDER, provider);
		values.put(Tables.Locations.SPEED, iSpeed == 0 ? null : (double)iSpeed);
		values.put(Tables.Locations.TIMESTAMP, lTimestamp);
		values.put(Tables.SignalStrengths.EVENT_ID, eventId);
		return values;
	}
	/**
	 * Creates a ContentValues object with keys taken from {@link Tables.Locations}
	 * and values taken from the location object passed as parameter.
	 * @param location
	 * @return
	 */
	public static ContentValues generateFromLocation(Location location, long stagedEventId, int satellites){
		/*
		 * Note:- A lot of the getters of the location object return 0.0f when the 
		 * appropriate data doesn't exist. We replace these by null for the sqlite database.
		 */
		ContentValues values = new ContentValues();
		if (location == null)
			location = new Location ("");
		
		//location.setTime(System.currentTimeMillis());
		location.setTime(location.getTime());
		values.put(
			Tables.Locations.ACCURACY,
			location.getAccuracy() == 0.0f ? null : location.getAccuracy()
		);
		values.put(
			Tables.Locations.ALTITUDE,
			location.getAltitude() == 0.0f ? null : location.getAltitude()
		);
		values.put(
			Tables.Locations.BEARING,
			location.getBearing() == 0.0f ? null : location.getBearing()
		);
		values.put(
			Tables.Locations.LATITUDE,
			location.getLatitude()
		);
		values.put(
			Tables.Locations.LONGITUDE,
			location.getLongitude()
		);
		values.put(
			Tables.Locations.PROVIDER,
			location.getProvider()
		);
		values.put(
			Tables.Locations.SPEED,
			location.getSpeed() == 0.0f ? null : location.getSpeed()
		);
		values.put(
			Tables.Locations.TIMESTAMP,
			location.getTime()
		);
		values.put(
			Tables.SignalStrengths.EVENT_ID,
			stagedEventId
		);
		values.put(
				Tables.Locations.SATELLITES,
				satellites
			);
		//MMCLogger.logToFile(MMCLogger.Level.DEBUG, "ContentValues", "generateFromLocation", "gpsTime="+location.getTime());
		return values;
	}
	
	/**
	 * This method generates a ContentValues object from the signal object so that it may
	 * be stored in the database.
	 * @param signal
	 * @param phoneType This is the phone type and must be one of {@link TelephonyManager#PHONE_TYPE_CDMA}
	 * or {@link TelephonyManager#PHONE_TYPE_GSM}.
	 * @param stagedEventId This is the id of the event that this signal has to be related to
	 * @return
	 */
	public static ContentValues generateFromSignal(SignalEx signal, int phoneType, int networkType, int serviceState, int dataState, long stagedEventId, int wifiSignal, JSONObject serviceMode){
		ContentValues values = new ContentValues();
		Integer dBm = 0;
		Integer signalDB = null;
		try {
			if (serviceMode != null && serviceMode.getLong("time") + 5000 < System.currentTimeMillis())
				serviceMode = null;
			if (signal == null) // as a result of a service outage
			{
				LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "ContentValues", "generateFromSignal", "signal == null");
				values.put(Tables.SignalStrengths.SIGNAL, -256);
				//now do the common parameters
				values.put(Tables.SignalStrengths.TIMESTAMP, System.currentTimeMillis());
				values.put(Tables.SignalStrengths.EVENT_ID, stagedEventId);
				values.put(Tables.SignalStrengths.COVERAGE, 0);
				return values;
			}
			if (signal.getSignalStrength() == null)  // as a result of a screen off (signal unknown)
				values.put(Tables.SignalStrengths.SIGNAL, (Integer) null);
				//do phone type specific actions first
			else if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
				boolean isEvdo = true;

				if (networkType == TelephonyManager.NETWORK_TYPE_1xRTT || networkType == TelephonyManager.NETWORK_TYPE_CDMA) {
					isEvdo = false;
					dBm = signal.getCdmaDbm();
				} else {
					dBm = signal.getEvdoDbm();
					int evdoDbm = signal.getEvdoDbm();
					// If there is no EVDO signal but there is CDMA signal, then use CDMA signal
					if (evdoDbm <= -120 || evdoDbm >= -1) {
						int cdmaDbm = signal.getCdmaDbm();
						if (cdmaDbm <= -120 || cdmaDbm >= -1)
							dBm = evdoDbm;  // no cdma signal either, so send the evdo signal afterall
						else {
							dBm = cdmaDbm;
							isEvdo = false; // display and report the CDMA signal if CDMA has signal and EVDO does not
						}
					}
				}

				//if (dBm == -1) // When Scott had a network outage on CDMA, he got -1, we want -256
				//	dBm = -256;
				if (dBm == -120 && networkType == TelephonyManager.NETWORK_TYPE_LTE)
					dBm = null;  // signal not known, this seems to happen with LTE advanced
				values.put(
						Tables.SignalStrengths.SIGNAL, dBm
						//isEvdo ? signal.getSignalStrength().getEvdoDbm() : signal.getSignalStrength().getCdmaDbm()
				);
				values.put(
						Tables.SignalStrengths.ECI0,
						isEvdo ? signal.getEvdoEcio() / 10.0 : signal.getCdmaEcio() / 10.0
				);
				values.put(
						Tables.SignalStrengths.SNR,
						isEvdo ? signal.getEvdoSnr() : null
				);
				//if (isEvdo)
				values.put(
						Tables.SignalStrengths.SIGNAL2G, signal.getCdmaDbm()
						//isEvdo ? signal.getSignalStrength().getCdmaDbm() : null
				);
				signalDB = dBm;
			} else if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {


				if (getPlatform() == 1) //On Android device
					signalDB = signal.getGsmSignalStrength();
				else if (getPlatform() == 3) {//On Blackberry device
					signalDB = PreferenceManager.getDefaultSharedPreferences(service.getApplicationContext()).
							getInt(PreferenceKeys.Miscellaneous.BB_SIGNAL, 99);

				}
				if (signalDB == 99 || signalDB == -1 || signalDB == null) {
					signalDB = null;
//				Integer signalLte = signal.getLayer3("mLteSignalStrength");
//				if (signalLte != null && signalLte < 99)
//				{
//					if (signalLte == 0)
//						signalDB = -120;
//					else
//						signalDB = -113 + signalLte*2;
//				}

					// If signal is unknown but signal bars are known, send bars
					Integer signalBar = signal.getLayer3("mGsmSignalBar");
					if (signalBar != null && signalBar != -1) {
						signalDB = getSignalDBFromBars(signalBar);
						values.put(Tables.SignalStrengths.SIGNALBARS, signalBar);
					}

				} else if (getPlatform() == 1)
					signalDB = signal.getDbmValue(networkType, phoneType);

				Integer ecio = signal.getLayer3("mUmtsEcio");
				if (ecio == null)
					ecio = signal.getLayer3("mgw_ecio");
				if (ecio == null)
					ecio = signal.getLayer3("mGsmEcio");
//			if (ecio == null)
//			{
//				ecio = signal.getLayer3("lastEcIoIndex");
//				if (ecio != null)
//					ecio = 2*signal.getLayer3Array("lastEcIoValues", ecio);
//			}

				Integer ecno = signal.getLayer3("mUmtsEcno");
				if (ecno == null)
					ecno = signal.getLayer3("mGsmEcno");
				Integer rscp = signal.getLayer3("mUmtsRscp");
				if (rscp == null)
					rscp = signal.getLayer3("mGsmRscp");
				if (rscp == null)
					rscp = signal.getLayer3("mWcdmaRscp");
				if ((signalDB == null || signalDB <= -120) && rscp != null && rscp > -120 && rscp < -20)
					signalDB = rscp;




				values.put(
						Tables.SignalStrengths.ECI0, ecio);
				values.put(
						Tables.SignalStrengths.RSCP, rscp);
				values.put(
						Tables.SignalStrengths.ECN0, ecno);
				values.put(
						Tables.SignalStrengths.SIGNAL, signalDB);
				values.put(
						Tables.SignalStrengths.BER,
						signal.getGsmBitErrorRate() == 99 ? null : signal.getGsmBitErrorRate()
				);
			}
			values.put(
					Tables.SignalStrengths.WIFISIGNAL, wifiSignal);
			// check for LTE signal signal quality parameters only if connected to LTE
			if (networkType == TelephonyManager.NETWORK_TYPE_LTE)
			{
				Integer lteRsrp = -1, lteRsrq, lteSnr, lteCqi;

				lteRsrp = signal.getLayer3("mLteRsrp");
				lteRsrq = signal.getLayer3("mLteRsrq");
				lteSnr = signal.getLayer3("mLteRssnr");
				if (lteRsrp != null && lteRsrp >= 40 && lteRsrp < 140)
					lteRsrp = -lteRsrp;
				else if (lteRsrp != null && lteRsrp > 0 && lteRsrp <= 32)
					lteRsrp = (lteRsrp - 2) * 2 + -109;
				if (lteSnr == null || lteSnr > 1000)
					lteSnr = signal.getLayer3("mLteSnr");
				if (lteSnr == null || lteSnr < -200 || lteSnr > 1000)
					lteSnr = null;
				if (lteRsrp != null && lteRsrp > 1000)
					lteRsrp = lteRsrq = null;

				lteCqi = signal.getLayer3("mLteCqi");
				values.put(Tables.SignalStrengths.LTE_RSRP, lteRsrp);
				values.put(Tables.SignalStrengths.LTE_RSRQ, lteRsrq);
				values.put(Tables.SignalStrengths.LTE_SNR, lteSnr);
				values.put(Tables.SignalStrengths.LTE_CQI, lteCqi);

			}
			// check for the LTE signal regardless, at least it will indicate of device supports LTE
			Integer lteRssi = signal.getLayer3("mLteRssi");
			if (lteRssi == null)
				lteRssi = signal.getLayer3("mLteSignalStrength");
			if (lteRssi != null) {
				if (lteRssi >= 0 && lteRssi < 32) {
					if (lteRssi == 0)
						lteRssi = -120;  // officially 0 means -113dB or less, but since lowest possible signal on Blackberry = -120, call it -120 for consistency
					else if (lteRssi == 1)
						lteRssi = -111;  // officially 1 = -111 dB
					else if (lteRssi > 1 && lteRssi <= 31)
						lteRssi = (lteRssi - 2) * 2 + -109;
				}

				// allow for the possibility of sending a 3G signal and LTE signal at the same time
				// but if LTE signal is present, and 3G signal says -120 or worse, ignore regular signal
				if (lteRssi > -120 && (dBm == null || dBm <= -120))
					values.put(Tables.SignalStrengths.SIGNAL, (Integer) lteRssi);

			}
			values.put(Tables.SignalStrengths.LTE_SIGNAL, lteRssi);

			if (serviceMode != null && serviceMode.getLong("time") + 20000 > System.currentTimeMillis()) {
				if (serviceMode.has("ecio") && serviceMode.getString("ecio").length() > 1)
				{
					int svc_ecio = Integer.parseInt(serviceMode.getString("ecio"),10);
					if (svc_ecio <= -2 && svc_ecio >= -30)
					{
						values.put(Tables.SignalStrengths.ECI0, svc_ecio);
					}
				}
				if (serviceMode.has("rscp") && serviceMode.getString("rscp").length() > 1)
				{
					int svc_rscp = Integer.parseInt(serviceMode.getString("rscp"),10);
					if (svc_rscp <= -20 && svc_rscp >= -120) //  && (signalDB == null || signalDB <= -120))
						values.put(Tables.SignalStrengths.SIGNAL, svc_rscp);
				}

				if (serviceMode.has("snr") && serviceMode.getString("snr").length() > 1)
				{
					float svc_fsnr = Float.parseFloat(serviceMode.getString("snr"));
					int svc_snr = (int)(svc_fsnr * 10);
					if (svc_snr > -200 && svc_snr < 2000)
						values.put(Tables.SignalStrengths.LTE_SNR, svc_snr);
				}

				if (serviceMode.has("rsrp") && serviceMode.getString("rsrp").length() > 1)
				{
					int svc_rsrp = Integer.parseInt(serviceMode.getString("rsrp"),10);
					if (svc_rsrp <= -20 && svc_rsrp >= -140)
						values.put(Tables.SignalStrengths.LTE_RSRP, svc_rsrp);
				}

				if (serviceMode.has("rsrq") && serviceMode.getString("rsrq").length() > 1)
				{
					int svc_rsrq = Integer.parseInt(serviceMode.getString("rsrp"),10);
					if (svc_rsrq <= -1 && svc_rsrq >= -30)
						values.put(Tables.SignalStrengths.LTE_RSRQ, svc_rsrq);
				}

			}

			//now do the common parameters
			values.put(
					Tables.SignalStrengths.TIMESTAMP,
					signal.getTimestamp()
			);
			values.put(
					Tables.SignalStrengths.EVENT_ID,
					stagedEventId
			);
			int coverage = 0;
			if (networkType == 0) {
				if (serviceState == ServiceState.STATE_IN_SERVICE)
					networkType = 1;
				//else if (serviceState == ServiceState.STATE_POWER_OFF)
				//	networkType = -1;
			}
			int networkTier = PhoneState.getNetworkGeneration(networkType);
			if (networkTier == 0) // dont make it 0 unless truly out of service
				networkTier = 1;
			if (serviceState == ServiceState.STATE_OUT_OF_SERVICE &&
					(dataState != TelephonyManager.DATA_CONNECTED || networkType != TelephonyManager.NETWORK_TYPE_LTE))  // Sprint can be connected to LTE and say outofservice
				networkTier = 0;
			else if (serviceState == ServiceState.STATE_POWER_OFF || serviceState == ServiceState.STATE_EMERGENCY_ONLY || serviceState == ServiceState.STATE_POWER_OFF || serviceState == 9 ) // 9 = MMCPhoneStateListenerOld.SERVICE_STATE_AIRPLANE)
				networkTier = -1;


			// tier 5 becomes 11111, tier 1 = 00001
			coverage = networkTier; // (1 << networkTier) - 1;

			//String reflect = listSignalFields (signal);

			values.put(
					Tables.SignalStrengths.COVERAGE,
					coverage
			);
			//MMCLogger.logToFile(MMCLogger.Level.DEBUG, "", "onSignal.listSignalFields", reflect);
			//MMCLogger.logToFile(MMCLogger.Level.DEBUG, "", "onSignal.values", values.toString());
		}
		catch (Exception e)
		{
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, "ContentValuesGenerator", "generateFromSignal", "exception", e);
		}
		return values; 
	}
	
	private static String listSignalFields (SignalEx mmcsignal)
	{
		int i;
		String strSignals = "";
		if (mmcsignal != null && mmcsignal.getSignalStrength() != null)
		{
			
			SignalStrength signalStrength = (SignalStrength) mmcsignal.getSignalStrength();
			
			Field[] fields = null;
			try {
				fields = signalStrength.getClass().getDeclaredFields();
				
				for (i=0; i<fields.length; i++)
				{
					fields[i].setAccessible(true);
					//if (!fields[i].getName().equals("CREATOR") && !fields[i].getName().equals("LOG_TAG") &&
					//		fields[i].getName().indexOf("INVALID") == -1 && fields[i].getName().indexOf("STRENGTH") == -1)
					if (fields[i].getName().toLowerCase().substring(0,1).equals(fields[i].getName().substring(0,1)))
					{
						try
						{
						strSignals += fields[i].getName() + "=";
						if (fields[i].get(signalStrength) != null)
							strSignals += fields[i].get(signalStrength).toString() + ",";
						else
							strSignals += "null";
						}
						catch (Exception e)
						{
							LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "", "listSignalFields", "exception", e);
						}
					}
				}
			} catch (SecurityException e) {
				LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "", "listSignalFields", "SecurityException", e);
			} catch (Exception e) {
				LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "", "listSignalFields", "exception", e);
			}
		}
		return strSignals;
	}

	protected static Integer getSignalDBFromBars (Integer signalBar)
	{
		int dBm = 0;
		switch (signalBar)
		{
		case 5: return -70;
		case 4: return -80;
		case 3: return -90;
		case 2: return -100;
		case 1: return -110;
		case 0: return null;
		}
		return dBm;
	}	

	/**
	 * This method generates a ContentValues object from the cell location object so that it 
	 * may be stored in the database.
	 * @param cellLoc The cell location
	 * @param phoneType This is the phone type and must be one of {@link TelephonyManager#PHONE_TYPE_CDMA}
	 * or {@link TelephonyManager#PHONE_TYPE_GSM}.
	 * @return
	 */
	public static ContentValues generateFromCellLocation(CellLocationEx cellLoc, long stagedEventId){
		ContentValues values = new ContentValues();
		
		int bsLow = cellLoc.getBSLow(), bsMid = cellLoc.getBSMid(), bsHigh = cellLoc.getBSHigh();
		if (bsLow == 65535)
			bsLow = -1;
		if (bsMid == 65535)
			bsMid = -1;
		if (bsHigh == 65535)
			bsHigh = -1;
		int bsCode = cellLoc.getBSCode();
		int bsBand = -1;
		int bsChan = -1;
		if (Global.getServiceMode() != null) {
			int val;
			try {
				JSONObject serviceMode = Global.getServiceMode();
				if (serviceMode != null && serviceMode.getLong("time") + 20000 > System.currentTimeMillis()) {
					if (serviceMode.has("psc") && serviceMode.getString("psc").length() > 1) {
						val = Integer.parseInt(serviceMode.getString("psc"), 10);
						if (val > 0) {
							bsCode = val;
						}
					}
					if (serviceMode.has("band") && serviceMode.getString("band").length() > 0) {
						val = Integer.parseInt(serviceMode.getString("band"), 10);
						if (val > 0) {
							bsBand = val;
						}
					}
					else if (serviceMode.has("freq") && serviceMode.getString("freq").length() > 0) {
						val = Integer.parseInt(serviceMode.getString("freq"), 10);
						if (val > 0) {
							bsBand = val;
						}
					}
					if (serviceMode.has("channel") && serviceMode.getString("channel").length() > 0) {
						val = Integer.parseInt(serviceMode.getString("channel"), 10);
						if (val > 0) {
							bsChan = val;
						}
					}
				}
			} catch (Exception e) {
			}
		}

		String netType = "";
		if (cellLoc.getCellLocation() != null && cellLoc.getCellLocation() instanceof GsmCellLocation)
			netType = "gsm";
		else if (cellLoc.getCellLocation() != null && cellLoc.getCellLocation() instanceof CdmaCellLocation)
			netType = "cdma";
		
		values.put(
				Tables.BaseStations.TIMESTAMP,
				cellLoc.getCellIdTimestamp()
		);
		values.put(
			Tables.BaseStations.NET_TYPE,
			netType
		);
		values.put(
				Tables.BaseStations.BS_CODE,
				bsCode
		);
		values.put(
			Tables.BaseStations.BS_LOW,
			bsLow == -1 ? null : bsLow
		);
		values.put(
			Tables.BaseStations.BS_MID,
			bsMid == -1 ? null : bsMid
		);
		values.put(
			Tables.BaseStations.BS_HIGH,
			bsHigh == -1 ? null : bsHigh
		);
		values.put(
				Tables.BaseStations.BS_BAND,
				bsBand == -1 ? null : bsBand
		);
		values.put(
				Tables.BaseStations.BS_CHAN,
				bsChan == -1 ? null : bsChan
		);
		values.put(
			Tables.SignalStrengths.EVENT_ID,
			stagedEventId
		);
		return values;
	}

	
 public static int getPlatform(){
	 if(android.os.Build.BRAND.toLowerCase().contains("blackberry") && Build.VERSION.SDK_INT < 18) {
		 return 3;
	 } 
	 else {
		 return 1;
	 }           
 }

}

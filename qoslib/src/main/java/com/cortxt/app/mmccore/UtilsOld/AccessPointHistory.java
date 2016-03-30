package com.cortxt.app.mmccore.UtilsOld;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cortxt.app.mmccore.MMCService;
import com.cortxt.app.mmcutility.DataObjects.EventObj;
import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.Utils.PreferenceKeys;
import com.cortxt.app.mmcutility.Utils.UsageLimits;

public class AccessPointHistory {

	private List<AccessPointSample> access_points_history = new ArrayList<AccessPointSample>();
	
	public final static int TYPE_WIFI = 10;
	public final static int TYPE_WIMAX = 11;
	public final static int TYPE_BLUETOOTH = 13;
	public final static int TYPE_BATTERY = 88;
	public final static int TYPE_ROAMING = 99;
	
	private long wifiStartTime = 0; //Used for access point start times
	private long wimaxStartTime = 0;
	private long bluetoothStartTime = 0;
	private String wifiId = null, wimaxId = null, bluetoothId = null;
	private long roamingStartTime = 0; 
	private long batteryStartTime = 0; 
	private int roamOn = 0; 
	private int wifiOn = 0, wifiEvent = 0;
	private int bluetoothOn = 0;
	private int wimaxOn = 0; 
	private int batteryOn = 0; 
	private int wifiSig = 0;
	private String stateSP = null;
	private MMCService mContext = null;
	
	public static final String TAG = AccessPointHistory.class.getSimpleName();

	public AccessPointHistory (MMCService context)
	{
		mContext = context;
		restoreState (); // state of the counters and timers will always be stored to preferences in case of restart
	}
	public String updateWifiHistory (int _type, long _start, long _end, String _id, int _sig) {			
		try	{
			AccessPointSample smp = new AccessPointSample(_type, _start, _end, _id, _sig);
			access_points_history.add (smp);
			//MMCLogger.logToFile(MMCLogger.Level.DEBUG, "AccessPointHistory", "updateWifiHistory", this.toString());
			return this.toString();			
		}
		catch (Exception e)	{
			return null;
		}		
	}
	
	// ON each 3hr checkpoint, prune the access point history lists, removing items 3 hours old
	public void clearAccessPointsHistory ()	{
		
		AccessPointSample sample;
		for(int i=0; i< access_points_history.size(); i++) {	
			sample = access_points_history.get(i);
			if (sample.startTimestamp + 180*60000 < System.currentTimeMillis())	{
				access_points_history.remove(i);
				i --;
			}
			else {
				break;	
			}
		}
		//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "clearAccessPointslHistory", " access points = " + access_points_history.size());
	}
	
	public void beginEvent (int type, int val)
	{
		switch (type)
		{
		case TYPE_BATTERY:
			if (batteryOn == TYPE_BATTERY)
				return;
			batteryOn = TYPE_BATTERY;
			batteryStartTime = System.currentTimeMillis(); 
			break;
		case TYPE_ROAMING:
			if (roamOn == TYPE_ROAMING)
				return;
			roamOn = TYPE_ROAMING;
			roamingStartTime = System.currentTimeMillis(); 
			break;
		case TYPE_WIFI:
			if (wifiOn == TYPE_WIFI)
				return;
			wifiOn = TYPE_WIFI;
			wifiStartTime = System.currentTimeMillis();
			
			wifiSig = val;
			wifiId = null;
			try
			{
				if (mContext != null)
				{
//					WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
//					WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//					wifiId = wifiInfo.getBSSID();
					int allow = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(PreferenceKeys.Miscellaneous.WIFI_EVENTS, 0);
					if (allow > 0 && mContext.getUsageLimits().getUsageProfile () > UsageLimits.MINIMAL)
					{
						// Wifi connection needs to last longer than 4 seconds to actually trigger
						mContext.handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								// If longer outage after 2 seconds, do nothing
								if (wifiStartTime == 0 || wifiEvent == 1 || wifiStartTime + 300000 - 1000 > System.currentTimeMillis()) //  && previousServiceState != ServiceState.STATE_EMERGENCY_ONLY)
								{
									MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "beginEvent", "Wifi connected < 10 minutes, ignoring");
									return;
								}
								wifiEvent = 1; // know that we triggered a wifi connect event
								mContext.getEventManager().triggerSingletonEvent(EventType.WIFI_CONNECT);
							}
						}, 300000);
					}

				}
			} catch (Exception e)
			{
				MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "beginEvent:wifiManager.getConnectionInfo()", "exception", e);
			}
			break;
		
		case TYPE_WIMAX:
			if (wimaxOn == TYPE_WIMAX)
				return;
			wimaxOn = TYPE_WIMAX;
			wimaxStartTime = System.currentTimeMillis();
			wimaxId = null;
//			try
//			{
//				if (mContext != null)
//				{
//					WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIMAX_SERVICE);
//					WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//					wimaxId = wifiInfo.getBSSID();
//				}
//			} catch (Exception e){}
			break;
		case TYPE_BLUETOOTH:
			if (bluetoothOn == TYPE_BLUETOOTH)
				return;
			bluetoothOn = TYPE_BLUETOOTH;
			bluetoothStartTime = System.currentTimeMillis();
			bluetoothId = null;
			break;
		}
		saveState ();
		
	}
	
	public void endEvent (int type)
	{
		String result = "";
	
		switch (type)
		{
		case TYPE_BATTERY:
			if (batteryStartTime != 0 && batteryOn != -1){
				result = updateWifiHistory(TYPE_BATTERY, batteryStartTime/1000, System.currentTimeMillis()/1000, null, 0); 
				
				//Reset start time and type for next record keeping
				batteryStartTime = 0;
				batteryOn = -1;
			}else
				return;
			break;
		
		case TYPE_ROAMING:
			if (roamingStartTime != 0 && roamOn != -1){
				result = updateWifiHistory(TYPE_ROAMING, roamingStartTime/1000, System.currentTimeMillis()/1000, null, 0); 
				
				//Reset start time and type for next record keeping
				roamingStartTime = 0;
				roamOn = -1;
			}else
				return;
			break;
		
		case TYPE_WIFI:
			if (wifiStartTime != 0 && wifiOn != -1) { 
				result = updateWifiHistory(TYPE_WIFI, wifiStartTime/1000, System.currentTimeMillis()/1000, wifiId, wifiSig); 
				if (wifiStartTime > 0)
				{
					int allow = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(PreferenceKeys.Miscellaneous.WIFI_EVENTS, 0);
					if (allow > 0 && mContext.getUsageLimits().getUsageProfile () != UsageLimits.MINIMAL && wifiEvent == 1)
					{
						wifiEvent = 0;
						EventObj evt = mContext.getEventManager().triggerSingletonEvent (EventType.WIFI_DISCONNECT);
						evt.setDuration (System.currentTimeMillis() - wifiStartTime);
					}
				}
				wifiStartTime = 0;
				wifiOn = -1;
			}else
				return;
			break;
			
		case TYPE_WIMAX:
			if (wimaxStartTime != 0 && wimaxOn != -1) { 
				result = updateWifiHistory(TYPE_WIMAX, wimaxStartTime/1000, System.currentTimeMillis()/1000, wimaxId, 0); 
				wimaxStartTime = 0;
				wimaxOn = -1;
			}else
				return;
			break;
		case TYPE_BLUETOOTH:
			if (bluetoothStartTime != 0 && bluetoothOn != -1) { 
				result = updateWifiHistory(TYPE_BLUETOOTH, bluetoothStartTime/1000, System.currentTimeMillis()/1000, bluetoothId, 0); 
				bluetoothStartTime = 0;
				bluetoothOn = -1;
			}else
				return;
			break;
		}
		//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "accessPointHistory", result);
		saveState ();
	}
	public int getSize() {
		return access_points_history.size();
	}
	
	public long totalTimeSpent(int _type) {			
		long timeSpent = 0;
			
		for(AccessPointSample sample : access_points_history) {			
			timeSpent += sample.endTimestamp - sample.startTimestamp;		
		}
		return timeSpent;	
	}	
	
	// State of the counters and timers will always be stored in case of restart
	private void saveState ()
	{
		stateSP = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", wifiStartTime,wimaxStartTime,wifiId,wimaxId,roamingStartTime,
					batteryStartTime,roamOn, wifiOn, wimaxOn, batteryOn, bluetoothStartTime, bluetoothId);
		SharedPreferences preferenceSettings = PreferenceManager.getDefaultSharedPreferences(mContext);
		preferenceSettings.edit().putString(PreferenceKeys.Miscellaneous.ACCESS_POINT_STATE, stateSP).commit ();
		//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "saveState", stateSP);
		
	}
	// State of the counters and timers will always be stored in case of restart
	private void restoreState ()
	{
		if (stateSP == null)
		{
			try
			{
				SharedPreferences preferenceSettings = PreferenceManager.getDefaultSharedPreferences(mContext);
				stateSP = preferenceSettings.getString(PreferenceKeys.Miscellaneous.ACCESS_POINT_STATE, "");
				if (stateSP != null && stateSP.length() > 10)
				{
					String[] parts = stateSP.split(",");
					if (parts.length == 10)
					{
						wifiStartTime = Long.parseLong(parts[0]);
						wimaxStartTime = Long.parseLong(parts[1]);
						wifiId = parts[2];
						wimaxId = parts[3];
						roamingStartTime = Long.parseLong(parts[4]);
						batteryStartTime = Long.parseLong(parts[5]);
						roamOn = Integer.parseInt(parts[6]);
						wifiOn = Integer.parseInt(parts[7]);
						wimaxOn = Integer.parseInt(parts[8]);
						batteryOn = Integer.parseInt(parts[9]);
						bluetoothStartTime =  Long.parseLong(parts[10]);
						bluetoothId = (parts[11]);
					}
				}
			}
			catch (Exception e)
			{}
		}
	}

	public String toString() {		
		String txt = "";
		String typeToString = "";

		int size = getSize();
		for(int i = 0; i < size; i++) {
			AccessPointSample sample = access_points_history.get(i);
			switch(sample.type) {
				case TYPE_WIFI:	typeToString = "Wifi";
					break;
				case TYPE_WIMAX: typeToString = "WiMax";
					break;
				case TYPE_ROAMING: typeToString = "Roam";
					break;
				case TYPE_BATTERY: typeToString = "Battery";
					break;
				case TYPE_BLUETOOTH: typeToString = "Bluetooth";
					break;
			}
			if(i > 0)
				txt += "," + typeToString;
			else 
				txt += typeToString;
			txt += "," + (sample.startTimestamp);
			txt += "," + (sample.endTimestamp - sample.startTimestamp);	
			txt += "," + sample.signal;	
			txt += ",";
			if (sample.id != null)
				txt += sample.id;
		}
		return txt;
	}

	public class AccessPointSample{
		public long startTimestamp, endTimestamp;
		public String id;
		int type = 0;
		public int signal;
		
		public AccessPointSample (int _type, long _startTimestamp, long _endTimestamp, String _id, int signal) {
			type = _type;
			startTimestamp = _startTimestamp;
			endTimestamp = _endTimestamp;
			id = _id;
		}		
	}
}

package com.cortxt.com.mmcextension.datamonitor;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.DataObjects.beans.DataStatsBean;
import com.cortxt.app.mmcutility.DataObjects.beans.RunningAppsBean;
import com.cortxt.app.mmcutility.DataObjects.database.DataMonitorDBReader;
import com.cortxt.app.mmcutility.DataObjects.database.DataMonitorDBWriter;
//import com.datamonitor.SMSObserver;

/**
 * 
 * StatsManager, is the class that manages several statistics such as
 * Screen ON & OFFs, GPS turn ON & OFFs, Phone talk BEGIN & ENDs, etc.
 * Prepares a string that represents all the statistics in a specific format.
 * 
 * @author Dileep | Paradigm Creatives
 *
 */
public class StatsManager {


	// Wifi State member variables
	public enum WifiState{ON, OFF};
	// Roaming State member variables
	public enum RoamingState{ON, OFF};

	// Screen State member variables
	public enum ScreenState{ON, OFF};
	// GPS member variables
	public enum GPSState{ON, OFF};
	// PhoneCall/TalkTime member variables
	public enum PhoneCallState{BEGIN, END};
	// Battery Charging Time -- member variables
	public enum BatteryChargeState{ON, OFF};

	/**
	 * Constructor used to instantiate the StatsManager class
	 */
	public StatsManager(Context context) {
	}	
	
	public StatsManager getStatsManager()  {
		return this;
	}
	private long lastscan = 0;
	/**
	 * Starts monitoring statistics by initializing Thread and Runnable
	 */
	@SuppressWarnings("static-access")
	public void startScan(){
	}
	/**
	 * Starts monitoring statistics by initializing Thread and Runnable
	 */
	@SuppressWarnings("static-access")
	public void startMonitoring() {
	}
	
	
	public void startFirstBucket() {
	}
	

	public void setWifiState(WifiState currentwifiState, boolean bCloseBucket){		
	}
	
	public void setRoamingState(RoamingState currentRoamingState, boolean bCloseBucket){
	}

	/**
	 * Setter method to set the state of the screen i.e. whether screen is ON or OFF
	 * 
	 * @param currentScreenState State of the screen, which can be either ON or OFF from
	 *  the enumeration ScreenState
	 */
	public void setScreenState(ScreenState currentScreenState, boolean bCloseBucket){
	}	
	
	/**
	 * Setter method to set the state of the GPS i.e. whether GPS is turned ON or OFF
	 * 
	 * @param currentGPSState State of the GPS, which can be either turned ON or OFF from
	 *  the enumeration GPSState
	 */
	public void setGPSState(GPSState currentGPSState, boolean bCloseBucket){
	}
	
	/**
	 * Setter method to set the state of the PhoneCalll i.e. whether PhoneCall BEGINs or ENDs
	 * 
	 * @param currentPhoneCallState State of the PhoneCall, which can be either BEGIN or END from
	 * setPhoneCallState the enumeration PhoneCallState
	 */
	public void setPhoneCallState(PhoneCallState currentPhoneCallState, boolean bCloseBucket){
	}
	
	/**
	 * Setter method to set the state of the BatteryCharge i.e. whether Phone battery is charging or not
	 * 
	 * @param currentBatteryChargeState State of the Phone Battery charging, which can be either ON or OFF from
	 *  the enumeration BatteryChargeState
	 */
	public void setBatteryChargeState(BatteryChargeState currentBatteryChargeState, boolean bCloseBucket){
	} 
	
	/**
	 * Increments the HandOffs count
	 */
	public void incrementHandOffsCount(){
	}	


	public String getForegroundApp ()
	{
		return null;
	}

	public String getRunningAppsString() {

		return "";
	}
}

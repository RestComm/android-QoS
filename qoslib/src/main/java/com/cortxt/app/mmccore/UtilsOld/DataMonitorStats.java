package com.cortxt.app.mmccore.UtilsOld;

import android.content.Context;

import com.cortxt.com.mmcextension.datamonitor.StatsManager;
import com.cortxt.com.mmcextension.datamonitor.StatsManager.BatteryChargeState;
import com.cortxt.com.mmcextension.datamonitor.StatsManager.GPSState;
import com.cortxt.com.mmcextension.datamonitor.StatsManager.PhoneCallState;
import com.cortxt.com.mmcextension.datamonitor.StatsManager.RoamingState;
import com.cortxt.com.mmcextension.datamonitor.StatsManager.ScreenState;
import com.cortxt.com.mmcextension.datamonitor.StatsManager.WifiState;

public class DataMonitorStats {
	
	private StatsManager mStatsManager;
	private WifiState wifiState;			
	private RoamingState  roamingState;
	private ScreenState  sceenState;
	private GPSState  gpsState;
	private PhoneCallState  phoneCallState;
	private BatteryChargeState  batteryChargeState;
	private Context mContext;
	
	public DataMonitorStats (Context context) {		
		mContext = context;
		mStatsManager = new StatsManager(context);
	}
	
	public void setWifi(boolean wifi) {
		wifiState = wifi ? WifiState.ON : WifiState.OFF;
		mStatsManager.setWifiState(wifiState, false);
	}
	
	public WifiState getWifi() {
		return wifiState;
	}
	
	public void setRoaming(boolean roaming) {
		roamingState = roaming ? RoamingState.ON : RoamingState.OFF;
		mStatsManager.setRoamingState(roamingState, false);
	}
	
	public RoamingState getRoaming() {
		return roamingState;
	}
	
	public void setScreen(boolean screen) {
		sceenState = screen ? ScreenState.ON : ScreenState.OFF;
		mStatsManager.setScreenState(sceenState, false);
	}
	
	public ScreenState getScreen() {
		return sceenState;
	}
	
	public void setGps(boolean gps) {
		gpsState = gps ? GPSState.ON : GPSState.OFF;
		mStatsManager.setGPSState(gpsState, false);
	}
	
	public GPSState getGps() {
		return gpsState;
	}
	
	public void setPhone(boolean phone) {
		phoneCallState = phone ? PhoneCallState.BEGIN : PhoneCallState.END;
		mStatsManager.setPhoneCallState(phoneCallState, false);
	}
	
	public PhoneCallState getPhone() {
		return phoneCallState;
	}
	
	public void setBattery(boolean battery) {
		batteryChargeState = battery ? BatteryChargeState.ON : BatteryChargeState.OFF;
		mStatsManager.setBatteryChargeState(batteryChargeState, false);
	}
	
	public BatteryChargeState getBattery() {
		return batteryChargeState;
	}

	public void handoff() {
		mStatsManager.incrementHandOffsCount();
	}	
	
	public void prepareAllStatistics() {
		//mStatsManager.fillSMSStatistics();
	}
	
	public void monitor() {
		mStatsManager.startMonitoring();
	}
	
	public String getRunningAppsString() {
		return mStatsManager.getRunningAppsString();
	}
	
	public void firstBucket() {
		mStatsManager.startFirstBucket(/*mContext, getWifi(), getRoaming(), getScreen(), getGps(), getPhone() , getBattery()*/);		
	}	
	
	public void scanApps() {
		mStatsManager.startScan();
	}

	public String getForegroundAppName () {return mStatsManager.getForegroundApp();}
}

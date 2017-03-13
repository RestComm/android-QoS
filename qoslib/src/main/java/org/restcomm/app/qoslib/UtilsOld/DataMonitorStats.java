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

package org.restcomm.app.qoslib.UtilsOld;

import android.content.Context;
import android.os.Handler;

import org.restcomm.app.mmcextension.datamonitor.StatsManager;
import org.restcomm.app.mmcextension.datamonitor.StatsManager.BatteryChargeState;
import org.restcomm.app.mmcextension.datamonitor.StatsManager.GPSState;
import org.restcomm.app.mmcextension.datamonitor.StatsManager.PhoneCallState;
import org.restcomm.app.mmcextension.datamonitor.StatsManager.RoamingState;
import org.restcomm.app.mmcextension.datamonitor.StatsManager.ScreenState;
import org.restcomm.app.mmcextension.datamonitor.StatsManager.WifiState;

public class DataMonitorStats {
	
	private StatsManager mStatsManager;
	//private SMSObserver smsObserver;
	private WifiState wifiState;			
	private RoamingState  roamingState;
	private ScreenState  sceenState;
	private GPSState  gpsState;
	private PhoneCallState  phoneCallState;
	private BatteryChargeState  batteryChargeState;
	private Context mContext;
	
	public DataMonitorStats (Context context, Handler handler) {
		mContext = context;
		//this.smsObserver = smsObserver;
		mStatsManager = new StatsManager(context, handler);
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
	
	public void setBattery(boolean battery, Integer percent) {
		batteryChargeState = battery ? BatteryChargeState.ON : BatteryChargeState.OFF;
		mStatsManager.setBatteryChargeState(batteryChargeState, false, percent);

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
		//if (smsObserver != null)
		//	smsObserver.verifyQueuedSMSesStatus();
		mStatsManager.startMonitoring();
	}
	
	public String getRunningAppsString(boolean bSend) {
		return mStatsManager.getRunningAppsString(bSend);
	}

	public String getStatsString ()
	{
		return mStatsManager.getStatsString();
	}
	public void cleanupStatsDB ()
	{
		mStatsManager.cleanupStatsDB ();
	}
	
	public void firstBucket() {
		mStatsManager.startFirstBucket(/*mContext, getWifi(), getRoaming(), getScreen(), getGps(), getPhone() , getBattery()*/);		
	}	
	
	public void scanApps() {
		mStatsManager.startScan();
	}
	public StatsManager getStatsManager() {
		return mStatsManager;
	}

	public String getForegroundAppName () {return mStatsManager.getForegroundApp();}
}

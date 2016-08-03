package com.cortxt.app.corelib.Services;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
//import android.media.audiofx.BassBoost.Settings;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

//import com.cortxt.app.MMC.ActivitiesOld.NerdScreen;
import com.cortxt.app.corelib.MainService;
import com.cortxt.app.corelib.R;
import com.cortxt.app.corelib.Services.Intents.IntentHandler;
import com.cortxt.app.utillib.ContentProvider.ContentValuesGenerator;
import com.cortxt.app.utillib.ContentProvider.TablesEnum;
import com.cortxt.app.corelib.Utils.RestCommManager;
import com.cortxt.app.utillib.DataObjects.EventType;
import com.cortxt.app.utillib.DataObjects.PhoneState;
import com.cortxt.app.utillib.Utils.DeviceInfoOld;
import com.cortxt.app.utillib.Utils.Global;
import com.cortxt.app.utillib.Utils.LoggerUtil;
import com.cortxt.app.utillib.Utils.PreferenceKeys;
import com.cortxt.com.mmcextension.PhoneHeuristic;
import com.cortxt.app.utillib.Utils.PreciseCallCodes;
import com.cortxt.app.utillib.Reporters.ReportManager;
import com.cortxt.app.utillib.Reporters.LocalStorageReporter.LocalStorageReporter.Events;
import com.cortxt.app.utillib.DataObjects.CellLocationEx;
import com.cortxt.app.utillib.DataObjects.SignalEx;
import com.cortxt.app.utillib.DataObjects.EventCouple;
import com.cortxt.app.utillib.DataObjects.EventObj;
import com.cortxt.app.corelib.UtilsOld.DataMonitorStats;

import com.cortxt.com.mmcextension.datamonitor.AppDataStatisticsRunnable;
import com.cortxt.app.utillib.Utils.UsageLimits;
import org.json.JSONObject;

/**
 * @author abhin
 * This is the class that MMC_Service instantiates and registers as 
 * a phone state listener for the following events
 * <ol>
 * 	<li>PhoneStateListener.LISTEN_CALL_STATE</li>
 * 	<li>PhoneStateListener.LISTEN_CELL_LOCATION</li>
 * 	<li>PhoneStateListener.LISTEN_DATA_ACTIVITY</li>
 * 	<li>PhoneStateListener.LISTEN_DATA_CONNECTION_STATE</li>
 * 	<li>PhoneStateListener.LISTEN_SERVICE_STATE</li>
 * 	<li>PhoneStateListener.LISTEN_SIGNAL_STRENGTHS</li>
 * </ol>
 */
public class LibPhoneStateListener extends PhoneStateListener {
	private final AppDataStatisticsRunnable dataActivityRunnable;
	private MainService owner;
	private DataMonitorStats dataMonitorStats;
	private RestCommManager restcommManager;
	public long tmLastCellUpdate = 0, tmLastCell = 0;
	public boolean validSignal = false;
	public static final String TAG = LibPhoneStateListener.class.getSimpleName();
	public static final int MMC_DROPPED_NOTIFICATION = 1001;
	/**
	 * If a call ends and the a cell ID was changed within this number of
	 * milliseconds in the past, then the call is flagged as a potential
	 * dropped call.
	 */
	public static final int CELL_LOCATION_EXPIRY_FOR_DROPPED_CALL = 5000;
	

	/**
	 * This variable stores a copy of the previously received network state.
	 */
	//public int previousNetworkState = -1;
	//private int previousServiceState = -1;
	//private ServiceState previousServiceStateObj = null;
	//private int previousServiceStateAirplane = 99;
	public static TelephonyManager telephonyManager;

	private Handler dataActivtyHandler;
	// keeps track of whether a global ServiceMode Panel has been manually closed by the user, its displayed during ServiceMode in debug
	public static boolean closedServicePanel = false;

//	private boolean bOffHook = false;
//	private static JSONObject mServicemode = null;
//	private String prevSvcValues = "";
//	private int networkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
//	private boolean callConnected = false, callDialing = false, callRinging = false;
//	private long timeConnected = 0, timeRinging = 0, timeDialed = 0;
//	private int lastKnownCallState;
	private PhoneState mPhoneState = null;
	/**
	 * Constructor that gets a copy of the owner object so that it can 
	 * manipulate the variables of the owner.
	 */
	public LibPhoneStateListener(MainService owner, PhoneState phonestate) {
		this.owner = owner;
		mPhoneState = phonestate;
		telephonyManager = (TelephonyManager)owner.getSystemService(Context.TELEPHONY_SERVICE);
		mPhoneState.telephonyManager = telephonyManager;
		restcommManager = new RestCommManager(this, owner);
		dataActivtyHandler = new Handler();
		dataActivityRunnable = new AppDataStatisticsRunnable(owner.getCallbacks(), dataActivtyHandler);
		mySensorManager = (SensorManager)owner.getSystemService(
				owner.SENSOR_SERVICE);

		// Proximity sensor code exists in case we want to go back to blacking out screen and forcing screen on during phone calls
        //myProximitySensor = mySensorManager.getDefaultSensor(
        //		Sensor.TYPE_PROXIMITY);

	}

	//last data caches
//	private MMCCellLocationOld lastKnownMMCCellLocation;
//	private long tmLastCell = 0;
//
//	protected ServiceState mLastServiceState;
//	protected long mLastServiceStateChangeTimeStamp =0;
//	protected long mLastDataNetworkChangeTimeStamp =0;
//
//	protected boolean mStateWasPowerOff = false;
//	private MMCSignalOld lastKnownMMCSignal, prevMMCSignal;
//	private SignalStrength lastKnownSignalStrength;
//	private long tmLastCellUpdate = 0;
//	private String lastCellString = "";

	private SensorManager mySensorManager;
	private Timer disconnectTimer = new Timer ();
	private long totalRxBytes = 0, totalTxBytes = 0;
	private String lastCellString = "";
	private SignalEx prevMMCSignal = null;

	/**
	 * When the cell location gets changed, the new cellId is added to the cell id buffer in the 
	 * owner. At the same time, the CELLCHANGE event is stored.
	 */
	@Override
	public void onCellLocationChanged(CellLocation location) {
		super.onCellLocationChanged(location);
			
		try {
            checkCDMACellSID (location);
			processNewCellLocation(new CellLocationEx(location));
			
			// See if this cellLocation has inner GsmLocation
			checkInnerGsmCellLocation (location);

			
		} catch (InterruptedException intEx){
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "onCellLocationChanged", "InterruptedException: " + intEx.getMessage());
		}
		catch (Exception ex){
			String err = ex.toString();
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "onCellLocationChanged", "InterruptedException: " + err);
		}
	}

	protected boolean proximityNear = false;
	protected boolean lastNear = false;

	SensorEventListener proximitySensorEventListener
	    = new SensorEventListener(){
		
	  @Override
	  public void onAccuracyChanged(Sensor sensor, int accuracy) {
	   
	  }
	
	  // Uses Proximity sensor during phone calls to force the screen on dim and black. 
	  // The only way to enable signal measurements during phone calls
	  @Override
	  public void onSensorChanged(SensorEvent event) {
	   // TODO Auto-generated method stub
	
	   if(event.sensor.getType()==Sensor.TYPE_PROXIMITY){
		   boolean bNearFace = event.values[0] < 1 ? true : false;
		   proximityNear = bNearFace;
		   // TODO Auto-generated method stub
		   //if (owner.bOffHook)
		   {
			   // For OS 4.1, we are able to hold the screen on during a call by coming to foreground
			//if (Build.VERSION.SDK_INT < 16)
			//		return;
			   // server can specify whether a confirmation can be invoked on a low rated potentially-dropped call
	           //int phoneScreen = PreferenceManager.getDefaultSharedPreferences(owner).getInt(PreferenceKeys.Miscellaneous.SERVERPHONESCREEN_ENABLE, 0);
       		   //if (phoneScreen == 0)
       		//	   return;
			   
			   if (bNearFace && lastNear != bNearFace)
			   {
				   LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onSensorChanged", "launch black screen");
				   //TimerTask launchConnectTask = new LaunchConnectTask("mmc");
				   //launchTimer.schedule(launchConnectTask, 1000);
				   lastNear = true;
			   }
			   else if (!bNearFace && lastNear != bNearFace)
			   {
				   LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onSensorChanged", "launch phone screen");
				   //TimerTask launchConnectTask = new LaunchConnectTask("phone");
				   //launchTimer.schedule(launchConnectTask, 1);
				   lastNear = false;
			   }
			   
		   }
	   }
	  }
    };
    
	@Override
	public void onDataActivity(int data){
		super.onDataActivity(data);
		if (owner.getUsageLimits().getDormantMode() > 0)
			return;
		String activity = null;
		try
		{
			activity = owner.getConnectionHistory().updateConnectionHistory(telephonyManager.getNetworkType(), telephonyManager.getDataState(), telephonyManager.getDataActivity(), mPhoneState.previousServiceStateObj, owner.getConnectivityManager().getActiveNetworkInfo());
		}
		catch (Exception e)
		{
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "onDataActivity", "ex " + e.getMessage());
		}
		if (data == TelephonyManager.DATA_ACTIVITY_IN || data == TelephonyManager.DATA_ACTIVITY_INOUT)
		{
			if (activity != null) {
				owner.getIntentDispatcher().updateConnection(activity, true);
			}
			//User allows - default yes
			if(PreferenceManager.getDefaultSharedPreferences(owner).getBoolean(PreferenceKeys.User.PASSIVE_SPEED_TEST, true)) {
				//Don't allow if a speedtest is in progress
				if(PreferenceManager.getDefaultSharedPreferences(owner).getBoolean(PreferenceKeys.Miscellaneous.SPEEDTEST_INPROGRESS, false)) {
					return;
				}
				if(PreferenceManager.getDefaultSharedPreferences(owner).getBoolean(PreferenceKeys.Miscellaneous.VIDEOTEST_INPROGRESS, false)) {
					return;
				}
				if (owner.getUsageLimits().getUsageProfile () == UsageLimits.MINIMAL)
					return;
				//server allows - default no
				int allow = PreferenceManager.getDefaultSharedPreferences(owner).getInt(PreferenceKeys.Miscellaneous.PASSIVE_SPEEDTEST_SERVER, 0);
				if(allow > 0) {
					dataThrougput();  
				}
			}				
		}
		else
		{
			if (activity != null) {
				owner.getIntentDispatcher().updateConnection(activity, false);
			}
		}

	}		
	
	public void dataThrougput() {
		synchronized(this) {
			totalRxBytes = TrafficStats.getTotalRxBytes();		
			totalTxBytes = TrafficStats.getTotalTxBytes();		
			if (dataActivityRunnable.hasDataActivity == 0) {
				//dataActivityRunnable.initializeHasDataActivity(1);
				dataActivityRunnable.init(totalRxBytes, totalTxBytes, true);
			}	
			else if (dataActivityRunnable.hasDataActivity == 1)
			{
				//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "onDataActivity", "in sampling");
			}
			else if (dataActivityRunnable.hasDataActivity == 2)
			{
				//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "onDataActivity", "already in download");
			}
		}
	}
	
	@Override
	public void onDataConnectionStateChanged(int state, int networkType){
		super.onDataConnectionStateChanged(state, networkType);
		//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "onDataConnectionStateChanged", String.format("Network type: %d, State: %d", networkType, state));
	
		//notify MainService of the new network type
		mPhoneState.updateNetworkType(networkType);
		
		int datastate = telephonyManager.getDataState();
		// disregard network change events if data is disabled or in airplane mode
		if (datastate == TelephonyManager.DATA_SUSPENDED || mPhoneState.previousServiceState == mPhoneState.SERVICE_STATE_AIRPLANE)
			return;
		
		if (PhoneState.ActiveConnection(owner) > 1) {// 10=Wifi, 11=Wimax, 12=Ethernet, 0=other
			mPhoneState.previousNetworkTier = -1;
			return;		
		}
		
		// Ignore any data outages that occur just after turning screen off, these are probably not to be blamed on the carrier
		if (mPhoneState.getScreenOnTime(false) + 30000 > System.currentTimeMillis())
			return;
		
		try{

			String conn = owner.getConnectionHistory().updateConnectionHistory (networkType, state, telephonyManager.getDataActivity(), mPhoneState.previousServiceStateObj, owner.getConnectivityManager().getActiveNetworkInfo());
			if (conn != null)
		   		owner.getIntentDispatcher().updateConnection(conn, false);
			
		} catch (Exception e) {}
		

		int networkGeneration = mPhoneState.getNetworkGeneration(networkType);
		
		// The 3G outage will be handled by the Service state outage
		if (mPhoneState.previousServiceState == ServiceState.STATE_OUT_OF_SERVICE || mPhoneState.previousServiceState == ServiceState.STATE_EMERGENCY_ONLY)
			return; 
		//if the network generation hasn't changed, then don't cause an event
		if (mPhoneState.previousNetworkTier == networkGeneration && mPhoneState.previousNetworkState == state){
			return;
		}


		SignalEx signal = mPhoneState.getLastMMCSignal();
		if (signal != null)
		{
			signal.setTimestamp(System.currentTimeMillis());
			mPhoneState.clearLastMMCSignal();  // to force a duplicate signal to be added
			processNewMMCSignal(signal);
		}
		//this was falsely reporting outages when screen turned off, and not coupling them to regained
		//if (datastate == TelephonyManager.DATA_DISCONNECTED)
		//	networkGeneration = 0;
		// First network state
		if (mPhoneState.previousNetworkType == -1)
			mPhoneState.previousNetworkType = networkType;
		else
		{
			switch (networkGeneration){
				case 3:	//3g
				case 4:	//3g
					stateChanged_3g(state);
					break;
				case 5:	//3g
					stateChanged_4g(state);
					break;
					
				case 1:
				case 2:	//2g
					stateChanged_2g(state);
					break;
					
				// disconnected data without disconnecting service?
				case 0:
					stateChanged_0g(state);
					break;
				
			}
		}
		//update the previous network generation and state
		if (state == TelephonyManager.DATA_CONNECTED && networkGeneration != 0)
			mPhoneState.previousNetworkTier = networkGeneration;
		// If there is truly an outage, the service state listener will update the previousNetworkTier to 0
		mPhoneState.previousNetworkState = state;
		mPhoneState.previousNetworkType = networkType;
		
	}
	public void processLastSignal ()
	{
		if (mPhoneState.lastKnownSignalStrength != null)
			onSignalStrengthsChanged(mPhoneState.lastKnownSignalStrength);
	}

	public void onVoLteServiceStateChanged (Object lteState)
	{
		if (lteState != null)
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onVoLteServiceStateChanged", lteState.toString());
		else
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onVoLteServiceStateChanged", "null");
	}

	public void onOemHookRawEvent (byte[] oemData)
	{
		if (oemData != null)
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onOemHookRawEvent", "length = " + Integer.toString(oemData.length));
		else
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onOemHookRawEvent", "null");
	}

	public void onPreciseCallStateChanged (Object preciseCallState)
	{
		if (preciseCallState != null)
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onPreciseCallStateChanged", preciseCallState.toString());
		else
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onPreciseCallStateChanged", "null");
	}

	@Override
	public void onSignalStrengthsChanged(SignalStrength signalStrength) {
		super.onSignalStrengthsChanged(signalStrength);
		if (DeviceInfoOld.getPlatform() != 1)  //Not an Android device
			return;

		if (!owner.isMMCActiveOrRunning())
		{
			mPhoneState.lastKnownSignalStrength = signalStrength;
			return;
		}
		mPhoneState.lastKnownSignalStrength = null;
		//if (signalStrength != null)
		//	MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "onSignalStrengthsChanged", signalStrength.toString());
		int pref = networkPreference(owner.getApplicationContext());
		try {
			if (mPhoneState.previousServiceState == ServiceState.STATE_IN_SERVICE || mPhoneState.previousServiceState == ServiceState.STATE_EMERGENCY_ONLY)
			{
				SignalEx mmcSignal = new SignalEx(signalStrength);
				processNewMMCSignal(mmcSignal);
				
			}
			else
			{
				SignalEx mmcSignal = new SignalEx();
				processNewMMCSignal(mmcSignal);
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			List<CellInfo> cells = telephonyManager.getAllCellInfo();
			if (cells != null)
				onCellInfoChanged(cells);
//				for (int c =0; c<cells.size(); c++)
//				{ 
//					String msg =  "cells[" + c + "]=" + cells.get(c).toString();
//					MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "onSignalStrengthsChanged", "cells[" + c + "]=" + cells.get(c).toString());
//					//Log.d(TAG, "cells[" + c + "]=" + cells.get(c).toString());
//				}
			}
			
		} catch (Exception e) {
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "onSignalStrengthsChanged", "Exception " + e.getMessage());
		}
	}

	// delay by 1 second so that it can check call log if needed to verify call connected
	class VerifyConnectTask extends TimerTask {
		
		@Override
		public void run() {
			if(DeviceInfoOld.getPlatform() == 3) {
				return;
			}
			EventCouple targetEventCouple = owner.getEventManager().getEventCouple(EventType.EVT_CONNECT, EventType.EVT_DISCONNECT);
			
			LoggerUtil.logToFile(LoggerUtil.Level.WTF, TAG, "VerifyConnectTask", "call connected=" + mPhoneState.isCallConnected() + " event=" + targetEventCouple);

            String pname = owner.getPackageName();
            int permissionForReadLogs = owner.getPackageManager().checkPermission(android.Manifest.permission.READ_LOGS, pname); //0 means allowed
            int permissionForPrecise = owner.getPackageManager().checkPermission("android.permission.READ_PRECISE_PHONE_STATE", pname); // 0 means allowed

            // For OS 4.1, need to use CALL LOG rather than logcat to determine if call connected or not
			if (targetEventCouple != null && Build.VERSION.SDK_INT >= 16 && !mPhoneState.isCallConnected())//permissionForReadLogs != 0 && permissionForPrecise != 0 ) // && !owner.isCallConnected())  //  && !owner.getUsageLimits().getUseRadioLog())
				checkCallLog();

			if (mPhoneState.lastCallDropped == true)
			{
				mPhoneState.lastCallDropped = false;
				if (targetEventCouple == null)
				{
					EventObj evt = owner.getEventManager().triggerSingletonEvent(EventType.EVT_CALLFAIL);
					popupDropped(EventType.EVT_CALLFAIL, 5, evt.getLocalID());
					evt.setCause (mPhoneState.lastDroppedCause);
					evt.setEventTimestamp(mPhoneState.disconnectTime);
				}
				else if (mPhoneState.isCallConnected() && targetEventCouple != null)
				{
					int rating = 7;
					if (mPhoneState.lastDroppedCause.equals("error_unspecified"))
						rating = 5;
					
					targetEventCouple.setStopEventType(EventType.EVT_DROP);
					owner.getEventManager().stopPhoneEvent(EventType.EVT_CONNECT, EventType.EVT_DROP);
					
					popupDropped(EventType.EVT_DROP, rating, targetEventCouple.getStopEvent().getLocalID());
					targetEventCouple.getStopEvent().setCause (mPhoneState.lastDroppedCause);
					targetEventCouple.getStopEvent().setEventTimestamp(mPhoneState.disconnectTime);
					//owner.getEventManager().updateEventDBField(targetEventCouple.getStopEvent().getUri(), Tables.Events.TIMESTAMP, Long.toString(disconnectTime));
				}
				else if (targetEventCouple != null)
				{
					//EventObj evt = owner.triggerSingletonEvent(EventType.EVT_CALLFAIL);
					targetEventCouple.setStopEventType(EventType.EVT_CALLFAIL);
					owner.getEventManager().stopPhoneEvent(EventType.EVT_CONNECT, EventType.EVT_CALLFAIL);
					popupDropped(EventType.EVT_CALLFAIL, 5, targetEventCouple.getStopEvent().getLocalID());
					targetEventCouple.getStopEvent().setCause (mPhoneState.lastDroppedCause);
					targetEventCouple.getStopEvent().setEventTimestamp(mPhoneState.disconnectTime);
					//owner.getEventManager().updateEventDBField(evt.getUri(), Tables.Events.TIMESTAMP, Long.toString(disconnectTime));
				}
				mPhoneState.setCallConnected(false);
				mPhoneState.lastCallDropped = false;
				if (!mPhoneState.bOffHook)
				{
					mPhoneState.setCallDialing(false);
				}
			}
			else if (mPhoneState.disconnectTime - mPhoneState.offhookTime < 2000 )   // if connect wasnt detected, use the time the call was dialed
			{	
				LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onCallStateChanged", "off hook too short: " + (mPhoneState.disconnectTime - mPhoneState.offhookTime));
				// If call did not connect, undo the call connect event
				if (targetEventCouple != null && targetEventCouple.getStartEvent() != null)
				{
					LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onCallStateChanged", "Undo CONNECT event");
					owner.getEventManager().unstageEvent(targetEventCouple.getStartEvent());
					owner.getEventManager().cancelCouple (targetEventCouple);
					//MainService.getGpsManager().unregisterListener(targetEventCouple.getStartEvent().gpsListener);
					//owner.getEventManager().deleteEventDB(targetEventCouple.getStartEvent().getUri(), null, null);

					mPhoneState.setCallConnected(false);
					int eventId = ReportManager.getInstance(owner).getEventId(targetEventCouple.getStartEvent().getEventTimestamp(), EventType.EVT_CONNECT.getIntValue());
  				    if (eventId != 0)
  				    	 ReportManager.getInstance(owner).deleteEvent (eventId);
  				    owner.startRadioLog (false, null, EventType.EVT_CONNECT);
				
				}
			}

		}
	}
	/**
	 * Waits 10 Seconds after a phone call is disconnected to look at signal changes to decide if a call ended normally or dropped
	 * There is often a delay before changes in signal are reported to the listener, which is why we wait before deciding
	 * This also gives a chance for other method to weigh-in, such as the logcat
	 */
	class DisconnectTimerTask extends TimerTask {
		int count = 0;
		public DisconnectTimerTask (int _count)
		{
			count = _count;
		}
		@Override
		public void run() {

			EventCouple targetEventCouple = owner.getEventManager().getEventCouple(EventType.EVT_CONNECT, EventType.EVT_DISCONNECT);
			
			LoggerUtil.logToFile(LoggerUtil.Level.WTF, TAG, "DisconnectTimerTask", "call connected=" + mPhoneState.isCallConnected() + " event=" + targetEventCouple);

			mPhoneState.heurCause = null;
			
			if(mPhoneState.isCallConnected() || mPhoneState.isCallDialing() || mPhoneState.isCallRinging())
			{
				
				int rating = 0;

				if (DeviceInfoOld.getPlatform() != 3)
				{
					PhoneHeuristic heur = new PhoneHeuristic (owner.getCallbacks(), mPhoneState);
					rating = heur.heuristicDropped(mPhoneState);
				}
				
				// Detected dropped call based on logcat cause, or proximity (phone against ear at disconnect time)
				if (mPhoneState.lastCallDropped == true)
				{
					rating = 5;
					mPhoneState.heurCause = mPhoneState.lastDroppedCause;
					if (targetEventCouple == null)
					{
						EventObj evt = owner.getEventManager().triggerSingletonEvent(EventType.EVT_CALLFAIL);
						popupDropped (EventType.EVT_CALLFAIL, rating, evt.getLocalID());
						evt.setCause (mPhoneState.lastDroppedCause);
						evt.setEventTimestamp(mPhoneState.disconnectTime);
					}
				}
				
				if (rating  > 2 && targetEventCouple != null)
				{
					LoggerUtil.logToFile(LoggerUtil.Level.WTF, TAG, "DisconnectTimerTask", "call dropped heuristic=" + mPhoneState.heurCause);
					
					//there is now a good chance that the call was dropped
					if(!mPhoneState.isCallConnected()) //  || (owner.isCallConnected() == true && owner.getTimeConnected() + 2000+count*2000 > System.currentTimeMillis()))
					{
						// failed call based on heuristic
						targetEventCouple.setStopEventType(EventType.EVT_CALLFAIL);
						owner.getEventManager().stopPhoneEvent(EventType.EVT_CONNECT, EventType.EVT_CALLFAIL);
						targetEventCouple.getStopEvent().setCause (mPhoneState.heurCause);
						targetEventCouple.getStopEvent().setEventTimestamp(mPhoneState.disconnectTime);
						targetEventCouple.getStopEvent().setEventIndex(rating); // something has to hold the confidence rating. This field will be sent to server as 'eventIndex'
						ReportManager.getInstance(owner).updateEventField(targetEventCouple.getStopEvent().getLocalID(), Events.KEY_TIER, Integer.toString(rating));
						ReportManager.getInstance(owner).updateEventField(targetEventCouple.getStopEvent().getLocalID(), "timeStamp", Long.toString(mPhoneState.disconnectTime));
					    
						LoggerUtil.logToFile(LoggerUtil.Level.WTF, TAG, "DisconnectTimerTask", "call changed to IDLE while call was dialing/ringing (CALL FAILED)");
						
						int allowConfirm = PreferenceManager.getDefaultSharedPreferences(owner).getInt(PreferenceKeys.Miscellaneous.ALLOW_CONFIRMATION, 5);
		        		if (allowConfirm > 0)
		        			popupDropped (EventType.EVT_CALLFAIL, rating, targetEventCouple.getStopEvent().getLocalID());
					}
					else if (mPhoneState.isCallConnected() && targetEventCouple != null)
					{
						LoggerUtil.logToFile(LoggerUtil.Level.WTF, TAG, "DisconnectTimerTask", "call changed to IDLE with low signal while during call (CALL DROPPED)");
						targetEventCouple.setStopEventType(EventType.EVT_DROP);
						owner.getEventManager().stopPhoneEvent(EventType.EVT_CONNECT, EventType.EVT_DROP);
						
						targetEventCouple.getStopEvent().setCause (mPhoneState.heurCause);
						targetEventCouple.getStopEvent().setEventTimestamp(mPhoneState.disconnectTime);
						targetEventCouple.getStopEvent().setEventIndex(rating); // something has to hold the confidence rating. This field will be sent to server as 'eventIndex'
						
						ReportManager.getInstance(owner).updateEventField(targetEventCouple.getStopEvent().getLocalID(), Events.KEY_TIER, Integer.toString(rating));
						ReportManager.getInstance(owner).updateEventField(targetEventCouple.getStopEvent().getLocalID(), "timeStamp", Long.toString(mPhoneState.disconnectTime));
						int allowConfirm = PreferenceManager.getDefaultSharedPreferences(owner).getInt(PreferenceKeys.Miscellaneous.ALLOW_CONFIRMATION, 5);
		        		if (allowConfirm > 0)
		        			popupDropped (EventType.EVT_DROP, rating, targetEventCouple.getStopEvent().getLocalID());
					}
					
				} 
				else if (targetEventCouple != null)
				{  
					EventType evtType = EventType.EVT_DISCONNECT;
					if (mPhoneState.isCallConnected())
						owner.getEventManager().stopPhoneEvent(EventType.EVT_CONNECT, EventType.EVT_DISCONNECT);
					else
					{
						evtType = EventType.EVT_UNANSWERED;
						targetEventCouple.setStopEventType(EventType.EVT_UNANSWERED);
						owner.getEventManager().stopPhoneEvent(EventType.EVT_CONNECT, EventType.EVT_UNANSWERED);
					}

					if (mPhoneState.heurCause == null || mPhoneState.heurCause.length() == 0)
						targetEventCouple.getStopEvent().setCause ("IDLE");
					else
						targetEventCouple.getStopEvent().setCause (mPhoneState.heurCause);
					targetEventCouple.getStopEvent().setEventIndex(rating);
					targetEventCouple.getStopEvent().setEventTimestamp(mPhoneState.disconnectTime);
					ReportManager.getInstance(owner).updateEventField(targetEventCouple.getStopEvent().getLocalID(), Events.KEY_TIER, Integer.toString(rating));
					ReportManager.getInstance(owner).updateEventField(targetEventCouple.getStopEvent().getLocalID(), "timeStamp", Long.toString(mPhoneState.disconnectTime));
				    
					//owner.getEventManager().updateEventDBField(targetEventCouple.getStopEvent().getUri(), Tables.Events.TIMESTAMP, Long.toString(disconnectTime));

					// server can specify whether a confirmation can be invoked on a low rated potentially-dropped call
	            	int allowConfirm = PreferenceManager.getDefaultSharedPreferences(owner).getInt(PreferenceKeys.Miscellaneous.ALLOW_CONFIRMATION, 5);
	        		if (allowConfirm > 0)
	        			popupDropped (evtType, rating, targetEventCouple.getStopEvent().getLocalID());
				}
			}
			mPhoneState.setCallConnected(false);
			mPhoneState.lastCallDropped = false;
			if (!mPhoneState.bOffHook)
			{
				mPhoneState.setCallDialing (false);
			}
			else
				phoneOffHook (TelephonyManager.CALL_STATE_OFFHOOK);
			//owner.startRadioLog (false, null);
		}
	}

	/*
	*  Called when phone state is Off-Hook (dialing out) or ringing (incoming call)
	 */
	public void phoneOffHook (int iPhoneState)
	{
		try
		{
			final EventObj event = owner.getEventManager().startPhoneEvent(EventType.EVT_CONNECT, EventType.EVT_DISCONNECT);
			if (event != null)
			{
				if (mPhoneState.bOffHook)
				{
					if (iPhoneState == TelephonyManager.CALL_STATE_OFFHOOK && (event.getFlags() & EventObj.CALL_INCOMING) > 0)
						mPhoneState.setCallConnected(true);
	//				if (iPhoneState == TelephonyManager.CALL_STATE_RINGING && (event.getFlags() & EventObj.CALL_INCOMING) == 0)
	//					setCallWaiting(true);
					return;
				}
				owner.startRadioLog (true, "call", EventType.EVT_CONNECT); // "monitoring signal strength");
				if (iPhoneState == TelephonyManager.CALL_STATE_RINGING)
				{
					event.setFlag(EventObj.CALL_INCOMING, true);
					mPhoneState.setCallRinging(true);
				}
				else
				{
					mPhoneState.setCallDialing(true); // in case it is an outgoing call (not sure), dialing time will start now
					mPhoneState.setCallRinging(false); // in case it is an outgoing call (not sure), dialing time will start now
				}
			}
			mPhoneState.bOffHook = true;
			mPhoneState.offhookTime = System.currentTimeMillis();

			mPhoneState.lastCallDropped = false;
			mPhoneState.lastDroppedCause = null;

			Intent intent = new Intent(IntentHandler.PHONE_CALL_CONNECT);
			owner.sendBroadcast(intent);

			// Delay for a few seconds and then check the voice network to detect if we have a VoLTE call
			owner.handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					int tech = mPhoneState.getVoiceNetworkType ();
					if (tech == mPhoneState.NETWORK_NEWTYPE_LTE && event != null) {
						event.setFlag (EventObj.CALL_VOLTE, true);
						LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "getRilVoiceRadioTechnology", "VOLTE CALL DETECTED");
					}
					else if ((tech <= 0 || tech == mPhoneState.NETWORK_NEWTYPE_IWLAN) && event != null)
					{
						event.setFlag (EventObj.CALL_OFFNET, true);
						LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "getRilVoiceRadioTechnology", "WIFI CALL DETECTED?");
					}
					//boolean isInCall = IsInCall ();
				}
			}, 3500);

			// Set all ongoing events as occurring in a call
			List<EventObj> ongoingEvents = owner.getEventManager().getOngoingEvents();
			int i;
			for (i = 0; i < ongoingEvents.size(); i++) {
				ongoingEvents.get(i).setFlag(EventObj.PHONE_INUSE, true);
			}
		}
		catch (Exception e)
		{
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "phoneOffHook", "exception", e);
		}
	}

//	private boolean IsInCall ()
//	{
//		TelecomManager telecomManager = (TelecomManager)owner.getSystemService(Context.TELECOM_SERVICE);
//		boolean incall = telecomManager.isInCall();
//		return incall;
//	}


	// Listener for connected and disconnected phone calls
	// Android Detects only on-hook and off-hook. To better detect, it starts timer tasks
	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		super.onCallStateChanged(state, incomingNumber);

		try
		{
			Intent intent;

			switch (state){
				case TelephonyManager.CALL_STATE_IDLE:  
					LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onCallStateChanged", "IDLE");

					if (mPhoneState.bOffHook == false)
					{	
						LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onCallStateChanged", "not off hook");
						return;
					}
					mPhoneState.disconnectTime = System.currentTimeMillis();
					mPhoneState.bOffHook = false;

				HashMap<String, Integer> handset = ReportManager.getHandsetCaps (owner);
				// If phone needs heuristics, check the signal for a dropped call
				int heurDelay = 9;
				if (handset.containsKey("capHeurDelay") )
					heurDelay = handset.get("capHeurDelay");
				if (DeviceInfoOld.getPlatform() == 3)
					heurDelay = 2;

					mPhoneState.bOffHook = false;
				TimerTask verifyConnectTask = new VerifyConnectTask();
				disconnectTimer.schedule(verifyConnectTask, 2000); // 1300
				TimerTask disconnectTimerTask1 = new DisconnectTimerTask(1);
				disconnectTimer.schedule(disconnectTimerTask1, heurDelay*1000);	
				intent = new Intent(IntentHandler.PHONE_CALL_DISCONNECT);
				owner.sendBroadcast(intent);
				if (disconnectLatch != null)
					disconnectLatch.countDown();
				
				
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onCallStateChanged", "OFFHOOK");
				//if (owner.bOffHook)
				//	return;

				phoneOffHook (TelephonyManager.CALL_STATE_OFFHOOK);
				//intent = new Intent(MMCIntentHandlerOld.PHONE_CALL_CONNECT);
				//owner.sendBroadcast(intent);
				if (connectLatch != null)
					connectLatch.countDown();
				
				//TimerTask launchConnectTask = new LaunchConnectTask();
				//disconnectTimer.schedule(launchConnectTask, 2000);
				break;
				
			case TelephonyManager.CALL_STATE_RINGING:
				LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onCallStateChanged", "RINGING");
				if (mPhoneState.bOffHook)
					return;
				phoneOffHook (TelephonyManager.CALL_STATE_RINGING);
				//if (incomingNumber != null && incomingNumber.length() > 1)
				//	txtIncomingNumber = incomingNumber;

				break;
			}					
		}
		catch (Exception e)
		{
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onCallStateChanged", "Exception", e);
		}
	}
	
	CountDownLatch connectLatch, disconnectLatch;
	
	public boolean waitForConnect ()
	{
		connectLatch = new CountDownLatch(1);
		try {
			boolean res = connectLatch.await (30,TimeUnit.SECONDS);
			boolean b = res;
			return res;
		} catch (InterruptedException e) {
			//if (connectLatch.getCount() <= 0)
			//	return true;
			return false;
		}
	}
	public boolean waitForDisconnect ()
	{
		disconnectLatch = new CountDownLatch(1);
		try {
			return disconnectLatch.await (50,TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			//if (connectLatch.getCount() <= 0)
			//	return true;
			return false;
		}
	}

	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
	public boolean isAirplaneModeOn(Context context) {

		//int disp = Settings.System.getInt(context.getContentResolver(), 
        //        Settings.System.SCREEN_OFF_TIMEOUT, 0) ;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
	        return Settings.System.getInt(context.getContentResolver(), 
	                Settings.System.AIRPLANE_MODE_ON, 0) != 0;          
	    } else {
	        return Settings.Global.getInt(context.getContentResolver(), 
	                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
	    }
	    
	}
	
	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
	public int networkPreference(Context context) {
		
		String pref = null;
		ConnectivityManager con = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		int attemptTwo = con.getNetworkPreference();

	    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
	    	return Settings.System.getInt(context.getContentResolver(), 
	    			Settings.System.NETWORK_PREFERENCE, 0);      
	    } else {
	    	   return Settings.Global.getInt(context.getContentResolver(), 
		                Settings.Global.NETWORK_PREFERENCE, 0) ;
	    }       
	}
	
	
	@Override
	public void onServiceStateChanged(ServiceState serviceState) {
		super.onServiceStateChanged(serviceState);	
		if (serviceState == null)
			return;

		boolean isRoaming = serviceState.getRoaming();
		String operator = serviceState.getOperatorAlphaLong();
		String mccmnc = serviceState.getOperatorNumeric();
		
		//owner.getConnectionHistory().updateConnectionHistory(cellnettype, state, activity, networkInfo)
		try{
		String activity = owner.getConnectionHistory().updateConnectionHistory(telephonyManager.getNetworkType(), telephonyManager.getDataState(), telephonyManager.getDataActivity(), serviceState, owner.getConnectivityManager().getActiveNetworkInfo());
		if (activity != null)
	   		owner.getIntentDispatcher().updateConnection(activity, false);
		} catch (Exception e)
		{
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "onServiceStateChanged", "exception with updateConnectionHistory:", e);
		}

		//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "onServiceStateChanged", String.format("State: %s, roaming: %s, operator: %s, mccmnc: %s",
		//			serviceState, isRoaming, operator, mccmnc));
		//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "onServiceStateChanged", "Reflected: " + listServiceStateFields(serviceState));

		boolean wasRoaming = PreferenceManager.getDefaultSharedPreferences(owner).getBoolean(PreferenceKeys.Miscellaneous.WAS_ROAMING, false);
		
		//If roaming, track time spend doing this			
		if (mPhoneState.isRoaming() != wasRoaming) {
			int roamValue = 2; //off
			String status = "off";
			if(mPhoneState.isRoaming()) {
				roamValue = 1; //on
				status = "on";
				//For DataMonitor tracking
				Intent intent = new Intent(IntentHandler.ROAMING_ON);
				owner.sendBroadcast(intent);
			}
			else {
				Intent intent = new Intent(IntentHandler.ROAMING_OFF);
				owner.sendBroadcast(intent);
			}
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onServiceStateChanged", "roaming status: " + status);
			owner.trackAccessPoints(roamValue);
			owner.getEventManager().triggerUpdateEvent(false, false);
			PreferenceManager.getDefaultSharedPreferences(owner).edit().putBoolean(PreferenceKeys.Miscellaneous.WAS_ROAMING, mPhoneState.isRoaming()).commit();
		}		
		
		//If wimax, track time spend doing this
		if(PhoneState.ActiveConnection(owner) == 12) {
			Intent intent = new Intent(IntentHandler.WIMAX_STATE_CHANGE);
			owner.sendBroadcast(intent);
		}
			
		//in airplane mode
		if(isAirplaneModeOn(owner.getApplicationContext()) == true){
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onServiceStateChanged", "airplane mode on");
			mPhoneState.previousServiceState = mPhoneState.SERVICE_STATE_AIRPLANE;
			try {
				SignalEx mmcSignal = new SignalEx();
				processNewMMCSignal(mmcSignal);
			} catch (Exception e) {
			}
			return;
		}		

		if(serviceState.getState() == ServiceState.STATE_IN_SERVICE) {
			if(mPhoneState.previousServiceState != ServiceState.STATE_IN_SERVICE) {
				
				//state changed from OUT_OF_SERVICE to IN_SERVICE
				LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onServiceStateChanged", "trigger regain service");
				owner.getEventManager().stopPhoneEvent(EventType.COV_VOD_NO, EventType.COV_VOD_YES);
				mPhoneState.mLastServiceStateChangeTimeStamp = System.currentTimeMillis();

			}
		}
		else if(serviceState.getState() == ServiceState.STATE_OUT_OF_SERVICE) {// || serviceState.getState() == ServiceState.STATE_EMERGENCY_ONLY) {
			if(mPhoneState.previousServiceState == mPhoneState.SERVICE_STATE_AIRPLANE)
				return;  // discard 'no-service' occurring after exiting airplane mode
			
			if(mPhoneState.previousServiceState == ServiceState.STATE_IN_SERVICE){ // && previousServiceState != SERVICE_STATE_AIRPLANE) {

				mPhoneState.previousServiceState = serviceState.getState();
				SignalEx signal = mPhoneState.getLastMMCSignal();
				processNewMMCSignal(signal);

				// Outage needs to last longer than 5 seconds to actually trigger
				int delay = 5000;
				if (mPhoneState.isCallConnected() || mPhoneState.disconnectTime + 12000 > System.currentTimeMillis())
					delay = 1;  // If phone call is connected (or recently disconnected), no delay, dont ignore short outages
				owner.handler.postDelayed(new Runnable() {
					  @Override
					  public void run() {
						  // If longer outage after 2 seconds, do nothing
						  if (mPhoneState.previousServiceState != ServiceState.STATE_OUT_OF_SERVICE) //  && previousServiceState != ServiceState.STATE_EMERGENCY_ONLY)
						  {
							  LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onServiceStateChanged", "Outage lasted < 5 sec, ignoring");
							  return;
						  }
						    // Officially an outage now
							// If service dropped straight from 3G to nothing, trigger a 3G outage as well
							// If was connected to wifi when service was lost, does not count as a 3G outage
							if (PhoneState.ActiveConnection(owner) <= 1 && mPhoneState.previousNetworkTier >= 3) // 10=Wifi, 11=Wimax, 12=Ethernet, 0=other
							{			
								owner.getEventManager().startPhoneEvent(EventType.COV_3G_NO, EventType.COV_3G_YES);
								if (mPhoneState.previousNetworkTier >= 5)
									owner.getEventManager().startPhoneEvent(EventType.COV_4G_NO, EventType.COV_4G_YES);
							}
						  mPhoneState.mLastServiceStateChangeTimeStamp = System.currentTimeMillis();
							
							LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onServiceStateChanged", "trigger lost service");
							//state changed from IN_SERVICE to OUT_OF_SERVICE 
							owner.getEventManager().startPhoneEvent(EventType.COV_VOD_NO, EventType.COV_VOD_YES);
							if (mPhoneState.previousNetworkTier >= 2)
								owner.getEventManager().startPhoneEvent(EventType.COV_DATA_NO, EventType.COV_DATA_YES);
							SignalEx signal = mPhoneState.getLastMMCSignal();
						  	processNewMMCSignal(signal);
						    mPhoneState.previousNetworkTier = 0;
							//previousNetworkState = 0;
					  }
					}, delay);
					
			}
		}
		mPhoneState.previousServiceState = serviceState.getState();
		mPhoneState.previousServiceStateObj = serviceState;
		
	}
	

	public void onPreciseCallState (PreciseCallCodes preciseCall)
	{
		int state = preciseCall.getRingingCallState();
		int fstate = preciseCall.getForegroundCallState();
		if (preciseCall.getDisconnectCause() != -1)
			onDisconnect("", preciseCall.getDisconnectCauseString());
		else if (fstate == PreciseCallCodes.PRECISE_CALL_STATE_DIALING ||
				fstate == PreciseCallCodes.PRECISE_CALL_STATE_ALERTING ||
				fstate == PreciseCallCodes.PRECISE_CALL_STATE_ACTIVE)
			onConnect ("", preciseCall.getForegroundCallStateString());
		owner.getConnectionHistory().updatePreciseCallHistory (preciseCall);
	}

	public void onServiceMenu ( String _timestamp, String values, String name) {
		//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "onServiceMenu", name);
		if (!closedServicePanel && owner.isMonitoringActive())
			MainService.updateSvcPanel(owner, values, name);
	}

	private long tmSvcUpdate = 0;
	public void onServiceMode ( String _timestamp, JSONObject servicemode, String values, String name) {
		//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "onServiceMode", servicemode.toString());
		if (!closedServicePanel && owner.isMonitoringActive())
			MainService.updateSvcPanel(owner, values, name);

		if (tmSvcUpdate + 2000 > System.currentTimeMillis())
			return;
		tmSvcUpdate = System.currentTimeMillis();

		try {
			if (name.equals("BASIC")) {
				if (!mPhoneState.prevSvcValues.equals(values)) {
					boolean bCellChanged = false, bNeighbors = false;

					SignalEx signal = mPhoneState.getLastMMCSignal();
					long timestamp = System.currentTimeMillis();

					if (mPhoneState.mServicemode != null) {
						if (servicemode.has("psc") && mPhoneState.mServicemode.has("psc") && !servicemode.getString("psc").equals(mPhoneState.mServicemode.getString("psc")))
							bCellChanged = true;
						if (servicemode.has("pci") && mPhoneState.mServicemode.has("pci") && !servicemode.getString("pci").equals(mPhoneState.mServicemode.getString("pci")))
							bCellChanged = true;
					} else
						bCellChanged = true;
					mPhoneState.mServicemode = servicemode;
					mPhoneState.prevSvcValues = values;
					servicemode.put("time", timestamp);
					if (signal != null) {
						signal.setTimestamp(timestamp);
						mPhoneState.clearLastMMCSignal();  // to force a duplicate signal to be added
						this.processNewMMCSignal(signal);
					}
					if (bCellChanged == true) {
						CellLocationEx cell = getLastCellLocation();
						if (cell != null) {
							cell.setCellIdTimestamp(timestamp);
							clearLastCellLocation();
							this.processNewCellLocation(cell);
						}
					}
					// if event is in progress, update it with service mode values

				}
			}
			else if (name.equals("NEIGHBOURS")) {
				if (servicemode.has("neighbors")) {
					String neighbors = owner.getCellHistory().updateNeighborHistory(servicemode.getJSONArray("neighbors"));
					if (neighbors != null && neighbors.length() > 2)
						owner.getIntentDispatcher().updateNeighbors(neighbors);
				}
			}

			owner.getConnectionHistory().updateServiceModeHistory(values, name);
		}
		catch(Exception x){
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "onServiceMode", "exception", x);
		}
	}
	/*
	*  Precise Information about phone-call-disconnect cause, if available from logcat or other priviledged means
	 */
	public void onDisconnect ( String _timestamp, String _cause)
	{
		_cause = _cause.trim();
		LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onDisconnect", _cause);
		if (!mPhoneState.isCallDialing() && !mPhoneState.isCallConnected())
		{
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onDisconnect", "ignoring because no call was dialing or connected");
			return;
		}
		HashMap<String, Integer> handsetcaps = ReportManager.getHandsetCaps (owner);
		int useDropCause = 2, useFailedCause = 2;
		int causeCode = 1;
		String cause = _cause;
		if (_cause.startsWith("FAIL") || _cause.startsWith("CAUSE"))
		{
			int space = _cause.indexOf (" ");
			String[] causes = _cause.substring(space+1).split(",");
			if (causes != null && causes.length > 0 && causes[0].length() > 0)
			{
				try{
					causeCode = Integer.parseInt(causes[0].trim(), 10);
					LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onDisconnect", "cause code: " + causeCode);
				}catch (Exception e){
					LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onDisconnect", "error parse cause code from: " + _cause);
				}
				if (causeCode == 65535 || causeCode == 0)
				{
					cause = "err_" + causeCode;//unspecified";
					causeCode = 1;
				}
			}
		}
		mPhoneState.lastDroppedCause = _cause;


		if (cause.equalsIgnoreCase("congestion") || cause.equalsIgnoreCase("call_drop") || (causeCode> 31 && causeCode != 510) || cause.equalsIgnoreCase("lost_signal") ||
				cause.equalsIgnoreCase("cdma_drop") || cause.equalsIgnoreCase("out_of_service") || cause.equalsIgnoreCase("icc_error"))
		{
			mPhoneState.lastCallDropped = true;
			mPhoneState.lastDroppedCause = _cause;
		}
		else if (cause.equalsIgnoreCase("error_unspecified") || cause.indexOf("err_") == 0)
		{
			//boolean bUseCause = true;
			if (handsetcaps.containsKey("capDropCause"))
				useDropCause = handsetcaps.get("capDropCause");

			if (handsetcaps.containsKey("capFailedCause"))
				useFailedCause = handsetcaps.get("capFailedCause");
			if ((mPhoneState.isCallConnected() && useDropCause != 0) ||
					(!mPhoneState.isCallConnected() && (useFailedCause != 0 && !mPhoneState.callRinging)))
			{
				mPhoneState.lastCallDropped = true;
				mPhoneState.lastDroppedCause = _cause;
			}
			else
			{
				if (!mPhoneState.isCallConnected())
				{
					if (mPhoneState.callRinging)
					{
						LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onDisconnect", "unspecified cause not considered failed because it rang (call may have been rejected)");
						mPhoneState.lastCallDropped = true;
						mPhoneState.lastDroppedCause = _cause;
					}
					else if (useFailedCause == 0)
						LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onDisconnect", "unspecified cause not considered failed because handset doesnt support unspecified failed cause");
				}
				else if (useDropCause == 0)
					LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onDisconnect", "unspecified cause not considered dropped because handset doesnt support unspecified dropped cause");
				else
					LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onDisconnect", "unspecified cause not considered dropped or failed for unknown reason");

			}
		}
		else
			_cause = "";
	}

	/*
	*  Precise Information about phone-call-connect states, if available from logcat or other priviledged means
	 */
	public void onConnect ( String _timestamp, String _state)
	{
		//start a phone connected event
		LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onConnect", _state);
		mPhoneState.lastDroppedCause = _state;

		if(_state.equalsIgnoreCase("active"))
		{
			if (mPhoneState.bOffHook == true)
			{
				if (mPhoneState.callConnected == false) {
					mPhoneState.setCallConnected(true);
					mPhoneState.timeConnected = System.currentTimeMillis();
					mPhoneState.lastCallDropped = false;
					mPhoneState.callDialing = false;
					//start a phone connected event
					EventCouple targetEventCouple = owner.getEventManager().getEventCouple(EventType.EVT_CONNECT, EventType.EVT_DISCONNECT);
					if (targetEventCouple != null && targetEventCouple.getStartEvent() != null) {
						targetEventCouple.getStartEvent().setEventTimestamp(System.currentTimeMillis());
						//start a phone connected event
						long connectDuration = 0;
						// The duration on the connected Call event will represent the time it took the call to begin ringing
						if (mPhoneState.callRinging = true && mPhoneState.timeRinging > mPhoneState.timeDialed && mPhoneState.timeDialed > 0 && mPhoneState.timeDialed > mPhoneState.timeRinging - 100000)
							connectDuration = mPhoneState.timeRinging - mPhoneState.timeDialed;
						connectDuration = mPhoneState.timeConnected - mPhoneState.timeDialed;
						targetEventCouple.getStartEvent().setConnectTime((int) connectDuration);
					}
				}
				else
				{
					LoggerUtil.logToFile(LoggerUtil.Level.WTF, TAG, "onConnect", "call active but already connected");
				}

				//startPhoneEvent(EventType.EVT_CONNECT, EventType.EVT_DISCONNECT);
			}
			else
			{
				mPhoneState.callDialing = false;
				LoggerUtil.logToFile(LoggerUtil.Level.WTF, TAG, "onConnect", "call active but not offhook");
			}
			mPhoneState.callRinging = false;
		}

		if (mPhoneState.bOffHook == true && mPhoneState.callRinging == false && mPhoneState.callConnected == false &&
				(_state.equalsIgnoreCase("dialing") || _state.equalsIgnoreCase("alerting")))
		{

			if (_state.equalsIgnoreCase("dialing") && mPhoneState.callDialing == false)
			{
				mPhoneState.callDialing = true;
				mPhoneState.timeDialed = System.currentTimeMillis();
			}
			if (_state.equalsIgnoreCase("alerting") && mPhoneState.callRinging == false)
			{
				mPhoneState.callRinging = true;
				mPhoneState.timeRinging = System.currentTimeMillis();
			}
		}

	}

	/*
	 * Check the Android call log after phone hangs up to see if and when a phone call began and ended
	 * This is only needed in Android 4.1 because they removed permission to access the radio logcat
	 * Android 4.1 requires a new permission called PERMISSION_READ_CALL_LOG
	 */
	public EventObj checkCallLog ()
	{
		EventCouple targetEventCouple = owner.getEventManager().getEventCouple(EventType.EVT_CONNECT, EventType.EVT_DISCONNECT);
		LoggerUtil.logToFile(LoggerUtil.Level.WTF, TAG, "VerifyConnectTask", "checkCallLog: targetEventCouple" + targetEventCouple);

		if (targetEventCouple == null || targetEventCouple.getStartEvent() == null )
			return null;
		EventObj connectEvent = targetEventCouple.getStartEvent();
		String[] strFields = {
				android.provider.CallLog.Calls.NUMBER,
				android.provider.CallLog.Calls.TYPE,
				android.provider.CallLog.Calls.DATE,
				android.provider.CallLog.Calls.DURATION
		};
		String strOrder = android.provider.CallLog.Calls.DATE + " DESC LIMIT 1";

		try
		{
			Cursor callCursor = owner.getContentResolver().query(
					android.provider.CallLog.Calls.CONTENT_URI,
					strFields,
					null,
					null,
					strOrder
			);


			if (callCursor != null && callCursor.moveToFirst())
			{
				String number = callCursor.getString(0);
				int type = callCursor.getInt(1);

				LoggerUtil.logToFile(LoggerUtil.Level.WTF, TAG, "checkCallLog", "type: " + type);

				if (type != android.provider.CallLog.Calls.MISSED_TYPE)
				{
					mPhoneState.setCallConnected(true);
					long callDate = callCursor.getLong(2);
					int callDuration = callCursor.getInt(3);
					long callEnd = callDate + (long)callDuration;

					LoggerUtil.logToFile(LoggerUtil.Level.WTF, TAG, "checkCallLog", "callDate: " + callDate + ", duration: " + callDuration);

					//if (callEnd < System.currentTimeMillis() + 5000)
					if (callDate >= connectEvent.getEventTimestamp()-12000 && callDuration > 0)
					{
						mPhoneState.timeConnected = System.currentTimeMillis() - 2000 - callDuration*1000; // callDate;
						connectEvent.setEventTimestamp(mPhoneState.timeConnected);
						long connectDuration = mPhoneState.timeConnected - mPhoneState.timeDialed;
						targetEventCouple.getStartEvent().setConnectTime((int) connectDuration);
						//eventManager.updateEventDBField(connectEvent.getUri(), Tables.Events.TIMESTAMP, Long.toString(timeConnected));
					}
					else
					{
						mPhoneState.timeConnected = 0;
						mPhoneState.setCallConnected(false);
						return null;
					}

				}
			}
		}
		catch (Exception e)
		{
			LoggerUtil.logToFile(LoggerUtil.Level.WTF, TAG, "checkCallLog", "exception:", e);
		}
		return connectEvent;
	}


	public void onNeighbors ( String _timestamp, int[] _list, int[] _list_rssi)
	{
		int i;
		if (_list == null || _list_rssi == null)
			return;

		if (_list.length > 0 && _list[0] != 0)
		{
			if (owner.getCellHistory() != null)
			{
				owner.getCellHistory().updateNeighborHistory (_list, _list_rssi);
			}
		}
	}

	public void popupDropped (final EventType droptype, final int rating, final int evtId)
	{
		if (rating == 0)
			return;
		owner.handler.post(new Runnable() {
			// @Override
			public void run() {
				String message = "";
				int icon;
				icon = R.drawable.ic_stat_dropped;
				String title = "";
				String msg = "";

				// server can specify whether a confirmation can be invoked on a low rated potentially-dropped call
				int allowConfirm = PreferenceManager.getDefaultSharedPreferences(owner).getInt(PreferenceKeys.Miscellaneous.ALLOW_CONFIRMATION, 5);
				String noConfirm = (owner.getResources().getString(R.string.NO_CONFIRMATION));
				int allowPopup = PreferenceManager.getDefaultSharedPreferences(owner).getInt(PreferenceKeys.Miscellaneous.ALLOW_DROP_POPUP, 2);
				if (allowPopup == 1 && !owner.getUseRadioLog())
					allowPopup = 0;
				if (allowPopup == 0)
					return;

				if (noConfirm.equals("1"))
					allowConfirm = 0;
				if (allowConfirm > 0 && rating < allowConfirm && rating < 4)  // if confirmation allow, must be above threshold or high rating dropped call
					return;
				else if (allowConfirm == 0 && rating < 4)  // drop call silently if marginal with no confirmation
					return;
				// allowConfirm>=5 disables the confirmation because rating always <= 5
				// allowConfirm=1 hits the 'else' and invokes confirmation if rating >= 1 and <5
				// allowConfirm=3 hits the 'else' and invokes confirmation if rating >= 3 and <5
				int expiry = 60000  * 2 * 60;
				int customText = (owner.getResources().getInteger(R.integer.CUSTOM_EVENTNAMES));
				message = owner.getString((customText == 1) ? R.string.sharecustom_speedtest_wifi : R.string.sharemessage_speedtest_wifi);

				if (rating >= 5 || allowConfirm == 0)
				{
					title = Global.getAppName(owner);
					msg = "mmc detected ";
					if (droptype == EventType.EVT_CALLFAIL)
						message = owner.getString((customText == 1) ? R.string.Custom_Notification_call_failed : R.string.MMC_Notification_call_failed);
					else
						message = owner.getString((customText == 1) ? R.string.Custom_Notification_call_dropped : R.string.MMC_Notification_call_dropped);
					message += ": " + owner.getString(R.string.MMC_Notification_view_event);
					msg += message;
				}
				else if (rating >= allowConfirm && rating > 1)
				{
					if (droptype == EventType.EVT_CALLFAIL)
					{
						title = owner.getString((customText == 1) ? R.string.Custom_Notification_did_you_fail : R.string.MMC_Notification_did_you_fail);
						message = owner.getString((customText == 1) ? R.string.Custom_Notification_did_failed : R.string.MMC_Notification_did_failed);
					}
					else if (droptype == EventType.EVT_DROP)
					{
						title = owner.getString((customText == 1) ? R.string.Custom_Notification_did_you_drop : R.string.Custom_Notification_did_dropped);
						message = owner.getString((customText == 1) ?  R.string.MMC_Notification_did_dropped : R.string.MMC_Notification_did_dropped);
					}
					else if (droptype == EventType.EVT_DISCONNECT || droptype == EventType.EVT_UNANSWERED)
					{
						expiry = 60000;
						icon = R.drawable.ic_stat_disconnect;
						title = owner.getString((customText == 1) ? R.string.Custom_Notification_did_you_disconnect : R.string.MMC_Notification_did_you_disconnect);
						message = owner.getString((customText == 1) ? R.string.Custom_Notification_did_disconnect : R.string.MMC_Notification_did_disconnect);
					}
					msg = message;
				}

				java.util.Date date = new java.util.Date();
				String time = date.toLocaleString();
				msg += " at " + time;
				//Toast toast = Toast.makeText(MainService.this, msg, Toast.LENGTH_LONG);
				//toast.show();

				NotificationManager notificationManager = (NotificationManager) owner.getSystemService(Service.NOTIFICATION_SERVICE);
				Notification notification = new Notification(icon, message, System.currentTimeMillis());
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				//Intent notificationIntent = new Intent(MainService.this, Dashboard.class);
				Intent notificationIntent = new Intent();//, "com.cortxt.app.mmcui.Activities.Dashboard");
				notificationIntent.setClassName(owner, "com.cortxt.app.uilib.Activities.Dashboard");
				notificationIntent.putExtra("eventId", evtId);

				notificationIntent.setData((Uri.parse("foobar://" + SystemClock.elapsedRealtime())));
				notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				PendingIntent pendingIntent = PendingIntent.getActivity(owner, MMC_DROPPED_NOTIFICATION + evtId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

				notification.setLatestEventInfo(owner, title, message, pendingIntent);
				notificationManager.notify(MMC_DROPPED_NOTIFICATION, notification);
				long expirytime = System.currentTimeMillis() + expiry;
				PreferenceManager.getDefaultSharedPreferences(owner).edit().putLong(PreferenceKeys.Monitoring.NOTIFICATION_EXPIRY, expirytime).commit();

			}});

	}

	public void processNewCellLocation(CellLocationEx cellLoc) throws InterruptedException {
		if (cellLoc.getCellLocation() != null && mPhoneState.lastKnownMMCCellLocation != null && tmLastCellUpdate + 60000 > System.currentTimeMillis() && cellLoc != null &&  cellLoc.getCellLocation().toString().equals(lastCellString))
			return;

		tmLastCellUpdate = System.currentTimeMillis();

		if (cellLoc == null) // This is so that when each event is staged, it associates the last known cell with it
			cellLoc = mPhoneState.lastKnownMMCCellLocation;
		else if (owner.getTravelDetector() != null)
		{
			CellLocation lastCellloc = null;
			if (mPhoneState.lastKnownMMCCellLocation != null)
				lastCellloc = mPhoneState.lastKnownMMCCellLocation.getCellLocation();
			owner.getTravelDetector().detectTravellingFromCellId(mPhoneState.getPhoneType(), cellLoc.getCellLocation(), lastCellloc, mPhoneState);
			//store the new cell location in the internal cache
			mPhoneState.lastKnownMMCCellLocation = cellLoc;
		}
		if (cellLoc == null || cellLoc.getCellLocation() == null) //|| cellLoc.getCellLocationLte() == null)
		{
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "processNewCellLocation", "cellLoc=null");
			return;
		}

		lastCellString = cellLoc.getCellLocation().toString();

		try
		{
			int bsHigh = cellLoc.getBSHigh(), bsMid = cellLoc.getBSMid(), bsLow = cellLoc.getBSLow();
			if (bsLow == 65535)  //cellid = -1
				bsLow = -1;

			tmLastCell = System.currentTimeMillis();
			//push the new location into the sqlite database
			long stagedEventId = owner.getEventManager().getStagedEventId();


			ContentValues values = ContentValuesGenerator.generateFromCellLocation(cellLoc, stagedEventId);
			owner.getDBProvider(owner).insert(TablesEnum.BASE_STATIONS.getContentUri(), values);
			owner.getIntentDispatcher().updateCellID(bsHigh, bsMid, bsLow);

			String neighbors = owner.getCellHistory().updateNeighborHistory(null, null);
			if (neighbors != null && neighbors != "")
			{
				owner.getIntentDispatcher().updateNeighbors (neighbors);
			}
			else if (android.os.Build.VERSION.SDK_INT >= 10 && telephonyManager.getNetworkType() == PhoneState.NETWORK_NEWTYPE_LTE && (cellLoc.getCellLocation() instanceof GsmCellLocation) == true)
			{
				int cid = bsLow + (bsMid<<16);
				int pci = ((GsmCellLocation)cellLoc.getCellLocation()).getPsc();
				if (pci <= 0)
				{
					pci = cellLoc.getBSCode();
				}
				neighbors = owner.getCellHistory().updateLteNeighborHistory(bsHigh,cid,pci);
				owner.getIntentDispatcher().updateLTEIdentity (neighbors);
				owner.getReportManager().setNeighbors(neighbors);
			}

			Intent intent = new Intent(IntentHandler.HANDOFF);
			owner.sendBroadcast(intent);
		}
		catch (Exception e)
		{
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "processNewCellLocation", "Exception", e);
		}

	}

	/*
	 *  Store the new signal strength in the SQLite DB and update the live status screen
	 *  This may be called by SignalStrength listener as well as on service outage or screen off
	 *  signal = null means no service / signal.getSignalStrength() = null means unknown due to screen off
	 */
	private long tmlastSig = 0;
	public void processNewMMCSignal(SignalEx signal)  {
		ContentValues values = null;
		// if in a service outage, store a null signal
		// (I've seen cases where phone was out of service yet it was still returning a signal level)
		try
		{
			if (mPhoneState.getLastServiceState() == ServiceState.STATE_OUT_OF_SERVICE)
				signal = null;

			// avoid storing repeating identical signals
			if (mPhoneState.lastKnownMMCSignal != null && mPhoneState.lastKnownMMCSignal.getSignalStrength() != null && signal != null && signal.getSignalStrength() != null)
				if (mPhoneState.lastKnownMMCSignal.getSignalStrength().toString().equals(signal.getSignalStrength().toString()) && tmlastSig + 3000 > System.currentTimeMillis())
					return;
			tmlastSig = System.currentTimeMillis();
			Integer dbmValue = 0;
			boolean isLTE = false;
			if (signal == null)
				dbmValue = -256;
			else if (signal.getSignalStrength() == null)
				dbmValue = 0;

			//store the last known signal
			if (signal != null && signal.getSignalStrength() != null)
			{
				prevMMCSignal = mPhoneState.lastKnownMMCSignal; // used for looking at signal just before a call ended
				mPhoneState.lastKnownMMCSignal = signal;
			}
			else if (signal == null)
				mPhoneState.lastKnownMMCSignal = null;

			//push the new signal level into the sqlite database
			long stagedEventId = owner.getEventManager().getStagedEventId();
			int serviceState = mPhoneState.getLastServiceState();
			int wifiSignal = -1;
			WifiManager wifiManager = (WifiManager)owner.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiinfo = wifiManager.getConnectionInfo() ;
			if (wifiinfo != null && wifiinfo.getBSSID() != null)
				wifiSignal =  wifiManager.getConnectionInfo().getRssi();

			//if (signal != null) //  disabled because we do want the no-signal to be written to the signals table
			{
				values = ContentValuesGenerator.generateFromSignal(signal, telephonyManager.getPhoneType(), telephonyManager.getNetworkType(),
						serviceState, telephonyManager.getDataState(), stagedEventId, wifiSignal, mPhoneState.mServicemode);
				Integer valSignal= (Integer)values.get("signal");
				if (mPhoneState.getNetworkType() == PhoneState.NETWORK_NEWTYPE_LTE) // && phoneStateListener.previousNetworkState == TelephonyManager.DATA_CONNECTED)
					valSignal= (Integer)values.get("lteRsrp");
				if (valSignal != null && dbmValue != null && valSignal > -130 && valSignal < -30) //  && (dbmValue <= -120 || dbmValue >= -1))
					dbmValue = valSignal;
				if ((dbmValue > -120 || mPhoneState.getNetworkType() == PhoneState.NETWORK_NEWTYPE_LTE) && dbmValue < -40)
					this.validSignal = true;
				if (this.validSignal) // make sure phone has at least one valid signal before recording
					owner.getDBProvider(owner).insert(TablesEnum.SIGNAL_STRENGTHS.getContentUri(), values);

			}
			//update the signal strength percentometer, chart, and look for low/high signal event
			if (dbmValue != null){
				if (dbmValue < -120) // might be -256 if no service, but want to display as -120 on livestatus chart
					dbmValue = -120;

				owner.getIntentDispatcher().updateSignalStrength(
						dbmValue, mPhoneState.getNetworkType(), owner.bWifiConnected, wifiSignal
				);

				// Store signal in a sharedPreference for tools such as Indoor/Transit sample mapper, which dont have reference to service
				if (isLTE == true)   // improve the value of the signal for LTE, so that Indoor samples don't look redder in LTE
					PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.SIGNAL_STRENGTH, (dbmValue+15)).commit();
				else
					PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.SIGNAL_STRENGTH, dbmValue).commit();

			}
		}
		catch (Exception e)
		{
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "processNewMMCSignal", "exception", e);
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "processNewMMCSignal", "values: " + values);
		}
		catch (Error err)
		{
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "processNewMMCSignal", "error" + err.getMessage());
		}
	}

	public CellLocationEx getLastCellLocation(){
		if (mPhoneState.lastKnownMMCCellLocation != null)
			return mPhoneState.lastKnownMMCCellLocation;
		CellLocation cellLoc = telephonyManager.getCellLocation();
		if (cellLoc != null)
		{
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "getLastCellLocation", "null cell, getCellLocation() = " + cellLoc.toString());

			CellLocationEx mmcCell = new CellLocationEx(cellLoc);
			try {
				processNewCellLocation(mmcCell);
			} catch (InterruptedException e) {
			}
			return mmcCell;
		}
		LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "getLastCellLocation", "null cell, getCellLocation() = null");
		return null;
	}
	public void clearLastCellLocation(){
		mPhoneState.lastKnownMMCCellLocation = null;
	}

	// data stall (disconnected data while still in service)
	private void stateChanged_0g(int state) {
		// No such thing as DATA outage event
		EventObj event = null;
		// DATA Outage defined as switching to and connecting to 1G (GPRS) from > 1G (EDGE or higher)
		if (state == TelephonyManager.DATA_DISCONNECTED && mPhoneState.previousNetworkTier > 0){
			//event = owner.startPhoneEvent(EventType.COV_DATA_DISC, EventType.COV_DATA_CONN);
			// 4G Outage defined as switching to and connecting to 2G from 4G
			if (mPhoneState.previousNetworkTier > 4){
				if (mPhoneState.isScreenOn() || mPhoneState.isOffHook() || owner.isTravelling())
					event = owner.getEventManager().startPhoneEvent(EventType.COV_4G_NO, EventType.COV_4G_YES);
			}
			if (mPhoneState.previousNetworkTier > 2){
				if (mPhoneState.isScreenOn() || mPhoneState.isOffHook() || owner.isTravelling())
					event = owner.getEventManager().startPhoneEvent(EventType.COV_3G_NO, EventType.COV_3G_YES);
			}
			if (mPhoneState.previousNetworkTier > 0){
				if (mPhoneState.isScreenOn() || mPhoneState.isOffHook() || owner.isTravelling())
					event = owner.getEventManager().startPhoneEvent(EventType.COV_DATA_NO, EventType.COV_DATA_YES);
			}
		}

	}
	
	private void stateChanged_2g(int state) {
		// No such thing as DATA outage event
		EventObj event = null;
		//if (state == TelephonyManager.DATA_CONNECTED && previousNetworkTier == 0 && owner.getNetworkGeneration() == 1){
		//	event = owner.stopPhoneEvent(EventType.COV_DATA_DISC, EventType.COV_DATA_CONN);
		//} 
		// DATA Outage defined as switching to and connecting to 1G (GPRS) from > 1G (EDGE or higher)
		if (state == TelephonyManager.DATA_CONNECTED && mPhoneState.previousNetworkTier > 1 && mPhoneState.getNetworkGeneration() == 1){
			if (mPhoneState.isScreenOn() || mPhoneState.isOffHook() || owner.isTravelling())
				event = owner.getEventManager().startPhoneEvent(EventType.COV_DATA_NO, EventType.COV_DATA_YES);
		} 
		// 3G Outage defined as switching to and connecting to 2G from >2G
		if (state == TelephonyManager.DATA_CONNECTED && mPhoneState.previousNetworkTier > 2){
			if (mPhoneState.isScreenOn() || mPhoneState.isOffHook() || owner.isTravelling())
				event = owner.getEventManager().startPhoneEvent(EventType.COV_3G_NO, EventType.COV_3G_YES);
		} 
		// 4G Outage defined as switching to and connecting to 2G from 4G
		if (state == TelephonyManager.DATA_CONNECTED && mPhoneState.previousNetworkTier > 4){
			if (mPhoneState.isScreenOn() || mPhoneState.isOffHook() || owner.isTravelling())
				event = owner.getEventManager().startPhoneEvent(EventType.COV_4G_NO, EventType.COV_4G_YES);
		}

		if (state == TelephonyManager.DATA_CONNECTED && mPhoneState.previousNetworkTier < 2 && mPhoneState.getNetworkGeneration() == 2)
			event = owner.getEventManager().stopPhoneEvent(EventType.COV_DATA_NO, EventType.COV_DATA_YES);


	}

	private void stateChanged_3g(int state) {
		EventObj event = null;
		//if (state == TelephonyManager.DATA_CONNECTED && previousNetworkTier == 0 && owner.getNetworkGeneration() == 1){
		//	event = owner.stopPhoneEvent(EventType.COV_DATA_DISC, EventType.COV_DATA_CONN);
		//} 
		// 3G Regained defined as switching to and connecting to 3G+ from <3G
		if (state == TelephonyManager.DATA_CONNECTED && mPhoneState.previousNetworkTier < 1)
			event = owner.getEventManager().stopPhoneEvent(EventType.COV_DATA_NO, EventType.COV_DATA_YES);
		if (state == TelephonyManager.DATA_CONNECTED && mPhoneState.previousNetworkTier < 3){
			if (mPhoneState.previousNetworkTier <= 1)
				event = owner.getEventManager().stopPhoneEvent(EventType.COV_DATA_NO, EventType.COV_DATA_YES);
			event = owner.getEventManager().stopPhoneEvent(EventType.COV_3G_NO, EventType.COV_3G_YES);
		} 
		// 4G Outage defined as switching to and connecting to 3G from LTE 4G
		if (state == TelephonyManager.DATA_CONNECTED && mPhoneState.previousNetworkTier > 4 && !mPhoneState.bOffHook){
			if (mPhoneState.isScreenOn() || mPhoneState.isOffHook() || owner.isTravelling())
				event = owner.getEventManager().startPhoneEvent(EventType.COV_4G_NO, EventType.COV_4G_YES);
		} 
		
	}
	
	private void stateChanged_4g(int state) {
		EventObj event = null;
		//if (state == TelephonyManager.DATA_CONNECTED && previousNetworkTier == 0 && owner.getNetworkGeneration() == 1){
		//	event = owner.stopPhoneEvent(EventType.COV_DATA_DISC, EventType.COV_DATA_CONN);
		//} 
		// 4G Regained defined as switching to and connecting to 4G+ from <3G
		// If it switches to and from LTE too often, this could result in excessive events, but it appears to hold LTE steady
		if (state == TelephonyManager.DATA_CONNECTED && mPhoneState.previousNetworkTier < 5){
			if (mPhoneState.previousNetworkTier <= 1)
				event = owner.getEventManager().stopPhoneEvent(EventType.COV_DATA_NO, EventType.COV_DATA_YES);
			if (mPhoneState.previousNetworkTier < 3)
				event = owner.getEventManager().stopPhoneEvent(EventType.COV_3G_NO, EventType.COV_3G_YES);
			
			String pref = mPhoneState.getNetworkTypesAndroidPreference();
			if (pref.indexOf("LTE") >= 0)
				return;
			// disregard and undo an LTE outage if it regains just after a phone call disconnects
			EventCouple targetEventCouple = owner.getEventManager().getEventCouple(EventType.COV_4G_NO, EventType.COV_4G_YES);
			
			if (mPhoneState.disconnectTime + 120000 > System.currentTimeMillis() && targetEventCouple != null
					&& targetEventCouple.getStartEvent() != null && targetEventCouple.getStartEvent().getEventTimestamp() + 30000 > mPhoneState.offhookTime
					&& mPhoneState.offhookTime > 0)
			{
				LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "stateChanged_4g", "Undo LTE outage event");
				// 4G outage wont be staged anymore, but it still needs to remove COV_4G_NO from 'eventCache'
				owner.getEventManager().unstageEvent(targetEventCouple.getStartEvent());
				owner.getEventManager().cancelCouple (targetEventCouple);
				//owner.getEventManager().deleteEventDB(targetEventCouple.getStartEvent().getUri(), null, null);
				int eventId = ReportManager.getInstance(owner).getEventId(targetEventCouple.getStartEvent().getEventTimestamp(), EventType.COV_4G_NO.getIntValue());
			    if (eventId != 0)
			    	 ReportManager.getInstance(owner).deleteEvent (eventId);
			}
			else
			{
				event = owner.getEventManager().stopPhoneEvent(EventType.COV_4G_NO, EventType.COV_4G_YES);
				//if (event != null)
				//	owner.queueActiveTest(EventType.LATENCY_TEST, 1);
			}
		}
	}

	public RestCommManager getRestCommManager ()
	{
		return restcommManager;
	}

	/**
	 * When the cell location gets changed, the new cellId is added to the cell id buffer in the 
	 * owner. At the same time, the CELLCHANGE event is stored.
	 */
	private long tmLastCellInfoUpdate = 0;
	private String lastCellInfoString = "";
	private List<Object> lastKnownCellInfo = null;
	
	@TargetApi(17) @Override
	public void onCellInfoChanged(List<CellInfo> cellinfos) {
		super.onCellInfoChanged(cellinfos);
		try {
			if (!owner.isMMCActiveOrRunning())
				return;
			if (tmLastCellInfoUpdate + 60000 > System.currentTimeMillis() && cellinfos != null && cellinfos.size() > 0 && cellinfos.get(0).toString().equals(lastCellInfoString))
				return;
			if (cellinfos != null && cellinfos.size() > 0)
				lastCellInfoString = cellinfos.get(0).toString();
			else
				lastCellInfoString = "";	
			tmLastCellInfoUpdate = System.currentTimeMillis();
			
			if (mPhoneState.getNetworkType() == mPhoneState.NETWORK_NEWTYPE_LTE)
			{
				String neighbors = owner.getCellHistory().updateLteNeighborHistory(cellinfos);
				if (neighbors != null)
				{
					owner.getIntentDispatcher().updateLTEIdentity (neighbors);
					owner.getReportManager().setNeighbors(neighbors);
				}
				
			}
			
			if (cellinfos != null && cellinfos.size() > 0 && cellinfos.get(0) != null)
				for (int i=0; i<cellinfos.size(); i++)
				{
					//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "onCellInfoChanged", "cellinfos["+i+"]: " + cellinfos.get(i).toString());
					if (mPhoneState.getNetworkType() == mPhoneState.NETWORK_NEWTYPE_LTE)
					{
						if (cellinfos.get(i) instanceof CellInfoLte)
						{
							CellIdentityLte cellIDLte = ((CellInfoLte)cellinfos.get(i)).getCellIdentity();
							//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "onCellInfoChanged", "Reflected: " + listCellInfoFields(cellIDLte));
						}
					}
				}
			//else
			//	MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "onCellInfoChanged", "cellinfos: null");
			
		} catch (Exception intEx){
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "onCellInfoChanged", "InterruptedException: " + intEx.getMessage());
		} 
	}

    // If this is a CDMACellLocation without SID and NID, see if we can extract it from the ServiceState
    private void checkCDMACellSID (CellLocation cell)
    {
        if (cell instanceof CdmaCellLocation)
        {
            CdmaCellLocation cdmaCell = (CdmaCellLocation)cell;
            if (cdmaCell.getSystemId() <= 0)
            {
                Field getSIDPointer = null;
                Field getNIDPointer = null;
                int SID = 0, NID = 0, BID = cdmaCell.getBaseStationId();
                try {
                    getSIDPointer = mPhoneState.previousServiceStateObj.getClass().getDeclaredField("mSystemId");
                    if (getSIDPointer != null)
                    {
                        getSIDPointer.setAccessible(true);
                        SID = (int) getSIDPointer.getInt(cdmaCell);
                    }
                    getNIDPointer = mPhoneState.previousServiceStateObj.getClass().getDeclaredField("mNetworkId");
                    if (getNIDPointer != null)
                    {
                        getNIDPointer.setAccessible(true);
                        NID = (int) getNIDPointer.getInt(cdmaCell);
                    }
                    cdmaCell.setCellLocationData(BID, cdmaCell.getBaseStationLatitude(), cdmaCell.getBaseStationLongitude(),
                                                SID, NID); // Update the SID and NID that we read from teh Servicestate
                } catch (Exception e) {
                    //MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "checkInnerGsmCellLocation","Field does not exist - mGsmCellLoc");
                }
            }
        }
    }
	// See if this cellLocation has inner GsmLocation
	private void checkInnerGsmCellLocation (CellLocation cell)
	{
		if (cell != null)
		{
			String strCells = "";
			
			Field getFieldPointer = null;
			try {
				getFieldPointer = cell.getClass().getDeclaredField("mGsmCellLoc"); //NoSuchFieldException 

			} catch (Exception e) {
				//MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "checkInnerGsmCellLocation","Field does not exist - mGsmCellLoc");
			}
			if (getFieldPointer != null){
				//now we're in business!
				try {
                    getFieldPointer.setAccessible(true);
                    GsmCellLocation gsmCell = (GsmCellLocation) getFieldPointer.get(cell);
					if (gsmCell != null)
					{
						int bsHigh = gsmCell.getLac();
						int bsLow = gsmCell.getCid();
						LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "checkInnerGsmCellLocation", "Obtained mGsmCellLoc LAC=" + gsmCell.getLac() + " toString=" + gsmCell.toString());
						
						if (mPhoneState.getNetworkType() == mPhoneState.NETWORK_NEWTYPE_LTE)
						{
                            int psc = 0;
                            if (android.os.Build.VERSION.SDK_INT >= 10)
                                psc = gsmCell.getPsc();
							String neighbors = owner.getCellHistory().updateLteNeighborHistory(bsHigh,bsLow,psc);
							owner.getIntentDispatcher().updateLTEIdentity (neighbors);
							owner.getReportManager().setNeighbors(neighbors);
						}
					}
				} catch (Exception e) {
					Log.d(TAG, "Could not get the inner GSM", e);
				}
			}
		}
	}

}

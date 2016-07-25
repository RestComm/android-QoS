package com.cortxt.app.corelib.Services.Intents;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import com.cortxt.app.corelib.MainService;
import com.cortxt.app.corelib.R;
import com.cortxt.app.utillib.ContentProvider.TablesEnum;
import com.cortxt.app.utillib.DataObjects.DeviceInfo;
import com.cortxt.app.utillib.DataObjects.EventObj;
import com.cortxt.app.utillib.DataObjects.EventType;
import com.cortxt.app.utillib.DataObjects.PhoneState;
import com.cortxt.app.utillib.Reporters.ReportManager;
import com.cortxt.app.utillib.Reporters.WebReporter.WebReporter;
import com.cortxt.app.utillib.Utils.CommonIntentActionsOld;
import com.cortxt.app.utillib.Utils.Global;
import com.cortxt.app.utillib.Utils.LoggerUtil;
import com.cortxt.app.utillib.Utils.PreciseCallCodes;
import com.cortxt.app.utillib.Utils.PreferenceKeys;
import com.cortxt.app.utillib.DataObjects.SignalEx;
import com.cortxt.app.utillib.Utils.CommonIntentBundleKeysOld;
import com.cortxt.app.corelib.UtilsOld.DataMonitorStats;
import com.cortxt.app.utillib.Utils.DeviceInfoOld;
import com.cortxt.app.corelib.Services.Events.EventResponse;
import com.google.gson.Gson;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This method handles all the intents that the service can receive. 
 * The intent filters are defined in the owner class but this class acts
 * as the manager for them.
 * @author abhin
 *
 */
public class IntentHandler extends BroadcastReceiver {
	public static final String TAG = IntentHandler.class.getSimpleName();
	public static final String ACTION_SPEEDTEST_RESULT = "com.cortxt.app.MMC.intent.ACTION_SPEEDTEST_RESULT";
	public static final String ACTION_SPEEDTEST_ERROR = "com.cortxt.app.MMC.intent.ACTION_SPEEDTEST_ERROR";
	public static final String ACTION_SPEEDTEST_COMPLETE = "com.cortxt.app.MMC.intent.ACTION_SPEEDTEST_COMPLETE";
	public static final String ACTION_SPEEDTEST_CANCELLED = "com.cortxt.app.MMC.intent.ACTION_SPEEDTEST_CANCELLED";
	/**
	 * Latency in milliseconds
	 */
	public static final String EXTRA_LATENCY = "latency";
	/**
	 * Download speed in bits/second
	 */
	public static final String EXTRA_DOWNLOAD_SPEED = "down";
	/**
	 * Upload speed in bits/second
	 */
	public static final String EXTRA_UPLOAD_SPEED = "up";
	public static final String EXTRA_EVENT_TYPE = "up";
	/**
	 * Latency progress in %
	 */
	public static final String EXTRA_LATENCY_PROGRESS = "latencyProgress";
	/**
	 * Download speed test progress in %
	 */
	public static final String EXTRA_DOWNLOAD_PROGRESS = "downloadProgress";
	/**
	 * Upload speed test progress in %
	 */
	public static final String EXTRA_UPLOAD_PROGRESS = "uploadProgress";
	public static final String ACTION_WEBTEST_RESULT = "com.cortxt.app.MMC.intent.ACTION_AUDIOTEST_RESULT";
	public static final String ACTION_WEBTEST_ERROR = "com.cortxt.app.MMC.intent.ACTION_AUDIOTEST_ERROR";
	public static final String ACTION_WEBTEST_COMPLETE = "com.cortxt.app.MMC.intent.ACTION_AUDIOTEST_COMPLETE";
	public static final String ACTION_WEBTEST_CANCELLED = "com.cortxt.app.MMC.intent.ACTION_AUDIOTEST_CANCELLED";
	/**
	 * Download progress in milliseconds
	 */
	public static final String EXTRA_BUFFER_PROGRESS = "buffprogress";
	public static final String EXTRA_PLAY_PROGRESS = "playprogress";
	public static final String EXTRA_RXBYTES = "rxbytes";

	/*
	*  logcat radio information
	*
	 */

	public static final String ACTION_RADIOLOG_DISCONNECT = "com.cortxt.app.MMC.intent.RADIOLOG_DISCONNECT";
	public static final String ACTION_RADIOLOG_CONNECT = "com.cortxt.app.MMC.intent.RADIOLOG_CONNECT";
	public static final String ACTION_RADIOLOG_NEIGHBORS = "com.cortxt.app.MMC.intent.RADIOLOG_NEIGHBORS";
	public static final String ACTION_RADIOLOG_SERVICEMODE = "com.cortxt.app.MMC.intent.RADIOLOG_SERVICE";
	public static final String ACTION_RADIOLOG_ERROR = "com.cortxt.app.MMC.intent.RADIOLOG_ERROR";
	public static final String ACTION_RADIOLOG_SERVICEMENU = "com.cortxt.app.MMC.intent.RADIOLOG_SERVICEMENU";
	public static final String ACTION_MMCSYS_VERSION = "com.cortxt.app.MMC.intent.MMCSYS_VERSION";
	public static final String EXTRA_RADIOLOG_TIME = "radio_time";
	public static final String EXTRA_RADIOLOG_DISC_CAUSE = "radio_disc_cause";
	public static final String EXTRA_RADIOLOG_CONN_STATE = "radio_conn_state";
	public static final String EXTRA_RADIOLOG_NEIGHBORS = "radio_neighbors";
	public static final String EXTRA_RADIOLOG_SVC_JSON = "radio_svc_json";
	public static final String EXTRA_RADIOLOG_SVC_TEXT = "radio_svc_text";
	public static final String EXTRA_RADIOLOG_SVC_NAME = "radio_svc_name";
	public static final String EXTRA_RADIOLOG_ERROR = "radio_error";
	public static final String EXTRA_RADIOLOG_ERROR_DETAIL = "radio_error_detail";
	public static final String EXTRA_MMCSYS_VERSION = "mmcsys_version";

	/**
	 * Download speed test progress in %
	 */
	public static final String EXTRA_STALLS = "stalls";
	public static final String EXTRA_STALL_TIME = "stallTime";
	public static final String EXTRA_DURATION = "duration";
	public static final String EXTRA_ACCESS_DELAY = "accessDelay";
	public static final String EXTRA_PLAY_DELAY = "playDelay";
	public static final String EXTRA_VIDEO_RES = "res";
	public static final String EXTRA_EVENTTYPE = "eventType";
	public static final String EXTRA_DOWNLOAD_TIME = "downloadTime";
	//private variables
	private static MainService owner;	//keep a local reference to the owner class in order to access the important variables
    private DataMonitorStats dataMonitorStats;
	ReportManager reportManager;
    private WakeLock wakeLock = null;
    private Gson gson;
			
	//constants related to the intents MainService can receive
	/**
	 * This action is meant to trigger an update event ( {@link EventType#COV_UPDATE} ).
	 */
	public static final String UPDATE_ACTION = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.UPDATE_ACTION";
	/**
	 * This action is meant to give the GPS a cold reset from the menu 
	 */
	public static final String COLDRESET_ACTION = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.COLDRESET_ACTION";
	
	/**
	 * This action is meant to trigger an csv email from the server to the users email 
	 */
	public static final String EMAIL_CSV = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.EMAIL_CSV";
	
	/**
	 * This action is meant to start a tracking event ({@link EventType#MAN_TRACKING})
	 */
	public static final String START_TRACKING_ACTION = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.START_TRACKING_ACTION";
	public static final String TRACK_REMOTE = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.TRACK_REMOTE";
	
	/**
	 * This is the key for the time, in milliseconds, the user chose to "track" for.
	 */
	public static final String TRACKING_NUM_5_MIN_PERIODS = "TRACKING_NUM_5_MIN_PERIODS";
	
//	public static final String TRACKING_SPEED_INTERVAL = "TRACKING_SPEED_INTERVAL";
//	public static final String TRACKING_COVERAGE_INTERVAL = "TRACKING_COVERAGE_INTERVAL";
//	public static final String TRACKING_VIDEO_INTERVAL = "TRACKING_VIDEO_INTERVAL";
//	public static final String TRACKING_CONNECT_INTERVAL = "TRACKING_CONNECT_INTERVAL";
//	public static final String TRACKING_SMS_INTERVAL = "TRACKING_SMS_INTERVAL";
//	public static final String TRACKING_WEB_INTERVAL = "TRACKING_WEB_INTERVAL";
//	public static final String TRACKING_VQ_INTERVAL = "TRACKING_VQ_INTERVAL";
//    public static final String TRACKING_AUDIO_INTERVAL = "TRACKING_AUDIO_INTERVAL";
	//public static final String TRACKING_COVERAGE = "TRACKING_COVERAGE";
	//public static final String TRACKING_SPEED = "TRACKING_SPEED";
	
	public static final String TRACKING_REMOTE_START_TIME = "TRACKING_REMOTE_START_TIME";
	public static final String TRACKING_REMOTE_INTERVAL = "TRACKING_REMOTE_INTERVAL";
	/**
	 * This action is meant to stop a tracking event ({@link EventType#MAN_TRACKING})
	 */
	public static final String STOP_TRACKING_ACTION = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.STOP_TRACKING_ACTION";

    /**
     * This action is meant to trigger running a WebSocket
     */
    public static final String RUN_WEBSOCKET = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.RUN_WEBSOCKET";
    public static final String EXTRA_START_WEBSOCKET = "START_WEBSOCKET";

	/**
	 * This action is meant to trigger speed tests.
	 */
	public static final String SPEED_TEST = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.SPEED_TEST";
	public static final String SMS_TEST = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.DO_SMSTEST";
	public static final String SMS_DELIVERED = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.SMS_DELIVERED";
	public static final String LATENCY_TEST = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.LATENCY_TEST";
	public static final String ACTION_STOP_SPEEDTEST = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.ACTION_STOP_SPEEDTEST";
	public static final String ACTION_STOP_VIDEOTEST = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.ACTION_STOP_VIDEOTEST";
	public static final String ACTION_DONE_SMSTEST = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.ACTION_SMSTEST_DONE";
	public static final String ACTION_SMSTEST_CANCEL = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.ACTION_SMSTEST_CANCEL";
	public static final String ACTION_ALARM_MINUTE = "com.cortxt.app.MMC.intent.action.ACTION_ALARM_MINUTE";
	public static final String ACTION_TRACKING_5MINUTE = "com.cortxt.app.MMC.intent.action.ACTION_TRACKING_5MINUTE";
    public static final String ACTION_TRACKING_1MINUTE = "com.cortxt.app.MMC.intent.action.ACTION_TRACKING_1MINUTE";
    public static final String ACTION_ALARM_3HOUR = "com.cortxt.app.MMC.intent.action.ACTION_ALARM_3HOUR";
	public static final String ROAMING_ON = "com.cortxt.app.MMC.intent.action.ROAMING_ON";
	public static final String ROAMING_OFF = "com.cortxt.app.MMC.intent.action.ROAMING_OFF";
	public static final String WIMAX_STATE_CHANGE = "com.cortxt.app.MMC.intent.action.WIMAX_STATE_CHANGE";
	public static final String GPS_STATE_ON = "com.cortxt.app.MMC.intent.action.GPS_STATE_ON";
	public static final String GPS_STATE_OFF = "com.cortxt.app.MMC.intent.action.GPS_STATE_OFF";
	public static final String HANDOFF = "com.cortxt.app.MMC.intent.action.HANDOFF";
	public static final String PHONE_CALL_CONNECT = "com.cortxt.app.MMC.intent.action.PHONE_CALL_CONNECT";
	public static final String PHONE_CALL_DISCONNECT = "com.cortxt.app.MMC.intent.action.PHONE_CALL_DISCONNECT";
	public static final String ACTION_ALARM_15MINUTE = "com.cortxt.app.MMC.intent.action.ACTION_ALARM_15MINUTE";
	public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	public static final String SMS_SENT = "android.provider.Telephony.SMS_SENT";
	public static final String MANUAL_PLOTTING_START = "com.cortxt.app.MMC.intent.action.MANUAL_PLOTTING_START";
	public static final String MANUAL_PLOTTING_END = "com.cortxt.app.MMC.intent.action.MANUAL_PLOTTING_END";
	public static final String MANUAL_PLOTTING_CANCEL = "com.cortxt.app.MMC.intent.action.MANUAL_PLOTTING_CANCEL";
	public static final String RESTART_MMC_SERVICE = "com.cortxt.app.MMC.intent.action.RESTART_MMC_SERVICE";
	public static final String COMMAND = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.COMMAND";
	public static final String COMMAND_EXTRA = "command_extra";
	public static final String SURVEY = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.SURVEY";
	public static final String SURVEY_EXTRA = "survey_extra";
	public static final String VIEWING_SIGNAL = "com.cortxt.app.MMC.intent.action.VIEWING_SIGNAL";
	public static final String MANUAL_TRANSIT_START = "com.cortxt.app.MMC.intent.action.MANUAL_TRANSIT_START";
	public static final String MANUAL_TRANSIT_END = "com.cortxt.app.MMC.intent.action.MANUAL_TRANSIT_END";
	public static final String MANUAL_TRANSIT_CANCEL = "com.cortxt.app.MMC.intent.action.MANUAL_TRANSIT_CANCEL";
	public static final String ACTIVE_TEST = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.ACTIVE_TEST";
    public static final String WEB_TEST = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.WEB_TEST";
    public static final String ACTION_ALARM_SCANAPPS = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.ALARM_SCANAPPS";
    public static final String GCM_MESSAGE = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.GCM_MESSAGE";
    public static final String GCM_MESSAGE_EXTRA = "gsm_message_extra";
	public static final String MMC_SERVICE_FOREGROUND = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.MMC_SERVICE_FOREGROUND";
	public static final String MMC_FOREGROUND_MESSAGE = "MMC_FOREGROUND_MESSAGE";
	public static final String MMC_FOREGROUND = "MMC_FOREGROUND";

//	public static final String ACTION_TRANSIT_DL_DONE = "com.cortxt.app.MMC.intent.action.ACTION_TRANSIT_DL_DONE";
//	public static final String ACTION_TRANSIT_DL_START = "com.cortxt.app.MMC.intent.action.ACTION_TRANSIT_DL_START";
//	static final String ACTION_BRIEFLY_RUN_LOCATION = "com.cortxt.app.MMC.intent.action.ACTION_BRIEFLY_RUN_LOCATION";
//	public static final String ACTION_GPS_LOCATION_UPDATE = "com.cortxt.app.MMC.intent.action.ACTION_GPS_LOCATION_UPDATE";
//	public static final String ACTION_NETWORK_LOCATION_UPDATE = "com.cortxt.app.MMC.intent.action.ACTION_NETWORK_LOCATION_UPDATE";
	

	/**
	 * constructor
	 */
	public IntentHandler(MainService owner, DataMonitorStats dataMonitorStats){
		this.owner = owner;	
		this.dataMonitorStats = dataMonitorStats;
		PowerManager powerManager = (PowerManager) owner.getSystemService(owner.POWER_SERVICE);
    	wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
        gson = new Gson();
		reportManager = owner.getReportManager();
	}

	public IntentHandler(){

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Bundle intentExtras = intent.getExtras();

		if (owner == null)
			return;
        owner.getWebSocketManager().sendIntentToWebSocket(action, intentExtras);
		
		//capture the "battery changed" event
		if (action.equals(Intent.ACTION_BATTERY_CHANGED)){
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
			DeviceInfoOld.battery = level * 100 / scale;
			int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);    
			boolean bCharging = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB; 
			owner.setBatteryCharging (bCharging);
			dataMonitorStats.setBattery(bCharging);
		}else if (action.equals(Intent.ACTION_POWER_CONNECTED)){
			owner.setBatteryCharging (true);	
			dataMonitorStats.setBattery(true);
		}else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)){
			owner.setBatteryCharging (false);
			dataMonitorStats.setBattery(false);
		}else if (action.equals(Intent.ACTION_VIEW)) {
			//this is supposed to trigger the update event
			//owner.triggerUpdateEvent(false, false);
		}else if (action.equals(CommonIntentActionsOld.ACTION_START_UI)) {
			String packagename = intent.getStringExtra("packagename");
			String mypackagename = context.getPackageName();
			PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PreferenceKeys.Miscellaneous.YEILDED_SERVICE, packagename).commit();
			// If the UI started on a different MMC app UI, then we stop this service until this UI is launched
			if (!packagename.equals(mypackagename))  // This will exit the service when safe, and won't restart it because it will be yeilded
				owner.restartSelf();
		}else if (action.equals(UPDATE_ACTION)){
			owner.getEventManager().triggerUpdateEvent(false, false);
		} else if (action.equals(COLDRESET_ACTION)){
			//this is supposed to trigger the update event
			owner.getGpsManager().coldStart("triggered by user");
		} else if (action.equals(EMAIL_CSV)){
			//this is supposed to trigger the update event
			owner.requestCsvEmail();
		} else if (action.equals(SPEED_TEST)){
            //this is supposed to trigger a speed test
            int trigger = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_SPEED_TRIGGER, 0);
            //owner.triggerSMSTest(trigger);
            owner.getEventManager().queueActiveTest(EventType.MAN_SPEEDTEST, trigger);
        } else if (action.equals(RUN_WEBSOCKET)){
			//this is supposed to trigger a speed test
			boolean bStart = intent.getBooleanExtra(EXTRA_START_WEBSOCKET, true);
            owner.getWebSocketManager().runWebSocket(bStart);
			//owner.triggerSpeedTest(trigger);
		} else if (action.equals(ACTIVE_TEST)){
			//this is supposed to trigger a speed test
            int evType = intent.getIntExtra (CommonIntentBundleKeysOld.EXTRA_TEST_TYPE, 0);
            EventType eventType = EventType.get(evType);
			int trigger = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_SPEED_TRIGGER, 0);
			owner.getEventManager().queueActiveTest(eventType, trigger);
		}
		else if (action.equals(SMS_TEST)){
			//this is supposed to trigger a speed test
			int trigger = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_SPEED_TRIGGER, 0);
			//owner.triggerSMSTest(trigger);
			owner.getEventManager().queueActiveTest(EventType.SMS_TEST, trigger);
		}
		else if (action.contains(SMS_DELIVERED)){
			//add delivery time to connection history
			
			int identifier = intent.getIntExtra("identifier", 0); //in case we need to search connection list to find which SMS
			long deliveryTime = 0;
			
			switch (getResultCode()) {
            case Activity.RESULT_OK:
            	deliveryTime = System.currentTimeMillis();
            	break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
            	deliveryTime = -1;  
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
            	deliveryTime = -1;
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
            	deliveryTime = -1;
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
            	deliveryTime = -1;
                break;
			}

			owner.getEventManager().handleSMSDeliverNotification(identifier, deliveryTime);
		}
		else if (action.equals(LATENCY_TEST)){
			int trigger = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_SPEED_TRIGGER, 0);
			owner.getEventManager().queueActiveTest(EventType.LATENCY_TEST, trigger);
			//owner.runLatencyTest(false);
		}
		else if (action.equals(ACTION_STOP_SPEEDTEST)){
			owner.getEventManager().killSpeedTest();
		}
		else if (action.equals(ACTION_STOP_VIDEOTEST)){
            int testType = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_TEST_TYPE, 0);
			owner.getEventManager().killActiveTest(testType);
		}
		else if (action.equals(ROAMING_ON)){
 			dataMonitorStats.setRoaming(true);
		}
		else if (action.equals(ROAMING_OFF)){	
 			dataMonitorStats.setRoaming(false);
		}
		else if (action.equals("android.intent.action.ANY_DATA_STATE"))
		{
			String apn = intentExtras.getString("apn");
			String state = intentExtras.getString("state");
			String apnType = intentExtras.getString("apnType");
			String extras = apnType + "" + state;
			if (state.equals("CONNECTED") && !apnType.equals("default"))
				extras = extras + "!";
			if (apnType != null && apnType.equals("default") && apn != null)
			{
				SharedPreferences securePref = MainService.getSecurePreferences(context);
				securePref.edit().putString(PreferenceKeys.Miscellaneous.KEY_APN, apn).commit();
			}
				
		}
		else if (action.equals("android.intent.action.DATA_CONNECTION_CONNECTED_TO_PROVISIONING_APN"))
		{
			String apn = intentExtras.getString("apn");
			String apnType = intentExtras.getString("apnType");
			String extras = apn + "," + apnType;
		}
		else if (action.equals("android.intent.action.ACTION_DATA_CONNECTION_FAILED"))
		{
			String phoneName = intentExtras.getString("phoneName");
			String reason = intentExtras.getString("reason");
			String extras = phoneName + "," + reason;
		}
		else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
			if (owner.getUsageLimits().getDormantMode() > 0)
				return;
			// wifi state change may trigger the event queue to be sent
			//MMCLogger.logToFile(MMCLogger.Level.DEBUG, "MMCIntentHandlerOld", "NETWORK_STATE_CHANGED_ACTION", "");
			owner.wifiStateChange ((NetworkInfo)intentExtras.getParcelable(WifiManager.EXTRA_NETWORK_INFO));
			owner.trackAccessPoints(0);
			dataMonitorStats.setWifi(PhoneState.isNetworkWifi(owner));
		}
		else if (action.equals(WIMAX_STATE_CHANGE)){
			owner.trackAccessPoints(0);
		}
		else if(action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
//			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//			BluetoothClass bclass = device.getBluetoothClass();
//			int major = bclass.getMajorDeviceClass();
//			if (major == 1024)
//				owner.setBTHeadsetState(1);
//			owner.trackAccessPoints();
		}
		else if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)) {
			owner.trackAccessPoints(0);
		}
		else if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
//			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//			BluetoothClass bclass = device.getBluetoothClass();
//			int major = bclass.getMajorDeviceClass();
//			if (major == 1024)
//				owner.setBTHeadsetState(0);
//			owner.trackAccessPoints();
		}
		else if(action.equals(Intent.ACTION_HEADSET_PLUG)) {
			try {
				int state = intent.getIntExtra("state", -1);
				owner.setHeadsetState(state);
			} catch(Exception e) {
				LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "onReceive", "error receiving Intent.ACTION_HEADSET_PLUG: " + e);
			}
		}
		else if(action.equals(Intent.ACTION_HEADSET_PLUG)) {
			int state = intent.getIntExtra("state", -1);
			owner.setHeadsetState(state);
		}
		else if (action.equals(CommonIntentActionsOld.ACTION_START_VOICETEST)){
			//this is supposed to trigger a speed test
			int trigger = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_VQ_TRIGGER, 0);
			owner.getVQManager().runTest(trigger);
		}
		else if (action.equals(CommonIntentActionsOld.ACTION_TEST_VQ_DEVICE)){
			owner.getVQManager().runTest(10);
		}
		else if (action.equals(CommonIntentActionsOld.ACTION_STOP_VOICETEST)){
			owner.getVQManager().killTest();
		}
		else if (action.equals(RESTART_MMC_SERVICE)){
			owner.restartNextIdle();
			
		}
		else if(action.equals(STOP_TRACKING_ACTION)) {
			owner.getEventManager().stopTracking();
		}
		else if(action.equals(Intent.ACTION_SCREEN_OFF)) {
			try {
				dataMonitorStats.setScreen(false);
				SignalEx mmcSignal = new SignalEx();
				owner.getPhoneStateListener().processNewMMCSignal(mmcSignal);
				if (owner.getTravelDetector() != null)
				{
					owner.getPhoneState().screenChanged(false);
					owner.getEventManager().screenChanged(false);
				}
			} catch (Exception e) {
				LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "onReceive", "received action SCREEN_OFF, calling MainService.processNewMMCSignal()", e);
			}
		}
		else if(action.equals(Intent.ACTION_SCREEN_ON)) {
			dataMonitorStats.setScreen(true);
			if (owner.getTravelDetector() != null)
			{			
				owner.getPhoneState().screenChanged(true);
				owner.getEventManager().screenChanged(true);
			}
		}		
		else if(action.equals(ACTION_ALARM_MINUTE)) {
			// CPU wakes very briefly only in order to spur cellid updates
			owner.getTravelDetector().triggerTravelCheck();
		}
		else if(action.equals(ACTION_TRACKING_5MINUTE)) {
			owner.getTrackingManager().runTracking();
		}
        else if(action.equals(ACTION_TRACKING_1MINUTE)) {
            LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onReceive", "ACTION_TRACKING_1MINUTE");
            owner.getTrackingManager().runTrackingTests();
        }
		else if(action.equals(ACTION_ALARM_3HOUR)) {
			dataMonitorStats.prepareAllStatistics();
			owner.getEventManager().triggerUpdateEvent(true, false);
//			Calendar cal = Calendar.getInstance(); 
//		    int hour = cal.get (Calendar.HOUR_OF_DAY); 
//			//If 12pm to 3 am
//		    if(((hour >= 0 && hour <= 3) || hour == 24) && owner.isNetworkWifi()) {
//				//See if transit info needs to be downloaded
//		    	
//		    	//Don't allow the app to shut down until the work is done, keep the CPU running
//		    	MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "onReceive", "ACTION_ALARM_3HOUR, wakelock turned on and checking transit "
//		    			+ "info uptodate at 24-hour: " + hour);
//		    	wakeLock.acquire();
//		    	
//		    	downloadAreasIfOutOfDate();
//		    }
		}
//		else if(action.equals(ACTION_TRANSIT_DL_DONE)) {
//			//Allow CPU to move on
//			if(wakeLock != null) {
//				wakeLock.release(); 
//				MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "onReceive", "received action ACTION_TRANSIT_DONE, wakelock turned off");
//			}
//		}
//		else if(action.equals(ACTION_TRANSIT_DL_START)) {
//			//Don't allow the app to shut down until the work is done, keep the CPU running
//	    	MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "onReceive", "ACTION_TRANSIT_DL_START, wakelock turned on and downloading transit if was requested");
//	    	wakeLock.acquire();
//	    	
//	    	downloadAreasIfOutOfDate();
//		}
		else if(action.equals(ACTION_ALARM_SCANAPPS)) {
			int intervalDM = PreferenceManager.getDefaultSharedPreferences(owner).getInt(PreferenceKeys.Miscellaneous.MANAGE_DATAMONITOR, 0);
			// run datamonitor if enabled
			if (intervalDM > 0)
			{
				dataMonitorStats.scanApps();
				dataMonitorStats.getRunningAppsString(); // for debug
			}

			// Also using this timer for GCM heartbeats (its a 5 minute heartbeat to tell Google Cloud Messaging to check the socket more often for more reliable push messages)
			// 2 independent timers might wake up device twice as often, doubling the battery impact, so I'm forcing it to use one for both cases
			boolean useHeartbeat = PreferenceManager.getDefaultSharedPreferences(owner).getBoolean("KEY_GCM_HEARTBEAT", false);
			if (useHeartbeat) {
				GcmKeepAlive gcm = new GcmKeepAlive(owner);
				gcm.broadcastIntents();
			}
		}
		else if(action.equals(ACTION_ALARM_15MINUTE)) {
			
			int intervalDM = PreferenceManager.getDefaultSharedPreferences(owner).getInt(PreferenceKeys.Miscellaneous.MANAGE_DATAMONITOR, 0);
			// run datamonitor if enabled
			if (intervalDM > 0)
			{
			    boolean firstBucketDone = PreferenceManager.getDefaultSharedPreferences(owner).getBoolean(PreferenceKeys.Miscellaneous.FIRST_BUCKET, false);
				if (!firstBucketDone) { //if false, first bucket needs to be done
					dataMonitorStats.firstBucket();
					dataMonitorStats.monitor();
					PreferenceManager.getDefaultSharedPreferences(owner).edit().putBoolean(PreferenceKeys.Miscellaneous.FIRST_BUCKET, true).commit();
					LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "MMCIntentHandlerOld", "onReceive", "First bucket at: " + System.currentTimeMillis() / 1000);
				}
				else {
					dataMonitorStats.monitor();
					//MMCLogger.logToFile(MMCLogger.Level.DEBUG, "MMCIntentHandlerOld", "onReceive", "15 min bucket at: " + System.currentTimeMillis()/1000);
				}
			}
			// regardless, still make sure GPS is idle unless needed
			owner.getGpsManager ().safeguardGps ();
		}
		else if(action.equals(GPS_STATE_ON)) {  
			dataMonitorStats.setGps(true);
		}
		else if(action.equals(GPS_STATE_OFF)) {  
			dataMonitorStats.setGps(false);
		}
		else if(action.equals(PHONE_CALL_CONNECT)) {  
			dataMonitorStats.setPhone(true);
		}
		else if(action.equals(PHONE_CALL_DISCONNECT)) {  
			dataMonitorStats.setPhone(false);
		}		
		else if(action.equals(HANDOFF)) {  
			dataMonitorStats.handoff();
		}
		else if(action.equals(MANUAL_TRANSIT_START)) {
			long lat = intent.getIntExtra("latitude", 0);
			long lon = intent.getIntExtra("longitude", 0);
			Location location = new Location("");	
			location.setLatitude(lat/1000000.0);
			location.setLongitude(lon/1000000.0);
			location.setAccuracy(-3);
			
//			PreferenceManager.getDefaultSharedPreferences(owner).edit().putString(
//    				PreferenceKeys.Miscellaneous.SURVEY_COMMAND,).commit();
			PreferenceManager.getDefaultSharedPreferences(owner).edit().putString(
    			"transitEvent", String.valueOf(lat) + ","+ String.valueOf(lon)).commit();
			reportManager.manualTransitEvent = owner.getEventManager().triggerSingletonEvent(EventType.MAN_TRANSIT);
			reportManager.manualTransitEvent.setLocation(location, 0);
			owner.getTravelDetector().setTravelling (false);
		}
		else if(action.equals(MANUAL_TRANSIT_END)) {
			if(reportManager.manualTransitEvent == null)
				return;
			String accelData = intent.getStringExtra("accelerometer");
			int stationFrom = intent.getIntExtra("stationFrom", 0);
			int stationTo = intent.getIntExtra("stationTo", 0);
			int duration = intent.getIntExtra("duration", 0);
			int corrected = intent.getIntExtra("corrected", 0);
			if (corrected != 0)
			{
				Location location = reportManager.manualTransitEvent.getLocation();
				location.setAccuracy(-4);
			}

			reportManager.manualTransitEvent.setAppData(accelData); //TODO want this to really be in appdata?
			reportManager.manualTransitEvent.setLookupid1(stationFrom);
			reportManager.manualTransitEvent.setLookupid2(stationTo);
			reportManager.manualTransitEvent.setDuration(duration);
			owner.getEventManager().unstageAndUploadEvent(reportManager.manualTransitEvent, null);
			reportManager.manualTransitEvent = null;
		}
		else if(action.equals(MANUAL_TRANSIT_CANCEL)) {
			if(reportManager.manualTransitEvent != null) {
				owner.getEventManager().unstageEvent(reportManager.manualTransitEvent);
				ReportManager reportManager = ReportManager.getInstance(owner);
				reportManager.getDBProvider().delete(TablesEnum.LOCATIONS.getContentUri(), "timestamp > ? And accuracy = 3", new String[]{ String.valueOf(reportManager.manualTransitEvent.getEventTimestamp())});
				reportManager.manualTransitEvent = null;
			}
		}
		else if(action.equals(MANUAL_PLOTTING_START)) {
			int floor = intent.getIntExtra("floor", 0);
//			int type = intent.getIntExtra("type", -1);  //1(indoor) or 2(outdoor)
			int topFloor = intent.getIntExtra("top", -1);
			int lat = intent.getIntExtra("latitude", -1);
			int lng = intent.getIntExtra("longitude", -1);
			long osm_id = intent.getLongExtra("osm_id", 0);
			String poly = intent.getStringExtra("poly");

			reportManager.manualPlottingEvent = owner.getEventManager().triggerSingletonEvent(EventType.MAN_PLOTTING);
			reportManager.manualPlottingEvent.setEventIndex(floor);
			reportManager.manualPlottingEvent.setDuration(topFloor);
			reportManager.manualPlottingEvent.setBuildingID(osm_id);
			reportManager.manualPlottingEvent.setAppData(poly);
			Location location = new Location("");	
			location.setLatitude(lat / 1000000.0);
			location.setLongitude(lng / 1000000.0);
			location.setAccuracy(-1);
			reportManager.updateEventField(reportManager.manualPlottingEvent.getLocalID(), "latitude", Double.toString(location.getLatitude()));
			reportManager.updateEventField(reportManager.manualPlottingEvent.getLocalID(), "longitude", Double.toString(location.getLongitude()));

			reportManager.manualPlottingEvent.setLocation(location, 0);
			presetEventId (reportManager.manualPlottingEvent);  // reserve an EventID for this manual sampling event, to be used for Share links

			owner.getTravelDetector().setTravelling (false);
		}
		else if(action.equals(MANUAL_PLOTTING_END)) {
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "onReceive", "MANUAL_PLOTTING_END");
//			owner.getEventManager().unstageEvent(manulaPlottingEvent); //does not upload
			if(reportManager.manualPlottingEvent != null)
				owner.getEventManager().unstageAndUploadEvent(reportManager.manualPlottingEvent, null);
			//After the event was submitted, reset it so we don't restore an old event in ManualMapping
			reportManager.manualPlottingEvent = null;
		}
		else if(action.equals(MANUAL_PLOTTING_CANCEL)) {
			if(reportManager.manualPlottingEvent != null)
			{
				reportManager.getDBProvider().delete(TablesEnum.LOCATIONS.getContentUri(), "timestamp > ? And accuracy < 0", new String[]{ String.valueOf(reportManager.manualPlottingEvent.getEventTimestamp())});
				reportManager.manualPlottingEvent = null;
				
			}
		}
		else if(intent.getAction().equals(SMS_RECEIVED)) {
			SmsMessage[] msgs = null;
//			String msg_from;
			if (intentExtras == null) 
				return;

			Object[] pdus = (Object[]) intentExtras.get("pdus");
        	msgs = new SmsMessage[pdus.length];
        	String[] msgBody = new String[msgs.length];
         	for(int i=0; i < msgs.length; i++) {
	        	msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
	//            msg_from = msgs[i].getOriginatingAddress();
				String msg = msgs[i].getMessageBody().trim();
				if (msg.length()> 10)
				{
					msg = msg.substring(1,msg.length()-1);
					msg = "{" + msg + "}";
				}

	         	msgBody[i] = msg;
	         	
         	}
         	handleCommands(msgBody, true, 0);
		}
		else if(action.equals(VIEWING_SIGNAL)) {  
			owner.setEnggQueryTime();
		}
		else if(intent.getAction().equals(SURVEY)) {
			if (intentExtras == null) 
				return;
            
			int surveyid = intentExtras.getInt(SURVEY_EXTRA); 
			postSurvey (surveyid);
		}
        else if(intent.getAction().equals(GCM_MESSAGE)) {
            if (intentExtras == null)
                return;
            try {
                String msg = intentExtras.getString(GCM_MESSAGE_EXTRA);
				long starttime = intentExtras.getLong("GCM_STARTTIME_EXTRA");
                EventResponse eventResponse = gson.fromJson(msg, EventResponse.class);
                eventResponse.init();
				eventResponse.setStartTime (starttime);
                eventResponse.handleEventResponse(owner, true);
            }catch (Exception e)
            {
                LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "onReceived GCM_MESSAGE", "exception", e);
            }
        }
		else if(intent.getAction().equals(COMMAND)) {
			if (intentExtras == null) 
				return;
            
			String commands = intentExtras.getString(COMMAND_EXTRA);
			if (commands == null)
				return;
			long starttime = intent.getLongExtra("STARTTIME_EXTRA", 0);
			String[] msgs = null;
			try {
				JSONArray cmds = new JSONArray(commands);
				msgs = new String[cmds.length()];
				for (int j=0; j<cmds.length(); j++)
					msgs[j] = cmds.getJSONObject(j).toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			//commands = commands.replace("[", "");
			//commands = commands.replace("]", "");
        	//String msgs[] = commands.split("/");
        	handleCommands(msgs, false, starttime);
		}

		else if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED))
		{
			int val = 0;
			val = intent.getIntExtra("android.telecom.extra.CALL_DISCONNECT_CAUSE", -1);
			String msg = intent.getStringExtra("android.telecom.extra.CALL_DISCONNECT_CAUSE");
			String msg2 = intent.getStringExtra("android.telecom.extra.CALL_DISCONNECT_MESSAGE");
			//MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "String CALL_DISCONNECT_CAUSE", msg);
            //MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "Int CALL_DISCONNECT_CAUSE", ""+val);
		}
        else if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL"))
        {
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            //if (phoneNumber != null)
            //    MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "NEW_OUTGOING_CALL", phoneNumber);
        }

		else if (intent.getAction().equals("android.intent.action.PRECISE_CALL_STATE"))
		{
			int val = 0;
			//val = intent.getIntExtra("android.telecom.extra.CALL_DISCONNECT_CAUSE", -1);
			int state_ringing = intent.getIntExtra("ringing_state",-1);
			int state_foreground = intent.getIntExtra("foreground_state",-1);
			int state_background = intent.getIntExtra("background_state",-1);
			
			int disconnect_cause = intent.getIntExtra("disconnect_cause",-1);
			int precise_disconnect_cause = intent.getIntExtra("precise_disconnect_cause",-1);
			PreciseCallCodes precisecall = new PreciseCallCodes(state_ringing,state_foreground,state_background,disconnect_cause,precise_disconnect_cause);
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "PRECISE_CALL_STATE", precisecall.toString());
			owner.getPhoneStateListener().onPreciseCallState(precisecall);
		}
		else if (intent.getAction().equals("android.intent.action.PRECISE_DATA_CONNECTION_STATE_CHANGED"))
		{
			int val = 0;
			//val = intent.getIntExtra("android.telecom.extra.CALL_DISCONNECT_CAUSE", -1);
			int state = intent.getIntExtra("state", -1);
			int networkType = intent.getIntExtra("networkType",-1);
			String reason = intent.getStringExtra("reason");
			String failCause = intent.getStringExtra("failCause");
			String apnType = intent.getStringExtra("apnType");
			String apn = intent.getStringExtra("apn");
			Object linkProperties = intent.getParcelableExtra("linkProperties");
			if (!apnType.equals("default"))
				return;
//			if (failCause != null && failCause.length() > 0)
//			{
//				String msg = String.format("state:%d,netType:%d,apnType:%s,reason:%s\r\nfailCause:%s", state,networkType,apnType,reason,failCause);
//				MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "PRECISE_DATA_CONNECTION_STATE_CHANGED", msg);
//				if (linkProperties != null)
//				{
//					msg = String.format("PRECISE_DATA_CONNECTION_STATE_CHANGED  linkProperties:%s", linkProperties.toString());
//					MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "PRECISE_DATA_CONNECTION_STATE_CHANGED", msg);
//				}
//			}
//			else if (reason != null && reason.length() > 0)
//			{
//				String msg = String.format("state:%d,netType:%d,apnType:%s,reason:%s\r\nfailCause:%s", state,networkType,apnType,reason,failCause);
//				MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "PRECISE_DATA_CONNECTION_STATE_CHANGED", msg);
//				if (linkProperties != null)
//				{
//					msg = String.format("PRECISE_DATA_CONNECTION_STATE_CHANGED  linkProperties:%s", linkProperties.toString());
//					MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "PRECISE_DATA_CONNECTION_STATE_CHANGED", msg);
//				}
//			}
		}
		else if (intent.getAction().equals("org.mobicents.restcomm.android.CONNECT_FAILED") ||
				intent.getAction().equals("org.mobicents.restcomm.android.CALL_STATE") ||
				intent.getAction().equals("org.mobicents.restcomm.android.DISCONNECT_ERROR")) {
			owner.getPhoneStateListener().getRestCommManager().handleIntent (owner, intent);
		}
		else if (intent.getAction().equals(ACTION_RADIOLOG_DISCONNECT))
		{
			String time = intent.getStringExtra(EXTRA_RADIOLOG_TIME);
			String cause = intent.getStringExtra(EXTRA_RADIOLOG_DISC_CAUSE);
			owner.getPhoneStateListener().onDisconnect(time, cause);
		}
		else if (intent.getAction().equals(ACTION_RADIOLOG_CONNECT))
		{
			String time = intent.getStringExtra(EXTRA_RADIOLOG_TIME);
			String state = intent.getStringExtra(EXTRA_RADIOLOG_CONN_STATE);
			owner.getPhoneStateListener().onConnect(time, state);
		}
		else if (intent.getAction().equals(ACTION_RADIOLOG_NEIGHBORS))
		{
			String time = intent.getStringExtra(EXTRA_RADIOLOG_TIME);
			String neighbors = intent.getStringExtra(EXTRA_RADIOLOG_NEIGHBORS);
			//owner.onConnect(time, state);
		}
		else if (intent.getAction().equals(ACTION_RADIOLOG_SERVICEMODE))
		{
			String time = intent.getStringExtra(EXTRA_RADIOLOG_TIME);
			String jsonstr = intent.getStringExtra(EXTRA_RADIOLOG_SVC_JSON);
			JSONObject json = null;
			try{json = new JSONObject(jsonstr);}
			catch (Exception e){}
			String values = intent.getStringExtra(EXTRA_RADIOLOG_SVC_TEXT);
			String name = intent.getStringExtra(EXTRA_RADIOLOG_SVC_NAME);
			owner.getPhoneStateListener().onServiceMode(time, json, values, name);
		}
		else if (intent.getAction().equals(ACTION_RADIOLOG_SERVICEMENU))
		{
			String time = intent.getStringExtra(EXTRA_RADIOLOG_TIME);
			String values = intent.getStringExtra(EXTRA_RADIOLOG_SVC_TEXT);
			String name = intent.getStringExtra(EXTRA_RADIOLOG_SVC_NAME);
			owner.getPhoneStateListener().onServiceMenu(time, values, name);
		}
		else if (intent.getAction().equals(ACTION_MMCSYS_VERSION))
		{
			Integer version = intent.getIntExtra(EXTRA_MMCSYS_VERSION, 0);
			owner.onSvcModeVersion(version);
		}
		else if (intent.getAction().equals(ACTION_RADIOLOG_ERROR))
		{
			String error = intent.getStringExtra(EXTRA_RADIOLOG_ERROR);
			String details = intent.getStringExtra(EXTRA_RADIOLOG_ERROR_DETAIL);
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, "RilReader", error, details);
			//owner.onConnect(time, state);
		}
		
	}
	
	public void handleCommands(String msgs[], boolean sms, long starttime) {
		try {
			for(int i = 0; i < msgs.length; i++) {
				String msgBody = msgs[i];


	    		if(msgBody.indexOf("\"mmctype\":") != -1) {
                    LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "handleCommands", msgBody);
	    			if (sms)
	    				abortBroadcast();
	    			msgBody = msgBody.replace('(', '{');
	    			msgBody = msgBody.replace(')', '}');
	    			int pos = msgBody.indexOf('{');
	    			if (pos >= 0)
	    				msgBody = msgBody.substring(pos);
	                    
                	JSONObject remoteJson = new JSONObject(msgBody); // .getJSONObject("stat");
                	String testType = "";
					if (remoteJson.has("mmctype"))
						testType = remoteJson.getString("mmctype");

					if(msgBody.indexOf("\"schedule\":") != -1) {
						PreferenceManager.getDefaultSharedPreferences(owner).edit().putString(PreferenceKeys.Miscellaneous.DRIVE_TEST_CMD, remoteJson.toString ()).commit();
						owner.getTrackingManager().startAdvancedTracking (remoteJson, starttime, true);
					}
                	else if (testType.equals("rt")) {
						PreferenceManager.getDefaultSharedPreferences(owner).edit().putString(PreferenceKeys.Miscellaneous.DRIVE_TEST_CMD, remoteJson.toString ()).commit();
						owner.getTrackingManager().startTracking (remoteJson, starttime, true);
                	}
                	else if (testType.equals("ue")) {
						owner.getEventManager().triggerUpdateEvent(false, false);
                	}
					else if (testType.equals("sms")) {
						owner.getEventManager().receivedSMSTestReply(remoteJson);
					}
                    else if (testType.equals("smstest")){
                		//owner.triggerSMSTest(3);
						owner.getEventManager().queueActiveTest(EventType.SMS_TEST, 3);
                	}
                    else if (testType.equals("latency")){
                		//owner.runLatencyTest(false);
						owner.getEventManager().queueActiveTest(EventType.LATENCY_TEST, 3);
                	}
                    else if (testType.equals("speed")){
                        LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "handleCommands", "triggerSpeedTest");
                		//owner.triggerSpeedTest(3);
						owner.getEventManager().queueActiveTest(EventType.MAN_SPEEDTEST, 3);
                	}
                    else if (testType.equals("video")){
                        //owner.triggerActiveTest(3, EventType.VIDEO_TEST.getIntValue());
						owner.getEventManager().queueActiveTest(EventType.VIDEO_TEST, 3);
                    }
                    else if (testType.equals("audio")){
                        //owner.triggerActiveTest(3, EventType.AUDIO_TEST.getIntValue());
						owner.getEventManager().queueActiveTest(EventType.AUDIO_TEST, 3);
                    }
                    else if (testType.equals("web")){
                        //owner.triggerActiveTest(3, EventType.WEBPAGE_TEST.getIntValue());
						owner.getEventManager().queueActiveTest(EventType.WEBPAGE_TEST, 3);
					}
					else if (testType.equals("youtube")){
						//owner.triggerActiveTest(3, EventType.WEBPAGE_TEST.getIntValue());
						owner.getEventManager().queueActiveTest(EventType.YOUTUBE_TEST, 3);
					}
                    else if (testType.equals("ws")){
                        owner.getWebSocketManager().runWebSocket(true);
					}
					else if (testType.equals("fill")){
						owner.getEventManager().triggerFillinEvent();
					}
                    else if (testType.equals("vq")){
                        LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "runTracking", "beginVoiceTest");
						owner.getEventManager().queueActiveTest(EventType.EVT_VQ_CALL, 3);
					} else if (testType.equals("stop")){
                        owner.getEventManager().stopTracking();
					}
					else if (testType.equals("refresh")){
						// Force immediate event with no real payload, simply to obtain an event response with new settings
						owner.getEventManager().triggerSingletonEvent(EventType.EVT_NONE);
					}
	    		}

			}
	    } catch(Exception e){
	    	LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "handleCommands", "exception", e);
	    }	
	}

	public IntentFilter declareIntentFilters() {
		IntentFilter intentFilter = new IntentFilter(IntentHandler.UPDATE_ACTION);
		intentFilter.addAction(IntentHandler.START_TRACKING_ACTION);
		intentFilter.addAction(IntentHandler.TRACK_REMOTE);
		intentFilter.addAction(IntentHandler.STOP_TRACKING_ACTION);
		intentFilter.addAction(IntentHandler.SPEED_TEST);
		intentFilter.addAction(IntentHandler.RUN_WEBSOCKET);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_START_UI);

		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_LOCATION_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_CELL_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_GPS_STATUS_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_SIGNAL_STRENGTH_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_EVENT_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_NEIGHBOR_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_NETWORK_UPDATE);
		//intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_RX_TX);

		intentFilter.addAction(IntentHandler.ACTION_SPEEDTEST_RESULT);
		intentFilter.addAction(IntentHandler.ACTION_SPEEDTEST_ERROR);
		intentFilter.addAction(IntentHandler.ACTION_SPEEDTEST_COMPLETE);
		intentFilter.addAction(IntentHandler.ACTION_SPEEDTEST_CANCELLED);

		intentFilter.addAction(IntentHandler.ACTION_WEBTEST_RESULT);
		intentFilter.addAction(IntentHandler.ACTION_WEBTEST_ERROR);
		intentFilter.addAction(IntentHandler.ACTION_WEBTEST_COMPLETE);
		intentFilter.addAction(IntentHandler.ACTION_WEBTEST_CANCELLED);

		//do not add filter if sms test permissions aren't all allowed
		if (PreferenceKeys.getSMSPermissionsAllowed(owner, true))
			intentFilter.addAction(IntentHandler.SMS_TEST);

		intentFilter.addAction(IntentHandler.SMS_DELIVERED);
		intentFilter.addAction(IntentHandler.LATENCY_TEST);
		intentFilter.addAction(IntentHandler.ACTION_STOP_SPEEDTEST);
		intentFilter.addAction(IntentHandler.RESTART_MMC_SERVICE);
		intentFilter.addAction(IntentHandler.COLDRESET_ACTION);
		intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
		intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction(IntentHandler.COMMAND);
		intentFilter.addAction(IntentHandler.SURVEY);
		//intentFilter.addAction(Intent.ACTION_VIEW);
		//intentFilter.addAction("android.intent.PHONE_STATE");
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		intentFilter.addAction(IntentHandler.ACTION_ALARM_MINUTE);
		intentFilter.addAction(IntentHandler.ACTION_TRACKING_5MINUTE);
		intentFilter.addAction(IntentHandler.ACTION_TRACKING_1MINUTE);
		intentFilter.addAction(IntentHandler.ACTION_ALARM_3HOUR);
		intentFilter.addAction(IntentHandler.ACTION_ALARM_15MINUTE);
		intentFilter.addAction(IntentHandler.ACTION_ALARM_SCANAPPS);
		intentFilter.addAction(IntentHandler.EMAIL_CSV);
		intentFilter.addAction(IntentHandler.GPS_STATE_OFF);
		intentFilter.addAction(IntentHandler.GPS_STATE_ON);
		intentFilter.addAction(IntentHandler.PHONE_CALL_CONNECT);
		intentFilter.addAction(IntentHandler.PHONE_CALL_DISCONNECT);
		intentFilter.addAction(IntentHandler.HANDOFF);
		intentFilter.addAction(IntentHandler.SMS_SENT);
		intentFilter.addAction(IntentHandler.SMS_RECEIVED);
		intentFilter.addAction(Intent.ACTION_SEND);
		intentFilter.addAction(IntentHandler.MANUAL_PLOTTING_START);
		intentFilter.addAction(IntentHandler.MANUAL_PLOTTING_END);
		intentFilter.addAction(IntentHandler.MANUAL_PLOTTING_CANCEL);
		intentFilter.addAction(IntentHandler.MANUAL_TRANSIT_START);
		intentFilter.addAction(IntentHandler.MANUAL_TRANSIT_END);
		intentFilter.addAction(IntentHandler.MANUAL_TRANSIT_CANCEL);
		intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
		intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

		intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
		intentFilter.addAction(IntentHandler.ROAMING_ON);
		intentFilter.addAction(IntentHandler.ROAMING_OFF);

		intentFilter.addAction(IntentHandler.VIEWING_SIGNAL);
		intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		intentFilter.addAction("android.intent.action.PRECISE_CALL_STATE");
		intentFilter.addAction("android.intent.action.PRECISE_DATA_CONNECTION_STATE_CHANGED");
		intentFilter.addAction("android.intent.action.PHONE_STATE");
		intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");

		intentFilter.addAction("android.intent.action.DATA_CONNECTION_CONNECTED_TO_PROVISIONING_APN");
		intentFilter.addAction("android.intent.action.ANY_DATA_STATE");
		intentFilter.addAction("android.intent.action.ACTION_DATA_CONNECTION_FAILED");

		intentFilter.addAction(CommonIntentActionsOld.ACTION_BLUETOOTH_ENDDOWNLOAD);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_START_VOICETEST);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_STOP_VOICETEST);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_TEST_VQ_DEVICE);

		intentFilter.addAction(IntentHandler.ACTIVE_TEST);
		intentFilter.addAction(IntentHandler.ACTION_STOP_VIDEOTEST);
		intentFilter.addAction(IntentHandler.GCM_MESSAGE);

		intentFilter.addAction ("org.mobicents.restcomm.android.CONNECT_FAILED");
		intentFilter.addAction ("org.mobicents.restcomm.android.CALL_STATE");
		intentFilter.addAction ("org.mobicents.restcomm.android.DISCONNECT_ERROR");

		intentFilter.addAction (IntentHandler.ACTION_RADIOLOG_DISCONNECT);
		intentFilter.addAction (IntentHandler.ACTION_RADIOLOG_CONNECT);
		intentFilter.addAction (IntentHandler.ACTION_RADIOLOG_NEIGHBORS);
		intentFilter.addAction (IntentHandler.ACTION_RADIOLOG_SERVICEMODE);
		intentFilter.addAction (IntentHandler.ACTION_RADIOLOG_SERVICEMENU);
		intentFilter.addAction (IntentHandler.ACTION_MMCSYS_VERSION);

		//intentFilter.addAction(Intent.ACTION_APP_ERROR);
		int raisePriority = owner.getPackageManager().checkPermission("android.permission.RAISED_THREAD_PRIORITY", owner.getPackageName());
		// With permission to raise priority for SMS messages
		if (raisePriority == 0 )
			intentFilter.setPriority(9999999);

		return intentFilter;
	}
	
	private void postSurvey (int surveyid)
	{
		int icon = 0;
		int customIcon = (owner.getResources().getInteger(R.integer.CUSTOM_NOTIFIER));
		if (customIcon == 0)
			icon = R.drawable.ic_stat_mmcactive;
		else
			icon = R.drawable.ic_stat_notification_icon;

		if(surveyid > 0) {

            int curr_id = PreferenceManager.getDefaultSharedPreferences(owner).getInt("surveyid", 0);

            if (curr_id == surveyid)
                return;
            //resultIntent.putExtra("id", surveyid);
			requestQuestions(surveyid);
		}
		else { //default survey id = 1
			//resultIntent.putExtra("id",1);
			requestQuestions(1);
		}

		String message = owner.getString (R.string.survey_notification);
		NotificationManager notificationManager = (NotificationManager) owner.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, message, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Intent notificationIntent = new Intent();
		notificationIntent.setComponent(new ComponentName(owner.getPackageName(), "com.cortxt.app.mmcui.Activities.SatisfactionSurvey"));
		notificationIntent.putExtra("id", surveyid);
		notificationIntent.setData((Uri.parse("foobar://"+SystemClock.elapsedRealtime())));
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		int MMC_SURVEY_NOTIFICATION = 8011;
		PendingIntent pendingIntent = PendingIntent.getActivity(owner, MMC_SURVEY_NOTIFICATION + surveyid, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		notification.setLatestEventInfo(owner, message, message, pendingIntent);
		notificationManager.notify(MMC_SURVEY_NOTIFICATION, notification);
	}

	public class GcmKeepAlive  {

		protected CountDownTimer timer;

		protected Context mContext;
		protected Intent gTalkHeartBeatIntent;
		protected Intent mcsHeartBeatIntent;

		public GcmKeepAlive(Context context) {
			mContext = context;
			gTalkHeartBeatIntent = new Intent(
					"com.google.android.intent.action.GTALK_HEARTBEAT");
			mcsHeartBeatIntent = new Intent(
					"com.google.android.intent.action.MCS_HEARTBEAT");
		}

		public void broadcastIntents() {
			System.out.println("sending heart beat to keep gcm alive");
			mContext.sendBroadcast(gTalkHeartBeatIntent);
			mContext.sendBroadcast(mcsHeartBeatIntent);
		}

	}

	private String getSurveyUrlString(int surveyId) {
		int tempSurveyId = 0;
		//Request questions from server, otherwise use strings file
		//SharedPreferences securePreferences = MainService.getSecurePreferences(this);
		String apiKey = MainService.getApiKey(owner);
		if(apiKey != null) {
			if(surveyId != 0)
				tempSurveyId = surveyId;
			return "/api/surveys/questions?sort=id&instanceid=" + tempSurveyId + "&apiKey=" + apiKey;
		}
		return apiKey;
	}

	public void requestQuestions(final int surveyId) {
		//Request questions from server, otherwise use strings file
		final String urlExtra = getSurveyUrlString(surveyId);
		if(urlExtra == null)
			return;

		new Thread(
				new Runnable() {
					@Override
					public void run() {

						String responseContents = "";
						try {
							// create the HttpURLConnection
							URL url = new URL(owner.getString(R.string.MMC_URL_LIN) + urlExtra);
							HttpURLConnection connection = (HttpURLConnection) url.openConnection();
							connection.setConnectTimeout(10000);
							connection.setReadTimeout(10000);
							connection.setRequestMethod("GET");
							connection.connect ();
							int response = connection.getResponseCode();
							responseContents = WebReporter.readString(connection);

							if(responseContents != null)
							{
								PreferenceManager.getDefaultSharedPreferences(owner).edit().putString("survey", responseContents).commit();
								PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt("surveyid", surveyId).commit();
							}

						} catch(Exception e) {
							System.out.println("error");
						}
					}
				}).start();
	}

	// Reserve an event ID to use for the event, so we can use it for share-links
	public void presetEventId(final EventObj event)
	{
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String master_url = owner.getString(R.string.MMC_URL_SPEED);
					String apikey = Global.getApiKey(owner);
					String master_complete_url = master_url + "/choose?apiKey=" + apikey;
					master_complete_url += "&ipv4=" + DeviceInfo.getIPAddress();
					String responseString = WebReporter.getHttpURLResponse(master_complete_url, true);
					//complete URL
					JSONObject json = new JSONObject(responseString);
					String eventId = json.getString("eventid");
					Long eventid = Long.valueOf(eventId);
					event.setEventId (eventid);
				} catch (Exception ex) {
					LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "presetEventId", "exception", ex);

				}
			}
		}).start();
	}
}

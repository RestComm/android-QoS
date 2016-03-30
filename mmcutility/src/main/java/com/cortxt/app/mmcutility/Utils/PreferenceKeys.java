package com.cortxt.app.mmcutility.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
//import android.preference.PreferenceManager;
import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.securepreferences.SecurePreferences;


public final class PreferenceKeys {
	
	//Profile related constants
	public final class Profile {
		public static final String PROFILE = "KEY_PROFILE";
		
		public final class Maximum {
			public static final String MONITOR_CALL_QUALITY = "KEY_MONITOR_CALL_QUALITY_MAXIMUM";
			public static final String DISABLE_DATA_WHEN_ROAMING = "KEY_DISABLE_DATA_WHEN_ROAMING_MAXIMUM";
			public static final String TURN_OFF_MONITORING = "KEY_TURN_OFF_MONITORING_MAXIMUM";
		}
		
		public final class Balanced {
			public static final String MONITOR_CALL_QUALITY = "KEY_MONITOR_CALL_QUALITY_BALANCED";
			public static final String DISABLE_DATA_WHEN_ROAMING = "KEY_DISABLE_DATA_WHEN_ROAMING_BALANCED";
			public static final String TURN_OFF_MONITORING = "KEY_TURN_OFF_MONITORING_BALANCED";
		}
		
		public final class Minimum {
			public static final String MONITOR_CALL_QUALITY = "KEY_MONITOR_CALL_QUALITY_MINIMUM";
			public static final String DISABLE_DATA_WHEN_ROAMING = "KEY_DISABLE_DATA_WHEN_ROAMING_MINIMUM";
			public static final String TURN_OFF_MONITORING = "KEY_TURN_OFF_MONITORING_MINIMUM";
		}
	}

	private static SecurePreferences securePrefs = null;
	public static com.securepreferences.SecurePreferences getSecurePreferences (Context context)
	{
		if (securePrefs == null)
			securePrefs = new com.securepreferences.SecurePreferences(context);
		return securePrefs;
	}

    public static String getSecurePreferenceString (String key, String def, Context context)
    {
        SharedPreferences securePref = getSecurePreferences(context);
        String value = securePref.getString (key, def);
        return value;
    }

	public static int getSecurePreferenceInt (String key, int def, Context context)
    {
        SharedPreferences securePref = getSecurePreferences(context);
        int value = securePref.getInt(key, def);
        return value;
    }

	public static boolean getSMSPermissionsAllowed(Context context, boolean checkForSMSSpeedTest)
	{
		PackageManager pkMan = context.getPackageManager();
		int smsSendPermissionValue = pkMan.checkPermission("android.permission.SEND_SMS", context.getPackageName());
		int smsReceivePermissionValue = pkMan.checkPermission("android.permission.RECEIVE_SMS", context.getPackageName());
		int smsReadPermissionValue = pkMan.checkPermission("android.permission.READ_SMS", context.getPackageName());

		if (smsSendPermissionValue==0 && smsReceivePermissionValue==0 && smsReadPermissionValue==0 && checkForSMSSpeedTest)
			return true;
		else if (smsReadPermissionValue==0 && !checkForSMSSpeedTest)
			return true;

		return false;
	}

	public static boolean isEventPermitted (Context context, EventType eventType, int trigger)
	{
		if (eventType == EventType.SMS_TEST) {
			if (!PreferenceKeys.getSMSPermissionsAllowed(context, true))
				return false;
		}
		else if (eventType == EventType.EVT_VQ_CALL) {
			PackageManager pkMan = context.getPackageManager();
			int voiceCallPermissionValue = pkMan.checkPermission("android.permission.CALL_PHONE", context.getPackageName()) | pkMan.checkPermission("android.permission.RECORD_AUDIO", context.getPackageName());
			if (voiceCallPermissionValue != 0)
				return false;
		}
		else if (trigger > 0 && (eventType == EventType.VIDEO_TEST || eventType == EventType.WEBPAGE_TEST || eventType == EventType.AUDIO_TEST || eventType == EventType.YOUTUBE_TEST)) {
			PackageManager pkMan = context.getPackageManager();
			int permissionValue = pkMan.checkPermission("android.permission.SYSTEM_ALERT_WINDOW", context.getPackageName());
			if (permissionValue != 0)
				return false;
		}
		return true;
	}

	//Monitoring related constants
	public final class Monitoring {
		@Deprecated
		public static final String QOS_MONITORING = "KEY_QOS_MONITORING";
		public static final String MONITOR_QOS = "KEY_MONITOR_QOS";
		public static final String MONITOR_COVERAGE = "KEY_MONITOR_COVERAGE";
		public static final String MONITOR_DATASPEED = "KEY_MONITOR_DATASPEED";
		public static final String MONITOR_WIFI_NOT = "KEY_MONITOR_WIFI_NOT";	//TODO why does the 'not' exist?
		public static final String MONITOR_ACVTIVESCAN = "KEY_MONITOR_ACTIVESCAN";
		public static final String NOTIFICATION_EXPIRY = "KEY_NOTIFICATION_EXPIRY";
		@Deprecated
		public static final String DATASPEED_MONITORING = "KEY_DATASPEED_MONITORING";
		@Deprecated
		public static final String WIFI_MONITORING = "KEY_WIFI_MONITORING";
		@Deprecated
		public static final String DIS_DATA_MONITORING = "KEY_DIS_DATA_MONITORING";
		@Deprecated
		public static final String FRONT_MONITORING = "KEY_FRONT_MONITORING";
	}
	
	//User related constants
	public final class User {
		public static final String USER_LEVEL = "KEY_USER_LEVEL";
		/**
		 * The id of the user as assigned by the server.
		 */
		public static final String USER_ID = "KEY_USER_ID";
		
		/**
		 * The id of the user as assigned by the server.
		 */
		public static final String APIKEY = "API_KEY_PREFERENCE";
		
		/**
		 * The email address that the user used to register with the server.
		 */
		public static final String USER_EMAIL = "KEY_USER_EMAIL";
		
		/**
		 * Whether the user wants passive speed tests
		 */
		public static final String PASSIVE_SPEED_TEST = "KEY_PASSIVE_SPEED_TEST";
		
		/**
		 * The email address that the user used to register with the server.
		 */
		public static final String CONTACT_EMAIL = "KEY_SETTINGS_CONTACT_EMAIL";
		/**
		 * The users twitter handle
		 */
		public static final String TWITTER = "KEY_SETTINGS_TWITTER";
		/**
		 * Whether the user was asked for a twitter handle yet
		 */
		public static final String ASKED_TWITTER = "KEY_SETTINGS_ASKED_TWITTER";
		/**
		 * The password that goes with the {@link #USER_EMAIL} to form a valid credential pair.
		 */
		public static final String USER_PASSWORD = "KEY_USER_PASSWORD";
		
		public static final String SHOW_MAP_TIP = "KEY_SHOW_MAP_TIP";
		public static final String SHOW_SPEED_TIP = "KEY_SHOW_SPEED_TIP";
		
		/**
		 * The last known version of the app, to detect upgrades
		 */
		public static final String VERSION = "KEY_VERSION";
		/**
		 * The id of the user as assigned by the server.
		 */
		public static final String USER_OBJECTID = "KEY_USER_OBJECTID";
		/**
		 * The permission of the user as assigned by the server.
		 */
		public static final String USER_PERMISSION = "KEY_USER_PERMISSION";

	}
	
	//Miscellaneous constants
	public final class Miscellaneous {
		public static final String MISC_OPT_CHARGING = "KEY_MISC_OPT_CHARGING";
		public static final String MISC_OPT_TIPS = "KEY_MISC_OPT_TIPS";
		public static final String MISC_OPT_DISABLEROAMING = "KEY_MISC_OPT_DISABLEROAMING";
		public static final String MISC_OPT_SHOWDURINGCALL = "KEY_MISC_OPT_SHOWDURINGCALL";
		public static final String MISC_OPT_SHOWHTTPERROR = "KEY_MISC_OPT_SHOWHTTPERROR";
		public static final String MISC_OPT_GPSRESTART = "KEY_MISC_OPT_GPSRESTART";
		public static final String CURRENT_CARRIER = "KEY_MISC_CURRENT_CARRIER";
		//public static final String MISC_OPT_SHARE_WITH_CARRIER = "KEY_SETTINGS_SHARE_WITH_CARRIER";
		public static final String SAMPLING_WALKING = "KEY_SAMPLING_WALKING";
		public static final String SIGNAL_STRENGTH = "KEY_SIGNAL_STRENGTH";
		public static final String BB_SIGNAL = "KEY_BB_SIGNAL";
		public static final String BB_SIGNAL_TIMESTAMP = "KEY_BB_SIGNAL_TIMESTAMP";
		public static final String BB_ALARM_TIMESTAMP = "KEY_BB_ALARM_TIMESTAMP";
		public static final String APK_INSTALL_STATUS = "KEY_APK_INSTALL_STATUS";
		public static final String MONITOR_DATA = "KEY_SETTINGS_MONITOR_DATA";
		public static final String SURVEY_COMMAND = "KEY_SURVEY_COMMAND";
		public static final String MANAGE_DATAMONITOR = "KEY_SETTINGS_MANAGE_DATAMONITOR";
		public static final String APPSCAN_DATAMONITOR = "KEY_SETTINGS_APPSCAN_DATAMONITOR";
		public static final String MISC_OPT_COLLECT_USAGE_DATA = "KEY_SETTINGS_COLLECT_USAGE_DATA";
		public static final String START_ON_BOOT = "KEY_SETTINGS_START_ON_BOOT";
		public static final String STOPPED_SERVICE = "KEY_SETTINGS_STOPPED_SERVICE";
		public static final String MONITOR_IN_SLEEP = "KEY_SETTINGS_MONITOR_IN_SLEEP";
		public static final String TRAVEL_DETECT = "KEY_SETTINGS_TRAVEL_DETECT";
		public static final String ROOT_ACCESS_SWITCH = "KEY_SETTINGS_ROOT_ACCESS";
		public static final String PASSIVE_SPEEDTEST_SERVER = "KEY_PASSIVE_SPEEDTEST_SERVER";
		public static final String TRAVEL_ENABLE = "KEY_SETTINGS_TRAVEL_ENABLE";
		public static final String CHANGED_TRAVEL = "KEY_USER_CHANGED_TRAVEL";
		public static final String CHANGED_NOTIFY = "KEY_USER_CHANGED_NOTIFY";
		public static final String USAGE_PROFILE = "KEY_SETTINGS_USAGE_PROFILE";
		public static final String USAGE_LIMIT = "KEY_SETTINGS_USAGE_LIMIT";
		public static final String USAGE_PROFILE_CHARGER = "KEY_SETTINGS_USAGE_PROFILE_CHARGER";
		public static final String USAGE_DORMANT_MODE = "KEY_SETTINGS_USAGE_DORMANT_MODE";
		public static final String HIDE_RANKING = "KEY_SETTINGS_HIDE_RANKING";
		public static final String HIDE_CALLS = "KEY_SETTINGS_HIDE_RANKING";
		public static final String HIDE_COMPARE = "KEY_SETTINGS_HIDE_COMPARE";
		public static final String HIDE_MAP = "KEY_SETTINGS_HIDE_MAP";
		public static final String USER_COV_ONLY = "KEY_SETTINGS_USER_COV_ONLY";
		public static final String CARRIER_COV_ONLY = "KEY_SETTINGS_CARRIER_COV_ONLY";
		public static final String HIDE_TWEET_SHARE = "KEY_SETTINGS_HIDE_TWEET_SHARE";
		public static final String ALLOW_TRAVEL_FILLINS = "KEY_SETTINGS_ALLOW_FILLINS";
		public static final String ALLOW_BUILDINGS = "KEY_SETTINGS_ALLOW_BUILDINGS";
		public static final String ALLOW_TRANSIT = "KEY_SETTINGS_ALLOW_TRANSIT";
		public static final String AUTO_CONNECTION_TESTS = "KEY_SETTINGS_AUTO_CONN_TESTS";
		public static final String ALLOW_CONFIRMATION = "KEY_SETTINGS_ALLOW_CONFIRMATION";
		public static final String ALLOW_DROP_POPUP = "KEY_SETTINGS_ALLOW_DROP_POPUP";
		public static final String MAX_ON_CHARGE = "KEY_SETTINGS_MAX_ON_CHARGE";
		public static final String DATA_ROAMING  = "KEY_SETTINGS_DATA_ROAMING";
		public static final String THROUGHPUT_STORED  = "KEY_SETTINGS_THROUGHPUT_STORED";
		public static final String WIFI_EVENTS = "KEY_SETTINGS_WIFI_EVENTS";
		public static final String SERVICE_VERSION_INSTALLED = "KEY_SERVICE_VERSION_INSTALLED";
		public static final String SERVICE_VERSION_BUNDLED = "KEY_SERVICE_VERSION_BUNDLED";
		public static final String DONT_ASK_FOR_GPS = "DONT_ASK_FOR_GPS";
		public static final String DONT_ASK_FOR_Q10 = "DONT_ASK_FOR_Q10";
		public static final String DONT_ASK_FOR_BB10 = "DONT_ASK_FOR_BB10";
		public static final String DONT_ASK_FOR_MAPPING_INSTRUCT_POLYGON = "KEY_DONT_ASK_FOR_MAPPING_INSTRUCT_POLYGON";
		public static final String DONT_ASK_FOR_MAPPING_INSTRUCT_SEARCH = "KEY_DONT_ASKMAPPING_INSTRUCT_SEARCH";
		public static final String DONT_ASK_FOR_MAPPING_INSTRUCT_ABOUT = "KEY_DONT_ASKMAPPING_INSTRUCT_ABOUT";
		public static final String DONT_ASK_FOR_MAPPING_INSTRUCT_ANCHOR = "KEY_DONT_ASK_MAPPING_INSTRUCT_ANCHOR";
		public static final String DONT_ASK_FOR_MAPPING_INSTRUCT_SAMPLE = "KEY_DONT_ASK_MAPPING_INSTRUCT_SAMPLE";
		public static final String DONT_ASK_FOR_MAPPING_INSTRUCT_DELETE = "KEY_DONT_ASK_MAPPING_INSTRUCT_DELETE";
		public static final String DONT_ASK_FOR_MAPPING_INSTRUCT_POLYGON_CREATE = "KEY_DONT_ASK_MAPPING_INSTRUCT_POLYGON_CREATE";
		public static final String DONT_ASK_FOR_MAPPING_INSTRUCT_POLYGON_DELETE = "KEY_DONT_ASK_MAPPING_INSTRUCT_POLYGON_DELETE_SELECT";
		public static final String DONT_ASK_FOR_MAPPING_NO_BUILDING = "KEY_DONT_ASK_FOR_MAPPING_NO_BUILDING";
		public static final String TRANSIT_DOWNLOAD_PROCESSED = "KEY_TRANSIT_DOWNLOAD_PROCESSED";
		public static final String WAS_ROAMING = "KEY_SETTINGS_WAS_ROAMING";
		public static final String FIRST_BUCKET = "KEY_SETTINGS_FIRST_BUCKET";
		public static final String DONT_ASK_FOR_ROOT_ACCESS = "DONT_ASK_FOR_ROOT_ACCESS";
		public static final String DONT_ASK_FOR_SYSTEM_ACCESS = "DONT_ASK_FOR_SYSTEM_ACCESS";
		public static final String DONT_ASK_FOR_NON_ROOT = "DONT_ASK_FOR_NON_ROOT";
		public static final String SPEEDTEST_INPROGRESS = "KEY_SPEEDTEST_INPROGRESS";
		public static final String VIDEOTEST_INPROGRESS = "KEY_VIDEOTEST_INPROGRESS";
		public static final String USER_SHUT_DOWN = "KEY_USER_SHUT_DOWN";
		public static final String SHOW_MAP_TIP = "KEY_SHOW_MAP_TIP";
		public static final String SHOW_SPEED_TIP = "KEY_SHOW_SPEED_TIP";
		public static final String SEND_ON_WIFI  = "KEY_SETTINGS_SEND_ON_WIFI";
		public static final String CHANGED_SEND_ON_WIFI  = "KEY_SETTINGS_CHANGED_SEND_ON_WIFI";
		public static final String TRACKING_EXPIRES = "KEY_SETTINGS_TRACKING_EXPIRES";

		//public static final String TRACKING_INTERVAL = "KEY_SETTINGS_TRACKING_INTERVAL";
		//public static final String TRACKING_COVERAGE = "KEY_SETTINGS_TRACKING_COVERAGE";
		//public static final String TRACKING_SPEED = "KEY_SETTINGS_TRACKING_SPEED";
//		public static final String TRACKING_COVERAGE_INTERVAL = "KEY_SETTINGS_TRACKING_COVERAGE_INTERVAL";
//		public static final String TRACKING_SPD_INTERVAL = "KEY_SETTINGS_TRACKING_SPEED_INTERVAL";
//		public static final String TRACKING_VIDEO_INTERVAL = "KEY_SETTINGS_TRACKING_VIDEO_INTERVAL";
//		public static final String TRACKING_SMS_INTERVAL = "KEY_SETTINGS_TRACKING_SMS_INTERVAL";
//		public static final String TRACKING_CONNECT_INTERVAL = "KEY_SETTINGS_TRACKING_CONNECT_INTERVAL";
//		public static final String TRACKING_WEB_INTERVAL = "KEY_SETTINGS_TRACKING_WEB_INTERVAL";
//		public static final String TRACKING_VQ_INTERVAL = "KEY_SETTINGS_TRACKING_VQ_INTERVAL";
//        public static final String TRACKING_AUDIO_INTERVAL = "KEY_SETTINGS_TRACKING_AUDIO_INTERVAL";
		public static final String DRIVE_TEST_CMD = "KEY_DRIVE_TEST_CMD";
		public static final String DRIVETEST_INDEX = "KEY_DRIVETEST_INDEX";
		
		public static final String HANDSET_CAPS = "KEY_SETTINGS_HANDSET_CAPS";
		public static final String LAST_SPEEDTEST = "KEY_SETTINGS_LAST_SPEEDTEST";
		public static final String SPEED_DAY_COUNT = "KEY_SETTINGS_SPEED_DAY_COUNT";
		public static final String SPEED_MONTH_BYTES = "KEY_SETTINGS_SPEED_MONTH_BYTES";
		public static final String SPEED_DAY = "KEY_SETTINGS_SPEED_DAY";
		public static final String SPEED_MONTH = "KEY_SETTINGS_SPEED_MONTH";
		public static final String SPEED_DAY_BYTES = "KEY_SPEED_DAY_BYTES";
		public static final String SPEED_EVENTID = "KEY_SPEED_EVENTID";
		public static final String SPEED_URL = "KEY_SPEED_URL";
		public static final String AUTOSPEED_ENABLE = "KEY_SETTINGS_AUTOSPEED_ENABLE";
        public static final String AUTOSPEED_SVR_ENABLE = "KEY_SETTINGS_AUTOSPEED_SVR_ENABLE";
		public static final String AUTOSPEED_SIZEMB = "KEY_SETTINGS_AUTOSPEED_SIZEMB";
        public static final String AUTOSPEED_SVR_SIZEMB = "KEY_SETTINGS_AUTOSPEED_SVR_SIZEMB";
		public static final String AUTOSPEED_MB_CHANGED = "KEY_SETTINGS_AUTOSPEED_MB_CHANGED";
		public static final String TOPOP_RESPONSE = "KEY_SETTINGS_TOPOP_RESPONSE";
		public static final String TOPOP_LAT = "KEY_SETTINGS_TOPOP_LAT";
		public static final String TOPOP_LNG = "KEY_SETTINGS_TOPOP_LNG";
		public static final String TOPSTATS_RESPONSE = "KEY_SETTINGS_TOPSTATS_RESPONSE";
		public static final String TOPSTATS_LAT = "KEY_SETTINGS_TOPSTATS_LAT";
		public static final String TOPSTATS_LNG = "KEY_SETTINGS_TOPSTATS_LNG";
		public static final String PHONESCREEN_ENABLE = "KEY_SETTINGS_PHONESCREEN_ENABLE";
		public static final String SERVERPHONESCREEN_ENABLE = "KEY_SETTINGS_SERVERPHONESCREEN_ENABLE";
		public static final String READ_LOG_PERMISSION = "KEY_SETTINGS_READ_LOG_PERMISSION";
		public static final String ACCESS_POINT_STATE = "KEY_SETTINGS_ACCESS_POINT_STATE";
		public static final String SCREEN_ON_UPDATE = "KEY_SETTINGS_SCREEN_ON_UPDATE";
		public static final String OPERATOR_RESPONSE = "KEY_SETTINGS_OPERATOR_RESPONSE";
		public static final String OPERATOR_REQUEST = "KEY_SETTINGS_OPERATOR_REQUEST";
		public static final String DROP_PROX = "KEY_SETTINGS_DROP_PROX";
		public static final String EVENTS_QUEUE  = "KEY_SETTINGS_EVENTS_QUEUE";
		public static final String LAST_TIME  = "KEY_SETTINGS_LAST_TIME";
		public static final String ICON_ALWAYS  = "KEY_SETTINGS_ICON_ALWAYS";
		public static final String ENGINEER_MODE_EXPIRES_TIME="ENGINEER_MODE_SETTINGS_TIME";

		public static final String SMS_DELIVERY_TIME="KEY_SETTINGS_SMS_DELIVERY_TIME";
		public static final String SMS_IN_PROGRESS="KEY_SETTINGS_SMS_IN_PROGRESS";

		public static final String VIDEO_URL = "KEY_VIDEO_URL";
        public static final String AUDIO_URL = "KEY_AUDIO_URL";
        public static final String WEB_URL = "KEY_WEB_URL";
        public static final String WEBSOCKET = "KEY_WEBSOCKET";
        public static final String WEBSOCKET_RUNNING = "KEY_WEBSOCKET_RUNNING";
		//public static final String VOICETEST_DIAL = "KEY_VOICETEST_DIAL";
		public static final String VOICETEST_SERVICE = "KEY_VOICETEST_SERVICE";
		public static final String VQ_EVENTID = "KEY_VQ_EVENTID";
		public static final String SPEED_SIZES_JSON = "SPEED_SIZES_JSON";

        public static final String USE_GCM = "KEY_USE_GCM";
		public static final String USE_SVCMODE = "KEY_USE_SVCMODE";
        public static final String KEY_GCM_REG_ID = "KEY_GCM_REG_ID";
        public static final String KEY_GCM_APP_VERSION = "KEY_GCM_APP_VERSION";
        public static final String SIGNED_OUT = "KEY_SIGNED_OUT";
		public static final String YOUTUBE_VIDEOID = "KEY_YOUTUBE_VIDEOID";
		public static final String KEY_SETTINGS_SERVICEMODE = "KEY_SETTINGS_SERVICEMODE";
		public static final String KEY_APN = "KEY_APN";
	}
	
	public final class Map {
		public static final String FILTERS = "KEY_SETTINGS_MAP_FILTERS";
		public static final String FILTERS_DROPPEDCALL = "KEY_SETTINGS_MAP_FILTERS_DROPPEDCALL";
		public static final String FILTERS_LOWSIGNAL = "KEY_SETTINGS_MAP_FILTERS_LOWSIGNAL";
		public static final String FILTERS_LOSTVOICECOVERAGE = "KEY_SETTINGS_MAP_FILTERS_LOSTVOICECOVERAGE";
		public static final String FILTERS_LOST3G = "KEY_SETTINGS_MAP_FILTERS_LOST3G";
		public static final String FILTERS_LOST4G = "KEY_SETTINGS_MAP_FILTERS_LOST4G";
		public static final String FILTERS_COVERAGE = "KEY_SETTINGS_MAP_FILTERS_COVERAGE";
		public static final String FILTERS_SPEED = "KEY_SETTINGS_MAP_FILTERS_SPEED";
		public static final String DATERANGE = "KEY_SETTINGS_MAP_DATERANGE";
	}
}

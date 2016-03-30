package com.cortxt.app.mmcutility.Utils;

//import com.cortxt.app.MMC.Activities.WebsiteLink;
//import com.cortxt.app.MMC.Activities.Login.Login;
//import com.cortxt.app.MMC.Activities.Login.MainScreen;

/**
 * When intents are used to send information, generally bundles are used to insert arbitrary key/value pairs
 * into it. The "key" value of these pairs has to be constant and easily accessible. This is why we store
 * those constant keys into this class.
 * @author abhin
 *
 */
//TODO add comments to the individual key constants later when you figure out the purpose of each key
public final class CommonIntentBundleKeysOld {

	public final class Miscellaneous {
		public static final String WEB_ADDRESS = "com.cortxt.app.MMC.CommonIntentBundleKeys.Miscellaneous.WEB_ADDRESS";
		public static final String AUTO_LOGIN = "com.cortxt.app.MMC.CommonIntentBundleKeys.Miscellaneous.AUTO_LOGIN";
	}

	public static final String GIVEMETIME = "KEY_GIVEMETIME";
	public static final String PHONE_RUNTIME = "KEY_PHONE_RUNTIME";
	public static final String EVENT_ARRAY = "KEY_EVENT_ARRAY";
	public static final String YOUR_EVENT_ARRAY = "KEY_YOUR_EVENT_ARRAY";    //TODO WTF ... COME UP WITH A BETTER NAME
	public static final String SIGNAL_ARRAY = "KEY_SIGNAL_ARRAY";
	public static final String GSM_VALUE = "KEY_GSM_VALUE";
	public static final String CDMA_VALUE = "KEY_CDMA_VALUE";
	public static final String EVDO_VALUE = "KEY_EVDO_VALUE";
	public static final String LAST_EVENT = "KEY_LAST_EVENT";
	public static final String LAST_EVENT_TIMESTAMP = "KEY_LAST_EVENT_TIMESTAMP";
	public static final String TIME_DURATION = "KEY_TIME_DURATION";    //TODO COME UP WITH A BETTER NAME AFTER FIGURING OUT THE PURPOSE OF THIS
	public static final String PROFILE_LEVEL = "KEY_PROFILE_LEVEL";
	public static final String USER_ID = "KEY_USER_ID";
	public static final String SIGNAL_STRENGTH = "KEY_SIGNAL_STRENGTH";
	public static final String SAVE_WIFI = "KEY_SAVE_WIFI";
	public static final String TRACKING = "KEY_TRACKING";    //TODO NOT SURE IF THIS IS THE BEST NAME
	//TODO START USING A TRUE/FALSE VALUE ASSIGNED TO TRACKING. DO NOT USE PRESENCE OF KEY_TRACKING / KEY_TRACKING_STOP AS PRIMARY INFORMATION SOURCE
	public static final String TEST_SPEED = "KEY_TEST_SPEED";
	public static final String GIVE_MAP_INFO = "KEY_GIVE_MAP_INFO";
	public static final String USER_ID_MAP = "KEY_USER_ID_MAP";
	public static final String GIVE_USER_EMAIL = "KEY_GIVE_USER_EMAIL";
	public static final String USER_EMAIL = "KEY_USER_EMAIL";
	public static final String GIVE_USER_LEVEL = "KEY_GIVE_USER_LEVEL";
	public static final String USER_LEVEL = "KEY_USER_LEVEL";    //TODO THIS MIGHT NOT BE NEEDED AS PROFILE_LEVEL COULD BE USED INSTEAD

	//TODO FIGURE OUT WHAT THE FOLLOWING BUNCH IS FOR AND SUBSEQUENTLY COME UP WITH BETTER NAMES FOR THESE
	public static final String QOS_MONITOR = "KEY_QOS_MONITOR";
	public static final String DATASPEED_MONITOR = "KEY_DATASPEED_MONITOR";
	public static final String WIFI_MONITOR = "KEY_WIFI_MONITOR";
	public static final String DIS_DATA_MONITOR = "KEY_DATA_MONITOR";    //TODO FIGURE OUT WHAT "DIS" MEANS
	public static final String FRONT_MONITOR = "KEY_FRONT_MONITOR";    //TODO WTH IS THIS
	public static final String OPTION_QOS = "KEY_OPTION_QOS";
	public static final String OPTION_SPEED = "KEY_OPTION_SPEED";
	public static final String OPTION_WIFI = "KEY_OPTION_WIFI";
	public static final String OPTION_DIS_DATA = "KEY_OPTION_DIS_DATA";    //TODO AGAIN, WTH IS "DIS"?
	public static final String OPTION_FRONT = "KEY_OPTION_FRONT";

	// UPDATE THE UI BUFFERS WITH LIVE DATA
	public static final String KEY_UPDATE_SATELLITE_COUNT = "UPDATE_SATELLITE_COUNT";
	public static final String KEY_UPDATE_SIGNAL_STRENGTH_PERCENTAGE = "SIGNAL_PERCENT";
	public static final String KEY_UPDATE_SIGNAL_STRENGTH_DBM = "SIGNAL_DBM";
	public static final String KEY_UPDATE_EVENT = "UPDATE_EVENT";
	public static final String KEY_UPDATE_BS_LOW = "UPDATE_BS_LOW";
	public static final String KEY_UPDATE_BS_MID = "UPDATE_BS_MID";
	public static final String KEY_UPDATE_BS_HIGH = "UPDATE_BS_HIGH";
	public static final String KEY_UPDATE_NEIGHBORS = "UPDATE_NEIGHBORS";
	public static final String KEY_UPDATE_CONNECTION = "UPDATE_CONNECTION";
	public static final String KEY_UPDATE_SATELLITES_USED_IN_FIX = "SATELLITES_USED_IN_FIX";
	public static final String KEY_LOCATION_UPDATE = "UPDATE_LOCATION";
	public static final String KEY_GPS_STATUS_UPDATE = "UPDATE_GPS_STATUS";
	public static final String KEY_UPDATE_LTEID = "UPDATE_LTEID";

	public static final String KEY_EXTRA_VIDEO_CHART = "KEY_EXTRA_VIDEO_CHART";
	public static final String KEY_EXTRA_VIDEO_TRIGGER = "KEY_EXTRA_VIDEO_TRIGGER";
	public static final String KEY_RX = "RX";
	public static final String KEY_TX = "TX";
	public static final String KEY_WIFI_CONNECTED = "WIFI_CONNECTED";
	public static final String KEY_WIFI_SIGNAL = "WIFI_SIGNAL";
	public static final String KEY_UPDATE_NETTYPE = "UPDATE_NETTYPE";
	public static final String EXTRA_SENDSOCKET = "SEND_SOCKET";

	public static final String ACTION_UPDATE = "com.cortxt.app.MMC.Activities.Livestatus.ACTION_UPDATE";
	public static final String ACTION_LOCATION_UPDATE = "com.cortxt.app.MMC.Activities.Livestatus.UPDATE_LOCATION";
	public static final String ACTION_NEIGHBOR_UPDATE = "com.cortxt.app.MMC.Activities.Livestatus.UPDATE_NEIGHBORS";
	public static final String ACTION_CONNECTION_UPDATE = "com.cortxt.app.MMC.Activities.Livestatus.UPDATE_CONNECTION";
	public static final String ACTION_GPS_STATUS_UPDATE = "com.cortxt.app.MMC.Activities.Livestatus.UPDATE_GPS_STATUS";
	public static final String ACTION_SIGNAL_STRENGTH_UPDATE = "com.cortxt.app.MMC.Activities.Livestatus.SIGNAL_STRENGTH_UPDATE";
	public static final String ACTION_EVENT_UPDATE = "com.cortxt.app.MMC.Activities.Livestatus.EVENT_UPDATE";
	public static final String ACTION_CELL_UPDATE = "com.cortxt.app.MMC.Activities.Livestatus.CELL_UPDATE";
	public static final String ACTION_RX_TX = "com.cortxt.app.MMC.Activities.Livestatus.ACTION_RX_TX";
	public static final String ACTION_NETWORK_UPDATE = "com.cortxt.app.MMC.ACTION_NETWORK_UPDATE";

	public static final String EXTRA_STALLS = "stalls";
	public static final String EXTRA_STALL_TIME = "stallTime";
	public static final String EXTRA_DURATION = "duration";
	public static final String EXTRA_ACCESS_DELAY = "accessDelay";
	public static final String EXTRA_PLAY_DELAY = "playDelay";
	public static final String EXTRA_VIDEO_RES = "res";
	public static final String EXTRA_EVENTTYPE = "eventType";
	public static final String EXTRA_DOWNLOAD_TIME = "downloadTime";

	public static final String EXTRA_UPDATE_UI = "UPDATE_UI";
	public static final String EXTRA_SPEED_TRIGGER = "SPEED_TRIGGER";
	public static final String EXTRA_TEST_TYPE = "TEST_TYPE";

	public static final String EXTRA_LATENCY = "latency";
	public static final String EXTRA_DOWNLOAD_SPEED = "down";
	public static final String EXTRA_UPLOAD_SPEED = "up";
	public static final String EXTRA_EVENT_TYPE = "up";
	public static final String EXTRA_LATENCY_PROGRESS = "latencyProgress";
	public static final String EXTRA_DOWNLOAD_PROGRESS = "downloadProgress";
	public static final String EXTRA_UPLOAD_PROGRESS = "uploadProgress";
	public static final String ACTION_WEBTEST_RESULT = "com.cortxt.app.MMC.intent.ACTION_AUDIOTEST_RESULT";
	public static final String ACTION_WEBTEST_ERROR = "com.cortxt.app.MMC.intent.ACTION_AUDIOTEST_ERROR";
	public static final String ACTION_WEBTEST_COMPLETE = "com.cortxt.app.MMC.intent.ACTION_AUDIOTEST_COMPLETE";
	public static final String ACTION_WEBTEST_CANCELLED = "com.cortxt.app.MMC.intent.ACTION_AUDIOTEST_CANCELLED";
	public static final String EXTRA_BUFFER_PROGRESS = "buffprogress";
	public static final String EXTRA_PLAY_PROGRESS = "playprogress";
	public static final String EXTRA_RXBYTES = "rxbytes";

	public static final String KEY_EXTRA_BLUETOOTH_PACKET = "EXTRA_BLUETOOTH_PACKET";
	public static final String KEY_EXTRA_BLUETOOTH_TOTAL = "EXTRA_BLUETOOTH_TOTAL";
	public static final String KEY_EXTRA_BT_FILENAME = "EXTRA_BT_FILENAME";
	public static final String KEY_EXTRA_BT_DOWNLOADED = "EXTRA_BT_DOWNLOADED";
	public static final String KEY_EXTRA_BT_DOWNLOAD_ERROR = "EXTRA_BT_DOWNLOAD_ERROR";
	public static final String KEY_EXTRA_BT_DOWNLOAD_FILE = "EXTRA_BT_DOWNLOAD_FILE";
	public static final String KEY_EXTRA_BT_STATUS = "EXTRA_BT_STATUS";
	public static final String EXTRA_VQ_UPLOAD_PROGRESS = "EXTRA_VQ_UPLOAD_PROGRESS";
	public static final String EXTRA_VQ_SCORE = "EXTRA_VQ_SCORE";
	public static final String EXTRA_VQ_TRIGGER = "EXTRA_VQ_TRIGGER";
	public static final String KEY_EXTRA_VQ_UPLOAD = "KEY_EXTRA_VQ_UPLOAD";
	public static final String KEY_EXTRA_VQ_UPLOADED = "KEY_EXTRA_VQ_UPLOADED";
	public static final String KEY_EXTRA_VQ_UPLOAD_ERROR = "KEY_EXTRA_VQ_UPLOAD_ERROR";
	public static final String KEY_EXTRA_VQ_SCORE = "KEY_EXTRA_VQ_SCORE";
	public static final String KEY_EXTRA_VQ_LEVEL = "KEY_EXTRA_VQ_LEVEL";
	public static final String KEY_EXTRA_VQ_CONNECT_ERROR = "KEY_EXTRA_VQ_CONNECT_ERROR";
}










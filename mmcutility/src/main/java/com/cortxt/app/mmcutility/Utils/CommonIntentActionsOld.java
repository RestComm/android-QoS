package com.cortxt.app.mmcutility.Utils;

/**
 * There are some intent actions that are used by multiple recipients.
 * These cannot be defined in the recipients themselves because we would
 * have to define them in multiple places and that would lead to a maintenance
 * hassle. We cannot cross-use the action strings because that would be a 
 * very bad practice and would haphazard code. So we declare them here and then
 * reference them in the respective recipients.
 * 
 * <b>
 * Note: As a design principle, all recipients of these actions must reference
 * the corresponding strings as public static final String variables.
 * </b>
 * @author abhin
 *
 */
public class CommonIntentActionsOld {
	/**
	 * This action string is used when letting the recipient know of the latest location
	 */
	public static final String LOCATION_UPDATE_ACTION = "com.cortxt.app.MMC.utils.CommonIntentActions.LOCATION_UPDATE";
	
	/**
	 * This action string is used when letting the recipient know when the gps has been turned ON/OFF
	 */
	public static final String GPS_STATUS_UPDATE_ACTION = "com.cortxt.app.MMC.utils.CommonIntentActions.GPS_STATUS_UPDATE";

	public static final String ACTION_SIGNAL_STRENGTH_UPDATE = "com.cortxt.app.MMC.Activities.Livestatus.SIGNAL_STRENGTH_UPDATE";
	public static final String ACTION_CONNECTION_UPDATE = "com.cortxt.app.MMC.Activities.Livestatus.UPDATE_CONNECTION";

	public static final String ACTION_WEBTEST_RESULT = "com.cortxt.app.MMC.intent.ACTION_AUDIOTEST_RESULT";
	public static final String ACTION_WEBTEST_ERROR = "com.cortxt.app.MMC.intent.ACTION_AUDIOTEST_ERROR";
	public static final String ACTION_WEBTEST_COMPLETE = "com.cortxt.app.MMC.intent.ACTION_AUDIOTEST_COMPLETE";
	public static final String ACTION_WEBTEST_CANCELLED = "com.cortxt.app.MMC.intent.ACTION_AUDIOTEST_CANCELLED";

	public static final String ACTION_SPEEDTEST_RESULT = "com.cortxt.app.MMC.intent.ACTION_SPEEDTEST_RESULT";
	public static final String ACTION_SPEEDTEST_ERROR = "com.cortxt.app.MMC.intent.ACTION_SPEEDTEST_ERROR";
	public static final String ACTION_SPEEDTEST_COMPLETE = "com.cortxt.app.MMC.intent.ACTION_SPEEDTEST_COMPLETE";
	public static final String ACTION_SPEEDTEST_CANCELLED = "com.cortxt.app.MMC.intent.ACTION_SPEEDTEST_CANCELLED";

	public static final String ACTION_ALARM_MINUTE = "com.cortxt.app.MMC.intent.action.ACTION_ALARM_MINUTE";
	public static final String ACTION_TRACKING_5MINUTE = "com.cortxt.app.MMC.intent.action.ACTION_TRACKING_5MINUTE";
	public static final String ACTION_TRACKING_1MINUTE = "com.cortxt.app.MMC.intent.action.ACTION_TRACKING_1MINUTE";
	public static final String ACTION_SMS_DELIVERED = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.SMS_DELIVERED";

	public static final String RESTART_MMC_SERVICE = "com.cortxt.app.MMC.intent.action.RESTART_MMC_SERVICE";
	public static final String EMAIL_CSV = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.EMAIL_CSV";

	public static final String SPEED_TEST = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.SPEED_TEST";
	public static final String SMS_TEST = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.DO_SMSTEST";
	public static final String LATENCY_TEST = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.LATENCY_TEST";
	public static final String ACTION_STOP_SPEEDTEST = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.ACTION_STOP_SPEEDTEST";
	public static final String ACTION_STOP_VIDEOTEST = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.ACTION_STOP_VIDEOTEST";
	public static final String ACTION_DONE_SMSTEST = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.ACTION_SMSTEST_DONE";
	public static final String ACTION_SMSTEST_CANCEL = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.ACTION_SMSTEST_CANCEL";

	public static final String SURVEY = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.SURVEY";
	public static final String SURVEY_EXTRA = "survey_extra";
	public static final String VIEWING_SIGNAL = "com.cortxt.app.MMC.intent.action.VIEWING_SIGNAL";
	public static final String MANUAL_TRANSIT_START = "com.cortxt.app.MMC.intent.action.MANUAL_TRANSIT_START";
	public static final String MANUAL_TRANSIT_END = "com.cortxt.app.MMC.intent.action.MANUAL_TRANSIT_END";
	public static final String MANUAL_TRANSIT_CANCEL = "com.cortxt.app.MMC.intent.action.MANUAL_TRANSIT_CANCEL";
	public static final String MANUAL_PLOTTING_START = "com.cortxt.app.MMC.intent.action.MANUAL_PLOTTING_START";
	public static final String MANUAL_PLOTTING_END = "com.cortxt.app.MMC.intent.action.MANUAL_PLOTTING_END";
	public static final String MANUAL_PLOTTING_CANCEL = "com.cortxt.app.MMC.intent.action.MANUAL_PLOTTING_CANCEL";
	public static final String ACTIVE_TEST = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.ACTIVE_TEST";
	public static final String WEB_TEST = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.WEB_TEST";
	public static final String COMMAND = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.COMMAND";
	public static final String COMMAND_EXTRA = "command_extra";

	public static final String UPDATE_ACTION = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.UPDATE_ACTION";
	/**
	 * This action is meant to give the GPS a cold reset from the menu
	 */
	public static final String COLDRESET_ACTION = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.COLDRESET_ACTION";
	public static final String START_TRACKING_ACTION = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.START_TRACKING_ACTION";
	public static final String STOP_TRACKING_ACTION = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.STOP_TRACKING_ACTION";
	public static final String TRACKING_NUM_5_MIN_PERIODS = "TRACKING_NUM_5_MIN_PERIODS";
	public static final String TRACK_REMOTE = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.TRACK_REMOTE";

	public static final String RUN_WEBSOCKET = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.RUN_WEBSOCKET";
	public static final String EXTRA_START_WEBSOCKET = "START_WEBSOCKET";

	public static final String ACTION_VOICETEST_RESULT = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.VOICETEST_RESULT";
	public static final String ACTION_VOICETEST_ERROR = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.VOICETEST_ERROR";
	public static final String ACTION_VQ_CONNECT_ERROR = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.ACTION_VQ_CONNECT_ERROR";
	public static final String ACTION_VQ_UPLOAD = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.VQ_UPLOAD";
	public static final String ACTION_VQ_ENDUPLOAD = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.VQ_ENDUPLOAD";
	public static final String ACTION_VQ_POLL_SCORE = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.VQ_POLL_SCORE";
	public static final String ACTION_VQ_AUDIO_LEVEL = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.ACTION_VQ_AUDIO_LEVEL";
	public static final String ACTION_BLUETOOTH_DOWNLOAD = "com.cortxt.app.MMC.Activities.Livestatus.BLUETOOTH_DOWNLOAD";
	public static final String ACTION_BLUETOOTH_STARTDOWNLOAD = "com.cortxt.app.MMC.Activities.Livestatus.ACTION_BLUETOOTH_STARTDOWNLOAD";
	public static final String ACTION_BLUETOOTH_ENDDOWNLOAD = "com.cortxt.app.MMC.Activities.Livestatus.ACTION_BLUETOOTH_ENDDOWNLOAD";
	public static final String ACTION_BLUETOOTH_STATUS = "com.cortxt.app.MMC.Activities.Livestatus.ACTION_BLUETOOTH_STATUS";
	public static final String ACTION_START_VOICETEST = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.START_VOICETEST";
	public static final String ACTION_STOP_VOICETEST = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.STOP_VOICETEST";
	public static final String ACTION_TEST_VQ_DEVICE = "com.cortxt.app.MMC.Services.Intents.MMCIntentHandler.TEST_VQ_DEVICE";
	public static final String ACTION_START_RECORDING = "com.cortxt.app.MMC.Activities.Livestatus.ACTION_START_RECORDING";
	public static final String ACTION_TEST_COMPLETE = "com.cortxt.app.MMC.Activities.Livestatus.ACTION_TEST_COMPLETE";

}

package com.cortxt.app.mmcutility.DataObjects;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.cortxt.app.mmcutility.R;
import com.cortxt.app.mmcutility.Utils.Global;
import com.cortxt.app.mmcutility.Utils.PreferenceKeys;

/**
 * This is a list of all the events
 * that can take place in the MMC_Service class.
 * @author abhin
 */
public enum EventType {
	/**
	 * not important. Deprecated.
	 */


	EVT_NONE (0, "NONE", R.string.eventtype_netchange, R.string.eventcustom_netchange, 0, 0, 0, 0, 0, 0, -1, EventTypeGenre.SINGLETON, "", false, false),
	
	/**
	 * When a phone call ends normally.
	 */
	EVT_DISCONNECT (1, "DISCONNECT", R.string.eventtype_disconnectcall, R.string.eventcustom_disconnectcall, R.drawable.flat_pin_call_ended_ver2, R.drawable.flat_pin_call_ended_ver2, R.drawable.mapicon_event_disconnect, R.drawable.ic_stat_disconnect_call_icon_dark, 20000, 20000, 5, EventTypeGenre.END_OF_COUPLE, "", true, false),
	
	/**
	 * When the signal drops below -110dbm for more than 8 seconds and there is 
	 * a phone call in progress.
	 */
	EVT_QOS_WEAK (2, "QOS LO", R.string.eventtype_lowsignal, R.string.eventcustom_lowsignal, 0, 0, 0, 0, 30000, 30000, 3, EventTypeGenre.START_OF_COUPLE, PreferenceKeys.Map.FILTERS_LOWSIGNAL, true, false),
	
	/**
	 * When there is a low signal is progress ( {@link #EVT_QOS_WEAK} ) followed by 
	 * a signal above -90dbm for more than 8 seconds. A phone call does not necessarily 
	 * have to be in progress for this event to be triggered.
	 */
	EVT_QOS_STRONG (3, "QOS HI", R.string.eventtype_regainedsignal, R.string.eventcustom_regainedsignal, 0, 0, 0, 0, 30000, 30000, 2, EventTypeGenre.END_OF_COUPLE, PreferenceKeys.Map.FILTERS_LOWSIGNAL, true, false),
	
	/**
	 * This is a dropped call. This includes calls that have been unsuccessful and the calls
	 * that have been dropped during an active call.
	 */
	EVT_DROP (4, "DROPPED", R.string.eventtype_droppedcall, R.string.eventcustom_droppedcall, R.drawable.flat_pin_dropped_call_ver2, R.drawable.flat_pin_dropped_selected, R.drawable.mapicon_event_droppedcall, R.drawable.ic_stat_dropped_call_icon_dark, 30000, 30000, 5, EventTypeGenre.END_OF_COUPLE, PreferenceKeys.Map.FILTERS_DROPPEDCALL, true, false),
	
	/**
	 * This is just a call being connected. This includes the user dialing a call and connecting as well
	 * as the user receiving a call and successfully connecting.
	 */
	EVT_CONNECT (5, "CONNECT", R.string.eventtype_connectcall, R.string.eventcustom_connectcall, R.drawable.flat_pin_complete_call_ver2, R.drawable.flat_pin_complete_call_ver2, R.drawable.mapicon_event_connect, R.drawable.ic_stat_eventicon_connectcall_dark, 30000, 1800000, 4, EventTypeGenre.START_OF_COUPLE, "", true, false),
	
	/**
	 * When a phone call fails to send.
	 */
	EVT_CALLFAIL (6, "FAILED", R.string.eventtype_failedcall, R.string.eventcustom_failedcall, R.drawable.flat_pin_failed_call_ver2, R.drawable.flat_pin_failed_call_selected, R.drawable.mapicon_event_failedcall, R.drawable.ic_stat_failed_call_icon_dark, 30000, 30000, -1, EventTypeGenre.END_OF_COUPLE, PreferenceKeys.Map.FILTERS_DROPPEDCALL, true, false),
	/**
	 * When a phone call ends normally.
	 */
	EVT_UNANSWERED (7, "UNANSWERED", R.string.eventtype_unansweredcall, R.string.eventcustom_unansweredcall, R.drawable.flat_pin_call_ended_ver2, R.drawable.flat_pin_call_ended_ver2, R.drawable.mapicon_event_disconnect, R.drawable.ic_stat_disconnect_call_icon_dark, 20000, 20000, 5, EventTypeGenre.END_OF_COUPLE, "", true, false),
	
	/**
	 * This event occurs when the phone's 4g availability goes from "no" to "yes".
	 */
	EVT_VQ_CALL (8, "VQ CALL", R.string.eventtype_vqcall, R.string.eventcustom_vqcall, R.drawable.flat_pin_complete_call_ver2, R.drawable.flat_pin_complete_call_ver2, R.drawable.mapicon_event_connect, R.drawable.ic_stat_eventicon_connectcall_dark, 30000, 30000, 4, EventTypeGenre.START_OF_COUPLE, "", true, false),
	
	/**
	 * This event occurs when the phone's 4g availability goes from "yes" to "no".
	 */
	//COV_DATA_DISC (9, "DATA CONN", R.string.eventtype_dataConn, R.drawable.eventicon_regainedservice, R.drawable.mapicon_event_lostlte, 30000, 10000, 10, EventTypeGenreOld.START_OF_COUPLE, PreferenceKeys.Map.FILTERS_LOST4G, true),
	
	/**
	 * This event occurs when the phone's 4g availability goes from "no" to "yes".
	 */
	COV_4G_YES (10, "4G YES", R.string.eventtype_gainLTE, R.string.eventcustom_gainLTE, R.drawable.flat_pin_lte_ver2,R.drawable.flat_pin_lte_ver2, R.drawable.mapicon_event_gainlte, R.drawable.ic_stat_eventicon_gainlte_dark, 30000, 30000, 11, EventTypeGenre.END_OF_COUPLE, PreferenceKeys.Map.FILTERS_LOST4G, true, false),
	
	/**
	 * This event occurs when the phone's 4g availability goes from "yes" to "no".
	 */
	COV_4G_NO (11, "4G NO", R.string.eventtype_lostLTE, R.string.eventcustom_lostLTE, R.drawable.flat_pin_no_lte_ver2, R.drawable.flat_pin_no_lte_selected, R.drawable.mapicon_event_lostlte, R.drawable.ic_stat_no_lte_icon_dark, 30000, 30000, 10, EventTypeGenre.START_OF_COUPLE, PreferenceKeys.Map.FILTERS_LOST4G, true, false),
	
	/**
	 * This event occurs when the phone's 3g availability goes from "no" to "yes".
	 */
	COV_3G_YES (12, "3G YES", R.string.eventtype_gain3g, R.string.eventcustom_gain3g, R.drawable.flat_pin_3g_ver2, R.drawable.flat_pin_3g_ver2, R.drawable.mapicon_event_gain3g, R.drawable.ic_stat_eventicon_gain3g_dark, 30000, 30000, 13, EventTypeGenre.END_OF_COUPLE, PreferenceKeys.Map.FILTERS_LOST3G, true, false),
	
	/**
	 * This event occurs when the phone's 3g availability goes from "yes" to "no".
	 */
	COV_3G_NO (13, "3G NO", R.string.eventtype_lost3g, R.string.eventcustom_lost3g, R.drawable.flat_pin_no_3g_ver2, R.drawable.flat_pin_no_3g_select, R.drawable.mapicon_event_lost3g, R.drawable.ic_stat_no_3g_icon_dark, 30000, 30000, 12, EventTypeGenre.START_OF_COUPLE, PreferenceKeys.Map.FILTERS_LOST3G, true, false),
	
	/**
	 * This event occurs when the phone's 4g availability goes from "no" to "yes".
	 */
	COV_DATA_YES (14, "DATA YES", R.string.eventtype_gain2g, R.string.eventcustom_gain2g,  R.drawable.flat_pin_2g_ver2, R.drawable.flat_pin_2g_ver2, R.drawable.mapicon_event_gain2g, R.drawable.ic_stat_eventicon_gain2g_dark, 30000, 30000, 15, EventTypeGenre.END_OF_COUPLE, "", true, false),//TODO : find out which preferenceKey to use
	
	/**
	 * This event occurs when the phone's 3g availability goes from "yes" to "no".
	 */
	COV_DATA_NO (15, "DATA NO", R.string.eventtype_lost2g, R.string.eventcustom_lost2g, R.drawable.flat_pin_no_2g_ver2, R.drawable.flat_pin_no_2g_selected, R.drawable.mapicon_event_lost2g, R.drawable.ic_stat_no_2g_icon_dark, 30000, 30000, 14, EventTypeGenre.START_OF_COUPLE, "", true, false),//TODO : find out which preferenceKey to use
	
	/**
	 * This event occurs when the phone's voice availability goes from "no" to "yes".
	 */
	COV_VOD_YES (16, "VOD YES", R.string.eventtype_regainedservice, R.string.eventcustom_regainedservice, R.drawable.flat_pin_service_ver2, R.drawable.flat_pin_service_ver2, R.drawable.mapicon_event_gainservice,  R.drawable.ic_stat_service_dark, 30000, 30000, 17, EventTypeGenre.END_OF_COUPLE, PreferenceKeys.Map.FILTERS_LOSTVOICECOVERAGE, true, false),
	
	/**
	 * This event occurs when the phone's voice availability goes from "yes" to "no".
	 */
	COV_VOD_NO (17, "VOD NO", R.string.eventtype_lostservice, R.string.eventcustom_lostservice, R.drawable.flat_pin_no_service_ver2, R.drawable.flat_pin_no_service_selected, R.drawable.mapicon_event_lostservice, R.drawable.ic_stat_no_service_dark, 30000, 30000, 18, EventTypeGenre.START_OF_COUPLE, PreferenceKeys.Map.FILTERS_LOSTVOICECOVERAGE, true, false),
	
	/**
	 * This is an event that occurs every 3 hours regardless of the state of the phone.
	 * This event can also be triggered on demand by the push of the button.
	 * 
	 * Moreover, this event can be triggered every 20 minutes when the device is in motion.
	 */
	COV_UPDATE (18, "UPDATE", R.string.eventtype_update, R.string.eventcustom_update, R.drawable.flat_pin_update_ver2, R.drawable.old_img_tracking_32, R.drawable.mapicon_event_update, R.drawable.ic_stat_update_dark, 0, 30000, -1, EventTypeGenre.SINGLETON, "", true, false),
	
	/**
	 * This event is triggered when the application is started.
	 * It is not sent to the server but it is stored in the internal db as a couple
	 */
	EVT_STARTUP (19, "STARTUP", R.string.eventtype_startup, R.string.eventcustom_startup, 0, 0, 0, 0, 0, 0, -1, EventTypeGenre.SINGLETON, "", false, false),
	
	/**
	 * This event is triggered when the application is ended. 
	 * It is not sent to the server but it is stored in the internal db as a couple
	 */
	EVT_SHUTDOWN (20, "SHUTDOWN", R.string.eventtype_shutdown, R.string.eventcustom_shutdown, 0, 0, 0, 0, 0, 0, -1, EventTypeGenre.SINGLETON, "", false, false),
	
	/**
	 * This event is triggered when the application shuts down unexpectedly.
	 * It is not sent to the server but it is stored in the internal db as a couple
	 */
	EVT_USHUTDOWN (21, "USHUTDOWN", R.string.eventtype_ushutdown, R.string.eventcustom_ushutdown, 0, 0, 0, 0, 0, 0, -1, EventTypeGenre.SINGLETON, "", false, false),
	
	/**
	 * 
	 */
	@Deprecated
	EVT_WIFI (22, "WIFI", R.string.eventtype_openwifi, R.string.eventcustom_openwifi, 0, 0, 0, 0, 0, 0, -1, EventTypeGenre.SINGLETON, "", false, false),
	
	/**
	 * 
	 */
	@Deprecated
	EVT_WIFI_PRIV (23, "WIFISEC", R.string.eventtype_securewifi, R.string.eventcustom_securewifi, 0, 0, 0, 0, 0, 0, -1, EventTypeGenre.SINGLETON, "", false, false),
	
	/**
	 * This is a request for a manual speedtest. 
	 */
	MAN_SPEEDTEST (24, "SPEED", R.string.eventtype_speedtest, R.string.eventcustom_speedtest, R.drawable.flat_pin_speed_test_ver2, R.drawable.flat_pin_speed_test_ver2, R.drawable.mapicon_event_speedtest, R.drawable.ic_stat_speed_test_dark, 0, 120000, -1, EventTypeGenre.SINGLETON, "", true, true),
	
	/**
	 * The user can switch on tracking for 5 minutes to 2 hours. During the tracking
	 * period, this event is triggered every 5 minutes during which it sends the trend string
	 * for the past 5 minutes.
	 */
	MAN_TRACKING (25, "TRACK", R.string.eventtype_track, R.string.eventcustom_track, R.drawable.flat_pin_update_ver2, R.drawable.old_img_tracking_32, R.drawable.mapicon_event_update, R.drawable.ic_stat_update_dark, 0, 300000, -1, EventTypeGenre.SINGLETON, "", true, false),
	
	/**
	 * This event is triggered when the user is in an un-chartered area.
	 * 
	 * When a "normal" event is triggered in an unchartered area, the phone realises that
	 * it is currently in an unchartered area. Because of this (assuming the phone is still
	 * moving), the phone starts sending these events to the server with 3 minute trend strings.
	 */
	EVT_FILLIN (26, "FILL-IN", R.string.eventtype_fillin, R.string.eventcustom_fillin, R.drawable.flat_pin_update_ver2, R.drawable.old_img_tracking_32, R.drawable.mapicon_event_update, R.drawable.ic_stat_update_dark, 0, 150000, -1, EventTypeGenre.SINGLETON, "", true, false),
	
	/**
	 * When the charger is connected.
	 * 
	 * This event is so far away from the {@link #EVT_CHARGER_OFF} event that if this was treated
	 * as a couple with it, then it would be stored in the phone for too long without being uploaded.
	 */
	EVT_CHARGER_ON (27, "CHARGE ON", R.string.eventtype_chargeron, R.string.eventcustom_chargeron, 0, 0, 0, 0, 0, 0, -1, EventTypeGenre.SINGLETON, "", false, false),
	
	/**
	 * When the charger is disconnected.
	 * 
	 * This event is so far away from the {@link #EVT_CHARGER_ON} event that if this was treated
	 * as a couple with it, then it would be stored in the phone for too long without being uploaded.
	 */
	EVT_CHARGER_OFF (28, "CHARGE OFF", R.string.eventtype_chargeroff, R.string.eventcustom_chargeroff, 0, 0, 0, 0, 0, 0, -1, EventTypeGenre.SINGLETON, "", false, false),
	
	/**
	 * This event is triggered every 20 minutes while in travelling mode.
	 * 
	 * It operates like a basic update event, except it doesnt get sent to the server
	 * It still gets a GPS fix and ends with a fill-in check
	 */
	TRAVEL_CHECK (29, "TRAVEL-CHECK", R.string.eventtype_periscope, R.string.eventcustom_periscope, R.drawable.flat_pin_update_ver2, R.drawable.flat_pin_update_ver2, R.drawable.mapicon_event_update, R.drawable.ic_stat_update_dark, 0, 10000, -1, EventTypeGenre.SINGLETON, "", true, false),
	WIFI_CONNECT (40, "WIFI_CONNECT", R.string.eventtype_wificonnect, R.string.eventcustom_wificonnect, R.drawable.flat_pin_wifi_connected, R.drawable.flat_pin_wifi_connected, R.drawable.icon_map_wifi_connected, R.drawable.ic_stat_wifi_dark, 0, 30000, 41, EventTypeGenre.SINGLETON, "", true, false),
	WIFI_DISCONNECT (41, "WIFI_DISCONNECT", R.string.eventtype_wifidisconnect, R.string.eventcustom_wifidisconnect, R.drawable.flat_pin_wifi_disconnected, R.drawable.flat_pin_wifi_disconnected, R.drawable.icon_map_wifi_disconnected, R.drawable.ic_stat_no_wifi_dark, 0, 30000, 40, EventTypeGenre.SINGLETON, "", true, false),
	

	/**
	 * Dropped call reported with TroubleTweet
	 */
	TT_DROP (30, "TT_DROPPED", R.string.eventtype_ttdropped, R.string.eventcustom_ttdropped, R.drawable.flat_pin_dropped_selected, R.drawable.flat_pin_dropped_selected, R.drawable.mapicon_event_droppedcall, R.drawable.ic_stat_dropped_call_icon_dark, 60000, 0, -1, EventTypeGenre.SINGLETON, PreferenceKeys.Map.FILTERS_DROPPEDCALL, true, false),
	TT_FAIL (31, "TT_FAILED", R.string.eventtype_ttfailed, R.string.eventcustom_ttfailed, R.drawable.flat_pin_failed_call_selected, R.drawable.flat_pin_failed_call_selected,  R.drawable.mapicon_event_failedcall, R.drawable.ic_stat_failed_call_icon_dark, 60000, 0, -1, EventTypeGenre.SINGLETON, PreferenceKeys.Map.FILTERS_DROPPEDCALL, true, false),
	TT_DATA (32, "TT_DATA", R.string.eventtype_ttdata, R.string.eventcustom_ttdata, R.drawable.flat_pin_no_3g_select, R.drawable.flat_pin_no_3g_select, R.drawable.mapicon_event_lost3g, R.drawable.ic_stat_no_3g_icon_dark, 60000, 0, -1, EventTypeGenre.SINGLETON, PreferenceKeys.Map.FILTERS_LOST3G, true, false),
	TT_NO_SVC (33, "TT_SERVICE", R.string.eventtype_ttservice, R.string.eventcustom_ttservice, R.drawable.flat_pin_no_service_selected, R.drawable.flat_pin_no_service_selected, R.drawable.mapicon_event_lostservice, R.drawable.ic_stat_no_service_dark, 60000, 0, -1, EventTypeGenre.SINGLETON, PreferenceKeys.Map.FILTERS_LOSTVOICECOVERAGE, true, false),

	APP_MONITORING (51, "MONITOR-APPS", R.string.eventtype_monitorAps, R.string.eventtype_monitorAps, R.drawable.flat_pin_passive_speed_test, R.drawable.flat_pin_passive_speed_test, R.drawable.icon_map_passive_speed_test, R.drawable.ic_stat_speed_test_dark, 0, 120000, -1, EventTypeGenre.SINGLETON, "", false, true),
	WEBPAGE_TEST (52, "WEBPAGE_TEST", R.string.eventtype_webpageTest, R.string.eventtype_webpageTest, R.drawable.flat_pin_speed_test_ver2, R.drawable.flat_pin_speed_test_ver2, R.drawable.mapicon_event_speedtest, R.drawable.ic_stat_speed_test_dark, 0, 120000, -1, EventTypeGenre.SINGLETON, "", false, true),
	SMS_TEST (53, "SMS_TEST", R.string.eventtype_smsTest, R.string.eventtype_smsTest, R.drawable.flat_pin_speed_test_ver2, R.drawable.flat_pin_speed_test_ver2, R.drawable.icon_map_connectivity, R.drawable.ic_stat_connectivity_dark, 0, 30000, -1, EventTypeGenre.SINGLETON, "", false, true),
	LATENCY_TEST (54, "CONNECTION_TEST", R.string.eventtype_latencyTest, R.string.eventtype_latencyTest, R.drawable.flat_pin_connectivity, R.drawable.flat_pin_connectivity, R.drawable.icon_map_connectivity, R.drawable.ic_stat_connectivity_dark, 0, 30000, -1, EventTypeGenre.SINGLETON, "", true, true),
	CONNECTION_FAILED (55, "CONNECTION_FAILED", R.string.eventtype_latencyTest, R.string.eventtype_latencyTest, R.drawable.flat_pin_connectivity, R.drawable.flat_pin_connectivity, R.drawable.icon_map_connectivity, R.drawable.ic_stat_connectivity_dark, 0, 30000, -1, EventTypeGenre.SINGLETON, "", true, true),
	MAN_TRANSIT (61, "MAN_TRANSIT", R.string.eventtype_manTransit, R.string.eventtype_manTransit, R.drawable.train_station_icon_ts, R.drawable.train_station_icon_ts, R.drawable.icon_map_train_station, R.drawable.ic_train_station, 0, 120000, -1, EventTypeGenre.SINGLETON, "", false, false),
	MAN_PLOTTING (60, "MAN_PLOTTING", R.string.eventtype_manPlottig, R.string.eventtype_manPlottig, R.drawable.flat_pin_sampling, R.drawable.flat_pin_sampling, R.drawable.icon_map_sampling, R.drawable.ic_stat_samples_dark, 0, 120000, -1, EventTypeGenre.SINGLETON, "", false, false),

	VIDEO_TEST (56, "VIDEO_TEST", R.string.eventtype_videoTest, R.string.eventtype_videoTest, R.drawable.flat_pin_speed_test_ver2, R.drawable.flat_pin_speed_test_ver2,  R.drawable.mapicon_event_speedtest, R.drawable.ic_stat_speed_test_dark, 0, 120000, -1, EventTypeGenre.SINGLETON, "", false, true),
    AUDIO_TEST (57, "AUDIO_TEST", R.string.eventtype_audioTest, R.string.eventtype_audioTest, R.drawable.flat_pin_speed_test_ver2, R.drawable.flat_pin_speed_test_ver2,  R.drawable.mapicon_event_speedtest, R.drawable.ic_stat_speed_test_dark, 0, 120000, -1, EventTypeGenre.SINGLETON, "", false, true),
	YOUTUBE_TEST (58, "YOUTUBE_TEST", R.string.eventtype_youtubeTest, R.string.eventtype_youtubeTest, R.drawable.flat_pin_speed_test_ver2, R.drawable.flat_pin_speed_test_ver2,  R.drawable.mapicon_event_speedtest, R.drawable.ic_stat_speed_test_dark, 0, 120000, -1, EventTypeGenre.SINGLETON, "", false, true),

	/**
	 * When a phone call ends normally.
	 */
	SIP_DISCONNECT (63, "SIP DISCONNECT", R.string.eventtype_disconnectcall, R.string.eventcustom_disconnectcall, R.drawable.flat_pin_call_ended_ver2, R.drawable.flat_pin_call_ended_ver2, R.drawable.mapicon_event_disconnect, R.drawable.ic_stat_disconnect_call_icon_dark, 20000, 20000, 5, EventTypeGenre.END_OF_COUPLE, "", true, false),

	/**
	 * This is a dropped call. This includes calls that have been unsuccessful and the calls
	 * that have been dropped during an active call.
	 */
	SIP_DROP (64, "SIP DROPPED", R.string.eventtype_droppedcall, R.string.eventcustom_droppedcall, R.drawable.flat_pin_dropped_call_ver2, R.drawable.flat_pin_dropped_selected, R.drawable.mapicon_event_droppedcall, R.drawable.ic_stat_dropped_call_icon_dark, 30000, 30000, 5, EventTypeGenre.END_OF_COUPLE, PreferenceKeys.Map.FILTERS_DROPPEDCALL, true, false),

	/**
	 * This is just a call being connected. This includes the user dialing a call and connecting as well
	 * as the user receiving a call and successfully connecting.
	 */
	SIP_CONNECT (65, "SIP CALL", R.string.eventtype_connectcall, R.string.eventcustom_connectcall, R.drawable.flat_pin_complete_call_ver2, R.drawable.flat_pin_complete_call_ver2, R.drawable.mapicon_event_connect, R.drawable.ic_stat_eventicon_connectcall_dark, 30000, 1800000, 4, EventTypeGenre.START_OF_COUPLE, "", true, false),

	/**
	 * When a phone call fails to send.
	 */
	SIP_CALLFAIL (66, "SIP FAILED", R.string.eventtype_failedcall, R.string.eventcustom_failedcall, R.drawable.flat_pin_failed_call_ver2, R.drawable.flat_pin_failed_call_selected, R.drawable.mapicon_event_failedcall, R.drawable.ic_stat_failed_call_icon_dark, 30000, 30000, -1, EventTypeGenre.END_OF_COUPLE, PreferenceKeys.Map.FILTERS_DROPPEDCALL, true, false),
	/**
	 * When a phone call ends normally.
	 */
	SIP_UNANSWERED (67, "SIP UNANSWERED", R.string.eventtype_unansweredcall, R.string.eventcustom_unansweredcall, R.drawable.flat_pin_call_ended_ver2, R.drawable.flat_pin_call_ended_ver2, R.drawable.mapicon_event_disconnect, R.drawable.ic_stat_disconnect_call_icon_dark, 20000, 20000, 5, EventTypeGenre.END_OF_COUPLE, "", true, false),

	/**
	 * This event occurs when the phone's 4g availability goes from "no" to "yes".
	 */
	SIP_VQ_CALL (68, "SIP VQ CALL", R.string.eventtype_vqcall, R.string.eventcustom_vqcall, R.drawable.flat_pin_complete_call_ver2, R.drawable.flat_pin_complete_call_ver2, R.drawable.mapicon_event_connect, R.drawable.ic_stat_eventicon_connectcall_dark, 30000, 30000, 4, EventTypeGenre.START_OF_COUPLE, "", true, false);


	// nerd, trouble, map, normal
	// for dealing with the server side code, each of these eventType values have to have 
	// an integer value. We cannot use the keyword 'value' because that has already been
	// used by the EnumType class internal to java. We use intValue instead.
	private final int intValue;
	// these enums also have eventName and eventString properties
	private final String eventName;
	private final int eventString;
	private final int eventCustomString;
	private final int normalImageResource;	//this is the resource integer for the icon related to this event
	private final int mapimageResource;	//this is the resource integer for the icon related to this event
	private final int troubleimageResource; // icon used for Trouble Tweet screen map
	private final int nerdImageResource; // icon used for Chart on Nerd Screen
	private final int preEventStageTime;
	private int postEventStageTime;
	private final int complimentaryEventType;
	private final EventTypeGenre genre;
	private final String preferenceKey;
	private  boolean waitsForSpeed = false;
	//private final boolean isGpsRequired;
	
	/**
	 * This map is for a reverse lookup. This means that when the user wants to get the 
	 * EventType object from the integer value of the EventType, then this map can be 
	 * used to do that lookup relatively quickly.
	 */
	private static final Map<Integer, EventType> lookup = new HashMap<Integer, EventType>();
	
	/**
	 * This variable caches the pre-event stage time so that it doesn't have to be calculated every time.
	 * After the post event staging time has ended, the upload is held off for a while so that any possible 
	 * overlaps can be taken care of.
	 */
	private static int largestPreEventStageTime = 0;
	private static int customEventNames = -1;
	
	
	public int getIntValue(){
		return this.intValue;
	}
	public String getEventName(){
		return this.eventName;
	}
	public String getEventString(Context context){
		int customTitles = Global.getInteger(context, "CUSTOM_EVENTNAMES");
		if (customTitles == 1 && this.eventCustomString != 0)
			return context.getString(this.eventCustomString);
		if (this.eventString != 0)
			return context.getString(this.eventString);
		else
			return "";
	}
	public int getEventString(){
		return this.eventString;
	}
	public int getNerdImageResource(){
		return this.nerdImageResource;
	}
	public int getTroubleImageResource(){
		return this.troubleimageResource;
	}
	public int getMapImageResource(){
		return this.mapimageResource;
	}
	public int getImageResource(){
		return this.normalImageResource;
	}
	public int getPreEventStageTime(){
		return this.preEventStageTime;
	}
	public int getPostEventStageTime(){
		return this.postEventStageTime;
	}
	public boolean waitsForSpeed(){
		return this.waitsForSpeed;
	}
	public void setPostEventStageTime(int newPostEventStageTime){
		this.postEventStageTime = newPostEventStageTime;
	}
	public int getComplimentaryEventType() {
		return complimentaryEventType;
	}
	public EventTypeGenre getGenre() {
		return genre;
	}
	public String getPreferenceKey() {
		return this.preferenceKey;
	}
	public static int getLargestPreEventStageTime(){
		return largestPreEventStageTime;
	}

	
	/*********************
	 * Constructors
	 *********************/
	/**
	 * Constructor
	 * @param intValue The integer value of the eventType that the server understands
	 * @param eventName The name of this event
	 * @param eventString The human readable string for this event
	 * @param imageResource The image resource that is used for this event
	 * @param preEventStageTime The number of milliseconds before the event takes place that the event can 
	 * collect data for.
	 * @param postEventStageTime The number of milliseconds after the event takes place that the event can
	 * collect data for.
	 * @param complimentaryEventType Event types can have complimentary event types that are used to form an event couple. The type
	 * of this parameter is an int instead of {@link EventType} because an enum cannot be used before it is defined and thus calling
	 * the constructor with another instance of {@link EventType} would throw an exception. It is not too big a penalty because the 
	 * reverse lookup from it to an instance of {@link EventType} is fairly quick.
	 * @param genre All event types can belong to one of the various genres in the {@link EventTypeGenre} enum.
	 * @param preferenceKey
	 * @param isGpsRequired if this event requires the gps to be turned on.
	 */
	EventType(
			int intValue, 
			String eventName, 
			int eventString, 
			int eventCustomString, 
			int nerdImageResource,
			int troubleImageResource,
			int mapimageResource, 
			int imageResource,
			int preEventStageTime, 
			int postEventStageTime, 
			int complimentaryEventType,
			EventTypeGenre genre,
			String preferenceKey,
			boolean isGpsRequired,
			boolean waitsForSpeed
		){
		this.intValue = intValue;
		this.eventName = eventName;
		
		this.eventString = eventString;
		this.eventCustomString = eventCustomString;
		this.nerdImageResource = nerdImageResource;
		this.troubleimageResource = troubleImageResource;
		this.mapimageResource = mapimageResource;
		this.normalImageResource = imageResource;
		this.preEventStageTime = preEventStageTime;
		this.postEventStageTime = postEventStageTime;
		this.complimentaryEventType = complimentaryEventType;
		this.genre = genre;
		this.preferenceKey = preferenceKey;
		this.waitsForSpeed = waitsForSpeed;
			
		//this.isGpsRequired = isGpsRequired;
	}
	
	/**
	 * The following code populates the reverse lookup map and other cache variables with the relevant data.
	 */
	static {
		for (EventType eventType : EventType.values()){
			lookup.put(eventType.getIntValue(), eventType);
			if (largestPreEventStageTime < eventType.preEventStageTime)
				largestPreEventStageTime = eventType.preEventStageTime;
		}
		
	}

	/**
	 * This method uses the lookup map to lookup the EventType corresponding to the 
	 * given int code.
	 * @param intCode The int code to do a reverse lookup on
	 * @return
	 */
	public static EventType get(int intCode){
		return lookup.get(intCode);
	}
}

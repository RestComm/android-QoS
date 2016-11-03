package com.cortxt.app.corelib.Services.Events;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;

import com.cortxt.app.corelib.MainService;
import com.cortxt.app.corelib.R;
import com.cortxt.app.utillib.ContentProvider.Tables;
import com.cortxt.app.utillib.ContentProvider.UriMatch;
import com.cortxt.app.utillib.CoverageSamples.CoverageSamplesSend;
import com.cortxt.app.utillib.DataObjects.PhoneState;
import com.cortxt.app.utillib.Reporters.ReportManager;
import com.cortxt.app.utillib.Reporters.ReportManager.EventKeys;
import com.cortxt.app.utillib.Reporters.LocalStorageReporter.LocalStorageReporter.Events;
import com.cortxt.app.utillib.DataObjects.DeviceInfo;
import com.cortxt.app.utillib.Utils.DeviceInfoOld;
import com.cortxt.app.utillib.DataObjects.EventType;
import com.cortxt.app.utillib.Utils.LoggerUtil;
import com.cortxt.app.utillib.Utils.PreferenceKeys;
import com.cortxt.app.corelib.UtilsOld.TrendStringGenerator;
import com.cortxt.app.utillib.DataObjects.EventData;
import com.cortxt.app.utillib.DataObjects.EventDataEnvelope;
import com.cortxt.app.utillib.DataObjects.EventCouple;
import com.cortxt.app.utillib.DataObjects.EventObj;
import com.cortxt.app.utillib.DataObjects.EventTypeGenre;

import com.google.gson.Gson;

/**
 * This class can be used to upload an event to the server. Everything in this
 * class happens in a separate thread.
 * @author Brad
 *
 */
public class EventUploader implements Runnable{
	/**
	 * This is the maximum time (in milliseconds) after an event occurs that the location
	 * received would be considered valid for the event.
	 * This means than for an event that occurs at timestamp T (in milliseconds) would use as
	 * its location the first location received in the interval (T, T+{@value #EVENT_LOCATION_MAXIMUM_TIME_OFFSET}]
	 * (in milliseconds).
	 */
	public static final int EVENT_LOCATION_MAXIMUM_TIME_OFFSET = 180000;	//in milliseconds
	
	/**
	 * This is the maximum time (in milliseconds) before or after an event occurs that the base station 
	 * received would be considered valid for the event.
	 * This means than for an event that occurs at timestamp T (in milliseconds) would use as
	 * its base station the closest location received (temporally) in the interval 
	 * (T-{@value #EVENT_LOCATION_MAXIMUM_TIME_OFFSET}, T+{@value #EVENT_LOCATION_MAXIMUM_TIME_OFFSET}] (in milliseconds).
	 */
	public static final int EVENT_BASE_STATION_MAXIMUM_TIME_OFFSET = 180000;	//in milliseconds
	
	private static final String TAG = EventUploader.class.getSimpleName();
	
	public EventObj event = null; // so that scheduler can check if event = null
	private EventObj complimentaryEvent = null;
	private List<EventCouple> ongoingEventCouples = null;
	public MainService owner = null;
	private boolean bReportLinux = false;
	
	private TrendStringGenerator trendStringGenerator = new TrendStringGenerator ();

	/**
	 * Constructor
	 */
	public EventUploader(EventObj _event, EventObj _complimentaryEvent, MainService context, boolean bLocal){
		this.ongoingEventCouples = context.getEventManager().getOngoingEventCouples();
		this.event = _event;
		this.complimentaryEvent = _complimentaryEvent; // hold reference to this event until event is sent
		this.owner = context;
		
		// decide whether to report event yet, set event = null to postpone
		
		if (bLocal)  // not being sent to server yet, don't postpone
			return;
		// IF TRYING TO SEND FIRST PART OF COUPLE, POSTPONE UNTIL 2ND PART OF COUPLE IS READY
		/*
		 * If the complimentary event is null or its uri is null, then this event is either a 
		 * singleton or it is the starting end of an event couple. In the later case,
		 * the event should not be uploaded to the server just yet.
		 */
		//iterate over the ongoing event couples if the complimentaryEvent is null or its uri is null
		if (_complimentaryEvent == null)
		{
			if (ongoingEventCouples != null && _event != null)
			{
				for (EventCouple eventCouple : ongoingEventCouples){
					//check if this event is the "starting end" of an event couple
					//if so, then this is not the right time to be uploading this event
					if (eventCouple.getStartEventType() == _event.getEventType()){
						//this is not the right time to be uploading this event
						event = null;
						return;
					}
				}	
			}
		}
		// NO start-of-couple is going to get uploaded by itself
		// (should have been caught above, except can happen if end-of-couple is 'on stage' and was removed from ongoing
		if (_event != null && _event.getEventType().getGenre() == EventTypeGenre.START_OF_COUPLE)
		{
			event = null;
			return;
		}
	}
	
	@Override
	public void run () {	
		report (false, null); // report not local (to internet)
	}
	
	public void report (boolean local, Location location) {
		try {
			//now create a list of eventData variables
			List<EventData> eventDataList = new ArrayList<EventData>();
			String strPhone = "";
			long duration = 0;


			EventDataEnvelope eventDataEnvelope = null;
			if (event != null) {
				// Dont send an unconfirmed Travel event
				if (event.getEventType() == EventType.TRAVEL_CHECK && owner.getTravelDetector().isConfirmed() == false) {
					LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "report", "skip unconfirmed travel event");
					return;
				}

				EventData eventData = generateEventDataFromEvent(event, local);
				if (eventData == null)
					return;
				//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "run", "reporting event type=" + event.getEventType() + ",lat:" + eventData.getFltEventLat() + ",local=" + local);
				if (eventData.getFltEventLat() == 0 && location != null) {
					eventData.setFltEventLat((float) location.getLatitude());
					eventData.setFltEventLng((float) location.getLongitude());
					eventData.setiUncertainty((int) location.getAccuracy());
				}
				eventData.setCallID(event.getLocalID());
				EventData eventData2 = null;
				eventDataList.add(eventData);

				if (event.getEventType() == EventType.APP_MONITORING && event.getDownloadSpeed() > 0 && event.getUploadSpeed() > 0) {
					//App throughput was getting stored twice
					boolean stored = PreferenceManager.getDefaultSharedPreferences(owner).getBoolean(PreferenceKeys.Miscellaneous.THROUGHPUT_STORED, false);
					if (!stored) {
						owner.getReportManager().storeEvent(eventData);
						PreferenceManager.getDefaultSharedPreferences(owner).edit().putBoolean(PreferenceKeys.Miscellaneous.THROUGHPUT_STORED, true).commit();
					} else
						PreferenceManager.getDefaultSharedPreferences(owner).edit().putBoolean(PreferenceKeys.Miscellaneous.THROUGHPUT_STORED, false).commit();
					;
				}
				if (event.getEventType() == EventType.MAN_PLOTTING) {
					eventData.setLookupid1(event.getBuildingID());
					eventData.setRunningApps(event.getAppData());  // contains user's polyline
				}

				// Event is 'reported' locally before GPS sampling is complete
				// to make it show up on the map as soon as it gets a first fix
				//if (local == true && ((event.getEventType() != EventType.MAN_SPEEDTEST && event.getEventType() != EventType.LATENCY_TEST && event.getEventType() != EventType.APP_MONITORING) || event.latency != 0))
				if (local == true && (event.getEventType().waitsForSpeed() == false || event.getLatency() != 0))
				//	(local == false && event.getEventType() == EventType.MAN_SPEEDTEST))  // but for speedtest, wait until complete
				{
					if (event.getLocalID() > 0 && eventData.getFltEventLng() != 0) {
						owner.getReportManager().updateEventField(event.getLocalID(), Events.KEY_LATITUDE, String.valueOf(eventData.getFltEventLat()));
						owner.getReportManager().updateEventField(event.getLocalID(), Events.KEY_LONGITUDE, String.valueOf(eventData.getFltEventLng()));
					} else if (event.getLocalID() == 0) {
						int evtID = owner.getReportManager().storeEvent(eventData); // .reportEvent (eventData, event, local, location);
						event.setLocalID(evtID);
						eventData.setCallID(evtID);
					}
					if (complimentaryEvent != null) {
						if (complimentaryEvent.getLocalID() > 0 && location != null) {
							owner.getReportManager().updateEventField(complimentaryEvent.getLocalID(), Events.KEY_LATITUDE, String.valueOf(location.getLatitude()));
							owner.getReportManager().updateEventField(complimentaryEvent.getLocalID(), Events.KEY_LONGITUDE, String.valueOf(location.getLongitude()));
						} else if (complimentaryEvent.getLocalID() == 0) {
							int evtID = owner.getReportManager().storeEvent(eventData); //(eventData2, complimentaryEvent, local, location);
							complimentaryEvent.setLocalID(evtID);
						}
					}
				}
				if (local)
					return;

				//if the complimentary event is not null, then this event must be
				//the "starting end" of an event couple. If so, then this event should
				//be uploaded alongside the main event
				if (complimentaryEvent != null && local == false) {
					eventData2 = generateEventDataFromEvent(complimentaryEvent, local);
					if (eventData2 != null) {
						//complimentaryEvent.setFlag (EventObj.SERVER_SENDING, true);
						eventData2.setCallID(complimentaryEvent.getLocalID());
						eventDataList.add(eventData2);
					}
				}
				//now create the eventDataEnvelope to wrap the list of eventData variables
				//along with other required variables
				String phoneNumFirst4 = "";
				if (strPhone != null && strPhone.length() > 4)
					phoneNumFirst4 = strPhone.substring(0, 4);


				eventDataEnvelope = generateEventDataEnvFromEventList(eventDataList, phoneNumFirst4);
				// when event is filled in, travel and fillin would like to see it before sending
				if (owner.isServiceRunning() && owner.getTravelDetector() != null)
					owner.getTravelDetector().eventCompleted(event);
			} else  // null event create dummy event envelope to trigger sending the event queue (without adding a new event)
				eventDataEnvelope = new EventDataEnvelope();

			boolean bSent = false, bFromQueue = false, bAddedQueue = false;
			loadEventsQueue();  // only loads if queue hasn't loaded yet (ensure loaded)
			ConcurrentLinkedQueue<EventDataEnvelope> eventQueue = owner.getEventManager().getEventQueue();

			// Send this event and any other events that were in the queue, as long as it didn't fail to send
			while (eventDataEnvelope != null) {
				bSent = uploadEventEnvelope(eventDataEnvelope, bFromQueue);  // as long as event was sent to server, it sent (even if server had an error)
				if (!bSent) {
					//if (!bFromQueue)
					{
						bAddedQueue = true;
						eventQueue.add(eventDataEnvelope);
						while (eventQueue.size() > 200)
							eventQueue.poll();
					}
					break;
				} else {
					eventDataEnvelope = eventQueue.poll();
					bFromQueue = true;
				}
			}
			// persist the queue every 3 hrs in case something happens
			if (event != null && (event.isCheckin || bAddedQueue))
				saveEvents(eventQueue);
		}
		finally
		{
			if (!local)
			{
				//LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "report(false)", "uploadingEvent(false)");
				owner.uploadingEvent(false);

			}
		}
	}
	
	/*
	 * Uploads the EVentEnvelope as a JSON packet using wsManager
	 * and then updates that event in the database with the result of sending
	 * return bSent = true if it reaches the server
	 */
	private boolean uploadEventEnvelope (EventDataEnvelope eventDataEnvelope, boolean queued)
	{
		//now upload the eventDataEnvelope to the server
		int eventTypeId = 0;
		int eventId = 0;
		EventType eventType = null;
		EventData eventData = null;
		int eventFlags = 0;
		boolean bForceSend = false;

		// if this is just a dummy event, pretend it was sent so it goes on to the next queued event
		if (eventDataEnvelope.getlStartTime() == 0l)
			return true;
		Iterator<EventData> iterator = null;
		try
		{
			iterator = eventDataEnvelope.getoEventData().iterator();
			// Last chance to update fields on an event before its about to be sent to server
			// read the event from the disk-based SQLite events database to see if the event type changed due to dropped call confirmation
			for (;iterator.hasNext();)
			{
				eventData = iterator.next ();
				eventTypeId = eventData.getEventType();
				eventType = EventType.get(eventTypeId);
				eventId = (int)eventData.getCallID();  // local SQLite database id of the event
				eventFlags = eventData.getFlags();
				if (eventId > 0 && (eventTypeId == 4 || eventTypeId == 6 || eventTypeId == 1 || eventTypeId == 7))
				{
					HashMap<String, String> event = ReportManager.getInstance(owner).getEventDetails(eventId);
					if (event != null && event.containsKey(EventKeys.RATING))
					{
						int newType = Integer.parseInt(event.get(EventKeys.TYPE));
						int rating = Integer.parseInt(event.get(EventKeys.RATING));
						eventData.setEventType(newType);
						eventData.setEventIndex(rating);
					}
				}

				if (eventType == EventType.MAN_SPEEDTEST || eventType == EventType.EVT_STARTUP || eventType == EventType.EVT_SHUTDOWN)
					bForceSend = true;
				// Force send TroubleTweets
				else if (eventType.getIntValue() >= 30 && eventType.getIntValue() <= 39)
					bForceSend = true;
                else if (eventType == EventType.EVT_VQ_CALL || eventType == EventType.VIDEO_TEST || eventType == EventType.AUDIO_TEST  || eventType == EventType.WEBPAGE_TEST)
                    bForceSend = true;
			}

		}
		catch (Exception e)
		{
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "uploadEventEnvelope", "exception reading the event type", e);
			return true;
		}
		
		boolean bSent = false;
		boolean allowRoaming = PreferenceManager.getDefaultSharedPreferences(owner).getBoolean(PreferenceKeys.Miscellaneous.DATA_ROAMING, false);
		boolean defaultWifi = false;
		String noConfirm = (owner.getResources().getString(R.string.WIFI_DEFAULT));
    	if (noConfirm.equals("1"))
    		defaultWifi = true;  // don't even allow confirmation buttons on drilled down event
    	
		boolean sendOnWifi = PreferenceManager.getDefaultSharedPreferences(owner).getBoolean(PreferenceKeys.Miscellaneous.SEND_ON_WIFI, defaultWifi);
		
		
 		int resultflag = EventObj.SERVER_ERROR;
 		if (queued && (eventType == EventType.TRAVEL_CHECK || eventType == EventType.EVT_STARTUP))
 		{
 			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "run", "not send queued event of type=" + eventType + " id=" + eventId);
 			return true;
 		}
		if (!owner.isOnline() || owner.getPhoneState().isCallConnected()) // if no connectivity, send event be sent later?
		{
			resultflag = EventObj.SERVER_NODATA;
			if (!owner.isOnline())
				LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "run", "offline, unable to send event type=" + eventType + " id=" + eventId);
			else if (owner.getPhoneState().isCallConnected())
				LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "run", "call in progress, not yet sending event type=" + eventType + " id=" + eventId);
		}
		else if (!owner.isServiceRunning() && eventType != EventType.EVT_SHUTDOWN)
		{
			resultflag = EventObj.SERVER_NODATA;
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "run", "service was stopped, queueing event " + eventType + " id=" + eventId);
		
		}
		else if (!allowRoaming && owner.getPhoneState().isRoaming() && !PhoneState.isNetworkWifi(owner) && !bForceSend)
		{
			resultflag = EventObj.SERVER_NODATA;
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "run", "roaming no-wifi, unable to send event type=" + eventType + " id=" + eventId);
		}
		else if (sendOnWifi && !PhoneState.isNetworkWifi(owner) && !bForceSend)
		{
			resultflag = EventObj.SERVER_NODATA;
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "run", "queuing no-wifi, unable to send event type=" + eventType + " id=" + eventId);
		}
		else
		{
			//synchronized (owner)
			{
				try {
					int allowSpeedTest = 0;
					if (!PhoneState.isNetworkWifi(owner) && !queued)
						allowSpeedTest = owner.getUsageLimits().allowSpeedTest(100000);
					eventDataEnvelope.setAllowSpeedTest(allowSpeedTest);
					//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "run", "uploading staged event type=" + eventType + " id=" + eventId);
					Gson gson = new Gson();
					String eventJSON = gson.toJson(eventDataEnvelope);
					String responseJSON = owner.getReportManager().submitEvent(eventJSON);
					EventResponse eventResponse = gson.fromJson(responseJSON, EventResponse.class);
					if (eventResponse != null) {
						eventResponse.init();
						eventResponse.handleEventResponse(owner, false);

						long[] eventids = eventResponse.getEventIds();
						int d = 0;
						iterator = eventDataEnvelope.getoEventData().iterator();
						// Last chance to update fields on an event before its about to be sent to server
						// read the event from the disk-based SQLite events database to see if the event type changed due to dropped call confirmation
						for (; iterator.hasNext(); ) {
							eventData = iterator.next();
							eventId = (int) eventData.getCallID();  // local SQLite database id of the event
							ReportManager reportManager = ReportManager.getInstance(owner.getApplicationContext());
							long svrEventID = eventData.getEventId();
//							if (d == 0 && event != null)
//								svrEventID = event.getEventID();
//							if (d == 1 && complimentaryEvent != null)
//								svrEventID = complimentaryEvent.getEventID();
							if (svrEventID == 0)
								svrEventID = eventids[d];
							LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "run", "uploaded eventtype=" + eventType + " eventid=" + svrEventID);
							reportManager.updateEventField(eventId, "eventid", Long.toString(svrEventID));
							if (eventData.getEventType() == EventType.EVT_VQ_CALL.getIntValue() || eventData.getEventType() == EventType.SIP_VQ_CALL.getIntValue() ||
									eventData.getEventType() == EventType.CONNECTION_FAILED.getIntValue())
								reportManager.updateEventField(eventId, Events.KEY_TYPE, Integer.toString(eventData.getEventType()));

							d++;
						}
					}
					resultflag = EventObj.SERVER_SENT;
					bSent = true;
					LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "run", "uploaded staged event type=" + eventType + " id=" + eventId);
				} catch (Exception e) {
					if (e instanceof UnknownHostException || e instanceof IOException || e instanceof SocketTimeoutException)
						bSent = false;
					else
						bSent = true;
					LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "uploadEventEnvelope", "exception uploading event type=" + eventType + " id=" + eventId, e);
				}
			}
		}
		try
		{
			eventDataEnvelope.getoEventData();
			eventData.setFlags (eventData.getFlags() | resultflag);

		}
		catch (Exception e)
		{
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "run", "error updating sent event", e);
		}
		return bSent;
	}
	
	
	/**
	 * Loads event requests from storage, and adds it to the queue 
	 */
	protected void loadEventsQueue(){
		
		ConcurrentLinkedQueue<EventDataEnvelope> eventQueue = owner.getEventManager().getEventQueue();
		if (eventQueue == null)
		{
			eventQueue = new ConcurrentLinkedQueue<EventDataEnvelope>();
			owner.getEventManager().setEventQueue(eventQueue);
		} else
			return;
	
		Gson gson = new Gson();
        SharedPreferences secureSettings = MainService.getSecurePreferences(owner);
		if (secureSettings.contains(PreferenceKeys.Miscellaneous.EVENTS_QUEUE)){
			try {
				String strQueue = secureSettings.getString(PreferenceKeys.Miscellaneous.EVENTS_QUEUE, "");
				//LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "loadQueue", strQueue);
				if (strQueue.length() < 100)
					return;
				JSONArray jsonqueue = new JSONArray(strQueue);
				for(int i = 0; i<jsonqueue.length(); i++){
					JSONObject jsonRequest = jsonqueue.getJSONObject(i); 
					//if(jsonRequest.getString("type").equals(EventDataEnvelope.TAG)) 
					{
						EventDataEnvelope request = gson.fromJson(jsonRequest.toString(), EventDataEnvelope.class);
						//EventDataEnvelope request = new EventDataEnvelope(jsonRequest);
						eventQueue.add(request);
					}
				}
				// remove the oldest events until queue is below 1000
				while (eventQueue.size() > 300)
					eventQueue.poll();
			} catch (JSONException e) {
				LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "loadEventsQueue", "JSONException loading events from storage", e);
			}
			catch (Exception e) {
				LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "loadEventsQueue", "Exception loading events from storage", e);
			}
		}
		
	}
	
	/**
	 * Persists the queue of events to the phone's preferences
	 */
	protected void saveEvents(ConcurrentLinkedQueue<EventDataEnvelope> eventQueue){
		//JSONArray jsonQueue= new JSONArray();
		if (eventQueue == null)
			return;
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		Gson gson = new Gson();
		// remove the oldest events until queue is below 400
		while (eventQueue.size() > 300)
			eventQueue.poll();
		
		for(EventDataEnvelope eventEnv: eventQueue){
			try {
				String strJSON = gson.toJson(eventEnv);
				sb.append(strJSON);
				sb.append(",");
		        //JSONObject evtJSON =  new JSONObject(strJSON);
				//jsonQueue.put(evtJSON);
			} catch (Exception e) {
				LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "persistQueue", "failed to persist event request", e);
			}
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append("]");

		SharedPreferences preferenceSettings = MainService.getSecurePreferences(owner);
		String stringQueue = sb.toString();//  jsonQueue.toString();

		preferenceSettings.edit().putString(PreferenceKeys.Miscellaneous.EVENTS_QUEUE, stringQueue).commit();
	}
	/**
	 * Uses the provided event instance to generate a {@link EventData} instance.
	 * @param _event
	 * @return
	 */

	private EventData generateEventDataFromEvent(EventObj _event, boolean local){
		Cursor cellDataCursor = null;
		Cursor locationDataCursor = null;
		Cursor signalDataCursor = null;
		boolean bTroubleTweet = false;
		if (_event.getEventType().getIntValue() >= EventType.TT_DROP.getIntValue() && _event.getEventType().getIntValue() <= EventType.TT_NO_SVC.getIntValue())
			bTroubleTweet = true;
		
		try 
		{
			if (!bTroubleTweet) {	
				String accuracyWhere = "";
				if(_event.getEventType() == EventType.MAN_PLOTTING || (_event.getFlags() & EventObj.MANUAL_SAMPLES) > 0) {
					accuracyWhere = " and accuracy = -1 or accuracy = -2";
				} 
				else if( _event.getEventType() == EventType.MAN_TRANSIT){
					accuracyWhere = " and accuracy = -3 or accuracy = -4";
				}	
				else {
					accuracyWhere = " and accuracy <> -1";
				}
				cellDataCursor = getCellsAssociatedWithEvent(_event);
				locationDataCursor = getLocationsAssociatedWithEvent(_event, accuracyWhere );
				signalDataCursor = getSignalStrengthsAssociatedWithEvent(_event);
			}
			 
			int MCCMNC[] = owner.getMCCMNC();
			int mcc = 0;
			int mnc = 0;
			if (MCCMNC != null && MCCMNC.length > 1)
			{
				mcc = MCCMNC[0];
				mnc = MCCMNC[1];
			}
	
			int localId = 0;
			if (_event.getUri() != null)
				localId = Integer.parseInt(_event.getUri().getLastPathSegment());
			
			//if(owner.getLastMMCSignal() != null) {
			//	Integer signal = owner.getLastMMCSignal().getDbmValue(owner.getNetworkType(), owner.getPhoneType());
				//Marking the rssi unknown with -256
			//	rssi = signal != null ? (int)signal : -256;
			//}
		
			CoverageSamplesSend covSamples = null;
			String neighbors = "";
			String connections = "";
			String trend2 = "";
			String Stats = "";
			String APs = "";
			String Apps = "";
			String ThroughputStats = "";
			String latency = "fail";
			int lookupid1 = 0;
			int lookupid2 = 0;
			String transitAccel = "";
			
			boolean check = false;
			
			if (local == false && !bTroubleTweet)
			{
				try{
					covSamples = trendStringGenerator.generateSamples(	//trend string (primary)
					cellDataCursor, locationDataCursor, signalDataCursor, owner,_event);
				}
				catch (Exception e)
				{
					LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "generateEventDataFromEvent", "exception occured trying to generate trend string ", e);
				}
			
				if (locationDataCursor == null || locationDataCursor.getCount() == 0 )
				{
					// failed to get GPS location, send a little GPS diagnosis
					boolean gpsEnabled = owner.getGpsManager().isLocationEnabled();
					if (!gpsEnabled)
						trend2 = "GPS=disabled,";
					else
						trend2 = "GPS=enabled,";
				}
				if (_event.getEventType() == EventType.COV_UPDATE)
				{

					{
						int intervalDM = PreferenceManager.getDefaultSharedPreferences(owner).getInt(PreferenceKeys.Miscellaneous.MANAGE_DATAMONITOR, 0);
						if (intervalDM > 0) // if enabled
						{
							int sleepHandoffs = owner.getUsageLimits().handleCheckin(true);
							trend2 += "IdleHandoffs=" + sleepHandoffs + ",";	
							SharedPreferences preferenceSettings = PreferenceManager.getDefaultSharedPreferences(owner);
							int allowDM = preferenceSettings.getInt(PreferenceKeys.Miscellaneous.MANAGE_DATAMONITOR, 0);
							if(allowDM > 0) {
								//DataMonitorDBReader dbReader = new DataMonitorDBReader();
								//DataMonitorDBWriter dbWriter = new DataMonitorDBWriter();
								
								try{
									
									//get Stats, APs, and Apps strings
									Stats = "[" + owner.getDataMonitorStats().getStatsString() + "]";
									APs = "5,type,start,dur,id,sig," + owner.getAccessPointHistory().toString();	
								} catch (Exception e) {
									LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "generateEventDataFromEvent", "exception in getStatsString: ", e);
								}
								try{
									owner.getAccessPointHistory().clearAccessPointsHistory(); 
								} catch (Exception e) {
									LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "generateEventDataFromEvent", "exception in clearAccessPointsHistory: ", e);
								}
								try{
									Apps = owner.getDataMonitorStats().getRunningAppsString(true);
									//cleanup
									owner.getDataMonitorStats().cleanupStatsDB ();

								} catch (Exception e) {
									LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "generateEventDataFromEvent", "exception in getRunningAppsString: ", e);
								}
								
							}
						}
					} 
				}
				if (_event.getEventType() == EventType.MAN_TRANSIT) {

					lookupid1 = _event.getLookupid1();
					lookupid2 = _event.getLookupid2();
					Apps = _event.getAppData();
					if(lookupid1 == 0 || lookupid2 == 0)
						return null;
				}
                if (_event.getEventType() == EventType.EVT_VQ_CALL)
                {
                    //Apps = _event.getAppData();
                }
                if (_event.getJSONResult() != null)
                {
					_event.setTcpStatsToJSON ();
                    Stats = _event.getJSONResult();
                }

                lookupid2 = _event.getLookupid2();
                lookupid1 = _event.getLookupid1();
					
				long startTimestamp = _event.getEventTimestamp() - _event.getEventType().getPreEventStageTime();	//get the starting timestamp of the trend string
				
				neighbors = owner.getCellHistory().getNeighborHistory(startTimestamp, _event.getEventTimestamp());
				connections = owner.getConnectionHistory().getHistory(startTimestamp, _event.getEventTimestamp(), _event.getStageTimestamp()+5000, _event);
				String servicemode = owner.getConnectionHistory().getServiceModeHistory(startTimestamp, _event.getEventTimestamp(), _event.getStageTimestamp() + 5000, _event.getEventType());
				if (servicemode != null && servicemode.length() > 10)
					trend2 = servicemode;
			}

			String apiKey = MainService.getApiKey(owner);
			
			int iUserID = MainService.getUserID(owner);
			if (iUserID == 0)
				iUserID = -1;
			Location location = _event.getLocation();
			
			if (location == null) {
				location = new Location("");
				if(_event.getEventType() == EventType.MAN_TRANSIT) {
					String loc = PreferenceManager.getDefaultSharedPreferences(owner).getString("transitEvent", null);
					if(loc != null) {
						String[] locs = loc.split(",");
						int lat = Integer.valueOf(locs[0]);
						int lon = Integer.valueOf(locs[1]);
						location = new Location("");
						location.setLatitude(lat/1000000.0);
						location.setLongitude(lon/1000000.0);
						PreferenceManager.getDefaultSharedPreferences(owner).edit().putString(
				    			"transitEvent", null).commit();
					}
				}
			}

			int bsHigh = 0, bsLow = 0, bsMid = 0, bsCode = 0;
			
			if (_event.getCell() != null) {
				bsHigh = _event.getCell().getBSHigh();
				bsLow = _event.getCell().getBSLow();
				bsMid = _event.getCell().getBSMid();
				bsCode = _event.getCell().getBSCode();
			}

			Integer signalDB = -255;
			if (_event.getSignal() != null)
				signalDB = _event.getSignal().getDbmValue(owner.getPhoneState().getNetworkType(), owner.getPhoneState().getPhoneType());
			// unknown signal? server sees that as -255
			if (signalDB == null || signalDB == 0)
				signalDB = -255; 
			
			EventData eventData = new EventData(
				0,	// 1 would allow the server to request an auto speed test
				_event.getConnectTime(),	//ConnectTime
				"",	//phone number
				owner.getPhoneState().getNetworkOperatorName(),
				String.format("Android %s,%s,%s", DeviceInfoOld.getManufacturer(), DeviceInfoOld.getPhoneModel(), DeviceInfoOld.getDevice()), 
				_event.getDuration(),	// the duration between the event and last complementary event
				covSamples,	// trend string 
				trend2, //trend string (secondary)
				neighbors,
				connections,
				_event.getEventTimestamp() / 1000,
				location.getTime() / 1000,	//timestamp of the gps fix used for this event (will be filled later)
				0L, //lStartTime (will be filled later)
				(float)location.getLongitude(),	//fltEventLng is the unscaled latitude
				(float)location.getLatitude(), //fltEventLat is the unscaled longitude
				_event.getEventType().getIntValue(),
				_event.getEventID(),	//we don't know the server side event id yet
				0L, //This is generated by the server so we don't worry about it just yet
				(int)_event.getEventIndex(),
				0,	//sample interval is deprecated
				(int)location.getAltitude(),	//altitude (will be filled later)
				(int)location.getAccuracy(),	//uncertainty (will be filled later)
				(int)location.getSpeed(),	//speed (will be filled later)
				(int)location.getBearing(),	//heading (will be filled later)
				_event.getSatellites(),	//number of satellites
				signalDB,  //signal strengh (will be filled later)
				bsLow,	// cellId at the time of the event
				bsMid,	//nid
				bsCode,	//previous cell (will be filled later) 
				getAppVersionCode(),
				_event.getBattery(),	// battery level
				0, // ec/i0 may be filled in later
				_event.getFlags(),	// flags
				localId,	//CallId is internal to the server
				iUserID,
				mcc,
				mnc,
				bsHigh,	//LAC 
				_event.getCause(),	// cause of dropped call
				apiKey,
				DeviceInfo.getIPAddress(),
				Stats, 
				APs, 
				Apps,
				latency,
				lookupid1,
				lookupid2
			);
			
			eventData.setAppName (_event.getAppName());
			if (_event.getEventType().waitsForSpeed()) {
				eventData.setLatency (_event.getLatency());
				eventData.setDownloadSpeed (_event.getDownloadSpeed());
				eventData.setUploadSpeed (_event.getUploadSpeed());
				eventData.setDownloadSize (_event.getDownloadSize());
				eventData.setUploadSize (_event.getUploadSize());
				eventData.setTier (_event.getTier());	
				if (_event.getEventType() == EventType.LATENCY_TEST && _event.getLatency() < 0)
					eventData.setDownloadSpeed(-_event.getLatency());
				
			}
			
			if (_event.getEventType() == EventType.APP_MONITORING && !local) {	

				if(_event.getAppData() != null) {
					eventData.setRunningApps(_event.getAppData());	
					LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "generateEventDataFromEvent", "AppData: " + _event.getAppData());
				}

			}

			if (_event.getEventID () > 0)
				eventData.setEventId (_event.getEventID());
			eventData.setWifi(_event.getWifiInfo(), _event.getWifiConfig());

			return eventData;
		} catch (Exception ex){
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "generateEventDataFromEvent", "exception occured trying to generate event data ", ex);
		}
		finally
		{
			//now close the cursors
			if (cellDataCursor != null)
				cellDataCursor.close();
			if (locationDataCursor != null)
				locationDataCursor.close();
			if (signalDataCursor != null)
				signalDataCursor.close();
		}
		return null;
	}

	/**
	 * Uses the provided event data list to generate a {@link EventDataEnvelope} instance.
	 * @param eventDataList
	 * @return
	 */
	private EventDataEnvelope generateEventDataEnvFromEventList(List<EventData> eventDataList, String phoneNumber) {
		try
		{
			//SharedPreferences serviceSetting = PreferenceManager.getDefaultSharedPreferences(owner);
			int userID = MainService.getUserID(owner);
			if (userID <= 0 && !owner.isServiceRunning())
				userID = owner.getLastUserID (); // have the userid if the user logged out and service is closing
			String apikey = MainService.getApiKey(owner);
			int MCCMNC[] = owner.getMCCMNC();
			int mcc = 0;
			int mnc = 0;
			if (MCCMNC != null && MCCMNC.length > 1)
			{
				mcc = MCCMNC[0];
				mnc = MCCMNC[1];
			}
			DeviceInfo mmcdev = owner.getDevice();
            
			EventDataEnvelope eventDataEnvelope = new EventDataEnvelope(
				0, 				//qos rating. This is deprecated
				0, 				//deprecated
				getAppVersionCode(), 
				0, 				//TODO 1 if the call was dropped, 0 otherwise
				0, 				//TODO 1 if dataspeed test is allowed, 0 otherwise
				userID,
				owner.getPhoneState().getNetworkOperatorName(),
				String.format(Locale.US,"Android %s,%s,%s", DeviceInfoOld.getManufacturer(), DeviceInfoOld.getPhoneModel(), DeviceInfoOld.getDevice()), 
				eventDataList.get(0).getlTimestamp(), 
				0,				//deprecated 
				0,				//deprecated 
				phoneNumber, 		
				DeviceInfoOld.battery, 			
				String.format(Locale.US,"Android %d", DeviceInfoOld.getAndroidVersion()),
				owner.getPhoneState().getPhoneType(), owner.getPhoneState().getNetworkType(),owner.getPhoneState().isRoaming(),
				DeviceInfo.getIPAddress(), mcc, mnc,
				mmcdev.getIMSI(),
				apikey,
				eventDataList
			);
			return eventDataEnvelope;
		}
	    catch (Exception ex){
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "generateEventDataFromEvent", "exception occured trying to generate trend string: " + ex.getMessage());
		}
		return null;
	}
	
	
	private int getAppVersionCode(){
		try {
			return owner.getPackageManager().getPackageInfo(owner.getPackageName(), 0).versionCode;
		} catch(NameNotFoundException e){
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "getAppVersionCode", "Could not find app version" + e.getMessage());
		}
		return -1;
	} 
	
	/**
	 * This method gets a cursor that contains all the location objects that are associated with the
	 * given event.
	 * @return
	 */
	private Cursor getLocationsAssociatedWithEvent(EventObj _event, String accuracy){

		long startTime = _event.getEventTimestamp(); 
		//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "getLocationsAssociatedWithEvent", "startTime="+startTime);
		Cursor cursor = owner.getDBProvider().query(
				UriMatch.LOCATIONS.getContentUri(),
				null,
				Tables.Locations.TIMESTAMP  + ">=?" + accuracy,
				new String[]{ String.valueOf(startTime)},
				Tables.Locations.TIMESTAMP + " ASC"
			);
		cursor.moveToFirst();
		long gpsTime = 0;
		if (!cursor.isBeforeFirst())
		{
			gpsTime = cursor.getLong(1);
			//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "getLocationsAssociatedWithEvent", "time="+gpsTime);
			
		}
		return cursor;
	}
	
	/**
	 * This method gets a cursor that contains all the baseStation rows that are associated with the given
	 * event.
	 * @return
	 */
	private Cursor getCellsAssociatedWithEvent(EventObj _event){
		Uri baseStationTable = 	(UriMatch.BASE_STATIONS.getContentUri()
			/*owner.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA 
			? UriMatch.BASE_STATIONS_CDMA.getContentUri()
			: UriMatch.BASE_STATIONS_GSM.getContentUri()*/
		);
	
		// If we go back to 15 minutes before the event for cellid changes, then it will walk forward to find the latest change before the event
		long startTime = _event.getEventTimestamp() - _event.getEventType().getPreEventStageTime() - 900000; 
		
		Cursor cursor = owner.getDBProvider().query(
				baseStationTable,
				null,
				Tables.BaseStations.TIMESTAMP  + ">?",
				new String[]{ String.valueOf(startTime)},
				Tables.BaseStations.TIMESTAMP + " ASC"
			);
		return cursor;
	}
	
	/**
	 * This method gets a cursor that contains all the signal strength rows that are associated with the given
	 * event. It gets signal strength well before event, but only the last one before 30 seconds before event will be the start
	 * @return
	 */
	private Cursor getSignalStrengthsAssociatedWithEvent(EventObj _event){

		// Single signal strength table now
		Uri signalStrengthTable = UriMatch.SIGNAL_STRENGTHS.getContentUri();
		String[] projection = new String[]{BaseColumns._ID, Tables.TIMESTAMP_COLUMN_NAME, Tables.SignalStrengths.SIGNAL, Tables.SignalStrengths.ECI0, Tables.SignalStrengths.SNR, Tables.SignalStrengths.BER, Tables.SignalStrengths.RSCP, Tables.SignalStrengths.SIGNAL2G, Tables.SignalStrengths.LTE_SIGNAL, Tables.SignalStrengths.LTE_RSRP, Tables.SignalStrengths.LTE_RSRQ, Tables.SignalStrengths.LTE_SNR, Tables.SignalStrengths.LTE_CQI, Tables.SignalStrengths.SIGNALBARS, Tables.SignalStrengths.ECN0, Tables.SignalStrengths.WIFISIGNAL, Tables.SignalStrengths.COVERAGE };

		long startTime = _event.getEventTimestamp() - _event.getEventType().getPreEventStageTime() - 300000; 
		
		Cursor cursor = owner.getDBProvider().query(
				signalStrengthTable,
				projection,
				Tables.SignalStrengths.TIMESTAMP  + ">?",
				new String[]{ String.valueOf(startTime)},
				Tables.SignalStrengths.TIMESTAMP + " ASC"
			);
		
		return cursor;
	}


}

package com.cortxt.app.mmcui.Activities.MyCoverage.EventsOverlay;

import java.text.DateFormat;
import java.util.Date;

import com.google.android.maps.GeoPoint;

public class EventMarker {
	private int mEventId;
	GeoPoint mLocation;
	private long mTimeStamp;
	private int mEventType;
	private int mMarkerResource;
	private String mDetails;
	private int netchangeType;
	private String mTitle;
	
	public EventMarker(int eventId, int latitudeE6, int longitudeE6, long timeStamp, int eventType, int markerResource, String title, int netchgType, String details) {
		super();
		mEventId = eventId;
		mLocation = new GeoPoint(latitudeE6, longitudeE6);
		mTimeStamp = timeStamp;
		mEventType = eventType;
		mMarkerResource = markerResource;
		mTitle = title;
		netchangeType = netchgType;
		mDetails = details;
	}

	public int getEventId() {
		return mEventId;
	}
	
	public long getTimeStamp() {
		return mTimeStamp;
	}
	
	public int getNetChangeType () {
		return netchangeType;
	}
	
	public GeoPoint getLocation() {
		return mLocation;
	}
	
	public int getEventType() {
		return mEventType;
	}
	
	public String getTitle() {
		return mTitle;
	}

	public String getDetails() {
		return mDetails;
	}
	
	public int getMarkerResource() {
		return mMarkerResource;
	}

	@Override
	public String toString() {
		String string = "event: " + getTitle() + " id: " + getEventId();
		Date date = new Date(mTimeStamp);
		DateFormat dateFormat = DateFormat.getDateTimeInstance();
		string += " date: " + dateFormat.format(date);
		return string;
	}
}

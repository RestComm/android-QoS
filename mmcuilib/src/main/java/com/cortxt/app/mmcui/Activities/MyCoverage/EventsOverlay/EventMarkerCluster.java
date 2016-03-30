package com.cortxt.app.mmcui.Activities.MyCoverage.EventsOverlay;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;

import android.graphics.Point;

public class EventMarkerCluster {
	/**
	 * The maximum distance between markers at which they are still clustered.
	 */
	private static final int CLUSTER_DISTANCE_THRESHOLD = 16;

	private ArrayList<EventMarker> mMarkers;

	public EventMarkerCluster() {
		mMarkers = new ArrayList<EventMarker>();
	}

	/**
	 * Adds a marker to the list.
	 * @param marker
	 */
	public void addMarker(EventMarker marker) {
		mMarkers.add(marker);
	}

	/**
	 * Checks if <code>m</code> is close enough to the cluster to be a part of it.
	 * @param m
	 * @param projection
	 * @return
	 */
	public boolean isClose(EventMarker m, Projection projection) {
		Point cluster = projection.toPixels(mMarkers.get(0).getLocation(), null);
		Point marker = projection.toPixels(m.getLocation(), null);

		if( (Math.abs(cluster.x - marker.x) < CLUSTER_DISTANCE_THRESHOLD)  &&  (Math.abs(cluster.y - marker.y) < CLUSTER_DISTANCE_THRESHOLD) ) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * If there is only 1 event in the cluster, returns the <code>eventType</code> of the event. Returns -1 if there is more than 1 event in the cluster.
	 * @return
	 */
	public int getEventType() {
		if(mMarkers.size() == 1) {
			return mMarkers.get(0).getEventType();
		}
		else if(mMarkers.size() > 1) {
			return -1;
		}
		else {
			throw new IllegalStateException("Cluster size was 0");
		}
	}

	/**
	 * Returns the location of the cluster.
	 * NOTE: The location of the first point in the cluster is treated as the location of the cluster
	 * @return
	 */
	public GeoPoint getLocation() {
		return mMarkers.get(0).getLocation();
	}


	public String getTitle() {
		if(mMarkers.size() == 1) {
			return mMarkers.get(0).getTitle();
		}
		else if(mMarkers.size() > 1) {
			return "";
		}
		else {
			throw new IllegalStateException("Cluster size was 0");
		}
	}

	public String getSnippet() {
		return "";
	}

	/**
	 * If there is only 1 event in the cluster, returns the <code>timestamp</code> of the event. Returns -1 if there is more than 1 event in the cluster.
	 * @return
	 */
	public long getTimestamp() {
		if(mMarkers.size() == 1) {
			return mMarkers.get(0).getTimeStamp();
		}
		else if(mMarkers.size() > 1) {
			return -1;
		}
		else {
			throw new IllegalStateException("Cluster size was 0");
		}
	}

	/**
	 * If there is only 1 event in the cluster, returns the resource id of the marker's drawable. Returns -1 if there is more than 1 event in the cluster.
	 * @return
	 */
	public int getMarkerResource() {
		if(mMarkers.size() == 1) {
			return mMarkers.get(0).getMarkerResource();
		}
		else if(mMarkers.size() > 1) {
			return -1;
		}
		else {
			throw new IllegalStateException("Cluster size was 0");
		}
	}

	/**
	 * Returns the number of markers in the cluster.
	 * @return
	 */
	public int size() {
		return mMarkers.size();
	}

	protected ArrayList<EventMarker> getMarkers() {
		return mMarkers;
	}

	@Override
	public String toString() {
		String string = "cluster(" + size() + "): [";

		for(int i=0; i<mMarkers.size(); i++) {
			string += mMarkers.toString() + ", ";
		}

		string += "]";
		return string;
	}
}

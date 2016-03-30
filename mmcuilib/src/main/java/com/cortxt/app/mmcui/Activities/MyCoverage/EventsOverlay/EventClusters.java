package com.cortxt.app.mmcui.Activities.MyCoverage.EventsOverlay;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class EventClusters {
	private ArrayList<EventMarkerCluster> mClusters;
	private Context mContext;
	private MapView mMapView;

	public EventClusters(Context context, MapView mapView) {
		mClusters = new ArrayList<EventMarkerCluster>();
		this.mContext = context;
		this.mMapView = mapView;
	}

	/**
	 * Adds <code>marker</code> to the first cluster that it is close enough to.
	 * If no clusters are close enough, a new cluster is created for the <code>marker</code>
	 * @param marker marker to add
	 */
	public void addEventMarker(EventMarker marker) {
		add(marker, this.mClusters);
	}
	
	public boolean hasEvent (int id)
	{
		for (EventMarkerCluster cluster: mClusters)
		{
			for (EventMarker marker : cluster.getMarkers())
			{
				if (marker.getEventId() == id)
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns a list of <code>EventOverlayItems</code> that are to be drawn on the map through <code>EventsOverlay</code>. The list contains one <code>EventOverlayItem</code> for each cluster.
	 * @return
	 */
	public ArrayList<EventOverlayItem> getMarkersForOverlay() {
		ArrayList<EventOverlayItem> overlayItems = new ArrayList<EventOverlayItem>();

		for(int i=0; i<mClusters.size(); i++) {
			//add each clusters item that is on the screen to overlayItems
			int eventType = mClusters.get(i).getEventType();

			GeoPoint point = mClusters.get(i).getLocation();
			String title = mClusters.get(i).getTitle();
			String snippet = mClusters.get(i).getSnippet();
			int clusterSize = mClusters.get(i).size();

			EventOverlayItem eventOverlayItem = new EventOverlayItem(point, title, snippet, clusterSize);

			if(eventType != -1) {
				int resourceId = mClusters.get(i).getMarkerResource();
				if (resourceId > 0)
				{
					Drawable marker = mContext.getResources().getDrawable(mClusters.get(i).getMarkerResource());
					marker.setBounds(
						marker.getIntrinsicWidth()/-2,
						marker.getIntrinsicHeight() * -1,
						marker.getIntrinsicWidth()/2,
						0
					);

					eventOverlayItem.setMarker(marker);
				}
			}
			overlayItems.add(eventOverlayItem);
		}

		return overlayItems;
	}

	/**
	 * To be called when the map zoom level changes. Recreates clusters using the new distances between markers.
	 */
	public void regroupClusters() {
		ArrayList<EventMarkerCluster> newClusters = new ArrayList<EventMarkerCluster>();

		for(int i=0; i<mClusters.size(); i++) {
			ArrayList<EventMarker> markers = mClusters.get(i).getMarkers();
			for(int j=0; j<markers.size(); j++) {
				EventMarker newMarker = markers.get(j);
				add(newMarker, newClusters);
			}
		}

		mClusters = newClusters;
	}

	public void clear() {
		mClusters.clear();
	}

	/**
	 * Adds <code>marker</code> to the first cluster that it is close enough to.
	 * If no clusters are close enough, a new cluster is created for the <code>marker</code>
	 * @param marker marker to add
	 * @param newClusters list of <code>ArrayList of EventMarkerCluster</code> to add to
	 */
	private void add(EventMarker marker, ArrayList<EventMarkerCluster> newClusters) {
		//if marker is close to any existing markers, add it to that cluster
		if (mMapView.getZoomLevel() < 17)
		{
			for(int i=0; i<newClusters.size(); i++) {
				if(newClusters.get(i).isClose(marker, mMapView.getProjection())) {
					newClusters.get(i).addMarker(marker);
					return;
				}
			}
		}

		//else create a new cluster and add marker to it
		EventMarkerCluster newCluster = new EventMarkerCluster();
		newCluster.addMarker(marker);
		newClusters.add(newCluster);
	}

	protected EventMarkerCluster getClusters(int index) {
		if (index < mClusters.size())
			return mClusters.get(index);
		return null;
	}

	@Override
	public String toString() {
		String string = "clusters: {";

		for(int i=0; i<mClusters.size(); i++) {
			string += mClusters.toString() + "; ";
		}

		string += "}";
		return string;
	}
}

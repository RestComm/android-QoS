package com.cortxt.app.mmcui.Activities.MyCoverage.EventsOverlay;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class EventOverlayItem extends OverlayItem {
	private int mClusterSize;

	public EventOverlayItem(GeoPoint point, String title, String snippet, int clusterSize) {
		super (point, snippet, title);
		this.mClusterSize = clusterSize;
	}
	
	public int getClusterSize() {
		return mClusterSize;
	}
}

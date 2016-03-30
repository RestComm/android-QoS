package com.cortxt.app.mmcui.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class CustomMyLocationOverlay extends MyLocationOverlay {
	public CustomMyLocationOverlay(Context context, MapView mapView) {
        super(context, mapView);
	}
	 @Override
	     protected void drawMyLocation(Canvas canvas, MapView mapView, Location location,
	             GeoPoint geoPoint, long when) {
	         //if (!recenter) 
		 	{
	             mapView.getController().stopAnimation(false);
	         }

	         // Now draw the location overlay.
	         super.drawMyLocation(canvas, mapView, location, geoPoint, when);
	     }
	}

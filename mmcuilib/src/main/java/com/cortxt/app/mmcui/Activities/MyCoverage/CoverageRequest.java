package com.cortxt.app.mmcui.Activities.MyCoverage;

import org.apache.http.client.methods.HttpGet;

import android.content.Context;

import com.cortxt.app.mmcutility.DataObjects.Carrier;
import com.google.android.maps.GeoPoint;

public class CoverageRequest extends HttpGet {
	private static final String TAG = CoverageRequest.class.getSimpleName();
	
	private static final String END_POINT = "/api/overlays";

	private static final String KEY_TYPE = "type";
	private static final String KEY_SW = "sw[]";
	private static final String KEY_NE = "ne[]";	
	
	public static final String TYPE_RSSI = "rssi";
	public static final String TYPE_VARIANCE = "variance";
	public static final String TYPE_RSSI_VARIANCE= "rssi-variance";
	
	/**
	 * Coordinates of south-west corner of area to request image for
	 */
	protected GeoPoint mSW;
	
	/**
	 * Coordinates of north-east corner of area to request image for
	 */
	protected GeoPoint mNE;
	
	/**
	 * Constructs a CoverageRequest object
	 * @param context
	 * @param type type of coverage to request
	 * @param carrier
	 * @param sw coordinates of south-west corner of area to request image for
	 * @param ne coordinates of north-east corner of area to request image for
	 */
	public CoverageRequest(Context context, String type, Carrier carrier, GeoPoint sw, GeoPoint ne) {
		mSW = sw;
		mNE = ne;

	}

	public GeoPoint getSW() {
		return mSW;
	}

	public GeoPoint getNE() {
		return mNE;
	}
	
	
}

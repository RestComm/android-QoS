package com.cortxt.app.mmcutility.Utils;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.PowerManager;

import com.cortxt.app.mmcutility.Reporters.ReportManager;


public class LocationRequest {

	int finalAccuracy = 0;
	int firstAccuracy = 0;
	int gpsSatellites = 0;
	Location gpsLocation;
	Location firstGpsLocation;
	Location netLocation;
	Location lastLocation;
	Location statsLocation;
	Location nearLocation;
	long gpsStartTime = 0;
	double firstAccuracyDeg = 0;
	public boolean bLastKnownLocation, bLocationChanged, bGPSTimeout = false;
	public boolean bNWRunning = false, bGPSRunning = false, bFinalLocation = false;
	Activity activity = null;
	Context mContext = null;

	Handler handler;
	GpsListenerForRequest locListener;
	LocationListenerForRequest locListener2;
//	public static LocationRequest startLocation(Context context, int timeout, int gpsAccuracy, OnLocationListener listener) {
//
//
//		LocationRequest lrequest = new LocationRequest (context, timeout, gpsAccuracy);
//		lrequest.lastLocation = location;
//		//lrequest.handler = handler;
//		lrequest.firstAccuracy = gpsAccuracy;
//		lrequest.firstAccuracyDeg = gpsAccuracy * 0.000005;
//		lrequest.timeout = timeout;
//		lrequest.bLastKnownLocation = true;
//		lrequest.bLocationChanged = true;
//		lrequest.mOnNewLocationListener = listener;
//
//		if (lrequest.mOnNewLocationListener != null)
//			lrequest.mOnNewLocationListener.onLocation (lrequest);
//
//		//if (handler != null)
//		//	handler.sendMessage(new Message ());
//
//		return lrequest;
//	}

	private OnLocationListener mOnNewLocationListener = null;
	private OnLocationListener mOnLocationListener = null;
	private OnStoppedListener mOnStoppedListener = null;
	private boolean bUpdateUI = false, bUseGPS = true;
	private int gpsTimeout = 90000;  // this timeout can be extended if 3 or more satellites in view
	public void setUpdateUI (boolean updateUI)
	{
		bUpdateUI = updateUI;
	}
	public void setUseGPS (boolean useGPS)
	{
		bUseGPS = useGPS;
	}
	public void setGPSTimeout (int timeout)
	{
		gpsTimeout = timeout;
	}
	public void setOnNewLocationListener(OnLocationListener listener)
	{
		mOnNewLocationListener = listener;
		if (this.lastLocation != null && listener != null) {
			this.netLocation = null;
			handleLocation(true);
		}
		//	mOnNewLocationListener.onLocation (this);
	}
	public void setOnLocationListener(OnLocationListener listener)
	{
		mOnLocationListener = listener;
		//if (this.lastLocation != null && listener != null)
		//	handleLocation(true);
		//	mOnNewLocationListener.onLocation (this);
	}

	public void setOnStoppedListener(OnStoppedListener listener)
	{
		mOnStoppedListener = listener;
	}

	public void handleLocation (boolean bNew)
	{
		if (bNew && mOnNewLocationListener != null){
			if (bUpdateUI == true && activity != null) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mOnNewLocationListener.onLocation(LocationRequest.this);
					}
				});
			} else
				mOnNewLocationListener.onLocation(this);
		}
		if (mOnLocationListener != null) {
			if (bUpdateUI == true && activity != null) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mOnLocationListener.onLocation(LocationRequest.this);
					}
				});
			} else
				mOnLocationListener.onLocation(this);
		}
	}

	public interface OnLocationListener
	{
		void onLocation (LocationRequest locationRequest);
	}

	public interface OnStoppedListener
	{
		void onStopped ();
	}
	
	// If set, continue until GPS accuracy reaches final accuracy, then stop
	public int getFinalAccuracy ()
	{
		return finalAccuracy;
	}
	public void setFinalAccuracy (int accuracy)
	{
		finalAccuracy = accuracy;
	}
	public int getSatellites ()
	{
		return gpsSatellites;
	}
//	public long getGpsTimeout ()
//	{
//		if (MMCService.getGpsManager() != null)
//			return MMCService.getGpsManager().gpsTimeout;
//		return 0;
//	}
	public Location getLocation ()
	{
		if (gpsLocation != null)
		{
			if (gpsLocation.getAccuracy() < finalAccuracy)
				return gpsLocation;
		}
		if (netLocation != null)
		{
			if (netLocation.getAccuracy() < 1600)
			{
				// If Gps wasnt accurate enough, still use if more accurate than network location
				if (gpsLocation != null && gpsLocation.getAccuracy() < netLocation.getAccuracy())
					return gpsLocation;
				return netLocation;
			}
			if (gpsLocation != null && gpsLocation.getAccuracy() < 1600)
				return gpsLocation;
		}
		return lastLocation;
	}
	public LocationRequest (Context context, int gpsAccuracy) {

		this.mContext = context;
		if (context instanceof Activity)
			this.activity = (Activity)context;
		Location location1, location2, location;
		location = ReportManager.getInstance(context.getApplicationContext()).getLastKnownLocation();
		location1 = ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER);
		location2 = ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (location != null && location.getTime() + 3 * 3600 * 1000 < System.currentTimeMillis())
		{
			// Prefer the MMC Stored location, but take the GPS last location if newer
			this.lastLocation = location;
			if (location1 != null && location1.getTime() + 60000 < location.getTime())
				this.lastLocation = location1;
		}
		else {
			if (location1 != null && (location2 == null || location1.getTime() > location2.getTime()))
				location = location1;
			if (location2 != null && (location1 == null || location2.getTime() > location1.getTime() + 60000))
				location = location2;
			this.lastLocation = location;
		}

		this.firstAccuracy = gpsAccuracy;
		this.finalAccuracy = gpsAccuracy;
		this.firstAccuracyDeg = gpsAccuracy * 0.000005;
		this.bLastKnownLocation = true;
		this.bLocationChanged = true;
	}

	public Location start ()
	{
		// Use both GPS and Network location to try to get an improved location for statistics
		// Indoors, GPS might not work, but in some locations Network might not work
		if (bUseGPS) {
			locListener = new GpsListenerForRequest();
			locListener.setFirstFixTimeout(gpsTimeout);//this.timeout); // using our own timeout to force gps off after
			// timeoutSeconds
			statsLocation = lastLocation;
			locListener.setOperationTimeout(0);
			locListener.setProvider(LocationManager.GPS_PROVIDER);
			bGPSRunning = true;
			gpsStartTime = System.currentTimeMillis();
			Global.registerLocationListener (true, locListener);
		}


		locListener2 = new LocationListenerForRequest();
		locListener2.setFirstFixTimeout(20 * 1000); // for network location, try for 20 seconds and decide whether to use it
		locListener2.setOperationTimeout(0);
		locListener2.setProvider(LocationManager.NETWORK_PROVIDER);
		bNWRunning = true;
		Global.registerLocationListener (false, locListener2);

		acquireWakeLock(this.mContext);
		return lastLocation;
	}

	public void LocationStopped (boolean bGPS)
	{
		if (bGPS)
			bGPSRunning = false;
		else
			bNWRunning = false;
		if (bGPSRunning == false && bNWRunning == false)
		{
			if (mOnStoppedListener != null)
				mOnStoppedListener.onStopped();
			releaseWakeLock();
		}
	}
	
	Location getGPSLocation ()
	{
		return gpsLocation;

	}
	
	public void stop ()
	{
		Global.unregisterLocationListener (true, locListener);
		Global.unregisterLocationListener (false, locListener2);
		bGPSRunning = false;
		bNWRunning = true;
	}
	
	class GpsListenerForRequest extends GpsListener {
		//
		public GpsListenerForRequest() {
			super("GpsListenerForRequest");
		}

		@Override
		public int attemptToRenewFirstFixTimeout(int numberOfSatellites) {
			if (numberOfSatellites >= 3){
				return 90000;
			}
			return 0;
		}
		/**
		 * if a timeout occurs, tell the listener
		 */
		@Override
		public void onTimeout() {
			if (bFinalLocation == true)
				return;
			bGPSTimeout = true;
			bGPSRunning = false;
			bLocationChanged = false;
			if (gpsLocation == null)
				statsLocation = netLocation;
			bFinalLocation = true;
			handleLocation (true);
		}
		//
		/**
		 * For events, the chaining property of the GpsManager is not utilised. Instead, we rely on the timeout to stop
		 * the gps for us. Therefore, after processing the location using both the
		 */
		@Override
		public boolean onLocationUpdate(Location location, int satellites) {
			gpsSatellites = satellites;
			boolean bLiveLocation = false; // consider it a live location if it has fluctuated
			if (location == null)
			{
				//gpsLocation = null;
				handleLocation (false);
				return true;
			}
			else
			{

				if (firstGpsLocation == null)
					firstGpsLocation = location;
				// consider it a live location if it has fluctuated
				if (location.getLatitude() != firstGpsLocation.getLatitude() || location.getLongitude() != firstGpsLocation.getLongitude())
				{
					if (gpsStartTime + 15000 < System.currentTimeMillis() || finalAccuracy == firstAccuracy)
						bLiveLocation = true;
				}
			}
			if (location.getAccuracy() < firstAccuracy && bLiveLocation) // We'll settle for 400 for statistics
			{
				boolean bLocChanged = false;

				gpsLocation = location;
				if (statsLocation == null || Math.abs(statsLocation.getLatitude() - location.getLatitude()) > firstAccuracyDeg || Math.abs(statsLocation.getLongitude() - location.getLongitude()) > firstAccuracyDeg)
				{
					if (netLocation == null || gpsLocation.getAccuracy() < netLocation.getAccuracy())
						bLocChanged = true;
				}
				if (bLastKnownLocation == true || bLocChanged == true || location.getAccuracy() < finalAccuracy) {
					statsLocation = location;

					bLastKnownLocation = false;
					bLocationChanged = bLocChanged;
					if (finalAccuracy < firstAccuracy)
						bLocationChanged = true;
					if (location.getAccuracy() < finalAccuracy)
						bFinalLocation = true;
					handleLocation (true);
					//if (mOnNewLocationListener != null)
					//	mOnNewLocationListener.onLocation (LocationRequest.this);
					// if (handler != null)
					//	 handler.sendMessage(new Message ());
					//return false;
				}

				//return true;
				if (bFinalLocation) {
					bGPSRunning = false;
					return false;
				}
				return true;
			}
			handleLocation (false);
			return true;
		}

		@Override
		public void gpsStarted ()
		{
		}
		@Override
		public void gpsStopped ()
		{
			LocationStopped (true);
		}
	}

	/**
	 * This class encapsulates the data and the logic for gps management for the service. This
	 * class is intended to be used for turning on the gps when an event takes place.
	 */
	class LocationListenerForRequest extends GpsListener {
	//
		public LocationListenerForRequest() {
			super("LocationListenerForStats");
		}
		//
		/**
		 * For events, the chaining property of the GpsManager is not utilised. Instead, we rely on the timeout to stop
		 * the gps for us. Therefore, after processing the location using both the
		 * {@link MMCServiceOld#processNewRawLocation(Location)} and
		 * {@link MMCServiceOld#processNewFilteredLocation(Location)} methods, we simply return true.
		 */
		/**
		 * When the Network Location times out after 15 seconds, send it in if its the best we got so far
		 */
		@Override
		public void onTimeout() {
			bNWRunning = false;
			// If we're still stuck on last known location, just send the best we got so far
			if (bLastKnownLocation == true && netLocation != null)
			{
				statsLocation = netLocation;
				if (gpsLocation != null && gpsLocation.getAccuracy() < firstAccuracy)
					statsLocation = gpsLocation;
				if (statsLocation == null || Math.abs(lastLocation.getLatitude() - statsLocation.getLatitude()) > firstAccuracyDeg || Math.abs(lastLocation.getLongitude() - statsLocation.getLongitude()) > firstAccuracyDeg)
					bLocationChanged = true;

				if (bGPSRunning == false)
					bFinalLocation = true;
				bLastKnownLocation = false;
				handleLocation (true);
			}
		}
		@Override
		public boolean onLocationUpdate(Location location, int satellites) {
			if (location == null)
				return true;
			boolean bLocChanged = false;
			boolean bBetterThanLastLocation = false;
			if (location.getAccuracy() < 50)
				location.setAccuracy(51);
			if (statsLocation == null || Math.abs(statsLocation.getLatitude() - location.getLatitude()) > firstAccuracyDeg || Math.abs(statsLocation.getLongitude() - location.getLongitude()) > firstAccuracyDeg)
			{
				if (bLastKnownLocation)
				{
					bLocChanged = true;
					if (lastLocation == null || Math.abs(lastLocation.getLatitude() - location.getLatitude()) > location.getAccuracy() * 0.000005 * 2 || Math.abs(lastLocation.getLongitude() - location.getLongitude()) > location.getAccuracy() * 0.000005 * 2)
						bBetterThanLastLocation = true;
				}
			}
			netLocation = location;
			if (location.getAccuracy() < finalAccuracy || bBetterThanLastLocation == true) // We'll settle for 400 for statistics
			{
				if (bLastKnownLocation == true || bLocChanged == true) {
					statsLocation = location;
					bLastKnownLocation = false;
					bLocationChanged = bLocChanged;
					handleLocation(true);
					return false;
					//if (mOnNewLocationListener != null)
					//	mOnNewLocationListener.onLocation (LocationRequest.this);
					//if (handler != null)
					//	handler.sendMessage(new Message ());
					//return false;
				}
			}
			handleLocation (false);
			return true;
			//return true;
		}

		@Override
		public void gpsStarted ()
		{
		}
		@Override
		public void gpsStopped ()
		{
			LocationStopped (false);
		}
	}

	private PowerManager.WakeLock wakeLock;
	//-------------------------------------------------------------------------------------------------
	void acquireWakeLock(Context context)
	{
		//Setup a WakeLock
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MMC LocationRequest partial wake");
		wakeLock.acquire();
//		wakeLock.setReferenceCounted(true);
//
//		//acquire the lock
//		if(!wakeLock.isHeld())
//		{
//			wakeLock.acquire();
//		}
	}

	void releaseWakeLock ()
	{
		//acquire the lock
		if(wakeLock.isHeld())
		{
			wakeLock.release();
		}
	}
	
}

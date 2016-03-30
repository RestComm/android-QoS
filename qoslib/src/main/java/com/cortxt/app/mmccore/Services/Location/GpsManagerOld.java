package com.cortxt.app.mmccore.Services.Location;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;

import com.cortxt.app.mmccore.MMCService;
//import com.cortxt.app.mmcui.Activities.NerdScreen;
import com.cortxt.app.mmccore.R;
import com.cortxt.app.mmccore.Services.Events.EventManager;
import com.cortxt.app.mmccore.Services.Intents.MMCIntentHandler;
import com.cortxt.app.mmcutility.DataObjects.EventObj;
import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.cortxt.app.mmcutility.Utils.GpsListener;
import com.cortxt.app.mmcutility.Utils.MMCLogger;

/**
 * This class manages locations for the MMC application. Specifically, this class manages the gps
 * for this application. This is because this application uses only gps for its locations. In the 
 * future, it is possible that a "fallback mechanism" using other sources of location might be 
 * employed.
 * @author Abhin
 *
 */
public class GpsManagerOld implements GpsStatus.Listener, LocationListener {
	/*
	 * =================================================================
	 * Start variable declaration
	 */
	private static final String TAG = GpsManagerOld.class.getSimpleName();
	public static final int LOCATION_UPDATE_MIN_TIME = 0;
	public static final int LOCATION_UPDATE_MIN_DIST = 0;

	public static final int MIN_CONSECUTIVE_GOOD_LOCATION_UPDATES_FOR_BUFFER = 0;


	private LocationManager locManager;
	private MMCService owner;
	private GpsStatus gpsStatus;
	public long gpsTimeout = 0;
	private int successfulLocationUpdates = 0;
	private int mNumberOfSatellites = 0;
	private String provider = null;
	private HandlerThread gpsHandlerThread = new HandlerThread("gps handlerthread");
	
	/**
	 * This is a general purpose timer that will be used for both first fix timeouts and
	 * operation timeouts.
	 */
	private Timer gpsTimer = new Timer(TAG);
	
	/**
	 * This collection holds all the listeners to this {@link GpsManagerOld}.
	 */
	private ArrayList<GpsListener> listeners = new ArrayList<GpsListener>();
	
	/*
	 * End variable declaration
	 * =================================================================
	 * Start constructor(s)
	 */
	
	public GpsManagerOld(MMCService owner){
		this.owner = owner;
		try{
			locManager = (LocationManager) owner.getSystemService(Service.LOCATION_SERVICE);
			locManager.addGpsStatusListener(this);	//register this class with the location listener
			gpsHandlerThread.start();
		}
		catch (Exception e)
		{
			MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "GpsManagerOld", "Exception", e);
		}
		catch (Error e)
		{
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "addListenerToCollection", "Error: " + e.toString());
		}
	}
	
	/*
	 * End constructor(s)
	 * =================================================================
	 * Start public methods
	 */

	/**
	 * 
	 */
	public Location getLastKnownLocation() {
		return locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}
	
	/*
	 * Detect if GPS is enabled on the device
	 */
	public boolean isLocationEnabled ()
	{
		return locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	/*
	 * Detect if GPS is enabled on the device
	 */
	public boolean isGpsRunning ()
	{
		if (listeners.isEmpty())
			return false;
		return true;
	}
	
	/**
	 * Turns the gps off and cancels the <code>Timer</code>s used by this class.
	 */
	public void stopGps() {
		locManager.removeUpdates(this);
		locManager.removeGpsStatusListener(this);
		gpsTimer.cancel();
		gpsTimeout = 0;
		//gpsHandlerThread.quit ();

		//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "stopGps", "mmc gps listener removed");
	}
	
	/*
	 * End public methods
	 * =================================================================
	 * Start GpsStatus.Listener methods
	 */

	/**
	 * This method is automatically called when the status of the Gps changes. In this program
	 * we use it to detect the firstFix event that can be used to disable the firstFixTimeouts of 
	 * many listeners and start the operationTimeout for them.
	 */
	@Override
	public void onGpsStatusChanged(int event) {
		//update the local copy of gpsStatus
		if(gpsStatus != null) {
			synchronized(gpsStatus) {
				locManager.getGpsStatus(gpsStatus);
			}
			
		}
		else {
			gpsStatus = locManager.getGpsStatus(null);
		}
		switch (event) {
		case GpsStatus.GPS_EVENT_FIRST_FIX:
			//iterate over the listeners collection and make firstFixReceived true
			Log.v(TAG, "First fix received");
			break;
		case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
			int numberOfSatellites = 0;
			int numberOfSatellitesUsedInFix = 0;
			
			synchronized(gpsStatus) {
				Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();
				for(GpsSatellite sat : satellites){
					if (sat.getSnr() > 10)
						numberOfSatellites++;
					if (sat.usedInFix())
						numberOfSatellitesUsedInFix++;
				}
			}
			if (numberOfSatellites != mNumberOfSatellites)
			{
				try{
					setNumberOfSatellites(numberOfSatellites);
					//if (listeners.size() > 0 && (!listeners.get(0).isFirstFixReceived() || numberOfSatellitesUsedInFix > 0))
					if (listeners != null && listeners.size() > 0 && (numberOfSatellitesUsedInFix > 0 || numberOfSatellites > 0))
								//Log.v(TAG, String.format("Gps status changed; numberOfSatellites:%d; numberOfSatellitesUsedInFix:%d", numberOfSatellites, numberOfSatellitesUsedInFix));
						owner.updateNumberOfSatellites(numberOfSatellites, numberOfSatellitesUsedInFix);
					
					Location lastloc = owner.getLastLocation ();
					if (lastloc == null || !lastloc.getProvider().equals(LocationManager.GPS_PROVIDER)) //  || lastloc.getTime() < System.currentTimeMillis() - 10000 )
					{
						for (int i=0; i<listeners.size(); i++) {
							GpsListener listener = listeners.get(i);
							if (listener.getProvider().equals(LocationManager.GPS_PROVIDER))
								listener.onLocationUpdate(null,0);
						}
					}
				}
				catch (Exception e) {}
			}
			break;
		case GpsStatus.GPS_EVENT_STARTED:
			owner.broadcastGpsStatus(true);
			Intent intent = new Intent(MMCIntentHandler.GPS_STATE_ON);
			owner.sendBroadcast(intent);
			break;
		case GpsStatus.GPS_EVENT_STOPPED:
			owner.updateNumberOfSatellites(0, 0); 
			owner.broadcastGpsStatus(false);
			Intent intent2 = new Intent(MMCIntentHandler.GPS_STATE_OFF);
			owner.sendBroadcast(intent2);
			break;
		}
	}
	
	/*
	 * End GpsStatus.Listener methods
	 * =================================================================
	 * Start LocationManager methods
	 */
	
	/**
	 * This method gets called whenever a new gps location is found.
	 * 
	 * It iterates over the various listeners and 
	 * <ul>
	 * 	<li>Ensures that the firstFixReceived boolean for all the listeners is true (in case the listener was added when the gps was already on).</li>
	 * 	<li>Calls the onLocationUpdate method of all the listeners.</li>
	 * </ul>
	 */
	@Override
	public void onLocationChanged(Location location) {
		synchronized(GpsManagerOld.this) 
		{
			//These are intents to get rid of direct use of GPSManager in Compare and Speedtest
//			if(provider != null && provider.equals(LocationManager.GPS_PROVIDER)) {
//				Intent intent = new Intent((MMCIntentHandlerOld.ACTION_GPS_LOCATION_UPDATE));
//				intent.putExtra("location", location);
//				owner.sendBroadcast(intent);
//			}
//			else if(provider != null && provider.equals(LocationManager.NETWORK_PROVIDER)) {
//				Intent intent = new Intent((MMCIntentHandlerOld.ACTION_NETWORK_LOCATION_UPDATE));
//				intent.putExtra("location", location);
//				owner.sendBroadcast(intent);
//			}
			
			Log.v(TAG, String.format(Locale.US,
				"transmiting location (%f, %f, %f) to listeners[%s]", 
				location.getLatitude(), 
				location.getLongitude(), 
				location.getAccuracy(), 
				listeners
			));
			owner.getIntentDispatcher().updateLocation(location);
			owner.processNewFilteredLocation(location, mNumberOfSatellites);
			for (int i=0; i<listeners.size(); i++) {
				GpsListener listener = listeners.get(i);
				//listener.setFirstFixReceived(true);
				
				boolean isAnotherLocationUpdateNeeded = listener.onLocationUpdate(location, mNumberOfSatellites);
				
				// If location is accurate enough, consider this the first fix - this listener has new timeout
				if (location.getAccuracy() < GpsListener.LOCATION_UPDATE_MIN_TREND_ACCURACY && listener.getOperationTimeout() > 0){
					if (!listener.isFirstFixReceived() && owner.isServiceRunning()) // cant use timer if service stopped and cancelled timer
					{
						listener.setFirstFixReceived(true);
						owner.getReportManager().fillTopCarriersStats (location.getLatitude(), location.getLongitude(), 0, false);
						
						//OperationTimeoutTimerTask op_task = new OperationTimeoutTimerTask(listener);
						//gpsTimer.schedule(op_task, listener.getOperationTimeout());
					}
				}
				if (!isAnotherLocationUpdateNeeded){
					Log.v(TAG, "Unregistering listener " + listener + " on user request");
					//the listener doesn't want any more location updates and the listener should now be un-registered
					removeListenerFromCollection(listener);
				}
			}
		}		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	public void startupGpsFinished (GpsListener listener)
	{
		if (!listener.isFirstFixReceived())
		{
			/*
			int numberOfSatellites = 0;
			int strongSatellites = 0;
			if (gpsStatus != null) {
				synchronized(gpsStatus) {
					for (GpsSatellite sat : gpsStatus.getSatellites())	//count the number of satellites
					{
						if (sat.getSnr() > 12)
							numberOfSatellites++;
						if (sat.getSnr() > 24)
							strongSatellites++;
					}
				}
			}
			*/
			// In some cases, unresponsive GPS needs a cold start
			// if it thinks its seeing no satellites at all
			if (getNumberOfSatellites() < 1)  
				coldStart ("no satellites detected");
			// or if its seeing plenty of strong satellites but is not getting a fix
			//if (strongSatellites > 4 && numberOfSatellites > 6)
			//	coldStart ("no fix but " + strongSatellites + " strong satellites and " + numberOfSatellites + " total satellites");
		}
	}
	public void coldStart (String reason)
	{
		locManager.sendExtraCommand(LocationManager.GPS_PROVIDER, "delete_aiding_data", null);
		if (reason.equals("triggered by user"))
		{
			String msg = owner.getString(R.string.GenericText_ColdStarted);
			Toast toast = Toast.makeText(owner, msg, Toast.LENGTH_LONG);
			toast.show();
			
			MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "coldStart", reason);
			owner.getEventManager().triggerUpdateEvent(false, false);
		}
	}
	/*
	 * End LocationManager methods
	 * ===================================================================
	 * Start miscellaneous public classes
	 */
	
	/**
	 * This is the timertask that handles the timeout action caused by not getting a first fix
	 * within the required period of time.
	 * @author Abhin
	 *
	 */
	class FirstFixTimeoutTimerTask extends TimerTask {
		private GpsListener listener;
		
		public FirstFixTimeoutTimerTask(GpsListener listener){
			this.listener = listener;
		}
		
		
		/**
		 * This timer task gets called when the firstFixTimeout period of a listener expires. In such cases,
		 * check if the particular listener has received a first fix. If not, then depending on the value
		 * of firstFixRenewalAllowed, either call the onTimeout method of the listener (if the renewal is allowed)
		 * or remove the listener from the listeners collection of the GpsManager. 
		 */
		
		@Override
		public void run() {
			synchronized(GpsManagerOld.this) {
				boolean bLog = listener.getProvider().equals(LocationManager.GPS_PROVIDER);
                bLog = false;
				if (!listener.isFirstFixReceived()){
					if (listener.isFirstFixRenewalAllowed()){
						//in this case, renew the first fix timeout by scheduling another timerTask with the timer
						
						//Log.v(TAG, "Attempting to renew firstFixTimeout with " + mNumberOfSatellites + " satellites");
						if (bLog) MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "FirstFixTimeoutTimerTask", "Attempting to renew firstFixTimeout with " + getNumberOfSatellites() + " satellites. listener: " + listener.hashCode());
						
						listener.setFirstFixTimeout(listener.attemptToRenewFirstFixTimeout(getNumberOfSatellites()));	//call the onTimeout method as promised
						listener.setFirstFixRenewalAllowed(false);	//make sure that this timer renewal is the last time it happens with this listener
						if (listener.getFirstFixTimeout() > 0 && owner.isServiceRunning()){	//if the onTimeout method returns lesser than zero, the renewal doesn't happen
							//Log.v(TAG, "Request to renew firstFixTimeout accepted; extending timeout by " + Integer.toString(listener.getFirstFixTimeout()) + " milliseconds");
							if (bLog) 
								MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "FirstFixTimeoutTimerTask", "Request to renew firstFixTimeout accepted; extending timeout by " + Integer.toString(listener.getFirstFixTimeout()) + " milliseconds");
							
							gpsTimer.schedule(new FirstFixTimeoutTimerTask(listener), listener.getFirstFixTimeout());
							gpsTimeout = System.currentTimeMillis() + listener.getFirstFixTimeout();
						} else {
							if (bLog) MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "FirstFixTimeoutTimerTask", "Request to renew firstFixTimeout declined for listener " + listener.hashCode());
							
							if (bLog) Log.v(TAG, "Request to renew firstFixTimeout declined for listener " + listener);
							//in this case, the user doesn't want to use the gps anymore. Therefore, the listener has to be
							//un-registered in order to turn the gps off.
							removeListenerFromCollection(listener);
							
							//signal the staging of the event at this point using the onTimeout method
							listener.onTimeout();
						}
					} else {
						if (bLog) 
							MMCLogger.logToFile(MMCLogger.Level.DEBUG,TAG, "FirstFixTimeoutTimerTask", "Could not get Location even after a second attempt; Giving up on listener " + listener.hashCode());
						//in this case, the first fix hasn't been received yet and renewal is not allowed.
						//therefore, this listener has to be unregistered.
						removeListenerFromCollection(listener);
						
						//signal the staging of the event at this point
						listener.onTimeout();
					}
				}
				else
				{
					if (bLog) MMCLogger.logToFile(MMCLogger.Level.DEBUG,TAG, "FirstFixTimeoutTimerTask", "First fix obtained, not a timeout on " + listener.hashCode());
				}
			}
		}
		
	}
	
	/**
	 * This is the timertask that handles the timeout action caused by the end of gps operation. 
	 * @author Abhin
	 *
	 */
	
	class OperationTimeoutTimerTask2 extends TimerTask {
		private GpsListener listener;
		
		public OperationTimeoutTimerTask2(GpsListener listener){
			this.listener = listener;
		}
		/**
		 * This timer task gets called when the operationTimeout period of the listener expires. This timeout is a 
		 * fail-safe mechanism to make sure programs that continuously return true from the onLocationUpdate method
		 * don't kill the phone's battery.
		 * When this method is called, the listener needs to be unregistered from the collection of listeners.
		 * It is possible that when this method is called, the listener will already be unregistered.
		 */
		@Override
		public void run() {
			synchronized(GpsManagerOld.this) {
				//Log.v(TAG, "operation timeout for listener [" + listener + "] occurred");
				//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "OperationTimeoutTimerTask", "operation timeout for listener [" + listener + "] occurred");
				
				removeListenerFromCollection(listener);
			}
		}
	}
	
	/*
	 * End miscellaneous public classes
	 * ===================================================================
	 * Start public methods
	 */
	
	public void safeguardGps ()
	{
		// Safeguard. if any event other than phone connect is still runnign after 10 minutes, kill by unstaging it
		synchronized(GpsManagerOld.this) {
			if (listeners.size() >= 1)
			{
				int i;
			
				for (i=0; i< listeners.size(); i++)
				{
					if (listeners.get(i) != null && listeners.get(i) instanceof EventManager.GpsListenerForEvent)
					{
						EventManager.GpsListenerForEvent evtlistener = (EventManager.GpsListenerForEvent)listeners.get(i);
						EventObj thisEvent = evtlistener.thisEvent;
				
						if (thisEvent.getEventTimestamp() + 10*60000 < System.currentTimeMillis() && (thisEvent.getEventType() != EventType.EVT_CONNECT || !owner.getPhoneState().isCallConnected()))
						{
							MMCLogger.logToFile(MMCLogger.Level.ERROR, "GpsListenerForEvent", "KILLED event running longer than 10 minutes", "event=" + thisEvent.getEventType() );
							owner.getEventManager().unstageEvent(thisEvent);
						}
					}
				}
				if (listeners.isEmpty())
					owner.goIdle ();
			}
		}
		
	}
	
	/**
	 * This method registers a listener. The process of registration automatically starts the gps
	 * if need be.
	 * @param listener
	 */
	public void registerListener(final GpsListener listener){
		synchronized(GpsManagerOld.this) {
			//add timer task for a first fix
			if (listener.getFirstFixTimeout() > 0 && owner.isServiceRunning())
			{
				gpsTimer.schedule(new FirstFixTimeoutTimerTask(listener), listener.getFirstFixTimeout());
				gpsTimeout = System.currentTimeMillis() + listener.getFirstFixTimeout();
			}
			Log.v(TAG, "registerListener");
			
			//add the listener to the collection in order to turn on the gps
			addListenerToCollection(listener);
		}
	}
	
	/**
	 * This method unregisters a given listener. It simply removes the listener from the {@link GpsManagerOld#listeners}
	 * collection. Unfortunately, this does not get rid of the timers that have callbacks scheduled. This means that
	 * the FirstFixTimeout and the operationTimeout should occur on the listener even though it's been removed
	 * from the {@link GpsManagerOld#listeners} collection. 
	 * This means that after removing a listener from the said collection, it would take some time before the listener
	 * gets garbage collected. This is not very harmful because the location updates and the gps status updates would not
	 * be executed on this listener (since it is not in the listeners collection anymore).
	 * @param listener
	 */
	public void unregisterListener(GpsListener listener){
		synchronized(GpsManagerOld.this) {
			Log.v(TAG, "unregisterListener");
			removeListenerFromCollection(listener);
		}
	}
	/*
	 * force the gps to stop when no events are running
	 */
	public void unregisterAllListeners(){
		synchronized(GpsManagerOld.this) {
			Log.v(TAG, "unregisterAllListeners");
			{
				if (listeners.size() >= 1)
				{
					//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "unregisterAllListeners", "stopped gps but " +listeners.size() + " listeners were still on, beginning with " + listeners.get(0).hashCode());
					listeners.clear();
				}
				//else
				//	MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "unregisterAllListeners", "all listeners were closed ");
			}
			// stop the gps
			locManager.removeUpdates(this);
			//gpsHandlerThread.quit ();
		}
	}
	
	/*
	 * End public methods
	 * ===================================================================
	 * Start private methods
	 */
	
	/**
	 * This method is used to add a listener to the {@link GpsManagerOld#listeners} collection.
	 * This method automatically starts the gps if needed(if the {@link GpsManagerOld#listeners}
	 * collection is not empty).
	 * @param listener The listener object that has to be added to the {@link GpsManagerOld#listeners} collection.
	 */
	private void addListenerToCollection(GpsListener listener){
		synchronized(GpsManagerOld.this) {
			//add the listener to the collection
			listeners.add(listener);
			
			if(MMCLogger.isDebuggable() && listener.getProvider().equals(LocationManager.GPS_PROVIDER)) {
				//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "addListenerToCollection", "added listener " + listener.hashCode());
			}
			try
			{
				//if the size of the collection is now 1, then the gps needs to be turned on
				if (listeners.size() == 1){
					locManager.removeUpdates(this);

					locManager.requestLocationUpdates(
							listener.getProvider(), 
						LOCATION_UPDATE_MIN_TIME, 
						LOCATION_UPDATE_MIN_DIST, 
						this, gpsHandlerThread.getLooper()
					);
					listener.gpsStarted ();
					if (false && listener.getProvider().equals(LocationManager.NETWORK_PROVIDER))
						MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "addListenerToCollection", "started " + listener.getProvider() + " location provider");
					else if (listener.getProvider().equals(LocationManager.GPS_PROVIDER))
						setNumberOfSatellites(0);
				}
			}
			catch (Exception e)
			{
				MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "addListenerToCollection", "GPS needs to start from main thread: ", e);
			}
			catch (Error e)
			{
				MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "addListenerToCollection", "Error: " + e.toString());
			}
		}
	}
	
	/**
	 * This method is used to remove the given listener from the {@link GpsManagerOld#listeners} collection.
	 * This method automatically stops the gps if needed (if the {@link GpsManagerOld#listeners} collection
	 * is empty).
	 * @param listener The listener object that has to be removed from the {@link GpsManagerOld#listeners} collection.
	 */
	private void removeListenerFromCollection(GpsListener listener){
		synchronized(GpsManagerOld.this) {
			//remove the listener from the collection
			if (listeners.isEmpty() || listener == null)
			{
				if (listeners.isEmpty() && (listener == null || listener.getProvider().equals(LocationManager.GPS_PROVIDER)))
					owner.goIdle ();
				return;
			}
			listeners.remove(listener);
			
			if(MMCLogger.isDebuggable() && listener.getProvider().equals(LocationManager.GPS_PROVIDER)) {
				//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "removeListenerFromCollection", "removed listener " + listener.hashCode());
			}
			
			//if the collection is now empty, then stop the gps
			if (listeners.isEmpty()){
				locManager.removeUpdates(this);
				//gpsHandlerThread.quit ();
				listener.gpsStopped ();
				if (listener.getProvider().equals(LocationManager.GPS_PROVIDER))
				{
					owner.goIdle ();
					//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "removeListenerFromCollection", "stopped listening for location updates " + listener.getProvider());
				}
			}
		}
	}

	public int getNumberOfSatellites() {
		return mNumberOfSatellites;
	}

	public void setNumberOfSatellites(int mNumberOfSatellites) {
		this.mNumberOfSatellites = mNumberOfSatellites;
	}
	
	/*
	 * End private methods
	 * ===================================================================
	 */

}

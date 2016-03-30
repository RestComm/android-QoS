package com.cortxt.app.mmcutility.Utils;

import android.location.Location;
import android.location.LocationManager;

/**
 * Created by bscheurman on 16-03-21.
 */
public class GpsListener {

    public static final float LOCATION_UPDATE_MIN_TREND_ACCURACY = 50.0f;
    public static final float LOCATION_UPDATE_MIN_SPEEDTEST_ACCURACY = 70.0f;
    public static final float LOCATION_UPDATE_MIN_EVENT_ACCURACY = 75.0f;
    public static final int DEFAULT_GPS_FIRST_FIX_TIMEOUT = 80000;	//in milliseconds
    //this is the maximum time the gps is allowed to stay on without a first fix
    public static final int DEFAULT_GPS_TIMEOUT = 30000;	//in milliseconds
    //This is the time the gps usually collects data for


    /**
     * This is the time in milliseconds that the gps is to be granted by
     * the party interested in a location for connecting to satellites and getting a first fix. If by
     * this time the gps is not able to get a first fix, then the GpsListener callback
     * method is called.
     */
    protected int firstFixTimeout;
    /**
     * This is the time in milliseconds that the gps will run after getting its first
     * fix. After this time, this GpsListener will be unregistered.
     */
    protected int operationTimeout;
    /**
     * This flag is used to determine if the Listener is allowed to renew its firstFixTimeout. This is
     * useful because after turning on the gps, the "first fix" event can happen multiple times (I know... weird).
     * This is because over a period of time, the gps turns off and on multiple times.
     */
    protected boolean firstFixRenewalAllowed = true;

    /**
     * Name used for debugging (when printing to log using toString).
     */
    protected String name = "";
    protected String provider = LocationManager.GPS_PROVIDER;

    public int getFirstFixTimeout() {
        return firstFixTimeout;
    }
    public void setFirstFixTimeout(int firstFixTimeout) {
        this.firstFixTimeout = firstFixTimeout;
    }
    public int getOperationTimeout() {
        return operationTimeout;
    }
    public void setOperationTimeout(int operationTimeout) {
        this.operationTimeout = operationTimeout;
    }
    public String getProvider() {
        return provider;
    }
    public void setProvider(String provider) {
        this.provider = provider;
    }
    public boolean isFirstFixRenewalAllowed() {
        return firstFixRenewalAllowed;
    }
    public void setFirstFixRenewalAllowed(boolean firstFixRenewalAllowed) {
        this.firstFixRenewalAllowed = firstFixRenewalAllowed;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public GpsListener(int firstFixTimeout, int operationTimeout, String name){
        this.firstFixTimeout = firstFixTimeout;
        this.operationTimeout = operationTimeout;
        this.name = name;
    }
    /**
     * Default constructor. Calls the parameterized constructor
     * with the default values
     */
    public GpsListener(){
        this(DEFAULT_GPS_FIRST_FIX_TIMEOUT, DEFAULT_GPS_TIMEOUT, "");
    }
    public GpsListener(String name){
        this(DEFAULT_GPS_FIRST_FIX_TIMEOUT, DEFAULT_GPS_TIMEOUT, name);
    }

    // This boolean is turned on when the listener receives its first location
    // Declared up here so Event object can access
    private boolean firstFixReceived = false;
    private Location lastLocation = null;

    public boolean isFirstFixReceived() {
        return firstFixReceived;
    }
    public void setFirstFixReceived(boolean firstFixReceived) {
        this.firstFixReceived = firstFixReceived;
    }
    public Location getLastLocation ()
    {
        return lastLocation;
    }

    public void setLastLocation (Location location)
    {
        lastLocation = location;
    }

    /**
     * This method is called by the GpsManager everytime a new gps location
     * comes around.
     * @param location The location received by the gps
     * @return True if the caller wants the gps to stay on for longer; false otherwise.
     */
    public boolean onLocationUpdate(Location location, int satellites) {
        if (location != null)
            MMCLogger.logToFile(MMCLogger.Level.DEBUG, "GpsListener", "onLocationUpdate", "lat=" + location.getLatitude() + ", lng=" + location.getLongitude() + ", acc=" + location.getAccuracy() + ", type=" + getProvider());

        return false;
    }

    /*
     * overridden by GpsListenerForEvent to start wakelock and clear previous location
     */
    public void gpsStarted ()
    {
    }
    /*
     * overridden by GpsListenerForEvent to release wakelock
     */
    public void gpsStopped ()
    {
    }
    /**
     * This method is called by the GpsManager when it finally gives up on getting a location fix from the gps.
     * <b>Note: This callback method will be called from a timer task. Therefore, it is higly recommended that no timeconsuming
     * operations be performed inside this callback. In case such a method is necessary, please perform it in a separate thread
     * or an async task.</b>
     */
    public void onTimeout(){

    }

    /**
     * This method is called when the original timeout supplied to the GpsManager via this GpsListenerOld
     * expires. This method gives the calling party a chance to evaluate whether they want to give the gps some more time
     * (and if so, then how much).
     * <b>Note: This callback method will be called from a timer task. Therefore, it is higly recommended that no timeconsuming
     * operations be performed inside this callback. In case such a method is necessary, please perform it in a separate thread
     * or an async task.</b>
     * @param numberOfSatellites The number of satellites that the gps is currently connected to. This is a useful parameter
     * for checking whether the gps has to be given some more time. Other parameters that should be taken into consideration
     * are the "current battery level" and the "usage profile" the user is running.
     * @return If a positive number is returned, then the GpsManager renews the timeout to that many milliseconds. If
     * a zero or a negative number is returned, then the GpsManager unregisters this listener.
     */
    public int attemptToRenewFirstFixTimeout(int numberOfSatellites) {
        return 0;
    }

    public String toString(){
        return "(" + name + ") firstFixTimeout: " + Integer.toString(firstFixTimeout) + " operationTimeout: " + Integer.toString(operationTimeout);
    }
}

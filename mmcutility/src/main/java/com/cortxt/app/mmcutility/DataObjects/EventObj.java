package com.cortxt.app.mmcutility.DataObjects;

import android.location.Location;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.telephony.ServiceState;

import com.cortxt.app.mmcutility.Utils.GpsListener;

import org.json.JSONObject;

/**
 * This class represents an event that can occur in the MMC Service class. 
 * The class comes with its own duration and timestamp variables.
 * @author abhin
 *
 */
public class EventObj {
	/*
	 * Private variables
	 */
	private EventType eventType;
	//private Time eventTimestamp;
	private long eventTimestamp;
	protected long duration, eventIndex, extraLong;
	protected int connectTime, numSatellites;
	protected int latency, downloadSpeed, uploadSpeed, downloadSize, uploadSize;
	private String cause = "";  // cause of a dropped call
	private String app = null;
	private long stageTimestamp;	//when the event gets staged
	private long eventid = 0;
	private boolean isUploaded;	//whether the event has been uploaded to the server
	private Uri uri;	//This is the uri of this event in the sqlite table
	private int localId = 0;
	private int flags = 0, battery = 0;
	private int tier = 0;
	private int signalFieldsMask = 0;
	private int band = 0;
	private long tx = 0,rx = 0;
	private Location location = null;
	private MMCSignal signal = null;
	private MMCCellLocation cell = null;
	private ServiceState service = null;
	private WifiInfo wifiinfo = null;
	private WifiConfiguration wificonfig = null;
    private JSONObject activeTest = null;
	public GpsListener gpsListener = null;
	public boolean isCheckin = false; // is the event a 3hr update
	private Runnable onUnstage;	//this callback will be called when the event gets unstaged
								//This callback is very important because if the event gets unstaged unexpectedly, 
								//it needs to unregister its listener with the GpsManager to avoid data being
								//written to the database twice.
	
	 //Used for APP_MONITORING event
	private String appData = null; 
	
	private int lookupid1 = 0;
	private int lookupid2 = 0;
    
	public static final int SERVICE_VOICE = 1;  // If 
	public static final int SERVICE_DATA = 2;
	public static final int SERVICE_3G = 4;
	public static final int SERVICE_4G = 8;
	public static final int SERVICE_WIFI = 16;
	public static final int SERVICE_UMA = 32;
	public static final int SERVER_READY = 64;
    public static final int SERVER_SENT = 128;
    //public static final int SERVER_CANCELLED = 256;
    public static final int SERVER_ERROR = 512;
    //public static final int SERVER_SENDING = 1024;
    //public static final int SERVER_FAILED = 2048;
    public static final int AUTOMATED = 4096; //1;
    public static final int RECORDING = 8192; // 2;
    public static final int PHONE_INUSE = 16384;
    public static final int SERVER_NODATA = 32768;
    public static final int SERVICE_WIMAX = 65536;
    public static final int TRANSIT_SAMPLES = (1<<23);
    public static final int MANUAL_SAMPLES = (1<<24);
    public static final int CALL_LOGCAT = (1<<25);
    public static final int CALL_PRECISE = (1<<26);
    public static final int CALL_INCOMING = 256;
    //public static final int CALL_PROXIMITY = 2048;  // Call ended with proximity sensor against face, which could indicate call was dropped
    //public static final int CALL_RECORDING = 131072;
	public static final int CALL_VOLTE = 2048;
	public static final int CALL_OFFNET = 1024;  // Call was not on the cell network, like wifi
    
    public static final int SIG_SIGNAL = 0;
    public static final int SIG_ECI0 = 1;
    public static final int SIG_SNR = 2; 
    public static final int SIG_BER = 3; 
    public static final int SIG_RSCP = 4; 
    public static final int SIG_SIGNAL2G = 5;
    public static final int SIG_LTE = 6; 
    public static final int SIG_LTERSRP = 7;
    public static final int SIG_LTERSRQ = 8;
    public static final int SIG_LTESNR = 9;
    public static final int SIG_LTECQI = 10;
    public static final int SIG_SIGNALBARS = 11;
    public static final int SIG_ECN0 = 12;
    public static final int SIG_WIFISIG = 13;
    public static final int SIG_COVERAGE = 14;
     /*
	 * Constructors
	 */
	public EventObj(EventType eventType, long eventTimestamp, long duration, Uri uri, Runnable onUnstage)  {
		this.eventType = eventType;
		this.eventTimestamp = eventTimestamp;
		this.duration = duration;
		this.uri = uri;
		this.stageTimestamp = -1L;
		this.onUnstage = onUnstage;
		this.isUploaded = false;
		this.rx = TrafficStats.getTotalRxBytes();
		this.tx = TrafficStats.getTotalTxBytes();
	}
		
	public EventObj(EventType eventType, long eventTimestamp, long duration) {
		this(eventType, eventTimestamp, duration, null, null);
	}
	public EventObj(EventType eventType, long duration){
		this(eventType, System.currentTimeMillis(), duration);
		setEventTimestampToNow();
	}
	public EventObj(EventType eventType, Uri uri){
		this(eventType, 0);
		this.uri = uri;
	}
	public EventObj(EventType eventType, Uri uri, long timestamp){
		this(eventType, timestamp, 0);
		this.uri = uri;
	}
	public EventObj(EventType eventType){
		this(eventType, 0);
	}
	
	public EventObj(String newAppData) {
		setAppData(newAppData);
	}
	
	/*
	 * Getters/Setters
	 */
	
	public String getAppData() {
		return appData;
	}
	public void setAppData(String newAppData) {
		this.appData = newAppData;
	}

	public int getLookupid1() {
		return lookupid1;
	}
	public void setLookupid1(int lookupid1) {
		this.lookupid1 = lookupid1;
	}

	public int getLookupid2() {
		return lookupid2;
	}
	
	public void setLookupid2(int lookupid2) {
		this.lookupid2 = lookupid2;
	}
		
	public EventType getEventType() {
		return eventType;
	}
	public int getIEventType() {
		return eventType.getIntValue();
	}
	
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}
	public void setEventType(int evtType) {
		EventType eventType = EventType.get(evtType);
		this.eventType = eventType;
	}
	public int getLocalID() {
		return localId;
	}
	public void setLocalID(int id) {
		this.localId = id;
	}
	public long getEventTimestamp() {
		return eventTimestamp;
	}
	public void setEventTimestamp(long eventTimestamp) {
		this.eventTimestamp = eventTimestamp;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public void setBuildingID (long id)
	{
		this.extraLong = id;
	}
	public long getBuildingID ()
	{
		return this.extraLong;
	}
	public int getConnectTime() {
		return connectTime;
	}
	public void setConnectTime (int connect) {
		this.connectTime = connect;
	}
	public int getSatellites() {
		return numSatellites;
	}
	public void setSatellites (int sats) {
		this.numSatellites = sats;
	}
	public long getEventIndex() {
		return eventIndex;
	}
	public void setEventIndex(long val) {
		this.eventIndex = val;
	}
	public int getBattery() { return battery; }
	public void setBattery (int battery) { this.battery = battery; }
	public String getCause() {
		return cause;
	}
	public void setCause(String _cause) {
		this.cause = _cause;
	}
	public String getAppName() { return app; }
	public void setAppName(String _app) {
		this.app = _app;
	}
	public long getRX () { return this.rx; }
	public long getTX ()
	{
		return this.tx;
	}
	
	public WifiInfo getWifiInfo() {
		return wifiinfo;
	}
	public WifiConfiguration getWifiConfig() {
		return wificonfig;
	}
	public void setWifi(WifiInfo wifiInfo, WifiConfiguration wifiConfig) {
		this.wifiinfo = wifiInfo;
		this.wificonfig = wifiConfig;
	}
	
	public boolean isUploaded() {
		return isUploaded;
	}
	/**
	 * Sets the flag indicating whether the event has been uploaded to the server yet.
	 * @param isUploaded
	 */
	public void setUploaded(boolean isUploaded) {
		this.isUploaded = isUploaded;
	}
	public Uri getUri() {
		return uri;
	}
	public void setUri(Uri uri) {
		this.uri = uri;
	}
	public long getStageTimestamp() {
		return stageTimestamp;
	}
	public void setStageTimestamp(long stageTimestamp) {
		this.stageTimestamp = stageTimestamp;
	}
	public Runnable getOnUnstage() {
		return onUnstage;
	}
	public void setOnUnstage(Runnable onUnstage) {
		this.onUnstage = onUnstage;
	}
	public void setServiceState (ServiceState serviceState)
	{
		service = serviceState;
	}
	public ServiceState getServiceState ()
	{
		return service;
	}
	public void setHasSignal (int i)
	{
		signalFieldsMask |= (1<<i);
	}
	public boolean hasSignal (int i)
	{
		if ((signalFieldsMask & (1<<i)) > 0)
			return true;
		else
			return false;
	}
	public void setTier (int t)
	{
		tier = t;
	}
	public int getTier ()
	{
		return tier;
	}
	public void setFlag (int flag, boolean bSet)
	{
		if (bSet == false)
            flags = flags & (~flag);
        else if (bSet == true) 
            flags = flags | (flag);
	}
	public int getFlags ()
	{
		return flags;
	}
	public void setEventId (long id)
	{
		eventid = id;
	}
	public long getEventID ()
	{
		return eventid;
	}
	/*
	 * Other public methods
	 */
	/**
	 * Sets the timestamp of the event to the current system time.
	 */
	public void setEventTimestampToNow(){
		this.eventTimestamp = System.currentTimeMillis(); // .setToNow();
	}
	public void setStageTimestampToNow(){
		this.stageTimestamp = System.currentTimeMillis();
	}
	public void setSpeedResult(int _latency, int _downloadSpeed, int _uploadSpeed, int _downloadSize, int _uploadSize) {
		this.latency = _latency;
		this.downloadSpeed = _downloadSpeed/8;
		this.uploadSpeed = _uploadSpeed/8;
		this.downloadSize = _downloadSize;
		this.uploadSize = _uploadSize;
		this.duration = 1;
	}
	
	public void setVideoResult(int playDelay, int accessDelay, int videoBytes, int _stalls, int _stallTime, String videoUrl, int videoHeight, int downloadDuration, int bufferPercent, int error, JSONObject resolutions) {
		this.latency = playDelay;
		//this.downloadSpeed = _stalls; // number of stalls ends up in the event index field (via downloadSpeed)
		if (((downloadDuration+accessDelay) / 1000) > 0)  // careful with div by zero
			downloadSpeed = videoBytes*1000 / ((downloadDuration+accessDelay) / 1000);
		this.uploadSpeed = _stalls;
		this.downloadSize = videoBytes;
		this.uploadSize = videoHeight;
		//this.eventIndex = _stalls;
		this.cause = videoUrl;
		//this.duration = _stallTime;
		//this.lookupid2 = downloadDuration;
        this.lookupid1 = bufferPercent;
		//this.extraLong = videoId;

        try {
            activeTest = new JSONObject();
            activeTest.put("play_delay", playDelay);
            activeTest.put("access_delay", accessDelay);
            activeTest.put("rxbytes", videoBytes);
            activeTest.put("stalls", _stalls);
            activeTest.put("stall_time", _stallTime);
            activeTest.put("duration", downloadDuration);
            activeTest.put("progress", bufferPercent);
            activeTest.put("vid_height", videoHeight);
            activeTest.put("error", error);
			activeTest.put("resolutions", resolutions);
            this.duration = downloadDuration;
            this.cause = videoUrl;
            this.eventIndex = _stalls;
        }
        catch (Exception e)
        {

        }
	}

    public void setSMSResult(int avgDuration, int deliveryTime, JSONObject smsTest, int num_tests ) {
		this.latency = deliveryTime;
        activeTest = smsTest;
        this.duration = avgDuration;
    }

    public String getJSONResult ()
    {
        if (activeTest != null)
        {
            return activeTest.toString();
        }
        return null;
    }
	
	public void setLatency(int latency) {
		this.latency = latency;
	}
	
	public int getLatency() {
		return latency;
	}
	
	public void setDownloadSpeed(int downloadSpeed) {
		this.downloadSpeed = downloadSpeed;
	}
	
	public int getDownloadSpeed ()
	{
		return downloadSpeed;
	}
	public int getUploadSpeed ()
	{
		return uploadSpeed;
	}
	public int getDownloadSize ()
	{
		return downloadSize;
	}
	public int getUploadSize ()
	{
		return uploadSize;
	}
	public MMCSignal getSignal() {
		return signal;
	}
	public void setSignal(MMCSignal signal) {
		this.signal = signal;
	}
	public MMCCellLocation getCell() {
		return cell;
	}
	public void setCell(MMCCellLocation cell) {
		this.cell = cell;
	}
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location, int satellites) {
		this.location = location;
		this.numSatellites = satellites;
	}
	public int getTierFromFlags ()
	{
		if ((flags&15)==15)
			return 5;
		else if ((flags&7) == 7)
			return 3;
		else if ((flags&3) == 3)
			return 2;
		else if ((flags&3) > 0)
			return 1;
		return 0;
	}

	public boolean isGPSFinished ()
	{
		if (gpsListener == null)
			return true;
		return gpsListener.isFirstFixReceived();
	}
	public Location getLastLocation ()
	{
		if (gpsListener == null)
			return null;
		return gpsListener.getLastLocation();
	}
}

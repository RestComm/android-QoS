package com.cortxt.app.mmcutility.Reporters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.cortxt.app.mmcutility.ContentProvider.Provider;
import com.cortxt.app.mmcutility.DataObjects.ConnectionHistory;
import com.cortxt.app.mmcutility.DataObjects.EventData;
import com.cortxt.app.mmcutility.DataObjects.PhoneState;
import com.cortxt.app.mmcutility.R;
import com.cortxt.app.mmcutility.Utils.CommonIntentActionsOld;
import com.cortxt.app.mmcutility.Utils.Global;
import com.cortxt.app.mmcutility.Utils.MMCException;
import com.cortxt.app.mmcutility.ICallbacks;
import com.cortxt.app.mmcutility.Reporters.LocalStorageReporter.LocalStorageReporter;
import com.cortxt.app.mmcutility.Reporters.WebReporter.ServerUpdateRequest;
import com.cortxt.app.mmcutility.Reporters.WebReporter.WebReporter;
import com.cortxt.app.mmcutility.DataObjects.EventObj;
import com.cortxt.app.mmcutility.DataObjects.MMCCDMADevice;
import com.cortxt.app.mmcutility.DataObjects.MMCDevice;
import com.cortxt.app.mmcutility.DataObjects.MMCGSMDevice;
import com.cortxt.app.mmcutility.DataObjects.Carrier;
import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.Utils.PreferenceKeys;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
//import com.google.android.maps.GeoPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
/**
 * ReportManager is in charge of handling all types of report and data retrieval mechanisms,
 * it will delegate particular task to concrete reporters so the report and retrieval of the data
 * gets accomplished.
 *
 * It is a singleton class since there will only be one reportmanager per  application
 * @author brad
 *
 */
public class ReportManager {
    private static ReportManager mInstance = null;
    private static Object mInstanceLock = new Object();

    private Context mContext;
    private WebReporter mWebReporter;
    private ICallbacks mmcService;
    private LocalStorageReporter mLocalStorageReporter;
    private Provider mSQLProvider;
    //private PhoneState mPhoneState;
    private ConnectionHistory mConnectionHistory;

    //meta-data variables
    public static final String TAG = ReportManager.class.getSimpleName();

    /**
     * This class holds the keys for maps returned by the stats functions.
     * @author nasrullah
     *
     */
    public Provider getDBProvider() {

        return mSQLProvider;
    }

    public static class StatsKeys {
        public static final String DROPPED_CALLS = "droppedCalls";
        public static final String FAILED_CALLS = "failedCalls";
        public static final String NORMALLY_ENDED_CALLS = "normallyEndedCalls";

        public static final String DOWNLOAD_SPEED_AVERAGE = "downSpeed";
        public static final String UPLOAD_SPEED_AVERAGE = "upSpeed";
        public static final String LATENCY_AVERAGE = "latency";

        public static final String COVERAGE_SERVICE = "covService";
        public static final String COVERAGE_3G = "cov3G";

        public static final String OPERATOR_NAME = "operator";
        public static final String SAMPLES = "samples";
        public static final String LOGO = "logo";
    }

    public static class EventKeys {
        public static final String TIMESTAMP = "timeStamp";
        public static final String ID = "id";
        public static final String TYPE = "eventType";
        public static final String NETTYPE = "netType";
        public static final String RATING = "rating";
        public static final String LATITUDE = "lat";
        public static final String LONGITUDE = "long";
        public static final String OPERATOR_ID = "opId";
    }

    public static class SpeedTestKeys {
        public static final String TIMESTAMP = "timeStamp";
        public static final String ID = "id";
        public static final String LATITUDE = "lat";
        public static final String LONGITUDE = "long";
        public static final String DOWNLOAD_SPEED = "downSpeed";
        public static final String UPLOAD_SPEED = "upSpeed";
        public static final String LATENCY = "latency";
        public static final String SPEEDTIER = "speedTier";
    }

    public static ReportManager getInstance(Context context) {
        synchronized (mInstanceLock) {
            if(mInstance == null) {
                mInstance = new ReportManager(context.getApplicationContext());
            }
        }
        return mInstance;
    }

    private ReportManager(Context context) {
        mContext = context;
        mWebReporter = new WebReporter(mContext);
        mLocalStorageReporter = new LocalStorageReporter(mContext);
        mSQLProvider = new Provider(mContext);
    }
    public void setService (ICallbacks svc, PhoneState phoneState, ConnectionHistory connectionHistory)
    {
        mmcService = svc;
        //mPhoneState = phoneState;
        mConnectionHistory = connectionHistory;
        Global.setCallback(svc);
    }

    public void setVQHandler (Handler handler)
    {
        mmcService.setVQHandler(handler);
    }

    public void stop() {
        synchronized (mInstanceLock) {
            mWebReporter.stop();
            mLocalStorageReporter.stop();
            mInstance = null;
        }
    }
    /*
     * Attempt to register a user and his device with Linux server
     * If bFailover is true, then try windows server if it fails with linux
     */
    public void authorizeDevice(String email, boolean bFailover) throws MMCException {
        MMCDevice device = getDevice();
        if (email == null)
            email = PreferenceKeys.getSecurePreferenceString(PreferenceKeys.User.USER_EMAIL, null, mContext);
        mWebReporter.authorizeDevice(device, email, bFailover);
        //mLocalStorageReporter.authorizeDevice(device, email, bFailover);
    }

    public boolean isAuthorized() {
        return mWebReporter.isAuthorized();
    }

    public void setAuthorized(String apiKey) {
        mWebReporter.setAuthorized(apiKey);
    }
    public void unAuthorizeDevice(MMCDevice device, String email) throws MMCException{
        mWebReporter.unAuthorizeDevice(device, email);
        mLocalStorageReporter.clearEvents();
    }

    //public void reportEvent(EventData eventdata, boolean bLocal, boolean bServer) {
    //	if (bServer)
    //		mWebReporter.reportEvent(event);
    //	if (bLocal)
    //		mLocalStorageReporter.reportEvent(event);
    //}

    public int storeEvent(EventData event) {
        return mLocalStorageReporter.storeEvent(event);
    }
    public void updateEventField(int evtID, String field, String value ) {
        mLocalStorageReporter.updateEventDBField(evtID, field, value);
    }

    //public void reportSpeedTest(SpeedTestEvent speedTestEvent) {
    //	mWebReporter.reportSpeedTest(speedTestEvent);
    //	mLocalStorageReporter.reportSpeedTest(speedTestEvent);
    //}
	/*
	public void reportTroubleSpot(TroubleSpotData troubleSpot) {
		//mWebReporter.reportTroubleSpot(troubleSpot);
		//mLocalStorageReporter.reportTroubleSpot(troubleSpot);
		WSManagerOld wsManager;
		if (mmcService == null || mmcService.getWSManager() == null)
			wsManager = new WSManagerOld(mContext);
		else
			wsManager = mmcService.getWSManager();
		EventData event = wsManager.reportTroubleSpot(troubleSpot);
		mLocalStorageReporter.storeEvent( event);
	}
	*/

    public void reportTroubleTweet(EventType eventType, int impactLevel, String comment, Location location) {
        // Create an Event out of the TroubleTweet data provided by the user
        if (mmcService == null)
            return;
        EventObj evt = mmcService.triggerSingletonEvent (eventType);
        // TroubleTweet event is triggered and uploads right away without GPS

        evt.setLocation(location, 0);
        evt.setCause(comment);
        evt.setEventIndex(impactLevel); // this ends up being sent to the server as 'eventIndex' which is a way of saying the severity level
        mmcService.temporarilyStageEvent(evt, null, null);
    }

    public void reportSimChange(MMCGSMDevice device) {
        mWebReporter.reportSimChange(device);
    }

    public boolean reportSettingChange(final String type, final String key, final Object value) {

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            HashMap<String, String> carrier =  getDevice().getCarrierProperties();
                            mWebReporter.reportSettingChange(type, key, value, carrier);
                        }
                        catch (Exception e){
                            MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "reportSettingChange", "error in fillTopCarriersStats ", e);

                        }
                        finally
                        {
                            loadingStats = false;
                        }
                    }
                }
        ).start();

        return true;
    }

    // Allows the Settings screen to tell the server to change the behavior of the notification icon
    // The icon can be shown always, or just when Active
    public void setIconBehavior ()
    {
        if (mmcService != null)
            mmcService.setIconBehavior();
    }

    public Cursor getRecentEvents(long timespan) {
        return mLocalStorageReporter.getRecentEvents(timespan);
    }
    public List<HashMap<String, String>> getEvents(HashSet<Integer> eventTypes) {
        return mLocalStorageReporter.getEvents(eventTypes);
    }
    public int getEventId(long timestamp, int eventType) {
        return mLocalStorageReporter.getEventId(timestamp/1000*1000, eventType);
    }
    public void deleteEvent (int eventId) {
        mLocalStorageReporter.deleteEvent (eventId);
    }
    public HashMap<String, String> getEventDetails(int eventId) {
        return mLocalStorageReporter.getEventDetails(eventId);
    }

    public List<HashMap<String, Long>> getSpeedTestResults(int eventtype, long startTime, long endTime) {
        return mLocalStorageReporter.getSpeedTestResults(eventtype, startTime, endTime);
    }

    public List<HashMap<String, Long>> getThroughputResults(long startTime, long endTime) {
        return mLocalStorageReporter.getThroughputResults(startTime, endTime);
    }

    public HashMap<String, Integer> getYourSpeedTestAverage(long startTime, long endTime, int speedTier) {
        return mLocalStorageReporter.getSpeedTestAverage(startTime, endTime, speedTier);
    }

    public HashMap<String, Integer> getYourCallStats(long startTime, long endTime) {
        return mLocalStorageReporter.getCallStats(startTime, endTime);
    }

    public JSONObject getTopCarriersStats(List<Carrier> operators, int months,
                                          double latitude, double longitude, float radius, int speedTier, boolean bReload) throws MMCException
    {
        //WSManagerOld wsManager;

        //if (mmcService == null || mmcService.getWSManager() == null)
        //	wsManager = new WSManagerOld(mContext);
        //else
        //	wsManager = mmcService.getWSManager();

        if (months == 0)
            months = 2;//(mContext.getResources().getInteger(R.integer.STATS_MONTHS));

        int medianSamples = 0;

        String ops = "";
        if (operators == null)
        {
            if (getCurrentCarrier() != null)
                ops = getCurrentCarrier().OperatorId;
            radius = 6000.0f;
        }
        else
        {
            medianSamples = operators.get(operators.size() / 2).Samples;
            ops = "0,";
            for (int i=0; i<operators.size(); i++)
            {
                if (operators.get(i) != null)
                {
                    ops += operators.get(i).OperatorId;
                    if (i<operators.size()-1)
                        ops += ",";
                }
            }

            int iMonths = 0;
            if (radius == 0f)
            {
                if (medianSamples < 500)
                    radius = 12000.0f;
                else if (medianSamples < 10000)
                    radius = 8000.0f;
                else if (medianSamples < 70000)
                    radius = 5000.0f;
                else
                    radius = 3000.0f;
            }
        }

        // see if top operators is cached
        if (topstats == null && !bReload)
        {
            SharedPreferences preferenceSettings = PreferenceKeys.getSecurePreferences(mContext);
            if (preferenceSettings.contains(PreferenceKeys.Miscellaneous.TOPSTATS_RESPONSE)){
                try {
                    String strTopStatsResponse = preferenceSettings.getString(PreferenceKeys.Miscellaneous.TOPSTATS_RESPONSE, "");
                    String strLat = preferenceSettings.getString(PreferenceKeys.Miscellaneous.TOPSTATS_LAT, "");
                    String strLng = preferenceSettings.getString(PreferenceKeys.Miscellaneous.TOPSTATS_LNG, "");
                    if (strTopStatsResponse != null && strTopStatsResponse.length() > 10 && strLat != null && strLat.length() > 0 && strLng.length() > 0)
                    {
                        double lat = Double.parseDouble(strLat);
                        double lng = Double.parseDouble(strLng);
                        if ((Math.abs(latitude - lat) < 0.01 && Math.abs(longitude - lng) < 0.01))
                        {
                            topstats = new JSONObject(strTopStatsResponse);
                            if (bReload == false)
                            {
                                try {
                                    topstats.put("radius", String.valueOf(radius));
                                } catch (JSONException e) {}
                                return topstats;
                            }

                        }
                    }

                } catch (Exception e) {
                    MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "loadEventsQueue", "Exception loading events from storage", e);
                }
            }
        }
        if (bReload)
            try
            {
                topstats = mWebReporter.getAreaStats (latitude, longitude, (int)radius, months, ops);
                if (topstats != null)
                    adjustStatsForSpeedTier (topstats, speedTier);
            }
            catch (Exception e)
            {
                //topoperators = null;
                topstatfailed = true;
            }
        if (topstats != null)
            try {
                topstats.put("radius", String.valueOf(radius));
            } catch (JSONException e) {}

        //topstats = wsManager.getTopCarriersStats(operators, months, latitude, longitude, 0, speedTier);

        return topstats;
    }

    // normally called from background to pre-fill the carriers stats fro teh top 5 carriers in area
    public void fillTopCarriersStats (final double latitude, final double longitude, final int radius, final boolean bForce)
    {

        // most cases: do nothing if already loaded, unless forced
        if (((topoperators != null || topopfailed == true) && !bForce) || loadingStats == true)
            return;
        loadingStats = true;
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            int mcc = getMCCMNC()[0];
                            getTopOperators (latitude, longitude, 4800, mcc, 15, bForce);
                            if (topoperators != null)
                                getTopCarriersStats(topoperators, 0,
                                        latitude, longitude, radius, 0, bForce);
                        }
                        catch (Exception e){
                            MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "fillTopCarriersStats", "error in fillTopCarriersStats ", e);

                        }
                        finally
                        {
                            loadingStats = false;
                        }
                    }
                }
        ).start();

    }

    private void adjustStatsForSpeedTier (JSONObject json, int speedTier) throws MMCException
    {
        String speedType = "Wifi";
        if (speedTier < 3)
            speedType = "2G";
        else if (speedTier < 5)
            speedType = "3G";
        else if (speedTier == 5)
            speedType = "LTE";

        try
        {
            Iterator<String> keys = json.keys();

            while( keys.hasNext() ){
                String key = (String)keys.next();
                JSONObject stat = (JSONObject) json.get(key);
                if (stat.has("download"+speedType))
                {
                    stat.put(ReportManager.StatsKeys.UPLOAD_SPEED_AVERAGE, stat.get("upload"+speedType));
                    stat.put(ReportManager.StatsKeys.LATENCY_AVERAGE, stat.get("latency"+speedType));
                    stat.put(ReportManager.StatsKeys.DOWNLOAD_SPEED_AVERAGE, stat.get("download"+speedType));
                }
            }
        }
        catch (Exception e)
        {
            throw new MMCException (e);
        }
    }
    public JSONObject getDidYouKnow () throws MMCException
    {
        String opid = "";
        if (getCurrentCarrier() != null)
            opid = getCurrentCarrier().OperatorId;
        return mWebReporter.getDidYouKnow (opid);

    }

    public Long confirmEvent (long ltime, EventType evttype, EventType newtype, int rating, int userid) throws MMCException
    {
        if (mmcService.isOnline())
            return mWebReporter.confirmEvent (ltime, evttype.getIntValue(), newtype.getIntValue(), rating, userid);
        else
            return -1l;
    }

    private List<Carrier> topoperators;
    private JSONObject topstats;
    private Location topOpLocation;
    private boolean loadingStats = false, topopfailed = false, topstatfailed = false;
    public List<Carrier> getTopOperators (double latitude, double longitude, int radius, int mcc, int limit, boolean bReload)
    {
        // see if top operators is cached
        if (topoperators == null)
        {
            SharedPreferences preferenceSettings = PreferenceKeys.getSecurePreferences(mContext);
            if (preferenceSettings.contains(PreferenceKeys.Miscellaneous.TOPOP_RESPONSE) && !bReload){
                try {
                    String strTopopResponse = preferenceSettings.getString(PreferenceKeys.Miscellaneous.TOPOP_RESPONSE, "");
                    String strLat = preferenceSettings.getString(PreferenceKeys.Miscellaneous.TOPOP_LAT, "");
                    String strLng = preferenceSettings.getString(PreferenceKeys.Miscellaneous.TOPOP_LNG, "");
                    if (strTopopResponse != null && strTopopResponse.length() > 10 && strLat != null && strLat.length() > 0 && strLng.length() > 0)
                    {
                        double lat = Double.parseDouble(strLat);
                        double lng = Double.parseDouble(strLng);
                        if ((Math.abs(latitude - lat) < 2.0 && Math.abs(longitude - lng) < 2.0))
                        {
                            topoperators = new ArrayList<Carrier>();
                            JSONArray operators = new JSONObject(strTopopResponse).getJSONArray("operators");
                            for(int i=0; i<operators.length(); i++)
                            {
                                Carrier carrier = new Carrier(operators.getJSONObject(i));
                                carrier.loadLogo (mContext);
                                if (!carrier.Name.equals("null") && !carrier.Name.equals(""))
                                    topoperators.add(carrier);
                            }
                            return topoperators;
                        }
                    }

                } catch (JSONException e) {
                    MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "loadEventsQueue", "JSONException loading events from storage", e);
                }
                catch (Exception e) {
                    MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "loadEventsQueue", "Exception loading events from storage", e);
                }
            }
        }

        if (bReload)
            try
            {
                topoperators = mWebReporter.getTopOperators (latitude, longitude, radius, mcc, limit);
            }
            catch (Exception e)
            {
                //topoperators = null;
                topopfailed = true;
            }
        return topoperators;
    }

    public Location getLastKnownLocation ()
    {
        return mLocalStorageReporter.getLastKnownLocation ();
    }
    private Bitmap carrierLogo = null;
    private Carrier carrierCurr = null;
    HashMap<String, String> carrierProps = null;

    public Bitmap getCarrierLogo(HashMap<String, String> carrierHash) throws MMCException
    {
        //carrierLogo = mWebReporter.getCarrierLogo(carrier);
        carrierCurr = getCurrentCarrier (carrierHash);
        if (carrierCurr != null)
            return carrierCurr.Logo;
        return null;
    }

    static long lastCarrierRequest = 0;

    private Carrier getCurrentCarrier (HashMap<String, String> carrier) throws MMCException
    {
        if (carrier == null)
            carrier =  getDevice().getCarrierProperties();
        if (carrier == null)
            return null;
        // Try to locally maintain a current carrier logo to avoid the request to the server
        if (carrierCurr == null || carrierLogo == null || carrierLogo.isRecycled() || carrierProps == null || !carrierProps.equals(carrier))
        {
            // If carrier is unknown, it will have to return null for now to user thread, and worker thread will obtain it for next time
            final HashMap<String, String> carrierParams = carrier;
            if (lastCarrierRequest + 10000 < System.currentTimeMillis())
            {
                lastCarrierRequest = System.currentTimeMillis();
                new Thread(new Runnable()
                {
                    @Override
                    public void run() {
                        try {
                            carrierLogo = mWebReporter.getCarrierLogo(carrierParams);
                        } catch (MMCException e) {
                            MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "getCurrentCarrier.run", "Exception: ", e);
                            //e.printStackTrace();
                            //throw new RuntimeException(e);
                        }
                    }
                }).start();
            }
            carrierCurr = mWebReporter.getCurrentCarrier();
            //saveCurrentCarrierToStorage(carrierCurr);
            return carrierCurr;
        }
        return carrierCurr;
    }


    public Carrier getCurrentCarrier () {
        //carrierCurr = getCurrentCarrierFromStorage();
        //if (carrierCurr == null)
        try {
            carrierCurr = getCurrentCarrier (null);
        } catch (MMCException e) {
            e.printStackTrace();
        }
        return carrierCurr;
    }

    public String getTwitterHandle(HashMap<String, String> carrierHash) throws MMCException {
        //return mWebReporter.getTwitterHandle(carrierHash);
        carrierCurr = getCurrentCarrier (); // carrierHash);
        if (carrierCurr != null)
            return carrierCurr.Twitter;
        return null;
    }

    public JSONArray getServerObjects(String type, HashMap<String, String> query) throws MMCException {
        return mWebReporter.getServerObjects (type, query);
    }

    String lastNeighbors;

    public String getNeighbors () {
        if (mmcService != null)
            return mmcService.getNeighbors();
        else
            return null;
    }

    public void setNeighbors (String nbrs) {
        lastNeighbors = nbrs;
    }

    public void saveCurrentCarrierToStorage(Carrier carr) {
        String fileName = null;

        if ((Environment.getExternalStorageState().toString()).equals(Environment.MEDIA_MOUNTED)) //SD card
            fileName = Environment.getExternalStorageDirectory().toString() + "/mmccarrier.txt";
        else
            fileName = (mContext.getApplicationContext()).getCacheDir().toString()  + "/mmccarrier.txt"; //cache

        if(fileName != null) {
            FileOutputStream fos = null;
            ObjectOutputStream out = null;
            try {
                fos = new FileOutputStream(fileName);
                if (fos != null)	{
                    out = new ObjectOutputStream(fos);
                    out.writeObject(carr);
                    out.flush();
                    out.close();
                    fos.close();
                }
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Carrier getCurrentCarrierFromStorage() {
        String fileName = null;
        Carrier carr = null;

        if ((Environment.getExternalStorageState().toString()).equals(Environment.MEDIA_MOUNTED))
            fileName = Environment.getExternalStorageDirectory().toString() + "/mmccarrier.txt";
        else
            fileName = (mContext.getApplicationContext()).getCacheDir().toString()  + "/mmccarrier.txt";

        if(fileName != null) {
            FileInputStream fis = null;
            ObjectInputStream in = null;
            try {
                fis = new FileInputStream (fileName);
                if (fis != null)	{
                    in = new ObjectInputStream(fis);
                    carr = (Carrier) in.readObject();
                    in.close();
                    fis.close();
                }
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return carr;
    }

    private int[] lastknownMCCMNC = new int[2];
    /**
     * @return {@link MMCDevice} object used to get device properties
     * @throws RuntimeException if phone type is not GSM or CDMA
     */
    public MMCDevice getDevice() {
        int phoneType = ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE)).getPhoneType();
        if(phoneType == TelephonyManager.PHONE_TYPE_GSM) {
            return new MMCGSMDevice(mContext);
        }
        else if(phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
            return new MMCCDMADevice(mContext);
        }
        else {
            return new MMCGSMDevice(mContext);
            //throw new RuntimeException("Unsupported phone type");
        }
    }

    /**
     * If MCC and MNC are known, returns them, and updates {@link ReportManager#lastknownMCCMNC}.<br>
     * If MCC and MNC are unknown and the phone is not roaming, returns {@link ReportManager#lastknownMCCMNC}.<br>
     * If MCC and MNC are unknown and the phone is roaming, returns <code>{0, 0}</code>.
     * @return int[] where element 0 is MCC, element 1 is MNC
     */
    public int[] getMCCMNC() {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        //MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "getMCCMNC", "mcc+mnc " + telephonyManager.getNetworkOperator());

        try {
            if (telephonyManager.getNetworkOperator() == null || telephonyManager.getNetworkOperator().length() < 3)
            {
                //MMCLogger.logToFile(MMCLogger.Level.WTF, TAG, "getMCCMNC", "no mnc, just " + telephonyManager.getNetworkOperator());
                return lastknownMCCMNC;
            }

            String mcc = telephonyManager.getNetworkOperator().substring(0, 3);
            String mnc = "0"; //
            if (telephonyManager.getNetworkOperator().length() > 3)
                mnc = telephonyManager.getNetworkOperator().substring(3);

            int MCC = Integer.parseInt(mcc);
            mnc = MMCDevice.fixMNC(mnc);
            int MNC = Integer.parseInt(mnc);
            lastknownMCCMNC[0] = MCC;
            lastknownMCCMNC[1] = MNC;
            return lastknownMCCMNC;
        }
        catch (IndexOutOfBoundsException e) {
            MMCLogger.logToFile(MMCLogger.Level.WTF, TAG, "getMCCMNC", "IndexOutOfBoundsException parsing mcc+mnc: " + e.getMessage() + ", mcc+mnc=" + telephonyManager.getNetworkOperator());
        }
        catch (NumberFormatException e) {
            MMCLogger.logToFile(MMCLogger.Level.WTF, TAG, "getMCCMNC", "NumberFormatException parsing mcc+mnc: " + e.getMessage() + ", mcc+mnc=" + telephonyManager.getNetworkOperator());
        }
        catch (Exception e) {
            MMCLogger.logToFile(MMCLogger.Level.WTF, TAG, "getMCCMNC", "Exception parsing mcc+mnc: " + e.getMessage() + ", mcc+mnc=" + telephonyManager.getNetworkOperator());
        }

        if(telephonyManager.isNetworkRoaming()) {
            return new int[] {0, 0};
        }
        else {
            return lastknownMCCMNC;
        }
    }

    private static HashMap<String, Integer> handsetCaps = null;
    public static HashMap<String, Integer> getHandsetCaps (Context context)
    {
        if (handsetCaps == null)
        {
            handsetCaps = new HashMap<String, Integer>();
            String caps = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceKeys.Miscellaneous.HANDSET_CAPS, "");
            String[] rdata = caps.split (",");
            for (int i=1; i<rdata.length; i+= 2)
            {
                try
                {
                    if (rdata[i+1].length() > 0)
                        handsetCaps.put (rdata[i], Integer.parseInt(rdata[i+1]));
                }
                catch (Exception e)
                {
                }
            }
        }
        return handsetCaps;
    }

    public static ConnectionHistory getConnectionHistory ()
    {
        if (mInstance != null)
            return mInstance.mConnectionHistory;
        return null;
    }

    public static Context getContext ()
    {
        if (mInstance != null)
            return mInstance.mContext;

        return null;
    }


    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static String GCM_SENDER_ID = "1084931208947";//"AIzaSyBgmkbPeFvuXabJbFhxWxE4W30aSIh9JPk";
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices(Context context, boolean forceGCM) {

        PackageManager pkMan = mContext.getPackageManager();
        int GCMPermissionValue = pkMan.checkPermission("com.google.android.c2dm.permission.RECEIVE", mContext.getPackageName());
        if (GCMPermissionValue != 0)
            return false;

        // See if GCM services are enabled by MMC
        if (forceGCM == false) {
            int useGCM = PreferenceManager.getDefaultSharedPreferences(context).getInt(PreferenceKeys.Miscellaneous.USE_GCM, 0);
            if (useGCM == 1)
                forceGCM = true;
        }
        if (forceGCM == false)
            return false;

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
//            if (context instanceof Activity) {
//                Activity activity = (Activity) context;
//                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
//                    GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
//                            PLAY_SERVICES_RESOLUTION_REQUEST).show();
//                } else {
//                    MMCLogger.logToFile(MMCLogger.Level.WTF, TAG, "checkPlayServices", "this Device does not support Google Play Services");
//                    //Log.i(TAG, "This device is not supported.");
//                    //finish();
//                }
//			}
            MMCLogger.logToFile(MMCLogger.Level.WTF, TAG, "checkPlayServices", "this Device does not support Google Play Services");
            return false;
        }
        else
        {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
            String regid = getRegistrationId(context);

            MMCLogger.logToFile(MMCLogger.Level.WTF, TAG, "checkPlayServices", "getRegistrationId = " + regid);

            if (regid.equals("")) {
                registerInBackground(context);
            }
        }
        return true;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground(final Context context) {

        new Thread(new Runnable()
        {
            @Override
            public void run() {
                String msg = "";

                try {
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

                    String regid = gcm.register(GCM_SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    //MMCLogger.logToFile(MMCLogger.Level.WTF, TAG, "registerInBackground", msg);


                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    if (sendGCMRegistrationIdToBackend(regid)) {
                        // Persist the registration ID - no need to register again.
                        storeRegistrationId(context, regid);
                    }
                } catch (Exception ex) {
                    msg = "Error :" + ex.getMessage();
                    MMCLogger.logToFile(MMCLogger.Level.WTF, TAG, "registerInBackground", msg);

                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }

            }
        }).start();
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private static String getRegistrationId(Context context) {
        SharedPreferences securePreferences = PreferenceKeys.getSecurePreferences(context);
        String registrationId = securePreferences.getString(PreferenceKeys.Miscellaneous.KEY_GCM_REG_ID, "");
        if (registrationId.equals("")) {
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int gcmRegisteredVersion = securePreferences.getInt(PreferenceKeys.Miscellaneous.KEY_GCM_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = securePreferences.getInt(PreferenceKeys.User.VERSION, -1);

        if (gcmRegisteredVersion != currentVersion) {
            return "";
        }
        return registrationId;
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private boolean sendGCMRegistrationIdToBackend(String registrationId) {
        return reportSettingChange(ServerUpdateRequest.DEVICE, ServerUpdateRequest.KEY_GCM_REG_ID, registrationId);

    }
    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        SharedPreferences securePreferences = PreferenceKeys.getSecurePreferences(context);
        int gcmRegisteredVersion = securePreferences.getInt(PreferenceKeys.Miscellaneous.KEY_GCM_APP_VERSION, Integer.MIN_VALUE);
        int appVersion = securePreferences.getInt(PreferenceKeys.User.VERSION, -1);

        //Log.i(TAG, "Saving regId on app version " + appVersion);
        securePreferences.edit().putInt(PreferenceKeys.Miscellaneous.KEY_GCM_APP_VERSION, appVersion).commit();
        securePreferences.edit().putString(PreferenceKeys.Miscellaneous.KEY_GCM_REG_ID, regId).commit();
    }
    /**
     * Start the MMC Service if we have an apikey from registration, otherwise register in the background and then start the service
     * {@code SharedPreferences}.
     *
     * @param login name ro email to set as this users login, otherwise it may register using the devices IMEI
     * @param isEmail should login be an email and be validated?
     * @param context application's context.
     */
    public boolean registerAndStartService (String login, boolean isEmail, Context context)
    {
        SharedPreferences preferenceSettings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences secureSettings = PreferenceKeys.getSecurePreferences(context);

        boolean isRegistered = this.isAuthorized();
        if (!isRegistered)
        {
            int userID = Global.getUserID(context);
            if (userID > 0)
                isRegistered = true;
        }

        if(isRegistered) {
            boolean bStoppedService = secureSettings.getBoolean(PreferenceKeys.Miscellaneous.STOPPED_SERVICE, false);

            if (!bStoppedService)
            {
                Global.startService(mContext);
            }
        }
        else {
            // Background registration
            //Intent intent = new Intent(SplashScreen.this, GetStarted2.class);
            //startActivity(intent);
            backgroundRegistration (login, isEmail, context);
        }
        secureSettings.edit().putInt(PreferenceKeys.User.VERSION, getAppVersionCode(context)).commit();
        return true;
    }

    private void backgroundRegistration (final String login, final boolean isEmail, final Context context)
    {
        if(isEmail == false || validateEmail(login) == true) {
            new Thread(new Runnable()
            {
                @Override
                public void run() {
                    String msg = "";

                    try {
                        ReportManager manager = ReportManager.getInstance(context);

                        MMCDevice device = manager.getDevice();
                        String regLogin = login;
                        if (login == null || login.equals(""))
                        {
                            regLogin = getDevice().getIMEI();
                        }
                        manager.authorizeDevice(regLogin, true);

                        return; // success
                    }
                    catch (final MMCException e) {
                        // error in registration. show it if we have an activity context
                        if (context instanceof Activity)
                        {
                            final Activity activity = (Activity)context;
                            Handler handler = new Handler();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    String error = context.getString(R.string.getstarted_register_error);
                                    String message = e.getMessage();
                                    if (e.getMessage().equals("api key was empty"))
                                        message = context.getString(R.string.getstarted_register_apikeyempty);
                                    if (e.getCause() instanceof UnknownHostException) //  || e.getCause() instanceof HttpHostConnectException)
                                        message = context.getString(R.string.getstarted_register_unknownhost);
                                    else if (e.getCause() instanceof IOException)
                                        message = context.getString(R.string.getstarted_register_ioexception);
                                    try {
                                        new AlertDialog.Builder(activity).setTitle(error).setMessage(message).setNeutralButton(R.string.GenericText_Close, null).show();
                                    } catch (Exception e) {
                                    }
                                }
                            });

                            return; // failed
                        }
                    }

                }
            }).start();
        }

    }

    public EventObj manualPlottingEvent = null;
    public EventObj manualTransitEvent = null;

    public EventObj getPlottingEvent() {
        return manualPlottingEvent;
    }

    public EventObj getTransitEvent() {
        return manualTransitEvent;
    }

    public String submitEvent(String eventJson) throws Exception {
        return mWebReporter.submitEvent(eventJson);
    }
    public String requestCsvEmail (int userid, String carrier, int mcc, int mnc, String manufacturer, String model, String device, String appname, String apikey) throws Exception {
        return mWebReporter.requestCsvEmail(userid, carrier, mcc, mnc, manufacturer, model, device, appname, apikey);
    }

    // Trigger an extended 'drive test' combining different types of tests on intervals
    // Moved to Global level so the UI Library
    public int startDriveTest(Context context, int minutes, boolean coverage, int speed, int connectivity, int sms, int video, int audio, int web, int vq, int youtube, int ping)
    {
        JSONObject settings = new JSONObject ();
        try {
            // Trigger certain events using the Library
            settings.put("dur", minutes/5);
            settings.put("cov", coverage?1:0);
            settings.put("spd", speed);
            settings.put("ct", connectivity);
            settings.put("sms", sms);
            settings.put("vid", video);
            settings.put("audio", audio);
            settings.put("web", web);
            settings.put("vq", vq);
            settings.put("youtube", youtube);
            settings.put("ping", ping);

            JSONObject jobj = new JSONObject ();
            jobj.put ("mmctype", "rt");
            jobj.put ("settings", settings);

            String command = "[" + jobj.toString () + "]";
            Intent mmcintent = new Intent(CommonIntentActionsOld.COMMAND);
            mmcintent.putExtra(CommonIntentActionsOld.COMMAND_EXTRA, command);
            context.sendBroadcast(mmcintent);

            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PreferenceKeys.Miscellaneous.DRIVE_TEST_CMD, jobj.toString ()).commit();

        }
        catch (Exception e)
        {
            MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "startDriveTest", "exception", e);
        }

        return 0;
    }

    private boolean validateEmail(String email) {
        return email.matches("^(?:[\\w\\!\\#\\$\\%\\&\\'\\*\\+\\-\\/\\=\\?\\^\\`\\{\\|\\}\\~]+\\.)*[\\w\\!\\#\\$\\%\\&\\'\\*\\+\\-\\/\\=\\?\\^\\`\\{\\|\\}\\~]+@(?:(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9\\-](?!\\.)){0,61}[a-zA-Z0-9]?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9\\-](?!$)){0,61}[a-zA-Z0-9]?)|(?:\\[(?:(?:[01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\.){3}(?:[01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\]))$");
    }
    private int getAppVersionCode(Context context){
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch(PackageManager.NameNotFoundException e){
            MMCLogger.logToFile(MMCLogger.Level.ERROR, "SplashScreen", "getAppVerionCode", "Could not find app version" + e.getMessage());
        }
        return -1;
    }



    public LocalStorageReporter getLocalStorageReporter ()
    {
        return mLocalStorageReporter;
    }
}
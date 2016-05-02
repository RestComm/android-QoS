package com.cortxt.app.utillib.DataObjects;

import android.app.Service;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.CellIdentityLte;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.cortxt.app.utillib.ContentProvider.Tables;
import com.cortxt.app.utillib.ContentProvider.UriMatch;
import com.cortxt.app.utillib.Reporters.ReportManager;
import com.cortxt.app.utillib.Utils.LoggerUtil;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * Created by bscheurman on 16-03-19.
 */
public class PhoneState {

    /*
	 *  NETWORK_TYPE values that don't or may not exist in our lower version of the API, these can still be returned by the phone
	 *
	 */
    public static final int NETWORK_NEWTYPE_EVDOB = 12;
    public static final int NETWORK_NEWTYPE_LTE = 13;
    public static final int NETWORK_NEWTYPE_EHRPD = 14;
    public static final int NETWORK_NEWTYPE_HSPAP = 15;
    public static final int NETWORK_NEWTYPE_GSM = 16;
    public static final int NETWORK_NEWTYPE_TD_SCDMA = 17;
    public static final int NETWORK_NEWTYPE_IWLAN = 18;

    public static final int NETWORK_NEWTYPE_WIFI = 100;

    public static final int LISTEN_VOLTE_STATE =        0x00004000;
    public static final int LISTEN_OEM_HOOK_RAW_EVENT = 0x00008000;
    public static final int LISTEN_PRECISE_CALL_STATE = 0x00000800;

    public static final int SERVICE_STATE_AIRPLANE = 9;

    public static final int TYPE_WIMAX = 6;
    public static final int TYPE_ETHERNET = 9;
    public static final int TYPE_BLUETOOTH = 7;


    public TelephonyManager telephonyManager;
    private Context mContext;
    public int previousNetworkTier = -1;
    public int previousNetworkType = -1;

    public long disconnectTime = 0, offhookTime = 0, timeLTEOutage = 0;  // to undo LTE outages due to phone calls
    public String heurCause = null;

    public int previousNetworkState = -1;
    public int previousServiceState = -1;
    public ServiceState previousServiceStateObj = null;
    public int previousServiceStateAirplane = 99;

    public boolean bOffHook = false;
    public static JSONObject mServicemode = null;
    public String prevSvcValues = "";
    public int networkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
    public boolean callConnected = false, callDialing = false, callRinging = false;
    public long timeConnected = 0, timeRinging = 0, timeDialed = 0;
    public int lastKnownCallState;
    public boolean lastCallDropped = false;
    public String lastDroppedCause = "";

    //last data caches
    public CellLocationEx lastKnownMMCCellLocation;

    public ServiceState mLastServiceState;
    public long mLastServiceStateChangeTimeStamp =0;
    public long mLastDataNetworkChangeTimeStamp =0;

    public boolean mStateWasPowerOff = false;
    public SignalEx lastKnownMMCSignal;
    public SignalStrength lastKnownSignalStrength;
    public long tmLastCellUpdate = 0, tmLastCell = 0;
    public static final String TAG = PhoneState.class.getSimpleName();

    public int getLastServiceState ()
    {
        return previousServiceState;
    }

    public PhoneState (Context context)
    {
        telephonyManager = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);
        mContext = context;
    }

    public ServiceState getLastServiceStateObj ()
    {
        return previousServiceStateObj;
    }


    public boolean isCallConnected ()
    {
        return callConnected;
    }
    public long getTimeConnected ()
    {
        return timeConnected;
    }
    public long getTimeDialed ()
    {
        return timeDialed;
    }
    public void setTimeConnected (long time)
    {
        timeConnected = time;
    }
    public void setCallConnected (boolean connected)
    {
        callConnected = connected;
//		if (connected == true)
//			timeConnected = System.currentTimeMillis();
//		else
//			timeConnected = 0;
//        MMCLogger.logToFile(MMCLogger.Level.WTF, TAG, "setCallConnected", connected + " " + timeConnected);

    }
    public boolean isCallDialing ()
    {
        return callDialing;
    }

    public boolean isCallRinging ()
    {
        return callRinging;
    }

    public void setCallRinging (boolean bRinging)
    {
        if (bRinging && callRinging == false)
        {
            callRinging = true;
            timeRinging = System.currentTimeMillis();
        }
        else if (bRinging == false)
            callRinging = false;
    }
    public void setCallDialing (boolean bDialing)
    {
        callDialing = bDialing;
        if (callDialing)
            timeDialed = System.currentTimeMillis();
        else
            timeDialed = 0;

    }

    public String getNetworkTypesAndroidPreference ()
    {
        String pref = "";//Settings.Global.getString(owner.getContentResolver(), Settings.Global.NETWORK_PREFERENCE);
        return pref;
    }
    public static int getNetworkGenerationAsFlags(int networkType)
    {
        int tier = getNetworkGeneration (networkType);
        int flags = 0;
        if (tier == 5)
            flags = 0x1F;
        else if (tier >= 3)
            flags = 0x7;
        else if (tier > 1)
            flags = 0x3;
        else
            flags = 0x1;
        return flags;
    }
    /**
     * Returns an integer to represent the generation of the network type.
     * Changed to a 5 tier designation where GPRS=tier1 and LTE=tier5
     * @param networkType
     * @return 0 for unknown, 2 for 2G and 3 for 3G.
     */
    public static int getNetworkGeneration(int networkType){
        switch(networkType){
            case TelephonyManager.NETWORK_TYPE_GPRS:	// < 2g - tier 1 because data rate is <64 kbps
                return 1;

            case TelephonyManager.NETWORK_TYPE_1xRTT:	//2g  (aka CDMA 2000)
            case TelephonyManager.NETWORK_TYPE_CDMA:	//2g  (havent decided if plain cdma should be tier 1)
            case TelephonyManager.NETWORK_TYPE_EDGE:	//2g
                return 2;

            case TelephonyManager.NETWORK_TYPE_EVDO_0:	//3g
            case TelephonyManager.NETWORK_TYPE_EVDO_A:	//3g
            case TelephonyManager.NETWORK_TYPE_UMTS:	//3g
                return 3;

            // NEW NETWORK_TYPES - We need to rconsider these as 3G for now until we are sure of how to handle 4G 'outages'
            // because these technologies might only be active when transferring data and we don't want to treat as 4G outage when it reverts back to 3G
            case TelephonyManager.NETWORK_TYPE_HSDPA:	//3.5g
            case TelephonyManager.NETWORK_TYPE_HSPA:	//3.5g
            case TelephonyManager.NETWORK_TYPE_HSUPA:	//3.5g

            case PhoneState.NETWORK_NEWTYPE_HSPAP:	//3.5g HSPA+
            case PhoneState.NETWORK_NEWTYPE_EVDOB:	//3.5g
            case PhoneState.NETWORK_NEWTYPE_EHRPD:	//3.5g
                return 4;

            case PhoneState.NETWORK_NEWTYPE_LTE:	// true 4g
                return 5;

            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return 0;
            default:
                return 1;

        }
    }

    // Network Tier / Generation for speed-test purposes.
    // In this case, WiFi counts as its own Generation (tier 10), to seperate Wifi speed-tests from 3G and 4G speed tests
    public static int getSpeedTier (Context context)
    {
        int networkType = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkType();
        int speedTier = ActiveConnection(context);
        if (speedTier == 0)
            speedTier = PhoneState.getNetworkGeneration(networkType);
        //if (speedTier == 0)
        //	speedTier = 3;
        return speedTier;
    }

    // Detect if a Wifi or Wimax connection is open
    public static int ActiveConnection (Context context) {
        try{
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    int wifiState = networkInfo.getType();
                    if (wifiState == ConnectivityManager.TYPE_WIFI)
                        return 10;
                    else if (wifiState == PhoneState.TYPE_WIMAX)
                        return 11;
                    else if (wifiState == PhoneState.TYPE_ETHERNET)
                        return 12;
                    else if (wifiState == PhoneState.TYPE_BLUETOOTH)
                        return 13;
                }
            }
        }catch (Exception e){return -1;}
        return 0;
    }

    public static boolean isNetworkWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null)
            {
                int wifiState = networkInfo.getType();
                return (wifiState == ConnectivityManager.TYPE_WIFI);
            }
        }
        return false;
    }
    /**
     * Returns an integer to represent the generation of the network type.
     * Changed to a 5 tier designation where GPRS=tier1 and LTE=tier5
     * @param networkType
     * @return 0 for unknown, 2 for 2G and 3 for 3G.
     */
    public static String getNetworkName (int networkType){
        switch(networkType){
            case TelephonyManager.NETWORK_TYPE_GPRS:	// < 2g - tier 1 because data rate is <64 kbps
                return "GPRS";

            case TelephonyManager.NETWORK_TYPE_1xRTT:	//2g  (aka CDMA 2000)
                return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_CDMA:	//2g  (havent decided if plain cdma should be tier 1)
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE:	//2g
                return "EDGE";

            case TelephonyManager.NETWORK_TYPE_EVDO_0:	//3g
                return "EVDO0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:	//3g
                return "EVDOA";
            case TelephonyManager.NETWORK_TYPE_HSDPA:	//3g
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:	//3g
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:	//3g
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_UMTS:	//3g
                return "UMTS";

            // NEW NETWORK_TYPES - We need to rconsider these as 3G for now until we are sure of how to handle 4G 'outages'
            // because these technologies might only be active when transferring data and we don't want to treat as 4G outage when it reverts back to 3G
            case PhoneState.NETWORK_NEWTYPE_HSPAP:	//3.5g HSPA+
                return "HSPA+";
            case PhoneState.NETWORK_NEWTYPE_EVDOB:	//3.5g
                return "EVDOB";
            case PhoneState.NETWORK_NEWTYPE_EHRPD:	//3.5g
                return "eHRPD";

            case PhoneState.NETWORK_NEWTYPE_LTE:	// true 4g
                return "LTE";

            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            default:
                return "Unknown";
        }
    }
    public int getNetworkGeneration() {
        return getNetworkGeneration(telephonyManager.getNetworkType());
    }

    public int getPhoneType() {
        return telephonyManager.getPhoneType();
    }

    public String getNetworkOperatorName(){
        return telephonyManager.getNetworkOperatorName();
    }

    // Check the Voice Network type using new Hidden TelephonyManager method
    // This static version is for the connection history to access
    public static int getVoiceNetworkType (ServiceState serviceState)
    {
        Method m = null;
        try {
            // Java reflection to gain access to TelephonyManager's
            // ITelephony getter
            Class c = Class.forName(serviceState.getClass().getName());
            Method mI = c.getDeclaredMethod("getRilVoiceRadioTechnology");
            mI.setAccessible(true);
            int voiceTechRil = (Integer)mI.invoke(serviceState);
            int voiceTech = PhoneState.rilRadioTechnologyToNetworkType(voiceTechRil);
            return voiceTech;
        }
        catch (Exception e)
        {
            String s = e.toString();
            LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "getVoiceNetworkType", "exception", e);
        }
        return -1;
    }

    // Check the Voice Network type using new Hidden TelephonyManager method
    public int getVoiceNetworkType ()
    {
        // we're going to get the voice network type from the last ServiceState
        Method m = null;
        try {
            // Java reflection to gain access to TelephonyManager's
            Class c = Class.forName(telephonyManager.getClass().getName());
            Method mI = c.getDeclaredMethod("getVoiceNetworkType");
            mI.setAccessible(true);
            int voiceTech = (Integer)mI.invoke(telephonyManager);
            LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "getVoiceNetworkType", "Voice Network = " + voiceTech);

            Method mI2 = c.getDeclaredMethod("getDataNetworkType");
            mI2.setAccessible(true);
            int dataTech = (Integer)mI2.invoke(telephonyManager);
            LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "getDataNetworkType", "Data Network = " + dataTech);


            return voiceTech;
        }
        catch (Exception e)
        {
            String s = e.toString();
            //LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "getVoiceNetworkType", "exception", e);
        }
        return previousNetworkType;
    }
    public boolean isRoaming() {

        if (telephonyManager != null)
        {
            Boolean roaming = telephonyManager.isNetworkRoaming();
            return roaming;
        }
        else return false;
    }

    public int getLastKnownCallState() {
        return lastKnownCallState;
    }

    protected void setLastKnownCallState(int lastKnownCallState) {
        this.lastKnownCallState = lastKnownCallState;
    }

    public SignalEx getLastMMCSignal(){
        return lastKnownMMCSignal;
    }
    public void clearLastMMCSignal(){
        lastKnownMMCSignal = null;
    }

    /*******************************************
     * HELPER FUNCTIONS
     *******************************************/

    public void updateNetworkType(int networkType) {
        this.networkType = networkType;
    }

    public int getNetworkType(){
        this.networkType = telephonyManager.getNetworkType();
        return this.networkType;
    }

    public static int getNetworkType (Context context)
    {
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);
        return tm.getNetworkType();
    }

    public static void setServicemode (JSONObject svcmode){ mServicemode = svcmode; }
    public static JSONObject getServiceMode () { return mServicemode;}
    public boolean isOffHook () { return bOffHook;}

    private long _tmScreenOn = 0, _tmScreenOff = 0;
    private boolean _screenOn = true;
    /*
	 * This is called when the screen turns off or on
	 * When the service receives an intent with such an action
	 * We avoid triggering travel during the first minute of turning the screen on because that tends to trigger it prematurely
	 */
    public void screenChanged (boolean bScreenOn)
    {
        if (bScreenOn == true && _screenOn == false)
            _tmScreenOn = System.currentTimeMillis();
        else if (bScreenOn == false && _screenOn == true)
            _tmScreenOff = System.currentTimeMillis();
        _screenOn = bScreenOn;
    }
    public boolean isScreenOn ()
    {
        return _screenOn;
    }
    public long getScreenOnTime (boolean bScreenOn)
    {
        if (bScreenOn && _screenOn)
            return _tmScreenOn;
        else if (!bScreenOn && !_screenOn)
            return _tmScreenOff;
        return 0;
    }
    /*
     * Return the number the server knows for network type
     * 1 = GSM, 2 = UMTS/HSPA, 3 = CDMA
     * This lets it know how to interpret the cell identifiers
     */
    public int getNetworkTypeNumber()
    {
        // Any UMTS based technologies, return 2
        switch (getNetworkType())
        {
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case NETWORK_NEWTYPE_HSPAP:
                return 2;
        }
        if (getPhoneType() == TelephonyManager.PHONE_TYPE_GSM)
            return 1;
        // TODO: Can we detect UMTS and return 2? Otherwise it returns 1 for GSM which is fine
        if (getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA)
            return 3;
        return 0;
    }


    private String listCellLocationFields (CellLocation cell)
    {
        if (cell != null)
        {
            String strCells = "";

            Field[] fields = null;
            try {
                fields = cell.getClass().getDeclaredFields();
                int i;
                for (i=0; i<fields.length; i++)
                {
                    fields[i].setAccessible(true);
                    if (!fields[i].getName().equals("CREATOR") && !fields[i].getName().equals("LOG_TAG") &&
                            fields[i].getName().indexOf("INVALID") == -1 && fields[i].getName().indexOf("STRENGTH") == -1)
                    {
                        strCells += fields[i].getName() + "=";
                        if (fields[i].get(cell) == null)
                            strCells += "null";
                        else
                            strCells += fields[i].get(cell).toString() + ",";
                    }
                }

                //MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "listSignalFields", strSignals);
                return strCells;
            } catch (SecurityException e) {
                LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "listCellFields", "SecurityException", e);
            } catch (Exception e) {
                LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "listCellFields", "exception", e);
            }
        }
        return "";
    }

    private String listCellInfoFields (CellIdentityLte cell)
    {
        if (cell != null)
        {
            String strCells = "";

            Field[] fields = null;
            try {
                fields = cell.getClass().getDeclaredFields();
                int i;
                for (i=0; i<fields.length; i++)
                {
                    fields[i].setAccessible(true);
                    if (!fields[i].getName().equals("CREATOR") && !fields[i].getName().equals("LOG_TAG") &&
                            fields[i].getName().indexOf("INVALID") == -1 && fields[i].getName().indexOf("STRENGTH") == -1)
                    {
                        strCells += fields[i].getName() + "=";
                        if (fields[i].get(cell) == null)
                            strCells += "null";
                        else
                            strCells += fields[i].get(cell).toString() + ",";
                    }
                }

                //MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "listSignalFields", strSignals);
                return strCells;
            } catch (SecurityException e) {
                LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "listCellInfoFields", "SecurityException", e);
            } catch (Exception e) {
                LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "listCellInfoFields", "exception", e);
            }
        }
        return "";
    }

    private String listServiceStateFields (ServiceState cell)
    {
        if (cell != null)
        {
            String strCells = "";

            Field[] fields = null;
            try {
                fields = cell.getClass().getDeclaredFields();
                int i;
                for (i=0; i<fields.length; i++)
                {
                    fields[i].setAccessible(true);
                    if (fields[i].getName().indexOf("m") == 0 )
                    {
                        strCells += fields[i].getName() + "=";
                        if (fields[i].get(cell) == null)
                            strCells += "null";
                        else
                            strCells += fields[i].get(cell).toString() + ",";
                    }
                }

                return strCells;
            } catch (SecurityException e) {
                LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "listServiceStateFields", "SecurityException", e);
            } catch (Exception e) {
                LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "listServiceStateFields", "exception", e);
            }
        }
        return "";
    }


    private String listSignalFields (ServiceState serviceState)
    {
        int i;
        SignalEx mmcsignal = getLastMMCSignal();
        String strSignals = "";
        if (mmcsignal != null && !mmcsignal.isUnknown())
        {
            Object signalStrength = mmcsignal.getSignalStrength();

            Field[] fields = null;
            try {
                fields = signalStrength.getClass().getDeclaredFields();

                for (i=0; i<fields.length; i++)
                {
                    fields[i].setAccessible(true);
                    //if (!fields[i].getName().equals("CREATOR") && !fields[i].getName().equals("LOG_TAG") &&
                    //		fields[i].getName().indexOf("INVALID") == -1 && fields[i].getName().indexOf("STRENGTH") == -1)
                    if (fields[i].getName().toLowerCase().substring(0,1).equals(fields[i].getName().substring(0,1)))
                    {
                        try
                        {
                            strSignals += fields[i].getName() + "=";
                            if (fields[i].get(signalStrength) != null)
                                strSignals += fields[i].get(signalStrength).toString() + ",";
                            else
                                strSignals += "null";
                        }
                        catch (Exception e)
                        {
                            LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "listSignalFields", "exception", e);
                        }
                    }
                }
            } catch (SecurityException e) {
                LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "listSignalFields", "SecurityException", e);
            } catch (Exception e) {
                LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "listSignalFields", "exception", e);
            }
        }
        if (serviceState != null)
        {
            Field[] fields = null;
            try {
                fields = serviceState.getClass().getDeclaredFields();
                for (i=0; i<fields.length; i++)
                {
                    fields[i].setAccessible(true);
                    if (fields[i].getName().toLowerCase().substring(0,1).equals(fields[i].getName().substring(0,1)))
                    {
                        try
                        {
                            strSignals += fields[i].getName() + "=";
                            if (fields[i].get(serviceState) != null)
                                strSignals += fields[i].get(serviceState).toString() + ",";
                            else
                                strSignals += "null";
                        }
                        catch (Exception e)
                        {
                            LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "listSignalFields", "exception", e);
                        }
                    }
                }
                //MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "listSignalFields", strSignals);

            } catch (SecurityException e) {
                LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "listSignalFields", "SecurityException", e);
            } catch (Exception e) {
                LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "listSignalFields", "exception", e);
            }
        }
        return strSignals;
    }

    public Cursor fetchSignals (int timespan, long startTime)
    {
        Cursor cursor = ReportManager.getInstance(mContext).getDBProvider().query(
                UriMatch.SIGNAL_STRENGTHS.getContentUri(),
                new String[]{ Tables.TIMESTAMP_COLUMN_NAME, Tables.SignalStrengths.SIGNAL, Tables.SignalStrengths.LTE_RSRP, Tables.SignalStrengths.ECN0, Tables.SignalStrengths.ECI0, Tables.SignalStrengths.SIGNAL2G, Tables.SignalStrengths.COVERAGE },
                Tables.TIMESTAMP_COLUMN_NAME + ">?",
                new String[]{ Long.toString(startTime - timespan) },
                Tables.TIMESTAMP_COLUMN_NAME + " DESC"
        );
        return cursor;
    }
    /**
     * Available radio technologies for GSM, UMTS and CDMA. Needs to be Converted to NETWORK_TYPE
     */
    public static final int RIL_RADIO_TECHNOLOGY_UNKNOWN = 0;
    public static final int RIL_RADIO_TECHNOLOGY_GPRS = 1;
    public static final int RIL_RADIO_TECHNOLOGY_EDGE = 2;
    public static final int RIL_RADIO_TECHNOLOGY_UMTS = 3;
    public static final int RIL_RADIO_TECHNOLOGY_IS95A = 4;
    public static final int RIL_RADIO_TECHNOLOGY_IS95B = 5;
    public static final int RIL_RADIO_TECHNOLOGY_1xRTT = 6;
    public static final int RIL_RADIO_TECHNOLOGY_EVDO_0 = 7;
    public static final int RIL_RADIO_TECHNOLOGY_EVDO_A = 8;
    public static final int RIL_RADIO_TECHNOLOGY_HSDPA = 9;
    public static final int RIL_RADIO_TECHNOLOGY_HSUPA = 10;
    public static final int RIL_RADIO_TECHNOLOGY_HSPA = 11;
    public static final int RIL_RADIO_TECHNOLOGY_EVDO_B = 12;
    public static final int RIL_RADIO_TECHNOLOGY_EHRPD = 13;
    public static final int RIL_RADIO_TECHNOLOGY_LTE = 14;
    public static final int RIL_RADIO_TECHNOLOGY_HSPAP = 15;
    // GSM radio technology only supports voice. It does not support data.
    public static final int RIL_RADIO_TECHNOLOGY_GSM = 16;
    public static final int RIL_RADIO_TECHNOLOGY_TD_SCDMA = 17;
    // IWLAN
    public static final int RIL_RADIO_TECHNOLOGY_IWLAN = 18;
    public static int rilRadioTechnologyToNetworkType(int rt) {
        switch(rt) {
            case RIL_RADIO_TECHNOLOGY_GPRS:
                return TelephonyManager.NETWORK_TYPE_GPRS;
            case RIL_RADIO_TECHNOLOGY_EDGE:
                return TelephonyManager.NETWORK_TYPE_EDGE;
            case RIL_RADIO_TECHNOLOGY_UMTS:
                return TelephonyManager.NETWORK_TYPE_UMTS;
            case RIL_RADIO_TECHNOLOGY_HSDPA:
                return TelephonyManager.NETWORK_TYPE_HSDPA;
            case RIL_RADIO_TECHNOLOGY_HSUPA:
                return TelephonyManager.NETWORK_TYPE_HSUPA;
            case RIL_RADIO_TECHNOLOGY_HSPA:
                return TelephonyManager.NETWORK_TYPE_HSPA;
            case RIL_RADIO_TECHNOLOGY_IS95A:
            case RIL_RADIO_TECHNOLOGY_IS95B:
                return TelephonyManager.NETWORK_TYPE_CDMA;
            case RIL_RADIO_TECHNOLOGY_1xRTT:
                return TelephonyManager.NETWORK_TYPE_1xRTT;
            case RIL_RADIO_TECHNOLOGY_EVDO_0:
                return TelephonyManager.NETWORK_TYPE_EVDO_0;
            case RIL_RADIO_TECHNOLOGY_EVDO_A:
                return TelephonyManager.NETWORK_TYPE_EVDO_A;
            case RIL_RADIO_TECHNOLOGY_EVDO_B:
                return TelephonyManager.NETWORK_TYPE_EVDO_B;
            case RIL_RADIO_TECHNOLOGY_EHRPD:
                return TelephonyManager.NETWORK_TYPE_EHRPD;
            case RIL_RADIO_TECHNOLOGY_LTE:
                return TelephonyManager.NETWORK_TYPE_LTE;
            case RIL_RADIO_TECHNOLOGY_HSPAP:
                return TelephonyManager.NETWORK_TYPE_HSPAP;
            case RIL_RADIO_TECHNOLOGY_GSM:
                return NETWORK_NEWTYPE_GSM;
            case RIL_RADIO_TECHNOLOGY_TD_SCDMA:
                return NETWORK_NEWTYPE_TD_SCDMA;
            case RIL_RADIO_TECHNOLOGY_IWLAN:
                return NETWORK_NEWTYPE_IWLAN;
            default:
                return TelephonyManager.NETWORK_TYPE_UNKNOWN;
        }
    }
}

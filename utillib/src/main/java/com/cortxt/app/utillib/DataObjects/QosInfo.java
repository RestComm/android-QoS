package com.cortxt.app.utillib.DataObjects;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;

import com.cortxt.app.utillib.ContentProvider.Provider;
import com.cortxt.app.utillib.ContentProvider.Tables;
import com.cortxt.app.utillib.ContentProvider.UriMatch;
import com.cortxt.app.utillib.R;
import com.cortxt.app.utillib.Reporters.ReportManager;
import com.cortxt.app.utillib.Utils.CommonIntentActionsOld;
import com.cortxt.app.utillib.Utils.LoggerUtil;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.sql.Date;
import java.util.BitSet;

/**
 * Created by bscheurman on 16-04-26.
 */
public class QosInfo {

    Context context;
    public static final String TAG = QosInfo.class.getSimpleName();

    public QosInfo (Context cxt)
    {
        context = cxt;
        updateFromDB();
    }

    private static String[][] keys = {{"Time", "Network", "Data", "LTE RSSI", "RSSI", "CDMA RSSI", "LTE RSRP", "LTE RSRQ",
            "LTE SNR", "LTE CQI", "EC/I0", "EC/N0", "RSCP", "SNR", "BER", "RRC", "ARFCN"},{"MCC", "MNC", "SID", "NID", "BID", "LAC",
            "RNC", "Cell ID", "PSC", "Lat", "Lng", "Tac", "Pci", "Ci", "Band"}};

    public String Time, Carrier, Data, RRC;
    private int LTE_RSSI, RSSI, CDMA_RSSI, LTE_RSRP, LTE_RSRQ;
    private float LTE_SNR;
    private int LTE_CQI, ECIO, ECNO, RSCP, SNR, ARFCN;
    private int SID, NID, BID, LAC, RNC, CellID, PSC, Lat, Lng;
    private int Tac, Pci, Ci;
    private String Neighbors;
    public int Satellites;
    private String LTEIdentity;
    private int WifiSec, WifiFreq, WifiSig;
    private long WifiID;
    Location location;
    public int Band, Channel;
    public int MCC, MNC;

    public CDMAInfo CDMAInfo;
    public GSMInfo GSMInfo;
    public LTEInfo LTEInfo;
    public WIFIInfo WiFiInfo;

    /**
     * This method updates the percentometer using the cursor given. It is assumed that the cursor includes either the
     */
    private void updateFromDB() {
        Cursor sig_cursor = null;
        Cursor cell_cursor = null;

        try {
            Uri signalUri = UriMatch.SIGNAL_STRENGTHS.getContentUri();
            Uri limitSignalUri = signalUri.buildUpon().appendQueryParameter("limit", "1").build();
            // sig_cursor = managedQuery(

            Provider dbProvider = ReportManager.getInstance(context.getApplicationContext()).getDBProvider();
            if (dbProvider == null) {
                return;
            }

            sig_cursor = dbProvider.query(UriMatch.SIGNAL_STRENGTHS.getContentUri(), null,
                    null,
                    null,
                    Tables.TIMESTAMP_COLUMN_NAME + " DESC");

            sig_cursor.moveToFirst();

            Uri baseStationTable = (UriMatch.BASE_STATIONS.getContentUri()/*telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA
																				? UriMatchOld.BASE_STATIONS_CDMA.getContentUri()
																				: UriMatchOld.BASE_STATIONS_GSM.getContentUri()*/
            );
            Uri limitBSUri = baseStationTable.buildUpon().appendQueryParameter("limit", "1").build();

            // Cursor cell_cursor = managedQuery(
            cell_cursor = dbProvider.query(limitBSUri, null, null, null, Tables.TIMESTAMP_COLUMN_NAME + " DESC");

            cell_cursor.moveToFirst();

            int LowIndex = cell_cursor.getColumnIndex(Tables.BaseStations.BS_LOW);
            int MidIndex = cell_cursor.getColumnIndex(Tables.BaseStations.BS_MID);
            int HighIndex = cell_cursor.getColumnIndex(Tables.BaseStations.BS_HIGH);
            int CodeIndex = cell_cursor.getColumnIndex(Tables.BaseStations.BS_CODE);
            int BandIndex = cell_cursor.getColumnIndex(Tables.BaseStations.BS_BAND);
            int ChanIndex = cell_cursor.getColumnIndex(Tables.BaseStations.BS_CHAN);
            int netTypeIndex = cell_cursor.getColumnIndex(Tables.BaseStations.NET_TYPE);
            String netType = cell_cursor.getString(netTypeIndex);
            int bsLow = cell_cursor.getInt(LowIndex);
            int bsMid = cell_cursor.getInt(MidIndex);
            int bsHigh = cell_cursor.getInt(HighIndex);
            int bsCode = cell_cursor.getInt(CodeIndex);
            int bsBand = cell_cursor.getInt(BandIndex);
            int bsChan = cell_cursor.getInt(ChanIndex);
            if (netType.equals("cdma")) {
                if (LowIndex != -1)
                    BID =  bsLow;
                if (MidIndex != -1)
                    NID = bsMid;
                if (HighIndex != -1)
                    SID = bsHigh;
            } else if (netType.equals("gsm")) {
                if (LowIndex != -1) {
                    RNC = bsMid;
                    CellID = cell_cursor.getInt(LowIndex);
                }
                // the network Id is kept 0 for gsm phones
                if (HighIndex != -1)
                    LAC = bsHigh;
                if (bsCode > 0 && bsCode < 1000)
                    PSC = bsCode;
                else
                    PSC = 0;
                if (bsBand > 0)
                    Band = bsBand;
                else
                    Band = 0;
                if (bsChan > 0)
                    Channel = bsChan;
                else
                    Channel = 0;
            }

            if (sig_cursor.getCount() != 0) {
                int signalIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.SIGNAL);
                int signal2GIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.SIGNAL2G);
                int rsrpIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.LTE_RSRP);
                int rsrqIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.LTE_RSRQ);
                int lteSnrIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.LTE_SNR);
                int lteSignalIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.LTE_SIGNAL);
                int lteCqiIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.LTE_CQI);
                int ecioIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.ECI0);
                int ecnoIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.ECN0);
                int snrIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.SNR);
                int berIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.BER);
                int rscpIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.RSCP);
                int tierIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.COVERAGE);
                Integer tier = sig_cursor.isNull(tierIndex) ? null : sig_cursor.getInt(tierIndex);
                Integer rssi = sig_cursor.isNull(signalIndex) ? null : sig_cursor.getInt(signalIndex);
                Integer rssi2G = sig_cursor.isNull(signal2GIndex) ? null : sig_cursor.getInt(signal2GIndex);
                Integer rsrp = sig_cursor.isNull(rsrpIndex) ? null : sig_cursor.getInt(rsrpIndex);
                Float lteSnr = (sig_cursor.isNull(lteSnrIndex) ? null : (float) sig_cursor.getInt(lteSnrIndex));
                Integer lteSignal = sig_cursor.isNull(lteSignalIndex) ? null : sig_cursor.getInt(lteSignalIndex);
                Integer lteCqi = sig_cursor.isNull(lteCqiIndex) ? null : sig_cursor.getInt(lteCqiIndex);
                Integer rsrq = sig_cursor.isNull(rsrqIndex) ? null : sig_cursor.getInt(rsrqIndex);
                Integer eci0 = sig_cursor.isNull(ecioIndex) ? null : sig_cursor.getInt(ecioIndex);
                Integer ecn0 = sig_cursor.isNull(ecnoIndex) ? null : sig_cursor.getInt(ecnoIndex);
                Integer snr = sig_cursor.isNull(snrIndex) ? null : sig_cursor.getInt(snrIndex);
                Integer ber = sig_cursor.isNull(berIndex) ? null : sig_cursor.getInt(berIndex);
                Integer rscp = sig_cursor.isNull(rscpIndex) ? null : sig_cursor.getInt(rscpIndex);

                if (eci0 != null && (netType.equals("cdma") || netType.equals("lte")) && eci0 <= -30)
                    eci0 = (eci0 / 10);
                else if (ecn0 != null && ecn0 > 1 && ecn0 < 60 && netType.equals("gsm"))
                    ecn0 = -(ecn0 / 2);
                else if (eci0 != null && eci0 > 1 && eci0 < 60 && netType.equals("gsm"))
                    eci0 = -(eci0 / 2);

                // if (lteSnr != null && lteSnr > 1 && lteSnr < 500)
                // lteSnr = (lteSnr+5)/10;
                if (lteSignal != null && lteSignal > -120 && lteSignal < -20) // rssi == lteSignal)
                {
                    LTE_RSSI = simpleValidate(lteSignal);
                    RSSI = 0;
                } else if (rssi == null || rssi == -255)
                    RSSI = 0;
                else if (rssi == -256)
                    RSSI = -256;
                else {
                    String name = "RSCP: ";
                    int spacing = 15;
                    if (netType.equals("gsm") && (tier == 3 || tier == 4)) {
                        RSCP = rssi;
                        RSSI = 0;
                    } else {
                        RSSI = rssi;
                        RSCP = 0;
                    }

                    LTE_RSSI = 0;
                }
                if (netType.equals("cdma") && rssi2G != null && rssi2G < -30 && rssi2G >= -120)
                    CDMA_RSSI = rssi2G;
                if (tier == 5) {
                    if (lteSnr != null && lteSnr > -101)
                        lteSnr = lteSnr / 10;
                    if (rsrq != null && rsrq > 0)
                        rsrq = -rsrq;
                    LTE_RSRP = simpleValidate(rsrp);
                    LTE_RSRQ = simpleValidate(rsrq);
                    LTE_SNR = simpleValidate(lteSnr);
                    LTE_CQI = simpleValidate(lteCqi);
                    ECIO = simpleValidate(eci0);
                    ECNO = simpleValidate(ecn0);
                }
                //nerdview.setValue(0, "RSCP", simpleValidate(rscp, "RSCP", "dBm"));
                //BER = simpleValidate(ber);
                SNR = simpleValidate(snr);

                if (rsrp != null && rsrp <= -10 && rsrp >= -140 && tier == 5) {
                    LTEIdentity = ReportManager.getInstance(context.getApplicationContext()).getNeighbors();
                } else {
                    LTEIdentity = null;
                    Neighbors = ReportManager.getInstance(context.getApplicationContext()).getNeighbors();
                }
            }
            location = ReportManager.getInstance(context.getApplicationContext()).getLastKnownLocation();


            try {
                JSONObject serviceMode = PhoneState.getServiceMode();
                if (serviceMode != null && serviceMode.getLong("time") + 5000 > System.currentTimeMillis()) {
                    if (serviceMode.has("rrc") && serviceMode.getString("rrc").length() > 1) {
                        RRC = serviceMode.getString("rrc");
                    }
                    else
                        RRC = null;
                    if (serviceMode.has("band") && serviceMode.getString("band").length() > 0) {
                        Band = Integer.parseInt(serviceMode.getString("band"));
                    }
                    //else
                    if (serviceMode.has("freq") && serviceMode.getString("freq").length() > 0) {
                        Band = Integer.parseInt(serviceMode.getString("freq"));
                    }
                    else
                        Band = 0;
                    if (serviceMode.has("channel") && serviceMode.getString("channel").length() > 0) {
                        Channel = Integer.parseInt(serviceMode.getString("channel"));
                    }
                    else
                        Channel = 0;
                }
                else
                {
                    RRC = null;
                    Band = 0;
                    Channel = 0;
                }

                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
                String carrier = telephonyManager.getNetworkOperatorName();
                String mcc = "0", mnc = "0";
                if (telephonyManager.getNetworkOperator() != null && telephonyManager.getNetworkOperator().length() >= 4)
                {
                    mcc = telephonyManager.getNetworkOperator().substring(0, 3);
                    mnc = telephonyManager.getNetworkOperator().substring(3);
                }
                int networkType = telephonyManager.getNetworkType();
                int networkTier = PhoneState.getNetworkGeneration(networkType);
                String nettype = PhoneState.getNetworkName(telephonyManager.getNetworkType());
                String data = PhoneState.getNetworkName (telephonyManager.getNetworkType()) + " ";
                int dataState = telephonyManager.getDataState();
                if (dataState == TelephonyManager.DATA_CONNECTED)
                {
                    String activity = getActivityName(telephonyManager.getDataActivity());
                    data += activity;
                }
                else if (telephonyManager.getNetworkType() != TelephonyManager.NETWORK_TYPE_UNKNOWN)
                {
                    String state = getStateName(telephonyManager.getDataState());
                    data += state;
                }

                Data =  data;
                Carrier = carrier;

                Date date = new Date(System.currentTimeMillis());
                final String dateStr = DateFormat.getDateFormat(context).format(date);
                final String timeStr = dateStr + "  " + DateFormat.getTimeFormat(context).format(date);

                Time = timeStr;

                MCC = Integer.parseInt(mcc);
                MNC = Integer.parseInt(mnc);

                // Tell the service we're watching the signal, so keep it updated
                Intent intent = new Intent(CommonIntentActionsOld.VIEWING_SIGNAL);
                context.sendBroadcast(intent);

                WifiInfo wifiinfo = getWifiInfo ();
                WifiConfiguration wifiConfig = getWifiConfig ();
                setWifi(wifiinfo, wifiConfig);

                if (netType.equals("cdma"))
                    CDMAInfo = new CDMAInfo (this);
                else if (netType.equals("gsm") && networkTier < 5)
                    GSMInfo = new GSMInfo (this);
                if (networkTier == 5) // LTE
                    LTEInfo = new LTEInfo (this);
                if (wifiinfo != null)
                    WiFiInfo = new WIFIInfo (this);
            }
            catch (Exception e)
            {
            }

        } catch (Exception e) {
            LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "updateNerdViewFromDB", "exception querying signal data: " + e.getMessage());
        } finally {
            if (cell_cursor != null)
                cell_cursor.close();
            if (sig_cursor != null)
                sig_cursor.close();
        }
    }

    public WifiInfo getWifiInfo ()
    {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        return wifiInfo;
    }


    public WifiConfiguration getWifiConfig ()
    {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        WifiConfiguration activeConfig = null;
        if (wifiManager.getConfiguredNetworks() == null)
            return null;

        for ( WifiConfiguration conn: wifiManager.getConfiguredNetworks())
        {
            if(( conn.BSSID != null && conn.BSSID.equals( wifiInfo.getBSSID() )) || (conn.SSID != null && conn.SSID.equals( wifiInfo.getSSID() )))
            {
                activeConfig = conn;
                break;
            }
        }
        if (activeConfig != null)
        {
            return activeConfig;
        }
        return null;
    }
    public void setWifi(WifiInfo wifiInfo, WifiConfiguration wifiConfig) {
        if (wifiInfo == null)
            return;

        String macid = wifiInfo.getBSSID();
        if (macid == null)
            return;
        String[] bytes = macid.split(":");
        long bssid = 0;
        for (int i=0; i<6; i++)
        {
            if (i < bytes.length)
            {
                long v = hexval(bytes[i]);
                bssid = bssid + (v<<((5-i)*8));
            }
        }
        WifiID = bssid;

        if (wifiConfig != null)
        {
            int bits = 0;
            for (int i=0; i<4; i++)
            {
                if (wifiConfig.allowedKeyManagement.get(i))
                    bits += 1<<i;
            }
            BitSet bs = new BitSet ();
            //bs.set(WifiConfiguration.KeyMgmt.NONE);
            //if (wifiConfig.allowedKeyManagement..allowedAuthAlgorithms.intersects(bs))
            WifiSec =  bits;
        }

        int freq = getWifiFrequency(wifiInfo);
        if (freq != -1)
            WifiFreq = freq;

        int sig = wifiInfo.getRssi();
        WifiSig = sig;
    }

    private static int getWifiFrequency (WifiInfo wifiInfo)
    {
        int returnValue = -1;
        try {
            Method freqMethod = WifiInfo.class.getMethod("getFrequency", (Class[]) null);
            if (freqMethod != null){
                //now we're in business!
                returnValue = (Integer) freqMethod.invoke(wifiInfo, (Object[]) null);
            }
        } catch (Exception e) {
        }
        return returnValue;

    }

    public String getStateName (int state)
    {
        switch (state)
        {
            case TelephonyManager.DATA_CONNECTED:
                return "conn";
            case TelephonyManager.DATA_CONNECTING:
                return "connecting";
            case TelephonyManager.DATA_DISCONNECTED:
                return "disconnect";
            case TelephonyManager.DATA_SUSPENDED:
                return "suspended";
        }
        return "-";
    }
    public String getActivityName (int activity)
    {
        switch (activity)
        {
            case TelephonyManager.DATA_ACTIVITY_DORMANT:
                return context.getString(R.string.LiveStatus_dormant);
            case TelephonyManager.DATA_ACTIVITY_IN:
                return context.getString(R.string.LiveStatus_receiving);
            case TelephonyManager.DATA_ACTIVITY_OUT:
                return context.getString(R.string.LiveStatus_sending);
            case TelephonyManager.DATA_ACTIVITY_INOUT:
                return context.getString(R.string.LiveStatus_sendrecv);
            case TelephonyManager.DATA_ACTIVITY_NONE:
                return context.getString(R.string.LiveStatus_noactivty);

        }
        return "U";
    }

    public String getServiceStateName (int state)
    {
        String name = "";
        if (state >= 10)
        {
            name = "roam ";
            if (state == 10)
                return name;
            else
                state = state - 10;
        }
        switch (state)
        {
            case ServiceState.STATE_OUT_OF_SERVICE:
                name += "(no svc)"; break;
            case ServiceState.STATE_EMERGENCY_ONLY:
                name += "(911 only)"; break;
            case PhoneState.SERVICE_STATE_AIRPLANE:
                name += "(airplane)"; break;
            case ServiceState.STATE_IN_SERVICE:
                name += "(in svc)"; break;
            case ServiceState.STATE_POWER_OFF:
                name += "(power off)"; break;
        }
        return name;
    }

    private int hexval (String s)
    {
        int val = 0;
        if (s.length() < 2)
            return 0;
        char a = s.charAt(0);
        if (a>='0' && a<='9')
            val = (int)(a-'0')<<4;
        else if (a>='a' && a<= 'f')
            val = (int)(a-'a'+10)<<4;
        else if (a>='A' && a<= 'F')
            val = (int)(a-'A'+10)<<4;

        a = s.charAt(1);
        if (a>='0' && a<='9')
            val += (int)(a-'0');
        else if (a>='a' && a<= 'f')
            val += (int)(a-'a'+10);
        else if (a>='A' && a<= 'F')
            val += (int)(a-'A'+10);
        return val;
    }
    private int simpleValidate(Integer signal) {
        if (signal == null || signal == 0 || signal == -1 || signal == 255 || signal == 99 || signal >= 32767 || signal <= -32767)
            return 0;
        return signal;
    }
    private float simpleValidate(Float signal) {
        if (signal == null || signal == 0)
            return 0;
        return signal;
    }

    @Override
    public String toString () {
        String str =  "Carrier: " + Carrier + "\n";
        str +=  "MCC: " + MCC + " MNC: " + MNC + "\n";
        str +=  "Data: " + Data + "\n";
        if (Band > 0)
            str +=  "Band: " + Band + "\n";
        if (Channel > 0)
            str +=  "Channel: " + Channel + "\n";
        if (location != null)
            str += "location: " + location.toString();
        return str;
    }

    public class CDMAInfo
    {
        public int BID = 0, SID = 0, NID = 0, RSSI = 0, ECIO = 0, SNR = 0;
        public CDMAInfo (QosInfo qos)
        {
            SID = qos.SID;
            BID = qos.BID;
            NID = qos.NID;
            RSSI = qos.CDMA_RSSI;
            ECIO = qos.ECIO;
            SNR = qos.SNR;
        }
        @Override
        public String toString () {
            String str =  "CDMA Info:" + "\n";
            str +=  "SID: " + SID + "\n";
            str +=  "NID: " + NID + "\n";
            str +=  "BID: " + BID + "\n";
            str +=  "RSSI: " + RSSI + "\n";
            if (ECIO < 0)
                str +=  "ECIO: " + ECIO + "\n";
            if (SNR != 0)
                str +=  "SNR: " + SNR + "\n";
            return str;
        }
    }

    public class GSMInfo
    {
        public int LAC = 0, RNC = 0, CellID = 0, PSC = 0;
        public int RSSI = 0, RSCP = 0, ECIO = 0;

        public GSMInfo (QosInfo qos)
        {
            LAC = qos.LAC;
            RNC = qos.RNC;
            CellID = qos.CellID;
            PSC = qos.PSC;
            RSSI = qos.RSSI;
            RSCP = qos.RSCP;
            ECIO = qos.ECIO;
            Neighbors = qos.Neighbors;
        }

        @Override
        public String toString () {
            String str =  "GSM Info:" + "\n";
            str +=  "LAC: " + LAC + "\n";
            str +=  "RNC: " + RNC + "\n";
            str +=  "CellID: " + CellID + "\n";
            str +=  "PSC: " + PSC + "\n";
            if (RSCP < 0)
                str +=  "RSCP: " + RSCP + "\n";
            else
                str +=  "RSSI: " + RSSI + "\n";
            if (ECIO < 0)
                str +=  "EC/I0: " + ECIO + "\n";
            if (Neighbors != null && !Neighbors.isEmpty())
                str +=  "Neighbors: " + Neighbors;
            return str;
        }
    }

    public class LTEInfo
    {
        public int RSSI, RSRP, RSRQ;
        public float SNR;
        public int Tac, Pci, Ci;
        public String LTEIdentity;

        public LTEInfo (QosInfo qos)
        {
            RSSI = qos.LTE_RSSI;
            RSRP = qos.LTE_RSRP;
            RSRQ = qos.LTE_RSRQ;
            SNR = qos.LTE_SNR;
            LTEIdentity = qos.LTEIdentity;
            // LTEIdentity =  "LTE Tac:" + tac + " Ci:" + ci + " pCi:" + pci + " eNB:" + eNB + "/" + cellid;
            if (LTEIdentity != null)
            {
                int pos = LTEIdentity.indexOf("Tac:");
                int pos2 = LTEIdentity.indexOf(" Ci:");
                String val = LTEIdentity.substring(pos+4, pos2);
                Tac = Integer.parseInt(val);

                pos = pos2;
                pos2 = LTEIdentity.indexOf(" pCi:");
                val = LTEIdentity.substring(pos + 4, pos2);
                Ci = Integer.parseInt(val);

                pos = pos2;
                pos2 = LTEIdentity.indexOf(" eNB:");
                if (pos2 <= 0)
                    pos2 = LTEIdentity.length();
                val = LTEIdentity.substring(pos + 5, pos2);
                Pci = Integer.parseInt(val);
            }else
            {
                Tac = qos.LAC;
                Ci = qos.CellID + (qos.RNC<<16);
                Pci = qos.PSC;
            }
        }
        @Override
        public String toString () {
            String str =  "LTE Info:" + "\n";
            str +=  "Tac: " + Tac + "\n";
            str +=  "Ci: " + Ci + "\n";
            str +=  "Pci: " + Pci + "\n";
            str +=  "RSRP: " + RSRP + "\n";
            str +=  "SNR: " + SNR + "\n";
            if (RSRQ < 0)
                str +=  "RSRQ: " + RSRQ + "\n";
            return str;
        }
    }

    public class WIFIInfo
    {
        public int RSSI = 0;
        public int Frequency = 0;
        public long WifiID = 0;

        public WIFIInfo (QosInfo qos)
        {
            RSSI = qos.WifiSig;
            Frequency = qos.WifiFreq;
            WifiID = qos.WifiID;
        }
        @Override
        public String toString () {
            String str =  "WIFI Info:" + "\n";
            str +=  "RSSI: " + RSSI + "\n";
            str +=  "Frequency: " + Frequency + "\n";
            str +=  "WifiID: " + WifiID + "\n";
            return str;
        }
    }
}

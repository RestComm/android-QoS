package com.cortxt.app.utillib.DataObjects;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.cortxt.app.utillib.ContentProvider.Provider;
import com.cortxt.app.utillib.ContentProvider.Tables;
import com.cortxt.app.utillib.ContentProvider.UriMatch;
import com.cortxt.app.utillib.R;
import com.cortxt.app.utillib.Reporters.ReportManager;
import com.cortxt.app.utillib.Utils.CommonIntentActionsOld;
import com.cortxt.app.utillib.Utils.LoggerUtil;

import org.json.JSONObject;

import java.lang.reflect.Method;
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

    public String Time, Network, Data, LTE_RSSI, RSSI, CDMA_RSSI, LTE_RSRP, LTE_RSRQ, LTE_SNR;
    public String LTE_CQI, ECIO, ECNO, RSCP, SNR, BER, RRC, ARFCN;
    public String MMC, MNC, SID, NID, BID, LAC, RNC, CellID, PSC, Lat, Lng;
    public String Tac, Pci, Ci, Band, Channel;
    public String Neighbors;
    public String Satellites;
    public String LTEIdentity;
    public String WifiSec, WifiFreq, WifiID, WifiSig;
    Location location;

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
                    BID = Integer.toString(bsLow);
                if (MidIndex != -1)
                    NID = Integer.toString(bsMid);
                if (HighIndex != -1)
                    SID = Integer.toString(bsHigh);
            } else if (netType.equals("gsm")) {
                if (LowIndex != -1) {
                    if (bsMid > 0)
                        RNC = Integer.toString(bsMid);
                    else
                        RNC = null;
                    CellID = Integer.toString(cell_cursor.getInt(LowIndex));
                }
                // the network Id is kept 0 for gsm phones
                if (HighIndex != -1)
                    LAC = Integer.toString(bsHigh);
                else
                    LAC = null;
                if (bsCode > 0 && bsCode < 1000)
                    PSC = Integer.toString(bsCode);
                else
                    PSC = null;
                if (bsBand > 0)
                    Band = Integer.toString(bsBand);
                else
                    Band = null;
                if (bsChan > 0)
                    ARFCN = Integer.toString(bsChan);
                else
                    ARFCN = null;
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
                    LTE_RSSI = simpleValidate(lteSignal, "LTE RSSI", "dBm");
                    RSSI = null;
                } else if (rssi == null || rssi == -255)
                    RSSI = context.getString(R.string.GenericText_Unknown);
                else if (rssi == -256)
                    RSSI = context.getString(R.string.GenericText_None);
                else {
                    String name = "RSCP: ";
                    int spacing = 15;
                    if (netType.equals("gsm") && (tier == 3 || tier == 4)) {
                        RSCP = rssi.toString();
                        RSSI = null;
                    } else {
                        RSSI = rssi.toString();
                        RSCP = null;
                    }

                    LTE_RSSI = null;
                }
                if (netType.equals("cdma") && rssi2G != null && rssi2G < -30 && rssi2G >= -120)
                    CDMA_RSSI = rssi2G.toString();
                if (tier == 5) {
                    if (lteSnr != null && lteSnr > -101)
                        lteSnr = lteSnr / 10;
                    if (rsrq != null && rsrq > 0)
                        rsrq = -rsrq;
                    LTE_RSRP = simpleValidate(rsrp, "LTE RSRP", "dBm");
                    LTE_RSRQ = simpleValidate(rsrq, "LTE RSRQ", "dB");
                    LTE_SNR = simpleValidate(lteSnr, "LTE SNR", "");
                    LTE_CQI = simpleValidate(lteCqi, "LTE CQI", "");
                    ECIO = simpleValidate(eci0, "EC/I0", "dB");
                    ECNO = simpleValidate(ecn0, "EC/N0", "dB");
                }
                //nerdview.setValue(0, "RSCP", simpleValidate(rscp, "RSCP", "dBm"));
                BER = simpleValidate(ber, "BER", "");
                SNR = simpleValidate(snr, "SNR", "");

                if (rsrp != null && rsrp <= -10 && rsrp >= -140 && tier == 5) {
                    LTEIdentity = ReportManager.getInstance(context.getApplicationContext()).getNeighbors();
                } else {
                    LTEIdentity = null;
                    Neighbors = ReportManager.getInstance(context.getApplicationContext()).getNeighbors();
                }
            }
            Location loc = ReportManager.getInstance(context.getApplicationContext()).getLastKnownLocation();


            try {
                JSONObject serviceMode = PhoneState.getServiceMode();
                if (serviceMode != null && serviceMode.getLong("time") + 5000 > System.currentTimeMillis()) {
                    if (serviceMode.has("rrc") && serviceMode.getString("rrc").length() > 1) {
                        RRC = serviceMode.getString("rrc");
                    }
                    else
                        RRC = null;
                    if (serviceMode.has("band") && serviceMode.getString("band").length() > 0) {
                        Band = serviceMode.getString("band");
                    }
                    //else
                    if (serviceMode.has("freq") && serviceMode.getString("freq").length() > 0) {
                        Band = serviceMode.getString("freq");
                    }
                    else
                        Band = null;
                    if (serviceMode.has("channel") && serviceMode.getString("channel").length() > 0) {
                        Channel = serviceMode.getString("channel");
                    }
                    else
                        Channel = null;
                }
                else
                {
                    RRC = null;
                    Band = null;
                    Channel = null;
                }

                WifiInfo wifiinfo = getWifiInfo ();
                WifiConfiguration wifiConfig = getWifiConfig ();
                setWifi(wifiinfo, wifiConfig);
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
        WifiID = Long.toString(bssid);

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
            WifiSec = Integer.toString(bits);
        }

        int freq = getWifiFrequency(wifiInfo);
        if (freq != -1)
            WifiFreq = Integer.toString(freq);

        int sig = wifiInfo.getRssi();
        WifiSig = Integer.toString(sig);
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
    private String simpleValidate(Integer signal, String type, String unit) {
        if (signal == null || signal == 0 || signal == -1 || signal == 255 || signal == 99 || signal >= 32767 || signal <= -32767)
            return null;
        return signal.toString() + " " + unit;
    }
    private String simpleValidate(Float signal, String type, String unit) {
        if (signal == null || signal == 0)
            return null;
        return signal.toString() + " " + unit;
    }
}

package com.cortxt.app.corelib.Utils;

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

import com.cortxt.app.corelib.R;
import com.cortxt.app.utillib.ContentProvider.Provider;
import com.cortxt.app.utillib.ContentProvider.Tables;
import com.cortxt.app.utillib.ContentProvider.UriMatch;
import com.cortxt.app.utillib.DataObjects.PhoneState;
import com.cortxt.app.utillib.Reporters.ReportManager;
import com.cortxt.app.utillib.Utils.CommonIntentActionsOld;
import com.cortxt.app.utillib.Utils.LoggerUtil;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.sql.Date;
import java.util.BitSet;

/**
 * QosInfo is a class that consolidates a wide variety of information about the network state of the phone
 * The information originated from several sources such as apis, listeners, hidden methods and possibly even service mode
 * QosInfo contains 4 inner classes representing the main network types, and each is only populated if in that network type
 * <ul>
 * <li>GSMInfo
 * <li>WiFiInfo
 * <li>LTEInfo
 * <li>CDMAInfo
 * <li>plus general info in main class
 * </ul>
 * <p>
 *
 * @author      Brad Scheurman
 * @version     %I%, %G%
 * @since       1.0
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
    public GSMInfo GSM_2GInfo;
    public WCDMAInfo GSM_3GInfo;
    public LTEInfo LTEInfo;
    public WIFIInfo WiFiInfo;
    /**
     * The connectedNetwork represents the network that is currently connected, whether it be LTE, WiFi etc..
     * As a NetworkInfo object, it can be used in a unified way to access the relevant type of signal indicator, quality indicator,
     * and an array of all the cellular identifiers used to uniquely identify the base station the device is connected to for the current network
     * For example: CDMA, LTE and WiFi can all be active at the same time, and it will populate CDMAInfo, LTEInfo and WiFiInfo
     * In this case, the connectedNetwork will be set to the WiFiInfo object because that is the only network in use for data connections
     * In other words, you can assume the connectedNetwork always has the relevant details
     */
    public NetworkInfo connectedNetwork;

    /**
     * NetworkInfo is the base class for all of the main types of data network including LTE, WiFi
     * It is meant to be used directly to simplify access to the most relevant indicators and identifiers for the active network
     * Thus it is recommended to use the connectedNetwork object directly, accessing the methods of this class
     * Alternatively you can use the derived classes such as LTEInfo or WiFiInfo more more specific information
     *
     * NetworkInfo provides a signal indicator, quality indicator, and an array of all the cellular identifiers
     * The signal indicator consists of a label such as 'RSSI' or 'RSRP' to describe the measurement the represents signal strength for the current type of network
     * 
     */
    public class NetworkInfo
    {
        protected int signal=0;
        protected int sigMin=0, sigMax=0;
        protected int sigExcellent=0;  // the minimum signal strength that would be considered as excellent
        protected String sigLabel="", sigUnits="";
        protected String sigDoc="";

        protected float noise = 0;
        protected int noiseMin=0, noiseMax=0;
        protected int noiseExcellent;  // the minimum signal strength that would be considered as excellent
        protected String noiseLabel="", noiseUnits="";
        protected String noiseDoc="";

        protected String[] identifierLabels, identifierDocs;
        protected long[] identifierValues;
        protected String networkType;

        public int getSignalRating ()
        {
            // calculate a rating from 0 to 5

            // unknown signal is rated as -1 and should be ignored
            if (signal == 0) return -1;
            // anything <= sigMin is rated as 0 for no signal
            if (signal <= sigMin) return 0;
            // anything >= sigExcellent is rated as 5
            if (signal >= sigExcellent) return 5;

            // all others are scaled from 1 to 4
            int quarter = (sigExcellent - sigMin) / 4;
            int rating = 1 + (signal-sigMin) * 4 / (sigExcellent-sigMin);
            return rating;

        }
        public String getSignalDetails (boolean withValue, boolean withRating) {
            if (getSignalLabel() == "")
                return "";
            String details = getSignalLabel() + ": ";
            if (withValue) {
                if (signal != 0)
                    details += signal + " " + getSignalUnits();
                else
                    details += "unknown";
            }
            if (withRating && signal != 0 && getSignalRating() > 0)
                details += " rating " + getSignalRating() + "/5";
            return details;
        }
        public String getSignalLabel () { return sigLabel; }
        public String getSignalUnits () { return sigUnits; }
        public int getSignalRangeMin () { return sigMin; }
        public int getSignalRangeMax () { return sigMax; }
        public String getSignalDoc () { return noiseDoc; }
        public int getSignal () { return signal;}

        public int getQualityRating ()
        {
            // calculate a rating from 0 to 5

            // unknown signal is rated as -1 and should be ignored
            if (noise == 0) return -1;
            // anything <= sigMin is rated as 0 for no signal
            if (noise <= noiseMin) return 0;
            // anything >= sigExcellent is rated as 5
            if (noise >= noiseExcellent) return 5;

            // all others are scaled from 1 to 4
            int quarter = (noiseExcellent - noiseMin) / 4;
            int rating = 1 + (int)(noise*10-noiseMin*10) * 4 / (int)(noiseExcellent*10-noiseMin*10);
            return rating;

        }
        public String getQualityDetails (boolean withValue, boolean withRating) {
            if (getQualityLabel() == "")
                return "";
            String details = getQualityLabel() + ": ";
            if (withValue)
            {
                if (noise != 0)
                    details += noise + " " + getQualityUnits() + " ";
                else
                    details += "unknown ";
            }
            if (withRating && noise != 0 && getQualityRating() >= 0)
                details += "rating " + getQualityRating() + "/5";

            return details;
        }
        public String getQualityLabel () { return noiseLabel; }
        public String getQualityUnits () { return noiseUnits; }
        public int getQualityRangeMin () { return noiseMin; }
        public int getQualityRangeMax () { return noiseMax; }
        public String getQualityDoc () { return noiseDoc; }
        public float getQuality () { return noise;}

        public String getIdentifier ()
        {
            String identifiers = "";

            int i;
            if (identifierValues != null && identifierLabels != null) {
                for (i = 0; i < identifierValues.length; i++) {
                    if (i < identifierLabels.length) {
                        identifiers += identifierLabels[i] + ": ";
                        if (identifierValues[i] == 0)
                            identifiers += "n/a ";
                        else
                            identifiers += identifierValues[i] + " ";
                    }
                }
            }
            return identifiers;
        }
        public String[] getIdentifierLabels () { return identifierLabels; };
        public long[] getIdentifierValues (){ return identifierValues; };
        public String[] getIdentifierDocs (){ return identifierDocs; };

        public String getType () { return networkType; }
    }

    /**
     * CDMAInfo exposes information relevant to CDMA Networks
     * <ul>
     * <li>SID
     * <li>BID
     * <li>NID
     * <li>RSSI
     * <li>ECI0 (Ec/n0)
     * <li>SNR
     * </ul>
     */
    public class CDMAInfo extends NetworkInfo
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

            // base class assignments to describe LTE info in a unified way
            networkType = "CDMA";
            signal = qos.RSSI;
            sigLabel = "RSCP";
            sigUnits = "dBm";
            sigDoc = "RSSI gives the total power of all received signal (Receive Signal Strength Indicator). RSSI includes noise enery, but subtracting Ec/i0 from RSSI gives the actual usable signal.";
            sigMax = -40;
            sigMin = -120;
            sigExcellent = -65;

            noise = qos.ECIO;
            noiseLabel = "Ec/iO";
            noiseUnits = "dB";
            noiseDoc = "Ec/iO gives a ratio expressed in dBs of signal to noise and/or interference (Energy/interference). ";
            noiseMax = -2;
            noiseMin = -22;
            noiseExcellent = -6;

            identifierLabels = new String[] {"SID","NID","BID"};
            identifierDocs = new String[] {"SID is the 'System ID' which identifies a region of towers, unique worldwide", "NID is the 'Network ID'", "BID is the 'Billing ID' which Uniquely identifies a cell tower, sector and band within a SID and NID"};
            identifierValues = new long[] {SID,NID,BID};
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

    /**
     * GSMInfo exposes information relevant to 2G GSM Networks such as GPRS and EDGE
     * <ul>
     * <li>LAC
     * <li>CellID
     * <li>RSSI
     * <li>ECI0 (Ec/n0)
     * </ul>
     */
    public class GSMInfo extends NetworkInfo
    {
        public int LAC = 0, RNC = 0, CellID = 0, PSC = 0;
        public int RSCP = 0, ECIO = 0;

        public GSMInfo(QosInfo qos)
        {
            LAC = qos.LAC;
            CellID = qos.CellID;
            RSSI = qos.RSSI;

            // base class assignments to describe LTE info in a unified way
            networkType = "2G GSM";
            signal = qos.RSSI;
            sigLabel = "RSSI";
            sigUnits = "dBm";
            sigDoc = "RSSI gives the total power of all received signal (Receive Signal Strength Indicator). A stronger signal is better, but noise and signal from other cell sectors is included in the total RSSI.";
            sigMax = -40;
            sigMin = -120;
            sigExcellent = -65;

            identifierLabels = new String[] {"LAC","CellId"};
            identifierDocs = new String[] {"Local Area Code, identifies a group of cell towers", "Cell Id uniquely identifies a cell tower, sector and band when combined with LAC"};
            identifierValues = new long[] {LAC,CellID};
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

    /**
     * WCDMAInfo exposes information relevant to 3G Networks such as UMTS and HSDPA
     * <ul>
     * <li>LAC
     * <li>RNC
     * <li>CellID
     * <li>PSC
     * <li>RSCP
     * <li>ECI0 (Ec/n0)
     * <li>SNR
     * </ul>
     */
    public class WCDMAInfo extends NetworkInfo
    {
        public int LAC = 0, RNC = 0, CellID = 0, PSC = 0;
        public int RSCP = 0, ECIO = 0;

        public WCDMAInfo(QosInfo qos)
        {
            LAC = qos.LAC;
            RNC = qos.RNC;
            CellID = qos.CellID;
            PSC = qos.PSC;
            RSCP = qos.RSCP;
            ECIO = qos.ECIO;
            Neighbors = qos.Neighbors;

            // base class assignments to describe LTE info in a unified way
            networkType = "3G GSM";
            signal = qos.RSCP;
            sigLabel = "RSCP";
            sigUnits = "dBm";
            sigDoc = "RSCP gives the power of the usable component of 3G signal (Receive Signal Code Power). It is lower compared to the RSSI because it measures a specific part of the signal that carries data.";
            sigMax = -40;
            sigMin = -120;
            sigExcellent = -75;

            noise = qos.ECIO;
            noiseLabel = "Ec/iO";
            noiseUnits = "dB";
            noiseDoc = "Ec/iO gives a ratio expressed in dBs of signal to noise and/or interference (Energy/interference). Android rarely reports Ec/i0 for GSM, except on some rooted devices";
            noiseMax = -2;
            noiseMin = -22;
            noiseExcellent = -6;

            identifierLabels = new String[] {"LAC","RNC","CellId","PSC"};
            identifierDocs = new String[] {"Local Area Code, identifies a group of cell towers", "RNC Radio Network Controller is the top 12 bits of a 28 bit Cell Identifier", "Cell Id is the bottom 16 bits of a 28 bit Cell identifier in 3G. Uniquely identifies a cell tower, sector and band when combined with LAC", "PSC Primary Scrambling Code, short non-unique cell identifier"};
            identifierValues = new long[] {LAC,RNC,CellID,PSC};
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

    public class LTEInfo extends NetworkInfo
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

            // base class assignments to describe LTE info in a unified way
            networkType = "LTE";
            signal = qos.LTE_RSRP;
            sigLabel = "RSRP";
            sigUnits = "dBm";
            sigDoc = "RSRP gives the power of the reference component of LTE signal (Reference Signal Receive Power). It is about 20 dBm lower compared to the RSSI because it measures a specific part of the signal that carries data.";
            sigMax = -40;
            sigMin = -122;
            sigExcellent = -80;

            noise = qos.LTE_SNR;
            if (noise == 0)
                noise = 0.1f;
            noiseLabel = "SINR";
            noiseUnits = "";
            noiseDoc = "SINR gives the ratio of signal to noise+interference (Signal to Interference+Noise Ratio). SINR is a good predictor data bit rate.";
            noiseMax = 30;
            noiseMin = -5;
            noiseExcellent = 20;

            identifierLabels = new String[] {"Tac","Ci","Pci"};
            identifierDocs = new String[] {"Tracking Area Code, like a GSM LAC", "Cell Id", "Physical cell id, 0-504, not unique"};
            identifierValues = new long[] {Tac,Ci,Pci};
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

    public class WIFIInfo extends NetworkInfo
    {
        public int RSSI = 0;
        public int Frequency = 0;
        public long WifiID = 0;

        public WIFIInfo (QosInfo qos)
        {
            RSSI = qos.WifiSig;
            Frequency = qos.WifiFreq;
            WifiID = qos.WifiID;

            // base class assignments to describe WiFi info in a unified way
            networkType = "WiFi";
            signal = RSSI;
            sigLabel = "RSSI";
            sigUnits = "dBm";
            sigDoc = "RSSI gives the overall power of the WiFi signal (Received Signal Strength Indicator)";
            sigMax = -40;
            sigMin = -120;
            sigExcellent = -65;
            identifierLabels = new String[] {"BSSID"};
            identifierDocs = new String[] {"BSSID is a unique number identifying the WiFi access point (Basic Service Set Identifier)"};
            identifierValues = new long[] {WifiID};
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

                // Instantiate only the relevant Network type
                if (netType.equals("cdma"))
                    NetworkInfo = CDMAInfo = new CDMAInfo (this);
                else if (netType.equals("gsm") && networkTier < 3)
                    NetworkInfo = GSM_2GInfo = new GSMInfo(this);
                else if (netType.equals("gsm") && networkTier < 5)
                    NetworkInfo = GSM_3GInfo = new WCDMAInfo(this);
                if (networkTier == 5) // LTE
                    NetworkInfo = LTEInfo = new LTEInfo (this);
                if (wifiConfig != null)
                    NetworkInfo = WiFiInfo = new WIFIInfo (this);  // The most relevant network ends up in NetworkInfo
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
        if (wifiInfo == null || wifiConfig == null)
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


}

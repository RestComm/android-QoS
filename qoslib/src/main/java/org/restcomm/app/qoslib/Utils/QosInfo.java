/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * For questions related to commercial use licensing, please contact sales@telestax.com.
 *
 */

package org.restcomm.app.qoslib.Utils;

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

import org.restcomm.app.utillib.ContentProvider.Provider;
import org.restcomm.app.utillib.ContentProvider.Tables;
import org.restcomm.app.utillib.ContentProvider.UriMatch;
import org.restcomm.app.utillib.DataObjects.PhoneState;
import org.restcomm.app.utillib.Reporters.ReportManager;
import org.restcomm.app.utillib.Utils.CommonIntentActionsOld;
import org.restcomm.app.utillib.Utils.LoggerUtil;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.sql.Date;
import java.util.BitSet;
import com.restcomm.app.qoslib.R;

/**
 * QosInfo is a class that consolidates a wide variety of information about the network state of the phone.
 * The information originated from several sources such as apis, listeners, hidden methods and possibly even service mode.
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

    /**
     * Specific information about the CDMA network if CDMA is active, otherwise this object is null
     */
    public CDMAInfo CDMAInfo;

    /**
     * Specific information about the 2G GSM network such as GPRS or EDGE if 2G active, otherwise this object is null
     */
    public GSMInfo GSMInfo;

    /**
     * Specific information about the 3G WCDMA network such as UMTS or HSPA if 3G active, otherwise this object is null
     */
    public WCDMAInfo WCDMAInfo;

    /**
     * Specific information about the LTE network if LTE is active, otherwise this object is null
     */
    public LTEInfo LTEInfo;

    /**
     * Specific information about the WiFi network if WiFi is active, otherwise this object is null
     */

    public WIFIInfo WiFiInfo;
    /**
     * The connectedNetwork represents the network that is currently connected, whether it be LTE, WiFi etc..
     * As a NetworkInfo object, it can be used in a unified way to access relevant info about the network.
     * For example: If CDMA, LTE and WiFi are active at the same time, the connectedNetwork is WiFi, because that is the only network in use for data connections.
     */
    public NetworkInfo connectedNetwork;

    /**
     * NetworkInfo is meant to be used directly to simplify access to information about the connected network.
     * NetworkInfo is also the base class for all of the main types of data network including LTEInfo, WiFiInfo, etc.
     * The {@link NetworkInfo connectedNetwork} object is a NetworkInfo object.
     *
     * NetworkInfo provides a signal indicator, quality indicator, and an array of all the cellular identifiers.
     * The signal indicator consists of a label such as 'RSSI' or 'RSRP' to describe the measurement the represents signal strength for the current type of network.
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

        /**
         *  Easy to understand rating for the signal type for the connected network.
         *
         *  @return rating from 0 to 5, equivalent to 'bars' where 5 bars is an excellent signal level
         */
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

        /**
         * getSignalDetails returns a text description of the signal type for the connected network.
         * the text is composed of the signalLabel and, optionally, the value, rating and units.
         * @param withValue  to include the dBm value of the signal  {@link int getSignal ()} and its units {@link String getSignalUnits ()}
         * @param withRating to include the rating of the signal {@link int getSignalRating ()}
         * @return the full text description
         */
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

        /**
         * Get the name of the type of signal for the connected network.
         * The label, units and ranges are defined in the constructor for each derived class.
         * @return the name of the relevent type of signal (RSSI, RSRP etc)
         */
        public String getSignalLabel () { return sigLabel; }
        /**
         * Get the unit for the type of signal for the connected network.
         * @return the unit for the relevent type of signal (usually dBm)
         */
        public String getSignalUnits () { return sigUnits; }
        /**
         * Get the minimum value of the signal range.
         * @return the minimum signal value (usually -120 dBm)
         */
        public int getSignalRangeMin () { return sigMin; }
        /**
         * Get the maximum value of the signal range.
         * @return the maximum signal value (usually -40 dBm)
         */
        public int getSignalRangeMax () { return sigMax; }
        /**
         * Get the value of the signal that gives excellent performance, expected near the tower.
         * Values above excellent are rated as 5 bars.
         * Each derived network type sets its range. RSRP -85 dBm is 'excellent' for LTE.
         * @return the excellent signal value
         */
        public int getSignalRangeExcellent () { return sigExcellent; }
        /**
         * Get a short documentation of the relevent type of signal for the network.
         * @return the description document
         */
        public String getSignalDoc () { return sigDoc; }
        /**
         * Get the actual value of the signal.
         * @return the signal value (usually -40 to -120)
         */
        public int getSignal () { return signal;}

        /**
         * Get the actual value of the noise or quality indicator.
         * Some network types can provide an additional performance indicator.
         * This is usually a measure of noise and is a good indicator or data throughput or voice quality.
         * @return the indicator value
         */
        public float getQuality () { return noise;}
        /**
         *  Easy to understand rating for the signal type for the connected network.
         *
         *  @return rating from 0 to 5, equivalent to 'bars' where 5 bars is an excellent signal level.
         */
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

        /**
         * getQualityDetails returns a text description of the main noise/quality indicator, if available, for the connected network.
         * The text is composed of the qualityLabel and, optionally, the value, rating and units.
         * @param withValue  to include the value of the quality indicator  {@link int getQuality ()} and its units {@link String getQualityUnits ()}
         * @param withRating to include the rating of the quality indicator {@link int getQualityRating ()}
         * @return
         */
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
        /**
         * Get the name of the type of quality/noise indicator the connected network.
         * The label, units and ranges are defined in the constructor for each derived class.
         * @return the name of the indicator for the connected network (Ec/i0, SNR etc)
         */
        public String getQualityLabel () { return noiseLabel; }
        /**
         * Get the unit for the type of quality/noise indicator for the connected network
         * @return the unit for the connected network (may be unitless and return a blank string)
         */
        public String getQualityUnits () { return noiseUnits; }
        /**
         * Get the minimum value of the range for the main quality/noise indicator
         * @return the minimum value (usually -30 for Ec/i0 or -10 for SNR)
         */
        public int getQualityRangeMin () { return noiseMin; }
        /**
         * Get the maximum value of the range for the main quality/noise indicator
         * @return the maximum signal value (usually -40 dBm)
         */
        public int getQualityRangeMax () { return noiseMax; }
        /**
         * Get the value of the main quality/noise indicator that gives excellent performance, expected near the tower
         * Values above excellent are rated as 5 bars
         * set for each derived network type, SNR 20 dBm is 'excellent' for LTE
         * @return the excellent signal value
         */
        public int getQualityRangeExcellent () { return sigMax; }
        /**
         * Get a short documentation of the relevent type of signal for the network
         * @return the description document
         */
        public String getQualityDoc () { return noiseDoc; }

        /**
         * getIdentifier returns a text description of the Base Station's full Identifier
         * For example a full 3G cell identifier consists of LAN,RNC,CellID and PSC
         * Each derived Network type defines an array of labels, values and doc descriptions to represent
         * all the components to be appended for getIdentifier
         * @return the full text description to indentify the current base station
         */
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
        /**
         * Get the array of names of the Identifier parts defined for the current Network type
         * @return the array of labels
         */
        public String[] getIdentifierLabels () { return identifierLabels; };
        /**
         * Get the array of values for the Identifier parts defined for the current Network type
         * @return the array of values
         */
        public long[] getIdentifierValues (){ return identifierValues; };
        /**
         * Get the array of docs for the Identifier parts defined for the current Network type
         * @return the array of values
         */
        public String[] getIdentifierDocs (){ return identifierDocs; };

        /**
         * Get the name of the type of network
         * @return the type of network
         */
        public String getType () { return networkType; }

        /**
         * Get the type of technology for the connected network
         * @return the type of network
         */
        public String getTechnology () { return Data; }
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
        /**
         * Initialize all the special labels, units, value ranges appropriate for the CDMA type of network
        */
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

        /**
         * Initialize all the special labels, units, value ranges appropriate for the 2G GSM type of network
         */
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

        /**
         * Initialize all the special labels, units, value ranges appropriate for the 3G WCDMA type of network
         */
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

    /**
     * LTEInfo exposes information relevant to LTE Networks
     * <ul>
     * <li>RSSI
     * <li>RSRP (main signal indicator)
     * <li>RSRQ
     * <li>SNR (main quality indicator)
     * <li>Base Station Identifiers
     * <li>Tac
     * <li>Ci
     * <li>Pci
     * <li>SNR
     * </ul>
     */
    public class LTEInfo extends NetworkInfo
    {
        public int RSSI, RSRP, RSRQ;
        public float SNR;
        public int Tac, Pci, Ci;
        public String LTEIdentity;

        /**
         * Initialize all the special labels, units, value ranges appropriate for the LTE type of network
         */
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

    /**
     * WIFIInfo exposes information relevant to WiFi Networks
     * <ul>
     * <li>RSSI (main signal indicator)
     * <li>(no quality indicator)
     * <li>Base Station Identifiers
     * <li>WifiID
     * </ul>
     */
    public class WIFIInfo extends NetworkInfo
    {
        public int RSSI = 0;
        public int Frequency = 0;
        public long WifiID = 0;

        /**
         * Initialize all the special labels, units, value ranges appropriate for the WiFi type of network
         */
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
     * Called Internally to initialize all of the current measurements for every type of connected newtork
     * This is like a snapshot of everything at this moment in time
     * The information comes from various sources and it mainly fetched from the database for this call, or from APIs such as WiFi
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
                    connectedNetwork = CDMAInfo = new CDMAInfo (this);
                else if (netType.equals("gsm") && networkTier < 3)
                    connectedNetwork = GSMInfo = new GSMInfo(this);
                else if (netType.equals("gsm") && networkTier < 5)
                    connectedNetwork = WCDMAInfo = new WCDMAInfo(this);
                if (networkTier == 5) // LTE
                    connectedNetwork = LTEInfo = new LTEInfo (this);
                if (wifiConfig != null)
                    connectedNetwork = WiFiInfo = new WIFIInfo (this);  // The most relevant network ends up in NetworkInfo
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

    private WifiInfo getWifiInfo ()
    {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        return wifiInfo;
    }


    private WifiConfiguration getWifiConfig ()
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
    private void setWifi(WifiInfo wifiInfo, WifiConfiguration wifiConfig) {
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

    public static int getWifiFrequency (WifiInfo wifiInfo)
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

    /*
    * Get the data connection state as a name
    * (connected, disconnected etc)
    * returns string name
    */
    public String getStateName (int state)
    {
        switch (state)
        {
            case TelephonyManager.DATA_CONNECTED:
                return "connected";
            case TelephonyManager.DATA_CONNECTING:
                return "connecting";
            case TelephonyManager.DATA_DISCONNECTED:
                return "disconnected";
            case TelephonyManager.DATA_SUSPENDED:
                return "suspended";
        }
        return "-";
    }
    /*
    * Get the current state of data activity, whether data is actively sending, receiving, etc
    * (send, recv, send recv, dormant, etc)
    * returns string to describe activity
    */
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

    /*
    * Get the cellular service state as a name
    * (in service, no service, etc)
    * returns string name
     */
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
                name += "no service"; break;
            case ServiceState.STATE_EMERGENCY_ONLY:
                name += "911 only"; break;
            case PhoneState.SERVICE_STATE_AIRPLANE:
                name += "airplane"; break;
            case ServiceState.STATE_IN_SERVICE:
                name += "in service"; break;
            case ServiceState.STATE_POWER_OFF:
                name += "power off"; break;
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

    /**
     * List the high level information about the Carrier Network such as the Carrier name, MCC and MNC
     * @return a String describing the QosInfo object
     */
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
            str += "location: " + location.toString() + "\n";
        if (connectedNetwork != null)
            str +=  "\n" + "Connected Network:\n" + connectedNetwork.toString ();
        return str;
    }


}

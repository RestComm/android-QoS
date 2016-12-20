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

package org.restcomm.app.utillib.CoverageSamples;

import android.database.Cursor;

import org.restcomm.app.utillib.ContentProvider.Tables;
import org.restcomm.app.utillib.DataObjects.EventObj;
import org.restcomm.app.utillib.DataObjects.EventType;

import java.util.ArrayList;

/**
 * Created by bscheurman on 16-03-21.
 */
public class CellSamples {
    public int high, mid, low, code;
    public int band, chan;
    public String netType;
    public ArrayList<EvtSample> samples = new ArrayList<EvtSample>();
    public CellSamples (int _high, int _mid, int _low, int _code, String _netType)
    {
        high = _high;
        mid = _mid;
        low = _low;
        code = _code; // (PSC)
        netType = _netType;
    }
    public CellSamples (Cursor cellCursor)
    {
        int currentHigh = 0;
        int currentMid = 0;
        int currentLow = 0;
        int currentCode = 0;
        int currentBand = 0;
        int currentChan = 0;

        if (!cellCursor.isAfterLast())
        {
            int highIndex, midIndex, lowIndex, codeIndex, bandIndex, chanIndex, netTypeIndex;

            highIndex = cellCursor.getColumnIndexOrThrow(Tables.BaseStations.BS_HIGH);
            midIndex = cellCursor.getColumnIndexOrThrow(Tables.BaseStations.BS_MID);
            lowIndex = cellCursor.getColumnIndexOrThrow(Tables.BaseStations.BS_LOW);
            codeIndex = cellCursor.getColumnIndexOrThrow(Tables.BaseStations.BS_CODE);
            bandIndex = cellCursor.getColumnIndexOrThrow(Tables.BaseStations.BS_BAND);
            chanIndex = cellCursor.getColumnIndexOrThrow(Tables.BaseStations.BS_CHAN);
            netTypeIndex = cellCursor.getColumnIndexOrThrow(Tables.BaseStations.NET_TYPE);

            currentHigh = cellCursor.getInt(highIndex);
            currentHigh = currentHigh == -1 ? 0 : currentHigh;	//server treats zero an invalid

            currentMid = cellCursor.getInt(midIndex);
            currentMid = currentMid == -1 ? 0 : currentMid;	//server treats zero as invalid

            currentLow = cellCursor.getInt(lowIndex);
            currentLow = currentLow == -1 ? 0 : currentLow;	//server treats zero as invalid

            currentCode = cellCursor.getInt(codeIndex);
            currentCode = currentCode == -1 ? 0 : currentCode;	//server treats zero as invalid

            currentBand = cellCursor.getInt(bandIndex);
            currentBand = currentBand == -1 ? 0 : currentBand;	//server treats zero as invalid

            currentChan = cellCursor.getInt(chanIndex);
            currentChan = currentChan == -1 ? 0 : currentChan;	//server treats zero as invalid

            high = currentHigh;
            mid = currentMid;
            low = currentLow;
            code = currentCode;
            band = currentBand;
            chan = currentChan;
            netType = cellCursor.getString(netTypeIndex);

        }
    }
    public void addSample (EvtSample sample, EventObj event)
    {
        if (event.getEventType() == EventType.MAN_PLOTTING && sample.lat == 0)
            return;
        // don't add sample if its the same
        if (samples.size() > 0)
        {
            EvtSample lastSmp = (samples.get(samples.size()-1));

            if (lastSmp.lat == sample.lat && lastSmp.lng == sample.lng && lastSmp.acc < 0 && sample.acc < 0)
                return;
            if (lastSmp.lat == sample.lat && lastSmp.lng == sample.lng && lastSmp.sig == sample.sig && lastSmp.acc == sample.acc
                    && lastSmp.cov == sample.cov)
            {
                if (lastSmp.layer3 != null && lastSmp.layer3.length() > 1 && sample.layer3 != null && sample.layer3.length() > 2)
                {
                    String[] lparts0 = lastSmp.layer3.split(",");
                    String[] lparts1 = sample.layer3.split(",");
                    if (lparts0.length > 0 && lparts1.length > 0 && lparts0[0].equals(lparts1[0]))
                    {
                        if (lparts0.length > 1 && lparts1.length > 1 && lparts0.length == lparts1.length && lparts0[1].equals(lparts1[1]))
                            return;
                        if (lparts0.length == 1 && lparts1.length == 1)
                            return;
                    }
                    //else if (lparts0.length <= 1)
                    //	MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "addSample", "problem: lastSmp.layer3=" + lastSmp.layer3 + "   sample.layer3=" + sample.layer3);
                }
                else
                    return;
            }

        }
        samples.add(sample);
    }
}

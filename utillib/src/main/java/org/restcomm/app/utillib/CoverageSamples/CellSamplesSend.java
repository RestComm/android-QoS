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

/**
 * Created by bscheurman on 16-03-21.
 */

public class CellSamplesSend
{
    protected String cell = "";
    public String samples = "";
    public CellSamplesSend (CellSamples cellsamp)
    {
        cell = cellsamp.high + "," + cellsamp.mid + "," + cellsamp.low;
        if (cellsamp.code > 0)
            cell += "," + cellsamp.code;
        else
            cell += ",";
        if (cellsamp.band > 0)
            cell += "," + cellsamp.band;
        else
            cell += ",";
        if (cellsamp.chan > 0)
            cell += "," + cellsamp.chan;
        else
            cell += ",";

        int i;
        for (i=0; i<cellsamp.samples.size(); i++)
            addSample (cellsamp.samples.get(i));
    }
    public void addSample (EvtSample sample)
    {
        samples += sample.toString();
    }
}


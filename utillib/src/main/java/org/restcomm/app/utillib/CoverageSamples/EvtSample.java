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

import java.util.Locale;

/**
 * Created by bscheurman on 16-03-21.
 */
public class EvtSample {
    public int sig = 0, cov = 0, sec = -999;
    public String layer3 = "";
    public int lat = 0, lng = 0;
    public int acc, sats;
    public EvtSample () {}
    public EvtSample (EvtSample sample)
    {
        if (sample != null)
        {
            sig = sample.sig; cov=sample.cov; lat = sample.lat; lng = sample.lng;
            acc = sample.acc; layer3 = sample.layer3;
            sats = sample.sats;
        }
    }
    public String toString ()
    {
        return String.format(Locale.US,"%d,%d,%d,%d,%d,%d,%d,%s ", sec,cov,lat,lng,acc,sig,sats,layer3);
    }
}

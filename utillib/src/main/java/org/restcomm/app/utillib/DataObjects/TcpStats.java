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

package org.restcomm.app.utillib.DataObjects;

import java.io.RandomAccessFile;

/**
 * Created by bscheurman on 16-06-20.
 */
public class TcpStats
{
    public int tcpResets = -1, tcpErrors = -1, tcpRetrans = -1;
    public int tcpIn = -1, tcpOut = -1;

    public int prevResets = -1, prevErrors = -1, prevRetrans = -1;
    public int prevIn = -1, prevOut = -1;

    public int numResets = 0, numErrors = 0, numRetrans = 0;
    public int numIn = 0, numOut = 0;

    public void updateCounts ()
    {
        if (prevResets > 0)
            numResets += tcpResets - prevResets;
        if (prevErrors > 0)
            numErrors += tcpErrors - prevErrors;

        if (prevRetrans > 0)
            numRetrans += tcpRetrans - prevRetrans;
        if (prevIn > 0)
            numIn += tcpIn - prevIn;
        if (prevOut > 0)
            numOut += tcpOut - prevOut;
    }

    // Read TCP stats on packet segments and errors from linux file /proc/net/snmp
    public float readTcpStats(boolean reset) {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/net/snmp", "r");
            // for network stats: cat /proc/net/netstat
            String line = reader.readLine();
            String[] header = null;
            String[] vals = null;
            while (line != null)
            {
                if (line.indexOf("Tcp:") == 0)
                {
                    header = line.split(" ");
                    //LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "StatsManager", "readTcpStats header: ", line);
                    line = reader.readLine();
                    vals = line.split(" ");
                    //LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "StatsManager", "readTcpStats values: ", line);
                    break;
                }
                line = reader.readLine();
            }
            prevRetrans = tcpRetrans;
            prevErrors = tcpErrors;
            prevResets = tcpResets;
            prevIn = tcpIn;
            prevOut =  tcpOut;

            if (header != null)
            {
                for (int h=0; h<header.length; h++)
                {
                    try {
                        if (header[h].equals("RetransSegs"))
                            tcpRetrans = Integer.valueOf(vals[h]);
                        else if (header[h].equals("InErrs"))
                            tcpErrors = Integer.valueOf(vals[h]);
                        else if (header[h].equals("OutRsts"))
                            tcpResets = Integer.valueOf(vals[h]);
                        else if (header[h].equals("InSegs"))
                            tcpIn = Integer.valueOf(vals[h]);
                        else if (header[h].equals("OutSegs"))
                            tcpOut = Integer.valueOf(vals[h]);
                    }
                    catch (Exception e) {}
                }
            }
            if (reset)
            {
                prevRetrans = tcpRetrans;
                prevErrors = tcpErrors;
                prevResets = tcpResets;
                prevIn = tcpIn;
                prevOut = tcpOut;
            }
            else
                updateCounts();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }
}
package com.cortxt.app.utillib.DataObjects;

import com.cortxt.app.utillib.Utils.LoggerUtil;

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
                    LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "StatsManager", "readTcpStats header: ", line);
                    line = reader.readLine();
                    vals = line.split(" ");
                    LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "StatsManager", "readTcpStats values: ", line);
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
package com.cortxt.app.mmcutility.CoverageSamples;

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


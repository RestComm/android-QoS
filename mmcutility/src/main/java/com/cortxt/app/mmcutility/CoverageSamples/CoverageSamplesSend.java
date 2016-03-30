package com.cortxt.app.mmcutility.CoverageSamples;

import com.cortxt.app.mmcutility.DataObjects.CoverageSamples;

import java.util.ArrayList;

/**
 * Created by bscheurman on 16-03-21.
 */
public class CoverageSamplesSend {
    private long startTime = 0l;
    private String aheader;
    public ArrayList<CellSamplesSend> cells = new ArrayList<CellSamplesSend>();
    public CoverageSamplesSend (CoverageSamples cov)
    {
        int i,j;
        for (i=0; i<cov.cells.size(); i++)
            cells.add(new CellSamplesSend(cov.cells.get(i)));
        startTime = cov.startTime;
        aheader = cov.aheader;
    }

    public long getStartTime () {return startTime;}
    public void setStartTime (long value) { startTime = value;}
    public String getHeader () {return aheader;}
    public void setHeader (String value) { aheader = value;}
}

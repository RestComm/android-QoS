package com.cortxt.app.mmcutility.DataObjects;

import com.cortxt.app.mmcutility.CoverageSamples.CellSamples;

import java.util.List;

/**
 * Created by bscheurman on 16-03-21.
 */
public class CoverageSamples {
    public long startTime = 0l;
    public String aheader;
    public List<CellSamples> cells;

    public long getStartTime () {return startTime;}
    public void setStartTime (long value) { startTime = value;}
    public String getHeader () {return aheader;}
    public void setHeader (String value) { aheader = value;}

}

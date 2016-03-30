package com.cortxt.app.mmcutility.CoverageSamples;

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

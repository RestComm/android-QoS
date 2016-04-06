package com.cortxt.com.mmcextension;

import android.content.Context;
import android.database.Cursor;
import android.telephony.TelephonyManager;

import com.cortxt.app.utillib.DataObjects.PhoneState;
import com.cortxt.app.utillib.ICallbacks;
import com.cortxt.app.utillib.Reporters.ReportManager;
import com.cortxt.app.utillib.Utils.Global;
import com.cortxt.app.utillib.Utils.TimeDataPoint;
import com.cortxt.app.utillib.Utils.TimeSeries;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by bscheurman on 16-03-08.
 */
public class PhoneHeuristic {

    public PhoneHeuristic (ICallbacks callback, PhoneState phoneState)
    {
    }

    public int heuristicDropped (PhoneState phoneState)
    {
       return 0;
    }
}

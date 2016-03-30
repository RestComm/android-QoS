package com.cortxt.com.mmcextension;

import android.content.Context;
import android.database.Cursor;
import android.telephony.TelephonyManager;

import com.cortxt.app.mmcutility.DataObjects.PhoneState;
import com.cortxt.app.mmcutility.ICallbacks;
import com.cortxt.app.mmcutility.Reporters.ReportManager;
import com.cortxt.app.mmcutility.Utils.Global;
import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.Utils.TimeDataPoint;
import com.cortxt.app.mmcutility.Utils.TimeSeries;

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

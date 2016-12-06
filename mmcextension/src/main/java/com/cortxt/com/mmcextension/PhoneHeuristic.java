package com.cortxt.com.mmcextension;

import com.restcomm.app.utillib.DataObjects.PhoneState;
import com.restcomm.app.utillib.ICallbacks;

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

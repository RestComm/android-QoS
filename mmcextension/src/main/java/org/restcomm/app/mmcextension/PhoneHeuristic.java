package org.restcomm.app.mmcextension;

import org.restcomm.app.utillib.DataObjects.PhoneState;
import org.restcomm.app.utillib.ICallbacks;

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

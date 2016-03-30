package com.cortxt.com.mmcextension;

import android.location.Location;
import android.telephony.CellLocation;

import com.cortxt.app.mmcutility.DataObjects.EventObj;
import com.cortxt.app.mmcutility.DataObjects.PhoneState;
import com.cortxt.app.mmcutility.ICallbacks;

public class TravelDetector {

	public ICallbacks owner;
	//UsageLimits _limits;

	public TravelDetector (ICallbacks mmc)
	{  
	}
	/* 
	 * Cleanup the travel detector 
	 * Stop alarms, release wake lock, etc
	 */
	public void stop ()
	{

	}

	public void detectTravellingFromCellId (int phoneType, CellLocation cellInfo, CellLocation lastCell, PhoneState phoneState) {
	}

    /* 
	 * Check the travel detection preferences, and set timers accordingly
	 * return true if the scanning mode preference changed to a new value
	 */
	public boolean updateTravelPreference ()
	{
		return true;
	}



	
	public void eventCompleted (EventObj event)
	{
	}

	// Called every 3 minutes or 10 minutes by the minute alarm manager (depends if travelling or not) 
	// When in travelling mode, this will periodically trigger a 'travel-check' which gets a gps location and checks for fill-in
	// remember the other purpose for the 1 minute alarm manager is to get base-station updates while sleeping to allow detecting travel in the 1st place
	public void triggerTravelCheck(){

	}

	public boolean isTravelling ()
    {
        return false;
    }
	

	// Cancel travelling if the first fix on the first travel event is near the last known location
	// Called from GpsListenerForEvent.onLocationUpdate, 
	// so that a periscope event is cancelled immediately on first fix if there hasnt been movement
	public boolean confirmTravelling (Location location)
	{
		return false;
	}

}

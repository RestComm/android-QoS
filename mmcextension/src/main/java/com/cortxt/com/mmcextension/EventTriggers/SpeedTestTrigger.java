package com.cortxt.com.mmcextension.EventTriggers;

import android.os.Handler;

import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.cortxt.app.mmcutility.ICallbacks;

public class SpeedTestTrigger {
	public static final String TAG = SpeedTestTrigger.class.getSimpleName();

	public void setCarrierLatencyUrl(String newLatencyUrl) {
	}
	
	public void setCarrierDownloadUrl(String newDownloadUrl) {
	}
	
	public void setCarrierUploadUrl(String newUploadUrl) {
	}

	
	public SpeedTestTrigger(ICallbacks ownercb, Handler handler) {
	}
	
	public void stop() {
	}
	
	public void killTest ()
	{
	}
	/**
	 * Runs a speed test in a background thread and reports the results if network is not wifi
	 * @param updateUI whether or not to update the UI about progress and results
	 */
	public void runTest(boolean updateUI, int trigger, EventType testType) {
	}

	public void handleSMSDeliveryNotification(Long deliveryTime, int smsID)
	{
	}
	public void  handleSMSReply(String smsID, String smsVal, String smsServerSentTime, String deliveredTime)
	{
	}

}




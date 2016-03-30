package com.cortxt.com.mmcextension.datamonitor;

import android.os.Handler;

import com.cortxt.app.mmcutility.ICallbacks;

public class AppDataStatisticsRunnable implements Runnable {
	
	public int hasDataActivity = 0;
	public static final String TAG = AppDataStatisticsRunnable.class.getSimpleName();
	
	public AppDataStatisticsRunnable(ICallbacks newOwner, Handler newHandler) {
	}
	

	public void init(long newMobileRxBytes, long newMobileTxBytes, boolean reset) {
	}
		
	@Override	
	public void run() {

	}
	
	public boolean finish() {
		return false;
	}	
}

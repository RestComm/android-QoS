package com.cortxt.com.mmcextension.datamonitor;

import android.os.Handler;

import com.restcomm.app.utillib.ICallbacks;

public class AppDataStatisticsRunnable implements Runnable {
	
	public int hasDataActivity = 0;
	public static final String TAG = AppDataStatisticsRunnable.class.getSimpleName();
	
	public AppDataStatisticsRunnable(ICallbacks newOwner, Handler newHandler, StatsManager statsManager) {
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

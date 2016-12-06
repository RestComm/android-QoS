package com.restcomm.app.qoslib;

import com.restcomm.app.utillib.Reporters.ReportManager;
import com.restcomm.app.utillib.Utils.Global;
import com.restcomm.app.utillib.Utils.LoggerUtil;
import com.restcomm.app.utillib.Utils.PreferenceKeys;
import com.securepreferences.SecurePreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class BootListener extends BroadcastReceiver {
	public static final String PREFERENCE_KEY_START_ON_BOOT = "KEY_SETTINGS_START_ON_BOOT";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
//		if(intent.getAction().equals(Intent.ACTION_SEND) ) {
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "BootListener", "ACTION_BOOT_COMPLETED", "");
			
			boolean startOnBoot = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREFERENCE_KEY_START_ON_BOOT, true);
			boolean isAuthorized = ReportManager.getInstance(context.getApplicationContext()).isAuthorized();
            SecurePreferences securePrefs = MainService.getSecurePreferences(context);
			boolean bStoppedService = securePrefs.getBoolean(PreferenceKeys.Miscellaneous.STOPPED_SERVICE, false);
			String yeilded = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceKeys.Miscellaneous.YEILDED_SERVICE, null);
			if (Global.isServiceYeilded(context))
				bStoppedService = true;

			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "BootListener", "startOnBoot=" + startOnBoot + ",isAuthorized=" + isAuthorized + ",bStoppedService=" + bStoppedService, "");
			
			if (!bStoppedService)
				if(isAuthorized && startOnBoot && !bStoppedService) {	
					LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "BootListener", "startService=" + startOnBoot, "");
					
					Intent bgServiceIntent = new Intent(context, MainService.class);
					context.startService(bgServiceIntent);
				}
		}
	}

}

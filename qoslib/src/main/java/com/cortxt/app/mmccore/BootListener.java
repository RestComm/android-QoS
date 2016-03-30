package com.cortxt.app.mmccore;

import com.cortxt.app.mmcutility.Reporters.ReportManager;
import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.Utils.PreferenceKeys;
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
			MMCLogger.logToFile(MMCLogger.Level.DEBUG, "BootListener", "ACTION_BOOT_COMPLETED", "");
			
			boolean startOnBoot = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREFERENCE_KEY_START_ON_BOOT, true);
			boolean isAuthorized = ReportManager.getInstance(context.getApplicationContext()).isAuthorized();
            SecurePreferences securePrefs = MMCService.getSecurePreferences (context);
			boolean bStoppedService = securePrefs.getBoolean(PreferenceKeys.Miscellaneous.STOPPED_SERVICE, false);
			
			MMCLogger.logToFile(MMCLogger.Level.DEBUG, "BootListener", "startOnBoot="+startOnBoot+",isAuthorized="+isAuthorized+",bStoppedService="+bStoppedService, "");
			
			if (!bStoppedService)
				if(isAuthorized && startOnBoot && !bStoppedService) {	
					MMCLogger.logToFile(MMCLogger.Level.DEBUG, "BootListener", "startService="+startOnBoot, "");
					
					Intent bgServiceIntent = new Intent(context, MMCService.class);
					context.startService(bgServiceIntent);
				}
		}
	}

}

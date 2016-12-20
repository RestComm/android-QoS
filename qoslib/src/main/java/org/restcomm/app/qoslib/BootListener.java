/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * For questions related to commercial use licensing, please contact sales@telestax.com.
 *
 */

package org.restcomm.app.qoslib;

import org.restcomm.app.utillib.Reporters.ReportManager;
import org.restcomm.app.utillib.Utils.Global;
import org.restcomm.app.utillib.Utils.LoggerUtil;
import org.restcomm.app.utillib.Utils.PreferenceKeys;
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

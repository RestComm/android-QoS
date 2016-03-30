package com.cortxt.app.mmcutility.Utils;

import java.util.Calendar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.preference.PreferenceManager;

import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.cortxt.app.mmcutility.ICallbacks;
import com.securepreferences.SecurePreferences;

public class UsageLimits  implements OnSharedPreferenceChangeListener{

	private int _usageProfile = -1; // users chosen level of monitoring 0=min, 1=balanced, 2=max
	private int _usageProfileCharger = -1; // users chosen level of monitoring 0=min, 1=balanced, 2=max
	private int _usageLimit = 10;
	private int _prevUsageProfile = 0;
	public int _fillinsPer3hr = 7, _gpsFailsPer3hr = 6, _gpsAttemptsPer3hr = 16;
	public int _consecFillinLimit = 2, _consecFillinCount = 0;
	public int _fillinAccuracy = 60;
	public int _batteryLimit = 40;
	
	public int _countFillins = 0;
	public int _countGpsFails = 0, _countGpsAttempts = 0;
	private int _countSleepHandoffs = 0;
	private int _coverageFillSamples = 0;
	private int _speedtestsPerDay = 8, _speedTestMBperMonth = 200;
	private int _bytesSpeedDay = _speedTestMBperMonth/12;
	private int _countSpeedDay = 0, _bytesSpeedMonth = 0, _speedDay = 0, _speedMonth = 0;
	private long _lastSpeedTest = 0;
	private ICallbacks owner= null;

	private int TRAVEL_PERIOD = 10;
	private int WAKEUP_PERIOD = 3;
	private int _detectionLevel = 0; // level of interrogation needed to persuade phone to reveal its cellid changes
	private boolean _userChangedTravel = true, _travelEnable = true;
	private boolean _useRadioLog =  true;
	private boolean _speedTestEnable = false;
	private int _dormantMode = 0;
	public static final int MINIMAL = 0;
	public static final int BALANCED = 1;
	public static final int MAXIMUM = 2;
	public static final String TAG = UsageLimits.class.getSimpleName();
	
	public UsageLimits (ICallbacks _owner)
	{
		owner = _owner;	
		
		boolean readLogPermission = false; //PreferenceManager.getDefaultSharedPreferences(owner).getBoolean(PreferenceKeys.Miscellaneous.READ_LOG_PERMISSION, false);
		String pname = owner.getContext().getPackageName();
		int permissionForReadLogs = owner.getContext().getPackageManager().checkPermission(android.Manifest.permission.READ_LOGS, pname); //0 means allowed
		int permissionForPrecise = owner.getContext().getPackageManager().checkPermission("android.permission.READ_PRECISE_PHONE_STATE", pname); // 0 means allowed
		
		if (permissionForReadLogs == 0)
			readLogPermission = true;
		//final String pname = getPackageName();
		//if (getPackageManager().checkPermission(android.Manifest.permission.READ_LOGS, pname) == 0)
		
		if (Build.VERSION.SDK_INT >= 16 && readLogPermission == false)
			_useRadioLog = false;
		if (permissionForPrecise == 0)  // But if we have permission for PRECISE_CALL_STATE, dont use logcat, this is better
			_useRadioLog = false;
		updateTravelPreference ();
		PreferenceManager.getDefaultSharedPreferences(owner.getContext()).registerOnSharedPreferenceChangeListener(this);
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
    {
		if (key.equals("KEY_SETTINGS_READ_LOG_PERMISSION"))
    	{	 
			_useRadioLog = PreferenceManager.getDefaultSharedPreferences(owner.getContext())
					.getBoolean(PreferenceKeys.Miscellaneous.READ_LOG_PERMISSION, false);
    	}
		
    	if (key.equals("KEY_SETTINGS_TRAVEL_DETECT"))
    	{	 
    		if (_detectionLevel != -1)
    		{	
    			if (updateTravelPreference () && _detectionLevel != -1) // returns true if preference was changed to a new value
    			{
		    		sharedPreferences.edit().putBoolean(PreferenceKeys.Miscellaneous.CHANGED_TRAVEL, true).commit();
		    		_userChangedTravel = true;
    			}
    		}
    	}
    	
    	if (key.equals(PreferenceKeys.Miscellaneous.USAGE_PROFILE) ||
					key.equals(PreferenceKeys.Miscellaneous.USAGE_PROFILE_CHARGER) ||
					key.equals("KEY_SETTINGS_TRAVEL_ENABLE")) 
    		updateTravelPreference ();
    	
//    	if (key.equals("KEY_SETTINGS_CONTACT_EMAIL") ||
//    			key.equals("KEY_SETTINGS_SHARE_WITH_CARRIER"))
//    	{
//    		owner.sendUserUpdate();
//    	}
    }
	public void reset3hrCounts ()
	{
		_countFillins = 0;
		_countGpsFails = 0;
		_countGpsAttempts = 0;
		
	}

	public int getDormantMode ()
	{
		return _dormantMode;
	}

	public int getUsageProfile ()
	{
		if (_dormantMode > 0)
			return (-_dormantMode);
		if (DeviceInfoOld.batteryCharging == true)
			return _usageProfileCharger;
		return _usageProfile;
	}
	
	public void setLevelLimit (Integer levelLimit)
	{
		int lim = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getInt(PreferenceKeys.Miscellaneous.USAGE_LIMIT, 10);
		int dormant = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getInt(PreferenceKeys.Miscellaneous.USAGE_DORMANT_MODE, 0);
		// level limit is ignored if we're dormant
		if (levelLimit == null || dormant > 0)  // this means if server says 'dormant' once, it needs to set 'un-dormant' before it can return
			return;
		if (lim != levelLimit)
		{
			PreferenceManager.getDefaultSharedPreferences(owner.getContext()).edit().putInt(PreferenceKeys.Miscellaneous.USAGE_LIMIT, (int)levelLimit).commit();
			updateTravelPreference ();
		}
	}

	public void setDormant (Integer levelDormant)
	{
		int dormant = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getInt(PreferenceKeys.Miscellaneous.USAGE_DORMANT_MODE, 0);
		if (levelDormant == null)
			return;
		if (dormant != levelDormant)
		{
			PreferenceManager.getDefaultSharedPreferences(owner.getContext()).edit().putInt(PreferenceKeys.Miscellaneous.USAGE_DORMANT_MODE, (int)levelDormant).commit();
			//if (dormant == 0) // come out of dormant state, and use original or limited level, whichever is smaller
			updateTravelPreference ();
		}
	}
	public boolean exceededGps (EventType eventType, boolean increment)
	{
        // These type of events will always invoke the GPS
        if (eventType == EventType.EVT_DROP || eventType == EventType.EVT_CALLFAIL || eventType == EventType.MAN_PLOTTING ||
                eventType == EventType.MAN_SPEEDTEST || eventType == EventType.MAN_TRACKING || eventType == EventType.COV_UPDATE || eventType == EventType.MAN_TRANSIT ||
                eventType == EventType.VIDEO_TEST || eventType == EventType.CONNECTION_FAILED || eventType == EventType.WEBPAGE_TEST || eventType == EventType.AUDIO_TEST || eventType == EventType.SMS_TEST)
            return false;

		if (increment == true)
		{
			// only increase the gps count if gps was off and is being turned on
			//if (owner.getGpsManager().isGpsRunning() == false)
				_countGpsAttempts ++;
		}

		// But events like outages, phone calls, etc may have a limit
		if (_countGpsFails >= _gpsFailsPer3hr || _countGpsAttempts >= _gpsAttemptsPer3hr)
		{
			if (_countGpsFails >= _gpsFailsPer3hr)
				MMCLogger.logToFile(MMCLogger.Level.DEBUG, "UsageLimits", "exceededGps", "countGpsFails > gpsFailsPer3hr");
			else
				MMCLogger.logToFile(MMCLogger.Level.DEBUG, "UsageLimits", "exceededGps", "_countGpsAttempts > _gpsAttemptsPer3hr");
			return true;
		}
		else
			return false;
	}
	public void getUsageProfileSetting ()
	{
		String strSetting = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getString(PreferenceKeys.Miscellaneous.USAGE_PROFILE, "1");
		_usageProfile = Integer.parseInt(strSetting);
		
		strSetting = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getString(PreferenceKeys.Miscellaneous.USAGE_PROFILE_CHARGER, "1");
		_usageProfileCharger = Integer.parseInt(strSetting);

		_dormantMode = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getInt(PreferenceKeys.Miscellaneous.USAGE_DORMANT_MODE, 0);
		// active profile may be modified by battery charger state
		int profile = getUsageProfile();
		// Server may impose a limit on the actual level of reporting
		int limit = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getInt(PreferenceKeys.Miscellaneous.USAGE_LIMIT, 10);

		// If server sends a _dormantMode, put MMC into dormant mode
		if (_dormantMode > 0)
		{
			profile = -_dormantMode;
		}
		else {
			if (limit < profile) {
				profile = limit;
			}
		}


		if (_prevUsageProfile != profile)
		{
			if (profile <= 0)  // minimal
			{
				_travelEnable = false;
				TRAVEL_PERIOD = 60;
				_batteryLimit = 100;
				_fillinsPer3hr = 0;
				_consecFillinLimit = 0;
				_gpsFailsPer3hr = 2;
				_gpsAttemptsPer3hr = 7;
				WAKEUP_PERIOD = 100;
				_speedtestsPerDay = 2;
				_speedTestMBperMonth = 40;

			}
			else if (profile > 0)  
			{
				if (profile == 1)   // balanced
				{
					TRAVEL_PERIOD = 20;
					_batteryLimit = 40;
					_fillinsPer3hr = 4;
					_consecFillinLimit = 2;
					_gpsFailsPer3hr = 5;
					_gpsAttemptsPer3hr = 15;
					WAKEUP_PERIOD = 6;
					_coverageFillSamples = 2;
					_speedtestsPerDay = 4;
					_speedTestMBperMonth = 250;
				}
				else  					// maximum
				{
					TRAVEL_PERIOD = 10;
					_batteryLimit = 10;
					_fillinsPer3hr = 20;
					_consecFillinLimit = 20;
					_gpsFailsPer3hr = 100;
					_gpsAttemptsPer3hr = 100;
					WAKEUP_PERIOD = 2;
					_coverageFillSamples = 4;
					_speedtestsPerDay = 8;
					_speedTestMBperMonth = 500;
				}
				_travelEnable = true;
			}
			if (_dormantMode >= 100) // Stop the Service
			{
				SecurePreferences prefs = PreferenceKeys.getSecurePreferences (owner.getContext());
				prefs.edit().putBoolean(PreferenceKeys.Miscellaneous.STOPPED_SERVICE, true).commit();
				// Restart will allow any current events to complete, service will stop, and will not restart because STOPPED_SERVICE=true
				Intent intent = new Intent(CommonIntentActionsOld.RESTART_MMC_SERVICE);
				owner.getContext().sendBroadcast(intent);
			}
			else if (_dormantMode >= 1) {
				Global.UPDATE_PERIOD  = (24 * 3600 * 1000); // check-in once per day
				if (_dormantMode > 1)
					Global.UPDATE_PERIOD = (0); // check-in never
				_speedtestsPerDay = 0;
				_speedTestMBperMonth = 0;
				owner.setAlarmManager();
				owner.manageDataMonitor (0,300);  // disable the data monitor
			}
			else if (_prevUsageProfile < 0)  // if we were dormant but no longer dormant, go back to 3hr intervals
			{
				Global.UPDATE_PERIOD = (3 * 3600 * 1000);
				owner.setAlarmManager();
				owner.manageDataMonitor (-1,-1);  // re-enable the data monitor
			}
			_prevUsageProfile = profile;
		}
		
	}
	
	public int handleCheckin (boolean reset)
	{
		int count = _countSleepHandoffs;
		
		if (reset)
		{
			_countSleepHandoffs = 0;
			reset3hrCounts();
			// Travel detector may have to re-enable the alarm manager if it was stopped due to exceeding gps limit
			owner.updateTravelPreference ();
		}
		
		return count;
	}
	/* 
	 * Check the travel detection preferences, and set timers accordingly
	 * return true if the scanning mode preference changed to a new value
	 */
	public boolean updateTravelPreference ()
	{
		String strSetting = "";
		
		strSetting = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getString(PreferenceKeys.Miscellaneous.TRAVEL_DETECT, "0");
		int prevLevel = _detectionLevel;
		_detectionLevel = Integer.parseInt(strSetting);
		if (MMCLogger.isDebuggable())
			_userChangedTravel = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getBoolean(PreferenceKeys.Miscellaneous.CHANGED_TRAVEL, false);
		
		//_travelEnable = PreferenceManager.getDefaultSharedPreferences(owner).getBoolean(PreferenceKeys.Miscellaneous.TRAVEL_ENABLE, true);
		getUsageProfileSetting ();
		//owner.changeWakeType (getUsageProfile());
		
		owner.updateTravelPreference();
		
		if (prevLevel == _detectionLevel)
			return false;
		return true;
		
	}
	


	public void setUseRadioLog (boolean bUseRadioLog)
	{
		_useRadioLog = bUseRadioLog;
	}
	
	public int getTravelDetectionLevel ()
	{
		return _detectionLevel;
	}
	
	public void setTravelDetectionLevel (int level)
	{
		_detectionLevel = level;
	}
	
	public int getTravelWakeupPeriod ()
	{
		return WAKEUP_PERIOD;
	}
	public int getTravelPeriod ()
	{
		return TRAVEL_PERIOD;
	}
	public boolean getTravelEnabled ()
	{
		return _travelEnable;
	}
	public int getCoverageFillSamples ()
	{
		return _coverageFillSamples;
	}
	public boolean userChangedTravel ()
	{
		return _userChangedTravel;
	}
	
	public void addSleepHandoff ()
	{
		_countSleepHandoffs ++;
	}
	public void addGpsFails ()
	{
		_countGpsFails ++;
	}
	public void addVideoTest ()
	{
		
	}
	public void addSpeedTest (int bytes)
	{
		// These settings need to be read and stored to persistent storage to be accurate across restarts
		if (_lastSpeedTest == 0)
		{
			_lastSpeedTest = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getLong(PreferenceKeys.Miscellaneous.LAST_SPEEDTEST, 0l);
			_countSpeedDay = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getInt(PreferenceKeys.Miscellaneous.SPEED_DAY_COUNT, 0);
			_bytesSpeedMonth = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getInt(PreferenceKeys.Miscellaneous.SPEED_MONTH_BYTES, 0);
			_speedDay = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getInt(PreferenceKeys.Miscellaneous.SPEED_DAY, 0);
			_speedMonth = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getInt(PreferenceKeys.Miscellaneous.SPEED_MONTH, 0);
		}
		_lastSpeedTest = System.currentTimeMillis();
		Calendar now = Calendar.getInstance();
		int day = now.get(Calendar.DATE);
		int month = now.get(Calendar.MONTH);
		if (day == _speedDay) {
			_countSpeedDay ++;
			_bytesSpeedDay += bytes;	
		}
		else
		{
			_speedDay = day;
			_countSpeedDay = 0;
		}
		if (month == _speedMonth)
			_bytesSpeedMonth += bytes;
		else
		{
			_speedMonth = month;
			_bytesSpeedMonth = 0;
		}
		
		PreferenceManager.getDefaultSharedPreferences(owner.getContext()).edit().putLong(PreferenceKeys.Miscellaneous.LAST_SPEEDTEST, _lastSpeedTest).commit();
		PreferenceManager.getDefaultSharedPreferences(owner.getContext()).edit().putInt(PreferenceKeys.Miscellaneous.SPEED_DAY_COUNT, _countSpeedDay).commit();
		PreferenceManager.getDefaultSharedPreferences(owner.getContext()).edit().putInt(PreferenceKeys.Miscellaneous.SPEED_MONTH_BYTES, _bytesSpeedMonth).commit();
		PreferenceManager.getDefaultSharedPreferences(owner.getContext()).edit().putInt(PreferenceKeys.Miscellaneous.SPEED_DAY_BYTES, _bytesSpeedDay).commit();
		PreferenceManager.getDefaultSharedPreferences(owner.getContext()).edit().putInt(PreferenceKeys.Miscellaneous.SPEED_DAY, _speedDay).commit();
		PreferenceManager.getDefaultSharedPreferences(owner.getContext()).edit().putInt(PreferenceKeys.Miscellaneous.SPEED_MONTH, _speedMonth).commit();
	}
	
	// determine if phone can allow an auto speed test, or if its over its limits
	public int allowSpeedTest (int bytes)
	{
		int svrEnabled = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getInt(PreferenceKeys.Miscellaneous.AUTOSPEED_SVR_ENABLE, -1);
        int svrSizeMB = -1;
        if (svrEnabled == 1)
        {
            svrSizeMB = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getInt(PreferenceKeys.Miscellaneous.AUTOSPEED_SVR_SIZEMB, 50);

            PreferenceManager.getDefaultSharedPreferences(owner.getContext()).edit().putBoolean(PreferenceKeys.Miscellaneous.AUTOSPEED_ENABLE, true).commit();
            PreferenceManager.getDefaultSharedPreferences(owner.getContext()).edit().putString(PreferenceKeys.Miscellaneous.AUTOSPEED_SIZEMB, String.valueOf(svrSizeMB)).commit();
        }
        boolean bEnabled = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getBoolean(PreferenceKeys.Miscellaneous.AUTOSPEED_ENABLE, false);
        if (bEnabled == false)
			return 0;
		// These settings need to be read and stored to persistent storage to be accurate across restarts
		if (_lastSpeedTest == 0)
		{
			_lastSpeedTest = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getLong(PreferenceKeys.Miscellaneous.LAST_SPEEDTEST, 0l);
			_countSpeedDay = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getInt(PreferenceKeys.Miscellaneous.SPEED_DAY_COUNT, 0);
			_bytesSpeedMonth = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getInt(PreferenceKeys.Miscellaneous.SPEED_MONTH_BYTES, 0);
			_speedDay = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getInt(PreferenceKeys.Miscellaneous.SPEED_DAY, 0);
			_speedMonth = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getInt(PreferenceKeys.Miscellaneous.SPEED_MONTH, 0);
			_bytesSpeedDay = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getInt(PreferenceKeys.Miscellaneous.SPEED_DAY_BYTES, 0);
		}
		Calendar now = Calendar.getInstance();
		int day = now.get(Calendar.DATE);
		int month = now.get(Calendar.MONTH); //if new month/day reset limits
		if (day != _speedDay)
		{
			_countSpeedDay = 0;
			_speedDay = day;
			_bytesSpeedDay = 0;
		}
		if (month != _speedMonth)
		{
			_bytesSpeedMonth = 0;
			_speedMonth = month;
		}
 		boolean bChangedMB = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getBoolean(PreferenceKeys.Miscellaneous.AUTOSPEED_MB_CHANGED, false);
		String mb_str = PreferenceManager.getDefaultSharedPreferences(owner.getContext()).getString(PreferenceKeys.Miscellaneous.AUTOSPEED_SIZEMB, "50");
        int mb = Integer.parseInt(mb_str);
        if (svrSizeMB >=0)
        {
            mb = svrSizeMB; // override if server sets a value
            bChangedMB =true;
        }
        int allowedMB = _speedTestMBperMonth;
		if (bChangedMB)
			allowedMB = mb;
		else
			PreferenceManager.getDefaultSharedPreferences(owner.getContext()).edit().putString(PreferenceKeys.Miscellaneous.AUTOSPEED_SIZEMB, String.valueOf(_speedTestMBperMonth)).commit();
		
		now.setTimeInMillis(_lastSpeedTest);
		String lastTest = now.toString();
		String log = String.format("Speed Test Limits: dayCount=%d, monthBytes=%d", _countSpeedDay, _bytesSpeedMonth);
		MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "allowSpeedTest", log);
		if (_countSpeedDay >= _speedtestsPerDay || _bytesSpeedMonth >= allowedMB * 1000000 
				|| _bytesSpeedDay >= allowedMB*1000000/12)
		{
			MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "allowSpeedTest", "Not Allowed");
			return 0;
		}
		return 1;
	}
}

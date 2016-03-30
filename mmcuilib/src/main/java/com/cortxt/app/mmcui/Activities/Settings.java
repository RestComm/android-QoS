package com.cortxt.app.mmcui.Activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcui.utils.ScalingUtility;
import com.cortxt.app.mmcutility.DataObjects.MMCDevice;
import com.cortxt.app.mmcutility.Reporters.ReportManager;
import com.cortxt.app.mmcutility.Reporters.WebReporter.ServerUpdateRequest;
import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.Utils.PreferenceKeys;
import com.cortxt.com.mmcextension.MMCSystemUtil;

/**
 * @author brad
 *
 */

public class Settings extends PreferenceActivity {
	private static final String PREFERENCE_KEY_SHARE_WITH_CARRIER = "KEY_SETTINGS_SHARE_WITH_CARRIER";
	private static final String PREFERENCE_KEY_CONTACT_EMAIL = "KEY_SETTINGS_CONTACT_EMAIL";
	private static final String PREFERENCE_KEY_TWITTER = "KEY_SETTINGS_TWITTER";
	
	private PreferenceChangeListener mPreferenceChangeListener;
	private long lastChange = 0;
	private static ListPreference listPreference;
	private static ListPreference listPreferenceCharger;
	public static CheckBoxPreference svcmodeActive;
	private static CheckBoxPreference svcmodeCalls;
    private static PreferenceScreen preferenceScreen;
	private static PreferenceScreen svcmodeScreen;
    private static int svrEnabled = -1, svrSizeMB = -1;
    private static boolean sendOnWifi = false, serviceModeEnabled = false;
	private static boolean allowSvc = true, allowGcm = false;
	private static String phonetype = "";
	private static Settings settings;
	private Handler handler;
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.MyTheme);
		super.onCreate(savedInstanceState);

		/* force this activity to use portrait layout for phones and landscape layout for 
		 * 'large' and 'sw600dp' devices */
		if(getResources().getBoolean(R.bool.isphone)) { 
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		settings = this;
		handler = new Handler();
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View contentView = inflater.inflate(R.layout.settings, null);
		ScalingUtility.getInstance(this).scaleView(contentView);
		setContentView(contentView);
        MMCActivity.customizeTitleBar(this, contentView, R.string.dashboard_settings, R.string.dashcustom_settings);
		phonetype = ReportManager.getInstance(this).getDevice().getManufacturer();

		//allowSvc = this.getResources().getBoolean(R.bool.ALLOW_SVCMODE);
		allowSvc = true;
		int useSvcMode = PreferenceManager.getDefaultSharedPreferences(Settings.this).getInt(PreferenceKeys.Miscellaneous.USE_SVCMODE, 0);
		if (useSvcMode != 1)
			allowSvc = false;

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


            mPreferenceChangeListener = new PreferenceChangeListener();
		sharedPreferences.registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);

		boolean svcEnabled = MMCSystemUtil.isServiceModeEnabled();
		if (svcEnabled == false) {
			MMCLogger.logToFile(MMCLogger.Level.DEBUG, "Settings", "onCreate", "mmcsys not found, unchecking service mode");
			PreferenceManager.getDefaultSharedPreferences(Settings.this).edit().putBoolean("KEY_SETTINGS_SVCMODE", false).commit();
		}
		boolean defaultWifi = false;
		String noConfirm = (getResources().getString(R.string.WIFI_DEFAULT));
		if (noConfirm.equals("1"))
			defaultWifi = true;  // don't even allow confirmation buttons on drilled down event
		sendOnWifi = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceKeys.Miscellaneous.SEND_ON_WIFI, defaultWifi);
        svrEnabled = sharedPreferences.getInt(PreferenceKeys.Miscellaneous.AUTOSPEED_SVR_ENABLE, -1);
        svrSizeMB = sharedPreferences.getInt(PreferenceKeys.Miscellaneous.AUTOSPEED_SVR_SIZEMB, -1);


        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            addPreferencesFromResource(R.xml.preferences);
	        
            listPreference = (ListPreference)findPreference(PreferenceKeys.Miscellaneous.USAGE_PROFILE);
            listPreferenceCharger = (ListPreference)findPreference(PreferenceKeys.Miscellaneous.USAGE_PROFILE_CHARGER);
            preferenceScreen = (PreferenceScreen)findPreference("KEY_SETTINGS_USAGE_GROUP");
            
	   		// Hide preference meant for debugging
	   		if (!MMCLogger.isDebuggable()) {
	   			ListPreference mListPref = (ListPreference) findPreference(PreferenceKeys.Miscellaneous.TRAVEL_DETECT); 
	   			this.getPreferenceScreen().removePreference(mListPref); 
	   		}
            if (svrEnabled != -1)
            {
                CheckBoxPreference mPref = (CheckBoxPreference) findPreference(PreferenceKeys.Miscellaneous.AUTOSPEED_ENABLE);
                this.getPreferenceScreen().removePreference(mPref);
                if (svrSizeMB != -1)
                {
                    EditTextPreference mTextPref = (EditTextPreference) findPreference(PreferenceKeys.Miscellaneous.AUTOSPEED_SIZEMB);
                    this.getPreferenceScreen().removePreference(mTextPref);
                }
            }
	   		CheckBoxPreference wifiPref = (CheckBoxPreference) findPreference(PreferenceKeys.Miscellaneous.SEND_ON_WIFI); 
			wifiPref.setDefaultValue(defaultWifi);
        } else {        	

        	getFragmentManager().beginTransaction().replace(R.id.content, new PrefsFragment()).commit();
        }

		String registrationId = sharedPreferences.getString(PreferenceKeys.Miscellaneous.KEY_GCM_REG_ID, "");
		if (!registrationId.equals(""))
			allowGcm = true;

        
        setUsageDescription (PreferenceKeys.Miscellaneous.USAGE_PROFILE);
		setUsageDescription(PreferenceKeys.Miscellaneous.USAGE_PROFILE_CHARGER);
		setUsageGroupDescription();

	}

	public void onExit(View view) {
		this.finish();
	}
	
    @TargetApi(14)
    static public class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            addPreferencesFromResource(R.xml.preferences_v14);
			svcmodeScreen = (PreferenceScreen)findPreference("KEY_SETTINGS_SERVICEMODE");

			serviceModeEnabled = true;
			SwitchPreference rootSwitch = (SwitchPreference) findPreference(PreferenceKeys.Miscellaneous.ROOT_ACCESS_SWITCH);

			//this.getPreferenceScreen().removePreference(rootSwitch);
            if(MMCDevice.isDeviceRooted(settings) == false){
    	   		// Hide preference meant for rooted phones    	   		
    	   		//SwitchPreference rootSwitch = (SwitchPreference) findPreference(PreferenceKeys.Miscellaneous.ROOT_ACCESS_SWITCH);
    	   		//this.getPreferenceScreen().removePreference(rootSwitch);
				this.getPreferenceScreen().removePreference(svcmodeScreen);
				serviceModeEnabled = false;
			}
			else
			{
				rootSwitch.setChecked(true);
				if (allowSvc == false || !phonetype.toLowerCase().equals("samsung") || Build.VERSION.SDK_INT < 17)
				{
					this.getPreferenceScreen().removePreference(svcmodeScreen);
					serviceModeEnabled = false;
				}
			}

			svcmodeActive = (CheckBoxPreference)findPreference("KEY_SETTINGS_SVCMODE");
			//svcmodeCalls = (CheckBoxPreference)findPreference("KEY_SETTINGS_SVCMODE_PHONECALLS");

            listPreference = (ListPreference)findPreference(PreferenceKeys.Miscellaneous.USAGE_PROFILE);
            listPreferenceCharger = (ListPreference)findPreference(PreferenceKeys.Miscellaneous.USAGE_PROFILE_CHARGER);
            preferenceScreen = (PreferenceScreen)findPreference("KEY_SETTINGS_USAGE_GROUP");

			if (allowGcm) {
				CheckBoxPreference checkPref = (CheckBoxPreference) findPreference("KEY_GCM_HEARTBEAT");
				this.getPreferenceScreen().removePreference(checkPref);
			}

            // Hide preference meant for debugging
	   		if (!MMCLogger.isDebuggable()) {
	   			ListPreference mListPref = (ListPreference) findPreference(PreferenceKeys.Miscellaneous.TRAVEL_DETECT); 
	   			this.getPreferenceScreen().removePreference(mListPref); 
	   		}	
	   		CheckBoxPreference wifiPref = (CheckBoxPreference) findPreference(PreferenceKeys.Miscellaneous.SEND_ON_WIFI); 
			wifiPref.setChecked(sendOnWifi);

            if (svrEnabled != -1)
            {
                CheckBoxPreference mPref = (CheckBoxPreference) findPreference(PreferenceKeys.Miscellaneous.AUTOSPEED_ENABLE);
                this.getPreferenceScreen().removePreference(mPref);
                if (svrSizeMB != -1)
                {
                    EditTextPreference mTextPref = (EditTextPreference) findPreference(PreferenceKeys.Miscellaneous.AUTOSPEED_SIZEMB);
                    this.getPreferenceScreen().removePreference(mTextPref);
                }
            }

			if (serviceModeEnabled == true)
			{
				((Settings)this.getActivity()).setSvcModeDescription();
			}

        }        

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
        		Bundle savedInstanceState) {
        	View v = super.onCreateView(inflater, container, savedInstanceState);
/*        	if(v != null) {
        		ListView listView = (ListView) v.findViewById(android.R.id.list);
        		listView.setPadding(0, 0, 0, 0);
        	}
*/        	return v;
        }
    } 
		
	@Override
	public void onResume() {
		super.onResume();

		setUsageDescription(PreferenceKeys.Miscellaneous.USAGE_PROFILE);
		setUsageDescription(PreferenceKeys.Miscellaneous.USAGE_PROFILE_CHARGER);
		setUsageGroupDescription();
	} 


	private void setSvcModeDescription ()
	{
		try
		{
			// String groupTitle = getString(R.string.Settings_ServiceMode);
			// String setting = getString(R.string.Settings_NotEnabled);
			if (svcmodeActive == null) {
				return;
			}
			boolean bActive = svcmodeActive.isChecked();
			//boolean bCalls = svcmodeCalls.isChecked();
			if (bActive) {
				MMCSystemUtil.promptInstallSystem(this, false);

//				boolean svcEnabled = MMCSystemUtil.isServiceModeEnabled();
//
//				boolean needDebugMode = false;
//				if (Build.VERSION.SDK_INT < 20) {
//					// check whether debug=high is enabled on the device
//					java.lang.Process p = Runtime.getRuntime().exec ("getprop ro.boot.debug_level");
//					int res = p.waitFor();
//					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//					String line = bufferedReader.readLine();
//					if (!line.equals("0x4948") && !line.equals("0x494d"))
//						needDebugMode = true;
//				}
//
//				// get user to enable debug mode, then it will reboot
//				if (!svcEnabled)
//					MMCSystemUtil.promptInstallSystem(this,!needDebugMode);  // don't reboot if debug mode also needs to be enabled
//				else if (needDebugMode)
//					MMCSystemUtil.promptDebugMode(this);
			}
			else
			{
				// prompt to delete service mode
				boolean svcEnabled = MMCSystemUtil.isServiceModeEnabled ();
				if (svcEnabled)
					MMCSystemUtil.promptRemoveSystem(this);
			}


//			if (svcmodeScreen.getDialog() != null)
//			{
//				svcmodeScreen.getDialog().setTitle(groupTitle + " - " + setting);
//			}
//			svcmodeScreen.setTitle(groupTitle);
			//svcmodeScreen.

		}
		catch (Exception e)
		{
			String s = e.toString();
		}
	}

	private void setUsageGroupDescription ()
	{
		try
		{
			String groupTitle = getString(R.string.Settings_Usage_Group);
			String setting = "";
			// make sure list preference's summary text is being updated. 
//			ListPreference listPreference = (ListPreference)findPreference(PreferenceKeys.Miscellaneous.USAGE_PROFILE); 
			
			final String value = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.Miscellaneous.USAGE_PROFILE, PreferenceKeys.Miscellaneous.USAGE_PROFILE); 
			if (listPreference == null) {
				return;
			}
			final int index = listPreference.findIndexOfValue(value); 
			if (index >= 0) 
			{ 
				CharSequence[] entries = listPreference.getEntries();
				if (entries != null && entries.length > index) 
					setting = (String)listPreference.getEntries()[index]; 
//				groupTitle += " - " + setting;
			}
			
			if (preferenceScreen.getDialog() != null)
			{
				preferenceScreen.getDialog().setTitle(groupTitle + " - " + setting); 
			}
			preferenceScreen.setTitle(groupTitle);
			 
		}
		catch (Exception e){}
		
	}
	private void setUsageDescription (final String key)
	{
		try
		{
			// make sure list preference's summary text is being updated. 
			ListPreference TemplistPreference = null;
			if(key.equals(PreferenceKeys.Miscellaneous.USAGE_PROFILE_CHARGER)) {
				TemplistPreference = listPreferenceCharger;
			}
			else {
				TemplistPreference = listPreference;
			}
//			ListPreference listPreference = (ListPreference)findPreference(key); 
			final String value = PreferenceManager.getDefaultSharedPreferences(this).getString(key, key); 
			final int index = TemplistPreference.findIndexOfValue(value); 
			String usageDescription = "";
			switch (index)
			{
			case 0:
				usageDescription = getString(R.string.Settings_Minimum_Description); break;
			case 1:
				usageDescription = getString(R.string.Settings_Balanced_Description); break;
			case 2:
				usageDescription = getString(R.string.Settings_Maximum_Description); break;
			}
			if (index >= 0) { 
				//final String summary = (String)listPreference.getEntries()[index]; 
				TemplistPreference.setSummary(usageDescription); 
			} 
		}
		catch (Exception e){}
	}
	class PreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			
			if (lastChange + 10000 < System.currentTimeMillis() && 
					(key.equals(PREFERENCE_KEY_SHARE_WITH_CARRIER) || key.equals(PREFERENCE_KEY_CONTACT_EMAIL) || key.equals(PREFERENCE_KEY_TWITTER)))
			{
				lastChange = System.currentTimeMillis();
				//if (((MMCApplication) Settings.this.getApplicationContext()).useLinux())
				{
					if(key.equals(PREFERENCE_KEY_SHARE_WITH_CARRIER)) {
						boolean shareWithCarrier = sharedPreferences.getBoolean(PREFERENCE_KEY_SHARE_WITH_CARRIER, true);
						ReportManager.getInstance(Settings.this).reportSettingChange(ServerUpdateRequest.DEVICE, ServerUpdateRequest.KEY_SHARE_WITH_CARRIER, shareWithCarrier);
					}
					if(key.equals(PREFERENCE_KEY_CONTACT_EMAIL)) {
						String email = sharedPreferences.getString(PREFERENCE_KEY_CONTACT_EMAIL, "");
						ReportManager.getInstance(Settings.this).reportSettingChange (ServerUpdateRequest.USER, ServerUpdateRequest.KEY_EMAIL_SETTING, email);
					}
					if(key.equals(PREFERENCE_KEY_TWITTER)) {
						String email = sharedPreferences.getString(PREFERENCE_KEY_TWITTER, "");
						ReportManager.getInstance(Settings.this).reportSettingChange(ServerUpdateRequest.USER, ServerUpdateRequest.KEY_TWITTER_SETTING, email);
					}
				}
//				else 
//				{
//					ReportManager.getInstance(Settings.this).sendUserUpdateWin();
//				}
			}
			else if (key.equals(PreferenceKeys.Miscellaneous.ICON_ALWAYS))
			{
				ReportManager.getInstance(Settings.this).setIconBehavior(); 
				sharedPreferences.edit().putBoolean(PreferenceKeys.Miscellaneous.CHANGED_NOTIFY, true).commit();
			}
			else if (key.equals(PreferenceKeys.Miscellaneous.SEND_ON_WIFI))
			{
				PreferenceManager.getDefaultSharedPreferences(Settings.this).edit().putBoolean(PreferenceKeys.Miscellaneous.CHANGED_SEND_ON_WIFI, true).commit();
			}
			else if (key.equals(PreferenceKeys.Miscellaneous.AUTOSPEED_SIZEMB))
			{
				PreferenceManager.getDefaultSharedPreferences(Settings.this).edit().putBoolean(PreferenceKeys.Miscellaneous.AUTOSPEED_MB_CHANGED, true).commit();
			}		
	
			// update preference's summary text 
			if (key.equals(PreferenceKeys.Miscellaneous.USAGE_PROFILE) || 
					key.equals(PreferenceKeys.Miscellaneous.USAGE_PROFILE_CHARGER)) 
			{
				setUsageDescription (key);
				setUsageGroupDescription ();
			}

			if (key.equals("KEY_SETTINGS_SVCMODE"))
			{
				PreferenceManager.getDefaultSharedPreferences(Settings.this).edit().putBoolean("KEY_SETTINGS_SVCMODE_CHANGED", true).commit();
				setSvcModeDescription();
			}

//			if (key.equals("KEY_SETTINGS_ROOT_ACCESS")) {
//				PreferenceManager.getDefaultSharedPreferences(Settings.this).edit().putBoolean(PreferenceKeys.Miscellaneous.DONT_ASK_FOR_ROOT_ACCESS, false).commit();
//			}
		}
		
	}
}

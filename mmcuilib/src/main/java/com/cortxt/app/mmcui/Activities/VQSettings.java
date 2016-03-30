package com.cortxt.app.mmcui.Activities;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.View;

import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcutility.Utils.PreferenceKeys;
import com.cortxt.com.mmcextension.VQ.VQManager;

/**
 * @author brad
 *
 */

public class VQSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	private long lastChange = 0;
	private static ListPreference listPreference;
	private static PreferenceScreen preferenceScreen;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.MyTheme);		
		super.onCreate(savedInstanceState);
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		addPreferencesFromResource(R.xml.preferences_vq);
		fillChoices ();
//		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH){	        	
//            addPreferencesFromResource(R.xml.preferences_vq);
//	        
//            listPreference = (ListPreference)findPreference(PreferenceKeys.Miscellaneous.USAGE_PROFILE);
//            
//	   		//CheckBoxPreference recorderPref = (CheckBoxPreference) findPreference(PreferenceKeys.Miscellaneous.SEND_ON_WIFI); 
//	   		//recorderPref.setDefaultValue(defaultRecorder);
//        }
//        else {        	
//        	getFragmentManager().beginTransaction().replace(android.R.id.content,
//                new PrefsFragment()).commit();
//        } 
		
       
	}	
	@Override     
	protected void onResume() {
	    super.onResume();
	    SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);  
	    onSharedPreferenceChanged(sharedPreferences, "KEY_SETTINGS_VQ_ENCODER");
	    onSharedPreferenceChanged(sharedPreferences, "KEY_SETTINGS_VQ_FORMAT");
	    onSharedPreferenceChanged(sharedPreferences, "KEY_SETTINGS_VQ_SAMPLE");
	    onSharedPreferenceChanged(sharedPreferences, "KEY_SETTINGS_VQ_SOURCE");
	    onSharedPreferenceChanged(sharedPreferences, "KEY_SETTINGS_VQ_LEVEL");
	    onSharedPreferenceChanged(sharedPreferences, "KEY_SETTINGS_VQ_LANG");
	    onSharedPreferenceChanged(sharedPreferences, "KEY_SETTINGS_VQ_VOICE");
	    //onSharedPreferenceChanged(sharedPreferences, "KEY_SETTINGS_VQ_TELEPHONE");
	    
	    
	    
	} //end onResume

	@Override     
	protected void onPause() {         
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	} //end onPause
	
	public void onExit(View view) {
		this.finish();
	}
	
    @TargetApi(14)
    static public class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
//            addPreferencesFromResource(R.xml.preferences_vq_v14);
         
//            listPreference = (ListPreference)findPreference(PreferenceKeys.Miscellaneous.USAGE_PROFILE);
//            
//	   		CheckBoxPreference recorderPref = (CheckBoxPreference) findPreference(PreferenceKeys.Miscellaneous.SEND_ON_WIFI); 
//	   		recorderPref.setChecked(externalRecorder);
        }        
    } 
		
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	    Preference pref = findPreference(key);
	    
	    if (pref instanceof ListPreference) {
	        ListPreference listPref = (ListPreference) pref;
	        pref.setSummary(listPref.getEntry());
	    }
	}
	
	private void fillChoices ()
	{
		String strConfig = PreferenceManager.getDefaultSharedPreferences(this).getString(VQManager.VQ_CONFIG, null);
		String strVQService = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.Miscellaneous.VOICETEST_SERVICE, "twilio");
		if (strVQService == null) // default to a value for now, but do not set the preference as this will enable VQ
			strVQService = "twilio";
		if (strConfig != null)
		{
			try
			{
				JSONObject config = new JSONObject (strConfig);
				if (config.has(strVQService))
				{
					config = config.getJSONObject(strVQService);
					JSONArray voices = config.getJSONArray("voices");
					int voiceCount = voices.length();
					JSONObject languages = config.getJSONObject("languages");
					int languageCount = languages.length();
					CharSequence[] voiceEntries = new CharSequence[voiceCount];
					CharSequence[] langEntries = new CharSequence[languageCount];
					
					for (int n=0; n<voiceCount; n++)
					{
						voiceEntries[n] = voices.getString(n);
					}
					Iterator keys = languages.keys();
					int n = 0;
					while(keys.hasNext()){
				        String key = (String)keys.next();
				        langEntries[n++] = key;
					}
					
					ListPreference voicesList = (ListPreference) findPreference(VQManager.KEY_SETTINGS_VQ_VOICE);
					ListPreference langsList = (ListPreference) findPreference(VQManager.KEY_SETTINGS_VQ_LANG);
					
					voicesList.setEntries(voiceEntries);
					voicesList.setEntryValues(voiceEntries);
					langsList.setEntries(langEntries);
					langsList.setEntryValues(langEntries);
				}
			}
			catch (Exception e)
			{
				
			}
		}
	}
	
}

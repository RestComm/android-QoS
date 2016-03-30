package com.cortxt.app.mmcui.Activities;

import java.sql.Date;
import java.util.Calendar;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcui.utils.FontsUtil;
import com.cortxt.app.mmcui.utils.ScalingUtility;
import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.cortxt.app.mmcutility.Reporters.ReportManager;
import com.cortxt.app.mmcutility.Utils.MmcConstants;
import com.cortxt.app.mmcutility.Utils.PreferenceKeys;

import org.json.JSONObject;

public class EngineeringModSettings  extends MMCActivity {
	final int[] itemValuesDuration = new int[]{1, 3, 6, 12, 24, 72, 0};
	final boolean[] itemValuesCovInterval = new boolean[]{false,true};
	final int[] itemValuesInterval = new int[]{0,1,2,5,10,15,30};
	final int[] itemValuesStart = new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24};
	final int[] itemValuesSchedule = new int[]{0,1,2,4,8,12,24,48,96}; //0 is 'one time'
//	CheckBox cbCoverage = null;
//	CheckBox cbSpeedtest = null;
//	CheckBox remoteTesting = null;
	Spinner spinnerCoverage = null; //recording duration
	Spinner spinnerSpeed = null; //speed test interval
	Spinner spinnerVideo = null; //video test interval
    Spinner spinnerAudio = null; //audio test interval
	Spinner spinnerConnect = null; //connectivity test interval
	Spinner spinnerSMS = null; // SMS test interval
	Spinner spinnerWeb = null; // web test interval
	Spinner spinnerVQ = null; // Voice Quality test interval
	Spinner spinnerYouTube = null; // Youtube video test test interval
	Spinner spinnerPing = null; // Ping test interval
	Spinner spinner = null; //schedule interval

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view  = inflater.inflate(R.layout.engineer_mode_settings, null, false);
		ScalingUtility.getInstance(this).scaleView(view);
        MMCActivity.customizeTitleBar(this, view, R.string.recordingdlg_title, R.string.recordingdlg_title);

		initViews(view);
		applyFonts(view);
		setContentView(view);
		initSelections ();

	}
	
	private void applyFonts(View view){
		TextView heading=(TextView)view.findViewById(R.id.actionbartitle);
		TextView recordSessionText=(TextView)view.findViewById(R.id.recordSessionText);
		TextView dataTypeText=(TextView)view.findViewById(R.id.dataTypeText);
		//TextView intervalText=(TextView)view.findViewById(R.id.intervalText);
		//CheckBox boxCov = (CheckBox) view.findViewById(R.id.checkBoxCoverage);
		//CheckBox boxSpeedTest = (CheckBox) view.findViewById(R.id.checkBoxSpeedtest);
		//TextView scheduleText=(TextView)view.findViewById(R.id.scheduleTextView);

		FontsUtil.applyFontToTextView(MmcConstants.font_Regular, heading, this);
		FontsUtil.applyFontToTextView(MmcConstants.font_Regular, recordSessionText, this);
		FontsUtil.applyFontToTextView(MmcConstants.font_Regular, dataTypeText, this);
		//FontsUtil.applyFontToTextView(MmcConstants.font_Regular, intervalText, this);
		//FontsUtil.applyFontToTextView(MmcConstants.font_MEDIUM, scheduleText, this);
		//FontsUtil.applyFontToButton(MmcConstants.font_Regular, boxCov, this);
		//FontsUtil.applyFontToButton(MmcConstants.font_Regular, boxSpeedTest, this);
		
		FontsUtil.applyFontToTextView(MmcConstants.font_Regular, (TextView) view.findViewById(R.id.intervalCoverage), this);
		FontsUtil.applyFontToTextView(MmcConstants.font_Regular, (TextView)view.findViewById(R.id.intervalSpeed), this);
		FontsUtil.applyFontToTextView(MmcConstants.font_Regular, (TextView)view.findViewById(R.id.intervalSMS), this);
		FontsUtil.applyFontToTextView(MmcConstants.font_Regular, (TextView)view.findViewById(R.id.intervalVideo), this);
        FontsUtil.applyFontToTextView(MmcConstants.font_Regular, (TextView)view.findViewById(R.id.intervalAudio), this);
        FontsUtil.applyFontToTextView(MmcConstants.font_Regular, (TextView)view.findViewById(R.id.intervalWeb), this);
		FontsUtil.applyFontToTextView(MmcConstants.font_Regular, (TextView)view.findViewById(R.id.intervalVQ), this);
		FontsUtil.applyFontToTextView(MmcConstants.font_Regular, (TextView)view.findViewById(R.id.intervalYouTube), this);
		FontsUtil.applyFontToTextView(MmcConstants.font_Regular, (TextView)view.findViewById(R.id.intervalPing), this);
		FontsUtil.applyFontToTextView(MmcConstants.font_Regular, (TextView)view.findViewById(R.id.intervalVQ), this);
		FontsUtil.applyFontToTextView(MmcConstants.font_Regular, (TextView)view.findViewById(R.id.intervalConnect), this);
		
	}
	
	public void BackActionClicked(View button){
		this.finish();
	}


	private void initSelections ()
	{
		String cmd = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.Miscellaneous.DRIVE_TEST_CMD, null);
		if (cmd == null)
			return;
		try {
			JSONObject cmdJson = new JSONObject(cmd);
			JSONObject testSettings = cmdJson.getJSONObject("settings");

			int selection = -1, val;
			boolean bVal = false;
			int numFiveMinutePeriods = 0;
			if (testSettings.has("dur"))
				numFiveMinutePeriods = testSettings.getInt("dur");
			else
				numFiveMinutePeriods = 3;
			//selection = java.util.Arrays.asList(itemValuesDuration).indexOf(numFiveMinutePeriods);
			selection = java.util.Arrays.binarySearch(itemValuesDuration, numFiveMinutePeriods);
			if (selection >= 0)
				spinner.setSelection(selection);
			if (testSettings.has("cov"))
			{
				selection = testSettings.getInt("cov");
				spinnerCoverage.setSelection(selection);
			}
			if (testSettings.has("spd"))
			{
				selection = java.util.Arrays.binarySearch(itemValuesInterval, testSettings.getInt("spd"));
				if (selection >= 0)
					spinnerSpeed.setSelection(selection);
			}
			if (testSettings.has("vid"))
			{
				selection = java.util.Arrays.binarySearch(itemValuesInterval, testSettings.getInt("vid"));
				if (selection >= 0)
					spinnerVideo.setSelection(selection);
			}
			if (testSettings.has("youtube"))
			{
				selection = java.util.Arrays.binarySearch(itemValuesInterval, testSettings.getInt("youtube"));
				if (selection >= 0)
					spinnerYouTube.setSelection(selection);
			}
			if (testSettings.has("ping"))
			{
				selection = java.util.Arrays.binarySearch(itemValuesInterval, testSettings.getInt("ping"));
				if (selection >= 0)
					spinnerPing.setSelection(selection);
			}
			if (testSettings.has("ct"))
			{
				selection = java.util.Arrays.binarySearch(itemValuesInterval, testSettings.getInt("ct"));
				if (selection >= 0)
					spinnerConnect.setSelection(selection);
			}
			if (testSettings.has("sms"))
			{
				selection = java.util.Arrays.binarySearch(itemValuesInterval, testSettings.getInt("sms"));
				if (selection >= 0)
					spinnerSMS.setSelection(selection);
			}
			if (testSettings.has("web"))
			{
				selection = java.util.Arrays.binarySearch(itemValuesInterval, testSettings.getInt("web"));
				if (selection >= 0)
					spinnerWeb.setSelection(selection);
			}
			if (testSettings.has("vq"))
			{
				selection = java.util.Arrays.binarySearch(itemValuesInterval, testSettings.getInt("vq"));
				if (selection >= 0)
					spinnerVQ.setSelection(selection);
			}
			if (testSettings.has("aud"))
			{
				selection = java.util.Arrays.binarySearch(itemValuesInterval, testSettings.getInt("aud"));
				if (selection >= 0)
					spinnerAudio.setSelection(selection);
			}
		}
		catch (Exception e)
		{

		}
	}

	public void startClicked(View button) {
		int numFiveMinutePeriodsToTrack = itemValuesDuration[spinner.getSelectedItemPosition()];
		boolean coverage = itemValuesCovInterval[spinnerCoverage.getSelectedItemPosition()];
		int speedInterval = itemValuesInterval[spinnerSpeed.getSelectedItemPosition()];
		int videoInterval = itemValuesInterval[spinnerVideo.getSelectedItemPosition()];
        int audioInterval = itemValuesInterval[spinnerAudio.getSelectedItemPosition()];
        int connectInterval = itemValuesInterval[spinnerConnect.getSelectedItemPosition()];
		int smsInterval = itemValuesInterval[spinnerSMS.getSelectedItemPosition()];
		int webInterval = itemValuesInterval[spinnerWeb.getSelectedItemPosition()];
		int vqInterval = itemValuesInterval[spinnerVQ.getSelectedItemPosition()];
		int ytInterval = itemValuesInterval[spinnerYouTube.getSelectedItemPosition()];
		int pingInterval = itemValuesInterval[spinnerPing.getSelectedItemPosition()];

		ReportManager.getInstance(getApplicationContext()).startDriveTest(this, numFiveMinutePeriodsToTrack * 5, coverage, speedInterval, connectInterval,
				smsInterval, videoInterval, audioInterval, webInterval, vqInterval, ytInterval, pingInterval);

//		sendBroadcast(intent);
		this.finish();
	}
	
	private void initViews(View v){
		spinner = (Spinner) v.findViewById(R.id.recordingTimeSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.LiveStatus_TrackingValues, R.layout.spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		spinner.setSelection(3);

		// do the same to fill the speed test interval spinner
		spinnerCoverage = (Spinner) v.findViewById(R.id.CoverageSpinnerView);
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
				R.array.recorddlg_cov_interval_values, R.layout.spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerCoverage.setAdapter(adapter2);
		spinnerCoverage.setSelection(1);
				
		// do the same to fill the speed test interval spinner
		spinnerSpeed = (Spinner) v.findViewById(R.id.SpeedSpinnerView);
		adapter2 = ArrayAdapter.createFromResource(this,
				R.array.recorddlg_interval_values, R.layout.spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSpeed.setAdapter(adapter2);
		spinnerSpeed.setSelection(0);
		
		// do the same to fill the Video interval spinner
		spinnerVideo = (Spinner) v.findViewById(R.id.VideoSpinnerView);
		adapter2 = ArrayAdapter.createFromResource(this,
				R.array.recorddlg_interval_values, R.layout.spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerVideo.setAdapter(adapter2);
		spinnerVideo.setSelection(0);

		// do the same to fill the Youtube interval spinner
		spinnerYouTube = (Spinner) v.findViewById(R.id.YouTubeSpinnerView);
		adapter2 = ArrayAdapter.createFromResource(this,
				R.array.recorddlg_interval_values, R.layout.spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerYouTube.setAdapter(adapter2);
		spinnerYouTube.setSelection(0);

        // do the same to fill the Audio interval spinner
        spinnerAudio = (Spinner) v.findViewById(R.id.AudioSpinnerView);
        adapter2 = ArrayAdapter.createFromResource(this,
                R.array.recorddlg_interval_values, R.layout.spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAudio.setAdapter(adapter2);
        spinnerAudio.setSelection(0);
		
		// do the same to fill the Connectivity interval spinner
		spinnerConnect = (Spinner) v.findViewById(R.id.ConnectSpinnerView);
		adapter2 = ArrayAdapter.createFromResource(this,
				R.array.recorddlg_interval_values, R.layout.spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerConnect.setAdapter(adapter2);
		spinnerConnect.setSelection(0);

		// do the same to fill the Ping test interval spinner
		spinnerPing = (Spinner) v.findViewById(R.id.PingSpinnerView);
		adapter2 = ArrayAdapter.createFromResource(this,
				R.array.recorddlg_interval_values, R.layout.spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerPing.setAdapter(adapter2);
		spinnerPing.setSelection(0);
		
		// do the same to fill the SMS Test interval spinner
		spinnerSMS = (Spinner) v.findViewById(R.id.SMSSpinnerView);
		adapter2 = ArrayAdapter.createFromResource(this,
				R.array.recorddlg_interval_values, R.layout.spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSMS.setAdapter(adapter2);
		spinnerSMS.setSelection(0);
		
		// do the same to fill the Web Test interval spinner
		spinnerWeb = (Spinner) v.findViewById(R.id.WebSpinnerView);
		adapter2 = ArrayAdapter.createFromResource(this,
				R.array.recorddlg_interval_values, R.layout.spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerWeb.setAdapter(adapter2);
		spinnerWeb.setSelection(0);
		
		// do the same to fill the Web Test interval spinner
		spinnerVQ = (Spinner) v.findViewById(R.id.VQSpinnerView);
		adapter2 = ArrayAdapter.createFromResource(this,
				R.array.recorddlg_interval_values, R.layout.spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerVQ.setAdapter(adapter2);
		spinnerVQ.setSelection(0);
		
//		spinner3 = (Spinner) v.findViewById(R.id.IntervalSpinnerView3);
//		ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this,
//				R.array.start_schedule, R.layout.spinner_item);
//		adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//		spinner3.setAdapter(adapter3);
////		spinner3.setSelection(findNextHour());
//		spinner3.setSelection(0);
//		
//		spinner4 = (Spinner) v.findViewById(R.id.IntervalSpinnerView4);
//		ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(this,
//				R.array.schedule_interval, R.layout.spinner_item);
//		adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//		spinner4.setAdapter(adapter4);
//		spinner4.setSelection(0);
		
		spinner.setOnItemSelectedListener(OnCatSpinnerCL);
		spinnerCoverage.setOnItemSelectedListener(OnCatSpinnerCL);
		spinnerSpeed.setOnItemSelectedListener(OnCatSpinnerCL);
		spinnerConnect.setOnItemSelectedListener(OnCatSpinnerCL);
		
		spinnerWeb.setOnItemSelectedListener(OnCatSpinnerCL);
		spinnerVQ.setOnItemSelectedListener(OnCatSpinnerCL);
		
		if (PreferenceKeys.getSMSPermissionsAllowed(this, true) == true ){
			spinnerSMS.setOnItemSelectedListener(OnCatSpinnerCL);
		}
		else
		{
			spinnerSMS.setVisibility(View.GONE);
			v.findViewById(R.id.intervalSMS).setVisibility(View.GONE);
		}
		
		String videoUrl = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.Miscellaneous.VIDEO_URL, null);
		if (videoUrl == null || videoUrl.length() == 0 || !PreferenceKeys.isEventPermitted(this, EventType.VIDEO_TEST, 1))
		{
			// Video Test disabled
			spinnerVideo.setVisibility(View.GONE);
			v.findViewById(R.id.intervalVideo).setVisibility(View.GONE);
		}

		String youtubeId = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.Miscellaneous.YOUTUBE_VIDEOID, null);
		if (youtubeId == null || youtubeId.length() == 0 || !PreferenceKeys.isEventPermitted(this, EventType.YOUTUBE_TEST, 1))
		{
			// Video Test disabled
			spinnerYouTube.setVisibility(View.GONE);
			v.findViewById(R.id.intervalYouTube).setVisibility(View.GONE);
		}

        String audioUrl = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.Miscellaneous.AUDIO_URL, null);
        if (audioUrl == null || audioUrl.length() == 0 || !PreferenceKeys.isEventPermitted(this, EventType.AUDIO_TEST, 1))
        {
            // Audio Test disabled
            spinnerAudio.setVisibility(View.GONE);
            v.findViewById(R.id.intervalAudio).setVisibility(View.GONE);
        }

        String webUrl = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.Miscellaneous.WEB_URL, null);
        if (webUrl == null || webUrl.length() == 0 || !PreferenceKeys.isEventPermitted(this, EventType.WEBPAGE_TEST, 1))
        {
            // Webpage Test disabled
            spinnerWeb.setVisibility(View.GONE);
            v.findViewById(R.id.intervalWeb).setVisibility(View.GONE);
        }
		
		String voiceDial = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.Miscellaneous.VOICETEST_SERVICE, null);
		PackageManager pkMan = this.getPackageManager();		
		int voiceCallPermissionValue = pkMan.checkPermission("android.permission.CALL_PHONE", getPackageName());
		
		if (voiceDial == null || voiceDial.length() == 0 || voiceCallPermissionValue != 0)
		{
			// Voice Quality Test disabled
			spinnerVQ.setVisibility(View.GONE);
			v.findViewById(R.id.intervalVQ).setVisibility(View.GONE);
		} 
		
		int allowConnTest = PreferenceManager.getDefaultSharedPreferences(this).getInt(PreferenceKeys.Miscellaneous.AUTO_CONNECTION_TESTS, 1);
		if (allowConnTest == 0)
		{
			// Voice Quality Test disabled
			spinnerConnect.setVisibility(View.GONE);
			v.findViewById(R.id.intervalConnect).setVisibility(View.GONE);
		}

		int allowPingTest = 0; // PreferenceManager.getDefaultSharedPreferences(this).getInt(PreferenceKeys.Miscellaneous.PING_TESTS, 1);
		if (allowPingTest == 0)
		{
			// Voice Quality Test disabled
			spinnerPing.setVisibility(View.GONE);
			v.findViewById(R.id.intervalPing).setVisibility(View.GONE);
		}

//		spinner3.setOnItemSelectedListener(OnCatSpinnerCL);
//		spinner4.setOnItemSelectedListener(OnCatSpinnerCL);
		
//		cbCoverage = (CheckBox)v.findViewById(R.id.checkBoxCoverage);
//		cbSpeedtest = (CheckBox)v.findViewById(R.id.checkBoxSpeedtest);
//		cbCoverage.setChecked(true);
//		cbSpeedtest.setChecked(false);
		
//		remoteTesting = (CheckBox)v.findViewById(R.id.remoteTestingCheckBox);
//		cbSpeedtest.setChecked(false);
//		((Spinner) spinner3).setEnabled(false);
//		((Spinner) spinner4).setEnabled(false);		
	}	
	
//	public void checkBoxListener() {		 
//		remoteTesting = (CheckBox) findViewById(R.id.remoteTestingCheckBox);
//		remoteTesting.setOnClickListener(new OnClickListener() {	 
//			@Override
//			public void onClick(View view) {
//				if (((CheckBox) view).isChecked()) {
//					((Spinner) spinner3).setEnabled(true);
//					((Spinner) spinner4).setEnabled(true);		
//				}
//				else {
//					((Spinner) spinner3).setEnabled(false);
//					((Spinner) spinner4).setEnabled(false);		
//				}	 
//			}
//		});	 
//	}
	
	private OnItemSelectedListener OnCatSpinnerCL = new AdapterView.OnItemSelectedListener() {
		public void onItemSelected (AdapterView<?> parent, View view, int pos, long id) {
//		((TextView)parent.getChildAt(0)).setTextSize(14);
		}
		public void onNothingSelected (AdapterView<?> parent) {}
	};
	
	private int findNextHour() {
        Calendar evtCal = Calendar.getInstance();
        evtCal.setTime (new Date(System.currentTimeMillis()));    
        int hour = evtCal.get (Calendar.HOUR);
		//will return current hour, but will be next hour bc it is used as an index
        return hour; 
	}
}

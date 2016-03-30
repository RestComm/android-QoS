package com.cortxt.app.mmcui.Activities;

import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcui.utils.FontsUtil;
import com.cortxt.app.mmcui.utils.ScalingUtility;
import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.cortxt.app.mmcutility.DataObjects.PhoneState;
import com.cortxt.app.mmcutility.Reporters.ReportManager;
import com.cortxt.app.mmcutility.Utils.CommonIntentActionsOld;
import com.cortxt.app.mmcutility.Utils.CommonIntentBundleKeysOld;
import com.cortxt.app.mmcutility.Utils.Global;
import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.Utils.MmcConstants;
import com.cortxt.app.mmcutility.Utils.PreferenceKeys;
import com.cortxt.app.mmcutility.Utils.TaskHelper;
import com.cortxt.app.mmcutility.Utils.TimeSeries;
import com.cortxt.app.mmcui.Activities.CustomViews.DropDownMenuWindow;
import com.cortxt.app.mmcui.Activities.CustomViews.NerdView;
import com.cortxt.app.mmcui.Activities.CustomViews.SatelliteCount;
import com.cortxt.app.mmcui.Activities.CustomViews.SatelliteStatus;
import com.cortxt.app.mmcui.Activities.CustomViews.Chart.Chart;
import com.cortxt.com.mmcextension.MMCSystemUtil;
import com.cortxt.app.mmcutility.ContentProvider.Provider;
import com.cortxt.app.mmcutility.ContentProvider.Tables;
import com.cortxt.app.mmcutility.ContentProvider.UriMatch;
import org.json.JSONObject;

//import com.cortxt.app.MMC.ActivitiesOld.Login.Logout;

/**
 * This class manages the Livestatus activity.
 * 
 * @author abhin
 * 
 */
public class NerdScreen extends MMCTrackedActivityOld {
	// constants
	public static final String TAG = NerdScreen.class.getSimpleName();
	/**
	 * This is the number of milliseconds before a redraw is forced upon the chart.
	 * 
	 * Note: this forced redraw is not the only source of re-draws.
	 */
	private static final int CHART_UPDATE_INTERVAL = 1000;
	/**
	 * This is the key for the chart series that gets stored in the instance state bundle.
	 */
	private static final String SAVE_INSTANCE_STATE_CHART_SERIES_KEY = "CHART_SERIES_KEY";
	/**
	 * This is the key for the marker series that gets stored in the instance state bundle.
	 */
	private static final String SAVE_INSTANCE_STATE_MARKER_SERIES_KEY = "MARKER_SERIES_KEY";
	/**
	 * This is the key for the percentometer value that gets stored in the instance state bundle.
	 */
	private static final String SAVE_INSTANCE_STATE_PERCENTOMETER_VAL_KEY = "PERCENTOMETER_VALUE_KEY";
	private static final String SAVE_INSTANCE_STATE_NERD_HASH_KEY = "NERD_HASH_KEY";

	// other variables
	private SharedPreferences defaultPrefs;
	// private Guage percentometer;
	private NerdView nerdview;
	private TextView mChartTitle;
	private SatelliteCount satelliteCount;
	private SatelliteStatus satelliteStatus;
	private Chart signalTrendChart;
	private IntentDispatcher intentDispatcher;
	private Timer chartUpdateTimer; // this is used to invalidate the chart every so often so that it updates
	public TelephonyManager telephonyManager;
	// private ImageView mCarrierLogo;
	// private ImageView mCarrierLogoBg;
	public static TextView timeText = null;
	private AsyncTask<Void, Void, Bitmap> mGetCarrierLogoTask;
	private DropDownMenuWindow engineerMenu = null;
	private ImageButton menuButton = null;
	private static Button startRecordingButton = null;
	private static Button stopRecording = null;
	private static TextView recordingEndsIn = null;
	private static String recordingEnds; // = context.getString (R.string.GenericText_TrackEnds)
	private static String recordingStop; // = context.getString (R.string.GenericText_StopTrack)
	private static RecordingTime recordingtimer;
	
	private boolean smsTestInProgress = false;
	public Context context;
	// private variables related to deciding the gps status message
	/*
	 * ===========================================================================
	 * Explanation of the messages used in the gps status widget
	 * ===========================================================================
	 * 
	 * If the number of satellites used in fix is below 4, then the fix is labeled
	 * "Partial Fix" and a corresponding message is displayed on the satelliteStatus
	 * widget.
	 * If the number of satellites used in the fix is greater than 4, then we look
	 * at the accuracy of the last location update. If the accuracy is greater
	 * than 40.0f, then we call it a "course fix" and the appropriate message is
	 * displayed. If the accuracy is less than or equal to 40.0f, then we use the
	 * term "fine fix".
	 */
	private int numberOfSatellitesUsedInFix = 0;
	private int numberOfSatellites = 0;
	private float locationUpdateAccuracy = 0f;
	private boolean isVisible = false;
	private int[] neighborList = null;
	private long neighborTime = 0;
	private String lteIdentity = "";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.new_live_status, null, false);
		// ScalingUtility.getInstance(this).scaleView(view);
		setContentView(view);
		// setContentView(R.layout.new_live_status);

		LinearLayout chartLayout = (LinearLayout) findViewById(R.id.chartLayout);
		int chartBg = getResources().getInteger(R.integer.CUSTOM_NERD_CHART_BG);
		if (chartBg >= 0 && chartBg <= 0xffffff) {
			chartLayout.setBackgroundColor(chartBg+0xff000000);
		}

		if(getResources().getBoolean(R.bool.CUSTOM_NERD_CHART_FULL_WIDTH)){
			try {
				RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) chartLayout.getLayoutParams();
				lp.leftMargin = 0;
				lp.rightMargin = 0;
				chartLayout.setLayoutParams(lp);
			}
			catch (Exception e) {
				String err = e.toString();
			}
		}
		signalTrendChart = (Chart) findViewById(R.id.signalTrendChart);

		mChartTitle = (TextView) findViewById(R.id.chart_title);
		int titleColor = getResources().getInteger(R.integer.CUSTOM_NERD_CHART_FG);
		if(mChartTitle != null && titleColor>=0&&titleColor<=0xffffff){
			mChartTitle.setTextColor(0xff000000 + titleColor);
		}

		if (signalTrendChart != null)
			signalTrendChart.setParent(this);

		menuButton = (ImageButton) findViewById(R.id.actionbarMenuIcon);
		startRecordingButton = (Button) findViewById(R.id.startButton);
		stopRecording = (Button) findViewById(R.id.RecordingInProgressButton);
		recordingEndsIn = (TextView) findViewById(R.id.recordingEndsIn);


		//ScalingUtility.getInstance(this).scaleView(chartLayout);
		recordingEndsIn.setText("Recording");
		recordingEndsIn.setVisibility(View.INVISIBLE);

		RelativeLayout satellitelayout = (RelativeLayout) findViewById(R.id.satellitelayout);
//
//		String screenColor = (this.getResources().getString(R.string.SCREEN_COLOR));
//		screenColor = screenColor.length() > 0 ? screenColor : "dddddd";
//		int satelliteBgColor = getResources().getInteger(R.integer.CUSTOM_SATELLITE_BG);
//		if(satelliteBgColor>=0){
//			satelliteBgColor += 0xff000000;
//			satellitelayout.setBackgroundColor(satelliteBgColor);
//		}else{
//			// no bg color for satellitelayout
//			satellitelayout.setBackgroundColor(0x00000000);
//		}
		int height = ScalingUtility.getInstance(this).getCurrentHeight();
 		int width = ScalingUtility.getInstance(this).getCurrentWidth();
		MMCLogger.logToFile(MMCLogger.Level.DEBUG, "NerdScreen", "onCreate", "height: " + height + " width: " + width);
		if (height <= width) {
 			ScalingUtility.getInstance(this).scaleView(chartLayout, 1.2f);
			ScalingUtility.getInstance(this).scaleView(satellitelayout, 1.0f);
 			ScalingUtility.getInstance(this).scaleView(startRecordingButton, 1.4f);
 			ScalingUtility.getInstance(this).scaleView(stopRecording, 1.4f);
 			ScalingUtility.getInstance(this).scaleView(recordingEndsIn,1.4f);
 		}
 		else
 		{
			ScalingUtility.getInstance(this).scaleView(chartLayout);
			ScalingUtility.getInstance(this).scaleView(satellitelayout);
			ScalingUtility.getInstance(this).scaleView(startRecordingButton);
 			ScalingUtility.getInstance(this).scaleView(stopRecording);
 			ScalingUtility.getInstance(this).scaleView(recordingEndsIn);
 		}

		stopRecording.setOnClickListener(recordingProgressListener);
		TextView heading = (TextView) findViewById(R.id.actionbartitle);
		FontsUtil.applyFontToTextView(MmcConstants.font_Regular, heading, NerdScreen.this);
		telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE); // this is just a reference to a
																					// previously existing
		// service. It thus is not a memory concern.

		//initViews(savedInstanceState);

		defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		defaultPrefs.edit().putBoolean(PreferenceKeys.Miscellaneous.USER_SHUT_DOWN, false).commit();
		intentDispatcher = new IntentDispatcher();

		if (getResources().getInteger(R.integer.CUSTOM_RECORDINGSTRING) == 1) {
			startRecordingButton.setText(R.string.CustomText_Track);
			// stopRecording.setText (R.string.CustomText_TrackEnds);
			recordingEndsIn.setText(R.string.CustomText_TrackEnds);
			recordingEnds = getString(R.string.CustomText_TrackEnds);
		} else
			recordingEnds = getString(R.string.GenericText_TrackEnds);
		recordingStop = getString(R.string.GenericText_StopTrack);
		
//		IntentFilter intentFilter = new IntentFilter(CommonIntentBundleKeysOld.ACTION_UPDATE);
//		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_LOCATION_UPDATE);
//		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_CELL_UPDATE);
//		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_GPS_STATUS_UPDATE);
//		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_SIGNAL_STRENGTH_UPDATE);
//		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_EVENT_UPDATE);
//		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_NEIGHBOR_UPDATE);
//		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_CONNECTION_UPDATE);
//
//		intentFilter.addAction(MMCIntentHandlerOld.ACTION_DONE_SMSTEST);
//		registerReceiver(broadcastReceiver, intentFilter);

		context = this;
		initEngg(this,view,this);

		// now use the savedInstanceState to possible initialise the value in the views
		if (savedInstanceState != null) {
			// if (savedInstanceState.containsKey(SAVE_INSTANCE_STATE_PERCENTOMETER_VAL_KEY))
			// percentometer.setPercentage(savedInstanceState.getInt(SAVE_INSTANCE_STATE_PERCENTOMETER_VAL_KEY));
			if (savedInstanceState.containsKey(SAVE_INSTANCE_STATE_CHART_SERIES_KEY))
				signalTrendChart.buffer.signalTimeSeries = (TimeSeries<Float>) savedInstanceState.getSerializable(SAVE_INSTANCE_STATE_CHART_SERIES_KEY);
			if (savedInstanceState.containsKey(SAVE_INSTANCE_STATE_MARKER_SERIES_KEY))
				signalTrendChart.buffer.eventTimeSeries = (TimeSeries<EventType>) savedInstanceState.getSerializable(SAVE_INSTANCE_STATE_MARKER_SERIES_KEY);
		}

		MMCSystemUtil.checkSvcModeVersion(this);

	}

	protected void onCreateOld(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.new_live_status, null, false);
		// ScalingUtility.getInstance(this).scaleView(view);
		setContentView(view);
		// setContentView(R.layout.new_live_status);
		RelativeLayout actionBarLayout = (RelativeLayout) findViewById(R.id.topactionbarLayout);
		ScalingUtility.getInstance(this).scaleView(actionBarLayout);
		customizeTitleBar(this, view, R.string.dashboard_engineer, R.string.dashcustom_engineer);
		LinearLayout chartLayout = (LinearLayout) findViewById(R.id.chartLayout);
		RelativeLayout satellitelayout = (RelativeLayout) findViewById(R.id.satellitelayout);

		int chartBg = getResources().getInteger(R.integer.CUSTOM_NERD_CHART_BG);
		if (chartBg >= 0 && chartBg <= 0xffffff) {
			chartLayout.setBackgroundColor(chartBg+0xff000000);
		}

		if(getResources().getBoolean(R.bool.CUSTOM_NERD_CHART_FULL_WIDTH)){
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) chartLayout.getLayoutParams();
			lp.leftMargin = 0;
			lp.rightMargin = 0;
			chartLayout.setLayoutParams(lp);
		}
		String screenColor = (this.getResources().getString(R.string.SCREEN_COLOR));
		screenColor = screenColor.length() > 0 ? screenColor : "dddddd";
		int satelliteBgColor = getResources().getInteger(R.integer.CUSTOM_SATELLITE_BG);
		if(satelliteBgColor>=0){
			satelliteBgColor += 0xff000000;
			satellitelayout.setBackgroundColor(satelliteBgColor);
		}else{
			// no bg color for satellitelayout
			satellitelayout.setBackgroundColor(0x00000000);
/*			int grayColor = Integer.parseInt(screenColor, 16);
			grayColor = Math.max(0, grayColor - 0x202020);
			grayColor += (0xff000000);
			satellitelayout.setBackgroundColor(grayColor);*/
		}

		menuButton = (ImageButton) findViewById(R.id.actionbarMenuIcon);
		startRecordingButton = (Button) findViewById(R.id.startButton);
		stopRecording = (Button) findViewById(R.id.RecordingInProgressButton);
		recordingEndsIn = (TextView) findViewById(R.id.recordingEndsIn);


		//ScalingUtility.getInstance(this).scaleView(chartLayout);
		recordingEndsIn.setText("Recording");
		recordingEndsIn.setVisibility(View.INVISIBLE);
		int height = ScalingUtility.getInstance(this).getCurrentHeight();
		int width = ScalingUtility.getInstance(this).getCurrentWidth();

		if (height <= width) {
			ScalingUtility.getInstance(this).scaleView(chartLayout, 2.5f);
			ScalingUtility.getInstance(this).scaleView(satellitelayout, 1.5f);
			ScalingUtility.getInstance(this).scaleView(startRecordingButton, 1.8f);
			ScalingUtility.getInstance(this).scaleView(stopRecording, 1.8f);
			ScalingUtility.getInstance(this).scaleView(recordingEndsIn,1.8f);
		}
		else
		{
			ScalingUtility.getInstance(this).scaleView(startRecordingButton);
			ScalingUtility.getInstance(this).scaleView(stopRecording);
			ScalingUtility.getInstance(this).scaleView(recordingEndsIn);
		}

		stopRecording.setOnClickListener(recordingProgressListener);
		TextView heading = (TextView) findViewById(R.id.actionbartitle);
		FontsUtil.applyFontToTextView(MmcConstants.font_Regular, heading, NerdScreen.this);
		telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE); // this is just a reference to a
		// previously existing
		// service. It thus is not a memory concern.

		initViews(savedInstanceState);

		defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		defaultPrefs.edit().putBoolean(PreferenceKeys.Miscellaneous.USER_SHUT_DOWN, false).commit();
		intentDispatcher = new IntentDispatcher();

		if (getResources().getInteger(R.integer.CUSTOM_RECORDINGSTRING) == 1) {
			startRecordingButton.setText(R.string.CustomText_Track);
			// stopRecording.setText (R.string.CustomText_TrackEnds);
			recordingEndsIn.setText(R.string.CustomText_TrackEnds);
			recordingEnds = getString(R.string.CustomText_TrackEnds);
		} else
			recordingEnds = getString(R.string.GenericText_TrackEnds);
		recordingStop = getString(R.string.GenericText_StopTrack);

		// percentometer = (Guage) findViewById(R.id.percentometer);
		satelliteCount = (SatelliteCount) findViewById(R.id.satelliteCount);
		satelliteStatus = (SatelliteStatus) findViewById(R.id.satelliteStatus);

		nerdview = (NerdView) findViewById(R.id.nerdview);
		timeText = (TextView) findViewById(R.id.timeAndDate);

		IntentFilter intentFilter = new IntentFilter(CommonIntentBundleKeysOld.ACTION_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_LOCATION_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_CELL_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_GPS_STATUS_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_SIGNAL_STRENGTH_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_EVENT_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_NEIGHBOR_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_CONNECTION_UPDATE);

		intentFilter.addAction(CommonIntentActionsOld.ACTION_DONE_SMSTEST);
		registerReceiver(broadcastReceiver, intentFilter);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// start timer to refresh the chart
		chartUpdateTimer = new Timer(TAG);
		chartUpdateTimer.schedule(new ChartUpdateTask(), 0, CHART_UPDATE_INTERVAL);
		isVisible = true;
		satelliteCount.updateSatelliteCount(numberOfSatellites, numberOfSatellitesUsedInFix);
		// on resuming, update the chart using the DB to capture
		// data that was recorded when the activity was paused
		signalTrendChart.buffer.updateActivityFromDB(signalTrendChart.getChartTimespan() * 2 + signalTrendChart.getChartTime());
		lteIdentity = ReportManager.getInstance(getApplicationContext()).getNeighbors();
		String lte1 = "", lte2 = "";		
		updateNerdViewFromDB();
		if (lteIdentity != null)
		{
			neighborTime = System.currentTimeMillis() + 300000; // dont let the LTE identity expire soon, it should be
														// nulled when no longer LTE
		}
		
		nerdview.setLTEIdentity(lteIdentity);

		if (Global.isGpsRunning())
			satelliteStatus.setText(getString(R.string.LiveStatus_GPSON));
		else
			satelliteStatus.setText(getString(R.string.LiveStatus_GPSOFF));

		resumeRecording();

	}

	@Override
	protected void onPause() {
		// stop timer
		chartUpdateTimer.cancel();
		chartUpdateTimer.purge();
		if (recordingtimer != null) {
			recordingtimer.cancel();
			recordingtimer = null;
		}
		isVisible = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(SAVE_INSTANCE_STATE_CHART_SERIES_KEY, signalTrendChart.buffer.signalTimeSeries);
		outState.putSerializable(SAVE_INSTANCE_STATE_MARKER_SERIES_KEY, signalTrendChart.buffer.eventTimeSeries);
		// outState.putInt(SAVE_INSTANCE_STATE_PERCENTOMETER_VAL_KEY, percentometer.getPercentage());
		outState.putSerializable(SAVE_INSTANCE_STATE_NERD_HASH_KEY, nerdview.nerdHash);
	}

	public static void initEngg (Context context, View enggView, NerdScreen enggActivity)
	{
		RelativeLayout actionBarLayout = (RelativeLayout) enggView.findViewById(R.id.topactionbarLayout);
		ScalingUtility.getInstance(context).scaleView(actionBarLayout);
		customizeTitleBar(context, enggView, R.string.dashboard_engineer, R.string.dashcustom_engineer);


		RelativeLayout satellitelayout = (RelativeLayout) enggView.findViewById(R.id.satellitelayout);

		String screenColor = (enggView.getResources().getString(R.string.SCREEN_COLOR));
		screenColor = screenColor.length() > 0 ? screenColor : "dddddd";
		int satelliteBgColor = enggView.getResources().getInteger(R.integer.CUSTOM_SATELLITE_BG);
		if(satelliteBgColor>=0){
			satelliteBgColor += 0xff000000;
			satellitelayout.setBackgroundColor(satelliteBgColor);
		}else{
			// no bg color for satellitelayout
			satellitelayout.setBackgroundColor(0x00000000);
		}
		// percentometer = (Guage) findViewById(R.id.percentometer);
		enggActivity.satelliteCount = (SatelliteCount) enggView.findViewById(R.id.satelliteCount);
		enggActivity.satelliteStatus = (SatelliteStatus) enggView.findViewById(R.id.satelliteStatus);

		enggActivity.nerdview = (NerdView) enggView.findViewById(R.id.nerdview);
		timeText = (TextView) enggView.findViewById(R.id.timeAndDate);

		enggActivity.telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE); // this is just a reference to a

		IntentFilter intentFilter = new IntentFilter(CommonIntentBundleKeysOld.ACTION_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_LOCATION_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_CELL_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_GPS_STATUS_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_SIGNAL_STRENGTH_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_EVENT_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_NEIGHBOR_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_CONNECTION_UPDATE);


		intentFilter.addAction(CommonIntentActionsOld.ACTION_DONE_SMSTEST);
		context.registerReceiver(enggActivity.broadcastReceiver, intentFilter);
		enggActivity.isVisible = true;


	}

	public void BackPanelClicked(View v) {
		closeEnggPanel();
	}

	static AlertDialog enggPanel;
	static NerdScreen enggpanelAct;
	public static void createEnggPanel (Context context) {
		PackageManager pkMan = context.getPackageManager();
		int systemAlertPermissionValue = pkMan.checkPermission("android.permission.SYSTEM_ALERT_WINDOW", context.getPackageName());
		if (systemAlertPermissionValue == 0) {
			View v = null;
			v = View.inflate(context, R.layout.enggmode_box, null);

			AlertDialog.Builder b = new AlertDialog.Builder(context);
			b.setCancelable(true);
			b.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					enggPanel.dismiss();
					enggPanel = null;
				}
			});
			b.setView(v);
			enggpanelAct = new NerdScreen ();
			enggpanelAct.context = context;
			enggPanel = b.create();
			enggPanel.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//			int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
//					WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
//					WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
//					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
//			enggPanel.getWindow().setFlags (flags, flags);

			initEngg(context, v, enggpanelAct);

//			ImageButton backButton = (ImageButton)v.findViewById(R.id.actionBarBackButton);
//			backButton.setOnClickListener(enggpanelAct.BackPanelClicked);
//			ImageButton backLogo = (ImageButton)v.findViewById(R.id.actionBarLogo);
//			backLogo.setOnClickListener(enggpanelAct.BackPanelClicked);

			//videoDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			enggPanel.show();
			//mBufferProgress = (ProgressBar) videoDialog.findViewById(R.id.buffer_progress);
		}
	}

	public static void closeEnggPanel ()
	{
		if (enggPanel != null)
		{
			enggPanel.dismiss();
			enggPanel = null;
		}
	}



	// handle the tracking menu item with a static function so that other activities can use this, even the map
	public boolean handleStartTracking() {
		if (!Global.isInTracking()) {

			Intent intent = new Intent(this, EngineeringModSettings.class);
			startActivity(intent);
		} else {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NerdScreen.this);
			alertDialogBuilder.setTitle(R.string.GenericText_StopTrack);
			View view = View.inflate(NerdScreen.this, R.layout.stop_recording_text, null);
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
				view.setBackgroundColor(Color.parseColor("#FFFFFF"));
			}
			alertDialogBuilder.setView(view);

			alertDialogBuilder.setNegativeButton(R.string.GenericText_No, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
			alertDialogBuilder.setPositiveButton(R.string.GenericText_Yes, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					startRecordingButton.setVisibility(View.VISIBLE);
					stopRecording.setVisibility(View.GONE);
					recordingEndsIn.setVisibility(View.INVISIBLE);
					Intent intent = new Intent(CommonIntentActionsOld.STOP_TRACKING_ACTION);
					sendBroadcast(intent);
				}
			});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();

		}
		return true;
	}

	private void resumeRecording() {
		if (Global.isInTracking()) {
			long currentTime = System.currentTimeMillis();
			long diff = PreferenceManager.getDefaultSharedPreferences(this).getLong(PreferenceKeys.Miscellaneous.ENGINEER_MODE_EXPIRES_TIME, 0) - currentTime;

			if (Global.isInTracking()) // diff>0){
				startRecordingTimer(diff);

		}
	}

	/* 
	 * Is the MMCService running?
	 */
	private boolean isMMCServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.cortxt.app.mmccore.MMCService".equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method just initializes the local copies of the various views on the activity.
	 */
	private void initViews(Bundle savedInstanceState) {

		// mCarrierLogo = (ImageView) findViewById(R.id.livestatus_carrierlogo);
		// mCarrierLogoBg = (ImageView) findViewById(R.id.livestatus_carrierlogobg);




		/**
		 * This task gets the carrier logo
		 */
		// mGetCarrierLogoTask = new AsyncTask<Void, Void, Bitmap>() {
		// @Override
		// protected Bitmap doInBackground(Void... params) {
		// try {
		// HashMap<String, String> carrier = ((MMCApplication)
		// getApplicationContext()).getDevice().getCarrierProperties();
		// return ReportManager.getInstance(getApplicationContext()).getCarrierLogo(carrier);
		// } catch (MMCException e) {
		// MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "GetCarrierLogoTask","exeption", e);
		// return null;
		// }
		// }
		//
		// @Override
		// protected void onPostExecute(Bitmap result) {
		// if(result != null) {
		// //mCarrierLogo.setImageBitmap(result);
		// }
		// }
		// }.execute((Void[])null);

	}

	/**
	 * The gps status message depends on the accuracy of the last location update and the number of satellites used to
	 * get the last fix. Whenever any of these 2 variables change, this method must be called to update the gps status
	 * message.
	 */
	private void updateGpsStatus() {
		if (numberOfSatellitesUsedInFix < 4 || locationUpdateAccuracy == 0) {
			satelliteStatus.setText(context.getString(R.string.LiveStatus_GPSPartialFix));
		} else {
			satelliteStatus.setText(context.getString(R.string.LiveStatus_GPSFix) + "\n+/- " + (int) locationUpdateAccuracy + "m");
			// if(locationUpdateAccuracy > GpsManagerOld.LOCATION_UPDATE_MIN_TREND_ACCURACY)

			// else
			// satelliteStatus.setText(getString(R.string.LiveStatus_GPSFineFix));
		}
	}

	/**
	 * This method updates the percentometer using the cursor given. It is assumed that the cursor includes either the
	 */
	private void updateNerdViewFromDB() {
		Cursor sig_cursor = null;
		Cursor cell_cursor = null;
		if (nerdview == null)
			return;
		try {
			Uri signalUri = UriMatch.SIGNAL_STRENGTHS.getContentUri();
			Uri limitSignalUri = signalUri.buildUpon().appendQueryParameter("limit", "1").build();
			// sig_cursor = managedQuery(

			Provider dbProvider = ReportManager.getInstance(context.getApplicationContext()).getDBProvider();
			if (dbProvider == null) {
				return;
			}

			sig_cursor = dbProvider.query(UriMatch.SIGNAL_STRENGTHS.getContentUri(), null, // new String[]{
																								// Tables.TIMESTAMP_COLUMN_NAME,
																								// Tables.SignalStrengths.SIGNAL,
																								// Tables.SignalStrengths.LTE_RSRP
																								// },
					null, // Tables.TIMESTAMP_COLUMN_NAME + ">?",
					null, // new String[]{ Long.toString(System.currentTimeMillis() -
							// signalTrendChart.getChartTimespan()) },
					Tables.TIMESTAMP_COLUMN_NAME + " DESC");

			sig_cursor.moveToFirst();

			Uri baseStationTable = (UriMatch.BASE_STATIONS.getContentUri()/*telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA
																				? UriMatch.BASE_STATIONS_CDMA.getContentUri()
																				: UriMatch.BASE_STATIONS_GSM.getContentUri()*/
			);
			Uri limitBSUri = baseStationTable.buildUpon().appendQueryParameter("limit", "1").build();

			// Cursor cell_cursor = managedQuery(
			cell_cursor = dbProvider.query(limitBSUri, null, null, null, Tables.TIMESTAMP_COLUMN_NAME + " DESC");

			cell_cursor.moveToFirst();

			int LowIndex = cell_cursor.getColumnIndex(Tables.BaseStations.BS_LOW);
			int MidIndex = cell_cursor.getColumnIndex(Tables.BaseStations.BS_MID);
			int HighIndex = cell_cursor.getColumnIndex(Tables.BaseStations.BS_HIGH);
			int CodeIndex = cell_cursor.getColumnIndex(Tables.BaseStations.BS_CODE);
			int BandIndex = cell_cursor.getColumnIndex(Tables.BaseStations.BS_BAND);
			int ChanIndex = cell_cursor.getColumnIndex(Tables.BaseStations.BS_CHAN);
			int netTypeIndex = cell_cursor.getColumnIndex(Tables.BaseStations.NET_TYPE);
			String netType = cell_cursor.getString(netTypeIndex);
			int bsLow = cell_cursor.getInt(LowIndex);
			int bsMid = cell_cursor.getInt(MidIndex);
			int bsHigh = cell_cursor.getInt(HighIndex);
			int bsCode = cell_cursor.getInt(CodeIndex);
			int bsBand = cell_cursor.getInt(BandIndex);
			int bsChan = cell_cursor.getInt(ChanIndex);
			if (netType.equals("cdma")) {
				if (LowIndex != -1)
					nerdview.setValue(1, "BID", Integer.toString(bsLow));
				if (MidIndex != -1)
					nerdview.setValue(1, "NID", Integer.toString(bsMid));
				if (HighIndex != -1)
					nerdview.setValue(1, "SID", Integer.toString(bsHigh));
			} else if (netType.equals("gsm")) {
				if (LowIndex != -1) {
					if (bsMid > 0)
						nerdview.setValue(1, "RNC", Integer.toString(bsMid));
					else
						nerdview.setValue(1, "RNC", null);
					nerdview.setValue(1, "Cell ID", Integer.toString(cell_cursor.getInt(LowIndex)));
				}
				// the network Id is kept 0 for gsm phones
				if (HighIndex != -1)
					nerdview.setValue(1, "LAC", Integer.toString(bsHigh));
				else
					nerdview.setValue(1, "LAC", null);
				if (bsCode > 0 && bsCode < 1000)
					nerdview.setValue(1, "PSC", Integer.toString(bsCode));
				else
					nerdview.setValue(1, "PSC", null);
				if (bsBand > 0)
					nerdview.setValue(1, "Band", Integer.toString(bsBand));
				else
					nerdview.setValue(1, "Band", null);
				if (bsChan > 0)
					nerdview.setValue(0, "ARFCN", Integer.toString(bsChan));
				else
					nerdview.setValue(0, "ARFCN", null);
			}

			if (sig_cursor.getCount() != 0) {
				int signalIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.SIGNAL);
				int signal2GIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.SIGNAL2G);
				int rsrpIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.LTE_RSRP);
				int rsrqIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.LTE_RSRQ);
				int lteSnrIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.LTE_SNR);
				int lteSignalIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.LTE_SIGNAL);
				int lteCqiIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.LTE_CQI);
				int ecioIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.ECI0);
				int ecnoIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.ECN0);
				int snrIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.SNR);
				int berIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.BER);
				int rscpIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.RSCP);
				int tierIndex = sig_cursor.getColumnIndex(Tables.SignalStrengths.COVERAGE);
				Integer tier = sig_cursor.isNull(tierIndex) ? null : sig_cursor.getInt(tierIndex);
				Integer rssi = sig_cursor.isNull(signalIndex) ? null : sig_cursor.getInt(signalIndex);
				Integer rssi2G = sig_cursor.isNull(signal2GIndex) ? null : sig_cursor.getInt(signal2GIndex);
				Integer rsrp = sig_cursor.isNull(rsrpIndex) ? null : sig_cursor.getInt(rsrpIndex);
				Float lteSnr = (sig_cursor.isNull(lteSnrIndex) ? null : (float)sig_cursor.getInt(lteSnrIndex));
				Integer lteSignal = sig_cursor.isNull(lteSignalIndex) ? null : sig_cursor.getInt(lteSignalIndex);
				Integer lteCqi = sig_cursor.isNull(lteCqiIndex) ? null : sig_cursor.getInt(lteCqiIndex);
				Integer rsrq = sig_cursor.isNull(rsrqIndex) ? null : sig_cursor.getInt(rsrqIndex);
				Integer eci0 = sig_cursor.isNull(ecioIndex) ? null : sig_cursor.getInt(ecioIndex);
				Integer ecn0 = sig_cursor.isNull(ecnoIndex) ? null : sig_cursor.getInt(ecnoIndex);
				Integer snr = sig_cursor.isNull(snrIndex) ? null : sig_cursor.getInt(snrIndex);
				Integer ber = sig_cursor.isNull(berIndex) ? null : sig_cursor.getInt(berIndex);
				Integer rscp = sig_cursor.isNull(rscpIndex) ? null : sig_cursor.getInt(rscpIndex);

				if (eci0 != null && (netType.equals("cdma") || netType.equals("lte")) && eci0 <= -30)
					eci0 = (eci0 / 10);
				else if (ecn0 != null && ecn0 > 1 && ecn0 < 60 && netType.equals("gsm"))
					ecn0 = -(ecn0 / 2);
				else if (eci0 != null && eci0 > 1 && eci0 < 60 && netType.equals("gsm"))
					eci0 = -(eci0 / 2);

				// if (lteSnr != null && lteSnr > 1 && lteSnr < 500)
				// lteSnr = (lteSnr+5)/10;
				if (lteSignal != null && lteSignal > -120 && lteSignal < -20) // rssi == lteSignal)
				{
					nerdview.setValue(0, "LTE RSSI", simpleValidate(lteSignal, "LTE RSSI", "dBm"));
					nerdview.setValue(0, "RSSI", null);
				} else if (rssi == null || rssi == -255)
					nerdview.setValue(0, "RSSI", context.getString(R.string.GenericText_Unknown));// "Unknown");
				else if (rssi == -256)
					nerdview.setValue(0, "RSSI", context.getString(R.string.GenericText_None));
				else {
					String name = "RSCP: ";
					int spacing = 15;
					if (netType.equals("gsm") && (tier == 3 || tier == 4))
					{
						nerdview.setValue(0, "RSCP", rssi.toString() + " dBm");
						nerdview.setValue(0, "RSSI", null);
					}
					else
					{
						nerdview.setValue(0, "RSSI", rssi.toString() + " dBm");
						nerdview.setValue(0, "RSCP", null);
					}
						
					nerdview.setValue(0, "LTE RSSI", null);
				}
				if (netType.equals("cdma") && rssi2G != null && rssi2G < -30 && rssi2G >= -120)
					nerdview.setValue(0, "CDMA RSSI", rssi2G.toString());
				if (tier == 5) {
					if (lteSnr != null && lteSnr > -101)
						lteSnr = lteSnr / 10;
					if (rsrq != null && rsrq > 0)
						rsrq = -rsrq;
					nerdview.setValue(0, "LTE RSRP", simpleValidate(rsrp, "LTE RSRP", "dBm"));
					nerdview.setValue(0, "LTE RSRQ", simpleValidate(rsrq, "LTE RSRQ", "dB"));
					nerdview.setValue(0, "LTE SNR", simpleValidate(lteSnr, "LTE SNR", ""));
					nerdview.setValue(0, "LTE CQI", simpleValidate(lteCqi, "LTE CQI", ""));
					nerdview.setValue(0, "EC/I0", simpleValidate(eci0, "EC/I0", "dB"));
					nerdview.setValue(0, "EC/N0", simpleValidate(ecn0, "EC/N0", "dB"));
				}
				//nerdview.setValue(0, "RSCP", simpleValidate(rscp, "RSCP", "dBm"));
				nerdview.setValue(0, "BER", simpleValidate(ber, "BER", ""));
				nerdview.setValue(0, "SNR", simpleValidate(snr, "SNR", ""));

				if (rsrp != null && rsrp <= -10 && rsrp >= -140 && tier == 5) {
					if (mChartTitle != null)
						mChartTitle.setText(context.getString(R.string.LiveStatus_SignalLTE));
				}
				else {
					if (mChartTitle != null)
						mChartTitle.setText(context.getString(R.string.LiveStatus_SignalTrend));
					nerdview.setLTEIdentity(null);
				}

				try {
					JSONObject serviceMode = PhoneState.getServiceMode();
					if (serviceMode != null && serviceMode.getLong("time") + 5000 > System.currentTimeMillis()) {
						if (serviceMode.has("rrc") && serviceMode.getString("rrc").length() > 1) {
							nerdview.setValue(0, "RRC", serviceMode.getString("rrc"));
						}
						else
							nerdview.setValue(0, "RRC", null);
						//if (serviceMode.has("band") && serviceMode.getString("band").length() > 0) {
						//	nerdview.setValue(1, "Band", serviceMode.getString("band"));
						//}
						//else
//						if (serviceMode.has("freq") && serviceMode.getString("freq").length() > 0) {
//							nerdview.setValue(1, "Band", serviceMode.getString("freq"));
//						}
//						else
//							nerdview.setValue(1, "Band", null);
//						if (serviceMode.has("channel") && serviceMode.getString("channel").length() > 0) {
//							nerdview.setValue(1, "Channel", serviceMode.getString("channel"));
//						}
//						else
//							nerdview.setValue(1, "Channel", null);
					}
					else
					{
						nerdview.setValue(0, "RRC", null);
						//nerdview.setValue(1, "Band", null);
						//nerdview.setValue(1, "Channel", null);
					}
				}
				catch (Exception e)
				{

				}

			}

			// if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA){
			// //set the cellId using the bid
			// int BIDIndex = cell_cursor.getColumnIndex(Tables.BaseStations.CDMAVersion.BID); //getlowmid high
			// if(BIDIndex != -1)
			// nerdview.setValue(1, "BID",Integer.toString(cell_cursor.getInt(BIDIndex)));
			//
			// //set the network id
			// int NIDIndex = cell_cursor.getColumnIndex(Tables.BaseStations.CDMAVersion.NID);
			// if (NIDIndex != -1)
			// nerdview.setValue(1, "NID",Integer.toString(cell_cursor.getInt(NIDIndex)));
			//
			// //set the system id
			// int SIDIndex = cell_cursor.getColumnIndex(Tables.BaseStations.CDMAVersion.SID);
			// if (SIDIndex != -1)
			// nerdview.setValue(1, "SID",Integer.toString(cell_cursor.getInt(SIDIndex)));
			//
			// } else if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM){
			// //set the cellId using
			// int cellidIndex = cell_cursor.getColumnIndex(Tables.BaseStations.GSMVersion.CELL_ID);
			//
			// if(cellidIndex != -1)
			// {
			// int cellID = cell_cursor.getInt(cellidIndex);
			// int bsic = cellID >> 16;
			// cellID = cellID & 0xFFFF;
			// nerdview.setValue(1, "BSIC",Integer.toString(bsic));
			// nerdview.setValue(1, "Cell ID",Integer.toString(cellID));
			// }
			// //the network Id is kept 0 for gsm phones
			// //set the lac
			// int lacIndex = cell_cursor.getColumnIndex(Tables.BaseStations.GSMVersion.LAC);
			// if (lacIndex != -1)
			// nerdview.setValue(1, "LAC",Integer.toString(cell_cursor.getInt(lacIndex)));
			//
			if (neighborTime + 20000 < System.currentTimeMillis())
				nerdview.setNeighbors(null);

			nerdview.nerdOut(telephonyManager);
			
			Intent intent = new Intent(CommonIntentActionsOld.VIEWING_SIGNAL);
			sendBroadcast(intent);

		} catch (Exception e) {
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "updateNerdViewFromDB", "exception querying signal data: " + e.getMessage());
		} finally {
			if (cell_cursor != null)
				cell_cursor.close();
			if (sig_cursor != null)
				sig_cursor.close();
		}
	}

	private String simpleValidate(Integer signal, String type, String unit) {
		if (signal == null || signal == 0 || signal == -1 || signal == 255 || signal == 99 || signal >= 32767 || signal <= -32767)
			return null;
		return signal.toString() + " " + unit;
	}
	private String simpleValidate(Float signal, String type, String unit) {
		if (signal == null || signal == 0)
			return null;
		return signal.toString() + " " + unit;
	}

	/**
	 * MMCService sends messages to this activity through intents. This class is responsible for receiving those intents
	 * and taking the appropriate action with them.
	 * 
	 * @author abhin
	 * 
	 */
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		Bundle extras;

		@Override
		public void onReceive(Context context, Intent intent) {
			extras = intent.getExtras();

			if (intent.getAction().equals(CommonIntentBundleKeysOld.ACTION_UPDATE)) {
				manageActionUpdate();
			} else if (intent.getAction().equals(CommonIntentBundleKeysOld.ACTION_LOCATION_UPDATE)) {
				manageLocationUpdate();
			} else if (intent.getAction().equals(CommonIntentBundleKeysOld.ACTION_GPS_STATUS_UPDATE)) {
				manageGpsStatusUpdate();
			} else if (intent.getAction().equals(CommonIntentBundleKeysOld.ACTION_SIGNAL_STRENGTH_UPDATE)) {
				manageSignalStrengthUpdate();
			} else if (intent.getAction().equals(CommonIntentBundleKeysOld.ACTION_EVENT_UPDATE)) {
				manageEventUpdate();
			} else if (intent.getAction().equals(CommonIntentBundleKeysOld.ACTION_NEIGHBOR_UPDATE)) {
				manageNeighborUpdate();
			} else if (intent.getAction().equals(CommonIntentBundleKeysOld.ACTION_CELL_UPDATE)) {
				manageCellUpdate();
			} else if (intent.getAction().equals(CommonIntentBundleKeysOld.ACTION_CONNECTION_UPDATE)) {
				manageConnectUpdate();
			}else if (intent.getAction().equals(CommonIntentActionsOld.ACTION_DONE_SMSTEST)) {
				//Double testAverage = intent.getExtras().getDouble("averageTime");
				manageSMSTestDone();
			}
			
		}

		private void manageSMSTestDone() {			
			PreferenceManager.getDefaultSharedPreferences(NerdScreen.this).edit().putBoolean(PreferenceKeys.Miscellaneous.SMS_IN_PROGRESS, false).commit();

		}

		private void manageEventUpdate() {
			if (extras != null) {
				if (extras.containsKey(CommonIntentBundleKeysOld.KEY_UPDATE_EVENT)) {
					EventType event = (EventType) extras.getSerializable(CommonIntentBundleKeysOld.KEY_UPDATE_EVENT);
					if (signalTrendChart != null)
						signalTrendChart.buffer.addEvent(event);
				}
			}
		}

		private void manageSignalStrengthUpdate() {
			if (extras != null) {
				// if (extras.containsKey(KEY_UPDATE_SIGNAL_STRENGTH_PERCENTAGE)){
				// percentometer.setPercentage(extras.getInt(KEY_UPDATE_SIGNAL_STRENGTH_PERCENTAGE));
				// }
				if (extras.containsKey(CommonIntentBundleKeysOld.KEY_UPDATE_SIGNAL_STRENGTH_DBM)) {
					if (signalTrendChart != null)
						signalTrendChart.buffer.addDataPoint(extras.getInt(CommonIntentBundleKeysOld.KEY_UPDATE_SIGNAL_STRENGTH_DBM));
					// nerdview.setSignal(extras.getInt(KEY_UPDATE_SIGNAL_STRENGTH_PERCENTAGE));
					updateNerdViewFromDB();
				}
			}
		}

		private void manageNeighborUpdate() {
			if (extras != null) {
				if (extras.containsKey(CommonIntentBundleKeysOld.KEY_UPDATE_NEIGHBORS)) {
					neighborList = extras.getIntArray(CommonIntentBundleKeysOld.KEY_UPDATE_NEIGHBORS);
					boolean bValid = false;
					for (int i = 0; i < neighborList.length - 1; i += 2)
						if (neighborList[i + 1] > -121)
							bValid = true;
					if (neighborList != null && bValid) {
						neighborTime = System.currentTimeMillis();
						nerdview.setNeighbors(neighborList);
					}
				} else if (extras.containsKey(CommonIntentBundleKeysOld.KEY_UPDATE_LTEID)) {
					lteIdentity = extras.getString(CommonIntentBundleKeysOld.KEY_UPDATE_LTEID);
					if (lteIdentity != null && lteIdentity.length() > 1) {
						neighborTime = System.currentTimeMillis() + 300000; // dont let the LTE identity expire soon, it
																			// should be nulled when no longer LTE
						nerdview.setLTEIdentity(lteIdentity);
					} else
						nerdview.setLTEIdentity(null);
				}

			}
		}

		
		private void manageConnectUpdate() {
			if (extras != null){
				if (extras.containsKey(CommonIntentBundleKeysOld.KEY_UPDATE_CONNECTION)){
					String connectString = extras.getString(CommonIntentBundleKeysOld.KEY_UPDATE_CONNECTION); 
					//nerdview.setValue(0, "Data", connectString);
					nerdview.update(telephonyManager, connectString);
					nerdview.invalidate();
				}
			}
		}
		 

		private void manageCellUpdate() {
			if (extras != null) {
				if (extras.containsKey(CommonIntentBundleKeysOld.KEY_UPDATE_BS_LOW) && extras.containsKey(CommonIntentBundleKeysOld.KEY_UPDATE_BS_HIGH)) {
					Integer bsHigh = (Integer) extras.getSerializable(CommonIntentBundleKeysOld.KEY_UPDATE_BS_HIGH);
					Integer bsMid = (Integer) extras.getSerializable(CommonIntentBundleKeysOld.KEY_UPDATE_BS_MID);
					Integer bsLow = (Integer) extras.getSerializable(CommonIntentBundleKeysOld.KEY_UPDATE_BS_LOW);
					if (signalTrendChart != null)
						signalTrendChart.buffer.addCellID(bsHigh, bsMid, bsLow);
					updateNerdViewFromDB();
				}
			}
		}

		private void manageGpsStatusUpdate() {
			if (extras != null && extras.containsKey(CommonIntentBundleKeysOld.KEY_GPS_STATUS_UPDATE)) {
				if (extras.getBoolean(CommonIntentBundleKeysOld.KEY_GPS_STATUS_UPDATE))
					satelliteStatus.setText(context.getString(R.string.LiveStatus_GPSON));
				else
					satelliteStatus.setText(context.getString(R.string.LiveStatus_GPSOFF));
			}
		}

		private void manageLocationUpdate() {
			if (extras != null && extras.containsKey(CommonIntentBundleKeysOld.KEY_LOCATION_UPDATE)) {
				Location locUpdate = (Location) extras.getParcelable(CommonIntentBundleKeysOld.KEY_LOCATION_UPDATE);
				if (locUpdate == null)
					locationUpdateAccuracy = 1000;
				else {
					locationUpdateAccuracy = locUpdate.getAccuracy();
					if (signalTrendChart != null)
						signalTrendChart.buffer.addLocationPoint(locUpdate);
				}
				// update the gps status
				updateGpsStatus();
			}
		}

		private void manageActionUpdate() {
			if (extras != null) {
				if (extras.containsKey(CommonIntentBundleKeysOld.KEY_UPDATE_SATELLITE_COUNT) && extras.containsKey(CommonIntentBundleKeysOld.KEY_UPDATE_SATELLITES_USED_IN_FIX)) {

					// update the internal variables that will be used to decide the gps status message
					numberOfSatellitesUsedInFix = extras.getInt(CommonIntentBundleKeysOld.KEY_UPDATE_SATELLITES_USED_IN_FIX);
					if (extras.containsKey(CommonIntentBundleKeysOld.KEY_UPDATE_SATELLITE_COUNT) && extras.containsKey(CommonIntentBundleKeysOld.KEY_UPDATE_SATELLITE_COUNT))
						numberOfSatellites = extras.getInt(CommonIntentBundleKeysOld.KEY_UPDATE_SATELLITE_COUNT);
					if (isVisible)
						satelliteCount.updateSatelliteCount(numberOfSatellites, numberOfSatellitesUsedInFix);
					// update the gps status
					updateGpsStatus();
				}
			}
		}
	};

	/**
	 * This class dispatches the various intents on the parent class' behalf so that IPC seems simple to its methods.
	 * 
	 * @author abhin
	 * 
	 */
	class IntentDispatcher {
		public IntentDispatcher() {

		}

		public void triggerUpdateEvent() {
			Intent intent = new Intent(CommonIntentActionsOld.UPDATE_ACTION);
			sendBroadcast(intent);
		}
        public void startWebSocket(boolean isRunning) {

            Intent intent = new Intent(CommonIntentActionsOld.RUN_WEBSOCKET);
            intent.putExtra(CommonIntentActionsOld.EXTRA_START_WEBSOCKET, !isRunning);
            sendBroadcast(intent);
        }

		public void gpsColdReset() {
			Intent intent = new Intent(CommonIntentActionsOld.COLDRESET_ACTION);
			sendBroadcast(intent);
		}

		/**
		 * Broadcasts an intent to start a tracking event.
		 * 
		 * @param numFiveMinutePeriodsToTrack
		 *            The number of five minute periods the tracking event should last for.
		 */
		public void startTracking(int numFiveMinutePeriodsToTrack) {
			Intent intent = new Intent(CommonIntentActionsOld.START_TRACKING_ACTION);
			intent.putExtra(CommonIntentActionsOld.TRACKING_NUM_5_MIN_PERIODS, numFiveMinutePeriodsToTrack);
			sendBroadcast(intent);
		}

		public void stopTracking() {
			Intent intent = new Intent(CommonIntentActionsOld.STOP_TRACKING_ACTION);
			sendBroadcast(intent);
		}
	}

	/**
	 * This class is used to periodically invalidate the chart so that it may refresh.
	 * 
	 * @author Abhin
	 * 
	 */
	class ChartUpdateTask extends TimerTask {
		@Override
		public void run() {
			signalTrendChart.postInvalidate();
			nerdview.update(telephonyManager, "");
		}
	}

	public void BackActionClicked(View v) {
		this.finish();
	}



	public void shareClicked(View v) {
		temporarilyDisableButton(v);
		int customSocialText = (getResources().getInteger(R.integer.CUSTOM_SOCIALTEXT));
		TaskHelper.execute(
				new ShareTask(this, context.getString((customSocialText == 1) ? R.string.sharemessage_nerdmode : R.string.sharemessage_nerdmode), context.getString((customSocialText == 1) ? R.string.sharemessagesubject_nerdmode : R.string.sharemessagesubject_nerdmode), findViewById(R.id.nerd_screen)));
	}

	public void temporarilyDisableButton(final View v) {

		if (v == null)
			return;

		v.setEnabled(false);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				NerdScreen.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						v.setEnabled(true);
					}
				});
			}
		}).start();
	}

	public void startRecordingClicked(View v) {
		handleStartTracking();
	}

	android.view.View.OnClickListener recordingProgressListener = new android.view.View.OnClickListener() {

		@Override
		public void onClick(View v) {

			handleStartTracking();
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			menuActionClicked(menuButton);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (engineerMenu != null && DropDownMenuWindow.isWindowAlreadyShowing) {
				engineerMenu.dismissWindow();
				return true;
			}
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	public void menuActionClicked(View menuButton) {
		if (DropDownMenuWindow.isWindowAlreadyShowing && engineerMenu != null) {
			engineerMenu.dismissWindow();
			return;
		}
		long currentTime = System.currentTimeMillis();
		if (currentTime - DropDownMenuWindow.lastWindowDismissedTime > 200) {
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View menuOptionsView = inflater.inflate(R.layout.engineer_mode_menu_options, null, false);
			ScalingUtility.getInstance(this).scaleView(menuOptionsView);

			TextView gpsColdStartOption = (TextView) menuOptionsView.findViewById(R.id.GpsColdStartOption);
			TextView updateOption = (TextView) menuOptionsView.findViewById(R.id.UpdateOption);
			TextView HistoryOption = (TextView) menuOptionsView.findViewById(R.id.HistoryOption);
			TextView smsTestStartOption = (TextView) menuOptionsView.findViewById(R.id.smsTestOption);
			TextView latencyTestOption = (TextView) menuOptionsView.findViewById(R.id.latencyTestOption);
			TextView voiceTestOption=(TextView)menuOptionsView.findViewById(R.id.VoiceTestOption);
			TextView videoTestOption=(TextView)menuOptionsView.findViewById(R.id.VideoTestOption);
            TextView audioTestOption=(TextView)menuOptionsView.findViewById(R.id.AudioTestOption);
            TextView webTestOption=(TextView)menuOptionsView.findViewById(R.id.WebTestOption);
			TextView youTubeOption=(TextView)menuOptionsView.findViewById(R.id.YoutubeOption);
            TextView websocketOption=(TextView)menuOptionsView.findViewById(R.id.WebSockOption);
			TextView appUsageOption=(TextView)menuOptionsView.findViewById(R.id.AppUsageOption);
			
			FontsUtil.applyFontToTextView(MmcConstants.font_Regular, gpsColdStartOption, this);
			FontsUtil.applyFontToTextView(MmcConstants.font_Regular, updateOption, this);
			FontsUtil.applyFontToTextView(MmcConstants.font_Regular, HistoryOption, this);
			FontsUtil.applyFontToTextView(MmcConstants.font_Regular, smsTestStartOption, this);
			FontsUtil.applyFontToTextView(MmcConstants.font_Regular, latencyTestOption, this);
			FontsUtil.applyFontToTextView(MmcConstants.font_Regular, voiceTestOption, this);
			FontsUtil.applyFontToTextView(MmcConstants.font_Regular, videoTestOption, this);
            FontsUtil.applyFontToTextView(MmcConstants.font_Regular, audioTestOption, this);
            FontsUtil.applyFontToTextView(MmcConstants.font_Regular, webTestOption, this);
			FontsUtil.applyFontToTextView(MmcConstants.font_Regular, youTubeOption, this);
            FontsUtil.applyFontToTextView(MmcConstants.font_Regular, websocketOption, this);
			FontsUtil.applyFontToTextView(MmcConstants.font_Regular, appUsageOption, this);

            if (isWebSocketRunning()) {
                websocketOption.setText("Stop WebSocket");
            } else {
                websocketOption.setText("Start WebSocket");

            }
			
			gpsColdStartOption.setOnClickListener(gpsColdStartClickListener);
			HistoryOption.setOnClickListener(historyClickListener);
			updateOption.setOnClickListener(updateEventClickListener);
			voiceTestOption.setOnClickListener(voiceTestClickListener);
			videoTestOption.setOnClickListener(videoTestClickListener);
            audioTestOption.setOnClickListener(audioTestClickListener);
            webTestOption.setOnClickListener(webTestClickListener);
			youTubeOption.setOnClickListener(youtubeClickListener);
            websocketOption.setOnClickListener(websocketClickListener);
			appUsageOption.setOnClickListener(appUsageClickListener);

			latencyTestOption.setOnClickListener(latencyTestClickListener);
			
			boolean smsInProgress = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceKeys.Miscellaneous.SMS_IN_PROGRESS, false);


			if (PreferenceKeys.getSMSPermissionsAllowed(this, true) == true ){
				if (smsInProgress == false)
					smsTestStartOption.setOnClickListener(smsTestStartClickListener);
				else
					smsTestStartOption.setText("SMS Test In Progress");
			}
			else
			{
				smsTestStartOption.setVisibility(View.GONE);
				menuOptionsView.findViewById(R.id.fourthSeparator).setVisibility(View.GONE);
			}
			
			String videoUrl = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.Miscellaneous.VIDEO_URL, null);
			if (videoUrl == null || videoUrl.length() == 0)
			{
				// Video Test disabled
				videoTestOption.setVisibility(View.GONE);
				menuOptionsView.findViewById(R.id.videoSeparator).setVisibility(View.GONE);
			}

			String youtubeUrl = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.Miscellaneous.YOUTUBE_VIDEOID, null);
			if (youtubeUrl == null || youtubeUrl.length() == 0)
			{
				// Video Test disabled
				youTubeOption.setVisibility(View.GONE);
				menuOptionsView.findViewById(R.id.youSeparator).setVisibility(View.GONE);
			}

            String audioUrl = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.Miscellaneous.AUDIO_URL, null);
            if (audioUrl == null || audioUrl.length() == 0)
            {
                // audioUrl Test disabled
                audioTestOption.setVisibility(View.GONE);
                menuOptionsView.findViewById(R.id.audioSeparator).setVisibility(View.GONE);
            }

            String webUrl = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.Miscellaneous.WEB_URL, null);
            if (webUrl == null || webUrl.length() == 0)
            {
                // Web Test disabled
                webTestOption.setVisibility(View.GONE);
                menuOptionsView.findViewById(R.id.webSeparator).setVisibility(View.GONE);

            }

//            int websock= PreferenceManager.getDefaultSharedPreferences(this).getInt(PreferenceKeys.Miscellaneous.WEBSOCKET, 0);
//            if (websock == 0 || !MMCLogger.isDebuggable())
//            {
//                // Web Test disabled
//                websocketOption.setVisibility(View.GONE);
//                menuOptionsView.findViewById(R.id.websockSeparator).setVisibility(View.GONE);
//            }

			PackageManager pkMan = this.getPackageManager();		
			int voiceCallPermissionValue = pkMan.checkPermission("android.permission.CALL_PHONE", getPackageName());
			
			String voiceDial = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.Miscellaneous.VOICETEST_SERVICE, null);
			if (voiceDial == null || voiceDial.length() == 0 || voiceCallPermissionValue != 0)
			{
				// Voice Test disabled
				voiceTestOption.setVisibility(View.GONE);
				menuOptionsView.findViewById(R.id.sixthSeparator).setVisibility(View.GONE);
			}
			
			int dataMonitor = PreferenceManager.getDefaultSharedPreferences(this).getInt(PreferenceKeys.Miscellaneous.MANAGE_DATAMONITOR, 0);
			if (dataMonitor <= 0)
			{
				// Voice Test disabled
				appUsageOption.setVisibility(View.GONE);
				menuOptionsView.findViewById(R.id.usageSeparator).setVisibility(View.GONE);
			}
			
			engineerMenu = new DropDownMenuWindow(menuOptionsView, this, MmcConstants.MAP_MENU_OFFSET, MmcConstants.GENERAL_MENU_WINDOW_WIDTH);
			engineerMenu.showCalculatorMenu(menuButton);
		}
	}

    protected boolean isWebSocketRunning ()
    {
        boolean b = PreferenceManager.getDefaultSharedPreferences(NerdScreen.this).getBoolean(PreferenceKeys.Miscellaneous.WEBSOCKET_RUNNING, false);
        return b;
    }
	android.view.View.OnClickListener smsTestStartClickListener = new android.view.View.OnClickListener() {

		@Override
		public void onClick(View v) {
			PreferenceManager.getDefaultSharedPreferences(NerdScreen.this).edit().putBoolean(PreferenceKeys.Miscellaneous.SMS_IN_PROGRESS, true).commit();
			
			smsTestInProgress = true;
			Intent intent = new Intent(CommonIntentActionsOld.SMS_TEST);
			//intent.putExtra("fromNerd", 1);
			sendBroadcast(intent);
			
			if (engineerMenu != null) {
				engineerMenu.dismissWindow();
			}
		}
	};
	
	android.view.View.OnClickListener latencyTestClickListener = new android.view.View.OnClickListener() {

		@Override
		public void onClick(View v) {
			
			Intent intent = new Intent(CommonIntentActionsOld.LATENCY_TEST);
			intent.putExtra(CommonIntentBundleKeysOld.EXTRA_SPEED_TRIGGER, 0);
			//intent.putExtra("fromNerd", 1);
			sendBroadcast(intent);
			if (engineerMenu != null) {
				engineerMenu.dismissWindow();
			}
		}
	};

	android.view.View.OnClickListener historyClickListener = new android.view.View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(NerdScreen.this, EventHistory.class);
			intent.putExtra("fromNerd", 1);
			startActivity(intent);
			if (engineerMenu != null) {
				engineerMenu.dismissWindow();
			}
		}
	};
	android.view.View.OnClickListener gpsColdStartClickListener = new android.view.View.OnClickListener() {

		@Override
		public void onClick(View v) {
			intentDispatcher.gpsColdReset();
			if (engineerMenu != null) {
				engineerMenu.dismissWindow();
			}
		}
	};
	android.view.View.OnClickListener updateEventClickListener = new android.view.View.OnClickListener() {

		@Override
		public void onClick(View v) {
			intentDispatcher.triggerUpdateEvent();
			//MMCService.startEvent (NerdScreen.this, EventType.EVT_VQ_CALL);
			//MMCService.startDriveTest(NerdScreen.this, 15, true, 2, 0, 0, 5, 0, 2, 0);

			if (engineerMenu != null) {
				engineerMenu.dismissWindow();
			}
		}
	};

	android.view.View.OnClickListener voiceTestClickListener=new android.view.View.OnClickListener () {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(NerdScreen.this, VoiceQTest.class);
			startActivity(intent);
			if(engineerMenu!=null){
				engineerMenu.dismissWindow();
			}
		}
	};
	
	android.view.View.OnClickListener videoTestClickListener=new android.view.View.OnClickListener () {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(NerdScreen.this, VideoTest.class);
			startActivity(intent);
			if(engineerMenu!=null){
				engineerMenu.dismissWindow();
			}
		}
	};

    android.view.View.OnClickListener audioTestClickListener=new android.view.View.OnClickListener () {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(NerdScreen.this, AudioTest.class);
            startActivity(intent);
            if(engineerMenu!=null){
                engineerMenu.dismissWindow();
            }
        }
    };

    android.view.View.OnClickListener webTestClickListener=new android.view.View.OnClickListener () {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(NerdScreen.this, WebPageTest.class);
			startActivity(intent);
			if(engineerMenu!=null){
				engineerMenu.dismissWindow();
			}
		}
	};

	android.view.View.OnClickListener youtubeClickListener=new android.view.View.OnClickListener () {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(NerdScreen.this, YouTubeTest.class);
			startActivity(intent);
			if(engineerMenu!=null){
				engineerMenu.dismissWindow();
			}
		}
	};

	android.view.View.OnClickListener appUsageClickListener=new android.view.View.OnClickListener () {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(NerdScreen.this, AppUsageStats.class);
			startActivity(intent);
			if(engineerMenu!=null){
				engineerMenu.dismissWindow();
			}
		}
	};

    android.view.View.OnClickListener websocketClickListener=new android.view.View.OnClickListener () {

        @Override
        public void onClick(View v) {
            intentDispatcher.startWebSocket(isWebSocketRunning());
            if (engineerMenu != null) {
                engineerMenu.dismissWindow();
            }
        }
    };

	public void startRecordingTimer(long time) {
		if (startRecordingButton == null)
			return;
		startRecordingButton.setVisibility(View.GONE);
		long startTime = time / (1000);
		long hours = 0;
		long minutes = 0;
		long seconds = 0;

		if (startTime > 0) {
			hours = startTime / 3600;
			startTime %= 3600;
			minutes = startTime / 60;
			seconds = startTime % 60;
			formatTime(hours, minutes, seconds);
			recordingEndsIn.setText(recordingEnds + " " + time);
			if (recordingtimer != null)
				recordingtimer.cancel();
			recordingtimer = new RecordingTime(time, 1000, hours, minutes, seconds);
			recordingtimer.start();
		} else
			recordingEndsIn.setVisibility(View.INVISIBLE);

		recordingEndsIn.setVisibility(View.VISIBLE);
		stopRecording.setVisibility(View.VISIBLE);
	}

	private static void formatTime(long hours, long minutes, long seconds) {
		String time = "";
		if (hours > 0) {
			time = "" + hours + ":";
			if (hours < 10) {
				time = "0" + hours + ":";
			}
		}

		if (minutes < 10) {
			time += "0" + minutes + ":";
		} else {
			time += "" + minutes + ":";
		}

		if (seconds < 10) {
			time += "0" + seconds;
		} else {
			time += "" + seconds;
		}
		recordingEndsIn.setText(recordingEnds + " " + time);
	}

	private static class RecordingTime extends CountDownTimer {
		long minutes = 0;
		long hours = 0;
		long seconds = 0;

		public RecordingTime(long millisInFuture, long countDownInterval, long hours, long minutes, long seconds) {
			super(millisInFuture, countDownInterval);
			this.hours = hours;
			this.minutes = minutes;
			this.seconds = seconds;
		}

		@Override
		public void onFinish() {

			startRecordingButton.setVisibility(View.VISIBLE);
			stopRecording.setVisibility(View.GONE);
			recordingEndsIn.setVisibility(View.INVISIBLE);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			seconds--;
			if (seconds <= 0 && minutes > 0) {
				minutes--;
				seconds = 59;
			} else if (minutes <= 0 && hours > 0) {
				hours--;
				minutes = 59;
				seconds = 59;
			}
			formatTime(hours, minutes, seconds);
		}

	}

}

package com.cortxt.app.uilib.Activities;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cortxt.app.uilib.Activities.CustomViews.Fragments.SpeedTestHistoryFragment;
import com.cortxt.app.uilib.R;
import com.cortxt.app.uilib.utils.FontsUtil;
import com.cortxt.app.uilib.utils.ScalingUtility;
import com.cortxt.app.utillib.DataObjects.Carrier;
import com.cortxt.app.utillib.DataObjects.EventType;
import com.cortxt.app.utillib.DataObjects.PhoneState;
import com.cortxt.app.utillib.Reporters.ReportManager;
import com.cortxt.app.utillib.Reporters.WebReporter.WebReporter;
import com.cortxt.app.utillib.Utils.CommonIntentActionsOld;
import com.cortxt.app.utillib.Utils.CommonIntentBundleKeysOld;
import com.cortxt.app.utillib.Utils.Global;
import com.cortxt.app.utillib.Utils.LocationRequest;
import com.cortxt.app.utillib.Utils.LoggerUtil;
import com.cortxt.app.utillib.Utils.Constants;
import com.cortxt.app.utillib.Utils.PreferenceKeys;
import com.cortxt.app.utillib.Utils.TaskHelper;
import com.cortxt.com.mmcextension.EventTriggers.SpeedTestTrigger;
import com.securepreferences.SecurePreferences;

public class SpeedTest extends MMCTrackedActivityOld {

	private static final float NEEDLE_MAX_ANGLE = 235.0f;
	/**
	 * Download speed that corresponds to highest value on the gauge (in bits/second)
	 */
	private static final int MAX_DOWNLOAD_SPEED = 20000000;
	/**
	 * Upload speed that corresponds to highest value on the gauge (in bits/second)
	 */
	private static final int MAX_UPLOAD_SPEED = 10000000;
	
	/**
	 * Upper limit on angle for which the needle glow should be red.
	 * Lower limit is 0.
	 */
	private static final float NEEDLEGLOW_RED_ANGLE = 40.0f;
	/**
	 * Upper limit on angle for which the needle glow should be yellow.
	 * Lower limit is {@link SpeedTest#NEEDLEGLOW_RED_ANGLE}
	 */
	private static final float NEEDLEGLOW_YELLOW_ANGLE = 80.0f;
	/**
	 * Upper limit on angle for which the needle glow should be green.
	 * Lower limit is {@link SpeedTest#NEEDLEGLOW_YELLOW_ANGLE}
	 */
	private static final float NEEDLEGLOW_GREEN_ANGLE = 135.0f;
	
	private static final float STATS_RADIUS = 8000.0f;
	
	private ResultsListener mResultsListener;
	private AsyncTask<Void, Void, JSONObject> mGetCarrierSpeedTask;
	private TextView mLatency;
	private TextView mDownSpeed;
	private TextView mUpSpeed;
	private ProgressBar mLatencyProgress;
	private ProgressBar mDownloadProgress;
	private ProgressBar mUploadProgress;
	private TextView mCarrierAvg;
	private TextView mCarrierLatency;
	private TextView mCarrierDownSpeed;
	private TextView mCarrierUpSpeed;
	private ImageView mDownloadNeedle;
	private ImageView mUploadNeedle;
	private TextView mGaugeDownSpeed;
	private TextView mGaugeDownUnits;
	private TextView mGaugeUpSpeed;
	private TextView mGaugeUpUnits;
//	private ImageView mDownloadNeedleGlow;
///	private ImageView mUploadNeedleGlow;
	private ImageView mTechnologyIcon;
//	private TextView mShareText;
//	private LinearLayout mShareInfo;
//	private LinearLayout mButtons;
//	private TextView mTimeAndLocation;
	private Location statsLocation = null, nearLocation = null;
	private boolean bFreshGps = false;
	private String mAddress = "";
	private AsyncTask<Void, Void, String> mGetAddressTask;
	private Handler mHandler = new Handler();
	private int bAddressShown = 0;
	private long lastLocationTime = 0;
	private int networkType = 0;
	private int newVariable = 0;
    private int speedRadius = 0;
	private static final String TAG = SpeedTest.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view  = inflater.inflate(R.layout.new_speedtest, null, false);
		setContentView(view);
		
		int height = ScalingUtility.getInstance(this).getCurrentHeight();
		int width = ScalingUtility.getInstance(this).getCurrentWidth();
		
		if (height <= width && android.os.Build.BRAND.toLowerCase().contains("blackberry")) {
			//Special scaling for q10 or square screens
			ImageView mSpeedTestBigGaugaeBase = (ImageView) findViewById(R.id.SpeedTestBigGaugaeBase);			
			MarginLayoutParams linearParams = (MarginLayoutParams) mSpeedTestBigGaugaeBase.getLayoutParams();
			linearParams.topMargin /= 2;
			mSpeedTestBigGaugaeBase.setLayoutParams(linearParams);	
			
//			TextView mBigGaugeSpeedValue = (TextView) findViewById(R.id.bigGaugeSpeedValue);
//			MarginLayoutParams linearParams2 = (MarginLayoutParams) mBigGaugeSpeedValue.getLayoutParams();
//			linearParams2.topMargin += 55;
//			mBigGaugeSpeedValue.setLayoutParams(linearParams2);
//
//			TextView mUploadSpeedValue = (TextView) findViewById(R.id.UploadSpeedValue);
//			MarginLayoutParams linearParams3 = (MarginLayoutParams) mUploadSpeedValue.getLayoutParams();
//			linearParams3.topMargin += 45;
//			mUploadSpeedValue.setLayoutParams(linearParams3);
			
			TableLayout mTableLayout = (TableLayout) findViewById(R.id.resultTableLayout);	
			MarginLayoutParams linearParams4 = (MarginLayoutParams) mTableLayout.getLayoutParams();
			linearParams4.topMargin /= 2;
			mTableLayout.setLayoutParams(linearParams4);
			
			//The scaling utility will over-shrink the gauges on square screens. scale by 1.5 to grow a little
			ScalingUtility.getInstance(this).scaleView(view, 1.0f);
			//This will shrink the table a little. It's default scaling will shrink the table to much, then this will inflate it a little more.
			//ScalingUtility.getInstance(this).scaleView(mTableLayout, 1.2f);
		}
		else 
			ScalingUtility.getInstance(this).scaleView(view, 1.0f);
			
		MMCActivity.customizeTitleBar (this,view,R.string.dashboard_speed, R.string.dashcustom_speed);
		
		mLatency = (TextView) findViewById(R.id.speedtest_latency);
		mDownSpeed = (TextView) findViewById(R.id.speedtest_download);
		mUpSpeed = (TextView) findViewById(R.id.speedtest_upload);
		mLatencyProgress = (ProgressBar) findViewById(R.id.speedtest_latencyprogress);
		mDownloadProgress = (ProgressBar) findViewById(R.id.speedtest_downloadprogress);
		mUploadProgress = (ProgressBar) findViewById(R.id.speedtest_uploadprogress);
		mCarrierAvg = (TextView) findViewById(R.id.speedtest_carrieravg);
		mCarrierLatency = (TextView) findViewById(R.id.speedtest_carrier_latency);
		mCarrierDownSpeed = (TextView) findViewById(R.id.speedtest_carrier_download);
		mCarrierUpSpeed = (TextView) findViewById(R.id.speedtest_carrier_upload);
		mDownloadNeedle = (ImageView) findViewById(R.id.speedtest_needle_download);
		mUploadNeedle = (ImageView) findViewById(R.id.smallGaugeNeedle);
		mGaugeDownSpeed = (TextView) findViewById(R.id.bigGaugeSpeedValue);
		mGaugeDownUnits = (TextView) findViewById(R.id.bigGaugeSpeedType);
		mGaugeUpSpeed = (TextView) findViewById(R.id.UploadSpeedValue);
		mGaugeUpUnits = (TextView) findViewById(R.id.uploadSpeedType);
				
		TextView actionBarTitle=(TextView)view.findViewById(R.id.actionbartitle);
		TextView resultHeading=(TextView)view.findViewById(R.id.ResultHead);
		TextView yourDevice=(TextView)view.findViewById(R.id.YourDeviceHead);
		TextView latencyText=(TextView)view.findViewById(R.id.latencyHead);
		TextView downloadText=(TextView)view.findViewById(R.id.DownloadHead);
		TextView uploadText=(TextView)view.findViewById(R.id.UploadHead);
		Button startButton=(Button)view.findViewById(R.id.startButton);
		Button speedTestProgressButton=(Button)view.findViewById(R.id.TestInProgressButton);
		
		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				PreferenceKeys.Miscellaneous.SPEEDTEST_INPROGRESS, false)) {
			findViewById(R.id.startButton).setVisibility(View.GONE);
			findViewById(R.id.TestInProgressButton).setVisibility(View.VISIBLE);
		}
		
		speedTestProgressButton.setOnClickListener(inProgressOnClickListener);

        MMCActivity.customizeHeadings(this, view, new int[]{R.id.ResultHead,R.id.YourDeviceHead,R.id.speedtest_carrieravg} );
		String labelColor = (getResources().getString(R.string.SPEED_CUSTOM_LABEL_COLOR));
        MMCActivity.customizeSimpleLabelsColor(view,new int[]{R.id.latencyHead,R.id.DownloadHead,R.id.UploadHead,R.id.speedtest_latency,R.id.speedtest_download,
		                                                R.id.speedtest_upload,R.id.speedtest_carrier_latency,R.id.speedtest_carrier_download,R.id.speedtest_carrier_upload},labelColor);
		customizeTopBackgroundColor();
		FontsUtil.applyFontToTextView(Constants.font_Regular, actionBarTitle, this);
		FontsUtil.applyFontToTextView(Constants.font_Regular, mLatency, this);
		FontsUtil.applyFontToTextView(Constants.font_Regular, mDownSpeed, this);
		FontsUtil.applyFontToTextView(Constants.font_Regular, mUpSpeed, this);
		FontsUtil.applyFontToTextView(Constants.font_MEDIUM, mCarrierAvg, this);
		FontsUtil.applyFontToTextView(Constants.font_Regular, mCarrierLatency, this);
		FontsUtil.applyFontToTextView(Constants.font_Regular, mCarrierDownSpeed, this);
		FontsUtil.applyFontToTextView(Constants.font_Regular, mCarrierUpSpeed, this);
		FontsUtil.applyFontToTextView(Constants.font_MEDIUM, mGaugeDownSpeed, this);
		FontsUtil.applyFontToTextView(Constants.font_MEDIUM, mGaugeDownUnits, this);
		FontsUtil.applyFontToTextView(Constants.font_MEDIUM, mGaugeUpSpeed, this);
		FontsUtil.applyFontToTextView(Constants.font_MEDIUM, mGaugeUpUnits, this);
		FontsUtil.applyFontToTextView(Constants.font_MEDIUM, resultHeading, this);
		FontsUtil.applyFontToTextView(Constants.font_MEDIUM, yourDevice, this);
		FontsUtil.applyFontToTextView(Constants.font_Regular, latencyText, this);
		FontsUtil.applyFontToTextView(Constants.font_Regular, downloadText, this);
		FontsUtil.applyFontToTextView(Constants.font_Regular, uploadText, this);
		FontsUtil.applyFontToButton(Constants.font_Regular, startButton, this);
		FontsUtil.applyFontToButton(Constants.font_Regular, speedTestProgressButton, this);
		
		//mDownloadNeedleGlow = (ImageView) findViewById(R.id.speedtest_needleglow_download);
		//mUploadNeedleGlow = (ImageView) findViewById(R.id.speedtest_needleglow_upload);
		mTechnologyIcon = (ImageView) findViewById(R.id.carrierGenerationImage);
		//mShareText = (TextView) findViewById(R.id.speedtest_sharetext);
		//mShareInfo = (LinearLayout) findViewById(R.id.speedtest_shareinfo);
		//mButtons = (LinearLayout) findViewById(R.id.speedtest_buttons);
		//mTimeAndLocation = (TextView) findViewById(R.id.speedtest_timeandlocation);
	//	float density = getResources().getDisplayMetrics().density;
//		if (density <= 1)
//		{
//			mTimeAndLocation.setVisibility(View.GONE);
//			mTimeAndLocation = null;
//			FrameLayout spacer =  (FrameLayout) findViewById(R.id.speedtest_spacer);
//			// Gets the layout params that will allow you to resize the layout
//			
//			android.view.ViewGroup.LayoutParams params = spacer.getLayoutParams();
//			params.height = (int)(40.0*density);
//		}
		
		mResultsListener = new ResultsListener();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CommonIntentActionsOld.ACTION_SPEEDTEST_RESULT);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_SPEEDTEST_ERROR);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_SPEEDTEST_COMPLETE);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_SPEEDTEST_CANCELLED);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_SIGNAL_STRENGTH_UPDATE);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_CONNECTION_UPDATE);
		
		registerReceiver(mResultsListener, intentFilter);
		
		setTechnologyIcon();
		setShareText();
		startLocation();
		showStoredStats ();
		//getCarrierSpeed();
		
		//boolean bShowMapTip = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceKeys.Miscellaneous.SHOW_SPEED_TIP, true);
		int activeconn = 0;
		if ((activeconn = ActiveConnection()) > 0)
		{
			int customText = (getResources().getInteger(R.integer.CUSTOM_COMPARENAMES));
			
			if (activeconn == 10)
				mCarrierAvg.setText (getString((customText == 1)?R.string.speedtest_custom_wifiavg:R.string.speedtest_wifiavg));
			else if (activeconn == 11)
				mCarrierAvg.setText (getString((customText == 1)?R.string.speedtest_custom_wimaxavg:R.string.speedtest_wimaxavg));
			else if (activeconn == 12)
				mCarrierAvg.setText (getString((customText == 1)?R.string.speedtest_custom_ethernetavg:R.string.speedtest_ethernetavg));
			else if (activeconn == 5)
				mCarrierAvg.setText ("LTE " + getString((customText == 1)?R.string.speedtest_custom_carrieravg:R.string.speedtest_carrieravg));
			else if (activeconn == 4 || activeconn == 3)
				mCarrierAvg.setText ("3G/4G " + getString((customText == 1)?R.string.speedtest_custom_carrieravg:R.string.speedtest_carrieravg));
			else if (activeconn == 2 || activeconn == 1)
				mCarrierAvg.setText ("2G " + getString((customText == 1)?R.string.speedtest_custom_carrieravg:R.string.speedtest_carrieravg));
				
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(PreferenceKeys.Miscellaneous.SHOW_SPEED_TIP, false).commit();
			String msg = this.getString(R.string.GenericText_SpeedTip);
			Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
			toast.show();
		}
		
//		IntentFilter locationFilter = new IntentFilter();
//		locationFilter.addAction(MMCIntentHandlerOld.ACTION_GPS_LOCATION_UPDATE);
//		locationFilter.addAction(MMCIntentHandlerOld.ACTION_NETWORK_LOCATION_UPDATE);
//		registerReceiver(broadcastReceiver, locationFilter);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(mResultsListener);
		
		if(mGetCarrierSpeedTask != null) {
			mGetCarrierSpeedTask.cancel(true);
		}
		if (locationRequest != null)
			locationRequest.stop();
	}
		
	android.view.View.OnClickListener inProgressOnClickListener = new android.view.View.OnClickListener() {

		@Override
		public void onClick(View v) {			
			AlertDialog.Builder builder = new AlertDialog.Builder(SpeedTest.this);
			builder.setMessage(getApplicationContext().getString(R.string.speedtest_stop));
			builder.setCancelable(false);
			builder.setPositiveButton(getString(R.string.GenericText_Yes), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {					
					Intent intent = new Intent(CommonIntentActionsOld.ACTION_STOP_SPEEDTEST);
					LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "OnClickListener", "click stop speedtest");
					sendBroadcast(intent);
					findViewById(R.id.startButton).setVisibility(View.VISIBLE);
					findViewById(R.id.TestInProgressButton).setVisibility(View.GONE);
					dialog.dismiss();
				}
			});
			builder.setNegativeButton(getString(R.string.GenericText_No), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			
			AlertDialog alert = builder.create();
			alert.show();
		}
	};
	
	public void BackActionClicked(View view){
		this.finish();
	}
	
	private void customizeTopBackgroundColor(){
		String bgColor = (getResources().getString(R.string.SPEEDTEST_TOP_BACK_COLOR));
		View topBackground=findViewById(R.id.upperBackground);    
		if(bgColor != null && bgColor.length()>0){
		   int color=Integer.parseInt(bgColor,16) + (0xff000000);
		   topBackground.setBackgroundColor(color);
		  }
		
		}
//	public void startLocation() {
//		Location location = ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).
//				getLastKnownLocation(LocationManager.GPS_PROVIDER);
//		if (location == null)
//			location = ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).
//				getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//		if (location == null)
//			location = ReportManager.getInstance(getApplicationContext()).getLastKnownLocation ();
//		nearLocation = location;
//		showAddress (location);
//		if (location != null && location.getLatitude() != 0)
//		{
//			getCarrierSpeed(location);
//		}
//		brieflyRunLocation (60);
//
//	}
	
	private LocationRequest locationRequest;
	public void startLocation() {
		locationRequest = new LocationRequest (this, 400);
		locationRequest.setUpdateUI(true);
		locationRequest.setOnNewLocationListener(new LocationRequest.OnLocationListener() {
			@Override
			public void onLocation(LocationRequest locationRequest) {
				if (locationRequest.bLocationChanged == false)
					return;
				Location location = locationRequest.getLocation();
				getCarrierSpeed(location);
				statsLocation = location;
				nearLocation = location;
				showAddress(location);
				bFreshGps = true;
				lastLocationTime = System.currentTimeMillis();
			}
		});
		locationRequest.start ();

	}
	
	// Handler will be called up to 3 times: instant lastLocation, first networkLocation, first good GPS Fix
	private Handler locationHandler = new Handler() 
	{
		@Override
		public void handleMessage (Message msg) {
			Location location = locationRequest.getLocation();
			getCarrierSpeed(location);
			statsLocation = location;
			nearLocation = location;
			showAddress (location);
			bFreshGps = true;
			lastLocationTime = System.currentTimeMillis();
		}
		
		
	};
	
	
//	public void brieflyRunLocation (int timeoutSeconds)
//	{
//		GpsListenerForStats locListener = new GpsListenerForStats();
//		locListener.setFirstFixTimeout(timeoutSeconds*1000); // using our own timeout to force gps off after timeoutSeconds
//		locListener.setOperationTimeout(0);
//		locListener.setProvider (LocationManager.GPS_PROVIDER);
//		if (MainService.getGpsManager() != null)
//			MainService.getGpsManager().registerListener(locListener);
//		
//		LocationListenerForStats locListener2 = new LocationListenerForStats();
//		locListener2.setFirstFixTimeout(20*1000); // using our own timeout to force gps off after timeoutSeconds
//		locListener2.setOperationTimeout(0);
//		locListener2.setProvider (LocationManager.NETWORK_PROVIDER);
//		if (MainService.getNetLocationManager() != null)
//			MainService.getNetLocationManager().registerListener(locListener2);
//	}
	
//	(int timeoutSeconds, String provider, boolean triggerUpdate, GpsListenerOld listener)
//	public void brieflyRunLocation (int timeoutSeconds) {
////		GpsListenerForStats locListener = new GpsListenerForStats();
////		locListener.setFirstFixTimeout(timeoutSeconds*1000); // using our own timeout to force gps off after timeoutSeconds
////		locListener.setOperationTimeout(0);
////		locListener.setProvider(LocationManager.GPS_PROVIDER);
////		if (MainService.getGpsManager() != null)
////			MainService.getGpsManager().registerListener(locListener);
//		Intent intentGPS = new Intent(MMCIntentHandlerOld.ACTION_BRIEFLY_RUN_LOCATION);  
//		intentGPS.putExtra("timeoutSeconds", timeoutSeconds*1000);
//		intentGPS.putExtra("provider", LocationManager.GPS_PROVIDER);
//		intentGPS.putExtra("triggerUpdate", false);
//		sendBroadcast(intentGPS);
//		
////		LocationListenerForStats locListener2 = new LocationListenerForStats();
////		locListener2.setFirstFixTimeout(20*1000); // using our own timeout to force gps off after timeoutSeconds
////		locListener2.setOperationTimeout(0);
////		locListener2.setProvider(LocationManager.NETWORK_PROVIDER);
////		if (MainService.getNetLocationManager() != null)
////			MainService.getNetLocationManager().registerListener(locListener2);
//		Intent intentNetwork = new Intent(MMCIntentHandlerOld.ACTION_BRIEFLY_RUN_LOCATION);  
//		intentNetwork.putExtra("timeoutSeconds", 20*1000);
//		intentNetwork.putExtra("provider", LocationManager.NETWORK_PROVIDER);
//		intentNetwork.putExtra("triggerUpdate", false);
//		sendBroadcast(intentNetwork);
//	}

	/*
	 * Tell Service to start a speed test, screen will listen for the results
	 * If service has been stopped, we'll remind the user to start the mmc service
	 */
	public void startClicked(View button) {
		
		if (!Global.isMainServiceRunning(this))
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setCancelable(true);
			//builder.setIcon(R.drawable.ic_mmclauncher);
			
			String strTitle = getString (R.string.GenericText_AskStartService);
			String strText = getString (R.string.GenericText_AskStartServiceDescription);
			String appname = Global.getAppName(SpeedTest.this);
			if (!appname.equals("MyMobileCoverage"))
			{
				strTitle = strTitle.replaceAll("MyMobileCoverage", appname);
				strText = strText.replaceAll("MyMobileCoverage", appname);
			}
			builder.setTitle(strTitle);
			builder.setMessage(strText);
			builder.setInverseBackgroundForced(true);
			builder.setPositiveButton(getString(R.string.GenericText_Yes), new DialogInterface.OnClickListener() {
			  @Override
			  public void onClick(DialogInterface dialog, int which) {
				// launch settings screen
				  Global.startService(SpeedTest.this);
				  trackEvent ("StartStop", "start", "SpeedTest", 0);
                  SecurePreferences securePrefs = PreferenceKeys.getSecurePreferences (SpeedTest.this);
                  securePrefs.edit().putBoolean(PreferenceKeys.Miscellaneous.STOPPED_SERVICE, false).commit();
				  dialog.dismiss();
			  }
			});
			builder.setNegativeButton(getString(R.string.GenericText_No), new DialogInterface.OnClickListener() {
			  @Override
			  public void onClick(DialogInterface dialog, int which) {
			    dialog.dismiss();
			  }
			});
			AlertDialog alert = builder.create();
			alert.show();
			return;
		}
		findViewById(R.id.startButton).setVisibility(View.GONE);
		findViewById(R.id.TestInProgressButton).setVisibility(View.VISIBLE);
		Intent intent = new Intent(CommonIntentActionsOld.SPEED_TEST);
		intent.putExtra(CommonIntentBundleKeysOld.EXTRA_SPEED_TRIGGER, 0);
		intent.putExtra(CommonIntentBundleKeysOld.EXTRA_UPDATE_UI, true);
		sendBroadcast(intent);
		trackEvent ("SpeedTest", "start", "", 0);
	}

	private void showStoredStats ()
	{
		try
		{
			ReportManager reportManager = ReportManager.getInstance(getApplicationContext());
			List<Carrier> operators = reportManager.getTopOperators (0, 0, 0, 0, 15, false);
			if (operators != null)
			{
				int speedTier = ActiveConnection ();
				if (speedTier == 0)
					speedTier = PhoneState.getNetworkGeneration(networkType);
				JSONObject stats = reportManager.getTopCarriersStats(operators, 0, 0, 0, 0, speedTier, false);
				if (stats != null)
				{
					Carrier currentCarrier = ReportManager.getInstance(getApplicationContext()).getCurrentCarrier();
					JSONObject carrierstats = stats.getJSONObject(currentCarrier.OperatorId);
					displayStats (carrierstats);
				}
			}
			
		}
		catch (Exception e) {
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "getStats", "", e);
			if (e.getCause() instanceof UnknownHostException || e.getCause() instanceof IOException)
				showError (R.string.GenericText_UnknownHost);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
//		mShareInfo.setVisibility(View.GONE);
//		
//		mButtons.setVisibility(View.VISIBLE);
//		if (mTimeAndLocation != null)
//		{
//			mTimeAndLocation.setGravity(1);  // left align when share
//			float density = getResources().getDisplayMetrics().density;
//			mTimeAndLocation.setPadding(0, (int)(5.0*density), 0, 0);
//		}
	}

	
	public void shareClicked(View button) {
		temporarilyDisableButton(button);
		
//		if(mDownloadNeedleGlow.getTag() != null) 
//		{
			//mShareInfo.setVisibility(View.VISIBLE);
			float density = getResources().getDisplayMetrics().density;
			int width = getResources().getDisplayMetrics().widthPixels;
//			if (mTimeAndLocation != null)
//			{
//				mTimeAndLocation.setGravity(3);  // left align when share
//				if (width > 320*density)
//					mTimeAndLocation.setPadding((int)(52.0*density), (int)(6*density), 0, 0);
//				else
//					mTimeAndLocation.setPadding((int)(36.0*density), 0, 0, 0);
//			}
//			mButtons.setVisibility(View.GONE);
			//if (width > 320*density)
				//mShareText.setPadding((width-(int)(320*density))/2, 0, 0, 6);
//			else if (density <= 1.0)
//			{
//				mShareText.setPadding(1, 0, 0, 1);
//				mShareText.setCompoundDrawablePadding (2);
//			}
			
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					String message;
					int customSocialText = (getResources().getInteger(R.integer.CUSTOM_SOCIALTEXT));
					String subject = getString((customSocialText == 1)?R.string.sharecustomsubject_speedtest:R.string.sharemessagesubject_speedtest);
					//int needleGlowImage = (Integer) mDownloadNeedleGlow.getTag();
					int activeconn = ActiveConnection ();
					if(activeconn == 10)
					{
						message = getString((customSocialText == 1)?R.string.sharecustom_speedtest_wifi:R.string.sharemessage_speedtest_wifi);
						subject = getString((customSocialText == 1)?R.string.sharecustomsubject_speedtestwifi:R.string.sharemessagesubject_speedtestwifi);
					}
					else if (activeconn == 11)
					{
						message = getString((customSocialText == 1)?R.string.sharecustom_speedtest_wimax:R.string.sharemessage_speedtest_wimax);
						subject = getString((customSocialText == 1)?R.string.sharecustomsubject_speedtestwimax:R.string.sharemessagesubject_speedtestwimax);
					}
//					else if(needleGlowImage == R.drawable.speedtest_needleglow_large_green ||
//							needleGlowImage == R.drawable.speedtest_needleglow_large_blue)
//						message = getString(R.string.sharemessage_speedtest_goodspeed);
					else
						message = getString((customSocialText == 1)?R.string.sharecustom_speedtest_poorspeed:R.string.sharemessage_speedtest_poorspeed);
					TaskHelper.execute(
							new ShareTask(SpeedTest.this, message, subject, findViewById(R.id.speedtest_container))); // .execute((Void[])null);
				}
			}, 1);
			
		}
	//}
	
	public void temporarilyDisableButton(final View v) {
		
		if(v == null)
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
                SpeedTest.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.setEnabled(true);
                    }
                });
            }
        }).start();
	}
	
	public void historyClicked(View button) {
//		List<HashMap<String, Long>> results = ReportManager.getInstance(getApplicationContext()).getSpeedTestResults(0, Long.MAX_VALUE);
//		if(results.size()<=0){
//			Toast toast = Toast.makeText(this, R.string.speedtest_no_test_history, Toast.LENGTH_SHORT);
//			toast.setGravity(Gravity.CENTER, 0, 0);
//			toast.show();
//		}else
		{
			Intent intent = new Intent(this, SpeedTestHistory.class);
			intent.putExtra ("EVENTTYPE", EventType.MAN_SPEEDTEST.getIntValue());
			startActivity(intent);
		}

//		Intent intent = new Intent(this, SpeedTestHistory.class);
//		startActivity(intent);
	}
	
	private void displayAddress (String address)
	{
//		if (mTimeAndLocation != null)
//		{
//			mTimeAndLocation.setText(address );
//		}
         if (speedRadius > 0)
        {
            String sRadius = String.format(getString(R.string.speedtest_radius), speedRadius);

            address += " " + sRadius;
        }

		 if (bAddressShown < 2)
			Toast.makeText(SpeedTest.this, address, Toast.LENGTH_LONG).show();
        if (speedRadius > 0)
		    bAddressShown ++;
	}
	private void showAddress (final Location location)
	{
		if (location == null || location.getLatitude() == 0.0)
		{
			displayAddress (getString(R.string.mystats_unknownlocation));
			bAddressShown = 0;
			return;
		}

		mGetAddressTask = new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				return WebReporter.geocode(SpeedTest.this, location);
			}

			@Override
			protected void onPostExecute(String result) {
				long timeStamp = System.currentTimeMillis();
				DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
				String dateTime = dateTimeFormat.format(new Date(timeStamp));

				String address = "";
				if(result != null)
					address = result;
				if (address.length() < 3)
					if (nearLocation != null && nearLocation.getLatitude() != 0)
						address = String.format("%.4f, %.4f", nearLocation.getLatitude(), nearLocation.getLongitude());
					else
						address = "unknown";
				int customText = (getResources().getInteger(R.integer.CUSTOM_COMPARENAMES));

				int speedTier = ActiveConnection ();
				if (speedTier == 0)
					speedTier = PhoneState.getNetworkGeneration(networkType);
				if (speedTier == 10)
					mAddress = getString((customText == 1)?R.string.speedtest_custom_wifiavgnear:R.string.speedtest_wifiavgnear);
				else if (speedTier == 11)
					mAddress = getString((customText == 1)?R.string.speedtest_custom_wimaxavgnear:R.string.speedtest_wimaxavgnear);
				else if (speedTier == 12)
					mAddress = getString((customText == 1)?R.string.speedtest_custom_ethernetavgnear:R.string.speedtest_ethernetavgnear);
				else if (speedTier < 3)
					mAddress = getString((customText == 1)?R.string.speedtest_custom_carrier2Gavg:R.string.speedtest_carrier2Gavg);
				else if (speedTier < 5)
					mAddress = getString((customText == 1)?R.string.speedtest_custom_carrier3Gavg:R.string.speedtest_carrier3Gavg);
				else if (speedTier < 6)
					mAddress = getString((customText == 1)?R.string.speedtest_custom_carrierLTEavg:R.string.speedtest_carrierLTEavg);

				mAddress = mAddress + " " + address;
				displayAddress (mAddress);

			}

		};
		TaskHelper.execute(mGetAddressTask);
	}

	private void getCarrierSpeed(final Location location) {
		//if(!isNetworkWifi())
		{
			mGetCarrierSpeedTask = new AsyncTask<Void, Void, JSONObject>() {
				
				@Override
				protected JSONObject doInBackground(Void... params) {
					try {
						ReportManager reportManager = ReportManager.getInstance(getApplicationContext());
						long startTime = System.currentTimeMillis() - 1;
						
						double latitude = 0, longitude = 0;
						float radius = Float.MAX_VALUE;
						
						//Location location = statsLocation;
						
						if(location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
							radius = STATS_RADIUS;
						}
						int speedTier = ActiveConnection();
						if (speedTier == 0)
							speedTier = PhoneState.getNetworkGeneration(networkType);
						LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "SpeedTest", "getCarrierSpeed", "request stats for lat,lng " + latitude + "," + longitude);
						//HashMap<String, String> carrierStats = reportManager.getCarrierStats(carrier, startTime, System.currentTimeMillis(), latitude, longitude, radius, speedTier);
						JSONObject stats = reportManager.getTopCarriersStats(null, 0, latitude, longitude, 6000, speedTier, true);
						Carrier currentCarrier = ReportManager.getInstance(getApplicationContext()).getCurrentCarrier();
                        String sRadius = stats.getString("radius");
                        if (sRadius != null)
                            speedRadius = ((int)Double.parseDouble(sRadius) + 500) / 1000;
						JSONObject carrierstats = stats.getJSONObject(currentCarrier.OperatorId);
						return carrierstats;
					}
					catch (Exception e) {
						LoggerUtil.logToFile(LoggerUtil.Level.ERROR, "SpeedTest", "getCarrierSpeed", "error", e);
						if (e.getCause() instanceof IOException)
							showError (R.string.GenericText_UnknownHost);
						return null;
					}
				}
	
				@Override
				protected void onPostExecute(JSONObject result) {
					if(result != null)
						displayStats (result);
					else {
						//Toast.makeText(SpeedTest.this, R.string.speedtest_carrierspeed_error, Toast.LENGTH_LONG).show();
					}
				}
				
			};
			TaskHelper.execute(mGetCarrierSpeedTask);
		}

	}
	
	private void displayStats (JSONObject stats)
	{
		try
		{

			int latency = 0, upSpeed= 0, downSpeed = 0;
			if (stats.has(ReportManager.StatsKeys.DOWNLOAD_SPEED_AVERAGE) && !stats.isNull(ReportManager.StatsKeys.DOWNLOAD_SPEED_AVERAGE))
				downSpeed = stats.getInt(ReportManager.StatsKeys.DOWNLOAD_SPEED_AVERAGE);
			if (stats.has(ReportManager.StatsKeys.UPLOAD_SPEED_AVERAGE) && !stats.isNull(ReportManager.StatsKeys.UPLOAD_SPEED_AVERAGE))
				upSpeed = stats.getInt(ReportManager.StatsKeys.UPLOAD_SPEED_AVERAGE);
			if (stats.has(ReportManager.StatsKeys.LATENCY_AVERAGE) && !stats.isNull(ReportManager.StatsKeys.LATENCY_AVERAGE))
				latency = stats.getInt(ReportManager.StatsKeys.LATENCY_AVERAGE);
			if (downSpeed == 0)
				mCarrierDownSpeed.setText("-");
			else
				mCarrierDownSpeed.setText(getFormattedSpeed(downSpeed) + " " + getUnits(downSpeed));
			if (upSpeed == 0)
				mCarrierUpSpeed.setText("-");
			else
				mCarrierUpSpeed.setText(getFormattedSpeed(upSpeed) + " " + getUnits(upSpeed));
			if (latency == 0)
				mCarrierLatency.setText("-");
			else
				mCarrierLatency.setText(latency + " " + getString(R.string.speedtest_milliseconds));

            if (mAddress != null && mAddress.length() > 1)
                displayAddress (mAddress);
		}
		catch (Exception e) {
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, "SpeedTest", "getCarrierSpeed.onPostExecute", "error", e);
			
		}
	}
	private void showError (final int stringres)
	{
		runOnUiThread (new Runnable() {
			@Override
			public void run() {Toast.makeText(SpeedTest.this, stringres, Toast.LENGTH_LONG).show();}});
	}
	
	private void setTechnologyIcon() {
		int activeconn = ActiveConnection ();
		if(activeconn == 0) {
			networkType = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getNetworkType();
			int networkGeneration = PhoneState.getNetworkGeneration(networkType);
			switch(networkGeneration) {
			case 2:
				mTechnologyIcon.setImageResource(R.drawable.speedtest_tech_2g);
				break;
			case 3:
				mTechnologyIcon.setImageResource(R.drawable.speedtest_tech_3g);
				break;
			case 4:
				mTechnologyIcon.setImageResource(R.drawable.speedtest_tech_3g);
				break;
			case 5:
				mTechnologyIcon.setImageResource(R.drawable.speedtest_tech_lte);
				break;
			}
		}
		else if (activeconn == 10) {
			mTechnologyIcon.setImageResource(R.drawable.speedtest_tech_wifi);
		}
		else if (activeconn == 11) {
			mTechnologyIcon.setImageResource(R.drawable.speedtest_tech_wimax);
		}
		else if (activeconn == 12) {
			mTechnologyIcon.setImageResource(R.drawable.speedtest_tech_ethernet);
		}
	}
	
	private void setShareText() {
		String carrier = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName();
		int activeconn = ActiveConnection ();
		if (activeconn == 10)
			carrier = "WiFi";
		else if (activeconn == 11)
			carrier = "WiMAX";
		else if (activeconn == 12)
			carrier = "Ethernet";
		String phone = "";
		if(android.os.Build.BRAND.length() > 0) {
			String brand = android.os.Build.BRAND.substring(0, 1).toUpperCase() + android.os.Build.BRAND.substring(1);
			phone = " - " + brand + " " + android.os.Build.MODEL;
		}
		long timeStamp = System.currentTimeMillis();
		DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
		String dateTime = dateTimeFormat.format(new Date(timeStamp));
		
		String shareText = carrier + phone + "\n" + dateTime;
		//mShareText.setText(shareText);
	}
	
	/**
	 * Set download speed on gauge and rotate needle
	 * @param downSpeed download speed in bits/second
	 */
	private void setGaugeDownloadSpeed(int downSpeed) {
		mGaugeDownSpeed.setText(getFormattedSpeed(downSpeed));
		mGaugeDownUnits.setText(getUnits(downSpeed));
		rotateNeedle(mDownloadNeedle, downSpeed, MAX_DOWNLOAD_SPEED);
	}
	
	/**
	 * Set upload speed on guage and rotate needle
	 * @param upSpeed upload speed in bits/second
	 */
	private void setGaugeUploadSpeed(int upSpeed) {
		mGaugeUpSpeed.setText(getFormattedSpeed(upSpeed));
		mGaugeUpUnits.setText(getUnits(upSpeed));
		rotateNeedle(mUploadNeedle, upSpeed, MAX_UPLOAD_SPEED);
	}
	
	/**
	 * Rotate the given needle image based on the given speed and maxSpeed
	 * @param needle image of needle to rotate
	 * @param speed speed to show on gauge (in bits/second)
	 * @param maxSpeed maximum speed on the gauge (in bits/second)
	 */
	private void rotateNeedle(ImageView needle, int speed, int maxSpeed) {
		float angle = getNeedleAngle(speed, maxSpeed);
		
		float startingAngle = 0.0f;
		if(needle.getTag() != null)
			startingAngle = (Float) needle.getTag();
		
		RotateAnimation animation = new RotateAnimation(startingAngle, angle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(200);
		animation.setFillAfter(true);
		needle.startAnimation(animation);
		needle.setTag(angle);
	}
	
	/**
	 * Get needle angle based on the speed to show and the maximum speed on the gauge.
	 * @param speed speed to show on gauge (in bits/second)
	 * @param maxSpeed maximum speed on gauge (in bits/second)
	 */
	private float getNeedleAngle(int speed, int maxSpeed) {
		float speedRatio = ((float) speed / (float) maxSpeed);
		float angleRatio = (float) (Math.log10(speedRatio + 0.1) + 1.0);
		angleRatio = Math.min(angleRatio, 1.0f);
		float angle = angleRatio * NEEDLE_MAX_ANGLE;
		return angle;
	}
	
	private void setDownloadNeedleGlow(int speed) {
		float angle = getNeedleAngle(speed, MAX_DOWNLOAD_SPEED);
		int glowImage;
		
		if(angle < NEEDLEGLOW_RED_ANGLE) {
			glowImage = R.drawable.speedtest_needleglow_large_red;
		}
		else if(angle < NEEDLEGLOW_YELLOW_ANGLE) {
			glowImage = R.drawable.speedtest_needleglow_large_yellow;
		}
		else if(angle < NEEDLEGLOW_GREEN_ANGLE){
			glowImage = R.drawable.speedtest_needleglow_large_green;
		}
		else {
			glowImage = R.drawable.speedtest_needleglow_large_blue;
		}
		
//		mDownloadNeedleGlow.setImageResource(glowImage);
//		mDownloadNeedleGlow.setTag(glowImage);
		RotateAnimation animation = new RotateAnimation(angle, angle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(1);
		animation.setFillAfter(true);
		//mDownloadNeedleGlow.startAnimation(animation);
	}
	
	private void setUploadNeedleGlow(int speed) {
		float angle = getNeedleAngle(speed, MAX_UPLOAD_SPEED);
		int glowImage;
		
		if(angle < NEEDLEGLOW_RED_ANGLE) {
			glowImage = R.drawable.speedtest_needleglow_small_red;
		}
		else if(angle < NEEDLEGLOW_YELLOW_ANGLE) {
			glowImage = R.drawable.speedtest_needleglow_small_yellow;
		}
		else if(angle < NEEDLEGLOW_GREEN_ANGLE){
			glowImage = R.drawable.speedtest_needleglow_small_green;
		}
		else {
			glowImage = R.drawable.speedtest_needleglow_small_blue;
		}
		
		//mUploadNeedleGlow.setImageResource(glowImage);
		RotateAnimation animation = new RotateAnimation(angle, angle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(1);
		animation.setFillAfter(true);
		//mUploadNeedleGlow.startAnimation(animation);
	}
	
	/**
	 * Gets the appropriate units for the given speed (kbps or mbps)
	 * @param speed transfer speed in bits/second
	 * @return the appropriate units for the given speed
	 */
	private String getUnits(int speed) {
		if(speed >=0) {
			return getString(R.string.speedtest_mbps);
		}
		else {
			return getString(R.string.speedtest_kbps);
		}
	}
	
	/**
	 * Gets the speed formatted for displaying, and with the units given by {@link SpeedTest#getUnits(int)}
	 * @param speed transfer speed in bits/second
	 * @return the speed formatted for displaying
	 */
	private String getFormattedSpeed(int speed) {
		if (speed == -1 || Math.abs(speed) > 1000000000)
			return "-";
		if(speed >= 0) {
			return String.format("%2.1f", (float)speed / 1000000.0f);
		}
		else {
			return String.format("%2.1f", (float)speed / 1000.0f);
		}
	}
	/*
	private boolean isNetworkWifi() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager != null) {
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			if (networkInfo != null) {
				int wifiState = networkInfo.getType();
				return (wifiState == ConnectivityManager.TYPE_WIFI);
			}
		}
		return false;
	}
	*/
	// Detect if a Wifi or Wimax connection is open
	protected int ActiveConnection () {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager != null) {
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			if (networkInfo != null) {
				int wifiState = networkInfo.getType();
				if (wifiState == ConnectivityManager.TYPE_WIFI)
					return 10;
				else if (wifiState == PhoneState.TYPE_WIMAX)
					return 11;
				else if (wifiState == PhoneState.TYPE_ETHERNET)
					return 12;
			}
		}
		return 0;
	}
	/**
	 * BroadcastReceiver that listens to results of speed test
	 * @author nasrullah
	 *
	 */
	class ResultsListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(CommonIntentActionsOld.ACTION_SPEEDTEST_RESULT)) {
				if(intent.hasExtra(CommonIntentBundleKeysOld.EXTRA_LATENCY)) {
					int latencyProgress = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_LATENCY_PROGRESS, 0);
					if(latencyProgress < 100) {
						mLatencyProgress.setVisibility(View.VISIBLE);
						mLatency.setVisibility(View.INVISIBLE);
						mLatencyProgress.setProgress(latencyProgress);
					}
					else {
						mLatencyProgress.setVisibility(View.INVISIBLE);
						mLatency.setVisibility(View.VISIBLE);
						long latency = intent.getLongExtra(CommonIntentBundleKeysOld.EXTRA_LATENCY, 0);
						if (latency == 0)
							mLatency.setText("-");
						else
							mLatency.setText(Long.toString(latency) + " " + getString(R.string.speedtest_milliseconds));
					}
				}
				else {
					mLatency.setText("-");
				}
				
				if(intent.hasExtra(CommonIntentBundleKeysOld.EXTRA_DOWNLOAD_SPEED)) {
					int downSpeed = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_DOWNLOAD_SPEED, 0);
					setGaugeDownloadSpeed(downSpeed);
					
					int downProgress = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_DOWNLOAD_PROGRESS, 0);
					if(downProgress < 100) {
						mDownloadProgress.setVisibility(View.VISIBLE);
						mDownSpeed.setVisibility(View.INVISIBLE);
						mDownloadProgress.setProgress(downProgress);
					}
					else {
						mDownloadProgress.setVisibility(View.INVISIBLE);
						mDownSpeed.setVisibility(View.VISIBLE);
						mDownSpeed.setText(getFormattedSpeed(downSpeed) + " " + getUnits(downSpeed));
						setDownloadNeedleGlow(downSpeed);
					}
				}
				else {
					rotateNeedle(mDownloadNeedle, 0, MAX_DOWNLOAD_SPEED);
					mGaugeDownSpeed.setText("-");
					mDownSpeed.setText("-");
					//mDownloadNeedleGlow.setImageResource(0);
				}
				
				if(intent.hasExtra(CommonIntentBundleKeysOld.EXTRA_UPLOAD_SPEED)) {
					int upSpeed = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_UPLOAD_SPEED, 0);
					setGaugeUploadSpeed(upSpeed);
					
					int uploadProgress = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_UPLOAD_PROGRESS, 0);
					if(uploadProgress < 100) {
						mUploadProgress.setVisibility(View.VISIBLE);
						mUpSpeed.setVisibility(View.INVISIBLE);
						mUploadProgress.setProgress(uploadProgress);
					}
					else {

						mUploadProgress.setVisibility(View.INVISIBLE);
						mUpSpeed.setVisibility(View.VISIBLE);
						mUpSpeed.setText(getFormattedSpeed(upSpeed) + " " + getUnits(upSpeed));
						setUploadNeedleGlow(upSpeed);
						
					}
				}
				else {
					rotateNeedle(mUploadNeedle, 0, MAX_UPLOAD_SPEED);
					mGaugeUpSpeed.setText("-");
					mUpSpeed.setText("-");
					//mUploadNeedleGlow.setImageResource(0);
				}
			}
			else if (intent.getAction().equals(CommonIntentActionsOld.ACTION_SPEEDTEST_COMPLETE))
			{
				((Button)findViewById(R.id.startButton)).setText(R.string.SpeedTest_TestAgain);
				findViewById(R.id.TestInProgressButton).setVisibility(View.GONE);
				findViewById(R.id.startButton).setVisibility(View.VISIBLE);
				// update the list on the right -- only do this if the Speed Test History fragment is visible
				SpeedTestHistoryFragment f = (SpeedTestHistoryFragment) getSupportFragmentManager().findFragmentById(R.id.fragSpeedTestHistory);
				if(f != null) {
					f.updateHistory();
				}
			}
			else if(intent.getAction().equals(CommonIntentActionsOld.ACTION_SPEEDTEST_ERROR) || intent.getAction().equals(CommonIntentActionsOld.ACTION_SPEEDTEST_CANCELLED)) {
				if (intent.getAction().equals(CommonIntentActionsOld.ACTION_SPEEDTEST_ERROR) )
				{
					Toast toast = Toast.makeText(SpeedTest.this, R.string.speedtest_error, Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				((Button)findViewById(R.id.startButton)).setText(R.string.SpeedTest_TestAgain);
				findViewById(R.id.TestInProgressButton).setVisibility(View.GONE);
				findViewById(R.id.startButton).setVisibility(View.VISIBLE);
				
				mGaugeUpSpeed.setText("-");
				mLatency.setText("-");
				mUpSpeed.setText("-");
				mDownSpeed.setText("-");
				mUploadProgress.setVisibility(View.INVISIBLE);
				mUpSpeed.setVisibility(View.VISIBLE);
				mDownloadProgress.setVisibility(View.INVISIBLE);
				mDownSpeed.setVisibility(View.VISIBLE);
				mLatencyProgress.setVisibility(View.INVISIBLE);
				mLatency.setVisibility(View.VISIBLE);
				rotateNeedle(mDownloadNeedle, 0, MAX_DOWNLOAD_SPEED);
				rotateNeedle(mUploadNeedle, 0, MAX_UPLOAD_SPEED);
			}
			else if(intent.getAction().equals(CommonIntentActionsOld.ACTION_SIGNAL_STRENGTH_UPDATE) || intent.getAction().equals(CommonIntentActionsOld.ACTION_CONNECTION_UPDATE)) {
				setTechnologyIcon ();
			}
		}
	}

}
 
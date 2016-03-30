package com.cortxt.app.mmcui.Activities;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcui.utils.FontsUtil;
import com.cortxt.app.mmcui.utils.ScalingUtility;
import com.cortxt.app.mmcutility.DataObjects.PhoneState;
import com.cortxt.app.mmcutility.Utils.CommonIntentActionsOld;
import com.cortxt.app.mmcutility.Utils.CommonIntentBundleKeysOld;
import com.cortxt.app.mmcutility.Utils.Global;
import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.Utils.MmcConstants;
import com.cortxt.app.mmcutility.Utils.PreferenceKeys;
import com.cortxt.app.mmcutility.Utils.TaskHelper;
import com.securepreferences.SecurePreferences;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

public class MMCActiveTest extends MMCTrackedActivityOld {


	private static final float STATS_RADIUS = 8000.0f;

	private ResultsListener mResultsListener;
	private AsyncTask<Void, Void, JSONObject> mGetCarrierSpeedTask;

	protected ImageView mTechnologyIcon;

    protected Handler mHandler = new Handler();
    protected int networkType = 0;
    protected int trigger = 0;
    protected View view;
    private boolean bComplete = true;
	protected int eventType = 0;

    protected ProgressBar mBufferProgress;

	private static final String TAG = MMCActiveTest.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);

        mResultsListener = new ResultsListener();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CommonIntentActionsOld.ACTION_WEBTEST_RESULT);
        intentFilter.addAction(CommonIntentActionsOld.ACTION_WEBTEST_ERROR);
        intentFilter.addAction(CommonIntentActionsOld.ACTION_WEBTEST_COMPLETE);
        intentFilter.addAction(CommonIntentActionsOld.ACTION_WEBTEST_CANCELLED);
        intentFilter.addAction(CommonIntentActionsOld.ACTION_SIGNAL_STRENGTH_UPDATE);
        intentFilter.addAction(CommonIntentActionsOld.ACTION_CONNECTION_UPDATE);

        registerReceiver(mResultsListener, intentFilter);
        Intent newintent = getIntent();
        if (newintent.hasExtra(CommonIntentBundleKeysOld.KEY_EXTRA_VIDEO_TRIGGER)) {
            MMCLogger.logToFile(MMCLogger.Level.DEBUG, "VideoTest", "onCreate", " triggered from background");
            trigger = newintent.getIntExtra (CommonIntentBundleKeysOld.KEY_EXTRA_VIDEO_TRIGGER, 0);
			bComplete = false;
        }

        setShareText();

	}

    protected void init ()
    {
        ScalingUtility.getInstance(this).scaleView(view, 1.0f);

        TextView actionBarTitle=(TextView)view.findViewById(R.id.actionbartitle);

        Button startButton=(Button)view.findViewById(R.id.startButton);
        Button speedTestProgressButton=(Button)view.findViewById(R.id.TestInProgressButton);

        speedTestProgressButton.setOnClickListener(inProgressOnClickListener);

        customizeTopBackgroundColor();
        FontsUtil.applyFontToTextView(MmcConstants.font_Regular, actionBarTitle, this);

        FontsUtil.applyFontToButton(MmcConstants.font_Regular, startButton, this);
        FontsUtil.applyFontToButton(MmcConstants.font_Regular, speedTestProgressButton, this);

        mTechnologyIcon = (ImageView) view.findViewById(R.id.carrierGenerationImage);
//		textBufferProg = (TextView)view.findViewById(R.id.textBufferProg);
//		textPlayProg = (TextView)view.findViewById(R.id.textPlayProg);
//		textStalls = (TextView)view.findViewById(R.id.textStalls);
//		textStallTime = (TextView)view.findViewById(R.id.textStallTime);
        mBufferProgress = (ProgressBar)view.findViewById(R.id.buffer_progress);
        //mPlayProgress = (ProgressBar)view.findViewById(R.id.play_progress);
        setTechnologyIcon();

    }
	@Override
	protected void onPause(){
        super.onPause();
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();

        if (bComplete == false) {
            Intent intent = new Intent(CommonIntentActionsOld.ACTION_STOP_VIDEOTEST);
            MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "OnClickListener", "click stop audio test");
            sendBroadcast(intent);
        }

		unregisterReceiver(mResultsListener);


//		if(mGetCarrierSpeedTask != null) {
//			mGetCarrierSpeedTask.cancel(true);
//		}
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (bComplete == false)
            {
                confirmCancel ();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

	View.OnClickListener inProgressOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
            confirmCancel ();
		}
	};

    private void confirmCancel ()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MMCActiveTest.this);
        builder.setMessage(getApplicationContext().getString(R.string.speedtest_stop));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.GenericText_Yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(CommonIntentActionsOld.ACTION_STOP_VIDEOTEST);
                MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "OnClickListener", "click stop audio test");
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

	/*
	 * Tell Service to start a speed test, screen will listen for the results
	 * If service has been stopped, we'll remind the user to start the mmc service
	 */
	public void startClicked(View button)  {

//		try {
//			mediaPlayer.start();
//		} catch (IllegalStateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		if (!isMMCServiceRunning())
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setCancelable(true);
			//builder.setIcon(R.drawable.ic_mmclauncher);

			String strTitle = getString (R.string.GenericText_AskStartService);
			String strText = getString(R.string.GenericText_AskStartServiceDescription);
			String appname = Global.getAppName(this);
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
				  // Start the MMC Service
				  Global.startService(MMCActiveTest.this);
				  trackEvent ("StartStop", "start", "AudioTest", 0);
                  SecurePreferences securePrefs = PreferenceKeys.getSecurePreferences(MMCActiveTest.this);
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
        bComplete = false;
        startTest ();
//		Intent intent = new Intent(MMCIntentHandlerOld.VIDEO_TEST);
//		intent.putExtra(SpeedTestTrigger.EXTRA_SPEED_TRIGGER, 0);
//		sendBroadcast(intent);

	}

    protected void startTest ()
    {
    }

	/*
	 * Is the MMCService running?
	 */
	private boolean isMMCServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("com.cortxt.app.mmccore.MMCService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}

	/*
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
					speedTier = MMCPhoneStateListenerOld.getNetworkGeneration(networkType);
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
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "getStats", "", e);
			if (e.getCause() instanceof UnknownHostException || e.getCause() instanceof HttpHostConnectException)
				showError (R.string.GenericText_UnknownHost);
		}
	}
	*/
	@Override
	protected void onResume() {
		super.onResume();

	}


	public void shareClicked(View button) {
		temporarilyDisableButton(button);


			float density = getResources().getDisplayMetrics().density;
			int width = getResources().getDisplayMetrics().widthPixels;

			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					String message;
					int customSocialText = (getResources().getInteger(R.integer.CUSTOM_SOCIALTEXT));
					String subject = getString((customSocialText == 1)?R.string.sharecustomsubject_speedtest:R.string.sharemessagesubject_speedtest);

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
					else
						message = getString((customSocialText == 1)?R.string.sharecustom_speedtest_poorspeed:R.string.sharemessage_speedtest_poorspeed);
					TaskHelper.execute(
							new ShareTask(MMCActiveTest.this, message, subject, findViewById(R.id.webtest_container))); // .execute((Void[])null);
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
                MMCActiveTest.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.setEnabled(true);
                    }
                });
            }
        }).start();
	}

	public void historyClicked(View button) {
		//List<HashMap<String, Long>> results = ReportManager.getInstance(getApplicationContext()).getSpeedTestResults(0, Long.MAX_VALUE);
		{
			Intent intent = new Intent(this, SpeedTestHistory.class);
			intent.putExtra ("EVENTTYPE",eventType);
			startActivity(intent);
		}

	}

	private void showError (final int stringres)
	{
		runOnUiThread (new Runnable() {
			@Override
			public void run() {Toast.makeText(MMCActiveTest.this, stringres, Toast.LENGTH_LONG).show();}});
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
	}

	private String getUnits(int speed) {
		if(speed > 1000000) {
			return getString(R.string.speedtest_mbps);
		}
		else {
			return getString(R.string.speedtest_kbps);
		}
	}

	/**
	 * Gets the speed formatted for displaying, and with the units given by {@link MMCActiveTest#getUnits(int)}
	 * @param speed transfer speed in bits/second
	 * @return the speed formatted for displaying
	 */
	private String getFormattedSpeed(int speed) {
		if (speed == -1 || Math.abs(speed) > 1000000000)
			return "-";
		if(speed > 1000000) {
			return String.format("%2.1f", (float)speed / 1000000.0f);
		}
		else {
			return String.format("%2.1f", (float)speed / 1000.0f);
		}
	}
	
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
	
	protected void setProgress (int bufferProgress, int playProgress, int stallCount, int stallTime, int accessDelay, int playDelay, int duration, int downloadTime)
	{
    }

    protected void handleTestProgress (Intent intent)
    {

    }

    protected void handleTestComplete ()
    {

    }

    protected void handleTestError ()
    {

    }
	/**
	 * BroadcastReceiver that listens to results of speed test
	 * @author nasrullah
	 *
	 */
	class ResultsListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			if(intent.getAction().equals(CommonIntentActionsOld.ACTION_WEBTEST_RESULT)) {

                if (bComplete == true)
                    return;
                handleTestProgress (intent);
			}
			else if (intent.getAction().equals(CommonIntentActionsOld.ACTION_WEBTEST_COMPLETE))
			{
				((Button)findViewById(R.id.startButton)).setText(R.string.SpeedTest_TestAgain);
				findViewById(R.id.TestInProgressButton).setVisibility(View.GONE);
				findViewById(R.id.startButton).setVisibility(View.VISIBLE);
                bComplete = true;
                handleTestComplete ();
				if (trigger > 0)
                    finish ();

				//surfaceHolder.getSurface().release();
				//TimeSeries<Float> videoTimeSeries = (TimeSeries<Float>) intent.getSerializableExtra(CommonIntentBundleKeysOld.KEY_EXTRA_VIDEO_CHART);
			}
			else if(intent.getAction().equals(CommonIntentActionsOld.ACTION_WEBTEST_ERROR) || intent.getAction().equals(CommonIntentActionsOld.ACTION_WEBTEST_CANCELLED)) {
				if (intent.getAction().equals(CommonIntentActionsOld.ACTION_WEBTEST_ERROR) )
				{
					Toast toast = Toast.makeText(MMCActiveTest.this, R.string.speedtest_error, Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
                bComplete = true;
                handleTestError ();
				((Button)findViewById(R.id.startButton)).setText(R.string.SpeedTest_TestAgain);
				findViewById(R.id.TestInProgressButton).setVisibility(View.GONE);
				findViewById(R.id.startButton).setVisibility(View.VISIBLE);
				//surfaceHolder.getSurface().release();
			}
			else if(intent.getAction().equals(CommonIntentActionsOld.ACTION_SIGNAL_STRENGTH_UPDATE) || intent.getAction().equals(CommonIntentActionsOld.ACTION_CONNECTION_UPDATE)) {
				setTechnologyIcon ();
			}
		}
	}

	
}
 
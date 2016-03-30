package com.cortxt.app.mmcui.Activities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cortxt.app.mmcui.Activities.CustomViews.DropDownMenuWindow;
import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcui.utils.FontsUtil;
import com.cortxt.app.mmcui.utils.ScalingUtility;
import com.cortxt.app.mmcutility.Reporters.ReportManager;
import com.cortxt.app.mmcutility.Utils.CommonIntentActionsOld;
import com.cortxt.app.mmcutility.Utils.CommonIntentBundleKeysOld;
import com.cortxt.app.mmcutility.Utils.Global;
import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.Utils.MmcConstants;
import com.cortxt.com.mmcextension.VQ.BluetoothRecorder;
import com.cortxt.com.mmcextension.VQ.VQManager;

public class VoiceQTest extends MMCTrackedActivityOld {

	public TextView consoleText, downloadHeading;
	private ProgressBar mDownloadProgress;
	private DropDownMenuWindow logMenu=null;
	private ImageButton menuButton=null;
	private Spinner spinnerDevice = null;
	private List<String> deviceNames = null;
	private CheckBox checkBoxBluetooth = null;
	private String deviceName = null, fileName = null;
	protected Button playVoicetest = null;
	private boolean useBT = true, testReady = true;
	private boolean headsetPlugged = false;
	private int sampleSetting = 0, encodeSetting = 0, formatSetting = 0;
	private String languageSetting, genderSetting, phoneNumberSetting;
	private AlertDialog.Builder discoveryDlg = null;
	//private ArrayList<String> mDevices = null;
	MediaPlayer mMediaPlayer = null;
	private boolean dlgShown = false;
	private static final String TAG = VoiceQTest.class.getSimpleName();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.voice_quality_test, null, false);
		setContentView(view);
        MMCActivity.customizeTitleBar(this, view, R.string.dashcustom_voicetest, R.string.dashcustom_voicetest);

		RelativeLayout actionBarLayout = (RelativeLayout) findViewById(R.id.topactionbarLayout);
		//ScalingUtility.getInstance(this).scaleView(view);
		ScalingUtility.getInstance(this).scaleView(view);
		mDownloadProgress = (ProgressBar) findViewById(R.id.voicetest_progress);
		consoleText = (TextView) findViewById(R.id.console_text);
		spinnerDevice = (Spinner) findViewById(R.id.spinnerBT);
		menuButton=(ImageButton)findViewById(R.id.actionbarMenuIcon);
		checkBoxBluetooth=(CheckBox)findViewById(R.id.checkBoxBluetooth);
		downloadHeading = (TextView)findViewById(R.id.downloadHeading);
		playVoicetest = (Button)findViewById(R.id.playVoiceTest);
		
		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(VQManager.VOICETEST_INPROGRESS, false)) {
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(
					VQManager.VOICETEST_INPROGRESS, false);
			//setTestReady (false);
			//downloadHeading.setText(this.getString(R.string.VQ_InProgress));
		}
		
		fileName = PreferenceManager.getDefaultSharedPreferences(this).getString(VQManager.VOICETEST_FILENAME, null);
		Button voiceTestProgressButton=(Button)view.findViewById(R.id.TestInProgressButton);
		voiceTestProgressButton.setOnClickListener(inProgressOnClickListener);
		
		useBT = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(VQManager.KEY_SETTINGS_USE_BLUETOOTH_REC,false);
		deviceName = PreferenceManager.getDefaultSharedPreferences(this).getString(VQManager.KEY_SETTINGS_BLUETOOTH_DEVICE,"");
		checkBoxBluetooth.setChecked(useBT);
		IntentFilter intentFilter = new IntentFilter(CommonIntentActionsOld.ACTION_BLUETOOTH_DOWNLOAD);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_BLUETOOTH_STARTDOWNLOAD);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_BLUETOOTH_ENDDOWNLOAD);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_BLUETOOTH_STATUS);
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_VQ_ENDUPLOAD);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_VQ_UPLOAD);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_VQ_POLL_SCORE);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_VQ_AUDIO_LEVEL);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_VQ_CONNECT_ERROR);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_VOICETEST_ERROR);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_START_RECORDING);
		intentFilter.addAction(CommonIntentActionsOld.ACTION_TEST_COMPLETE);
		
		registerReceiver(broadcastReceiver, intentFilter);
        setHeadsetState (Global.isHeadsetPlugged() ? 1 : 0);
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(broadcastReceiver);
	}
	@Override
	protected void onResume() {
		super.onResume();

		findDevices ();
		//consoleText.setText("");
		try{
			encodeSetting = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("KEY_SETTINGS_VQ_ENCODER",String.valueOf(MediaRecorder.AudioEncoder.AAC)));
			sampleSetting = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("KEY_SETTINGS_VQ_SAMPLE",String.valueOf(1)));  // 1 = Recorder.SAMPLE_16000
			formatSetting = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("KEY_SETTINGS_VQ_FORMAT",String.valueOf(MediaRecorder.OutputFormat.MPEG_4)));
			
		}
		catch (Exception e)
		{}
	}
	public void BackActionClicked(View view) {
		this.finish();
	}


	protected void findDevices () // Fill Spinner with list of Paired BlueTooth devices
	{
		BluetoothRecorder r = BluetoothRecorder.getInstance(VoiceQTest.this);
		List<BluetoothDevice> devices = r.getDevices ();
		int indexWatch = -1;
		deviceNames = new ArrayList<String>();
		if (devices != null && devices.size() > 0)
		{
			for (int i=0; i<devices.size(); i++){
				if (devices.get(i).getName().equals(deviceName))
					indexWatch = i;
				deviceNames.add (devices.get(i).getName());
			}
			if (indexWatch == -1)
			{
				for (int i=0; i<devices.size(); i++){
					BluetoothDevice device = devices.get(i);
					ParcelUuid[] uuids = device.getUuids();
					if (device != null && uuids != null && uuids.length > 0)
					{
						for (int j=0; j<uuids.length; j++)
						{
			        		ParcelUuid puuid = uuids[j];
			        		UUID uuid = puuid.getUuid();
			        		if (uuid.toString().equals(BluetoothRecorder.BT_UUID.toString()))
			        		{
			        			indexWatch = i;
			        			break;
			        		}
						}
					}
				}
			}
			if (indexWatch == -1)
				indexWatch = 0;
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, deviceNames);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnerDevice.setAdapter(dataAdapter);
			spinnerDevice.setSelection(indexWatch); //default to S5 watch or 1st paired device
			spinnerDevice.setOnItemSelectedListener(new OnItemSelectedListener() {
	
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					deviceName = (String)spinnerDevice.getSelectedItem();
					PreferenceManager.getDefaultSharedPreferences(VoiceQTest.this).edit().putString(VQManager.KEY_SETTINGS_BLUETOOTH_DEVICE,deviceName).commit();
				}
	
				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub
					
				}
			});
		}
	}


    public void checkedBluetooth (View view) {
        useBT = checkBoxBluetooth.isChecked();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(VQManager.KEY_SETTINGS_USE_BLUETOOTH_REC,useBT).commit();
        setTestReady (testReady);
    }

	public void buttonTestDevice (View view) {
		setTestReady (false);
		ReportManager.getInstance(getApplicationContext()).setVQHandler(mHandler);
		Intent intent = new Intent(CommonIntentActionsOld.ACTION_TEST_VQ_DEVICE);
		MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "OnClickListener", "buttonTestDevice - test VQ Device");
		sendBroadcast(intent);
	}
	
	public void discoverBluetooth (View view) {
    	// If you want to enable discoverability
//    	Intent discoverableIntent = new
//		Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//		this.startActivity(discoverableIntent);
		
		BluetoothRecorder r = BluetoothRecorder.getInstance(VoiceQTest.this);
		r.startDiscovery();
		
		//mDevices = new ArrayList<String>();
		discoveryDlg = new AlertDialog.Builder(this);
		discoveryDlg.setTitle("Pair with VQ Module?");
		discoveryDlg.setMessage("Plug audio cable to VQ Module (you may have to turn on the screen) and make it discoverable");
		
		discoveryDlg.setInverseBackgroundForced(true);
		dlgShown = false;
		discoveryDlg.setPositiveButton(R.string.GenericText_OK, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
				VoiceQTest.this.startActivity(intent);
			}
		});

		discoveryDlg.setNegativeButton(R.string.VQ_Cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				BluetoothRecorder r = BluetoothRecorder.getInstance(VoiceQTest.this);
				r.cancelDiscovery();
				dialog.dismiss();
			}
		});
		
		AlertDialog alert = discoveryDlg.create();
		alert.show();
//		BluetoothRecorder r = BluetoothRecorder.getInstance(VoiceQTest.this);
//		r.startDiscovery ();
	}

	// Handler in DataTransferActivity
	private static final int MESSAGE_READ = 9999;
	public Handler mHandler = new Handler() {
	public void handleMessage(Message msg) {
	  switch (msg.what) {
//	    case SOCKET_CONNECTED: {
//	      mBluetoothConnection = (ConnectionThread) msg.obj;
//	      if (!mServerMode)
//	        mBluetoothConnection.write("this is a message".getBytes());
//	      break;
//	    }
//	    case DATA_RECEIVED: {
//	      data = (String) msg.obj;
//	      tv.setText(data);
//	      if (mServerMode)
//	       mBluetoothConnection.write(data.getBytes());
//	     }
	     case MESSAGE_READ:
	      // your code goes here
	    	 String omsg = (String) msg.obj;
	    	 addText(omsg);
	  }
	}};
//	private boolean connected = false;
//	private AsyncTask<Void, Void, String> mConnectTask;
	public void beginVoiceTest(View v) {
		VQManager vqManager = VQManager.getInstance ();// ReportManager.getInstance(getApplicationContext()).getVQManager();
		if (vqManager == null)
		{
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "beginVoiceTest", "VQManager = null");
			return;
		}
        consoleText.setText("");
		setTestReady (false);
		vqManager.setHandler (mHandler);
		Intent intent = new Intent(CommonIntentActionsOld.ACTION_START_VOICETEST);
		MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "OnClickListener", "beginVoiceTest");
		sendBroadcast(intent);
	}
//		
//		
//		mConnectTask = new AsyncTask<Void, Void, String>() {
//			@Override
//			protected String doInBackground(Void... params) {
//				String resultString = null;
//				if (useBT)
//				{
//					BluetoothRecorder r = BluetoothRecorder.getInstance(VoiceQTest.this);
//	
//					try {
//						if (!connected)
//						{
//							r.setSampleRate(sampleSetting);
//							r.setAudioEncoder(encodeSetting);
//							r.setAudioFormat(formatSetting);
//							//r.getDevice ("mmc-brad1");
//							r.getDevice (deviceName);
//							//r.getDevice ("G2");
//							//r.getDevice ("XT1058");
//							resultString = r.connectBluetooth(mHandler);
//							if (resultString == null)
//								connected = true;
//						}
//						else
//						{
//							r.disconnect();
//							connected = false;
//						}
//						
//						return resultString;
//					} catch (Exception e) {
//						//if (e.getCause() instanceof UnknownHostException || e.getCause() instanceof HttpHostConnectException)
//						//	showError(R.string.GenericText_UnknownHost);
//						return e.toString();
//					}
//				}
//				return "unknown";
//			}
//
//			@Override
//			protected void onPostExecute(String result) {
//				if (result == null)
//					callTwilio();
//				else
//				{
//					addText("connection failed to VQ: " + result);
//					doneVoiceTest ();
//					downloadHeading.setText(R.string.VQ_Connect_Failed);
//					//callTwilio();
//				}
//			}
//
//		}.execute((Void[]) null);
//	}
	
	

	/**
	 * Adds a line to the text displayed to <code>text</code>
	 * @param text
	 */
	public void addText(String text) {
		//logTextView.setText(text);
		CharSequence oldText = consoleText.getText();
		oldText = oldText + "\n" + text;
		consoleText.setText(oldText);
	}

	
	/**
	 * Updates the text displayed to <code>text</code>
	 * @param text
	 */
	public void updateText(String text) {
		consoleText.setText(text);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU ) {
			topMenuActionClicked(menuButton);
			return true;
		}else if(keyCode==KeyEvent.KEYCODE_BACK){
			if(logMenu !=null && DropDownMenuWindow.isWindowAlreadyShowing){
				logMenu.dismissWindow();
				return true;
			}
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	android.view.View.OnClickListener inProgressOnClickListener = new android.view.View.OnClickListener() {

		@Override
		public void onClick(View v) {		
			if (testReady == true)
			{
				beginVoiceTest(v);
				return;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(VoiceQTest.this);
			builder.setMessage(getApplicationContext().getString(R.string.VQ_test_stop));
			builder.setCancelable(false);
			builder.setPositiveButton(getString(R.string.GenericText_Yes), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {					
					Intent intent = new Intent(CommonIntentActionsOld.ACTION_STOP_VOICETEST);
					MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "OnClickListener", "click stop speedtest");
					sendBroadcast(intent);
					
					findViewById(R.id.startVoiceTest).setVisibility(View.VISIBLE);
					findViewById(R.id.TestInProgressButton).setVisibility(View.GONE);
					
					doneVoiceTest ();
					dialog.dismiss();
					BluetoothRecorder r = BluetoothRecorder.getInstance(VoiceQTest.this);
					r.disconnect();
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
	
	public void topMenuActionClicked(View button){
		if(DropDownMenuWindow.isWindowAlreadyShowing && logMenu!=null){
			logMenu.dismissWindow();
			return;
		}
		long currentTime=System.currentTimeMillis();
		if(currentTime-DropDownMenuWindow.lastWindowDismissedTime>200){
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View menuOptionsView = inflater.inflate(R.layout.voice_quality_menu, null, false);
			ScalingUtility.getInstance(this).scaleView(menuOptionsView);
			TextView settings =(TextView)menuOptionsView.findViewById(R.id.SettingsOption);
			FontsUtil.applyFontToTextView(MmcConstants.font_Regular, settings, this);
			
			settings.setOnClickListener(settingsClickListener);
			
			logMenu=new DropDownMenuWindow(menuOptionsView,this,MmcConstants.MAP_MENU_OFFSET,MmcConstants.GENERAL_MENU_WINDOW_WIDTH);
			logMenu.showCalculatorMenu(menuButton);
		}
	}
	public void playVoiceTest(View v) {
        fileName = PreferenceManager.getDefaultSharedPreferences(this).getString(VQManager.VOICETEST_FILENAME, null);

        if (fileName == null)
			return;
		if (headsetPlugged)
		{
			String msg = this.getString(R.string.VQ_disconnectJack);
			Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
			toast.show();
		}
		String dir = "/mmc_audio/";
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + dir + fileName;
		
		//path = "/sdcard/sample.mp3";
		mMediaPlayer = new MediaPlayer();
        try {
			mMediaPlayer.setDataSource(path);
			mMediaPlayer.prepare();
	        mMediaPlayer.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
//		 Intent intent = new Intent();  
//	   //ComponentName comp = new ComponentName("com.android.music", "com.android.music.MediaPlaybackActivity");
//	   //intent.setComponent(comp);
//	   intent.setAction(android.content.Intent.ACTION_VIEW);  
//	   File file = new File(path.toString());  
//	   intent.setDataAndType(Uri.fromFile(file), "audio/*");  
//	   startActivity(intent);
	}
	public void doneVoiceTest ()
	{
		PreferenceManager.getDefaultSharedPreferences(VoiceQTest.this).edit().putBoolean(
				VQManager.VOICETEST_INPROGRESS, false).commit();
		setTestReady (true);
		downloadHeading.setText(this.getString(R.string.VQ_Idle));
	}
	
	android.view.View.OnClickListener settingsClickListener=new android.view.View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent startSettingsIntent = new Intent(VoiceQTest.this, VQSettings.class);
			startActivity(startSettingsIntent);
			if(logMenu!=null){
				logMenu.dismissWindow();
			}
		}
	};
	
	
	public void setHeadsetState (int state)
	{
		headsetPlugged = state == 1 ? true : false;
		setTestReady (testReady);
	}
	public boolean isHeadsetPlugged ()
	{
		return headsetPlugged;
	}
	private void setTestReady (boolean ready)
	{
		testReady = ready;
		if (ready && (headsetPlugged || useBT == false))
		{
			findViewById(R.id.startVoiceTest).setVisibility(View.VISIBLE);
			findViewById(R.id.TestInProgressButton).setVisibility(View.GONE);
			PreferenceManager.getDefaultSharedPreferences(VoiceQTest.this).edit().putBoolean(VQManager.VOICETEST_INPROGRESS, false).commit();
			//connected = false;
		}
		else
		{
			findViewById(R.id.startVoiceTest).setVisibility(View.GONE);
			Button button = (Button)findViewById(R.id.TestInProgressButton);
			button.setVisibility(View.VISIBLE);
			if (testReady == true && !headsetPlugged)
				button.setText(R.string.VQ_NeedHeadphone);
			else
				button.setText(R.string.VQ_InProgress);
		}
	}
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		Bundle extras;

		@Override
		public void onReceive(Context context, Intent intent) {
			extras = intent.getExtras();

			if (intent.getAction().equals(CommonIntentActionsOld.ACTION_BLUETOOTH_DOWNLOAD)) {
				manageDownloadProgress();
			} 
			else if (intent.getAction().equals(CommonIntentActionsOld.ACTION_BLUETOOTH_STARTDOWNLOAD)) {
				manageStartDownload();
			}  // Sent when transfer is complete, also sent on errors
			else if (intent.getAction().equals(CommonIntentActionsOld.ACTION_BLUETOOTH_ENDDOWNLOAD)) {
				manageEndDownload();
			} 
			else if (intent.getAction().equals(CommonIntentActionsOld.ACTION_BLUETOOTH_STATUS)) {
				manageStatus();
			}
			else if (intent.getAction().equals(CommonIntentActionsOld.ACTION_VQ_UPLOAD)) {
				manageUploadProgress();
			} 
			else if (intent.getAction().equals(CommonIntentActionsOld.ACTION_VQ_ENDUPLOAD)) {
				manageEndUpload();
			} 
			else if (intent.getAction().equals(CommonIntentActionsOld.ACTION_VQ_POLL_SCORE)) {
				manageVQScore();
			} 
			else if (intent.getAction().equals(CommonIntentActionsOld.ACTION_VQ_AUDIO_LEVEL)) {
				manageAudioLevel();
			} 
			else if (intent.getAction().equals(CommonIntentActionsOld.ACTION_VQ_CONNECT_ERROR)) {
				manageConnectError ();
			}
			else if (intent.getAction().equals(CommonIntentActionsOld.ACTION_VOICETEST_ERROR)) {
				manageVQError ();
			}
			else if (intent.getAction().equals(CommonIntentActionsOld.ACTION_START_RECORDING)) {
				manageStartRecording ();
			}
			else if (intent.getAction().equals(CommonIntentActionsOld.ACTION_TEST_COMPLETE)) {
				manageTestComplete ();
			}
			
			else if(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
				int state = intent.getIntExtra("state", -1);
				setHeadsetState(state);
			}
			// When discovery finds a device
			else if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            findDevices ();
	            BluetoothRecorder r = BluetoothRecorder.getInstance(VoiceQTest.this);
				
	            String msg = "Found ";
	            if (device != null)
	            	msg += device.getName();
	            else
	            	msg += "null";
	            Toast toast = Toast.makeText(VoiceQTest.this, msg, Toast.LENGTH_LONG);
	            toast.show();
	            // Add the name and address to an array adapter to show in a ListView
	            //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
	        }
			else if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
	            // Get the BluetoothDevice object from the Intent
	            findDevices ();
			}
		}
		
		private void manageDownloadProgress() {
			if (extras != null) {
				if (extras.containsKey(CommonIntentBundleKeysOld.KEY_EXTRA_BLUETOOTH_PACKET)) {
					int packetnum = extras.getInt(CommonIntentBundleKeysOld.KEY_EXTRA_BLUETOOTH_PACKET);
					int total = extras.getInt(CommonIntentBundleKeysOld.KEY_EXTRA_BLUETOOTH_TOTAL);
					int progress = 0;
					if (total > 0)
						progress = packetnum * 100 / total;
					mDownloadProgress.setProgress(progress);
				}
			}
		}
		private void manageUploadProgress() {
			if (extras != null) {
				if (extras.containsKey(CommonIntentBundleKeysOld.KEY_EXTRA_VQ_UPLOAD)) {
					int percent = extras.getInt(CommonIntentBundleKeysOld.KEY_EXTRA_VQ_UPLOAD);
					mDownloadProgress.setProgress(percent);
					downloadHeading.setText(VoiceQTest.this.getString(R.string.VQ_Uploading));
				}
			}
		}
		
		private void manageVQScore() {
			if (extras != null) {
				if (extras.containsKey(CommonIntentBundleKeysOld.KEY_EXTRA_VQ_SCORE)) {
					int score = extras.getInt(CommonIntentBundleKeysOld.KEY_EXTRA_VQ_SCORE);
					double dscore = (double)((score + 5)/10)/100;
					if (score > 0)
						downloadHeading.setText(VoiceQTest.this.getString(R.string.VQ_Score) + ": " + dscore);
					else
						downloadHeading.setText(VoiceQTest.this.getString(R.string.VQ_ScoreFailed) + ": " + dscore);
					setTestReady (true);
				}
			}
		
		}
		
		private void manageTestComplete ()
		{
			setTestReady (true);
			downloadHeading.setText(VoiceQTest.this.getString(R.string.VQ_Idle) + ": " + fileName);
			
		}
		
		private void manageAudioLevel() {
			if (extras != null) {
				if (extras.containsKey(CommonIntentBundleKeysOld.KEY_EXTRA_VQ_LEVEL)) {
					int level = extras.getInt(CommonIntentBundleKeysOld.KEY_EXTRA_VQ_LEVEL);
					mDownloadProgress.setProgress(level);
					if (level > 98)
						downloadHeading.setText("Audio level peaked");
				}
			}
		}
		
		private void manageEndUpload () {
			if (extras != null) {
				if (extras.containsKey(CommonIntentBundleKeysOld.KEY_EXTRA_VQ_UPLOADED)) {
					int result = extras.getInt(CommonIntentBundleKeysOld.KEY_EXTRA_VQ_UPLOADED);
					if (result == 1)
					{
						downloadHeading.setText(VoiceQTest.this.getString(R.string.VQ_Upload_Complete) + ": " + fileName);
						Drawable background = getResources().getDrawable( R.drawable.start_button_selector );
						playVoicetest.setBackgroundDrawable(background);
					}
					else
					{
						String error = "error";
						if (extras.containsKey(CommonIntentBundleKeysOld.KEY_EXTRA_VQ_UPLOAD_ERROR))
							error = extras.getString(CommonIntentBundleKeysOld.KEY_EXTRA_VQ_UPLOAD_ERROR);
						downloadHeading.setText(VoiceQTest.this.getString(R.string.VQ_Upload_Failed) + ": " + error);
						
					}
					
				}
			}
			//downloadHeading.setText(VoiceQTest.this.getString(R.string.VQ_WaitForGPS));
			//doneVoiceTest ();
		}
		private void manageStartDownload() {
			if (extras != null) {
				if (extras.containsKey(CommonIntentBundleKeysOld.KEY_EXTRA_BT_FILENAME)) {
					fileName = extras.getString(CommonIntentBundleKeysOld.KEY_EXTRA_BT_FILENAME);
					downloadHeading.setText(VoiceQTest.this.getString(R.string.VQ_Downloading) + ": " + fileName);
					PreferenceManager.getDefaultSharedPreferences(VoiceQTest.this).edit().putString(VQManager.VOICETEST_FILENAME, fileName).commit();
				}
			}
		}
		private long startTime = 0;
		private void manageStartRecording ()
		{
			startTime = System.currentTimeMillis();
			downloadHeading.setText(VoiceQTest.this.getString(R.string.VQ_Recording));
//			Intent it = new Intent (VoiceQTest.this, VoiceQTest.class);
//			it.setAction(Intent.ACTION_MAIN);
//			it.setComponent(new ComponentName(VoiceQTest.this.getPackageName(), VoiceQTest.class.getName()));
//			it.setFlags (Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			//Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP
//			VoiceQTest.this.startActivity(it);
//			//VoiceQTest.this.
			
//			Intent i = new Intent(VoiceQTest.this, SplashScreen.class);
//			i.setAction(Intent.ACTION_MAIN);
//			i.addCategory(Intent.CATEGORY_LAUNCHER);
//			startActivity(i);
		}
		
		// Failed to connect to the VQ Module
		private void manageConnectError() {
			// re-enable the start test button
			setTestReady (true);
			addText(VoiceQTest.this.getString(R.string.VQ_Connectfailed_msg1));
			addText(VoiceQTest.this.getString(R.string.VQ_Connectfailed_msg2));
			
			if (deviceNames.size() > 1)
				addText(VoiceQTest.this.getString(R.string.VQ_Connectfailed_msg3));
			
			if (extras.containsKey(CommonIntentBundleKeysOld.KEY_EXTRA_VQ_CONNECT_ERROR)) {
				String err = extras.getString(CommonIntentBundleKeysOld.KEY_EXTRA_VQ_CONNECT_ERROR);
				addText("");
				addText(err);
			}
		}
		
		private void manageVQError () {
			// re-enable the start test button
			setTestReady (true);
			addText(VoiceQTest.this.getString(R.string.VQ_Error_Tryagain));
		}
		
		private void manageEndDownload() {
			if (extras != null) {
				if (extras.containsKey(CommonIntentBundleKeysOld.KEY_EXTRA_BT_DOWNLOADED)) {
					int result = extras.getInt(CommonIntentBundleKeysOld.KEY_EXTRA_BT_DOWNLOADED);
					if (result == 1)
					{
						downloadHeading.setText(VoiceQTest.this.getString(R.string.VQ_Download_Complete) + ": " + fileName);
						Drawable background = getResources().getDrawable( R.drawable.start_button_selector );
						playVoicetest.setBackgroundDrawable(background);
						
					}
					else
					{
						String error = "error";
						if (extras.containsKey(CommonIntentBundleKeysOld.KEY_EXTRA_BT_DOWNLOAD_ERROR))
							error = extras.getString(CommonIntentBundleKeysOld.KEY_EXTRA_BT_DOWNLOAD_ERROR);
						downloadHeading.setText(VoiceQTest.this.getString(R.string.VQ_Download_Failed) + ": " + error);
						
					}
					
				}
			}
			//doneVoiceTest ();
		}
		
		private void manageStatus() {
			if (extras != null) {
				if (extras.containsKey(CommonIntentBundleKeysOld.KEY_EXTRA_BT_STATUS)) {
					String status = extras.getString(CommonIntentBundleKeysOld.KEY_EXTRA_BT_STATUS);
					downloadHeading.setText(status);
				}
			}
		}
	};
}

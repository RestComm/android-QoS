package com.cortxt.app.mmcui.Activities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cortxt.app.mmcui.Activities.CustomViews.DropDownMenuWindow;
import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcui.utils.FontsUtil;
import com.cortxt.app.mmcui.utils.ScalingUtility;
import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.Utils.MmcConstants;

/*
 * NOTE: This is temporarily written in a quick and dirty way. Fix later, after dropped call testing.
 */

public class DeveloperScreenOld extends MMCActivity {
	public static final String TAG = DeveloperScreenOld.class.getSimpleName();
	public static final String DEBUG_EMAIL = "debug@mymobilecoverage.com";
	public static final String SUBJECT_LOG_EMAIL = "DEBUG report";
	public static final String BODY_LOG_EMAIL = "report attached\n\n****LOG BEGINS****%s*\n\n**LOG ENDS***";
	public static final String TMP_FILE_PATH = "/sdcard/logdb.db";
	public static final String DB_PATH = "/data/data/com.cortxt.app.MMC/databases/mmc.db";
  /**
   * Max size of the text to be displayed in the log screen
   */
	public static final int MAX_SIZE_LOG_BUFFER = 50000;
  /**
   * Max size of the of per log line, we want to avoid
   * displaying debug messages coming from speed tests network packets
   */
	public static final int MAX_LINE_SIZE = 10000;
	
	private boolean transit = false;

	/*
	 *
	 * =================================================
	 * Start private variables
	 */	

	private Handler handler = new Handler ();

	private Thread readLogThread;
	private ReadLogThreadRunnable readLogThreadRunnable;

	/**
	 * The tags for which to show log messages.
	 * Each element should have the tag name and priority, separated by a :.
	 */
	private ArrayList<String> tags;
	private TextView logTextView;
	private DropDownMenuWindow logMenu=null;
	private ImageButton menuButton=null;
	/*
	 * Stop private variables
	 * =================================================
	 * Start overridden methods
	 */	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view  = inflater.inflate(R.layout.developerscreen, null, false);
		ScalingUtility.getInstance(this).scaleView(view);
		setContentView(view);
        customizeTitleBar(this,view,R.string.GenericText_Log, R.string.GenericText_Log);
		
		logTextView = (TextView) findViewById(R.id.developerscreen_text);
		menuButton=(ImageButton)findViewById(R.id.actionbarMenuIcon);

		tags = new ArrayList<String>();
		tags.add(DeveloperScreenOld.TAG + ":*");
		
		Intent intent = this.getIntent();
		if(intent.hasExtra("transit"))
			transit = intent.getBooleanExtra("transit", false);
	}

	@Override
	protected void onPause() {
		super.onPause();

		readLogThreadRunnable.requestKill();
		readLogThreadRunnable = null;
		readLogThread = null;
	}

	@Override
	protected void onResume() {
		super.onResume();

		logTextView.setText("");

		readLogThreadRunnable = new ReadLogThreadRunnable();
		readLogThread = new Thread(readLogThreadRunnable);
		readLogThread.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//inflate the menu from the xml
		new MenuInflater(getApplication()).inflate(R.menu.developer, menu);

		return super.onCreateOptionsMenu(menu);
	}
	public void backActionClicked(View button){
		this.finish();
	}

	private File copyToTempFile(String fileFrom, String fileTo) {
		File tmp = null;
		try {
			tmp = new File(fileTo);
			BufferedOutputStream boff = new BufferedOutputStream(new FileOutputStream(tmp));
			BufferedInputStream biff = new BufferedInputStream(new FileInputStream(new File(fileFrom)));
			byte[] buf = new byte[1024];
			int len = 0;
			len = biff.read(buf);
			while (len > 0) {
				boff.write(buf, 0, len);
				len = biff.read(buf);
			}
			boff.flush();
			boff.close();
		} catch (IOException e) {
			Log.e(TAG,"Fail to copy file" + e.getMessage());
			tmp = null;
		} catch (SecurityException e) {
			Log.e(TAG,"Fail to copy file" + e.getMessage());
			tmp = null;
		} 
		return tmp;
	}

	/**
	 * Clears the log file
	 */
	private boolean clearLog() {
		File tmp = null;
		if(transit)
			tmp = new File(MMCLogger.LOG_TRANSIT_FILE);
		else
			tmp = new File(MMCLogger.LOG_FILE);
		updateText("");
		return tmp.delete();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.Developer_Menu_SendLogEmail) {
			String filePath = MMCLogger.LOG_FILE;
			if(transit)
				filePath = MMCLogger.LOG_TRANSIT_FILE;
			
			File tmp = copyToTempFile(DB_PATH, TMP_FILE_PATH);
			File[] filePaths = new File[]{
					tmp,
					new File(filePath)
			};
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
			emailIntent.setType("application/*");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {DEBUG_EMAIL}); 
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, SUBJECT_LOG_EMAIL); 
			String body = String.format(BODY_LOG_EMAIL, logTextView.getText());
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);

			ArrayList<Uri> uris = new ArrayList<Uri>();
			for(File file : filePaths) {
				if (file != null) {
					uris.add(Uri.fromFile(file));
				}
			}

			emailIntent.putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, uris);
			startActivity(Intent.createChooser(emailIntent, this.getString(R.string.GenericLabel_ReportError)));
		} else if(item.getItemId() == R.id.Developer_Menu_ClearLog) {
			clearLog();
		}
		return super.onOptionsItemSelected(item);
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
	public void topMenuActionClicked(View button){
		if(DropDownMenuWindow.isWindowAlreadyShowing && logMenu!=null){
			logMenu.dismissWindow();
			return;
		}
		long currentTime=System.currentTimeMillis();
		if(currentTime-DropDownMenuWindow.lastWindowDismissedTime>200){
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View menuOptionsView = inflater.inflate(R.layout.developer_log_menu, null, false);
			ScalingUtility.getInstance(this).scaleView(menuOptionsView);
			TextView clearLog=(TextView)menuOptionsView.findViewById(R.id.ClearOption);
			TextView sendLog=(TextView)menuOptionsView.findViewById(R.id.SendOption);
			String ff = MmcConstants.font_Light;
			if(!isPortrait) {
				ff = MmcConstants.font_Regular;
			}
			FontsUtil.applyFontToTextView(ff, clearLog, this);
			FontsUtil.applyFontToTextView(ff, sendLog, this);
			
			clearLog.setOnClickListener(clearLogClickListener);
			sendLog.setOnClickListener(sendLogClickListener);
			
			logMenu=new DropDownMenuWindow(menuOptionsView,this,MmcConstants.MAP_MENU_OFFSET,MmcConstants.GENERAL_MENU_WINDOW_WIDTH);
			logMenu.showCalculatorMenu(menuButton);
		}
	}
	
	android.view.View.OnClickListener clearLogClickListener=new android.view.View.OnClickListener() {

		@Override
		public void onClick(View v) {
			clearLog();
			if(logMenu!=null){
				logMenu.dismissWindow();
			}
		}
	};
	android.view.View.OnClickListener sendLogClickListener=new android.view.View.OnClickListener () {

		@Override
		public void onClick(View v) {
			String filePath = MMCLogger.LOG_FILE;
			if(transit)
				filePath = MMCLogger.LOG_TRANSIT_FILE;
			File tmp = copyToTempFile(DB_PATH, TMP_FILE_PATH);
			File[] filePaths = new File[]{
					tmp,
					new File(filePath)
			};
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
			emailIntent.setType("application/*");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {DEBUG_EMAIL}); 
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, SUBJECT_LOG_EMAIL); 
			String body = String.format(BODY_LOG_EMAIL, logTextView.getText());
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);

			ArrayList<Uri> uris = new ArrayList<Uri>();
			for(File file : filePaths) {
				if (file != null) {
					uris.add(Uri.fromFile(file));
				}
			}

			emailIntent.putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, uris);
			startActivity(Intent.createChooser(emailIntent, DeveloperScreenOld.this.getString(R.string.GenericLabel_ReportError)));
			if(logMenu!=null){
				logMenu.dismissWindow();
			}
		}
	};

	/*
	 * Stop overridden methods
	 * =================================================
	 * Start public methods
	 */	

	/**
	 * Returns the tags for which log messages should be shown.
	 * @return
	 */
	public ArrayList<String> getTags() {
		return tags;
	}

	/**
	 * Adds a line to the text displayed to <code>text</code>
	 * @param text
	 */
	public void addText(String text) {
		//logTextView.setText(text);
		CharSequence oldText = logTextView.getText();
		oldText = oldText + "\n" + text;
		logTextView.setText(oldText);
	}

	
	/**
	 * Updates the text displayed to <code>text</code>
	 * @param text
	 */
	public void updateText(String text) {
		logTextView.setText(text);
	}
	
	

	/*
	 * Stop public methods
	 * =================================================
	 * Start helper classes and objects
	 */	

	class ReadLogThreadRunnable implements Runnable {
		private boolean killThread;

		@Override
		public void run() {
			//			dumpLogcatToScreen();
			dumpLogFileToScreen();
		}

		public boolean killRequested() {
			return killThread;
		}

		public void requestKill() {
			killThread = true;
		}

		/**
		 * Writes text from {@link MMCLogger#LOG_FILE} to screen.
		 * it only outputs the first MAX_SIZE_LOG_BUFFER
		 */
		private void dumpLogFileToScreen() {
			try {
				File file = null;
				if(transit) {
					file = new File(MMCLogger.LOG_TRANSIT_FILE);
				}
				else 
					file = new File(MMCLogger.LOG_FILE);
				if(!file.exists()) {
					return;
				}
				FileReader fileReader = new FileReader(file);
				BufferedReader inputStream = new BufferedReader(fileReader);

				try {
					String line = null;
					final StringBuilder strBlr = new StringBuilder();
					while(((line = inputStream.readLine()) != null) && strBlr.length() < MAX_SIZE_LOG_BUFFER) {
						if(line.length() < MAX_LINE_SIZE) {
							strBlr.insert(line.indexOf(line), line + "\n");
						}
						if (strBlr.length() >= MAX_SIZE_LOG_BUFFER){
							strBlr.delete(strBlr.length()/2, strBlr.length()); //delete half the log when its full
						}
						
					}

					handler.post(new Runnable() {
						@Override
						public void run() {
							DeveloperScreenOld.this.addText(strBlr.toString());
						}
					});
				}
				catch (IOException ioe_writingToFile) {
					Log.e("MMCLogger", "error writing to log file", ioe_writingToFile);
				}
				finally {
					inputStream.close();
					fileReader.close();
				}
			}
			catch (IOException ioe_openingFile) {
				Log.e("MMCLogger", "error opening log file", ioe_openingFile);
			}
		}

		/**
		 * Dumps text from logcat matching the tags in {@link DeveloperScreenOld#tags}
		 */
		private void dumpLogcatToScreen() {
			String logcatCommand = "/system/bin/logcat ";
			ArrayList<String> tags = DeveloperScreenOld.this.getTags();
			for(int i=0; i<tags.size(); i++) {
				logcatCommand += tags.get(i) + " ";
			}
			logcatCommand += "*:S";

			Process process = null;
			try {
				process = Runtime.getRuntime().exec(logcatCommand);
			}
			catch (IOException ioe) {
				Log.e(DeveloperScreenOld.TAG, "Error executing logcat command", ioe);
				return;
			}


			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

				while(!killRequested()) {
					final String line = reader.readLine();

					handler.post(new Runnable() {
						@Override
						public void run() {
							DeveloperScreenOld.this.addText(line);
						}
					});

				}
			}
			catch (IOException ioe) {
				Log.e(TAG, "Error reading log input stream", ioe);
				return;
			}

		}

	}

}

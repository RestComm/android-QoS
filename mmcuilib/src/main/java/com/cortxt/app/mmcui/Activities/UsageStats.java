package com.cortxt.app.mmcui.Activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.cortxt.app.mmcui.Activities.CustomViews.UsageAdapter;
import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.Utils.Stats;
import com.cortxt.app.mmcutility.DataObjects.beans.CPUStatBean;
import com.cortxt.app.mmcutility.DataObjects.beans.DataStatsBean;
import com.cortxt.app.mmcutility.DataObjects.beans.MemoryStatBean;
import com.cortxt.app.mmcutility.DataObjects.database.DataMonitorDBReader;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;


public class UsageStats extends Activity{
	
	private ListView mCpuStats;
	private ListView mMemoryStats;
	private ListView mDataStats;
	
	private Button mGetCpuStats;
	private Button mGetMemStats;
	private Button mGetDataStats;
	private Button mGetBatteryStats;
	
	private UsageAdapter mCpuStatsAdapter;
	private UsageAdapter mMemoryStatsAdapter;
	private UsageAdapter mDataStatsAdapter;
	
	private Stats mNowStats;
	public static final String TAG = UsageStats.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		//requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.usage_stats);
		//getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_layout);
		//TextView title = (TextView)findViewById(R.id.dmtitle);
		//title.setText("Usage");
		mCpuStats = (ListView) findViewById(R.id.cpustats);
		mCpuStats.setTag(null);
		mMemoryStats = (ListView) findViewById(R.id.memorystats);
		mMemoryStats.setTag(null);
		mDataStats = (ListView) findViewById(R.id.datastats);
		mDataStats.setTag(null);
		
		mGetCpuStats = (Button) findViewById(R.id.getcpustats);
		mGetMemStats = (Button) findViewById(R.id.getmemorystats);
		mGetDataStats = (Button) findViewById(R.id.getdatastats);
		mGetBatteryStats = (Button) findViewById(R.id.getbatterystats);
		
		mGetCpuStats.setBackgroundColor(Color.parseColor("#05B8CC"));
		mGetMemStats.setBackgroundColor(Color.parseColor("#CFCFCF"));
		mGetDataStats.setBackgroundColor(Color.parseColor("#CFCFCF"));
		mGetBatteryStats.setBackgroundColor(Color.parseColor("#CFCFCF"));
		
		mCpuStatsAdapter = new UsageAdapter(this);
		mMemoryStatsAdapter = new UsageAdapter(this);
		mDataStatsAdapter = new UsageAdapter(this);
		
		mGetCpuStats.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mGetCpuStats.setBackgroundColor(Color.parseColor("#05B8CC"));
				mGetMemStats.setBackgroundColor(Color.parseColor("#CFCFCF"));
				mGetDataStats.setBackgroundColor(Color.parseColor("#CFCFCF"));
				mGetBatteryStats.setBackgroundColor(Color.parseColor("#CFCFCF"));
				
				mNowStats = Stats.CPUSTATS;
				if (statsHandler.post(statsUpdateRunnable)) {
					mGetCpuStats.setEnabled(false);
				}
			}
		});
		mGetMemStats.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mGetCpuStats.setBackgroundColor(Color.parseColor("#CFCFCF"));
				mGetMemStats.setBackgroundColor(Color.parseColor("#05B8CC"));
				mGetDataStats.setBackgroundColor(Color.parseColor("#CFCFCF"));
				mGetBatteryStats.setBackgroundColor(Color.parseColor("#CFCFCF"));
				
				mNowStats = Stats.MEMORYSTATS;
				if (statsHandler.post(statsUpdateRunnable)) {
					mGetMemStats.setEnabled(false);
				}
			}
		});

		mGetDataStats.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mGetCpuStats.setBackgroundColor(Color.parseColor("#CFCFCF"));
				mGetMemStats.setBackgroundColor(Color.parseColor("#CFCFCF"));
				mGetDataStats.setBackgroundColor(Color.parseColor("#05B8CC"));
				mGetBatteryStats.setBackgroundColor(Color.parseColor("#CFCFCF"));
				
				mNowStats = Stats.DATASTATS;
				if (statsHandler.post(statsUpdateRunnable)) {
					mGetDataStats.setEnabled(false);
				}
			}
		});
		
//		mGetBatteryStats.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				startActivity(new Intent(UsageStats.this,
//						PowerUsageSummary.class));
//			}
//		});
		mNowStats = Stats.CPUSTATS;
	}
	
	private Runnable statsUpdateRunnable = new Runnable() {
		@Override
		public void run() {
			switch (mNowStats) {
			case CPUSTATS:
				executeTop();
				Collections.sort(listOfData, new CPUStatsComparator());
				statsHandler.sendEmptyMessage(0);
				break;
			case MEMORYSTATS:
				ArrayList<MemoryStatBean> memory_stats = getMemoryStats();
				Collections.sort(memory_stats, new MemoryStatsComparator());
				Message msg = statsHandler.obtainMessage();
				msg.what = 1;
				msg.obj = memory_stats;
				statsHandler.sendMessage(msg);
				break;
			case DATASTATS:
				DataMonitorDBReader dmReader = new DataMonitorDBReader();
				ArrayList<DataStatsBean> data_stats = dmReader.getDataStatistics(getApplicationContext(), null);
				msg = statsHandler.obtainMessage();
				msg.what = 2;
				msg.obj = data_stats;
				statsHandler.sendMessage(msg);
				break;
			default:
				break;
			}
		}
	};
	
	class CPUStatsComparator implements Comparator<CPUStatBean> {
		@Override
		public int compare(CPUStatBean lhs, CPUStatBean rhs) {
			return Integer.parseInt(lhs.getCpuUsage().trim()) > Integer
					.parseInt(rhs.getCpuUsage()) ? -1 : Integer.parseInt(lhs
					.getCpuUsage().trim()) < Integer.parseInt(rhs.getCpuUsage()
					.trim()) ? 1 : lhs.getAppName().compareToIgnoreCase(
					rhs.getAppName());
		}
	}

	class MemoryStatsComparator implements Comparator<MemoryStatBean> {

		@Override
		public int compare(MemoryStatBean lhs, MemoryStatBean rhs) {
			return Double.parseDouble(lhs.getMemoryUsage().trim()) > Double
					.parseDouble(rhs.getMemoryUsage()) ? -1 : Double
					.parseDouble(lhs.getMemoryUsage().trim()) < Double
					.parseDouble(rhs.getMemoryUsage().trim()) ? 1 : lhs
					.getAppName().compareToIgnoreCase(rhs.getAppName());
		}

	}
	
	private Handler statsHandler;

	private void createStatsHandler() {
		statsHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case 0:
					mGetCpuStats.setEnabled(true);
					mCpuStats.setVisibility(View.VISIBLE);
					mMemoryStats.setVisibility(View.GONE);
					mDataStats.setVisibility(View.GONE);
					if (listOfData.size() > 0) {
						mCpuStatsAdapter.setCPUUsage(listOfData);
						if (mCpuStats.getTag() != null) {
							mCpuStatsAdapter.notifyDataSetChanged();
						} else {
							mCpuStats.setTag(listOfData);
							mCpuStats.setAdapter(mCpuStatsAdapter);
						}
					}
					statsHandler.removeMessages(1);
					mGetMemStats.setEnabled(true);
					statsHandler.removeMessages(2);
					mGetDataStats.setEnabled(true);
					statsHandler.removeMessages(3);
					statsHandler.postDelayed(statsUpdateRunnable, 10000);
					break;
				case 1:
					mGetMemStats.setEnabled(true);
					mCpuStats.setVisibility(View.GONE);
					mMemoryStats.setVisibility(View.VISIBLE);
					mDataStats.setVisibility(View.GONE);
					if (msg.obj == null) {
					} else {
						ArrayList<MemoryStatBean> memory_stats = (ArrayList<MemoryStatBean>) msg.obj;
						if (memory_stats != null && memory_stats.size() > 0) {
							mMemoryStatsAdapter.setMemoryUsage(memory_stats);
							if (mMemoryStats.getTag() != null) {
								mMemoryStatsAdapter.notifyDataSetChanged();
							} else {
								mMemoryStats.setTag(memory_stats);
								mMemoryStats.setAdapter(mMemoryStatsAdapter);
							}
						}
					}
					statsHandler.removeMessages(0);
					mGetCpuStats.setEnabled(true);
					statsHandler.removeMessages(2);
					mGetDataStats.setEnabled(true);
					statsHandler.removeMessages(3);
					statsHandler.postDelayed(statsUpdateRunnable, 10000);
					break;
				case 2:
					mGetDataStats.setEnabled(true);
					if (Build.VERSION.SDK_INT < 8) {
						
					}
					mCpuStats.setVisibility(View.GONE);
					mMemoryStats.setVisibility(View.GONE);
					mDataStats.setVisibility(View.VISIBLE);
					if (msg.obj == null) {
					} else {
						ArrayList<DataStatsBean> data_stats = (ArrayList<DataStatsBean>) msg.obj;
						if (data_stats != null && data_stats.size() > 0) {
							mDataStatsAdapter.setDataUsage(data_stats);
							if (mDataStats.getTag() != null) {
								mDataStatsAdapter.notifyDataSetChanged();
							} else {
								mDataStats.setTag(data_stats);
								mDataStats.setAdapter(mDataStatsAdapter);
							}
						}
					}
					statsHandler.removeMessages(0);
					mGetCpuStats.setEnabled(true);
					statsHandler.removeMessages(1);
					mGetMemStats.setEnabled(true);
					statsHandler.removeMessages(3);
					statsHandler.postDelayed(statsUpdateRunnable, 10000);
					break;
				default:
					break;
				}
			};
		};
	}

	@Override
	protected void onPause() {
		statsHandler.removeMessages(0);
		statsHandler.removeMessages(1);
		statsHandler.removeMessages(2);
		statsHandler.removeCallbacks(statsUpdateRunnable);
		statsHandler = null;
		super.onPause();
	};

	@Override
	protected void onResume() {
		createStatsHandler();
		mGetCpuStats.setEnabled(true);
		mGetMemStats.setEnabled(true);
		mGetMemStats.setEnabled(true);
		statsHandler.post(statsUpdateRunnable);
		super.onResume();
	}

	private void executeTop() {
		java.lang.Process p = null;
		BufferedReader in = null;
		try {
			p = Runtime.getRuntime().exec("top -n 1 -d 1");
			if (p == null) {
				MMCLogger.logToFile(MMCLogger.Level.WARNING, TAG, "executeTop", "Requested Program cannot be executed.");
				Log.e("executeTop", "Requested Program cannot be executed.");
				return;
			}
			in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			int cpuIndex = -1;
			int nameIndex = -1;
			int pidIndex = -1;
			listOfData = new ArrayList<CPUStatBean>();
			while ((line = in.readLine()) != null) {
				CPUStatBean appData = new CPUStatBean();
				if (line.contains("CPU%") && line.contains("Name")) {
					line = line.trim();
					String[] headers = line.split("\\s+");
					for (int i = 0; i < headers.length; i++) {
						if (headers[i].equals("PID")) {
							pidIndex = i;
						} else if (headers[i].equals("CPU%")) {
							cpuIndex = i;
						} else if (headers[i].equals("Name")) {
							nameIndex = i;
						}
					}
					continue;
				}
				if (line.contains(".") && pidIndex != -1 && nameIndex != -1
						&& cpuIndex != -1) {
					line = line.trim();
					String[] listOfParams = line.split("\\s+");
					ApplicationInfo ai;
					final PackageManager pm = getApplicationContext()
							.getPackageManager();
					try {
						appData.setPid(listOfParams[pidIndex].trim());
						appData.setCpuUsage(listOfParams[cpuIndex].trim().replace(
								"%", ""));
						appData.setPackageName(listOfParams[nameIndex].trim());
						
						ai = pm.getApplicationInfo(
								listOfParams[nameIndex].trim(), 0);
					} catch (final NameNotFoundException e) {
						ai = null;
					} catch (Exception e)
					{
						ai = null;
						appData = null;
					}
					if (appData != null)
					{
						Drawable icon = (ai != null ? pm.getApplicationIcon(ai)
								: null);
						appData.setIcon(icon);
						if (ai != null) {
							if (isARunningProcess(ai.processName)
									&& ai.uid >= 10000) {
								appData.setAppName((String) pm
										.getApplicationLabel(ai));
								listOfData.add(appData);
							}
						}
					}
				}
			}
			MMCLogger.logToFile(MMCLogger.Level.WARNING, TAG, "executeTop", "total number of running processes" + listOfData.size());
			
		} catch (IOException e) {
			MMCLogger.logToFile(MMCLogger.Level.WARNING, TAG, "executeTop", "Requested Program cannot be executed.", e);
			
		} finally {
			try {
				if (in != null)
					in.close();
				destroyProcess(p);
			} catch (IOException e) {
				MMCLogger.logToFile(MMCLogger.Level.WARNING, TAG, "executeTop", "error in closing and destroying top process.", e);
			}
		}
	}

	private boolean isARunningProcess(String processName) {
		if (processName == null)
			return false;
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> processes = manager
				.getRunningAppProcesses();

		for (RunningAppProcessInfo process : processes) {
			if (processName.equals(process.processName)) {
				return true;
			}
		}
		return false;
	}

	private static void destroyProcess(Process process) {
		try {
			if (process != null) {
				process.exitValue();
			}
		} catch (IllegalThreadStateException e) {
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "destroyProcess", "Illegal state exception occurred while destroying the process.", e);
			process.destroy();
		}
	}

	private ArrayList<CPUStatBean> listOfData;

	private ArrayList<MemoryStatBean> getMemoryStats() {
		ArrayList<MemoryStatBean> numberOfApplications = new ArrayList<MemoryStatBean>();
		if (listOfData != null) {
			int[] numberOfProcesses = new int[listOfData.size()];
			for (int i = 0; i < listOfData.size(); i++) {
				numberOfProcesses[i] = Integer.parseInt(listOfData.get(i)
						.getPid().trim());
			}
			ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			Debug.MemoryInfo[] memoryInfo = activityManager
					.getProcessMemoryInfo(numberOfProcesses);
			double totalRAM = 0;
			for (int i = 0; i < memoryInfo.length; i++) {
				if (memoryInfo[i].getTotalPss() > 0) {
					MemoryStatBean bean = new MemoryStatBean();
					bean.setAppName(listOfData.get(i).getAppName());
					bean.setIcon(listOfData.get(i).getIcon());
					bean.setPackageName(listOfData.get(i).getPackageName());
					totalRAM = totalRAM
							+ (memoryInfo[i].getTotalPss() / 1024.0);
					bean.setMemoryUsage(String.format(Locale.getDefault(),
							"%.2f", memoryInfo[i].getTotalPss() / 1024.0));
					bean.setPid(listOfData.get(i).getPid());
					numberOfApplications.add(bean);
				}
			}
			return numberOfApplications;
		}
		return null;
	}
}

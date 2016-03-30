package com.cortxt.app.mmcutility.DataObjects.database;

import java.util.ArrayList;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

//import com.cortxt.app.MMC.Utils.MMCLogger;
import com.cortxt.app.mmcutility.DataObjects.beans.DataStatsBean;
import com.cortxt.app.mmcutility.DataObjects.beans.RunningAppsBean;
import com.cortxt.app.mmcutility.DataObjects.beans.SMSDetailsBean;


public class DataMonitorDBReader {

	private SQLiteDatabase sqlDB;
	Context context;
	public static final String TAG = DataMonitorDBReader.class.getSimpleName();

	/**
	 * Returns list of connection failed records available in DB
	 * 
	 * @param context
	 *            the context in which the database operations should be
	 *            performed
	 * @return ArrayList<ConnectionFailureDetails>, returns list of records
	 *         available in DB
	 */
//	public ArrayList<ConnectionFailureBean> getConnectionFailedRecords(
//			Context context) {
//
//		if (context == null) {
//			return null;
//		}
//		ArrayList<ConnectionFailureBean> connectionFailureBeans = new ArrayList<ConnectionFailureBean>();
//		DatabaseHandler dbHandler = new DatabaseHandler(context);
//		sqlDB = dbHandler.getReadableDatabase();
//		try {
//			Cursor cursor = sqlDB.query(
//					DatabaseHandler.TABLE_CONNECTION_FAILURES, null, null,
//					null, null, null, null);
//			if (cursor != null) {
//				if (cursor.moveToFirst()) {
//					do {
//						if (connectionFailureBeans != null) {
//							ConnectionFailureBean connectionFailedDetails = new ConnectionFailureBean();
//							connectionFailedDetails.setConnectionType(cursor.getInt(1));
//							connectionFailedDetails.setConnectionFailureTime(cursor.getString(2));
//							connectionFailureBeans.add(connectionFailedDetails);
//						}
//					} while (cursor.moveToNext());
//				}
//				cursor.close();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
////			Logger.logStackTrace(e);
//		} finally {
//			sqlDB.close();
//		}
//
//		return connectionFailureBeans;
//	}


	/**
	 * Returns the total records count of connection failures that are present in DB
	 * 
	 * @param context
	 *            the context in which the database operations should be
	 *            performed
	 * @return int, connection failure total records count
	 */
	public synchronized int getCFTotalRecordsCount(Context context) {
		int count = 0;
		if (context == null) {
			return 0;
		}
		try
		{
			DatabaseHandler dbHandler = new DatabaseHandler(context);
			sqlDB = dbHandler.getReadableDatabase();
		}
		catch (Exception e)
		{
			return 0;
		}
		try {
			Cursor cursor = sqlDB.query(DatabaseHandler.TABLE_CONNECTION_FAILURES,
					null, null, null, null, null, null);
			if (cursor != null) {
				count = cursor.getCount();
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
//			Logger.logStackTrace(e);
		} finally {
			sqlDB.close();
		}

		return count;
	}


	/**
	 * Returns list of sms records available in DB
	 * 
	 * @param context
	 *            the context in which the database operations should be
	 *            performed
	 * @return ArrayList<SMSDetailsBean>, returns list of sms records available
	 *         in DB
	 */
	public synchronized ArrayList<SMSDetailsBean> getSMSRecords(Context context) {

		if (context == null) {
			return null;
		}
		ArrayList<SMSDetailsBean> smsBeans = new ArrayList<SMSDetailsBean>();
		try
		{
			DatabaseHandler dbHandler = new DatabaseHandler(context);
			sqlDB = dbHandler.getReadableDatabase();
		}
		catch (Exception e)
		{
			return smsBeans;
		}
		try {
			Cursor cursor = sqlDB.query(DatabaseHandler.TABLE_SMS_STATISTICS,
					null, null, null, null, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						if (smsBeans != null) {
							SMSDetailsBean smsDetails = new SMSDetailsBean();
							smsDetails.setSMSId(cursor.getInt(1));
							smsDetails.setSMSType(cursor.getInt(2));
							smsDetails.setSMSAddress(cursor.getString(3));
							smsDetails.setSMSDate(cursor.getString(4));
							smsBeans.add(smsDetails);
						}
					} while (cursor.moveToNext());
				}
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
//			Logger.logStackTrace(e);
		} finally {
			sqlDB.close();
		}

		return smsBeans;
	}

	/**
	 * Returns the total records count of sms that are present in DB
	 * 
	 * @param context
	 *            the context in which the database operations should be
	 *            performed
	 * @return int, sms total records count
	 */
	public synchronized int getSMSTotalRecordsCount(Context context) {
		int count = 0;
		if (context == null) {
			return 0;
		}
		try
		{
			DatabaseHandler dbHandler = new DatabaseHandler(context);
			sqlDB = dbHandler.getReadableDatabase();
		}
		catch (Exception e)
		{
			return 0;
		}
		try {
			Cursor cursor = sqlDB.query(DatabaseHandler.TABLE_SMS_STATISTICS,
					null, null, null, null, null, null);
			if (cursor != null) {
				count = cursor.getCount();
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
//			Logger.logStackTrace(e);
		} finally {
			sqlDB.close();
		}

		return count;
	}

	/**
	 * Returns the records count for sms of given sms_type
	 * 
	 * @param context
	 *            the context in which the database operations should be
	 *            performed
	 * @param sms_type
	 *            the type of the sms for which records count to be retrieved
	 * @return int, records count from sms table for the given sms_type
	 */
	public synchronized int getSMSTypeRecordsCount(Context context, int sms_type) {
		int count = 0;
		if (context == null || sms_type == -1) {
			return 0;
		}
		try
		{
			DatabaseHandler dbHandler = new DatabaseHandler(context);
			sqlDB = dbHandler.getReadableDatabase();
		}
		catch (Exception e)
		{
			return 0;
		}
		String whereClause = "sms_type = '" + sms_type + "'";
		try {
			Cursor cursor = sqlDB.query(DatabaseHandler.TABLE_SMS_STATISTICS,
					null, whereClause, null, null, null, null);
			if (cursor != null) {
				count = cursor.getCount();
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
//			Logger.logStackTrace(e);
		} finally {
			sqlDB.close();
		}

		return count;
	}

	/**
	 * Verifies whether the given sms record exists in the table or not
	 * 
	 * @param context
	 *            the context in which the database operations should be
	 *            performed
	 * @param sms_id
	 *            id of the sms
	 * @return boolean, returns the status whether the given sms record exists
	 *         or not
	 */
	public synchronized boolean isSMSIdExists(Context context, int sms_id) {
		boolean result = false;
		if (context == null || sms_id == -1) {
			return false;
		}
		try
		{
			DatabaseHandler dbHandler = new DatabaseHandler(context);
			sqlDB = dbHandler.getReadableDatabase();
		}
		catch (Exception e)
		{
			return false;
		}
		String[] projections = { "sms_type" };
		String where = "sms_id = '" + sms_id + "'";
		try {
			Cursor cursor1 = sqlDB.query(DatabaseHandler.TABLE_SMS_STATISTICS,
					projections, where, null, null, null, null);
			if (cursor1 != null) {
				if (cursor1.moveToFirst()) {
					int smsstats_id = cursor1.getInt(0);
					result = true;
				}
				cursor1.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlDB.close();

		}
		return result;
	}

	/**
	 * Returns list of throughput records available in DB
	 * 
	 * @param context
	 *            the context in which the database operations should be
	 *            performed
	 * @return ArrayList<DataStatsBean>, returns list of data stats records(with
	 *         throughput values) available in DB
	 */
	public synchronized ArrayList<DataStatsBean> getThroughtputRecords(Context context) {
//		if (context == null) {
			return null;
//		}
//		ArrayList<DataStatsBean> throughputBeans = new ArrayList<DataStatsBean>();
//		DatabaseHandler dbHandler = new DatabaseHandler(context);
//		sqlDB = dbHandler.getReadableDatabase();
//		try {
//			Cursor cursor = sqlDB.query(
//					DatabaseHandler.TABLE_THROUGHPUT_STATISTICS, null, null, null, null, null, null);
//			if (cursor != null) {
//				if (cursor.moveToFirst()) {
//					do {
//						if (throughputBeans != null) {
//							DataStatsBean throughputDetails = new DataStatsBean();
//							throughputDetails.setAppUid(cursor.getInt(1));
//							throughputDetails.setAppPkgName(cursor.getString(2));						
//							throughputDetails.setSent(cursor.getDouble(3));
//							throughputDetails.setReceived(cursor.getDouble(4));
//							throughputDetails.setThroughput(cursor.getDouble(5));							
//							throughputDetails.setAppData(cursor.getString(6));
//							throughputDetails.setCurrentSent(cursor.getDouble(7));
//							throughputDetails.setCurrentReceived(cursor.getDouble(8));
//							
//							
//							if(throughputDetails.getAppPkgName().equals("com.android.vending")) {
//								MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "getThroughputRecords", "for com.andoid.vending");
//								System.out.println(" getThroughputRecords for com.andoid.vending");
//							}
//							
//							throughputBeans.add(throughputDetails);
//						}
//					} while (cursor.moveToNext());
//				}
//				cursor.close();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			sqlDB.close();
//		}

//		return throughputBeans;
	}

	/**
	 * Returns the throughput record for the given application package name
	 * available in DB
	 * 
	 * @param context
	 *            the context in which the database operations should be
	 *            performed
	 * @param app_package
	 *            application's package name
	 * @return DataStatsBean, returns throughput record(with throughput values)
	 */
	public synchronized DataStatsBean getThroughputRecordForAppPackage(Context context, String app_package) {

//		if (context == null || app_package == null) {
			return null;
//		}
//		DataStatsBean throughputDetails = new DataStatsBean();
//		DatabaseHandler dbHandler = new DatabaseHandler(context);
//		sqlDB = dbHandler.getReadableDatabase();
//		String where = "app_package = '" + app_package + "'";
//		try {
//			Cursor cursor = sqlDB.query(
//					DatabaseHandler.TABLE_THROUGHPUT_STATISTICS, null, where, null, null, null, null);
//			if (cursor != null) {
//				if (cursor.moveToFirst()) {
//					throughputDetails.setAppUid(cursor.getInt(1));
//					throughputDetails.setAppPkgName(cursor.getString(2));
//					throughputDetails.setSent(cursor.getDouble(3));
//					throughputDetails.setReceived(cursor.getDouble(4));
//					throughputDetails.setThroughput(cursor.getDouble(5));
//					throughputDetails.setAppData(cursor.getString(6));
//					throughputDetails.setCurrentSent(cursor.getDouble(7));
//					throughputDetails.setCurrentReceived(cursor.getDouble(8));
//					if(throughputDetails.getAppPkgName().equals("com.android.vending")) {
//						MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "getThroughputRecord", "for com.andoid.vending");
//						System.out.println("getThroughputRecord for com.andoid.vending");
//					}
//				}
//				cursor.close();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			sqlDB.close();
//		}
//
//		return throughputDetails;
	}

	/**
	 * Verifies whether the given throughput record exists in the table or not
	 * 
	 * @param context
	 *            the context in which the database operations should be
	 *            performed
	 * @param app_package
	 *            application's package name
	 * @return boolean, returns the status whether the given throughput record
	 *         exists or not
	 */
	public synchronized boolean isAppPackageExists(Context context, String app_package) {
		boolean result = false;
		if (context == null || app_package == null) {
			return false;
		}
		try
		{
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		sqlDB = dbHandler.getReadableDatabase();
		}
		catch (Exception e)
		{
			return false;
		}
		String[] projections = { "app_throughput" };
		String where = "app_package = '" + app_package + "'";
		try {
			Cursor cursor1 = sqlDB.query(
					DatabaseHandler.TABLE_THROUGHPUT_STATISTICS, projections,
					where, null, null, null, null);
			if (cursor1 != null) {
				if (cursor1.moveToFirst()) {
					double throughput = cursor1.getDouble(0);
					// System.out.println("THROUGHPUT db record exists");
					result = true;
				}
				cursor1.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlDB.close();
		}
		return result;
	}


	/**
	 * Returns list of crash records available in DB
	 * 
	 * @param context
	 *            the context in which the database operations should be
	 *            performed
	 * @return ArrayList<CrashDetailsBean>, returns list of crash details records
	 * 			available in DB
	 */
//	public ArrayList<CrashDetailsBean> getCrashRecords(Context context) {
//
//		if (context == null) {
//			return null;
//		}
//		ArrayList<CrashDetailsBean> crashBeans = new ArrayList<CrashDetailsBean>();
//		DatabaseHandler dbHandler = new DatabaseHandler(context);
//		sqlDB = dbHandler.getReadableDatabase();
//		try {
//			Cursor cursor = sqlDB.query(
//					DatabaseHandler.TABLE_CRASH_STATISTICS, null, null,
//					null, null, null, null);
//
//			if (cursor != null) {
//				if (cursor.moveToFirst()) {
//					do {
//						if (crashBeans != null) {
//							CrashDetailsBean crashDetails = new CrashDetailsBean();
//							crashDetails.setAppUId(cursor.getInt(1));
//							crashDetails.setAppPackage(cursor.getString(2));
//							crashDetails.setAppName(cursor.getString(3));
//							crashDetails.setLastCrashDate(cursor.getString(4));
//							crashDetails.setCrashCount(cursor.getInt(5));
//							crashBeans.add(crashDetails);
//						}
//					} while (cursor.moveToNext());
//				}
//				cursor.close();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
////			Logger.logStackTrace(e);
//		} finally {
//			sqlDB.close();
//		}
//
//		return crashBeans;
//	}

	/**
	 * Returns the crash record for the given application package name
	 * available in DB
	 * 
	 * @param context
	 *            the context in which the database operations should be
	 *            performed
	 * @param app_package
	 *            application's package name
	 * @return CrashDetailsBean, returns crash details record
	 */
//	public CrashDetailsBean getCrashRecordForAppPackage(Context context,
//			String app_package) {
//
//		if (context == null || app_package == null) {
//			return null;
//		}
//		CrashDetailsBean crashDetails = new CrashDetailsBean();
//		DatabaseHandler dbHandler = new DatabaseHandler(context);
//		sqlDB = dbHandler.getReadableDatabase();
//		String where = "app_package = '" + app_package + "'";
//		try {
//			Cursor cursor = sqlDB.query(
//					DatabaseHandler.TABLE_CRASH_STATISTICS, null, where,
//					null, null, null, null);
//			if (cursor != null) {
//				if (cursor.moveToFirst()) {
//					crashDetails.setAppUId(cursor.getInt(1));
//					crashDetails.setAppPackage(cursor.getString(2));
//					crashDetails.setAppName(cursor.getString(3));
//					crashDetails.setLastCrashDate(cursor.getString(4));
//					crashDetails.setCrashCount(cursor.getInt(5));
//				}
//				cursor.close();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
////			Logger.logStackTrace(e);
//		} finally {
//			sqlDB.close();
//		}
//
//		return crashDetails;
//	}

	/**
	 * Verifies whether the given crash record exists in the table or not
	 * 
	 * @param context
	 *            the context in which the database operations should be
	 *            performed
	 * @param app_package
	 *            application's package name
	 * @return boolean, returns the status whether the given crash record
	 *         exists or not
	 */
	public synchronized boolean isCrashForAppPackageExists(Context context, String app_package) {
		boolean result = false;
		if (context == null || app_package == null) {
			return false;
		}
		try
		{
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		sqlDB = dbHandler.getReadableDatabase();
		}
		catch (Exception e)
		{
			return false;
		}
		String[] projections = { "crash_count" };
		String where = "app_package = '" + app_package + "'";
		try {
			Cursor cursor1 = sqlDB.query(
					DatabaseHandler.TABLE_CRASH_STATISTICS, projections,
					where, null, null, null, null);
			if (cursor1 != null) {
				if (cursor1.moveToFirst()) {
					int crash_count = cursor1.getInt(0);
					result = true;
				}
				cursor1.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlDB.close();
		}
		return result;
	}


	public synchronized DataStatsBean isPackageDataStatsExists(Context context, String packageName) {
		DataStatsBean tempBean = null;
		if (context == null || packageName == null) {
			return null;
		}
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		try {
			sqlDB = dbHandler.getReadableDatabase();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			return null;
		}
		if (sqlDB == null) {
			return null;
		}
		String where = DatabaseHandler.KEY_PACKAGE_NAME + "=?";
		try {
			Cursor cursor = sqlDB
					.query(DatabaseHandler.TABLE_DATA_STATISTICS, null, where,
							new String[] { packageName }, null, null, null);
			if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
				tempBean = new DataStatsBean(context);
				tempBean.setCellularReceived(cursor.getDouble(cursor
						.getColumnIndex(DatabaseHandler.KEY_CELLULAR_RECEIVED)));
				tempBean.setCellularSent(cursor.getDouble(cursor
						.getColumnIndex(DatabaseHandler.KEY_CELLULAR_SENT)));
				tempBean.setWifiReceived(cursor.getDouble(cursor
						.getColumnIndex(DatabaseHandler.KEY_WIFI_RECEIVED)));
				tempBean.setWifiSent(cursor.getDouble(cursor
						.getColumnIndex(DatabaseHandler.KEY_WIFI_SENT)));
				tempBean.setSent(cursor.getDouble(cursor
						.getColumnIndex(DatabaseHandler.KEY_EXISTED_SENT)));
				tempBean.setReceived(cursor.getDouble(cursor
						.getColumnIndex(DatabaseHandler.KEY_EXISTED_RECEIVED)));
				cursor.close();
				return tempBean;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try{
			sqlDB.close();
			} catch (Exception e) {}
		}
	}

	
	
	public synchronized ArrayList<DataStatsBean> getDataStatistics(Context context, String packageNames) {
			if (context == null) {
				return null;
			}
			DatabaseHandler dbHandler = new DatabaseHandler(context);
			try {
				sqlDB = dbHandler.getReadableDatabase();
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				return null;
			}
			String where = "";
			if (packageNames != null) 
				where = " where package_name in(" + packageNames + ")";
			
			String queryString = "select " + DatabaseHandler.KEY_PACKAGE_NAME + ","
					+ DatabaseHandler.KEY_APP_NAME 
					+ ",((" + DatabaseHandler.KEY_WIFI_SENT + ")/1024) as wifi_sent_total,"
					+ "((" + DatabaseHandler.KEY_WIFI_RECEIVED + ")/1024) as wifi_received_total,"
					+ "((" + DatabaseHandler.KEY_CELLULAR_SENT + ")/1024) as cellular_sent_total,"
					+ "((" + DatabaseHandler.KEY_CELLULAR_RECEIVED + ")/1024) as cellular_received_total,"
					+ DatabaseHandler.KEY_WIFI_SENT + "+"
					+ DatabaseHandler.KEY_WIFI_RECEIVED + "+"
					+ DatabaseHandler.KEY_CELLULAR_SENT + "+"
					+ DatabaseHandler.KEY_CELLULAR_RECEIVED 
					+ " as total from " + DatabaseHandler.TABLE_DATA_STATISTICS
					+ where
					+ " order by total DESC," + DatabaseHandler.KEY_APP_NAME
					+ " ASC";
			
//			String queryString = "select " + DatabaseHandler.KEY_PACKAGE_NAME + ","
//					+ DatabaseHandler.KEY_APP_NAME + ",(("
//					+ DatabaseHandler.KEY_WIFI_SENT + "+"
//					+ DatabaseHandler.KEY_LAST_BOOT_WIFI_SENT
//					+ ")/1024) as wifi_sent_total,(("
//					+ DatabaseHandler.KEY_WIFI_RECEIVED + "+"
//					+ DatabaseHandler.KEY_LAST_BOOT_WIFI_RECEIVED
//					+ ")/1024) as wifi_received_total,(("
//					+ DatabaseHandler.KEY_CELLULAR_SENT + "+"
//					+ DatabaseHandler.KEY_LAST_BOOT_CELLULAR_SENT
//					+ ")/1024) as cellular_sent_total,(("
//					+ DatabaseHandler.KEY_CELLULAR_RECEIVED + "+"
//					+ DatabaseHandler.KEY_LAST_BOOT_CELLULAR_RECEIVED
//					+ ")/1024) as cellular_received_total,"
//					+ DatabaseHandler.KEY_WIFI_SENT + "+"
//					+ DatabaseHandler.KEY_WIFI_RECEIVED + "+"
//					+ DatabaseHandler.KEY_CELLULAR_SENT + "+"
//					+ DatabaseHandler.KEY_CELLULAR_RECEIVED + "+"
//					+ DatabaseHandler.KEY_LAST_BOOT_WIFI_SENT + "+"
//					+ DatabaseHandler.KEY_LAST_BOOT_WIFI_RECEIVED + "+"
//					+ DatabaseHandler.KEY_LAST_BOOT_CELLULAR_SENT + "+"
//					+ DatabaseHandler.KEY_LAST_BOOT_CELLULAR_RECEIVED
//					+ " as total from " + DatabaseHandler.TABLE_DATA_STATISTICS
//					+ where
//					+ " order by total DESC," + DatabaseHandler.KEY_APP_NAME
//					+ " ASC";
			Cursor cursor = sqlDB.rawQuery(queryString, null);
			if (cursor.getCount() <= 0 || !cursor.moveToFirst()) {
				sqlDB.close();
				return null;
			}
			ArrayList<DataStatsBean> dataStats = new ArrayList<DataStatsBean>();
			do {
				DataStatsBean bean = new DataStatsBean(context);
				String packageName = cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_PACKAGE_NAME));
				
				bean.setAppPkgName(packageName);
				//bean.setAppName(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_APP_NAME)));
				bean.setWifiSent(cursor.getDouble(cursor.getColumnIndex("wifi_sent_total")));
				bean.setWifiReceived(cursor.getDouble(cursor.getColumnIndex("wifi_received_total")));
				bean.setCellularSent(cursor.getDouble(cursor.getColumnIndex("cellular_sent_total")));
				bean.setCellularReceived(cursor.getDouble(cursor.getColumnIndex("cellular_received_total")));
				PackageManager packageInfo = context.getPackageManager();
				try {
					bean.setIcon(packageInfo.getApplicationIcon(packageName));
					dataStats.add(bean);
				} catch (NameNotFoundException e) {
					
				}
				
			} while (cursor.moveToNext());
			cursor.close();
			sqlDB.close();
			return dataStats;
		}

	public synchronized ArrayList<RunningAppsBean> getListOfRunningApps(Context context) {
		if (context == null) {
			return null;
		}
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		try {
			sqlDB = dbHandler.getReadableDatabase();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			return null;
		}
		if (sqlDB == null) {
			return null;
		}
		Cursor cursor = sqlDB.query(
				DatabaseHandler.TABLE_RUNNING_APP_DURATION_STATISTICS, null,
				null, null, null, null, null);
		if (cursor.getCount() <= 0 || !cursor.moveToFirst()) {
			sqlDB.close();
			return null;
		}
		ArrayList<RunningAppsBean> listOfRunningApps = new ArrayList<RunningAppsBean>();
		do {
			RunningAppsBean bean = new RunningAppsBean();
			
			String appPackageName = cursor.getString(cursor.getColumnIndex("package_name"));
			bean.setAppPkgName(appPackageName);
			
			long start = cursor.getLong(cursor.getColumnIndex("started_time"));
			bean.setStartedTime(start);
		
			//long current = cursor.getLong(cursor.getColumnIndex("current_time"));
			//bean.setCurrentTime(current);
			
			long total = cursor.getLong(cursor.getColumnIndex("total_time"));
			//total = total <= 0 ? start - current : total;
			bean.setActiveTime(total);
			
			final PackageManager pm = context.getPackageManager();
			ApplicationInfo ai;
			try {
				ai = pm.getApplicationInfo(appPackageName, 0);
				bean.setAppName(pm.getApplicationLabel(ai).toString());
				listOfRunningApps.add(bean);
			} catch (NameNotFoundException e) {
				DataMonitorDBWriter writer = new DataMonitorDBWriter();
				long deletedStatus = writer.deleteUserDeletedAppFromDB(context,appPackageName);
//				Logger.info(appPackageName
//						+ " has been removed from the device and it's deleted status in DB is:"
//						+ deletedStatus);
			}

		} while (cursor.moveToNext());
		cursor.close();
		sqlDB.close();
		return listOfRunningApps;
	}

	public synchronized ArrayList<RunningAppsBean> getListOfIdleApps(Context context, String packageNames) {
		ArrayList<RunningAppsBean> listOfApps = new ArrayList<RunningAppsBean>();
		if (context == null) {
			return listOfApps;
		}
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		
		try{
		sqlDB = dbHandler.getReadableDatabase();
		}
		catch (Exception e)
		{
			return listOfApps;
		}
		String[] projections = { "package_name,started_time,total_time" };
		String where = "package_name not in(" + packageNames + ") and current_time<>0 and started_time<>0";
		Cursor cursor  = null;
		try {
			cursor = sqlDB.query(
					DatabaseHandler.TABLE_RUNNING_APP_DURATION_STATISTICS,
					projections, where, null, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				do {
					RunningAppsBean bean = new RunningAppsBean();
					bean.setAppPkgName(cursor.getString(cursor.getColumnIndex("package_name")));
					bean.setStartedTime(cursor.getLong(cursor.getColumnIndex("started_time")));
					bean.setActiveTime(cursor.getLong(cursor.getColumnIndex("total_time")));
					listOfApps.add(bean);
				} while (cursor.moveToNext());				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
			sqlDB.close();
		}
		return listOfApps;
	}

	public synchronized long hasAppDuration(Context context, String app_package) {
		if (context == null || app_package == null) {
			return -1;
		}
		
		try
		{
			DatabaseHandler dbHandler = new DatabaseHandler(context);
			sqlDB = dbHandler.getReadableDatabase();
		}
		catch (Exception e)
		{
			return -1;
		}
		String[] projections = { "started_time" };
		String where = "package_name=?";
		try {
			Cursor cursor = sqlDB.query(
					DatabaseHandler.TABLE_RUNNING_APP_DURATION_STATISTICS,
					projections, where, new String[] { app_package }, null,
					null, null);
			if (cursor != null) {
				if (cursor.getCount() <= 0) {
					return 0;
				} else if (cursor.moveToFirst()) {
					long duration = cursor.getLong(cursor.getColumnIndex("started_time"));
					if(duration == 0) {
						return 2;
					}
					if (duration > 0) {
						return 1;
					}
					else {
						return -1;
					}
				}
				cursor.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlDB.close();
		}
		return -1;
	}
	
	
	public synchronized ArrayList<RunningAppsBean> getRunningAppsData(Context context, String packageNames) {
		if (context == null) {
			return null;
		}
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		try {
			sqlDB = dbHandler.getReadableDatabase();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			return null;
		}
		if (sqlDB == null) {
			return null;
		}
		String where = "", a = "";
		if (packageNames != null) 
			where = " where package_name in(" + packageNames + ")";
		
		String runtime_cols =  "started_time,total_time, " ;
		String queryString = "select data.package_name,"
				+ "data.app_name"
				+ ",started_time,total_time,"
				+ DatabaseHandler.KEY_WIFI_SENT + ","
				+ DatabaseHandler.KEY_WIFI_RECEIVED + ","
				+ DatabaseHandler.KEY_CELLULAR_SENT + ","
				+ DatabaseHandler.KEY_CELLULAR_RECEIVED + ","
				+ DatabaseHandler.KEY_LAST_CHECK_WIFI_SENT + ","
				+ DatabaseHandler.KEY_LAST_CHECK_WIFI_RECEIVED + ","
				+ DatabaseHandler.KEY_LAST_CHECK_CELLULAR_SENT + ","
				+ DatabaseHandler.KEY_LAST_CHECK_CELLULAR_RECEIVED
				+ " from " + DatabaseHandler.TABLE_DATA_STATISTICS + " as data "
				+ " left outer join " + DatabaseHandler.TABLE_RUNNING_APP_DURATION_STATISTICS  + " as run "
				+ " on data.package_name = run.package_name "
				+ where
				+ " order by data.app_name ASC";
		Cursor cursor = sqlDB.rawQuery(queryString, null);
		if (cursor.getCount() <= 0 || !cursor.moveToFirst()) {
			sqlDB.close();
			return null;
		}
		ArrayList<RunningAppsBean> dataStats = new ArrayList<RunningAppsBean>();
		do {
			RunningAppsBean bean = new RunningAppsBean();
			String packageName = cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_PACKAGE_NAME));
			bean.setAppPkgName(packageName);
			
			bean.setStartedTime(cursor.getLong(cursor.getColumnIndex("started_time")));
			bean.setActiveTime(cursor.getLong(cursor.getColumnIndex("total_time")));
			if (packageName.toLowerCase().indexOf("mmc") > 0)
				a = "";
			//bean.setAppName(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_APP_NAME)));
			bean.setWifiSent(cursor.getDouble(cursor.getColumnIndex(DatabaseHandler.KEY_WIFI_SENT)));
			bean.setWifiReceived(cursor.getDouble(cursor.getColumnIndex(DatabaseHandler.KEY_WIFI_RECEIVED)));
			bean.setCellularSent(cursor.getDouble(cursor.getColumnIndex(DatabaseHandler.KEY_CELLULAR_SENT)));
			bean.setCellularReceived(cursor.getDouble(cursor.getColumnIndex(DatabaseHandler.KEY_CELLULAR_RECEIVED)));
			
			bean.setWifiSentLast(cursor.getDouble(cursor.getColumnIndex(DatabaseHandler.KEY_LAST_CHECK_WIFI_SENT)));
			bean.setWifiReceivedLast(cursor.getDouble(cursor.getColumnIndex(DatabaseHandler.KEY_LAST_CHECK_WIFI_RECEIVED)));
			bean.setCellularSentLast(cursor.getDouble(cursor.getColumnIndex(DatabaseHandler.KEY_LAST_CHECK_CELLULAR_SENT)));
			bean.setCellularReceivedLast(cursor.getDouble(cursor.getColumnIndex(DatabaseHandler.KEY_LAST_CHECK_CELLULAR_RECEIVED)));
			
			PackageManager packageInfo = context.getPackageManager();
			dataStats.add(bean);
			
		} while (cursor.moveToNext());
		cursor.close();
		sqlDB.close();
		return dataStats;
	}

//	public synchronized ArrayList<RunningAppsBean> getRunningAppsStartTimes(Context context, String packageNames) {
//		ArrayList<RunningAppsBean> listOfApps = new ArrayList<RunningAppsBean>();
//		if (context == null) {
//			return listOfApps;
//		}
//		try
//		{
//			DatabaseHandler dbHandler = new DatabaseHandler(context);
//			sqlDB = dbHandler.getReadableDatabase();
//		}catch (Exception e)
//		{
//			return listOfApps;
//		}
//		String[] projections = { "package_name,started_time,total_time, " };
//		String where = "";
//		if (packageNames != null)
//			where += "package_name in(" + packageNames + ")";
//		Cursor cursor  = null;
//		try {
//			cursor = sqlDB.query(
//					DatabaseHandler.TABLE_RUNNING_APP_DURATION_STATISTICS,
//					projections, where, null, null, null, null);
//			if (cursor != null && cursor.moveToFirst()) {
//				do {
//					RunningAppsBean bean = new RunningAppsBean();
//					bean.setAppPkgName(cursor.getString(cursor.getColumnIndex("package_name")));
//					bean.setStartedTime(cursor.getLong(cursor.getColumnIndex("started_time")));
//					bean.setActiveTime(cursor.getLong(cursor.getColumnIndex("total_time")));
//					listOfApps.add(bean);
//				} while (cursor.moveToNext());				
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (cursor != null)
//				cursor.close();
//			sqlDB.close();
//		}
//		return listOfApps;
//	}

	
	public synchronized String getStatsString(Context context) {
		String txt = "", temp = "";
		if (context == null) {
			return "";
		}
		try{
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		sqlDB = dbHandler.getReadableDatabase();
		}
		catch (Exception e)
		{
			return "";
		}
		
		try {
			Cursor cursor = sqlDB.query(DatabaseHandler.TABLE_STATS_STRING,null, null, null, null, null, null);
//			int n = cursor.getCount();
//			MMCLogger.logToFile(MMCLogger.Level.DEBUG, "DataMonitorDBReader", "getStatsString", "Initial cursor count: " + n);
			if (cursor.moveToFirst()) {
				do{
					if (cursor != null) {	
//						txt += (cursor.getString(cursor.getColumnIndex("stats_string")));
//						txt += (cursor.getString(0));
						temp = cursor.getString(1);
						if(temp.length() >0) {
							txt += temp;					
							if(!cursor.isLast()) {
								txt += ",";
							}
						}						
					}
				} while (cursor.moveToNext());				
			cursor.close();	
			}
		} catch (Exception e) {
			e.printStackTrace();
//			Logger.logStackTrace(e);
		} finally {
			sqlDB.close();
			
		}

		return txt;		
	}		
	
	public synchronized String getHighestThroughputApp(Context context) {
		
//		//get only the running apps
//		ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);        
//		List<RunningTaskInfo> runningTasks = manager.getRunningTasks(Integer.MAX_VALUE);
//        List<String> currentRunnningAppNames = new ArrayList<String>();      
//        for (RunningTaskInfo info : runningTasks) {
//            String pname = info.topActivity.getPackageName();
//                int status = getAppImportance(pname, context);
//                if(status >= 1 && status <= 4)
//                	currentRunnningAppNames.add(pname);
//        }		
//        
//        ArrayList<DataStatsBean> throughputData = new ArrayList<DataStatsBean>();	
//		throughputData = getThroughtputRecords(context);
//		
//		//get the running app with highest throughput
//		double max = 0;
		String appWithMax = "";
//		for(int i = 0; i < throughputData.size(); i++) {
//			if(currentRunnningAppNames.contains(throughputData.get(i).getAppPkgName())) {
//				if (max < throughputData.get(i).getCurrentReceived()) {
//					max = throughputData.get(i).getCurrentReceived();
//					appWithMax = throughputData.get(i).getAppPkgName();
//					
//					if(appWithMax.equals("com.android.vending")) {
//						System.out.println("getHighestThroughputApp for com.andoid.vending, throughput: " + max);
//					}
//				}
//			}			
//		}
//		System.out.println("getHighestThroughputApp for " + appWithMax + ", max : "+ max);
//		
		return appWithMax == "" ? null : appWithMax;
	}
	
	public synchronized String throughputToString(Context context, String packageName) {
		String throughputDataString = "";
//		try {
//			DataStatsBean bean = getThroughputRecordForAppPackage(context, packageName);	
//			
//			if(bean.getCurrentReceived() > 0) {
//				PackageManager packageManager = context.getPackageManager();
//				ApplicationInfo applicationInfo = null;
//							
//				//convert package name to app name			
//				try {	
//					applicationInfo = packageManager.getApplicationInfo(packageName, 0);
//				} catch (NameNotFoundException e) {
//					e.printStackTrace();
//				}
//				throughputDataString += (String)((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : packageName) + ",";
//				throughputDataString += String.valueOf(bean.getAppData());	
//			}
//			MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "throughputToString", "throughputDataString: " + throughputDataString);
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
		
		return throughputDataString;
	}
	
//	private int getAppImportance(String packageName, Context context) {	
//		ActivityManager manager = (ActivityManager) context.getSystemService(Service.ACTIVITY_SERVICE);	
//		List <RunningAppProcessInfo> runningProcesses = manager.getRunningAppProcesses();	    
//
//		for (RunningAppProcessInfo info : runningProcesses) {
//			String process = info.processName;
//			if(process.equals(packageName)) {
//				if(info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//					return 1;  //foreground
//				}
//				if(info.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
//					return 2; //background
//				}
//				if(info.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
//					return 3; //visible - actively visible to the user, but not in the immediate foreground
//				}
//				if(info.importance == RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE) {
//					return 4; //visible - actively visible to the user, but not in the immediate foreground
//				}
//				if(info.importance == RunningAppProcessInfo.IMPORTANCE_SERVICE) {
//		    	return 5; //visible - actively visible to the user, but not in the immediate foreground
//				}
//			}
//		}    
//		return 0;  
//	}	
}

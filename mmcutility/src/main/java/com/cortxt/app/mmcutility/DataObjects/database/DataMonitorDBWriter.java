package com.cortxt.app.mmcutility.DataObjects.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.DataObjects.beans.DataStatsBean;

public class DataMonitorDBWriter {

	private SQLiteDatabase sqlDB;

	/**
	 * Inserts connection failed details into DB and returns the status whether
	 * the record inserted successfully or not
	 * 
	 * @param context
	 *            the context in which the database operations should be
	 *            performed
	 * @param connectionType
	 *            type of the failed connection
	 * @param cf_time
	 *            time of the cf(connection failed)
	 * 
	 * @return boolean, returns the status of record insertion to DB
	 */
	public synchronized boolean uploadConnectionFailuresDetails(Context context,
			int connectionType, String cf_time) {
		System.out
				.println("DataMonitor -- DataMonitorDBWriter --- uploadConnectionFailuresDetails");
//		Logger.info(TAG
//				+ "DataMonitorDBWriter --- uploadConnectionFailuresDetails --- Begin");

		boolean result = false;
		if (context == null || connectionType == -1 || cf_time == null) {
			return result;
		}

		DatabaseHandler dbHandler = new DatabaseHandler(context);
		sqlDB = dbHandler.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put("connection_type", connectionType); // Connection type(MOBILE
														// or WIFI)
		values.put("cf_time", cf_time); // Connection failure time

		try {
			// Inserting Row
			long res = sqlDB.insert(DatabaseHandler.TABLE_CONNECTION_FAILURES,
					null, values);
			if (res == -1) {
				result = false;
//				Logger.info("DataMonitorDBWriter --- record not inserted to DB");
			} else {
				result = true;
//				Logger.info("DataMonitorDBWriter --- record inserted to DB");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlDB.close(); // Closing database connection
		}
//		Logger.info(TAG
//				+ "DataMonitorDBWriter uploadConnectionFailuresDetails --- End");
		return result;
	}

	/**
	 * Inserts sms statistics into DB and returns the status whether the record
	 * inserted successfully or not
	 * 
	 * @param context
	 *            the context in which the database operations should be
	 *            performed
	 * @param sms_id
	 *            id of the sms being recorded in the android platform's sms
	 *            content provider table
	 * @param sms_type
	 *            type of the sms (whether received sms or sent sms or failed
	 *            sms)
	 * @param sms_address
	 *            address represents the To/From depends on the context (if the
	 *            sms is received then sms_address represents the phone number
	 *            of 'From' contact else if the sms is sent then sms_address
	 *            represents the phone number of 'To' contact)
	 * @param sms_date
	 *            sms date
	 * 
	 * @return boolean, returns the status of record insertion to DB
	 */
	public synchronized boolean uploadSMSStatistics(Context context, int sms_id,
			int sms_type, String sms_address, String sms_date) {
//		Logger.info(TAG + "DataMonitorDBWriter --- uploadSMSStatistics --- Begin");

		boolean result = false;
		if (context == null || sms_id == -1 || sms_type == -1 || sms_address == null || sms_date == null) {
			return result;
		}

		DatabaseHandler dbHandler = new DatabaseHandler(context);
		sqlDB = dbHandler.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put("sms_id", sms_id);
		values.put("sms_type", sms_type);
		values.put("sms_address", sms_address);
		values.put("sms_date", sms_date);

		try {
			// Inserting Row
			long res = sqlDB.insert(DatabaseHandler.TABLE_SMS_STATISTICS, null,values);
			if (res == -1) {
				result = false;
//				Logger.info("DataMonitorDBWriter --- record not inserted to DB");
			} else {
				result = true;
//				Logger.info("DataMonitorDBWriter --- record inserted to DB");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlDB.close(); // Closing database connection
		}
//		Logger.info(TAG + "DataMonitorDBWriter uploadSMSStatistics --- End");
		return result;
	}

	/**
	 * Updates sms statistics in DB and returns the status whether the record
	 * updated succesfully or not
	 * 
	 * @param context
	 *            the context in which the database operations should be
	 *            performed
	 * @param sms_id
	 *            id of the sms being recorded in the android platform's sms
	 *            content provider table
	 * @param sms_type
	 *            type of the sms (whether received sms or sent sms or failed
	 *            sms)
	 * @param sms_address
	 *            address represents the To/From depends on the context (if the
	 *            sms is received then sms_address represents the phone number
	 *            of 'From' contact else if the sms is sent then sms_address
	 *            represents the phone number of 'To' contact)
	 * @param sms_date
	 *            sms date
	 * 
	 * @return boolean, returns the status of record update in DB
	 */
	public synchronized boolean updateSMSStatistics(Context context, int sms_id, int sms_type, String sms_address, String sms_date) {
		System.out
				.println("DataMonitor -- DataMonitorDBWriter --- updateSMSStatistics");
//		Logger.info(TAG
//				+ "DataMonitorDBWriter --- updateSMSStatistics --- Begin");

		boolean result = false;
		if (context == null || sms_id == -1 || sms_type == -1 || sms_address == null || sms_date == null) {
			return result;
		}

		DatabaseHandler dbHandler = new DatabaseHandler(context);
		sqlDB = dbHandler.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put("sms_type", sms_type);
		values.put("sms_address", sms_address);
		values.put("sms_date", sms_date);
		String whereClause = "sms_id = '" + sms_id + "'";
		try {
			// Inserting Row
			long res = sqlDB.update(DatabaseHandler.TABLE_SMS_STATISTICS,values, whereClause, null);
			if (res == -1) {
				result = false;
//				Logger.info("DataMonitorDBWriter --- record not updated in DB");
			} else {
				result = true;
//				Logger.info("DataMonitorDBWriter --- record updated in DB");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlDB.close(); // Closing database connection
		}
//		Logger.info(TAG + "DataMonitorDBWriter updateSMSStatistics --- End");
		return result;
	}

	/**
	 * Inserts throughput statistics into DB and returns the status whether the
	 * record inserted successfully or not
	 * 
	 * @param context
	 *            the context in which the database operations should be
	 *            performed
	 * @param app_uid
	 *            uid of the application
	 * @param app_package
	 *            application package name
	 * @param app_datatransferred
	 *            data transferred by the application over the network
	 * @param app_datareceived
	 *            data received by the application over the network
	 * @param app_throughput
	 *            calculated throughput of the application
	 * 
	 * @return boolean, returns the status of record insertion to DB
	 */
	public synchronized boolean uploadThroughtputStatistics(Context context, int app_uid, String app_package, 
			double app_datatransferred, double app_datareceived, double curSent, 
			double curReceived, double app_throughput) {

		boolean result = false;
		if (context == null || app_uid == -1 || app_package == null
				|| app_datatransferred < 0 || app_datareceived < 0
				|| app_throughput < 0 ) {
			return result;
		}

		DatabaseHandler dbHandler = new DatabaseHandler(context);
		sqlDB = dbHandler.getWritableDatabase();
		
		//new data: time, sent, received
		String data = "firstTime";
		
		ContentValues values = new ContentValues();
		values.put("app_uid", app_uid);
		values.put("app_package", app_package);
		values.put("app_datatransferred", app_datatransferred);
		values.put("app_datareceived", app_datareceived);
		values.put("app_throughput", app_throughput);
		values.put("app_appdata", data);
		values.put("app_curdatasent", curSent);
		values.put("app_curdatareceived", curReceived);
		
		if(app_package.equals("com.android.vending")) {
			System.out.println(" uploadThroughtputStatistics for com.andoid.vending, received since boot: " + app_datareceived);
		}

		try {
			// Inserting Row
			long res = sqlDB.insert(DatabaseHandler.TABLE_THROUGHPUT_STATISTICS, null, values);
			if (res == -1) {
				result = false;
//				Logger.info("DataMonitorDBWriter --- record not inserted to DB");
			} else {
				result = true;
//				Logger.info("DataMonitorDBWriter --- record inserted to DB");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlDB.close(); // Closing database connection
		}
//		Logger.info(TAG
//				+ "DataMonitorDBWriter uploadThroughtputStatistics --- End");
		return result;
	}

	/**
	 * Updates throughput statistics in DB and returns the status whether the
	 * record updated successfully or not
	 * 
	 * @param context
	 *            the context in which the database operations should be
	 *            performed
	 * @param app_uid
	 *            uid of the application
	 * @param app_package
	 *            application package name
	 * @param app_datatransferred
	 *            data transferred by the application over the network
	 * @param app_datareceived
	 *            data received by the application over the network
	 * @param app_throughput
	 *            calculated throughput of the application
	 * >>>>>>> feature_CrashStatistics

	 * @return boolean, returns the status of record update in DB
	 */
	public synchronized boolean updateThroughputStatistics(Context context, int app_uid, String app_package, double app_datatransferred,
			double app_datareceived,  double curSent, double curReceived, double app_throughput) {

		boolean result = false;
//		if (context == null || app_uid == -1 || app_package == null
//				|| app_datatransferred < 0 || app_datareceived < 0
//				|| app_throughput < 0) {
//			return result;
//		}
//		
//		if(app_package.equals("com.android.vending")) {
//			System.out.println(" updateThroughputStatistics for com.andoid.vending, throughput: " + app_throughput);
//		}
//
//		DatabaseHandler dbHandler = new DatabaseHandler(context);
//		sqlDB = dbHandler.getWritableDatabase();
//
//		DataMonitorDBReader dbReader = new DataMonitorDBReader();
//		DataStatsBean bean = dbReader.getThroughputRecordForAppPackage(context, app_package);
//		
//		//old string data
//		
//		String data = "null";
//		double sent = curSent;
//		double rec = curReceived;
//		try {
//			bean.getAppData();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		if(data.equals("firstTime") || data.equals("null")) {
//			data = "";			
//		}
//		else {
//			data += ",";
//			sent += bean.getSent(); //accumulate values
//			rec += bean.getReceived();			
//		}
//		
//		//plus new data: time, sent, received
//		data += String.valueOf(System.currentTimeMillis()/1000) + "," + String.valueOf(curSent);// +"," + String.valueOf(curReceived); not tracking sent yet
//		
//		ContentValues values = new ContentValues();
//		values.put("app_package", app_package);
//		values.put("app_datatransferred", app_datatransferred);
//		values.put("app_datareceived", app_datareceived);
//		values.put("app_throughput", app_throughput);
//		values.put("app_appdata", data);
//		values.put("app_curdatasent", sent);
//		values.put("app_curdatareceived", rec);
//
//		String whereClause = "app_package = '" + app_package + "'";
//		try {
//			// Inserting Row
//			long res = sqlDB.update(DatabaseHandler.TABLE_THROUGHPUT_STATISTICS, values,whereClause, null);
//			if (res == -1) {
//				result = false;
//			} else {
//				result = true;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			sqlDB.close(); 
//		}
		
		return result;
	}


   	/**
	 * Inserts crash statistics into DB and returns the status whether the record 
	 * inserted successfully or not
	 *
	 * @param context the context in which the database operations should be performed
	 * @param app_uid uid of the application
	 * @param app_package application package
	 * @param app_name application name
	 * @param lastcrash_date recent crash date of the application
	 * @param crash_count total crashes count
	 * 
	 * @return boolean, returns the status of record insertion to DB
	 */
	public synchronized boolean uploadCrashStatistics(Context context, int app_uid, String app_package, 
						String app_name, String lastcrash_date, int crash_count) {

		boolean result = false;
		if(context == null || app_uid == -1 || app_package == null || 
				app_name == null || lastcrash_date == null){
			return result;
		}

		DatabaseHandler dbHandler = new DatabaseHandler(context);
		sqlDB = dbHandler.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put("app_uid", app_uid);
		values.put("app_package", app_package);
		values.put("app_name", app_name);
		values.put("lastcrash_date", lastcrash_date);
		values.put("crash_count", crash_count);

	    try{
	    // Inserting Row
	    long res = sqlDB.insert(DatabaseHandler.TABLE_CRASH_STATISTICS, null, values);
		    if(res == -1){
		    	result = false;
//		    	Logger.info("DataMonitorDBWriter --- record not inserted to DB");
		    }else{
		    	result = true;
//		    	Logger.info("DataMonitorDBWriter --- record inserted to DB");
		    }
	    }catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlDB.close(); // Closing database connection
		}
//		Logger.info(TAG + "DataMonitorDBWriter uploadCrashStatistics --- End");
		return result;
	}


   	/**
	 * Updates crash statistics in DB and returns the status whether the record 
	 * updated succesfully or not
	 *
	 * @param context the context in which the database operations should be performed
	 * @param app_uid uid of the application
	 * @param app_package application package name
	 * @param app_name application name 
	 * @param lastcrash_date recent crash date of the application
	 * @param crash_count total crashes count
	 * 
	 * @return boolean, returns the status of record update in DB
	 */
	public synchronized boolean updateCrashStatistics(Context context, int app_uid, String app_package, 
			String app_name, String lastcrash_date, int crash_count) {

		boolean result = false;
		if(context == null || app_uid == -1 || app_package == null 
					|| app_name == null || lastcrash_date == null){
			return result;
		}

		DatabaseHandler dbHandler = new DatabaseHandler(context);
		sqlDB = dbHandler.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put("app_uid", app_uid);
		values.put("app_name", app_name);
		values.put("lastcrash_date", lastcrash_date);
		values.put("crash_count", crash_count);

		String whereClause = "app_package = '" + app_package + "'";
	    try{
	    // Inserting Row
	    long res = sqlDB.update(DatabaseHandler.TABLE_CRASH_STATISTICS, values, whereClause, null);
		    if(res == -1){
		    	result = false;
//		    	Logger.info("DataMonitorDBWriter --- record not updated in DB");
		    }else{
		    	result = true;
//		    	Logger.info("DataMonitorDBWriter --- record updated in DB");
		    }
	    }catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlDB.close(); // Closing database connection
		}
//		Logger.info(TAG + "DataMonitorDBWriter updateCrashStatistics --- End");
		return result;
	}


	
	/**
	 * Deletes all records from DB
	 * 
	 * @param context
	 *            , the context in which the database operations should be
	 *            performed
	 * @return boolean, returns whether all records deleted successfully or not
	 */
	public synchronized boolean deleteAllConnectionFailureRecords(Context context) {
//		Logger.info(TAG + "DataMonitorDBWriter deleteAllRecords --- Begin");
		boolean result = false;

		if (context == null) {
			return result;
		}
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		sqlDB = dbHandler.getWritableDatabase();
		try {// delete Row
			int res = sqlDB.delete(DatabaseHandler.TABLE_CONNECTION_FAILURES,null, null);
			if (res == -1) {
				result = false;
			} else {
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlDB.close(); // Closing database connection
		}
//		Logger.info(TAG + "DataMonitorDBWriter deleteAllRecords --- End");
		return result;
	}

	/**
	 * Deletes all records from SMS statistics table
	 * 
	 * @param context
	 *            , the context in which the database operations should be
	 *            performed
	 * @return boolean, returns whether all records deleted successfully or not
	 */
	public synchronized boolean deleteAllSMSStatisticsRecords(Context context) {
//		Logger.info(TAG
//				+ "DataMonitorDBWriter deleteAllSMSStatisticsRecords --- Begin");
		boolean result = false;

		if (context == null) {
			return result;
		}
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		sqlDB = dbHandler.getWritableDatabase();
		try {// delete Row
			int res = sqlDB.delete(DatabaseHandler.TABLE_SMS_STATISTICS, null,null);
			if (res == -1) {
				result = false;
			} else {
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlDB.close(); // Closing database connection
		}
//		Logger.info(TAG
//				+ "DataMonitorDBWriter deleteAllSMSStatisticsRecords --- End");
		return result;
	}

	/**
	 * Deletes all records from throughput statistics table
	 * 
	 * @param context
	 *            , the context in which the database operations should be
	 *            performed
	 * @return boolean, returns whether all records deleted successfully or not
	 */
	public synchronized boolean deleteAllThroughputStatisticsRecords(Context context) {
//		Logger.info(TAG
//				+ "DataMonitorDBWriter deleteAllThroughputStatisticsRecords --- Begin");
		boolean result = false;

		if (context == null) {
			return result;
		}
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		sqlDB = dbHandler.getWritableDatabase();
		try {// delete Row
			int res = sqlDB.delete(DatabaseHandler.TABLE_THROUGHPUT_STATISTICS,null, null);

			if (res == -1) {
				result = false;
			} else {
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlDB.close(); // Closing database connection
		}
//		Logger.info(TAG
//				+ "DataMonitorDBWriter deleteAllSMSStatisticsRecords --- End");
		return result;
	}

	
   	/**
	 * Deletes all records from crash statistics table
	 *
	 * @param context, the context in which the database operations should be performed
	 * @return boolean, returns whether all records deleted successfully or not
	 */
	public synchronized boolean deleteAllCrashStatisticsRecords(Context context){
//		Logger.info(TAG + "DataMonitorDBWriter deleteAllCrashStatisticsRecords --- Begin");
		boolean result = false;

		if (context == null) {
			return result;
		}
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		sqlDB = dbHandler.getWritableDatabase();
		    try{// delete Row
		    int res = sqlDB.delete(DatabaseHandler.TABLE_CRASH_STATISTICS, null, null);
			    if(res == -1){
			    	result = false;
			    }else{
			    	result = true;
			    }
		    }catch (Exception e) {
				e.printStackTrace();
			}finally{
				sqlDB.close(); // Closing database connection
			}
//		Logger.info(TAG + "DataMonitorDBWriter deleteAllCrashStatisticsRecords --- End");
		return result;
	}	

	public synchronized long insertDataStatisticsRecord(Context context,DataStatsBean bean) {
		// Logger.info(TAG +
		// "DataMonitorDBWriter --- insertDataStatisticsRecord");
		if (context == null || bean.getAppPkgName() == null) {
			return -1;
		}

		DatabaseHandler dbHandler = new DatabaseHandler(context);
		try {
			sqlDB = dbHandler.getWritableDatabase();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			return -1;
		}
		if (sqlDB == null) {
			return -1;
		}
		ContentValues values = new ContentValues();
		values.put(DatabaseHandler.KEY_PACKAGE_NAME, bean.getAppPkgName());
		values.put(DatabaseHandler.KEY_APP_NAME, bean.getAppName());
		values.put(DatabaseHandler.KEY_WIFI_SENT, 0);
		values.put(DatabaseHandler.KEY_WIFI_RECEIVED, 0);
		values.put(DatabaseHandler.KEY_CELLULAR_SENT, 0);
		values.put(DatabaseHandler.KEY_CELLULAR_RECEIVED, 0);
		values.put(DatabaseHandler.KEY_LAST_CHECK_WIFI_SENT, 0);
		values.put(DatabaseHandler.KEY_LAST_CHECK_WIFI_RECEIVED, 0);
		values.put(DatabaseHandler.KEY_LAST_CHECK_CELLULAR_SENT, 0);
		values.put(DatabaseHandler.KEY_LAST_CHECK_CELLULAR_RECEIVED, 0);
		if (bean.getSent() > 0) {
			values.put(DatabaseHandler.KEY_EXISTED_SENT, bean.getSent());
		}
		if (bean.getReceived() > 0) {
			values.put(DatabaseHandler.KEY_EXISTED_RECEIVED,
					bean.getReceived());
		}
		if (values.size() <= 10) {
			sqlDB.close();
			return -1;
		}

		try {
			// Inserting Row
			return sqlDB.insert(DatabaseHandler.TABLE_DATA_STATISTICS, null,values);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			sqlDB.close(); // Closing database connection
		}
	}

	public synchronized long updateDataStatsForPackage(Context context,DataStatsBean bean, boolean bWifi, boolean bReboot) {
		// System.out
		// .println("DataMonitor -- DataMonitorDBWriter --- updateDataStatsForPackage");
		if (context == null || bean.getAppPkgName() == null) {
			return -1;
		}
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		try {
			sqlDB = dbHandler.getWritableDatabase();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		ContentValues values = new ContentValues();
		if (bWifi) {
			if (bean.getWifiSent() > 0) {
				values.put(DatabaseHandler.KEY_WIFI_SENT, bean.getWifiSent());
			}
			if (bean.getWifiReceived() > 0) {
				values.put(DatabaseHandler.KEY_WIFI_RECEIVED,bean.getWifiReceived());
			}
			
		}
		else 
		{
			if (bean.getCellularSent() > 0) {
				values.put(DatabaseHandler.KEY_CELLULAR_SENT,bean.getCellularSent());
			}
			if (bean.getCellularReceived() > 0) {
				values.put(DatabaseHandler.KEY_CELLULAR_RECEIVED,bean.getCellularReceived());
			}
		}
		
		if (bReboot)
		{
			values.put(DatabaseHandler.KEY_EXISTED_SENT,0);
			values.put(DatabaseHandler.KEY_EXISTED_RECEIVED,0);
		}
		else
		{
			values.put(DatabaseHandler.KEY_EXISTED_SENT,bean.getSent());
			values.put(DatabaseHandler.KEY_EXISTED_RECEIVED,bean.getReceived());
		}
		if (values.size() <= 0) {
			sqlDB.close();
			return -1;
		}
		String whereClause = DatabaseHandler.KEY_PACKAGE_NAME + "=?";
		try {
			// Updating Row
			return sqlDB.update(DatabaseHandler.TABLE_DATA_STATISTICS, values,whereClause, new String[] { bean.getAppPkgName() });
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			sqlDB.close(); // Closing database connection
		}
	}

//	public synchronized void updateDataStatsBeforeShutdown(Context context) {
//		// System.out
//		// .println("DataMonitor -- DataMonitorDBWriter --- updateDataStatsBeforeShutdown");
//		if (context == null) {
//			return;
//		}
//		DatabaseHandler dbHandler = new DatabaseHandler(context);
//		try {
//			sqlDB = dbHandler.getWritableDatabase();
//		} catch (SQLException sqle) {
//			sqle.printStackTrace();
//		}
//		String sql = "UPDATE " + DatabaseHandler.TABLE_DATA_STATISTICS
//				+ " SET " + DatabaseHandler.KEY_LAST_BOOT_WIFI_SENT + "="
//				+ DatabaseHandler.KEY_WIFI_SENT + "+"
//				+ DatabaseHandler.KEY_LAST_BOOT_WIFI_SENT + ","
//				+ DatabaseHandler.KEY_LAST_BOOT_WIFI_RECEIVED + "="
//				+ DatabaseHandler.KEY_WIFI_RECEIVED + "+"
//				+ DatabaseHandler.KEY_LAST_BOOT_WIFI_RECEIVED + ","
//				+ DatabaseHandler.KEY_LAST_BOOT_CELLULAR_SENT + "="
//				+ DatabaseHandler.KEY_CELLULAR_SENT + "+"
//				+ DatabaseHandler.KEY_LAST_BOOT_CELLULAR_SENT + ","
//				+ DatabaseHandler.KEY_LAST_BOOT_CELLULAR_RECEIVED + "="
//				+ DatabaseHandler.KEY_CELLULAR_RECEIVED + "+"
//				+ DatabaseHandler.KEY_LAST_BOOT_CELLULAR_RECEIVED + ","
//				+ DatabaseHandler.KEY_WIFI_SENT + "=0,"
//				+ DatabaseHandler.KEY_WIFI_RECEIVED + "=0,"
//				+ DatabaseHandler.KEY_CELLULAR_SENT + "=0,"
//				+ DatabaseHandler.KEY_CELLULAR_RECEIVED + "=0;";
//		
//		Cursor cursor = sqlDB.rawQuery(sql, null);
//		if (cursor != null && cursor.getCount() > 0) {
////			Logger.info("Updation of Datastats is successfully completed: "
////					+ cursor.getCount());
//		}
//		sqlDB.close();
//	}

	public synchronized long insertRunningAppsRecord(Context context, String packageName,long time, int interval) {
		if (context == null) {
			return -1;
		}
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		try {
			sqlDB = dbHandler.getWritableDatabase();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		if (sqlDB == null) {
			return -1;
		}
		ContentValues values = new ContentValues();
		values.put("package_name", packageName);
		values.put("started_time", time);
		values.put("current_time", time);
		values.put("total_time", interval);
		try {
			// Inserting Row
			return sqlDB.insert(DatabaseHandler.TABLE_RUNNING_APP_DURATION_STATISTICS,null, values);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			sqlDB.close(); // Closing database connection
		}
	}

	public synchronized long updateRunningAppsRecord(Context context, String packageName, long duration, int interval) {
		if (context == null || interval == 0) {
			return -1;
		}
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		try {
			sqlDB = dbHandler.getWritableDatabase();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		if (sqlDB == null) {
			return -1;
		}
		String updateSql = "update " + DatabaseHandler.TABLE_RUNNING_APP_DURATION_STATISTICS + " set total_time = total_time + ?, current_time = ? where package_name=?";
		//ContentValues values = new ContentValues();
		//values.put("current_time", duration);
		//String where = "package_name=?";
		try {
			sqlDB.execSQL(updateSql, new String[] { Integer.toString(interval), Long.toString(duration), packageName });
			return 0;
			// Inserting Row
			//return sqlDB.update(DatabaseHandler.TABLE_RUNNING_APP_DURATION_STATISTICS,values, where, new String[] { packageName });
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			sqlDB.close(); // Closing database connection
		}
	}

	public synchronized long deleteUserDeletedAppFromDB(Context context, String appPackageName) {
		if (context == null) {
			return -1;
		}
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		try {
			sqlDB = dbHandler.getWritableDatabase();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		if (sqlDB == null) {
			return -1;
		}		
		try {
			return sqlDB.delete(DatabaseHandler.TABLE_RUNNING_APP_DURATION_STATISTICS,"package_name=?", new String[]{appPackageName});			
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			sqlDB.close(); // Closing database connection
		}
	}
	
	public synchronized long deleteAppsFromDB(Context context) {
		if (context == null) {
			return -1;
		}
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		try {
			sqlDB = dbHandler.getWritableDatabase();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		if (sqlDB == null) {
			return -1;
		}		
		try {
			//reset the app data usage
			String updateCmd = "update " + DatabaseHandler.TABLE_DATA_STATISTICS + " set "
					+ DatabaseHandler.KEY_LAST_CHECK_WIFI_SENT + "=" + DatabaseHandler.KEY_WIFI_SENT + ","
					+ DatabaseHandler.KEY_LAST_CHECK_WIFI_RECEIVED + "=" + DatabaseHandler.KEY_WIFI_RECEIVED + ","
					+ DatabaseHandler.KEY_LAST_CHECK_CELLULAR_SENT + "=" + DatabaseHandler.KEY_CELLULAR_SENT + ","
					+ DatabaseHandler.KEY_LAST_CHECK_CELLULAR_RECEIVED + "=" + DatabaseHandler.KEY_CELLULAR_RECEIVED + "";
					
			sqlDB.execSQL(updateCmd);
			// clear the app runtime counts
			return sqlDB.delete(DatabaseHandler.TABLE_RUNNING_APP_DURATION_STATISTICS,null,null);	
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			try {
				sqlDB.close(); // Closing database connection
			}catch (Exception e) {}
		}
	}

	public synchronized long updateTotalTimeForApps(Context context,
			String packageName, long totalTime) {
		if (context == null) {
			return -1;
		}
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		try {
			sqlDB = dbHandler.getWritableDatabase();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		if (sqlDB == null) {
			return -1;
		}
		ContentValues values = new ContentValues();
		values.put("current_time", 0);
		values.put("started_time", 0);
		values.put("total_time", totalTime);
		String where = "package_name=?";
		try {
			// Inserting Row
			return sqlDB.update(DatabaseHandler.TABLE_RUNNING_APP_DURATION_STATISTICS,values, where, new String[] { packageName });
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			sqlDB.close(); // Closing database connection
		}
		
	}
	
	public synchronized long updateExistedAppWithTheLatestRunningTime(Context context, String packageName, long currentTime, long startTime, int interval) {
		if (context == null) {
			return -1;
		}
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		try {
			sqlDB = dbHandler.getWritableDatabase();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		if (sqlDB == null) {
			return -1;
		}
		ContentValues values = new ContentValues();
		values.put("total_time", interval);
		values.put("started_time", startTime);
		String where = "package_name=?";
		try {
			// Inserting Row
			return sqlDB.update(DatabaseHandler.TABLE_RUNNING_APP_DURATION_STATISTICS,values, where, new String[] { packageName });
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			sqlDB.close(); // Closing database connection
		}
	}
		
	public synchronized boolean uploadStatsString(Context context, String stats) {
		boolean result = false;
		if (context == null || stats.indexOf("com.datamonitor.util.StatsManager") == 0) {
			return result;
		}

		try {
			DatabaseHandler dbHandler = new DatabaseHandler(context);
			sqlDB = dbHandler.getWritableDatabase();
	
			ContentValues values = new ContentValues();
			values.put("stats_string", stats); 

			// Inserting Row
			long res = sqlDB.insert(DatabaseHandler.TABLE_STATS_STRING, null, values);
			if (res == -1) {
				result = false;
//				Logger.info("DataMonitorDBWriter --- record not inserted to DB");
			} else {
				result = true;
//				Logger.info("DataMonitorDBWriter --- record inserted to DB");
			}
		} catch (Exception e) {
			MMCLogger.logToFile(MMCLogger.Level.ERROR, "DataMonitorWriter", "constructor", "invalid uri", e);
		} finally {
			if (sqlDB != null)
				sqlDB.close(); // Closing database connection
		}
//		Logger.info(TAG
//				+ "DataMonitorDBWriter uploadStatsString --- End");
		return result;
	}
	
	public synchronized boolean delete15MinStatsBucket(Context context) {
		boolean result = false;
		int res = 0, count = 0;
//		String orderBy = "stats_id DESC";

		if (context == null) {
			return result;
		}
		try{
		DatabaseHandler dbHandler = new DatabaseHandler(context);
		sqlDB = dbHandler.getWritableDatabase();
		}catch (Exception e)
		{
			return false;
		}
		
		Cursor cursor = sqlDB.query(DatabaseHandler.TABLE_STATS_STRING, null, null,null, null, null, null);
		
		try {
//			do {	
				if (cursor != null) {
					if (cursor.moveToFirst()) {
//						String rowId = cursor.getString(0);//cursor.getColumnIndex("stats_id"));
//						res = sqlDB.delete(DatabaseHandler.TABLE_STATS_STRING, "stats_id=?", new String[]{rowId});
						res = sqlDB.delete(DatabaseHandler.TABLE_STATS_STRING, null, null);
//						count++;
//					}
				}
			}
//			while (count < 12 && cursor.moveToNext());
			cursor.close();
			if (res == -1) {
				result = false;
			} else {
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlDB.close(); 
		}
		return result;
	}

	public synchronized boolean delete15MinStatsBucketForSMS(Context context) {
		boolean result = false;
		int res = 0;
		if (context == null) {
			return result;
		}
		try
		{
			DatabaseHandler dbHandler = new DatabaseHandler(context);
			sqlDB = dbHandler.getWritableDatabase();
		}catch (Exception e)
		{
			return false;
		}
		Cursor cursor = sqlDB.query(DatabaseHandler.TABLE_SMS_STATISTICS, null, null,null, null, null, null);
		
		try {
				if (cursor != null) {
					if (cursor.moveToFirst()) {
						res = sqlDB.delete(DatabaseHandler.TABLE_SMS_STATISTICS, null, null);
				}
			}
			cursor.close();
			if (res == -1) {
				result = false;
			} else {
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlDB.close(); 
		}
		return result;
	}
}
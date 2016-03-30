package com.cortxt.app.mmcutility.DataObjects.database;

import java.io.File;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;


/**
 * Database helper class used to create all the tables used by the application.
 * 
 * @author Paradigm
 * 
 */
public class DatabaseHandler extends SQLiteOpenHelper {

	public static final String DATABASE_FILE_PATH = Environment.getExternalStorageDirectory().getPath();
	public static final String DATABASE_NAME = "MMCDataMonitor.db";
	public static final int DATABASE_VERSION = 3;

	public static final String TABLE_CONNECTION_FAILURES = "connectionFailures";
	public static final String TABLE_SMS_STATISTICS = "smsStatistics";
	public static final String TABLE_THROUGHPUT_STATISTICS = "throughputStatistics";
	public static final String TABLE_DATA_STATISTICS = "datastatistics";
	public static final String TABLE_CRASH_STATISTICS = "crashstatistics";
	public static final String TABLE_RUNNING_APP_DURATION_STATISTICS = "runningappsduration";
	public static final String TABLE_STATS_STRING = "statsString";
	public static final String TABLE_MANUAL_POLYGON = "manualPolygon";
	public static final String TABLE_TRANSIT_AREAS = "transitAreas";
	public static final String TABLE_TRANSIT_TRANSPORT = "transitRoutes";
	public static final String TABLE_TRANSIT_STATIONS = "routeStops";
	public static final String TABLE_TRANSIT_ITINERARIES = "transitItineraries";
	public static final String TABLE_TRANSIT_CITIES = "transitCities";
	public static final String TABLE_TRANSIT_POLYLINE = "transitShapes";
	
	public static final String KEY_PACKAGE_NAME = "package_name";
	public static final String KEY_APP_NAME = "app_name";
	public static final String KEY_WIFI_SENT = "wifi_sent";
	public static final String KEY_WIFI_RECEIVED = "wifi_received";
	public static final String KEY_CELLULAR_SENT = "cellular_sent";
	public static final String KEY_CELLULAR_RECEIVED = "cellular_received";
	public static final String KEY_LAST_CHECK_WIFI_SENT = "last_boot_wifi_sent";
	public static final String KEY_LAST_CHECK_WIFI_RECEIVED = "last_boot_wifi_received";
	public static final String KEY_LAST_CHECK_CELLULAR_SENT = "last_boot_cellular_sent";
	public static final String KEY_LAST_CHECK_CELLULAR_RECEIVED = "last_boot_cellular_received";
	public static final String KEY_EXISTED_SENT = "existed_sent";
	public static final String KEY_EXISTED_RECEIVED = "existed_received";
	public static final String KEY_TIMESTAMP = "timestamp";

	private static final String CREATE_DATAMONITOR_CONNECTIONFAILURES_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_CONNECTION_FAILURES
			+ " ("
			+ "cf_id integer primary key autoincrement,"
			+ "connection_type integer," + "cf_time text" + " );";
	
	private static final String CREATE_DATAMONITOR_SMSSTATISTICS_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_SMS_STATISTICS
			+ " ("
			+ "smsstats_id integer primary key autoincrement,"
			+ "sms_id integer,"
			+ "sms_type integer,"
			+ "sms_address text,"
			+ "sms_date text" + " );";

	private static final String CREATE_DATAMONITOR_DATASTATISTICS_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_DATA_STATISTICS
			+ " ("
			+ KEY_PACKAGE_NAME+" varchar,"
			+ KEY_APP_NAME+" varchar,"
			+ KEY_WIFI_SENT+" real,"
			+ KEY_WIFI_RECEIVED+" real,"
			+ KEY_CELLULAR_SENT+" real,"
			+ KEY_CELLULAR_RECEIVED+" real,"
			+ KEY_LAST_CHECK_WIFI_SENT+" real,"
			+ KEY_LAST_CHECK_WIFI_RECEIVED+" real,"
			+ KEY_LAST_CHECK_CELLULAR_SENT+" real,"
			+ KEY_LAST_CHECK_CELLULAR_RECEIVED+" real,"
			+ KEY_EXISTED_SENT+" real," 
			+ KEY_EXISTED_RECEIVED+" real" 
			+ ");";

	private static final String CREATE_DATAMONITOR_THROUGHPUT_TABLE = "CREATE TABLE IF NOT EXISTS " +
			TABLE_THROUGHPUT_STATISTICS + " (" +
			"tp_id integer primary key autoincrement,"+
			"app_uid integer,"+
			"app_package text," +
			"app_datatransferred double," +
			"app_datareceived double," +
			"app_throughput double,"+
			"app_appdata text,"+
			"app_curdatasent double,"+
			"app_curdatareceived double"+
			" );";

	private static final String CREATE_DATAMONITOR_CRASH_TABLE = "CREATE TABLE IF NOT EXISTS " +
			TABLE_CRASH_STATISTICS + " (" +
			"crash_id integer primary key autoincrement,"+
			"app_uid integer,"+
			"app_package text," +
			"app_name text," +
			"lastcrash_date text," +
			"crash_count integer" +
			" );";

	private static final String CREATE_DATAMONITOR_APP_RUNNING_DURATION_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_RUNNING_APP_DURATION_STATISTICS
			+ "(package_name varchar,"
			+ "started_time long,"
			+ "current_time long,"
			+ "total_time long);";
	
	private static final String CREATE_DATAMONITOR_STATS_STRING_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_STATS_STRING
			+ " ("
			+ "stats_id integer primary key autoincrement,"
			+ "stats_string text" 
			+ ");";
	
	private static final String CREATE_MANUAL_POLYGON_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_MANUAL_POLYGON
			+ " ("
			+ "points varchar"
			+ ");";

	private static final String CREATE_TABLE_TRANSIT_AREAS = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_TRANSIT_AREAS
			+ "("
			+ "name varchar,"
			+ "area_id integer,"
//			+ "download_flag integer,"
			+ "timestamp long"
			+ ");"; 
	
	private static final String CREATE_TABLE_TRANSIT_CITIES = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_TRANSIT_CITIES
			+ "("
			+ "city_name varchar,"
			+ "area_id integer,"
			+ "city_id integer"
			+ ");"; 
	
	private static final String CREATE_TRANSIT_TRANSPORT_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_TRANSIT_TRANSPORT
			+ "("
			+ "short_name varchar,"
			+ "long_name varchar,"
			+ "agency_id varchar,"
			+ "area_id integer,"
			+ "transport_id integer,"
			+ "type integer,"
			+ "count integer"
			+ ");";
	
	private static final String CREATE_TRANSIT_STATIONS_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_TRANSIT_STATIONS
			+ "("
			+ "transport_id integer,"
			+ "stop_id varchar,"
			+ "station_id integer,"
			+ "name varchar,"
			+ "area_id integer,"
			+ "latitude integer,"
			+ "longitude integer,"
			+ "stop_sequence integer,"
			+ "distance varchar,"
			+ "duration integer,"
			+ "station_line varchar,"
			+ "intersect_latitude integer,"
			+ "intersect_longitude integer"
			+ ");";
	
	private static final String CREATE_TRANSIT_ITINERARIES_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_TRANSIT_ITINERARIES
			+ "("
			+ "arrival_id varchar,"
			+ "depart_id varchar,"
			+ "itinerary_id varchar,"
			+ "transport_id integer"
			+ ");";
	
	private static final String CREATE_TRANSIT_POLYLINE_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_TRANSIT_POLYLINE
			+ "("
			+ "area_id integer,"
			+ "latitude integer,"
			+ "longitude integer,"
			+ "has_stations varchar,"
			+ "transport_id integer,"
			+ "download_flag integer"
			+ ");";
	
	private static final String TAG = "DatabaseHelper";

	/**
	 * Constructor of the class DatabaseHandler, which extends from
	 * SQLiteOpenHelper
	 * 
	 * @param context
	 *            the context in which DB operations can be performed
	 */
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		//super(context, DATABASE_FILE_PATH + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * Overrides inherited function onCreate
	 * 
	 * @param db
	 *            the instance of SQLiteDatabase
	 */
	public void onCreate(SQLiteDatabase db) {
//		Logger.info(TAG + "onCreate --- Begin");
		db.execSQL(CREATE_DATAMONITOR_CONNECTIONFAILURES_TABLE);
		db.execSQL(CREATE_DATAMONITOR_SMSSTATISTICS_TABLE);
		db.execSQL(CREATE_DATAMONITOR_THROUGHPUT_TABLE);
 		db.execSQL(CREATE_DATAMONITOR_DATASTATISTICS_TABLE);
		db.execSQL(CREATE_DATAMONITOR_CRASH_TABLE);
		db.execSQL(CREATE_DATAMONITOR_APP_RUNNING_DURATION_TABLE);
		db.execSQL(CREATE_DATAMONITOR_STATS_STRING_TABLE);
		db.execSQL(CREATE_MANUAL_POLYGON_TABLE);
		db.execSQL(CREATE_TABLE_TRANSIT_AREAS);
		db.execSQL(CREATE_TABLE_TRANSIT_CITIES);
		db.execSQL(CREATE_TRANSIT_TRANSPORT_TABLE);
		db.execSQL(CREATE_TRANSIT_STATIONS_TABLE);
		db.execSQL(CREATE_TRANSIT_ITINERARIES_TABLE);
		db.execSQL(CREATE_TRANSIT_POLYLINE_TABLE);
//		Logger.info(TAG + "Database created...");
//		Logger.info(TAG + "onCreate --- End");
	}

	/**
	 * Overrides inherited function onUpgrade
	 * 
	 * @param db
	 *            the instance of SQLiteDatabase
	 * @param oldVersion
	 *            old version of the database
	 * @param newVersion
	 *            new version of the database
	 */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try{
		db.execSQL("drop table if exists" + TABLE_CONNECTION_FAILURES + ";");
		db.execSQL("drop table if exists" + TABLE_SMS_STATISTICS + ";");
		db.execSQL("drop table if exists" + TABLE_DATA_STATISTICS + ";");
		db.execSQL("drop table if exists" + TABLE_THROUGHPUT_STATISTICS + ";");
		db.execSQL("drop table if exists" + TABLE_CRASH_STATISTICS + ";");
		db.execSQL("drop table if exists" + TABLE_RUNNING_APP_DURATION_STATISTICS + ";");
		db.execSQL("drop table if exists" + TABLE_STATS_STRING + ";");
		db.execSQL("drop table if exists" + TABLE_MANUAL_POLYGON + ";");
		db.execSQL("drop table if exists" + TABLE_TRANSIT_CITIES + ";");
		db.execSQL("drop table if exists" + TABLE_TRANSIT_AREAS + ";");
		db.execSQL("drop table if exists" + TABLE_TRANSIT_TRANSPORT + ";");
		db.execSQL("drop table if exists" + TABLE_TRANSIT_STATIONS + ";");
		db.execSQL("drop table if exists" + TABLE_TRANSIT_ITINERARIES + ";");
		db.execSQL("drop table if exists" + TABLE_TRANSIT_POLYLINE + ";");
		
		}catch(SQLException e){
			e.printStackTrace();
//			Logger.info("Exception occurred while dropping the tables"+e.getLocalizedMessage());
		}
		onCreate(db);
	}
}
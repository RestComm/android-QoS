package org.restcomm.app.utillib.ContentProvider;


import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.telephony.TelephonyManager;

import org.restcomm.app.utillib.Utils.DeviceInfoOld;
import org.restcomm.app.utillib.Utils.GpsListener;
import org.restcomm.app.utillib.Utils.LoggerUtil;

/**
 * This is the content provider that will be used by everything in the project
 * that has to do CRUD operations to the SQLite database.
 * @author Abhin
 *
 */
public class Provider {
	/*
	 * ============================================================
	 * Start private variables
	 */

	private static final String TAG = Provider.class.getSimpleName();

	private static final String DATABASE_NAME = null; // "mmc.db";
	private static final int DATABASE_VERSION = 9;

	private int phoneType = TelephonyManager.PHONE_TYPE_NONE;
	private DatabaseHelper databaseHelper; 
	private static final UriMatcher uriMatcher;

	/*
	 * End private variables
	 * ============================================================
	 * Start overriden methods
	 */

	public Provider(Context context) {
		phoneType = DeviceInfoOld.getPhoneType(context);
		databaseHelper = new DatabaseHelper(context);
	}
	
	public String getType(Uri uri) {
		UriMatch match = UriMatch.get(uriMatcher.match(uri));
		switch (match){
		//first check if the match is for a single row
//		case BASE_STATION_CDMA_ID:
//		case BASE_STATION_GSM_ID:
//		case EVENT_COUPLE_ID:
//		case EVENT_ID:
//		case LOCATION_ID:
//		case SIGNAL_STRENGTH_ID:
//			return match.Table.ContentItemType;
//
//			//now check for the multiple row case
//		case BASE_STATIONS_CDMA:
//		case BASE_STATIONS_GSM:
//		case SIGNAL_STRENGTHS:
//		case EVENT_COUPLES:
//		case EVENTS:
//		case LOCATIONS:
//			return match.Table.ContentType;
		
			//first check if the match is for a single row
		case BASE_STATION_ID:
		//case EVENT_COUPLE_ID:
		//case EVENT_ID:
		case LOCATION_ID:
		case SIGNAL_STRENGTH_ID:
			return match.Table.ContentItemType;

			//now check for the multiple row case
		case BASE_STATIONS:
		case SIGNAL_STRENGTHS:
		//case EVENT_COUPLES:
		//case EVENTS:
		case LOCATIONS:
			return match.Table.ContentType; 

			//and for defaulting, throw an exception
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	/**
	 * This method is automatically called when an "insert" is attempted. 
	 * This method first confirms that uri is valid.
	 * Then this method checks whether the ContentValues variables has
	 * all the required values for the insert.
	 * Then this method does the actual insert and if it fails, then it 
	 * throws a SQLException.
	 * @throws SQLException Thrown if the insert fails
	 */
	public void insert(Uri uri, ContentValues initialValues) {
		//validate the uri
		int uriMatchCode = uriMatcher.match(uri);
		if (uriMatchCode == UriMatcher.NO_MATCH){
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null){
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}


		// Make sure that the fields are all set
		UriMatch uriMatch = UriMatch.get(uriMatchCode);

		try {
			SQLiteDatabase db = databaseHelper.getWritableDatabase();
			long rowId = db.insert(uriMatch.Table.Name, null, values);
			if (rowId > 0){
	//			Uri rowUri = ContentUris.withAppendedId(uriMatch.getContentUri(), rowId);
	//			getContext().getContentResolver().notifyChange(rowUri, null);
	//			return rowUri;
				return;
			}
		}
		catch (Exception e){
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "Exception inserting row into ", uri.toString(), e);
		}

		LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "Failed to insert row into ", uri.toString() + " value: " + values.toString());
		//throw new SQLException("Failed to insert row into " + uri);
	}

	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		UriMatch match = UriMatch.get(uriMatcher.match(uri));

		qb.setProjectionMap(null);	//this should let all the columns through TODO change this later if necessary
		qb.setTables(match.Table.Name);
		//TODO implement the fallback default order later

		// Get the database and run the query
		SQLiteDatabase db = databaseHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		return c;
	}

	public int delete (Uri uri, String selection, String[] selectionArgs) {
		
		//validate the uri
		int uriMatchCode = uriMatcher.match(uri);
		if (uriMatchCode == UriMatcher.NO_MATCH){
			//throw new IllegalArgumentException("Unknown URI " + uri);
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "Unknown URI", "uri");
			return -1;
		}

		// Make sure that the fields are all set
		UriMatch uriMatch = UriMatch.get(uriMatchCode);

		try{
			SQLiteDatabase db = databaseHelper.getWritableDatabase();
			int count = db.delete(uriMatch.Table.Name, selection, selectionArgs);
			return count;
		}
		catch (Exception e)
		{
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "Exception deleting ", uri.toString(), e);
		}
		return -1;
	}

	/*
	 * get distance travelled in last number seconds
	 * 
	 */
	public double getDistanceTravelled(int seconds) {
		double lat = 0.0, lng = 0.0, lat0 = 0.0, lng0 = 0.0, lat1 = 0.0, lng1 = 0.0;
		Long tStart = System.currentTimeMillis() - seconds*1000;
		String startTime = tStart.toString(); //  df.format (new Date(tStart));
		
		//Sql to find all event couples for a the given type
		String sqlLocations = "select latitude, longitude from locations" +
				" where accuracy <= " +  String.valueOf(GpsListener.LOCATION_UPDATE_MIN_TREND_ACCURACY) +
				" and timestamp > " + startTime + " and timestamp < " + System.currentTimeMillis() + " order by timestamp asc";
		// Get the database and run the query
		SQLiteDatabase db = databaseHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(sqlLocations, null);
		if (cursor != null)
		{
			try 
			{
				int latitudeIndex = cursor.getColumnIndexOrThrow(Tables.Locations.LATITUDE);
				int longitudeIndex = cursor.getColumnIndexOrThrow(Tables.Locations.LONGITUDE);
				
				if (cursor.moveToFirst())
				{
					// oldest location in n seconds
					lat1 = cursor.getDouble(latitudeIndex);
					lng1 = cursor.getDouble(longitudeIndex);
				
					cursor.moveToLast();
					// newest location in n seconds
					lat0 = cursor.getDouble(latitudeIndex);
					lng0 = cursor.getDouble(longitudeIndex);
	
					double earthRadius = 6371000.0;
					lat = (lat1 - lat0);
					lng = (lng1 - lng0);
					// distance vector in meters
					double dX = lng * (Math.PI * earthRadius*Math.cos(lat1*Math.PI/180.0)) / 180.0;
					double dY = lat * (Math.PI * earthRadius) / 180.0;
					double dis = Math.sqrt(dX*dX+dY*dY);
					// MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "getDistanceTravelled", "distance travelled in " + seconds + ": " + dis + " lat0=" + lat0 + ",lat1=" + lat1 + ",lng0=" + lng0 + ",lng1=" + lng1);
					return dis;
				}
			} catch (Exception e) {
				LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "getDistanceTravelled", "Exception", e);
			}
			finally
			{
				cursor.close ();
			}
		}
		return 0.0;
	}

	/**
	 * Deletes records from the temporary db (signals, locations, cells) that are 4 hours old
	 *
	 */
	public void pruneDB() {
		SQLiteDatabase db = databaseHelper.getReadableDatabase();

		try {
			int numRows = -1;
			//delete locations and signals from 4 hours ago
			long timeLimit = System.currentTimeMillis() - 4L*3600L*1000L;
			numRows = db.delete(TablesEnum.LOCATIONS.Name, Tables.Locations.TIMESTAMP + "< ?", new String[]{String.valueOf(timeLimit)});
			numRows = db.delete(TablesEnum.SIGNAL_STRENGTHS.Name, Tables.SignalStrengths.TIMESTAMP + "< ?", new String[]{String.valueOf(timeLimit)});
			numRows = db.delete(TablesEnum.BASE_STATIONS.Name,  Tables.BaseStations.TIMESTAMP + "< ?", new String[]{String.valueOf(timeLimit)});
			}catch(SQLiteException e) {
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "pruneDB", "Prunning the db failed", e);
		}

	}
	
	/*
	 * End overriden methods
	 * ====================================================================
	 * Start public methods
	 */

	private class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			
			//create the signal strength table
			db.execSQL(String.format(
					"CREATE TABLE %s " +
							"(%s INTEGER PRIMARY KEY, " +
							"%s INTEGER, " +
							"%s INTEGER, " +
							"%s INTEGER, " +
							"%s INTEGER, " +
							"%s INTEGER, " +
							"%s INTEGER, " +
							"%s INTEGER, " +
							"%s INTEGER, " +
							"%s INTEGER, " +
							"%s INTEGER, " +
							"%s INTEGER, " +
							"%s INTEGER, " +
							"%s INTEGER, " +
							"%s INTEGER, " +
							"%s INTEGER, " +
							"%s INTEGER, " +
							"%s INTEGER);",

							TablesEnum.SIGNAL_STRENGTHS.Name,
							Tables.SignalStrengths._ID,
							Tables.SignalStrengths.TIMESTAMP,
							Tables.SignalStrengths.SIGNAL,
							Tables.SignalStrengths.ECI0,
							Tables.SignalStrengths.SNR,
							Tables.SignalStrengths.BER,
							Tables.SignalStrengths.RSCP,
							Tables.SignalStrengths.SIGNAL2G,
							Tables.SignalStrengths.LTE_SIGNAL,
							Tables.SignalStrengths.LTE_RSRP,
							Tables.SignalStrengths.LTE_RSRQ,
							Tables.SignalStrengths.LTE_SNR,
							Tables.SignalStrengths.LTE_CQI,
							Tables.SignalStrengths.SIGNALBARS,
							Tables.SignalStrengths.ECN0,
							Tables.SignalStrengths.WIFISIGNAL,
							Tables.SignalStrengths.COVERAGE,
							Tables.SignalStrengths.EVENT_ID
					));

				//create the base station table
				String strSQL = String.format(
						"CREATE TABLE %s " +
								"(%s INTEGER PRIMARY KEY, " +
								"%s INTEGER, " +
								"%s STRING, " +
								"%s INTEGER, " +
								"%s INTEGER, " +
								"%s INTEGER, " +
								"%s INTEGER, " +
								"%s INTEGER, " +
								"%s INTEGER, " +
								"%s INTEGER);",

								TablesEnum.BASE_STATIONS.Name,
								Tables.BaseStations._ID,
								Tables.BaseStations.TIMESTAMP,
								Tables.BaseStations.NET_TYPE,
								Tables.BaseStations.BS_LOW,
								Tables.BaseStations.BS_MID,
								Tables.BaseStations.BS_HIGH,
								Tables.BaseStations.BS_CODE,
								Tables.BaseStations.BS_CHAN,
								Tables.BaseStations.BS_BAND,
								Tables.BaseStations.EVENT_ID
						);
				db.execSQL(strSQL);

			//create the locations table
			db.execSQL(String.format(
					"CREATE TABLE %s " +
							"(%s INTEGER PRIMARY KEY, " +
							"%s INTEGER, " +
							"%s REAL, " +
							"%s REAL, " +
							"%s REAL, " +
							"%s REAL, " +
							"%s REAL, " +
							"%s REAL, " +
							"%s INTEGER, " +
							"%s TEXT, " +
							"%s INTEGER);",

							TablesEnum.LOCATIONS.Name,
							Tables.Locations._ID,
							Tables.Locations.TIMESTAMP,
							Tables.Locations.ALTITUDE,
							Tables.Locations.ACCURACY,
							Tables.Locations.BEARING,
							Tables.Locations.LATITUDE,
							Tables.Locations.LONGITUDE,
							Tables.Locations.SPEED,
							Tables.Locations.SATELLITES,
							Tables.Locations.PROVIDER,
							Tables.Locations.EVENT_ID
					));
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			/*
			 * Till the alpha is released, we would just use "drop table" as the upgrade
			 * procedure. After the alpha of course, proper upgrade procedures would have to be used.
			 */
			if (newVersion > oldVersion){
				//drop all the tables if they exist
				for (TablesEnum table : TablesEnum.values())
					db.execSQL("DROP TABLE IF EXISTS " + table.Name);

				//now call onCreate
				onCreate(db);
			}
		}

	}

	/*
	 * End private helper classes / objects
	 * =======================================================================
	 * Start static area
	 */

	static {
		//Initialise the uri matcher
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		for (UriMatch match : UriMatch.values()){
			uriMatcher.addURI(Tables.AUTHORITY, match.Path, match.Code);
		}
	}

	/*
	 * End static area
	 * =======================================================================
	 */

}

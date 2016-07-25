package com.cortxt.app.utillib.Reporters.WebReporter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Pair;

import com.cortxt.app.utillib.DataObjects.Carrier;
import com.cortxt.app.utillib.DataObjects.DeviceInfo;
import com.cortxt.app.utillib.DataObjects.GSMDevice;
import com.cortxt.app.utillib.Utils.Global;
import com.cortxt.app.utillib.Utils.LibException;
import com.cortxt.app.utillib.Utils.LoggerUtil;
import com.cortxt.app.utillib.Utils.PreferenceKeys;
import com.google.gson.Gson;
import com.securepreferences.SecurePreferences;

/**
 * WebReporter is in charge of reporting events to a web server 
 * @author estebanginez
 *
 */
public class WebReporter  {

	public static final String TAG = WebReporter.class.getSimpleName();
	public static final String EVENTS_QUEUE_KEY_PREFERENCE = "EVENTS_QUEUE_KEY_PREFERENCE";
	private static final String PREFERENCE_KEY_ENABLE_DATA_ROAMING = "KEY_SETTINGS_ENABLE_DATA_ROAMING";
	
	public static final String JSON_API_KEY= "apiKey";
	public static final String USER_ID_KEY= "userID";
	private static final String JSON_ERROR_KEY= "errors";
	private static final String JSON_NETWORK_KEY = "operator";
	private static final String JSON_NETWORKS_KEY = "networks";
	private static final String JSON_NETWORK_ID_KEY = "_id";
	private static final String JSON_NETWORK_LOGOPATH_KEY = "path";
	private static final String JSON_NETWORK_TWITTERHANDLE_KEY = "twitter";
	
	protected Context mContext;
	//protected HttpClient mHttpClient;
	protected String mHost;
	protected static String mStaticAssetURL;
	
	/**
	 * It is used to make sure the queue of events gets sent, by keeping the cpu awake
	 */
	private WakeLock mFlushQueueWakeLock;

	protected ConcurrentLinkedQueue<Request> mRequestQueue;
	/**
	 * The api key that the server requires for each request that
	 * gets sent to the server
	 */
	private String mApiKey;

	public WebReporter(Context context) {
		mContext = context;

		//Retrieve api key if it exists
		mApiKey = Global.getApiKey(mContext);
		//Retrieve the host from the strings.xml

		mHost = Global.getApiUrl(context);
		mStaticAssetURL = Global.getString(context, "MMC_STATIC_ASSET_URL");

		mRequestQueue = new ConcurrentLinkedQueue<Request>();
		
		mFlushQueueWakeLock = ((PowerManager)mContext.getSystemService(Context.POWER_SERVICE))
				.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mFlushQueueWakeLock.setReferenceCounted(false);
	}

	/**
	 * Cleans up any used resources. Saves all events that have not been sent
	 */
	public void stop() {
		saveEvents();
	}

	/**
	 * Persists the queue of events to the phone's preferences
	 */
	protected void saveEvents(){
		JSONArray jsonQueue= new JSONArray();
		for(Request request: mRequestQueue){
			try {
				jsonQueue.put(request.serialize());
			} catch (Exception e) {
				LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "persistQueue", "failed to persist event request", e);
			}
		}

		SharedPreferences preferenceSettings = PreferenceManager.getDefaultSharedPreferences(mContext);
		String stringQueue = jsonQueue.toString();

		LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "saveEvents", stringQueue);

		preferenceSettings.edit().putString(EVENTS_QUEUE_KEY_PREFERENCE, jsonQueue.toString()).commit();
	}

	
	/**
	 * Sends the registration request, receives the request and parses the response back
	 */
	public void authorizeDevice(DeviceInfo device, String email, String password, boolean bFailover)  throws LibException {
		String host = mHost;
		{
			SharedPreferences preferenceSettings = PreferenceKeys.getSecurePreferences(mContext);
			preferenceSettings.edit().putString(PreferenceKeys.User.USER_EMAIL, email).commit();
			preferenceSettings.edit().putString(PreferenceKeys.Miscellaneous.KEY_GCM_REG_ID, "").commit();

			try {

				HttpURLConnection connection = RegistrationRequest.POSTConnection(host, device, email, password, true);
				//verifyResponse throws an MMCException
				if(verifyConnectionResponse(connection)) {

					String contents = readString(connection); //getResponseString(response);
					JSONObject json = new JSONObject(contents);
					mApiKey = json.optString(JSON_API_KEY, "");

					int dormant = json.optInt("dormant", 0);
					int userID = json.optInt(USER_ID_KEY, 0);
					if(mApiKey.length() > 0 ) {
						//SharedPreferences preferenceSettings = PreferenceManager.getDefaultSharedPreferences(mContext);
                        preferenceSettings.edit().putString(PreferenceKeys.User.APIKEY, mApiKey).commit();
						// Also save email for this version so it can be copied to the contact email setting for email raw data
						preferenceSettings.edit().putString(PreferenceKeys.User.USER_EMAIL, email).commit();
						preferenceSettings.edit().putString(PreferenceKeys.User.CONTACT_EMAIL, email).commit();
						PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(PreferenceKeys.User.CONTACT_EMAIL, email).commit();
                        preferenceSettings.edit().putBoolean(PreferenceKeys.Miscellaneous.SIGNED_OUT, false).commit();
						PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt(PreferenceKeys.Miscellaneous.USAGE_DORMANT_MODE, (int) dormant).commit();


					} else {
						LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "authorizeDevice", "api key is empty");
						throw new LibException("api key was empty");
					}
					if (userID > 0)
					{
                        preferenceSettings.edit().putInt(PreferenceKeys.User.USER_ID, userID).commit();
						LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "authorizeDevice", "userID=" + userID);
					}
					else
					{
						LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "authorizeDevice", "user id is empty");
					}
				}
			} catch (IOException e) {
				//Just log the exception for now
				LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "authorizeDevice", "fail to send authorization", e);
//				if (bFailover)
//					AuthorizeWindows (email);
				throw new LibException(e);
			} catch (JSONException e) {
				LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "authorizeDevice", "fail to recieve authorization", e);
//				if (bFailover)
//					AuthorizeWindows (email);
				throw new LibException(e);
			} 
			catch (Exception e) {
				LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "authorizeDevice", "fail to recieve authorization", e);
//				if (bFailover)
//					AuthorizeWindows (email);
				throw new LibException(e);
			} 
			
		}
	}

	/**
	 * Sends the registration request, receives the request and parses the response back
	 */
	public JSONArray getServerObjects(String type, HashMap<String, String> query) throws LibException
	{
		try {
			String email = PreferenceKeys.getSecurePreferenceString(PreferenceKeys.User.USER_EMAIL, null, mContext);
			
			URL request = RequestServerGet.getURL(mHost, mApiKey, email, type, query);
			HttpURLConnection connection = (HttpURLConnection) request.openConnection();
			connection.connect ();
			verifyConnectionResponse(connection);
			String responseString = readString(connection);

			JSONArray objects = new JSONObject(responseString).getJSONArray(type);
			return objects;
			
		}
		catch (IOException e) {
			throw new LibException(e);
		}
		catch (Exception e) {
			throw new LibException(e);
		}
	}
		
	public boolean isAuthorized() {
		boolean authorized = false;
        int userId = Global.getUserID(mContext);
		
		if (mApiKey != null && mApiKey.length() > 10 && userId > 0)
			authorized = true;
        // Unless user signed himself out
        SecurePreferences prefs = PreferenceKeys.getSecurePreferences(mContext);
        if (prefs.getBoolean(PreferenceKeys.Miscellaneous.SIGNED_OUT, false) == true)
            authorized = false;

		if (authorized == true && Global.isOnline() && Looper.myLooper() != Looper.getMainLooper())
		{
			URL request = RequestServerGet.getURL(mHost, mApiKey, null, "user", null);
			try {
				HttpURLConnection connection = (HttpURLConnection) request.openConnection();
				connection.connect();
				if (connection.getResponseCode() == 401)
					return false;
			}
			catch (Exception e) {
				LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "isAuthorized", "exception", e);
			}
		}
		return authorized; //  && mApiKey.length() > 10);
	}
	
	public void setAuthorized (String apiKey)
	{
		mApiKey = apiKey;
	}

	public void unAuthorizeDevice(DeviceInfo device, String email) throws LibException {
		SharedPreferences preferenceSettings = PreferenceKeys.getSecurePreferences(mContext);
		preferenceSettings.edit().remove(PreferenceKeys.User.APIKEY);
		preferenceSettings.edit().commit();
		mApiKey = null;
	}


	/**
	 * Report that the phone's SIM has changed
	 * @param device
	 */
	public void reportSimChange(GSMDevice device) {
		try {
			HttpURLConnection conn = ServerUpdateRequest.PUTSimChangeRequest(mHost, ServerUpdateRequest.DEVICE, mApiKey, device);
			verifyConnectionResponse(conn);
		} catch (Exception e) {
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "updateSim", "error rendering request", e);
		}
	}
	
	/**
	 * Report that the user has changed the "share with carrier" setting
	 */
	public boolean reportSettingChange(String type, String key, Object value, HashMap<String, String> carrier) {
		try {
			HttpURLConnection conn = ServerUpdateRequest.PUTSettingChangeRequest(mHost, type, mApiKey, key, value, carrier);
			return verifyConnectionResponse(conn);

		} catch (Exception e) {
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "reportSettingChange", "error rendering request", e);
            return false;
		}
	}
	

	/*
	 *  Get a DidYouKnow fact from the server
	 *  Some facts return a counter to be displayed in a special counter view
	 *  or two counters to interpolate
	 */
	public Long confirmEvent (long ltime, int evttype, int newtype, int rating, int userid) throws LibException
	{
		try {
			String path = mHost + "/api/confirmevent";
		
			//if (!((MMCApplication) mContext.getApplicationContext()).useLinux())
			//	path = mContext.getString(R.string.MMC_URL_WIN) + "ConfirmEvent.aspx";

			URL request = ConfirmEventRequest.getURL(path, mApiKey, ltime, evttype, newtype, rating, userid);
			HttpURLConnection connection = (HttpURLConnection) request.openConnection();
			connection.connect();
			verifyConnectionResponse(connection);
			String responseString = readString(connection);

		}
		catch (IOException e) {
			throw new LibException(e);
		}
		catch (Exception e) {
			throw new LibException(e);
		}
		return 1l;
	}
	
	/*
	 *  Get a DidYouKnow fact from the server
	 *  Some facts return a counter to be displayed in a special counter view
	 *  or two counters to interpolate
	 */
	public JSONObject getDidYouKnow (String opid) throws LibException
	{
		JSONObject jsonfact;
		try {
			String path = mHost + "/api/didyouknow";
		
			URL request = DidYouKnowRequest.getURL(path, mApiKey, opid);
			HttpURLConnection connection = (HttpURLConnection) request.openConnection();
			connection.connect ();
			String responseString = readString(connection);
			jsonfact = new JSONObject(responseString);

		}
		catch (IOException e) {
			throw new LibException(e);
		}
		catch (Exception e) {
			throw new LibException(e);
		}
		return jsonfact;
	}
	
	public List<Carrier> getTopOperators(double latitude, double longitude, int radius, int mcc, int limit) throws LibException
	{	
		List<Carrier> carriers = new ArrayList<Carrier>();
		try {
			String path = mHost + "/api/topop";
		
			TelephonyManager telephony = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
			String ccode = telephony.getNetworkCountryIso();
            URL request = TopOperatorsRequest.getURL(path, mApiKey, latitude, longitude, radius, mcc, limit, ccode);
			HttpURLConnection connection = (HttpURLConnection) request.openConnection();
			connection.connect ();
			verifyConnectionResponse(connection);
			String topResponseString = readString(connection);
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "getTopOperators", request.toString());

            //HttpClient mHttpClient = HttpUtils.createHttpClient();
			//HttpResponse topResponse = mHttpClient.execute(topRequest);
			//verifyResponse(topResponse);
			if (topResponseString.length() > 2)
			{
                SharedPreferences securePreferences = PreferenceKeys.getSecurePreferences(mContext);
                securePreferences.edit().putString(PreferenceKeys.Miscellaneous.TOPOP_RESPONSE, topResponseString).commit();
                securePreferences.edit().putString(PreferenceKeys.Miscellaneous.TOPOP_LAT, Double.toString(latitude)).commit();
                securePreferences.edit().putString(PreferenceKeys.Miscellaneous.TOPOP_LNG, Double.toString(longitude)).commit();
			}
		
			JSONArray operators = new JSONObject(topResponseString).getJSONArray("operators");
			for(int i=0; i<operators.length(); i++) 
			{
				Carrier carrier = new Carrier(operators.getJSONObject(i));
				carrier.loadLogo (mContext);
                if (carrier != null)
				    carriers.add(carrier);
			}
		}
		catch (IOException e) {
			throw new LibException(e);
		}
		catch (Exception e) {
			throw new LibException(e);
		}
		return carriers;
	}
		
	public JSONObject getAreaStats(double latitude, double longitude, int radius, int months, String ops) throws LibException
	{	
		JSONObject areastats = null;
		try {
			String path = mHost + "/api/stats";

			URL request = AreaStatsRequest.getURL(path, mApiKey, latitude, longitude, radius, months, ops);
			HttpURLConnection connection = (HttpURLConnection) request.openConnection();
			connection.connect ();
			verifyConnectionResponse(connection);
			String statResponseString = readString(connection);

			if (statResponseString.length() > 2)
			{
                SharedPreferences secureSettings = PreferenceKeys.getSecurePreferences(mContext);
                secureSettings.edit().putString(PreferenceKeys.Miscellaneous.TOPSTATS_RESPONSE, statResponseString).commit();
                secureSettings.edit().putString(PreferenceKeys.Miscellaneous.TOPSTATS_LAT, Double.toString(latitude)).commit();
                secureSettings.edit().putString(PreferenceKeys.Miscellaneous.TOPSTATS_LNG, Double.toString(longitude)).commit();
			}
		
			areastats = new JSONObject(statResponseString); // .getJSONObject("stat");
			
		}
		catch (IOException e) {
			throw new LibException(e);
		}
		catch (Exception e) {
			throw new LibException(e);
		}
		return areastats;
	}
	/**
	 * Gets the logo of the carrier specified by <code>carrier</code>
	 * @param carrier
	 * @return carrier logo
	 * @throws LibException
	 */
	Carrier carrierCurr;
	public Bitmap getCarrierLogo(HashMap<String, String> carrierparams) throws LibException {
        SharedPreferences securePreferences = PreferenceKeys.getSecurePreferences(mContext);
		try {
			//NetworkRequest networksRequest = new NetworkRequest(mHost, mApiKey, carrierparams);
			URL request = NetworkRequest.getURL(mHost, mApiKey, carrierparams);

			String networksResponseString = null;
 			String opresponse = securePreferences.getString(PreferenceKeys.Miscellaneous.OPERATOR_RESPONSE, null);
 			String oprequest = securePreferences.getString(PreferenceKeys.Miscellaneous.OPERATOR_REQUEST, null);
 			if (opresponse != null && oprequest != null)
			{
 				if (oprequest.equals(request.getQuery()))
 				{
 					networksResponseString = opresponse;
 				}
			}
 			if (networksResponseString == null)
 			{
 				HttpURLConnection connection = (HttpURLConnection) request.openConnection();
				connection.connect ();
				verifyConnectionResponse(connection);
				networksResponseString = readString(connection);
                securePreferences.edit().putString(PreferenceKeys.Miscellaneous.OPERATOR_RESPONSE, networksResponseString).commit();
                securePreferences.edit().putString(PreferenceKeys.Miscellaneous.OPERATOR_REQUEST, request.getQuery()).commit();
 			}JSONObject operator = new JSONObject(networksResponseString).getJSONObject(JSON_NETWORK_KEY);
			carrierCurr = new Carrier(operator);
			
			String carriername = carrierCurr.Name;
	        String logo = carrierCurr.Path;
	        logo = logo.substring(1); // remove leading slash
	        carrierCurr.loadLogo ( mContext);
	    	String logoPath = mContext.getApplicationContext().getFilesDir() + carrierCurr.Path;
			try
			{
				carrierCurr.Logo = BitmapFactory.decodeFile(logoPath);
			}
			catch (OutOfMemoryError e)
			{
				LoggerUtil.logToFile(LoggerUtil.Level.ERROR, "StatCategory", "StatCategory", "OutOfMemoryError loading logo " + logoPath);
			}
			return carrierCurr.Logo;
		}
		catch (IOException e) {
			throw new LibException(e);
		}
		catch (JSONException e) {
            securePreferences.edit().putString(PreferenceKeys.Miscellaneous.OPERATOR_RESPONSE, null).commit();
			throw new LibException(e);
		}
		catch (Exception e) {
			throw new LibException(e);
		}
	}
	public Carrier getCurrentCarrier ()
    {
        return carrierCurr;
    }
	
	
    private static String ReplaceCharacters (String str, String c, String r)
    {
    	if (str == null)
    		return "";
        int pos = str.indexOf (c);
        while (pos >= 0)
        {
            str = str.substring (0, pos) + r + str.substring (pos+c.length(), str.length());
            pos = str.indexOf (c);
        }
        return str;
    }
	
	public String getTwitterHandle(HashMap<String, String> carrier) throws LibException {
		try {
			URL request = NetworkRequest.getURL(mHost, mApiKey, carrier);
			HttpURLConnection connection = (HttpURLConnection) request.openConnection();
			connection.connect ();
			verifyConnectionResponse(connection);
			String networksResponseString = readString(connection);

			//JSONArray networks = new JSONObject(networksResponseString).getJSONArray(JSON_NETWORK_KEY);
			JSONObject network = new JSONObject(networksResponseString).getJSONObject(JSON_NETWORK_KEY);
			String twitter = network.getString(JSON_NETWORK_TWITTERHANDLE_KEY);
			return twitter;
		}
		catch (IOException e) {
			throw new LibException(e);
		}
		catch (Exception e) {
			throw new LibException(e);
		}
	}
	protected static boolean verifyConnectionResponse(HttpURLConnection connection) throws LibException {
		int responseCode = 0;
		try {
			responseCode =connection.getResponseCode();
		} catch (Exception e) {
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "verifyConnectionResponse", "getResponseCode", e);
			throw new LibException(e);
		}
		String contents = "";
		if(responseCode < HttpURLConnection.HTTP_OK || responseCode >= 400) {
			try {
				contents = readString(connection);
				LoggerUtil.logToFile(LoggerUtil.Level.WTF, TAG, "verifyResponse", "response = " + contents);

				JSONObject json = new JSONObject(contents);
				String message = json.optString(JSON_ERROR_KEY, "response had no error message");
				//MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "verifyResponse", "error in request "
				//		+ responseCode + " " + message);
				throw new LibException(message);
			} catch (Exception e) {
				LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "verifyResponse", contents, e);
				throw new LibException(e);
			}
		}
		else {
			//If the response is valid, evaluate if we need to parse the content
			switch(responseCode) {
				case HttpURLConnection.HTTP_OK:
					return true;
				case HttpURLConnection.HTTP_CREATED:
					return true;
				case HttpURLConnection.HTTP_NO_CONTENT:
					return false;
				default:
					return false;
			}
		}
	}

	/**
	 * Submits an event to the server.
	 * @param requestJSON
	 */
	public String submitEvent(String requestJSON) throws Exception{

		String linuxUrl = Global.getApiUrl(mContext);
		Gson gson = new Gson();
		String responseJSON = sendJSONPacket(linuxUrl + "/api/events", requestJSON, false);
		return responseJSON;
	}

	/**
	 * Uses the json packet supplied as the arguments to call the web service defined by the
	 * <code>endpoint</code> supplied. The response is returned as a string that is expected to be parsed
	 * later (as a json packet).
	 * @param endpoint
	 * @param jsonPacket
	 * @param log whether to log the <code>jsonPacket</code> to {@link LoggerUtil#LOG_FILE}
	 * @return
	 */
	private String sendJSONPacket(String endpoint, String jsonPacket, boolean log) throws Exception {

		URL url = new URL(endpoint);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000);
		conn.setConnectTimeout(15000);
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setFixedLengthStreamingMode(jsonPacket.getBytes().length);

		conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
		conn.setRequestProperty("MMCBrand", Global.getAppName(mContext));
		conn.setRequestProperty("MMCVersion", Global.getString(mContext, "app_versionName"));

		LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "sendJSONPacket", url.toString());
		//LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "sendJSONPacket json: ", jsonPacket);

		//open
		conn.connect();
		OutputStream os = new BufferedOutputStream(conn.getOutputStream());
		os.write(jsonPacket.getBytes());
		//clean up
		os.flush();

		String responseContents = readString(conn);
		return responseContents;

	}

	/**
	 * Requests the server to send a Csv to the users email
	 * with a 24 hr start-stop time interval
	 * @return
	 */
	public String requestCsvEmail (int userid, String carrier, int mcc, int mnc, String manufacturer, String model, String device, String appname, String apikey) throws Exception
	{
		SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");
		Date date = new Date();
		String stoptime = date.toGMTString();
		long startms = date.getTime() - 24*3600000;
		Date datestart = new Date(startms);
		String starttime = datestart.toGMTString();
		String parameters = "?userid=" + userid + "&start=" + starttime + "&stop=" + stoptime;
		parameters += "&carrier=" + carrier + "&mcc=" + mcc + "&mnc=" + mnc + "&manuf=" + manufacturer + "&model=" + model + "&device=" + device + "&email=1";
		parameters += "&appname=" + appname + "&lang=" + DeviceInfo.getLanguageCode() + "&apiKey=" + apikey;

		parameters = parameters.replace(" ", "%20");
		String url = Global.getApiUrl(mContext)+ "/CoverageData.aspx" + parameters;
		URL request = new URL (url);
		HttpURLConnection connection = (HttpURLConnection) request.openConnection();
		connection.connect();
		String strResponse = readString(connection);
		return strResponse;
	}

	/**
	 * @return true if the device is considered roaming on the current network
	 */
	protected boolean isNetworkRoaming() {
		return ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE)).isNetworkRoaming();
	}
	
	/**
	 * @return user setting for sending data while roaming
	 */
	protected boolean isDataRoamingEnabled() {
		return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(PREFERENCE_KEY_ENABLE_DATA_ROAMING, false);
	}
	
	/**
	 * @return true if the current connected network is wifi
	 */
	protected boolean isNetworkWifi() {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager != null) {
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			if(networkInfo != null) {
				int wifiState = networkInfo.getType();
				return (wifiState == ConnectivityManager.TYPE_WIFI);
			}
		}
		return false;
	}

	public static String getHttpURLResponse (String path,LinkedList<Pair> params, boolean bVerifyJson) throws Exception
	{
		String paramsString = WebReporter.URLEncodedFormat(params);
		return getHttpURLResponse (path + paramsString, bVerifyJson);
	}

	public static String getHttpURLResponse (String url, boolean bVerifyJson) throws Exception
	{
		URL urlObj = new URL (url);
		HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
		connection.setReadTimeout(10000);
		connection.setConnectTimeout(15000);
		connection.connect();
		if (bVerifyJson)
			verifyConnectionResponse(connection);
		String responseString = readString(connection);

		return responseString;
	}

	public static String readString (HttpURLConnection connection) throws IOException, UnsupportedEncodingException
	{
		InputStream stream = null;
		String msg = "readString from url: ";

		try {
			int code = connection.getResponseCode();
			BufferedReader br = null;
			msg += connection.getURL().toString();

			boolean error = false;
			try {
				 br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			} catch (Exception e)
			{
				error = true;
				try{
					br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
				}
				catch (Exception e2) {
					LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, msg, "exception", e2);
				}
			}
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line+"\n");
			}
			br.close();
			String str = sb.toString();
			if (error)
			{
				LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, msg, "error " + str);
				return "";
			}
			return str;

		}
		catch (Exception ex) {
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, msg, "exception", ex);
		}
		finally
		{
			//if (stream != null)
			//	try{stream.close();} catch (Exception e) {}

			if (connection != null) {
				try {
					connection.disconnect();
				} catch (Exception ex) {
					LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "readString", "exception closing stream", ex);
				}
			}
		}
		return null;
	}

	public static String geocode (Context context, Location location)
	{
		return geocode (context, location.getLatitude(), location.getLongitude());
	}
	public static String geocode (Context context, double latitude, double longitude)
	{
		String addressString = String.format("%.4f, %.4f", latitude, longitude);
		try {
			String apiKey = Global.getApiKey(context);
			String server = Global.getApiUrl(context);
			String url = server + "/api/osm/location?apiKey=" + apiKey + "&location=" + latitude + "&location=" + longitude;
			String response = WebReporter.getHttpURLResponse(url, false);

			JSONObject json = null;
			JSONArray jsonArray = null;

			if(response == null)
				return addressString;

			try {
				jsonArray = new JSONArray(response);
			} catch (JSONException e) {
				return addressString;
			}
			try {
				for(int i = 0; i < jsonArray.length(); i++) {
					json = jsonArray.getJSONObject(i);
					if(json.has("error")) {
						String error = json.getString("error");
						return null;
					}
					else {
						addressString = "";
						json = json.getJSONObject("address");
						String number = "";
						if(json.has("house_number")) {
							number = json.getString("house_number");
							addressString += number + " ";
						}
						String road = json.getString("road");
						addressString += road;// + suburb;
						return addressString;
					}
				}
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		} catch(Exception e) {
		}

		return addressString;
	}

	public static String URLEncodedFormat (List <Pair> parameters)
	{
        final StringBuilder result = new StringBuilder();
        for (final Pair parameter : parameters) {
            final String encodedName = encode(parameter.first.toString());
            final String value = parameter.second.toString();
            final String encodedValue = value != null ? encode(value) : "";
            if (result.length() > 0)
                result.append("&");
            result.append(encodedName);
            result.append("=");
            result.append(encodedValue);
        }
        return result.toString();
    }
	public static String encode (final String content) {
        try {
            return URLEncoder.encode(content, "utf-8");
        } catch (UnsupportedEncodingException problem) {
            throw new IllegalArgumentException(problem);
        }
    }

}
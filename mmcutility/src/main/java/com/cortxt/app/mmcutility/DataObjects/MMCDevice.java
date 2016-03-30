package com.cortxt.app.mmcutility.DataObjects;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.Utils.SystemPropertiesProxy;
import com.cortxt.app.mmcutility.Utils.LiveBuffer;

/**
 * This class contains methods for getting all available device properties.
 * @author nasrullah
 * @see MMCGSMDevice
 * @see MMCCDMADevice
 */
public abstract class MMCDevice {
	private static final String TAG = MMCDevice.class.getSimpleName();

	public static final String[] SUPPORTED_LANGUAGES = { "en", "es" };

	public static final String KEY_TYPE = "type";
	public static final String TYPE_ANDROID = "Android";
	public static final String KEY_APP_VERSION = "appVersion";
	public static final String KEY_APP_NAME = "appName";
	public static final String KEY_MANUFACTURER = "manufacturer";
	public static final String KEY_MODEL = "model";
	public static final String KEY_ANDROID_VERSION_NUMBER = "osNum";
	public static final String KEY_BOARD = "board";
	public static final String KEY_RADIO = "radio";
	public static final String KEY_SERIAL = "serial";
	public static final String KEY_BOOTLOADER = "bootloader";
	public static final String KEY_LANGUAGE= "language";

	public static final String KEY_PHONE_TYPE = "phoneType";
	public static final String TYPE_GSM = "gsm";
	public static final String TYPE_CDMA = "cdma";
	public static final String TYPE_LTE = "lte";
	public static final String TYPE_IDEN = "iden";
	public static final String TYPE_UNKNOWN = "unknown";
	public static final String KEY_PHONE_NUMBER = "phone";
	public static final String KEY_MCC = "mcc";
	public static final String KEY_MNC = "mnc";
	public static final String KEY_CARRIER = "carrier";
	public static final String KEY_IMSI = "imsi";	
	public static final String KEY_IP = "ipv4";
	public static final String KEY_DEVICE_ROOTED="rooted";
	protected Context mContext;
	protected TelephonyManager mTelephonyManager;

	public MMCDevice(Context context) {
		mContext = context;
		mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	}

	public String getPhoneType() {
		//String str = android.os.Build.getRadioVersion();
		//String v = System.getProperty("os.version");
		int phoneType = mTelephonyManager.getPhoneType();
		if (phoneType == TelephonyManager.PHONE_TYPE_CDMA )
			return TYPE_CDMA;
		else if (phoneType == TelephonyManager.PHONE_TYPE_GSM )
			return TYPE_GSM;
		else if (phoneType == TelephonyManager.NETWORK_TYPE_LTE)
			return TYPE_LTE;
		else
			return TYPE_UNKNOWN;
	}
	public int getNetworkType ()
	{
		return mTelephonyManager.getNetworkType();
	}

	/**
	 * @return The phone number, or an empty string if it is unknown
	 */
	public String getPhoneNumber() {
        String phonenum = mTelephonyManager.getLine1Number();
        MMCLogger.logToFile(MMCLogger.Level.DEBUG, "MMCDevice", "getLine1Number:", phonenum);
		return mTelephonyManager.getLine1Number() != null ? mTelephonyManager.getLine1Number() : "";
	}

	/**
	 * @return The phone's mcc, or an empty string if it is unknown
	 */
	public Integer getMCC() {
		String networkOperator = mTelephonyManager.getNetworkOperator();

		if(networkOperator != null && networkOperator.length() > 3) {
			String mcc =  networkOperator.substring(0, 3);
			int MCC = 0;
			try{
				MCC = Integer.parseInt(mcc);
			} catch (Exception e) {}
			return MCC;
		}
		else {
			return 0;
		}
	}
	public String getIMSI()
	{
		try{
			String myIMSI = SystemPropertiesProxy.get(mContext, "persist.lg.data.firstopsim");
			if (myIMSI != null && myIMSI.length() > 10)
			{
				if (myIMSI.length() > 15)
					myIMSI = myIMSI.substring(0,15);
				return myIMSI;
			}
			return mTelephonyManager.getSubscriberId() != null ? mTelephonyManager.getSubscriberId() : "";
		}
		catch (Exception e){}
		return "";
	}
	public String getIMEI()
	{
		return mTelephonyManager.getDeviceId();
	}
	/**
	 * @return The phone's mnc, or an empty string if it is unknown
	 */
	public Integer getMNC() {
		String networkOperator = mTelephonyManager.getNetworkOperator();
		if(networkOperator != null && networkOperator.length() > 3) {
			String mnc = MMCDevice.fixMNC(networkOperator.substring(3));
			int MNC = 0;
			try{
				MNC = Integer.parseInt(mnc);
			} catch (Exception e) {}
			return MNC;
		}
		else {
			return 0;
		}
	}

	public static String fixMNC (String mnc)
	{
		mnc = mnc.replace("f", "");
		mnc = mnc.replace("F", "");
		return mnc;
	}

	/**
	 * @return The carrier name, or an empty string if it is unknown
	 */
	public String getCarrier() {
		return mTelephonyManager.getNetworkOperatorName() != null ? mTelephonyManager.getNetworkOperatorName() : "";
	}

	public static String getIPAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress()) ) {
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (Exception ex) {
			MMCLogger.logToFile(MMCLogger.Level.ERROR, "Event", "getLocalIpAddress", "", ex);

		}
		return null;
	}

	/**
	 * @return The phone's manufacturer, or an empty string if it is unknown
	 */
	public static String getManufacturer() {
		return android.os.Build.MANUFACTURER != null ? android.os.Build.MANUFACTURER : "";
	}

	/**
	 * @return The phone's model, or an empty string if it is unknown
	 */
	public String getModel() {
		return android.os.Build.MODEL != null ? android.os.Build.MODEL : "";
	}

	/**
	 * @return The phone's board model, or an empty string if it is unknown
	 */
	public String getBoard() {
		return android.os.Build.BOARD != null ? android.os.Build.BOARD : "";
	}

	/**
	 * @return The phone's radio version, or an empty string if it is unknown
	 */
	public String getRadio() {
		try {
			java.lang.reflect.Field radioField = android.os.Build.class.getField("RADIO");
			String radio = (String) radioField.get(new android.os.Build());
			if(!radio.equalsIgnoreCase("unknown")) {
				return radio;
			}
			else {
				return "";
			}

		} catch (SecurityException e) {
			return "";
		} catch (NoSuchFieldException e) {
			return "";
		} catch (IllegalArgumentException e) {
			return "";
		} catch (IllegalAccessException e) {
			return "";
		}
	}

	/**
	 * @return The phone's os version number, or an empty string if it is unknown
	 */
	public String getAndroidVersionNumber() {
		return android.os.Build.VERSION.RELEASE != null ? android.os.Build.VERSION.RELEASE : "";
	}

	private static Class<android.os.Build.VERSION> versionClass = android.os.Build.VERSION.class;
	public static Integer getAndroidVersion(){
		String field = "SDK_INT";
		try {
			java.lang.reflect.Field returnValue = versionClass.getField(field);
			return (Integer) returnValue.get(new android.os.Build.VERSION());
		} catch (Exception e) {
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "getAndroidVersion", String.format("Could not get field %s", field), e);
		}
		return -1;
	}

	/**
	 * @return The phone's serial, or an empty string if it is unknown
	 */
	public String getSerial() {
		try {
			java.lang.reflect.Field serialField = android.os.Build.class.getField("SERIAL");
			String serial = (String) serialField.get(new android.os.Build());
			if(!serial.equalsIgnoreCase("unknown")) {
				return serial;
			}
			else {
				return "";
			}
		} catch (SecurityException e) {
			return "";
		} catch (NoSuchFieldException e) {
			return "";
		} catch (IllegalArgumentException e) {
			return "";
		} catch (IllegalAccessException e) {
			return "";
		}
	}

	/**
	 * @return The phone's bootloader version, or an empty string if it is unknown
	 */
	public String getBootloader() {
		try {
			java.lang.reflect.Field bootloaderField = android.os.Build.class.getField("BOOTLOADER");
			String bootloader = (String) bootloaderField.get(new android.os.Build());
			if(!bootloader.equalsIgnoreCase("unknown")) {
				return bootloader;
			}
			else {
				return "";
			}
		} catch (SecurityException e) {
			return "";
		} catch (NoSuchFieldException e) {
			return "";
		} catch (IllegalArgumentException e) {
			return "";
		} catch (IllegalAccessException e) {
			return "";
		}
	}

	/**
	 * @return The MMC app version, or an empty string if it is unknown
	 */
	public int getAppVersion() {
		try {
			return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "getAppVersion", "failed to get app version name", e);
			return 0;
		}
	}
	public static boolean isDeviceRooted(Context context){
		String buildTags = android.os.Build.TAGS;
		if (buildTags != null && buildTags.contains("d -keys")) {
			return true;
		}

		try {
			String[] places = {"/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su",
					"/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su"};
					//"/system/app/Superuser.apk"};
			for (String where : places) {
				if ( new File( where  ).exists() ) {
					if (context != null)
						PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("KEY_SETTINGS_ROOT_ACCESS", true).commit();
					return true;
				}
			}
		}

		catch (Exception e1) {
		}

		// user can manually say the device is rooted
		boolean rooted = false;
		if (context != null)
			rooted = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("KEY_SETTINGS_ROOT_ACCESS", false);
		return rooted;
		//boolean command = canExecuteCommand("/system/xbin/which su") || canExecuteCommand("/system/bin/which su") || canExecuteCommand("which su");
		//return command;



	}
	
	private static  boolean canExecuteCommand(String command) {
		boolean executedSuccesfully;
		try {
			Runtime.getRuntime().exec(command);
			executedSuccesfully = true;
		} catch (Exception e) {
			executedSuccesfully = false;
		}
		return executedSuccesfully;
	}

	/**
	 * @return a HashMap of the phone's properties that are sent to the linux server for registration.
	 * The keys are the same as the keys expected by the linux server.
	 */
	public HashMap<String, String> getProperties() {
		HashMap<String, String> properties = new HashMap<String, String>();

		properties.put(KEY_TYPE, TYPE_ANDROID);
		properties.put(KEY_APP_VERSION, Integer.toString(getAppVersion()));
		PackageManager packageManager = mContext.getApplicationContext().getPackageManager();
		ApplicationInfo applicationInfo = mContext.getApplicationContext().getApplicationInfo();
		String title = (String)packageManager.getApplicationLabel(applicationInfo);
		properties.put(KEY_APP_NAME, title);
		properties.put(KEY_MANUFACTURER, getManufacturer());
		properties.put(KEY_MODEL, getModel());
		properties.put(KEY_BOARD, getBoard());
		properties.put(KEY_DEVICE_ROOTED, String.valueOf(isDeviceRooted(null)));

		if(getAndroidVersion() > 0)
			properties.put(KEY_ANDROID_VERSION_NUMBER, "Android " + getAndroidVersion());


		if(getSerial().length() > 0)
			properties.put(KEY_SERIAL, getSerial());

		if(getRadio().length() > 0)
			properties.put(KEY_RADIO, getRadio());

		if(getBootloader().length() > 0)
			properties.put(KEY_BOOTLOADER, getBootloader());


		if(getPhoneType().length() > 0)
			properties.put(KEY_PHONE_TYPE, getPhoneType());

		if(getPhoneNumber().length() > 0)
			properties.put(KEY_PHONE_NUMBER, getPhoneNumber());

		//if(getMCC().length() > 0)
		properties.put(KEY_MCC, getMCC().toString());

		//if(getMNC().length() > 0)
		properties.put(KEY_MNC, getMNC().toString());

		if(getCarrier().length() > 0)
			properties.put(KEY_CARRIER, getCarrier());

		properties.put(KEY_LANGUAGE, getLanguageCode());

		final String ip = getIPAddress();
		if ( ip != null && ip.length() >= 7) {
			properties.put(KEY_IP, ip); // don't send bad ips or nones that wouldn't pass on server side
		}

		final String imsi = getIMSI();
		if(imsi != null && imsi.length() > 0)
			properties.put(KEY_IMSI, imsi);		

		return properties;
	}

	/**
	 * @return a HashMap of the phone's properties that are sent to the linux server for registration.
	 * The keys are the same as the keys expected by the linux server.
	 */
	public HashMap<String, String> getCarrierProperties() {
		HashMap<String, String> properties = new HashMap<String, String>();

		if(getPhoneType().length() > 0)
			properties.put(KEY_PHONE_TYPE, getPhoneType());

		properties.put(KEY_MCC, getMCC().toString());

		properties.put(KEY_MNC, getMNC().toString());

		if(getCarrier().length() > 0)
			properties.put(KEY_CARRIER, getCarrier());

		final String ip = getIPAddress();
		if ( ip != null && ip.length() >= 7) {
			properties.put(KEY_IP, ip); // don't send bad ips or nones that wouldn't pass on server side
		}

		final String imsi = getIMSI();
		if(imsi != null && imsi.length() > 0)
			properties.put(KEY_IMSI, imsi);

		return properties;
	}

	public static String getLanguageCode() {
		final String lang = Locale.getDefault().getLanguage();
		for ( String l : SUPPORTED_LANGUAGES ) {
			if ( l.equals(lang) ) return l;
		}

		return "en";
	}

}

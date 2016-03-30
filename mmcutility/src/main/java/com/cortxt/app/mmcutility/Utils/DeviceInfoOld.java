package com.cortxt.app.mmcutility.Utils;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * This class is very useful while constructing json packets for events to be sent
 * to the server. It provides the necessary strings for the packet.
 */
public class DeviceInfoOld {	
	public static final String TAG = DeviceInfoOld.class.getSimpleName();
	private static Class<android.os.Build> buildClass = android.os.Build.class;	
	private static Class<android.os.Build.VERSION> versionClass = android.os.Build.VERSION.class;
	public static int battery = -1;	//Note: this is public and is accessible by all
	public static boolean batteryCharging = false;
	private static int suggestedPhoneType = TelephonyManager.PHONE_TYPE_NONE;	//this is used during jUnit testing
	
	public static String getManufacturer(){
		return getField("MANUFACTURER");
	}
	
	public static String getPhoneModel(){
		return getField("MODEL");
	}
	
	public static String getDevice(){
		return getField("DEVICE");
	}
	
	public static Integer getAndroidVersion(){
		String field = "SDK_INT";
		try {
			java.lang.reflect.Field returnValue = versionClass.getField(field);
			return (Integer) returnValue.get(new android.os.Build.VERSION());
		} catch (SecurityException e) {
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "getAndroidVersion", String.format("Could not get field %s", field), e);
		} catch (NoSuchFieldException e) {
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "getAndroidVersion", String.format("Could not get field %s", field), e);
		} catch (IllegalAccessException e){
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "getAndroidVersion", String.format("Could not get field %s", field), e);
		} catch (Exception e){
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "getAndroidVersion", String.format("Could not get field %s", field), e);
		}
		return -1;
	}
	
	private static String getField(String field){
		try {
			java.lang.reflect.Field returnValue = buildClass.getField(field);
			return (String) returnValue.get(new android.os.Build());
		} catch (SecurityException e) {
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "getAndroidVersion", String.format("Could not get field %s", field), e);
		} catch (NoSuchFieldException e) {
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "getAndroidVersion", String.format("Could not get field %s", field), e);
		} catch (IllegalAccessException e){
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "getAndroidVersion", String.format("Could not get field %s", field), e);
		} catch (Exception e){
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "getAndroidVersion", String.format("Could not get field %s", field), e);
		}
		return "";
	}
	
	/**
	 * For JUnit tests, this method is called during the setup/teardown phase to
	 * make the DeviceInfo return a spcific phone type. 
	 * @param phoneType
	 */
	public static void setPhoneTypeForTesting(int phoneType){
		suggestedPhoneType = phoneType;
		Log.d(TAG, "***** Changing phone type for JUnit testing *****");
	}
	
	public static int getPhoneType(Context context){
		if (suggestedPhoneType != TelephonyManager.PHONE_TYPE_NONE){
			return suggestedPhoneType;
		} else {
			TelephonyManager tm = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
			return tm.getPhoneType();
		}
	}

	public static int getPlatform(){
		if(android.os.Build.BRAND.toLowerCase().contains("blackberry") && Build.VERSION.SDK_INT < 18) {
			return 3;
		}
		else {
			return 1;
		}
	}
}

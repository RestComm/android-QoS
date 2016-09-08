package com.cortxt.app.utillib.Utils;

/**
 * Created by bscheurman on 16-08-25.
 */
/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cortxt.app.utillib.Reporters.ReportManager;
import com.cortxt.app.utillib.Reporters.WebReporter.ServerUpdateRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(refreshedToken);

        // Check stored Token
        String regid = getGCMRegistrationId(this);
        if (regid == null || !regid.equals(refreshedToken)) {
            registerInBackground(this, refreshedToken);
        }
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    public static void checkFCMRegistration (final Context context)
    {
        String regid = getGCMRegistrationId(context);
        if (regid == null) {
            SharedPreferences securePreferences = PreferenceKeys.getSecurePreferences(context);
            String registrationId = securePreferences.getString(PreferenceKeys.Miscellaneous.KEY_GCM_REG_ID, "");
            if (registrationId.equals(""))
                registrationId = FirebaseInstanceId.getInstance().getToken();
            if (registrationId != null && !registrationId.equals(""))
                registerInBackground(context, registrationId);
        }
    }
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    public static void registerInBackground(final Context context, final String regid) {

        new Thread(new Runnable()
        {
            @Override
            public void run() {
                String msg = "";

                try {
                    //GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

                    //String regid = gcm.register(GCM_SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    //MMCLogger.logToFile(MMCLogger.Level.WTF, TAG, "registerInBackground", msg);


                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    boolean sentToken = sendGCMRegistrationIdToBackend(context, regid);
                    // Persist the registration ID - no need to register again.
                    storeRegistrationId(context, regid, sentToken);

                } catch (Exception ex) {
                    msg = "Error :" + ex.getMessage();
                    LoggerUtil.logToFile(LoggerUtil.Level.WTF, TAG, "registerInBackground", msg);

                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }

            }
        }).start();
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private static String getGCMRegistrationId(Context context) {
        SharedPreferences securePreferences = PreferenceKeys.getSecurePreferences(context);
        String registrationId = securePreferences.getString(PreferenceKeys.Miscellaneous.KEY_GCM_REG_ID, "");
        if (registrationId.equals("")) {
            return null;
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int gcmRegisteredVersion = securePreferences.getInt(PreferenceKeys.Miscellaneous.KEY_GCM_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = securePreferences.getInt(PreferenceKeys.User.VERSION, -1);

        if (gcmRegisteredVersion != currentVersion) {
            return null;
        }

        // If GCM Token hasn't been sent to the current ApiKey, have it re-send
        String gcmSentTo = securePreferences.getString(PreferenceKeys.Miscellaneous.KEY_GCM_REG_SENTTO, null);
        if (gcmSentTo == null || !gcmSentTo.equals(Global.getApiKey(context)))
            return null;

        return registrationId;
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
     private static boolean sendGCMRegistrationIdToBackend(Context context, String registrationId) {
        if (Global.getApiKey(context) == null)
            return false;
        ReportManager reportmanager = ReportManager.getInstance(context);
        return reportmanager.reportSettingChange(ServerUpdateRequest.DEVICE, ServerUpdateRequest.KEY_GCM_REG_ID, registrationId);

    }
    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private static void storeRegistrationId(Context context, String regId, boolean sentToken) {
        SharedPreferences securePreferences = PreferenceKeys.getSecurePreferences(context);
        int gcmRegisteredVersion = securePreferences.getInt(PreferenceKeys.Miscellaneous.KEY_GCM_APP_VERSION, Integer.MIN_VALUE);
        int appVersion = securePreferences.getInt(PreferenceKeys.User.VERSION, -1);

        //Log.i(TAG, "Saving regId on app version " + appVersion);
        securePreferences.edit().putInt(PreferenceKeys.Miscellaneous.KEY_GCM_APP_VERSION, appVersion).commit();
        securePreferences.edit().putString(PreferenceKeys.Miscellaneous.KEY_GCM_REG_ID, regId).commit();
        if (sentToken)
            securePreferences.edit().putString(PreferenceKeys.Miscellaneous.KEY_GCM_REG_SENTTO, Global.getApiKey(context)).commit();

    }

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static String GCM_SENDER_ID = "1084931208947";//"AIzaSyBgmkbPeFvuXabJbFhxWxE4W30aSIh9JPk";
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
//    public boolean checkPlayServices(Context context, boolean forceGCM) {
//
//        PackageManager pkMan = this.getPackageManager();
//        int GCMPermissionValue = pkMan.checkPermission("com.google.android.c2dm.permission.RECEIVE", this.getPackageName());
//        if (GCMPermissionValue != 0)
//            return false;
//
//        // See if GCM services are enabled by MMC
//        if (forceGCM == false) {
//            int useGCM = PreferenceManager.getDefaultSharedPreferences(context).getInt(PreferenceKeys.Miscellaneous.USE_GCM, 0);
//            if (useGCM == 1)
//                forceGCM = true;
//        }
//        if (forceGCM == false)
//            return false;
//
//        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
//        if (resultCode != ConnectionResult.SUCCESS) {
////            if (context instanceof Activity) {
////                Activity activity = (Activity) context;
////                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
////                    GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
////                            PLAY_SERVICES_RESOLUTION_REQUEST).show();
////                } else {
////                    MMCLogger.logToFile(MMCLogger.Level.WTF, TAG, "checkPlayServices", "this Device does not support Google Play Services");
////                    //Log.i(TAG, "This device is not supported.");
////                    //finish();
////                }
////			}
//            LoggerUtil.logToFile(LoggerUtil.Level.WTF, TAG, "checkPlayServices", "this Device does not support Google Play Services");
//            return false;
//        }
//        else
//        {
////            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
////            String regid = getRegistrationId(context);
////
////            LoggerUtil.logToFile(LoggerUtil.Level.WTF, TAG, "checkPlayServices", "getRegistrationId = " + regid);
////
////            if (regid.equals("")) {
////                registerInBackground(context);
////            }
//        }
//        return true;
//    }
}

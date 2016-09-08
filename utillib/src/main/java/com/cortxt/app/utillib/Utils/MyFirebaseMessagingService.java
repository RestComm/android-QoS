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

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
//import android.support.v4.app.NotificationCompat;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cortxt.app.utillib.Reporters.ReportManager;
import com.cortxt.app.utillib.Utils.Global;
import com.cortxt.app.utillib.Utils.LoggerUtil;
import com.cortxt.app.utillib.Utils.PreferenceKeys;
import com.cortxt.app.utillib.Utils.SNTPClient;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.securepreferences.SecurePreferences;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String MainServiceClass = "com.cortxt.app.corelib.MainService";
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        final Context fContext = this;

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            final String msg = remoteMessage.getData().get("msg");//extras.getString("msg");
            String stime = remoteMessage.getData().get("timestamp"); // Long.toString(System.currentTimeMillis()); // extras.getString("timestamp");
            String from = remoteMessage.getData().get("from");;//extras.getString("from");
            String messageType = "msg";
            final long time = Long.parseLong(stime);
            final long currtime = System.currentTimeMillis();

            LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "GcmBroadcastReceiver", "onReceive", "received " + messageType + ", timestamp = " + stime + ", msg = " + msg);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        long ntptime = getNTPTime();
                        long currtime2 = System.currentTimeMillis();
                        long correction = currtime2 - ntptime; // time correction is negative if Android clock is slow, behind the network time
                        final long starttime = time + 10000 + correction; // a negative correction means schedule sooner according to thr Android time
                        LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "GcmBroadcastReceiver", "onReceive", "NTPtime " + ntptime + ", correction = " + correction + ", starttime = " + starttime +
                                ",currtime = " + currtime + ", currtime2 = " + currtime2);

                        if (time + 60000 > ntptime)
                            passGCMMessage(fContext, msg, starttime);
                        else
                            LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "GcmBroadcastReceiver", "onReceive", "Expired GCM MESSAGE" + (time + 60000) + " < " + ntptime);
                    } catch (Exception e) {
                        LoggerUtil.logToFile(LoggerUtil.Level.ERROR, "GcmBroadcastReceiver", "onReceived", "NTP Time check  exception", e);
                        if (time + 60000 > currtime)
                            passGCMMessage(fContext, msg, time + 10000);
                    }
                }
            }).start();
        }

        // Check if message contains a notification payload.
        //if (remoteMessage.getNotification() != null) {
        //    Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        //}

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    private boolean isMainServiceRunning (Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MainServiceClass.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void passGCMMessage (Context context, String msg, long time)
    {
        LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "GcmBroadcastReceiver", "onReceive", "msg = " + msg);
        if (isMainServiceRunning(context)) {
            Intent mmcintent = new Intent(CommonIntentActionsOld.GCM_MESSAGE);
            mmcintent.putExtra(CommonIntentActionsOld.GCM_MESSAGE_EXTRA, msg);
            mmcintent.putExtra("GCM_STARTTIME_EXTRA", time);
            context.sendBroadcast(mmcintent);
        } else {
            boolean isAuthorized = ReportManager.getInstance(context.getApplicationContext()).isAuthorized();
            SecurePreferences securePrefs = PreferenceKeys.getSecurePreferences(context);
            boolean bStoppedService = securePrefs.getBoolean(PreferenceKeys.Miscellaneous.STOPPED_SERVICE, false);
            if (bStoppedService) // See if service was stopped due to dormant state set by server
            {
                LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "GcmBroadcastReceiver", "onReceive", "restarting dormant service");
                int dormantMode = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).getInt(PreferenceKeys.Miscellaneous.USAGE_DORMANT_MODE, 0);
                if (dormantMode >= 100 && !Global.isServiceYeilded(context)) {
                    PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit().putInt(PreferenceKeys.Miscellaneous.USAGE_DORMANT_MODE, 1).commit();
                    bStoppedService = false;
                    securePrefs.edit().putBoolean(PreferenceKeys.Miscellaneous.STOPPED_SERVICE, false).commit();
                }
            }

            LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "GcmBroadcastReceiver", "onReceive", "startService");

            if (isAuthorized && !bStoppedService) {
                Intent intent = new Intent ();
                // Start the MainService
                ComponentName comp = new ComponentName(context.getPackageName(), MainServiceClass);
                intent.putExtra(CommonIntentActionsOld.GCM_MESSAGE_EXTRA, msg);
                context.startService((intent.setComponent(comp)));
            }
        }
    }
    private long getNTPTime(){
        long now = 0;
        SNTPClient sntpClient = new SNTPClient();

        if (sntpClient.requestTime("0.pool.ntp.org", 5000)) {
            now = sntpClient.getNtpTime();
            return now;
        }

        return System.currentTimeMillis();
    }
    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
//    private void sendNotification(String messageBody) {
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);
//
//        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.ic_stat_ic_notification)
//                .setContentTitle("FCM Message")
//                .setContentText(messageBody)
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri)
//                .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
//    }
}

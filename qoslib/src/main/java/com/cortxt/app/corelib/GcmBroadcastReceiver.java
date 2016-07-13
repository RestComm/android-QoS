package com.cortxt.app.corelib;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.cortxt.app.corelib.Services.Intents.IntentHandler;
import com.cortxt.app.utillib.Reporters.ReportManager;
import com.cortxt.app.utillib.Utils.Global;
import com.cortxt.app.utillib.Utils.LoggerUtil;
import com.cortxt.app.utillib.Utils.PreferenceKeys;
import com.cortxt.app.utillib.Utils.SNTPClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.securepreferences.SecurePreferences;

/**
 * Created by bscheurman on 2015-04-13.
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        final Context fContext = context;
        LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "GcmBroadcastReceiver", "onReceive", " ");

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);


        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            try {
                if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                    //context.sendNotification("Send error: " + extras.toString());
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                    //sendNotification("Deleted messages on server: " +
                    //        extras.toString());
                    // If it's a regular GCM message, do some work.
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                    final String msg = extras.getString("msg");
                    String stime = extras.getString("timestamp");
                    String from = extras.getString("from");
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

                setResultCode(Activity.RESULT_OK);
            }
            catch (Exception e)
            {
                LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "GcmBroadcastReceiver", "onReceive", "exception", e);
            }
        }
    }
    private boolean isMainServiceRunning (Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MainService.class.getCanonicalName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void passGCMMessage (Context context, String msg, long time)
    {
        LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "GcmBroadcastReceiver", "onReceive", "msg = " + msg);
        if (isMainServiceRunning(context)) {
            Intent mmcintent = new Intent(IntentHandler.GCM_MESSAGE);
            mmcintent.putExtra(IntentHandler.GCM_MESSAGE_EXTRA, msg);
            mmcintent.putExtra("GCM_STARTTIME_EXTRA", time);
            context.sendBroadcast(mmcintent);
        } else {
            boolean isAuthorized = ReportManager.getInstance(context.getApplicationContext()).isAuthorized();
            SecurePreferences securePrefs = MainService.getSecurePreferences(context);
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
                ComponentName comp = new ComponentName(context.getPackageName(),
                        MainService.class.getName());
                intent.putExtra(IntentHandler.GCM_MESSAGE_EXTRA, msg);
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
}
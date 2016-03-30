package com.cortxt.app.mmccore.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.cortxt.app.mmccore.MMCService;
import com.cortxt.app.mmccore.Services.Intents.MMCIntentHandler;
import com.cortxt.app.mmccore.UtilsOld.ActiveEvent;
import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.cortxt.app.mmcutility.DataObjects.PhoneState;
import com.cortxt.app.mmcutility.Reporters.ReportManager;
import com.cortxt.app.mmcutility.Utils.Global;
import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.Utils.PreferenceKeys;
import com.cortxt.app.mmcutility.Utils.UsageLimits;

import org.json.JSONObject;

/**
 * Created by bscheurman on 16-01-23.
 */
public class MMCCommand {

    public static final String TAG = MMCCommand.class.getSimpleName();

    public static String getLogin (Context context)
    {
        SharedPreferences securePref = MMCService.getSecurePreferences(context);
        String value = securePref.getString (PreferenceKeys.User.USER_EMAIL, null);
        return value;
    }

    public static void setLogin (Context context, String login)
    {
        SharedPreferences securePref = MMCService.getSecurePreferences(context);
        String oldlogin = securePref.getString(PreferenceKeys.User.USER_EMAIL, "");
        if (login == null || oldlogin.equals(login) || login.length() < 3)
            return;
        securePref.edit().putString(PreferenceKeys.User.USER_EMAIL, login).commit ();
        registerLogin(context, login);
    }

    public static void setLoginToIMEI (Context context)
    {
        SharedPreferences securePref = MMCService.getSecurePreferences(context);
        ReportManager manager = ReportManager.getInstance(context);
        String imei = manager.getDevice().getIMEI();

        String oldlogin = securePref.getString(PreferenceKeys.User.USER_EMAIL, "");
        if (imei == null || oldlogin.equals(imei) || imei.length() < 3)
            return;
        securePref.edit().putString(PreferenceKeys.User.USER_EMAIL, imei).commit ();
        registerLogin(context, imei);
    }

    private static void registerLogin (final Context context, final String login)
    {
        final ReportManager reportmanager = ReportManager.getInstance(context);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    reportmanager.authorizeDevice(login,false);
                    reportmanager.checkPlayServices(context, true);
                } catch (Exception e) {
                }
            }
        }).start();

    }

    public static void start (Context context)
    {
        Global.startService(context);
    }

    // Trigger certain events using the Library
    public static int startEvent (Context context, ActiveEvent event)
    {
        if (!isEventEnabled(context, event.eventType))
            return 2; // Event has not been enabled by server
        if (!isEventPermitted(context, event.eventType, 1))
            return 3; // Event doesnt have enough manifest permissions to run

        JSONObject jobj = new JSONObject ();
        try {
            // Trigger certain events using the Library
            if (event == ActiveEvent.SPEED_TEST)
                jobj.put("mmctype", "speed");
            else if (event == ActiveEvent.UPDATE_EVENT)
                jobj.put("mmctype", "ue");
            else if (event == ActiveEvent.AUDIO_TEST)
                jobj.put("mmctype", "audio");
            else if (event == ActiveEvent.CONNECT_TEST)
                jobj.put("mmctype", "latency");
            else if (event == ActiveEvent.SMS_TEST)
                jobj.put("mmctype", "sms");
            else if (event == ActiveEvent.VIDEO_TEST)
                jobj.put("mmctype", "video");
            else if (event == ActiveEvent.YOUTUBE_TEST)
                jobj.put("mmctype", "youtube");
            else if (event == ActiveEvent.WEB_TEST)
                jobj.put("mmctype", "web");
            else if (event == ActiveEvent.VOICE_QUALITY_TEST)
                jobj.put("mmctype", "vq");
            else if (event == ActiveEvent.COVERAGE_EVENT)
                jobj.put("mmctype", "fill");
            else
                return 1; // Event can't be triggered manually
        }
        catch (Exception e)
        {
            MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "Library startEvent", "exception", e);
        }
        String command = "[" + jobj.toString () + "]";
        Intent mmcintent = new Intent(MMCIntentHandler.COMMAND);
        mmcintent.putExtra(MMCIntentHandler.COMMAND_EXTRA, command);
        context.sendBroadcast(mmcintent);
        return 0;
    }

    // Trigger an extended 'drive test' combining different types of tests on intervals
    public static int startDriveTest (Context context, int minutes, boolean coverage, int speed, int connectivity, int sms, int video, int audio, int web, int vq, int youtube, int ping)
    {
        ReportManager.getInstance(context.getApplicationContext()).startDriveTest(context, minutes, coverage, speed, connectivity,
                sms, video, audio, web, vq, youtube, ping);
        return 0;
    }

    public static boolean isEventEnabled (Context context, EventType eventType)
    {
        String pref;
        if (eventType == EventType.AUDIO_TEST)
        {
            pref = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceKeys.Miscellaneous.AUDIO_URL, null);
            if (pref == null || pref.length() == 0)
                return false;
        }
        else if (eventType == EventType.VIDEO_TEST)
        {
            pref = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceKeys.Miscellaneous.VIDEO_URL, null);
            if (pref == null || pref.length() == 0)
                return false;
        }
        else if (eventType == EventType.YOUTUBE_TEST)
        {
            pref = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceKeys.Miscellaneous.YOUTUBE_VIDEOID, null);
            if (pref == null || pref.length() == 0)
                return false;
        }
        else if (eventType == EventType.WEBPAGE_TEST)
        {
            pref = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceKeys.Miscellaneous.WEB_URL, null);
            if (pref == null || pref.length() == 0)
                return false;
        }
        else if (eventType == EventType.EVT_VQ_CALL)
        {
            pref = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceKeys.Miscellaneous.VOICETEST_SERVICE, null);
            if (pref == null || pref.length() == 0)
                return false;
        }
        return true;
    }

    public static boolean isEventPermitted (Context context, EventType eventType, int trigger)
    {
        if (eventType == EventType.SMS_TEST) {
            if (!PreferenceKeys.getSMSPermissionsAllowed(context, true))
                return false;
        }
        // Allow auto-connection tests?
        else if (eventType == EventType.LATENCY_TEST && trigger == 1) {
            if (context instanceof MMCService) // this check only applies if called from the MMCService
            {
                MMCService svc = (MMCService)context;
                String reason = null;
                int allow = PreferenceManager.getDefaultSharedPreferences(context).getInt(PreferenceKeys.Miscellaneous.AUTO_CONNECTION_TESTS, 1);
                if (PhoneState.isNetworkWifi(context))
                    reason = "on wifi";
                if (!svc.isOnline())
                    reason = "offline";
                if (allow != 1 && !MMCLogger.isDebuggable())
                    reason = "not enabled";
                if (svc.getPhoneStateListener() != null &&
                        (svc.getPhoneState().isCallConnected() == true || svc.getPhoneState().isCallDialing() == true))
                    reason = "phone call";
                if (svc.getUsageLimits().getUsageProfile () <= UsageLimits.MINIMAL)
                    reason = "minimal";
                if (svc.getUsageLimits().getDormantMode () >= 1)
                    reason = "dormant";
                if (reason != null)
                {
                    MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "runLatencyTest cancelled ", reason);
                    return false;
                }
            }
        }
        else if (eventType == EventType.EVT_VQ_CALL) {
            PackageManager pkMan = context.getPackageManager();
            int voiceCallPermissionValue = pkMan.checkPermission("android.permission.CALL_PHONE", context.getPackageName()) | pkMan.checkPermission("android.permission.RECORD_AUDIO", context.getPackageName());
            if (voiceCallPermissionValue != 0)
                return false;
        }
        else if (trigger > 0 && (eventType == EventType.VIDEO_TEST || eventType == EventType.WEBPAGE_TEST || eventType == EventType.AUDIO_TEST || eventType == EventType.YOUTUBE_TEST)) {
            PackageManager pkMan = context.getPackageManager();
            int permissionValue = pkMan.checkPermission("android.permission.SYSTEM_ALERT_WINDOW", context.getPackageName());
            if (permissionValue != 0)
                return false;
        }
        return true;
    }
}

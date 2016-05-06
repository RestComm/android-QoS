package com.cortxt.app.corelib.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.cortxt.app.corelib.MainService;
import com.cortxt.app.corelib.Services.Intents.IntentHandler;
import com.cortxt.app.corelib.UtilsOld.ActiveEvent;
import com.cortxt.app.utillib.DataObjects.DeviceInfo;
import com.cortxt.app.utillib.DataObjects.EventType;
import com.cortxt.app.utillib.DataObjects.PhoneState;
import com.cortxt.app.utillib.DataObjects.QosInfo;
import com.cortxt.app.utillib.Reporters.ReportManager;
import com.cortxt.app.utillib.Utils.Global;
import com.cortxt.app.utillib.Utils.LoggerUtil;
import com.cortxt.app.utillib.Utils.PreferenceKeys;
import com.cortxt.app.utillib.Utils.UsageLimits;
import com.securepreferences.SecurePreferences;

import org.json.JSONObject;

/**
 * Created by bscheurman on 16-01-23.
 */
public class QosAPI {

    public static final String TAG = QosAPI.class.getSimpleName();

    /**
     * Start the MMC/QOS Service. Keep this running to monitor for phone calls and other events
     * This should be called as soon as possible, before other calls to the library
     * If a login has been set with setLogin, it will register in the background if needed
     *
     * @param context pass your context
     */
    public static void start (Context context)
    {
        Global.startService(context);
    }


    /**
     * Get the login name that was set with setLogin
     * The login name can be an email or any type of name, but it should be unique to avoid mixing data with another user
     *
     * @param context pass your context
     * @return the login name that has been set for the user
     */
    public static String getLogin (Context context)
    {
        SharedPreferences securePref = MainService.getSecurePreferences(context);
        String value = securePref.getString (PreferenceKeys.User.USER_EMAIL, null);
        return value;
    }
    /**
     * Get the password name that may have been set with setLogin
     * The password is optional but is used to login to the website to view data
     * If an email is used as a login name, there is no need to set a password because it can be set via email
     * There is also no need to set a password if the user does not need to directly access data on the web site
     *
     * @param context pass your context
     * @return the password that has been set for the user
     */
    public static String getPassword (Context context)
    {
        SharedPreferences securePref = MainService.getSecurePreferences(context);
        String value = securePref.getString (PreferenceKeys.User.USER_PASSWORD, null);
        return value;
    }

    /**
     * Set the login name to register the user with
     * The user/login is used to group data on the server collected by this device
     * It will register with the server if the service has been started
     *
     * @param context pass your context
     * @param login login name can be an email or any type of name, but it should be unique to avoid mixing data with another user
     */
    public static void setLogin (Context context, String login)
    {
        SharedPreferences securePref = MainService.getSecurePreferences(context);
        String oldlogin = securePref.getString(PreferenceKeys.User.USER_EMAIL, "");
        if (login == null || oldlogin.equals(login) || login.length() < 3)
            return;
        securePref.edit().putString(PreferenceKeys.User.USER_EMAIL, login).commit ();
        registerLogin(context, login);
    }

    /**
     * Set the login name to register the user with
     * This version also sets a password for the user
     * The user/login is used to group data on the server collected by this device
     *
     * @param context pass your context
     * @param login login name can be an email or any type of name, but it should be unique to avoid mixing data with another user
     * @param password optional but is used to login to the website to view data
     */
    public static void setLogin (Context context, String login, String password)
    {
        SharedPreferences securePref = MainService.getSecurePreferences(context);
        String oldlogin = securePref.getString(PreferenceKeys.User.USER_EMAIL, "");
        if (login == null || oldlogin.equals(login) || login.length() < 3)
            return;
        securePref.edit().putString(PreferenceKeys.User.USER_EMAIL, login).commit ();
        securePref.edit().putString(PreferenceKeys.User.USER_PASSWORD, password).commit ();
        registerLogin(context, login, password);
    }

    /**
     * Set the login name anonymously to be the device IMEI
     * The user/login is used to group data on the server collected by this device
     *
     * @param context pass your context
     */
    public static void setLoginToIMEI (Context context)
    {
        SharedPreferences securePref = MainService.getSecurePreferences(context);
        ReportManager manager = ReportManager.getInstance(context);
        String imei = manager.getDevice().getIMEI();

        String oldlogin = securePref.getString(PreferenceKeys.User.USER_EMAIL, "");
        if (imei == null || oldlogin.equals(imei) || imei.length() < 3)
            return;
        securePref.edit().putString(PreferenceKeys.User.USER_EMAIL, imei).commit ();
        registerLogin(context, imei);
    }

    // private function used by setLogin to register
    private static void registerLogin (final Context context, final String login, final String password)
    {
        final ReportManager reportmanager = ReportManager.getInstance(context);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    reportmanager.authorizeDevice(login,password,false);
                    reportmanager.checkPlayServices(context, true);
                } catch (Exception e) {
                }
            }
        }).start();

    }

    // private function used by setLogin to register
    private static void registerLogin (final Context context, final String login)
    {
        registerLogin(context, login, null);
    }


    /**
     * Trigger an Active Event manually from the application, generally this is a type of active test or coverage gathering
     * Allowable events are from {@link ActiveEvent}
     *
     * @param context pass your context
     * @param {@link ActiveEvent} type of event to start
     * @return 0 if successful, 1 if event type can't be triggered, 2 if event is not enabled for your user, 3 if app doesnt have necessary permissions
     */
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
            LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "Library startEvent", "exception", e);
        }
        String command = "[" + jobj.toString () + "]";
        Intent mmcintent = new Intent(IntentHandler.COMMAND);
        mmcintent.putExtra(IntentHandler.COMMAND_EXTRA, command);
        context.sendBroadcast(mmcintent);
        return 0;
    }

    /**
     * Trigger an extended 'drive test' combining different types of tests on intervals
     *
     * @param context pass your context
     * @param minutes duration to repeat the tests in minutes
     * @param coverage true to gather coverage during the duration of the test
     * @param speed run speed tests during the test, the value is the number of minutes in betwee
     * @param connectivity run connectivity tests during the test, the value is the number of minutes in between
     * @param sms run sms tests during the test, the value is the number of minutes in between
     * @param video run video streaming tests during the test, the value is the number of minutes in between
     * @param audio run audio streaming tests during the test, the value is the number of minutes in between
     * @param web run web page load tests during the test, the value is the number of minutes in between
     * @param vq run voice quality tests during the test, the value is the number of minutes in between
     * @param youtube run youtube streaming tests during the test, the value is the number of minutes in between
     * @param youtube run ping tests during the test, the value is the number of minutes in betwee
     * @return 0 if successful
     */
    public static int startDriveTest (Context context, int minutes, boolean coverage, int speed, int connectivity, int sms, int video, int audio, int web, int vq, int youtube, int ping)
    {
        ReportManager.getInstance(context.getApplicationContext()).startDriveTest(context, minutes, coverage, speed, connectivity,
                sms, video, audio, web, vq, youtube, ping);
        return 0;
    }

    /**
     * Check whether an Active test EventType is able to run
     *
     * @param context pass your context
     * @param eventType {@link EventType} type of event to check
     * @return true if EventType is enabled to run
     */
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

    /**
     * Check whether an Active test EventType has Android permissions needed to run
     *
     * @param context pass your context
     * @param eventType {@link EventType} type of event to check
     * @param trigger 0 to trigger the event manually, 1 to see if it can trigger the event automatically
     * @return true if EventType is enabled to run
     */
    public static boolean isEventPermitted (Context context, EventType eventType, int trigger)
    {
        if (eventType == EventType.SMS_TEST) {
            if (!PreferenceKeys.getSMSPermissionsAllowed(context, true))
                return false;
        }
        // Allow auto-connection tests?
        else if (eventType == EventType.LATENCY_TEST && trigger == 1) {
            if (context instanceof MainService) // this check only applies if called from the MainService
            {
                MainService svc = (MainService)context;
                String reason = null;
                int allow = PreferenceManager.getDefaultSharedPreferences(context).getInt(PreferenceKeys.Miscellaneous.AUTO_CONNECTION_TESTS, 1);
                if (PhoneState.isNetworkWifi(context))
                    reason = "on wifi";
                if (!svc.isOnline())
                    reason = "offline";
                if (allow != 1 && !LoggerUtil.isDebuggable())
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
                    LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "runLatencyTest cancelled ", reason);
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

    /**
     * Cause the service to close and restart itself
     * You can do this when your App is finished, just as it closes
     * The reason to do this is to reclaim some memory held by the application's process.
     * When the service restarts it will have a new process, otherwise the process will continue to hold all the RAM the application was using
     * @param activity pass the activity that is being closed to close the App
     */
    public static void finishUI (Activity activity)
    {
        SecurePreferences securePrefs = MainService.getSecurePreferences(activity);
        boolean bStoppedService = securePrefs.getBoolean(PreferenceKeys.Miscellaneous.STOPPED_SERVICE, false);
        if (!bStoppedService)
        {
            Intent intent = new Intent(IntentHandler.RESTART_MMC_SERVICE);
            activity.sendBroadcast(intent);
        }
    }

    /**
     * Request all of the Network data the service has obtained
     * It is like a snapshot of all current or last known Network, Cell, Signal and Location data
     * The first call to this may not have up-to-date signal data, but it will begin listening so that subsequent calls will be up-to-date
     * The information will already be up to date during or just after a phone call, active test, or coverage sampling
     * It can be a good idea to keep calling on an interval for updates
     * @param context pass your context
     * @return {@link QosInfo} object containing Network info including {@link QosInfo.CDMAInfo},{@link QosInfo.GSMInfo},{@link QosInfo.LTEInfo},{@link QosInfo.WIFIInfo}
     */
    public static QosInfo getQoSInfo (Context context)
    {
        QosInfo qos = new QosInfo(context);
        return qos;
    }
}

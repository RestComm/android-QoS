package org.restcomm.app.qoslib.Utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import org.restcomm.app.qoslib.MainService;
import org.restcomm.app.qoslib.Services.Intents.IntentHandler;
import org.restcomm.app.qoslib.UtilsOld.ActiveEvent;
import com.restcomm.app.utillib.DataObjects.EventType;
import com.restcomm.app.utillib.DataObjects.PhoneState;
import com.restcomm.app.utillib.Reporters.ReportManager;
import com.restcomm.app.utillib.Utils.Global;
import com.restcomm.app.utillib.Utils.LoggerUtil;
import com.restcomm.app.utillib.Utils.PreferenceKeys;
import com.restcomm.app.utillib.Utils.UsageLimits;
//import com.securepreferences.SecurePreferences;

import org.json.JSONObject;

import java.util.List;


/**
 * QosAPI is the main interface between the client application and the Quality of Service Library
 * These methods allow the app to easily communicate and start and stop the background service
 * using static methods.
 * The main tasks to be performed using this class are:
 * <ul>
 * <li>start the service
 * <li>set the login, and in some cases a password. This will register with the server.
 * <li>Request QoS information such as all the mobile network measurements
 * <li>Trigger events such as speed tests, which gather data and send to the server
 * <li>Check for permissions
 * </ul>
 * <p>
 *
 * @author      Brad Scheurman
 * @version     %I%, %G%
 * @since       1.0
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
    public static void start (Context context, boolean bFromUI)
    {
        Global.startService(context, bFromUI);
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
                    //reportmanager.checkPlayServices(context, true);
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
//    public static void finishUI (Activity activity)
//    {
//        SecurePreferences securePrefs = MainService.getSecurePreferences(activity);
//        boolean bStoppedService = securePrefs.getBoolean(PreferenceKeys.Miscellaneous.STOPPED_SERVICE, false);
//        if (!bStoppedService)
//        {
//            Intent intent = new Intent(IntentHandler.RESTART_MMC_SERVICE);
//            activity.sendBroadcast(intent);
//        }
//    }

    /**
     * Request a snapshot of all of the Network data the service has obtained
     * Network, Cell, Signal and Location data
     * The first call to this may not have up-to-date signal data, but it will begin listening so that subsequent calls will be up-to-date
     * The information will already be up to date during or just after a phone call, active test, or coverage sampling
     * For best results, call repeatedly on an interval
     * @param context pass your context
     * @return {@link QosInfo} where The most relevant information is contained in the connectedNetwork {@link QosInfo.NetworkInfo}
     * */
    public static QosInfo getQoSInfo (Context context)
    {
        QosInfo qos = new QosInfo(context);
        return qos;
    }

    public static void showHistory (final Activity activity, EventType[] eventTypes) {
        try {
            Intent intent = new Intent(activity, EventHistory.class);
            int[] ieventtypes = new int[eventTypes.length];
            for (int i=0; i<eventTypes.length;i++)
                ieventtypes[i] = eventTypes[i].getIntValue();
            intent.putExtra("eventtypes", ieventtypes);
            activity.startActivity(intent);
        }
        catch (Exception e)
        {

        }
    }
    public static void showQoSPanel (final Activity activity) {
        try {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
            CharSequence qosInfo = getQosInfo (activity);
            builder1.setMessage(qosInfo);
            builder1.setTitle("QOS Info");
            builder1.setCancelable(true);
            final AlertDialog alert11 = builder1.create();
            alert11.show();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CharSequence qosInfo = getQosInfo(activity);
                    if (alert11.isShowing()) {
                        try {
                            alert11.setMessage(qosInfo);
                            handler.postDelayed(this, 1000);
                        } catch (Exception e) {
                        }
                    }
                }
            }, 1000);

        } catch (Exception e) {
            LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "CreateDevInfoAlertDialog", "exeption", e);
        }
    }

    public static CharSequence getQosInfo (Activity activity)
    {
        // Request all known Network information from QoS library
        QosInfo info = QosAPI.getQoSInfo(activity);
        if (info.connectedNetwork == null)
            return "";
        // The basic info as a string
        String strInfo = info.connectedNetwork.getType() + "\n";
        strInfo += info.connectedNetwork.getSignalDetails(true, true) + "\n";
        strInfo += info.connectedNetwork.getQualityDetails(true, true) + "\n";
        strInfo += info.connectedNetwork.getIdentifier() + "\n";

        return strInfo;

    }

    public static String watchActivity = null;
    public static long lastWatchActivity = 0;
    public static void watchHostApp (Context context, Class launchActivity, boolean bWatch)
    {
        if (bWatch && launchActivity != null)
        {
            LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "watchHostApp", launchActivity.getCanonicalName());
            watchActivity = launchActivity.getCanonicalName();
        }
        else
            watchActivity = null;
        PreferenceKeys.getSecurePreferences(context).edit().putString("PREF_WATCH_ACTIVITY", watchActivity).commit();
    }


    public static void checkHostApp (Context context)
    {
        watchActivity = PreferenceKeys.getSecurePreferences(context).getString("PREF_WATCH_ACTIVITY", watchActivity);
        if (watchActivity != null)
        {
            if (System.currentTimeMillis() - lastWatchActivity < 60000 || SystemClock.elapsedRealtime() > 300000)
                return;
            lastWatchActivity = System.currentTimeMillis();
            //int app = Global.getAppImportance (context.getPackageName(), context);
            boolean isRunning = isRunning(context, context.getPackageName());
            // 1 indicates foreground app and 2 indicates background (but not service-only)
            if (isRunning)
                return;
            try {
                // otherwise, launch the apps main activity
                Intent intent = new Intent();
                intent.setClassName(context, watchActivity);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "checkHostApp mode=", "start " + watchActivity);
            }
            catch (Exception e)
            {
                LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "checkHostApp", "exception", e);
            }
        }
    }

    static public boolean isMMCServiceRunning() {
        return Global.isMMCServiceRunning();
    }
    static public boolean isRunning(Context context, String PackageName){
        // Get the Activity Manager
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        // Get a list of running tasks, we are only interested in the last one,
        // the top most so we give a 1 as parameter so we only get the topmost.
        List< ActivityManager.RunningTaskInfo > rtasks = manager.getRunningTasks(10000);
        // Get the info we need for comparison.
        for (ActivityManager.RunningTaskInfo task:rtasks) {
            ComponentName c = task.baseActivity;
            if (c != null && c.toString().indexOf (PackageName) >= 0)
                return true;
        }

//        List<ActivityManager.AppTask> tasks = manager.getAppTasks();
//
//        // Get the info we need for comparison.
//        for (ActivityManager.AppTask task:tasks) {
//            ComponentName c = task.getTaskInfo().origActivity;
//            if (c == null && c.equals(watchActivity))
//                return true;
//        }

        // If not then our app is not on the foreground.
        return false;
    }
}

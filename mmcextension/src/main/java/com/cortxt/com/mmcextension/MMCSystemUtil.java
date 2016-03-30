package com.cortxt.com.mmcextension;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.Toast;


import com.cortxt.app.mmcutility.DataObjects.MMCDevice;
import com.cortxt.app.mmcutility.Utils.Global;
import com.cortxt.app.mmcutility.Utils.MMCLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by bscheurman on 15-12-08.
 */
public class MMCSystemUtil {

    private static String privapp = "priv-app";
    public static final String TAG = MMCSystemUtil.class.getSimpleName();

    // Check the version of the Service Mode module and offer to upgrade it if a new one is bundled
    private static Activity checkActivity = null;
    public static void checkSvcModeVersion ( Activity activity) {

       return;
    }

    // Response: Check the version of the Service Mode module and offer to upgrade it if a new one is bundled
    public static void onSvcModeVersion ( int version) {
    }
    public static void promptInstallSystem (final Activity activity, boolean skipPrompt) {
    }
    public static void promptRemoveSystem (final Activity activity)
    {
    }

    public static boolean isServiceModeAllowed (Context context)
    {
       return false;
    }

    public static boolean isServiceModeEnabled ()
    {
        return false;
    }

    public static void startRilReader (Context context, boolean bStart, boolean useLogcat)
    {
    }

}

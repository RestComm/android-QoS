package org.restcomm.app.mmcextension;

import android.app.Activity;
import android.content.Context;

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
    public static void checkFirebaseRegistration (Context context)
    {
    }

}

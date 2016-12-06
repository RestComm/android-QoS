package com.restcomm.app.utillib.Reporters.WebReporter;

import android.util.Pair;

import com.restcomm.app.utillib.Utils.LoggerUtil;

import java.net.URL;
import java.util.LinkedList;

/**
 * Created by bscheurman on 16-07-29.
 */
public class UserRequest {
    public static final String TAG = NetworkRequest.class.getSimpleName();
    private static final String END_POINT = "/api/user";

    private static final String KEY_MCC = "mcc";
    private static final String KEY_MNC = "mnc";
    private static final String KEY_CARRIER = "carrier";
    private static final String KEY_SID = "sid";

    public static URL getURL(String host, String apiKey, int userid) {

        LinkedList<Pair> params = new LinkedList<Pair>();
        params.add(new Pair(WebReporter.JSON_API_KEY, apiKey));
        params.add(new Pair("id", userid));

        String paramsString = WebReporter.URLEncodedFormat(params);
        try {
            return new URL(host + END_POINT + "?" + paramsString);
        }
        catch (Exception e) {
            LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "constructor", "invalid uri", e);
            throw new RuntimeException(e);
        }
    }
}
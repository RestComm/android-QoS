/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * For questions related to commercial use licensing, please contact sales@telestax.com.
 *
 */

package org.restcomm.app.utillib.Reporters.WebReporter;

import android.util.Pair;

import org.restcomm.app.utillib.Utils.LoggerUtil;

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
package com.restcomm.app.utillib.Reporters.WebReporter;

import com.restcomm.app.utillib.DataObjects.DeviceInfo;
import com.restcomm.app.utillib.Utils.LoggerUtil;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class represents the registation of a device api call to the server
 * @author brad scheurman
 *
 */
public class RegistrationRequest  {


    private static final String TAG = RegistrationRequest.class.getSimpleName();
    private static final String END_POINT = "/api/devices/register";

    public static HttpURLConnection POSTConnection(String host, DeviceInfo device, String email, String password, boolean share) throws Exception
    {
        URL url = new URL(host + END_POINT);
        String message = toJSON(device, email, password, share);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setFixedLengthStreamingMode(message.getBytes().length);

        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

        LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "authorizeDevice", url.toString());

        //open
        conn.connect();

        //setup send
        OutputStream os = new BufferedOutputStream(conn.getOutputStream());
        os.write(message.getBytes());
        //clean up
        os.flush();
        return conn;

    }

    /**
     * @return String the json representation of the body of the request.
     */
    public static String toJSON(DeviceInfo device, String email, String password, boolean share) {
        String json = "";
        HashMap<String, String> phoneProperties = device.getProperties();
        if(phoneProperties != null) {
          JSONObject data = new JSONObject(phoneProperties);
          try {
            data.put("login", email);
            data.put("share", share);
              if (password != null)
              {
                  data.put("password", password);
              }
            json = data.toString();
          } catch (JSONException e) {
            LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "toJSON", e.getMessage());
          }
        }
        return json;
    }

}

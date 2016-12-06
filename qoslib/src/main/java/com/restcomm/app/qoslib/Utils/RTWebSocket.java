package com.restcomm.app.qoslib.Utils;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.restcomm.app.qoslib.MainService;
import com.restcomm.app.utillib.Utils.CommonIntentBundleKeysOld;
import com.restcomm.app.utillib.Reporters.WebReporter.WebReporter;
import com.restcomm.app.utillib.Utils.LoggerUtil;
import com.restcomm.app.utillib.Utils.PreferenceKeys;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import com.restcomm.app.qoslib.R;
/**
 * Created by bscheurman on 16-02-07.
 */

public class RTWebSocket {

    private MainService context;
    public static final String TAG = RTWebSocket.class.getSimpleName();

    public RTWebSocket(MainService contxt)
    {
        context = contxt;
    }
    public void runWebSocket (boolean bStart)
    {
        if (bStart)
        {
            connectWebSocket();
        }
        else
        {
            closeWebSocket();
        }
    }
    private WebSocketClient mWebSocketClient;
    public boolean wsConnected = false;
    private void closeWebSocket() {
        URI uri;
        if (mWebSocketClient != null) {
            mWebSocketClient.close();
        }
        else
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PreferenceKeys.Miscellaneous.WEBSOCKET_RUNNING, false).commit();

    }
    private void connectWebSocket() {
        URI uri;
        try {
            String ws_uri = context.getString(R.string.MMC_WEBSOCKET_URL);
            uri = new URI(ws_uri);
        } catch (URISyntaxException e) {
            //e.printStackTrace();
            return;
        }

        String apiKey = context.getApiKey(context);

        HashMap<String,String> header = new HashMap<String,String> ();
        header.put("apiKey", apiKey);
        mWebSocketClient = new WebSocketClient(uri, new Draft_17(), header, 10000) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                //Log.i("Websocket", "Opened");
                LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "connectWebSocket", "Websocket opened ");
                //mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
                PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PreferenceKeys.Miscellaneous.WEBSOCKET_RUNNING, true).commit();
                wsConnected = true;

                // Force intents to be resent for last known location, signal and cell
                context.getEventManager().signalSnapshot(null);

                context.getIntentDispatcher().updateNetwork ();

            }

            @Override
            public void onMessage(String s) {
                final String message = s;

            }

            @Override
            public void onClose(int i, String s, boolean b) {
                LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "connectWebSocket", "Websocket closed " + s);

                // close web socket
                PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PreferenceKeys.Miscellaneous.WEBSOCKET_RUNNING, false).commit();
                mWebSocketClient = null;
                wsConnected = false;
                mWebSocketQueue.clear();
                webSocketThread = null;

            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
                LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "connectWebSocket", "onError ", e);
            }
        };

        LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "connectWebSocket", "get SSL");
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance( "TLS" );
            sslContext.init( null, null, null ); // will use java's default key and trust store which is sufficient unless you deal with self-signed certificates

            SSLSocketFactory factory = sslContext.getSocketFactory();// (SSLSocketFactory) SSLSocketFactory.getDefault();

            mWebSocketClient.setSocket(factory.createSocket());

            mWebSocketClient.connect();
            LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "connectWebSocket", "called connect");
        } catch (Exception e)
        {
            LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "connectWebSocket", "Exception: ", e);
        }
    }

    private ConcurrentLinkedQueue<String> mWebSocketQueue = new ConcurrentLinkedQueue<String>();
    private Thread webSocketThread = null;

    public boolean queueSocketSend (String message)
    {
        mWebSocketQueue.add(message);
        // Ensure one worker thread to run each test in the queue, then the thread ends when empty
        if (webSocketThread == null) {
            LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "queueSocketSend", "begin worker thread");
            webSocketThread = new Thread() {
                public void run() {
                    while (wsConnected) {
                        try {
                            String wsMessage = mWebSocketQueue.poll();

                            if (wsMessage != null && wsMessage.length() > 1) {
                                sendSocketMessage(wsMessage);
                            }
                            else
                                sleep (500);
                        }
                        catch (Exception e)
                        {
                            LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "queueSocketSend", "exception", e);
                        }
                    }
                    webSocketThread = null;
                    LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "queueSocketSend", "end worker thread");
                }
            };
            webSocketThread.start();
        }
        return true;
    }

    public int sendIntentToWebSocket (String action, Bundle intentExtras)
    {
        if (mWebSocketClient != null && wsConnected)
        {
            if (action.equals(CommonIntentBundleKeysOld.ACTION_CONNECTION_UPDATE) || intentExtras == null)
                return 0; // writing intents about data connections can cause a vicious cycle, as writing them causes new intents which will be written
            if (action.equals("android.intent.action.ANY_DATA_STATE"))
                return 0;  // this is great information, but could potentially contain a password and get us in trouble
            //if (action.equals(CommonIntentBundleKeysOld.ACTION_GPS_STATUS_UPDATE))
            //	return 0;
            //Retrieve api key if it exists
            String apiKey = context.getApiKey(context);
            String actionKey = "";
            int pos = action.lastIndexOf('.');
            actionKey = action.substring(pos+1);
            String message = null;
            try {
                JSONObject jobj = new JSONObject();

                jobj.put("url", "/api/devices/feed");
                jobj.put("callbackid", "x");
                JSONObject jparams = new JSONObject();
                jparams.put(WebReporter.JSON_API_KEY, apiKey);
                jparams.put("type", "intent");
                jparams.put("intent", actionKey);

                if (action.equals(CommonIntentBundleKeysOld.ACTION_CONNECTION_UPDATE))
                {
//					String connectString = intentExtras.getString(CommonIntentBundleKeysOld.KEY_UPDATE_CONNECTION);
//					String[] vals = connectString.split(",");
//					jparams.put("TYPE", vals[0]);
//					jparams.put("STATE", vals[1]);
//					jparams.put("ACT", vals[2]);
                }
                else {
                    for (String key : intentExtras.keySet()) {
                        Object value = intentExtras.get(key);
                        jparams.put(key, value);
                        if (key.equals(CommonIntentBundleKeysOld.EXTRA_SENDSOCKET) && intentExtras.getBoolean(key) == false)
                            return 1;
                    }
                }
                jobj.put ("params", jparams);
                message = jobj.toString();
            }
            catch (Exception e)
            {
                LoggerUtil.logToFile(LoggerUtil.Level.WTF, TAG, "sendIntentToWebSocket", "json exception: ", e);
            }

            if (message != null)
                queueSocketSend(message);

        }
        return 0;
    }
    public void sendSocketMessage(String message) {
        LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "sendSocketMessage", message);

        try {
            if (mWebSocketClient != null) {
                mWebSocketClient.send(message);
            }
        }
        catch (Exception e)
        {
            LoggerUtil.logToFile(LoggerUtil.Level.WTF, TAG, "sendSocketMessage", "send exception: ", e);
        }
    }

    public boolean isConnected () { return wsConnected;}

}

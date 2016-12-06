package com.telestax.restcomm_helloworld;

import android.content.Intent;
//import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.restcomm.app.qoslib.Utils.QosAPI;
import com.restcomm.app.utillib.DataObjects.EventType;
//import com.cortxt.app.utillib.Utils.FirebaseInvite;

import java.util.HashMap;


import org.restcomm.android.sdk.RCConnection;
import org.restcomm.android.sdk.RCConnectionListener;
import org.restcomm.android.sdk.RCDevice;
import org.restcomm.android.sdk.RCDeviceListener;
import org.restcomm.android.sdk.RCPresenceEvent;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MainActivity extends FragmentActivity implements RCDeviceListener, RCConnectionListener, OnClickListener,
        ServiceConnection {

    private RCDevice device;
    boolean serviceBound = false;

    private RCConnection connection, pendingConnection;
    private HashMap<String, Object> params;
    private static final String TAG = "MainActivity";

    //private GLSurfaceView videoView;
    private boolean videoReady = false;
    private EditText editServer, editUser, editPwd, editDial;
    //VideoTrack localVideoTrack, remoteVideoTrack;
    //VideoRenderer localVideoRenderer, remoteVideoRenderer;

    // Local preview screen position before call is connected.
    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;
    // Local preview screen position after call is connected.
    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 2;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 25;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;
    //private ScalingType scalingType;

    // UI elements
    Button btnDial;
    Button btnAnswer;
    Button btnHangup;
    Button btnApply;
    Button btnInfo;
    Button btnHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set window styles for fullscreen-window size. Needs to be done before
        // adding content.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.activity_main);

        // initialize UI
        btnDial = (Button)findViewById(R.id.button_dial);
        btnDial.setOnClickListener(this);
        btnDial.setEnabled(false);
        btnAnswer = (Button)findViewById(R.id.button_answer);
        btnAnswer.setOnClickListener(this);
        btnHangup = (Button)findViewById(R.id.button_hangup);
        btnHangup.setOnClickListener(this);
        btnHangup.setEnabled(false);
        btnApply = (Button)findViewById(R.id.button_apply);
        btnApply.setOnClickListener(this);
        btnInfo = (Button)findViewById(R.id.button_info);
        btnInfo.setOnClickListener(this);
        btnHistory = (Button)findViewById(R.id.button_history);
        btnHistory.setOnClickListener(this);

        editServer = (EditText)findViewById(R.id.editServer);
        editUser = (EditText)findViewById(R.id.editUser);
        editPwd = (EditText)findViewById(R.id.editPwd);
        editDial= (EditText)findViewById(R.id.editDial);

        String prefServer = PreferenceManager.getDefaultSharedPreferences(this).getString("PREF_SERVER", null);
        if (prefServer != null)
            editServer.setText (prefServer);
        String prefUser = PreferenceManager.getDefaultSharedPreferences(this).getString("PREF_USER", null);
        if (prefUser != null)
            editUser.setText (prefUser);
        String prefPwd = PreferenceManager.getDefaultSharedPreferences(this).getString("PREF_PWD", null);
        if (prefPwd != null)
            editPwd.setText(prefPwd);
        String prefDial = PreferenceManager.getDefaultSharedPreferences(this).getString("PREF_DIAL", null);
        if (prefDial != null)
            editDial.setText(prefDial);

        /*
        RCClient.setLogLevel(Log.VERBOSE);
        RCClient.initialize(getApplicationContext(), new RCClient.RCInitListener() {
            public void onInitialized() {
                Log.i(TAG, "RCClient initialized");
            }

            public void onError(Exception exception) {
                Log.e(TAG, "RCClient initialization error");
            }
        });

        params = new HashMap<String, Object>();
        // update the IP address to your Restcomm instance
        params.put("pref_proxy_domain", "sip:" + editServer.getText().toString() + ":5060"); // :5080
        params.put("pref_sip_user", editUser.getText().toString());
        params.put("pref_sip_password", editPwd.getText().toString());
        params.put("turn-enabled", true);
        params.put("turn-url", "https://service.xirsys.com/ice");
        params.put("turn-username", "atsakiridis");
        params.put("turn-password", "4e89a09e-bf6f-11e5-a15c-69ffdcc2b8a7");

        device = RCClient.createDevice(params, this);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        // we don't have a separate activity for the calls, so use the same intent both for calls and messages
        device.setPendingIntents(intent, intent);
        device.listen();

        // Make sure Qos server is started
        QosAPI.start(this, true);
        // make sure user is registered with the QoS server
        String login = editUser.getText().toString() + "@" + editServer.getText().toString();
        QosAPI.setLogin(this, login);
        // Check Firebase invites before proceeding from SplashScreen
        FirebaseInvite firebaseInvite = new FirebaseInvite(this);
        firebaseInvite.setOnResponseListener(new FirebaseInvite.OnResponseListener() {
            @Override
            public void onResponse(final FirebaseInvite invite) {
                // By the time the Splash screen delay is done, we should have the invitation result
                if (invite.invited) {
                    LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "OnResponseListener", "Firebase Invite: " + invite.url);
                    if (invite.url.indexOf ("simpleshare") > 0) {
                        Uri uri = Uri.parse (invite.url);
                        String evtid = uri.getQueryParameter("id");
                        final long iEventID = Long.parseLong(evtid);
                        String evttype= uri.getQueryParameter("type");
                        int iEventType = Integer.parseInt(evttype);

                        Handler invitehandler = new Handler ();
                        invitehandler.postDelayed(new Runnable () {
                            @Override
                            public void run() {
                                Intent intent = new Intent(MainActivity.this, EventDetailWeb.class);
                                intent.putExtra("url", invite.deepLink);
                                intent.putExtra("eventId", 0);
                                startActivity(intent);
                            }
                        }, 2000);

                    }
                }
            }
        });
        */
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // The activity is about to become visible.
        Log.i(TAG, "%% onStart");

        bindService(new Intent(this, RCDevice.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
        Log.i(TAG, "%% onStop");

        // Unbind from the service
        if (serviceBound) {
            //device.detach();
            unbindService(this);
            serviceBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
        Log.i(TAG, "%% onDestroy");
        device.release();
        /*
        RCClient.shutdown();
        device = null;
        */
    }

    // Callbacks for service binding, passed to bindService()
    @Override
    public void onServiceConnected(ComponentName className, IBinder service)
    {
        Log.i(TAG, "%% onServiceConnected");
        // We've bound to LocalService, cast the IBinder and get LocalService instance
        RCDevice.RCDeviceBinder binder = (RCDevice.RCDeviceBinder) service;
        device = binder.getService();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        HashMap<String, Object> params = new HashMap<String, Object>();
        // we don't have a separate activity for the calls and messages, so let's use the same intent both for calls and messages
        params.put(RCDevice.ParameterKeys.INTENT_INCOMING_CALL, intent);
        params.put(RCDevice.ParameterKeys.INTENT_INCOMING_MESSAGE, intent);

        //params.put(RCDevice.ParameterKeys.SIGNALING_DOMAIN, "sip:" + editServer.getText().toString() + ":5060"); // :5080
        params.put(RCDevice.ParameterKeys.SIGNALING_DOMAIN, editServer.getText().toString()); // :5080
        params.put(RCDevice.ParameterKeys.SIGNALING_USERNAME, editUser.getText().toString());
        params.put(RCDevice.ParameterKeys.SIGNALING_PASSWORD, editPwd.getText().toString());
        params.put(RCDevice.ParameterKeys.MEDIA_TURN_ENABLED, true);
        params.put(RCDevice.ParameterKeys.MEDIA_ICE_URL, "https://service.xirsys.com/ice");
        params.put(RCDevice.ParameterKeys.MEDIA_ICE_USERNAME, "atsakiridis");
        params.put(RCDevice.ParameterKeys.MEDIA_ICE_PASSWORD, "4e89a09e-bf6f-11e5-a15c-69ffdcc2b8a7");
        params.put(RCDevice.ParameterKeys.SIGNALING_SECURE_ENABLED, true);

        if (!device.isInitialized()) {
            device.initialize(getApplicationContext(), params, this);
            device.setLogLevel(Log.VERBOSE);
        }


        // Make sure Qos server is started
        QosAPI.start(this, true);
        // make sure user is registered with the QoS server
        String login = editUser.getText().toString() + "@" + editServer.getText().toString();
        QosAPI.setLogin(this, login);
        // Check Firebase invites before proceeding from SplashScreen

        /*
        FirebaseInvite firebaseInvite = new FirebaseInvite(this);
        firebaseInvite.setOnResponseListener(new FirebaseInvite.OnResponseListener() {
            @Override
            public void onResponse(final FirebaseInvite invite) {
                // By the time the Splash screen delay is done, we should have the invitation result
                if (invite.invited) {
                    LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "OnResponseListener", "Firebase Invite: " + invite.url);
                    if (invite.url.indexOf ("simpleshare") > 0) {
                        Uri uri = Uri.parse (invite.url);
                        String evtid = uri.getQueryParameter("id");
                        final long iEventID = Long.parseLong(evtid);
                        String evttype= uri.getQueryParameter("type");
                        int iEventType = Integer.parseInt(evttype);

                        Handler invitehandler = new Handler ();
                        invitehandler.postDelayed(new Runnable () {
                            @Override
                            public void run() {
                                Intent intent = new Intent(MainActivity.this, EventDetailWeb.class);
                                intent.putExtra("url", invite.deepLink);
                                intent.putExtra("eventId", 0);
                                startActivity(intent);
                            }
                        }, 2000);

                    }
                }
            }
        });
        */

        serviceBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0)
    {
        Log.i(TAG, "%% onServiceDisconnected");
        serviceBound = false;
    }

//    private void videoContextReady()
//    {
//        videoReady = true;
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_qoslog) {
            Intent intent = new Intent (this, QosLog.class);
            startActivity (intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // UI Events
    public void onClick(View view) {
        if (view.getId() == R.id.button_dial) {
            if (connection != null) {
                Log.e(TAG, "Error: already connected");
                return;
            }
            String dial = editDial.getText().toString();
            if (dial.indexOf("@") == -1)
                dial = dial + "@" + editServer.getText().toString ();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("PREF_DIAL", dial).commit();

            HashMap<String, Object> connectParams = new HashMap<String, Object>();
            // CHANGEME: update the IP address to your Restcomm instance. Also, you can update the number
            // from '1235' to any Restcomm application you wish to reach
            //connectParams.put("username", "sip:+1235@cloud.restcomm.com");
            connectParams.put("username", "sip:+" + dial + ":5060");
            connectParams.put("video-enabled", false);

            connectParams.put("local-video", findViewById(R.id.local_video_layout));
            connectParams.put("remote-video", findViewById(R.id.remote_video_layout));

            // if you want to add custom SIP headers, please uncomment this
            //HashMap<String, String> sipHeaders = new HashMap<>();
            //sipHeaders.put("X-SIP-Header1", "Value1");
            //connectParams.put("sip-headers", sipHeaders);

            connection = device.connect(connectParams, this);
            if (connection == null) {
                Log.e(TAG, "Error: error connecting");
                return;
            }
            //device.updateParams(params);
        } else if (view.getId() == R.id.button_hangup) {
            if (connection == null) {
                Log.e(TAG, "Error: not connected");
            }
            else {
                connection.disconnect();
                connection = null;
                pendingConnection = null;
            }
        }
        else if (view.getId() == R.id.button_apply) {
            if (connection == null) {
                Log.e(TAG, "Error: not connected");
            } else {
                connection.disconnect();
                connection = null;
                pendingConnection = null;
            }

            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("PREF_SERVER", editServer.getText().toString()).commit();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("PREF_USER", editUser.getText().toString()).commit();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("PREF_PWD", editPwd.getText().toString()).commit();

            // If user setting changed, register with the QoS server
            String login = editUser.getText().toString() + "@" + editServer.getText().toString();
            QosAPI.setLogin(this, login);

            params = new HashMap<String, Object>();
            // CHANGEME: update the IP address to your Restcomm instance
            //params.put("pref_proxy_ip", editServer.getText());
            //params.put("pref_proxy_port", "5080");
            params.put(RCDevice.ParameterKeys.SIGNALING_DOMAIN, "sip:" + editServer.getText().toString() + ":5060");// + ":5080");
            params.put(RCDevice.ParameterKeys.SIGNALING_USERNAME, editUser.getText().toString());
            params.put(RCDevice.ParameterKeys.SIGNALING_PASSWORD, editPwd.getText().toString());
            device.updateParams(params);
            /*
            device = RCClient.createDevice(params, this);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            // we don't have a separate activity for the calls, so use the same intent both for calls and messages
            device.setPendingIntents(intent, intent);

            device.listen();
            */

        }else if (view.getId() == R.id.button_answer) {
            if (this.pendingConnection == null) {
                Log.e(TAG, "Error: not ringing");
                return;
            }
            HashMap<String, Object> connectParams = new HashMap<String, Object>();
            connectParams.put("username", "sip:dane@184.73.55.15:5080");
            connectParams.put("video-enabled", false);
            this.pendingConnection.accept(connectParams);
            connection = this.pendingConnection;
            if (connection == null) {
                Log.e(TAG, "Error: error connecting");
                //sendRCNoConnectionIntent(connectParams.get("username").toString(), device.getReachability());
                return;
            }
        }
        else if (view.getId() == R.id.button_info) {
            QosAPI.showQoSPanel(this);
        }
        else if (view.getId() == R.id.button_history) {
            EventType[] eventTypes = {EventType.SIP_CONNECT, EventType.SIP_DISCONNECT, EventType.SIP_DROP, EventType.SIP_UNANSWERED, EventType.SIP_CALLFAIL};
            QosAPI.showHistory(this, eventTypes);
//            try {
//                Intent intent = new Intent(this, EventHistory.class);
//                int[] ieventtypes = new int[eventTypes.length];
//                for (int i=0; i<eventTypes.length;i++)
//                    ieventtypes[i] = eventTypes[i].getIntValue();
//                intent.putExtra("eventtypes", ieventtypes);
//                this.startActivity(intent);
//            }
//            catch (Exception e)
//            {
//
//            }
        }
    }


    // RCDevice Listeners
    public void onStartListening(RCDevice device)
    {

    }

    public void onStopListening(RCDevice device)
    {

    }

    public void onStopListening(RCDevice device, int errorCode, String errorText)
    {
        Log.i(TAG, errorText);
    }

    public boolean receivePresenceEvents(RCDevice device)
    {
        return false;
    }

    public void onPresenceChanged(RCDevice device, RCPresenceEvent presenceEvent)
    {

    }

    public void onIncomingConnection(RCDevice device, RCConnection connection)
    {
        Log.i(TAG, "Connection arrived");
        this.pendingConnection = connection;
    }

    public void onIncomingMessage(RCDevice device, String message, HashMap<String, String> parameters)
    {
        final HashMap<String, String> finalParameters = parameters;
        final String finalMessage = message;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String newText = finalParameters.get("username") + ": " + finalMessage + "\n";
                Log.i(TAG, "Message arrived: " + newText);
            }
        });
    }

    public void onConnectivityUpdate(RCDevice device, RCConnectivityStatus connectivityStatus)
    {

    }

    public void onDigitSent(RCConnection connection, int statusCode, String statusText)
    {

    }

    // RCConnection Listeners
    public void onConnecting(RCConnection connection)
    {
        Log.i(TAG, "RCConnection connecting");
        btnHangup.setEnabled(true);
    }

    public void onConnected(RCConnection connection, HashMap<String, String> customHeaders)
    {
        Log.i(TAG, "RCConnection connected");
    }

    public void onDisconnected(RCConnection connection)
    {
        Log.i(TAG, "RCConnection disconnected");
        this.connection = null;
        pendingConnection = null;
        btnHangup.setEnabled(false);
//        // reside local renderer to take up all screen now that the call is over
//        VideoRendererGui.update(localRender,
//                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
//                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType, true);
//
//        if (localVideoTrack != null) {
//
//            localVideoTrack.removeRenderer(localVideoRenderer);
//            localVideoTrack = null;
//        }
//
//        if (remoteVideoTrack != null) {
//            remoteVideoTrack.removeRenderer(remoteVideoRenderer);
//            remoteVideoTrack = null;
//        }
    }
    public void onError(RCConnection connection, int errorCode, String errorText)
    {

    }

    public void onDisconnected(RCConnection connection, int errorCode, String errorText) {

        Log.i(TAG, errorText);
        this.connection = null;
        pendingConnection = null;
    }

    public void onCancelled(RCConnection connection) {
        Log.i(TAG, "RCConnection cancelled");
        this.connection = null;
        pendingConnection = null;
    }

    public void onDeclined(RCConnection connection) {
        Log.i(TAG, "RCConnection declined");
        this.connection = null;
        pendingConnection = null;
    }

    public void onStartListening(RCDevice device, RCDeviceListener.RCConnectivityStatus connectivityStatus)
    {

    }

    public void onMessageSent(RCDevice device, int statusCode, String statusText)
    {

    }

    public void onReleased(RCDevice device, int statusCode, String statusText)
    {
        btnDial.setEnabled(false);
    }

    public void onInitialized(RCDevice device, RCDeviceListener.RCConnectivityStatus connectivityStatus, int statusCode, String statusText)
    {
        btnDial.setEnabled(true);
    }

    public void onInitializationError(int errorCode, String errorText)
    {
        btnDial.setEnabled(true);
    }

    public void onLocalVideo(RCConnection connection)
    {

    }

    public void onRemoteVideo(RCConnection connection)
    {

    }
//    public void onReceiveLocalVideo(RCConnection connection, VideoTrack videoTrack) {
//        Log.v(TAG, "onReceiveLocalVideo(), VideoTrack: " + videoTrack);
//        if (videoTrack != null) {
//            //show media on screen
//            videoTrack.setEnabled(true);
//            localVideoRenderer = new VideoRenderer(localRender);
//            videoTrack.addRenderer(localVideoRenderer);
//            localVideoTrack = videoTrack;
//        }
//    }

//    public void onReceiveRemoteVideo(RCConnection connection, VideoTrack videoTrack) {
//        Log.v(TAG, "onReceiveRemoteVideo(), VideoTrack: " + videoTrack);
//        if (videoTrack != null) {
//            //show media on screen
//            videoTrack.setEnabled(true);
//            remoteVideoRenderer = new VideoRenderer(remoteRender);
//            videoTrack.addRenderer(remoteVideoRenderer);
//
//            VideoRendererGui.update(remoteRender,
//                    REMOTE_X, REMOTE_Y,
//                    REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);
//            VideoRendererGui.update(localRender,
//                    LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED,
//                    LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED,
//                    VideoRendererGui.ScalingType.SCALE_ASPECT_FIT, true);
//
//            remoteVideoTrack = videoTrack;
//        }
//    }


//
//    private void showInfo () {
//        try {
//            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
//            // Request all known Network information from QoS library
//            QosInfo info = QosAPI.getQoSInfo(this);
//            // The basic info as a string
//            String devInfo = info.toString();
//            // Network type specific info is divided into objects for CDMA, GSM, LTE, WiFi
//            // CDMAInfo is null unless device is connected to a CDMA network, can co-exist with LTE
//            if (info.CDMAInfo != null)
//                devInfo += info.CDMAInfo.toString();
//            // GSM_2GInfo is null unless connected to 2G GSM Network, it is null in LTE
//            if (info.GSMInfo != null)
//                devInfo += info.GSMInfo.toString();
//            // GSMInfo is null unless connected to 3G GSM Network, it is null in LTE
//            if (info.WCDMAInfo != null)
//                devInfo += info.WCDMAInfo.toString();
//            // LTEInfo is null unless on LTE
//            if (info.LTEInfo != null)
//                devInfo += info.LTEInfo.toString();
//            // WiFiInfo is null unless connected to WiFi
//            if (info.WiFiInfo != null)
//                devInfo += info.WiFiInfo.toString();
//
//            devInfo = devInfo.replace("\n", "<br>");
//            builder1.setMessage(Html.fromHtml(devInfo));
//            builder1.setTitle("QOS Info");
//            builder1.setCancelable(true);
//            AlertDialog alert11 = builder1.create();
//            alert11.show();
//            // Make the textview clickable. Must be called after show()
//            ((TextView) alert11.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
//        } catch (Exception e) {
//            LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "CreateDevInfoAlertDialog", "exeption", e);
//        }
//    }
}

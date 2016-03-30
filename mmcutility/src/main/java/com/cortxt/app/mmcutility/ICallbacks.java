package com.cortxt.app.mmcutility;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.telephony.CellLocation;

import com.cortxt.app.mmcutility.DataObjects.EventObj;
import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.cortxt.app.mmcutility.Utils.GpsListener;

import org.json.JSONObject;

/**
 * Created by bscheurman on 16-03-17.
 */
public interface ICallbacks {
    void setVQHandler (Handler handler);
    EventObj triggerSingletonEvent (EventType eventType);
    void temporarilyStageEvent(EventObj event, EventObj compEvent, Location loc);
    void unstageEvent(EventObj event);
    Context getContext ();
    boolean isOnline ();
    JSONObject getServiceMode ();
    void setIconBehavior ();
    String getNeighbors ();
    void setNeighbors (String nbrs);
    int getLastServiceState();

    void registerLocationListener (boolean useGPS, GpsListener listener);
    void unregisterLocationListener (boolean useGPS, GpsListener listener);

    Location getLastLocation();

    void startRadioLog (boolean bStart, String reason, int eventType);
    void setAlarmManager ();
    void manageDataMonitor (int setting, Integer appscan_seconds);
    void updateTravelPreference ();
    void queueActiveTest(EventType evType, int trigger);
    void setActiveTestComplete(EventType testType);
    void localReportEvent (EventObj itestEvent);
    boolean isEventRunning(EventType eventType);

    EventObj getStartEvent (EventType startEventType, EventType stopEventType, boolean bStart);
    long getLastCellSeen (CellLocation cell);
    boolean waitForConnect ();
    boolean isGpsRunning (); // Needed for Engineering screen to show it
    boolean isInTracking ();
    boolean isHeadsetPlugged ();
    boolean isWifiConnected ();
    String getStackTrace (Exception e);
}

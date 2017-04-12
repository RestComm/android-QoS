package com.cortxt.app.utillib;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.telephony.CellLocation;

import com.cortxt.app.utillib.DataObjects.EventObj;
import com.cortxt.app.utillib.DataObjects.EventType;
import com.cortxt.app.utillib.Utils.GpsListener;

import org.json.JSONObject;

/**
 * Created by bscheurman on 16-03-17.
 */
public interface ICallbacks{
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

    Location getLastLocation();
    void setLastLocation (Location location);
    int getLastNumSatellites();

    void startRadioLog (boolean bStart, String reason, int eventType);
    void setAlarmManager ();
    void manageDataMonitor (int setting, Integer appscan_seconds);
    void updateTravelPreference ();
    void queueActiveTest(EventType evType, int trigger);
    void setActiveTestComplete(EventType testType);
    void localReportEvent (EventObj itestEvent);
    boolean isEventRunning(EventType eventType);

    EventObj getStartEvent (EventType startEventType, EventType stopEventType, boolean bStart);
    EventObj getLatestEvent ();
    long getLastCellSeen (CellLocation cell);
    boolean waitForConnect ();
    boolean isGpsRunning (); // Needed for Engineering screen to show it
    boolean isInTracking ();
    boolean isTravelling ();
    boolean isHeadsetPlugged ();
    boolean isWifiConnected ();
    String getStackTrace (Exception e);
    String getDriveTestTrigger ();
    void triggerDriveTest (String reason, boolean start);
    void stopTracking ();
}

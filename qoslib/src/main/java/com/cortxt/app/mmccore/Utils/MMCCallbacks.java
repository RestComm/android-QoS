package com.cortxt.app.mmccore.Utils;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.telephony.CellLocation;

import com.cortxt.app.mmccore.MMCService;
import com.cortxt.app.mmcutility.DataObjects.EventCouple;
import com.cortxt.app.mmcutility.DataObjects.EventObj;
import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.cortxt.app.mmcutility.DataObjects.PhoneState;
import com.cortxt.app.mmcutility.ICallbacks;
import com.cortxt.app.mmcutility.Utils.GpsListener;

import org.json.JSONObject;

/**
 * Created by bscheurman on 16-03-18.
 */
public class MMCCallbacks implements ICallbacks {

    MMCService mContext;

    public MMCCallbacks(MMCService context)
    {
        mContext = context;
    }
    public Context getContext ()
    {
        return mContext;
    }
    public void setVQHandler (Handler handler)
    {
        mContext.getVQManager().setHandler(handler);
    }

    public EventObj triggerSingletonEvent(EventType eventType) {
        return mContext.getEventManager().registerSingletonEvent(eventType);
    }
    public void temporarilyStageEvent(EventObj event, EventObj compEvent, Location loc)
    {
        mContext.getEventManager().temporarilyStageEvent(event, compEvent, loc);
    }

    public void unstageEvent(EventObj event)
    {
        mContext.getEventManager().unstageEvent(event);
    }

    public boolean isOnline() {
        return mContext.isOnline();
    }

    // Allows the Settings screen to tell the server to change the behavior of the notification icon
    // The icon can be shown always, or just when Active
    public void setIconBehavior () {
        mContext.setIconBehavior();
    }

    public JSONObject getServiceMode ()
    {
        return PhoneState.getServiceMode();
    }

    String lastNeighbors;
    public String getNeighbors () {
        if (mContext != null)
        {
            mContext.getCellHistory().updateNeighborHistory(null, null);
            int lastService = mContext.getPhoneState().getLastServiceState();
            mContext.getIntentDispatcher().updateConnection(",,,," + lastService);
            mContext.setEnggQueryTime();
        }
        return lastNeighbors;
    }
    public void setNeighbors (String nbrs) {
        lastNeighbors = nbrs;
    }

    public int getLastServiceState()
    {
        if (mContext != null && mContext.getPhoneStateListener() != null)
            return mContext.getPhoneState().getLastServiceState();
        return 0;
    }

    public void registerLocationListener (boolean useGPS, GpsListener listener)
    {
        if (useGPS && MMCService.getGpsManager() != null)
            MMCService.getGpsManager().registerListener(listener);
        else if (!useGPS && MMCService.getNetLocationManager() != null)
            MMCService.getNetLocationManager().registerListener(listener);
    }
    public void unregisterLocationListener (boolean useGPS, GpsListener listener)
    {
        if (useGPS && MMCService.getGpsManager() != null)
            MMCService.getGpsManager().unregisterListener(listener);
        else if (!useGPS && MMCService.getNetLocationManager() != null)
            MMCService.getNetLocationManager().unregisterListener(listener);
    }

    public Location getLastLocation()
    {
        return mContext.getLastLocation();
    }

    public void startRadioLog (boolean bStart, String reason, int eventType)
    {
        mContext.startRadioLog(bStart, reason, null);
    }

    public void setAlarmManager ()
    {
        mContext.setAlarmManager();
    }

    public void manageDataMonitor (int setting, Integer appscan_seconds)
    {
        mContext.manageDataMonitor(setting, appscan_seconds);
    }

    public void updateTravelPreference ()
    {
        mContext.updateTravelPreference();
    }
    public void queueActiveTest(EventType eventType, int trigger) {
        mContext.getEventManager().queueActiveTest(eventType, trigger);
    }
    public void setActiveTestComplete(EventType testType) {mContext.getEventManager().setActiveTestComplete(testType);}


    public void localReportEvent (EventObj event)
    {
        mContext.getEventManager().localReportEvent(event);
    }

    public boolean isEventRunning(EventType eventType)
    {
        return mContext.getEventManager().isEventRunning(eventType);
    }
    public EventObj getStartEvent (EventType startEventType, EventType stopEventType, boolean bStart) {
        EventObj event = null;
        EventType start = startEventType;
        EventType stop = stopEventType;
        EventCouple eventCouple = mContext.getEventManager().getEventCouple(start, stop);
        if (eventCouple != null)
        {
            if (bStart)
                event = eventCouple.getStartEvent();
            else
                event = eventCouple.getStopEvent();
        }
        return event;
    }
    public long getLastCellSeen (CellLocation cell)
    {
        return mContext.getCellHistory().getLastCellSeen(cell);
    }

    public boolean waitForConnect ()
    {
        return mContext.getPhoneStateListener().waitForConnect();
    }

    public boolean isWifiConnected ()
    {
        return mContext.bWifiConnected;
    }
    public boolean isGpsRunning () // Needed for Engineering screen to show it
    {
        if (MMCService.getGpsManager() != null)
            return MMCService.getGpsManager().isGpsRunning();
        return false;
    }
    public boolean isInTracking ()
    {
        return MMCService.isInTracking();
    }

    public boolean isHeadsetPlugged ()
    {
        return MMCService.isHeadsetPlugged();
    }

    public String getStackTrace (Exception e)
    {
        String stackTrace = "\n\t" + e.toString();
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        int len = stackTraceElements.length;
        if (len > 3)
            len = 3;
        for(int i=0; i<len; i++) {
            stackTrace += "\n\t" + stackTraceElements[i].getClassName() + "." + stackTraceElements[i].getMethodName() + " (" + stackTraceElements[i].getFileName() + " : " + stackTraceElements[i].getLineNumber() + ")";
        }
        return stackTrace;
    }
}

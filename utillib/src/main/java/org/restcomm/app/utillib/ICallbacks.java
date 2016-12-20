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

package org.restcomm.app.utillib;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.telephony.CellLocation;

import org.restcomm.app.utillib.DataObjects.EventObj;
import org.restcomm.app.utillib.DataObjects.EventType;

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

package com.cortxt.app.mmcutility.DataObjects;

import java.util.ArrayList;
import java.util.List;

import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

//import com.cortxt.app.mmccore.ServicesOld.MMCPhoneStateListenerOld;
import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.Utils.PreciseCallCodes;

public class ConnectionHistory {

	public static final int TYPE_RXTX = 99;
	public static final int TYPE_SPECIAL = 100;
	public static final int TYPE_LATENCY = 101;
	public static final int TYPE_DOWNLOAD = 102;
	public static final int TYPE_UPLOAD = 103;
	
	public static final int TYPE_THROUGHPUT = 105;
	public static final int TYPE_VIDEO = 106;
	public static final int TYPE_SMSTEST = 107;
	public static final int TYPE_PRECISECALL = 108;
	public static final int TYPE_SIP = 109;

	private List<ConnectionSample> connect_history = new ArrayList<ConnectionSample>();
	private List<ServiceModeSample> svcmode_history = new ArrayList<ServiceModeSample>();
	private long tmLastUpdate = 0, tmLastCellUpdate = 0;
	private String lastConnectString = "";
	public static final String TAG = ConnectionHistory.class.getSimpleName();
	private long rxLast = 0, txLast = 0;
	
	// Called when a neighbor list is detected in the RadioLog
	// It augments that neighbor list with the neighbor from the API
	public String updateConnectionHistory (int cellnettype, int state, int activity, ServiceState serviceState, NetworkInfo networkInfo)
	{
		int i;
		//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "onDataActivityChanged", String.format("Network type: %d, State: %d, Activity: %d", cellnettype, state, activity));
		
		tmLastUpdate = System.currentTimeMillis();
		//if (!_isTravelling)
		{
			String txt = "";
			String stringConnections = "";
			int activeType = -1, activeDetailState =-1;
			if (networkInfo != null)
			{
				//MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "onDataActivityChanged", String.format("ActiveNet: %s", networkInfo.toString()));
				
				activeType = networkInfo.getType();
				//activeDetailState = networkInfo.getDetailedState().ordinal();
			}
			ConnectionSample smp = new ConnectionSample(cellnettype, state, activity);
			smp.activeType = activeType;
			if (serviceState != null)
			{
				smp.serviceState = serviceState.getState();
				if (serviceState.getRoaming() == true)
					smp.serviceState += 10;
				smp.voiceType = PhoneState.getVoiceNetworkType(serviceState);
			}
			switch (activity)
			{
			case TelephonyManager.DATA_ACTIVITY_DORMANT:
				stringConnections ="dormant"; break;
			case TelephonyManager.DATA_ACTIVITY_IN:
				stringConnections ="in"; break;
			case TelephonyManager.DATA_ACTIVITY_OUT:
				stringConnections ="out"; break;
			case TelephonyManager.DATA_ACTIVITY_INOUT:
				stringConnections ="inout"; break;
			case TelephonyManager.DATA_ACTIVITY_NONE:
				stringConnections ="none"; break;
			
			}
			stringConnections = cellnettype + "," + state + "," + activity + "," + activeType + "," + serviceState + "," + smp.voiceType;
			if (!lastConnectString.equals(stringConnections))
			{
				// Update Neighbor list history if state changes
				connect_history.add (smp);
				lastConnectString = stringConnections;

				long rx = TrafficStats.getTotalRxBytes();
				long tx = TrafficStats.getTotalTxBytes();
				if (rx > rxLast || tx > txLast)
				{
					ConnectionSample smp2 = new ConnectionSample(TYPE_RXTX, rx, tx);
					rxLast = rx; txLast = tx;
					connect_history.add(smp2);
				}
				lastRxTxUpdate = System.currentTimeMillis();
				return stringConnections;
			}

			return null;
		}
	}

	private long lastRxTxUpdate = 0;
	public void updateRxTx ()
	{
		if (lastRxTxUpdate + 4000 < System.currentTimeMillis())
		{
			// Update Neighbor list history if state changes
			long rx = TrafficStats.getTotalRxBytes();
			long tx = TrafficStats.getTotalTxBytes();
			if (rx > rxLast + 10000 || tx > txLast + 10000) {
				ConnectionSample smp2 = new ConnectionSample(TYPE_RXTX, rx, tx);
				rxLast = rx; txLast = tx;
				connect_history.add(smp2);
			}
			lastRxTxUpdate = System.currentTimeMillis();
		}
	}
	public void updateThroughputHistory (int rxbytes, int txbytes)
	{
		ConnectionSample smp = new ConnectionSample(105, rxbytes, txbytes);
		connect_history.add (smp);
	}
	
	public void updateVideoTestHistory (int bufferProgress, int playProgress, int stalls, int bytes)
	{
		ConnectionSample smp = new ConnectionSample(TYPE_VIDEO, bufferProgress, playProgress);
		smp.activeType = stalls;
		smp.serviceState = bytes;
		connect_history.add (smp);
	}

	public void updateSIPCallHistory (int rx, int tx, int state)
	{
		ConnectionSample smp = new ConnectionSample(TYPE_SIP, rx, tx);
		smp.activeType = state;
		connect_history.add (smp);
	}
	public void updateSpeedTestHistory(int latency, int latencyProgress, int downloadSpeed, int downloadProgress, int uploadSpeed, int uploadProgress, int counter)
	{
		int type = 0, prog = 0;
		if (downloadSpeed == -1)
		{
			type = 101; prog = latencyProgress;
		}
		else if (uploadSpeed == -1)
		{
			type = 102; prog = downloadProgress;
		}
		else 
		{
			type = 103; prog = uploadProgress;
		}
		ConnectionSample smp = new ConnectionSample(type, prog, counter);
		// Update Neighbor list history if state changes
		connect_history.add (smp);
	}
	
	public void updateSMSTestHistory(long smsSendTime, long smsDeliveryTime, long smsSentFromServerTime, long smsArrivalTime) {
		int deliverDiff = (int)(smsSentFromServerTime - smsSendTime);
		int responseDiff = (int)(smsArrivalTime - smsSentFromServerTime);
		int smsRoundTripTime = (int)(smsArrivalTime - smsSendTime);
		System.out.println ("updateSMSTestHistory :"+smsSendTime+" "+smsSentFromServerTime+" "+smsArrivalTime+" "+smsRoundTripTime+" "+responseDiff);
		ConnectionSample smp = new ConnectionSample(smsSendTime, TYPE_SMSTEST, deliverDiff, responseDiff, smsRoundTripTime, (int) smsDeliveryTime);
		connect_history.add(smp);
	}
	public void updatePreciseCallHistory(PreciseCallCodes preciseCall) {
		
		int state = preciseCall.getRingingCallState();
		if (state > 0)
		{
			ConnectionSample smp = new ConnectionSample(System.currentTimeMillis(), TYPE_PRECISECALL, 0, state, 0, 0);
			connect_history.add(smp);
		}
		state = preciseCall.getForegroundCallState();
		if (state > 0)
		{
			ConnectionSample smp = new ConnectionSample(System.currentTimeMillis(), TYPE_PRECISECALL, 1, state, 0, 0);
			connect_history.add(smp);
		}
		state = preciseCall.getBackgroundCallState();
		if (state > 0)
		{
			ConnectionSample smp = new ConnectionSample(System.currentTimeMillis(), TYPE_PRECISECALL, 2, state, 0, 0);
			connect_history.add(smp);
		}
	}


	public void updateServiceModeHistory (String svcscreen, String name)
	{

		ServiceModeSample smp = new ServiceModeSample(System.currentTimeMillis(), svcscreen, name);
		svcmode_history.add (smp);
	}
	
	public void start ()
	{
		lastConnectString = "";
	}
	// Called when an event is being reported to the server
	// It builds a neighbor list history that doesnt ovrlap the previously reported neighbor list history
	public String getHistory (long startTime, long eventTime, long endTime, EventObj evt)
	{
		int i;
		EventType evtType = evt.getEventType();
		int peakBytes = 0;
		String txt = "";
		String hdr = "7,sec,type,state,activity,ctype,svcstate,voicetype";
		int size = connect_history.size();
        ConnectionSample netsmp = null;
        for (i=0; i<size; i++)
		{
			ConnectionSample smp = connect_history.get(i);
            if (smp.type < 100)
                netsmp = smp;
			ConnectionSample smp2 = null;
			if (i<size-1)
				smp2 = connect_history.get(i+1);
            if (netsmp != null && netsmp.timestamp < startTime && netsmp.type < TYPE_SPECIAL-1 && (smp2 == null || smp2.timestamp>=startTime))
            {
                // Network Type sample before event started
                txt += "," + ((smp.timestamp - eventTime)/1000);
                txt += "," + netsmp.type + "," + netsmp.getStateName(evt) +"," + netsmp.getActivityName(evt); // + "," + smp.activity;
                txt += "," + netsmp.activeType + "," + netsmp.serviceState + "," + netsmp.voiceType;
                netsmp = null;
            }
			//else if ((smp.timestamp >= startTime && smp.timestamp <= endTime) || (smp2 != null && smp.timestamp<startTime && smp2.timestamp>=startTime && smp.type < 100 )
			//		|| (smp2 == null && smp.timestamp<startTime && smp.type < 100))
            else if (smp.timestamp >= startTime && smp.timestamp <= endTime)
			{
				//if (smp.sent == 0)
				if ((evtType == EventType.MAN_SPEEDTEST && smp.type >= TYPE_LATENCY && smp.type <= TYPE_UPLOAD) ||
						(evtType == EventType.APP_MONITORING && smp.type == TYPE_THROUGHPUT) || smp.type < TYPE_SPECIAL ||
						(evtType == EventType.VIDEO_TEST && smp.type == TYPE_VIDEO) || (evtType == EventType.AUDIO_TEST && smp.type == TYPE_VIDEO) ||
						(evtType == EventType.YOUTUBE_TEST && smp.type == TYPE_VIDEO)|| (evtType == EventType.WEBPAGE_TEST && smp.type == TYPE_VIDEO) ||
						(evtType == EventType.SMS_TEST && smp.type == TYPE_SMSTEST) ||
						(evtType.getIntValue() >= EventType.SIP_DISCONNECT.getIntValue() && evtType.getIntValue() <= EventType.SIP_UNANSWERED.getIntValue()  && smp.type == TYPE_SIP) )
				{
					// only include throughput byte counts while increasing, not after they return to 0
					if (smp.type == TYPE_THROUGHPUT)
					{
						if (smp.state >= peakBytes)
							peakBytes = (int)smp.state;
						else 
							continue;
					}
					txt += "," + ((smp.timestamp - eventTime)/1000);
					//if (smp.type > 100)
					if (smp.timestamp > eventTime && smp.type != TYPE_THROUGHPUT && smp.type != TYPE_RXTX)
					{	
						txt += ".";
						if ((smp.timestamp - eventTime)%1000 < TYPE_SPECIAL)
							txt += "0";
						txt += (((smp.timestamp - eventTime)%1000)/10);
					}
						
					//txt += ","+ MMCPhoneStateListenerOld.getNetworkName (smp.type)+ "," + smp.getStateName()+"," + smp.getActivityName(); // + "," + smp.activity;
					txt += ","+ smp.type+ "," + smp.getStateName(evt)+"," + smp.getActivityName(evt); // + "," + smp.activity;
					
					if (smp.type == TYPE_SMSTEST || smp.type == TYPE_VIDEO || smp.type == TYPE_SIP)
						txt += "," + smp.activeType + "," + smp.serviceState + ",";
					else if (smp.type >= TYPE_SPECIAL)
						txt += ",,,";
					else
						txt += "," + smp.activeType + "," + smp.serviceState + "," + smp.voiceType;
				}
				smp.sent = 1;
			}
		}
		if (txt.length() > 1)
			return hdr+txt;
		else
		{
			return "";
		}
			
	}
	// ON each 3hr checkpoint, prune the cell history lists, removing items 3 hours old
	public void clearHistory ()
	{
		// clear 1 hour old items out of history lists
		int i;
		for (i=0; i<connect_history.size()-4; i++)
		{
			if (connect_history.get(i).timestamp + 60*60000 < System.currentTimeMillis())
			{
				connect_history.remove(i);
				i--;
			}
			else
				break;
			
		}

		for (i=0; i<svcmode_history.size()-4; i++)
		{
			if (svcmode_history.get(i).timestamp + 60*60000 < System.currentTimeMillis())
			{
				svcmode_history.remove(i);
				i--;
			}
			else
				break;

		}
		MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "clearConnectionHistory", "history=" + connect_history.size());
	}

	// Called when an event is being reported to the server
	// It builds a neighbor list history that doesnt ovrlap the previously reported neighbor list history
	public String getServiceModeHistory (long startTime, long eventTime, long endTime, EventType evtType) {
		int i;
		int peakBytes = 0;
		String txt = "";

		int size = svcmode_history.size();
		if (size == 0)
			return null;
		StringBuilder builder = new StringBuilder();
		String[][] prevLines = {null,null,null};
		ConnectionSample netsmp = null;
		for (i = 0; i < size; i++) {
			ServiceModeSample smp = svcmode_history.get(i);
			if (smp.timestamp >= eventTime - 3000 && smp.timestamp <= endTime)
			{
				if (i>0) {
					ServiceModeSample prevSmp = svcmode_history.get(i - 1);
					if (smp.svcscreen.equals(prevSmp.svcscreen))
						continue;
				}
				int time = (int)((smp.timestamp - eventTime)/1000);
				builder.append("time:" + time + ":screen:"+smp.screenname+"\n");
				if (prevLines[smp.index] == null)
				{
					builder.append(smp.svcscreen + "\n");
				}
				else
				{
					String[] lines = smp.svcscreen.split("\n");
					for (int j=0; j<lines.length; j++)
					{
						if (j < prevLines[smp.index].length && lines[j].equals(prevLines[smp.index][j]))
							builder.append("\n");
						else if (lines[j].trim().equals(""))
							builder.append("-\n");
						else
							builder.append(lines[j].trim() + "\n");
					}
				}
				prevLines[smp.index] = smp.svcscreen.split("\n");
			}
		}
		String wow = builder.toString();
		return wow;
	}


	public class ServiceModeSample {
		public String svcscreen, screenname;
		int index = 0;
		public long timestamp;

		public ServiceModeSample(long sampleTime, String _svcscreen, String name) {
			timestamp = sampleTime;
			if (name.equals("BASIC"))
				index = 0;
			else if (name.equals("NEIGHBOURS"))
				index = 1;
			else if (name.equals("MM INFO"))
				index = 2;
			svcscreen = _svcscreen;
			screenname = name;
		}
	}
	// describes the data activity at a moment in time
	public class ConnectionSample
	{
		public int type =-1, sent = 0, actNetwork = -1;
		public long state = -1, activity = -1;
		public long timestamp;
		public int activeType, serviceState, deliveryTime;
		public int voiceType;
		
		public ConnectionSample (int _type, long _state, long _activity) {
			timestamp = System.currentTimeMillis();
			state = _state;
			type = _type;
			activity = _activity;
		}
		
		public ConnectionSample (long sampleTime, int _type, int _state, int _activity, int roundTrip) {
			timestamp = sampleTime;
			state = _state;
			type = _type;
			activity = _activity;
			activeType = roundTrip;
		}
		
		public ConnectionSample (long sampleTime, int _type, int _state, int _activity, int roundTrip, int _deliveryTime) {
			timestamp = sampleTime;
			state = _state;
			type = _type;
			activity = _activity;
			activeType = roundTrip;
			deliveryTime = _deliveryTime;
		}

		
		public String getStateName (EventObj evt)
		{
			if (type >= TYPE_SPECIAL)
			{
				if (type == TYPE_VIDEO)
					return String.valueOf((double)state/10d);

				return String.valueOf(state);
			}
			if (type == TYPE_RXTX)  // report tx, rx
			{
				long rx = state;
				if (evt.getRX() > 0 && rx > evt.getRX()) {
					rx = rx - evt.getRX();
					return String.valueOf(rx/1000);
				}
				return "";
			}
			switch ((int)state)
			{
			case TelephonyManager.DATA_CONNECTED:
				return "C"; 
			case TelephonyManager.DATA_CONNECTING:
				return "c"; 
			case TelephonyManager.DATA_DISCONNECTED:
				return "D"; 
			case TelephonyManager.DATA_SUSPENDED:
				return "S"; 
			}
			return "U";
		}
		
		public String getActivityName (EventObj evt)
		{
			if (type >= TYPE_SPECIAL)
			{
				if (type == TYPE_VIDEO)	
					return String.valueOf((double)activity/10d);

				return String.valueOf(activity);
			}
			if (type == TYPE_RXTX)  // report tx, rx
			{
				long tx = activity;
				if (evt.getTX() > 0 && tx > evt.getTX()) {
					tx = tx - evt.getTX();
					return String.valueOf(tx/1000);
				}
				return "";
			}
			switch ((int)activity)
			{
			case TelephonyManager.DATA_ACTIVITY_DORMANT:
				return "D"; 
			case TelephonyManager.DATA_ACTIVITY_IN:
				return "R"; 
			case TelephonyManager.DATA_ACTIVITY_OUT:
				return "T";
			case TelephonyManager.DATA_ACTIVITY_INOUT:
				return "B"; 
			case TelephonyManager.DATA_ACTIVITY_NONE:
				return "N";	
			}
			return "U";
		}
	}
}

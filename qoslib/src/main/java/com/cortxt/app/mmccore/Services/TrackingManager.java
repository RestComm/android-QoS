package com.cortxt.app.mmccore.Services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.cortxt.app.mmccore.MMCService;
import com.cortxt.app.mmccore.Services.Intents.MMCIntentHandler;
import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.Utils.PreferenceKeys;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author nasrullah
 */
public class TrackingManager {
	public static final String TAG = TrackingManager.class.getSimpleName();
	
	private MMCService owner;
	//private Timer timer;
	private Handler handler = new Handler();
	private long trackingExpires = 0;
	//private boolean bCoverage = true, bSpeed = false, bTracking = false;
	private boolean bTracking = false;
	private int coverageInterval = 5, speedtestInterval = 0, videoInterval = 0, audioInterval = 0;
	private int webInterval = 0, smsInterval = 0, connectInterval = 0, vqInterval = 0, youtubeInterval = 0;
	private int advancedIndex = 0;
	private boolean advancedWaiting = false;
	private int count = 0;
    private Runnable testRunnable = null;
	private JSONArray scheduledCommands = null;
	
	public TrackingManager(MMCService owner) {
		this.owner = owner;
	}
	
	public void resumeTracking ()
	{
		try {
			String cmd = PreferenceManager.getDefaultSharedPreferences(owner).getString(PreferenceKeys.Miscellaneous.DRIVE_TEST_CMD, null);
			long expires = PreferenceManager.getDefaultSharedPreferences(owner).getLong(PreferenceKeys.Miscellaneous.TRACKING_EXPIRES, 0);
			//advancedIndex = PreferenceManager.getDefaultSharedPreferences(owner).getInt(PreferenceKeys.Miscellaneous.DRIVETEST_INDEX, -1);

			if (cmd != null && expires > System.currentTimeMillis()) {
				JSONObject cmdJson = new JSONObject(cmd);


				int numFiveMinutePeriods = (int) (expires - System.currentTimeMillis());
				numFiveMinutePeriods = numFiveMinutePeriods / (5 * 60000) + 1;

				if (cmdJson.has("schedule"))
				{
					JSONObject testSchedule = cmdJson.getJSONObject("schedule");
					testSchedule.put("dur", numFiveMinutePeriods);
					startAdvancedTracking(cmdJson, 0);
				}
				else {
					JSONObject testSettings = cmdJson.getJSONObject("settings");
					testSettings.put("dur", numFiveMinutePeriods);
					startTracking(cmdJson, 0);
				}
				trackingExpires = expires;

				MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "resumeTracking", "expires=" + expires + ",cmd=" + cmd);

			}

		}
		catch(Exception e)
		{
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "resumeTracking", "Exception reading stored tracking settings");
		}

	}

	public void startAdvancedTracking (JSONObject cmdJson, long starttime) {
		try {
			//JSONObject commands = cmdJson.getJSONObject("commands");
			JSONObject schedule = cmdJson.getJSONObject("schedule");

			int numFiveMinutePeriods = 0;
			if (schedule.has("dur"))
				numFiveMinutePeriods = schedule.getInt("dur");
			else
				numFiveMinutePeriods = 5;
			numFiveMinutePeriods = (numFiveMinutePeriods + 4) / 5;

			if (schedule.has("commands")) {
				// The active test commands will be scheduled in a special
				scheduledCommands = schedule.getJSONArray("commands");
				advancedIndex = 0;

				// Run the normal Signal tracking as usual
				startCoverageTracking (numFiveMinutePeriods);
				long startDelay = 1000;
				if (starttime > System.currentTimeMillis())
					startDelay = starttime - System.currentTimeMillis();

				handler.postDelayed(
						testRunnable = new Runnable() {
							public void run() {
								runAdvancedTrackingTests();
							}
						}, startDelay);
			}
		} catch (Exception e) {
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "startTracking", "exception", e);
		}
	}

	public void startCoverageTracking (int numFiveMinutePeriods)
	{
		bTracking = true;
		this.owner.keepAwake(true, true);
		AlarmManager alarmMgr = (AlarmManager) owner.getSystemService( Service.ALARM_SERVICE );
		Intent intent = new Intent(MMCIntentHandler.ACTION_TRACKING_5MINUTE);
		PendingIntent alarm = PendingIntent.getBroadcast( owner, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmMgr.cancel( alarm );
		trackingExpires = System.currentTimeMillis() + (numFiveMinutePeriods) * 5L * 60L * 1000L;

		PreferenceManager.getDefaultSharedPreferences(owner).edit().putLong(PreferenceKeys.Miscellaneous.TRACKING_EXPIRES, trackingExpires).commit();
		PreferenceManager.getDefaultSharedPreferences(owner).edit().putLong(PreferenceKeys.Miscellaneous.ENGINEER_MODE_EXPIRES_TIME, trackingExpires + 0*5L * 60L * 1000L).commit();

		long delay = 0;
		if (numFiveMinutePeriods == 0)  // continuous tracking
		{
			trackingExpires = 0;
			// store a tracking expiry date of 10 million seconds from now
			PreferenceManager.getDefaultSharedPreferences(owner).edit().putLong(PreferenceKeys.Miscellaneous.TRACKING_EXPIRES, System.currentTimeMillis()+10000000000l).commit();
		}
		MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "startTracking", "numFiveMinutePeriods=" + numFiveMinutePeriods + ",covInterval=" + coverageInterval + ",SpeedInterval="+speedtestInterval + ",videoInterval="+ videoInterval);

		delay = 5L * 60L * 1000L;
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, delay, alarm);
	}
	public void startTracking (JSONObject cmdJson, long starttime)
	{
		try {
//			if (true)
//			{
//				//String json = "{\"commands\":{\"schedule\":{\"dur\":1,\"commands\":[{\"mmctype\":\"speed\",\"loop\":2,\"settings\":{}}]}},\"userid\":99323899,\"carrierid\":1381,\"deviceid\":\"0de9614b-558b-4d82-b62f-c78c75882e85\"}";
				String json = "{\"schedule\":{\"dur\":6,\"commands\":[{\"mmctype\":\"speed\",\"loop\":2,\"settings\":{}},{\"mmctype\":\"predelay\",\"loop\":60,\"settings\":{}},{\"mmctype\":\"ue\",\"loop\":1,\"settings\":{}},{\"mmctype\":\"audio\",\"loop\":2,\"settings\":{}},{\"mmctype\":\"postdelay\",\"loop\":60,\"settings\":{}}]},\"userid\":78,\"carrierid\":1,\"deviceid\":\"5f0cc3b0-8b1e-4493-bd3d-3dd234d66e03\"}";
//				cmdJson = new JSONObject(json);
//				startAdvancedTracking (cmdJson);
//				return;
//			}
			JSONObject testSettings = cmdJson.getJSONObject("settings");

			int numFiveMinutePeriods = 0;
			if (testSettings.has("dur"))
				numFiveMinutePeriods = testSettings.getInt("dur");
			else
				numFiveMinutePeriods = 3;
			if (testSettings.has("cov"))
				this.coverageInterval = testSettings.getInt("cov");
			if (testSettings.has("spd"))
				this.speedtestInterval = testSettings.getInt("spd");
			if (testSettings.has("vid"))
				this.videoInterval = testSettings.getInt("vid");
			if (testSettings.has("ct"))
				this.connectInterval = testSettings.getInt("ct");
			if (testSettings.has("sms"))
				this.smsInterval = testSettings.getInt("sms");
			if (testSettings.has("web"))
				this.webInterval = testSettings.getInt("web");
			if (testSettings.has("youtube"))
				this.youtubeInterval = testSettings.getInt("youtube");
			if (testSettings.has("vq"))
				this.vqInterval = testSettings.getInt("vq");
			if (testSettings.has("aud"))
				this.audioInterval = testSettings.getInt("aud");

			startCoverageTracking (numFiveMinutePeriods);
			long startDelay = 1000;
			if (starttime > System.currentTimeMillis())
				startDelay = starttime - System.currentTimeMillis();

			handler.postDelayed(
                    testRunnable = new Runnable() {
                        public void run() {
                            runTrackingTests();
                        }
                    }, startDelay);
		}
		catch (Exception e)
		{
			MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "startTracking", "exception", e);
		}
	}
	
	public boolean isTracking ()
	{
		//if (timer != null && (trackingExpires > System.currentTimeMillis() || trackingExpires == 0))
		if (bTracking == true && (trackingExpires+0*280000 > System.currentTimeMillis() || trackingExpires == 0))
			return true;
		else 
		{
			bTracking = false;
			return false;
		}
	}

	public boolean isAdvancedTrackingWaiting ()
	{
		//if (timer != null && (trackingExpires > System.currentTimeMillis() || trackingExpires == 0))
		if (bTracking == true && (trackingExpires+0*280000 > System.currentTimeMillis() || trackingExpires == 0))
		{
			if (advancedIndex >= 0 && advancedWaiting)
				return true;
		}
		else
		{
			bTracking = false;
			return false;
		}
		return false;
	}
	
	/**
	 * Cancels all scheduled tracking events.
	 * Does not stop a currently running tracking event.
	 */
	public void cancelScheduledEvents() {
		//if(timer != null) {
		//	timer.cancel();
		//	timer = null;
		//}
		try
		{
			MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "cancelScheduledEvents", "");
            bTracking = false;
            trackingExpires = System.currentTimeMillis() - 10000;
			PreferenceManager.getDefaultSharedPreferences(owner).edit().putLong(PreferenceKeys.Miscellaneous.TRACKING_EXPIRES, 0L).commit();

            AlarmManager alarmMgr = (AlarmManager) owner.getSystemService( Service.ALARM_SERVICE );
			Intent intent = new Intent(MMCIntentHandler.ACTION_TRACKING_5MINUTE);
			PendingIntent alarm = PendingIntent.getBroadcast( owner, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			alarmMgr.cancel(alarm);
			handler.removeCallbacks(testRunnable);

		}catch (Exception e)
		{
			MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "cancelScheduledEvents", "Exception", e);	
		}
	}

    // every 1 minute, execute drive tests that come due
    public void runTrackingTests ()
    {
		if (trackingExpires < System.currentTimeMillis() || bTracking == false)
			return;

		try{
			if (bTracking && (trackingExpires == 0 || trackingExpires > System.currentTimeMillis() + 30000)) {

				handler.postDelayed(
						testRunnable = new Runnable() {
							public void run() {
								runTrackingTests();
							}
						}, 60L * 1000L);

			}
		}catch (Exception e)
		{
			MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "startTracking", "start:Exception", e);
		}

        if (speedtestInterval > 0 && (count % speedtestInterval) == 0)
        {
            owner.getEventManager().queueActiveTest(EventType.MAN_SPEEDTEST, 2);
        }

        if (connectInterval > 0 && (count % connectInterval) == 0)
        {
            owner.getEventManager().queueActiveTest(EventType.LATENCY_TEST, 2);
        }
        if (smsInterval > 0 && (count % smsInterval) == 0)
        {
            owner.getEventManager().queueActiveTest(EventType.SMS_TEST, 2);
        }
        if (webInterval > 0 && (count % webInterval) == 0)
        {
            owner.getEventManager().queueActiveTest(EventType.WEBPAGE_TEST, 2);
        }
		if (youtubeInterval > 0 && (count % youtubeInterval) == 0)
		{
			owner.getEventManager().queueActiveTest(EventType.YOUTUBE_TEST, 2);
		}
        if (vqInterval > 0 && (count % vqInterval) == 0)
        {
            owner.getEventManager().queueActiveTest(EventType.EVT_VQ_CALL, 2);
            //owner.triggerWebTest(2);
            //Intent intent = new Intent(CommonIntentBundleKeysOld.ACTION_START_VOICETEST);
            //MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "runTracking", "beginVoiceTest");
            //owner.sendBroadcast(intent);
        }
        if (videoInterval > 0 && (count % videoInterval) == 0)
        {
            owner.getEventManager().queueActiveTest(EventType.VIDEO_TEST, 2);
            //tasks.add(new Runnable() {
            //    @Override public void run() {
            //        owner.triggerActiveTest(2, EventType.VIDEO_TEST.getIntValue()); }});
        }
        if (audioInterval > 0 && (count % audioInterval) == 0)
        {
            owner.getEventManager().queueActiveTest(EventType.AUDIO_TEST, 2);
            //tasks.add(new Runnable() {
            //    @Override public void run() {
            //        owner.triggerActiveTest(2, EventType.AUDIO_TEST.getIntValue()); }});
        }
		count += 1;
        MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "runTrackingTests", "count="+count);



    }

	// Queue a series of tests with delays, then detect when they complete, and repeat them again
	public void runAdvancedTrackingTests ()
	{
		if (trackingExpires < System.currentTimeMillis() || bTracking == false)
			return;

		count += 1;

		try{
			advancedWaiting = false;
			JSONObject command = scheduledCommands.getJSONObject(advancedIndex);
			String cmdtype = command.getString("mmctype");
			int loop = command.getInt("loop");

			MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "runAdvancedTrackingTests", "cmdtype="+cmdtype + " loop=" + loop );

			if (cmdtype.equals("speed"))
				owner.getEventManager().queueActiveTest(EventType.MAN_SPEEDTEST, 2);
			else if (cmdtype.equals("latency"))
				owner.getEventManager().queueActiveTest(EventType.LATENCY_TEST, 2);
			else if (cmdtype.equals("smstest"))
				owner.getEventManager().queueActiveTest(EventType.SMS_TEST, 2);
			else if (cmdtype.equals("web"))
				owner.getEventManager().queueActiveTest(EventType.WEBPAGE_TEST, 2);
			else if (cmdtype.equals("youtube"))
				owner.getEventManager().queueActiveTest(EventType.YOUTUBE_TEST, 2);
			else if (cmdtype.equals("vq"))
				owner.getEventManager().queueActiveTest(EventType.EVT_VQ_CALL, 2);
			else if (cmdtype.equals("video"))
				owner.getEventManager().queueActiveTest(EventType.VIDEO_TEST, 2);
			else if (cmdtype.equals("audio"))
				owner.getEventManager().queueActiveTest(EventType.AUDIO_TEST, 2);
			else if (cmdtype.equals("predelay"))  // in case a pre-delay doesn't follow a test, treat as a post delay
				cmdtype = "postdelay";
			else if (!cmdtype.equals("postdelay")) // unrecognized command
			{
				cmdtype = "postdelay";
				loop = 1;
			}


			if (cmdtype.equals("postdelay"))
			{
				handler.postDelayed(new Runnable() {
					public void run() {
						runAdvancedTrackingTests();
					}
				}, (int)(1000 * loop));
			}
			else {
				// check for pre-delay in next index
				int nextIndex = (advancedIndex + 1) % scheduledCommands.length();
				command = scheduledCommands.getJSONObject(nextIndex);
				cmdtype = command.getString("mmctype");
				loop = command.getInt("loop");
				if (cmdtype.equals("predelay")) {
					advancedIndex = (advancedIndex + 1) % scheduledCommands.length();
					handler.postDelayed(new Runnable() {
						public void run() {
							runAdvancedTrackingTests();
						}
					}, (int) (1000 * loop));
				} else if (cmdtype.equals("postdelay"))
				{
					// wait for the test queue to complete
					advancedWaiting = true;

				} else {
					// wait for the test queue to complete
					advancedWaiting = true;
				}
			}
			advancedIndex = (advancedIndex + 1) % scheduledCommands.length();


		}catch (Exception e)
		{
			MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "startTracking", "start:Exception", e);
		}
	}

	public void runTracking () {

		MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "runTracking", "covInterval=" + coverageInterval + ",speedInterval=" + speedtestInterval + ",count="+count + ",videoInterval="+ videoInterval);	

		if (trackingExpires > 0 && trackingExpires+0*280000 <= System.currentTimeMillis())
			cancelScheduledEvents ();
		else if (coverageInterval > 0) {
			owner.getEventManager().triggerSingletonEvent(EventType.MAN_TRACKING);
		}
	}

}

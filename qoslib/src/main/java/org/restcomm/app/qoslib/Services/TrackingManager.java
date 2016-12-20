package org.restcomm.app.qoslib.Services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.preference.PreferenceManager;

import org.restcomm.app.qoslib.MainService;
import org.restcomm.app.qoslib.Services.Intents.IntentHandler;
import com.restcomm.app.utillib.DataObjects.EventType;
import com.restcomm.app.utillib.Utils.LoggerUtil;
import com.restcomm.app.utillib.Utils.PreferenceKeys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author nasrullah
 */
public class TrackingManager {
	public static final String TAG = TrackingManager.class.getSimpleName();
	
	private MainService owner;
	//private Timer timer;
	private Handler handler = new Handler();
	private int trackingElapsed = 0;
	private int durMinutes = 0;
	//private long trackingExpires = 0;
	//private boolean bCoverage = true, bSpeed = false, bTracking = false;
	private boolean bTracking = false;
	private int coverageInterval = 5, speedtestInterval = 0, videoInterval = 0, audioInterval = 0;
	private int webInterval = 0, smsInterval = 0, connectInterval = 0, vqInterval = 0, youtubeInterval = 0;
	private int advancedIndex = 0;
	private boolean advancedWaiting = false;
	private int count = 0;
	private String testTrigger = "";
    private Runnable testRunnable = null;
	private JSONArray scheduledCommands = null;
	
	public TrackingManager(MainService owner) {
		this.owner = owner;
	}
	
	public void resumeTracking ()
	{
		try {
			String cmd = PreferenceManager.getDefaultSharedPreferences(owner).getString(PreferenceKeys.Miscellaneous.DRIVE_TEST_CMD, null);
			//long expires = PreferenceManager.getDefaultSharedPreferences(owner).getLong(PreferenceKeys.Miscellaneous.TRACKING_EXPIRES, 0);
			int elapsed = PreferenceManager.getDefaultSharedPreferences(owner).getInt(PreferenceKeys.Miscellaneous.TRACKING_ELAPSED, 0);
			//advancedIndex = PreferenceManager.getDefaultSharedPreferences(owner).getInt(PreferenceKeys.Miscellaneous.DRIVETEST_INDEX, -1);
			durMinutes = 0;

			if (cmd != null) {
				JSONObject cmdJson = new JSONObject(cmd);

				if (cmdJson.has("schedule"))
				{
					JSONObject testSchedule = cmdJson.getJSONObject("schedule");
					if (testSchedule.getInt("dur") > 0)
					{
						durMinutes = testSchedule.getInt("dur");
						if (elapsed >= durMinutes*60)
							return;
//						if (expires > System.currentTimeMillis()) {
//							int numFiveMinutePeriods = (int) (expires - System.currentTimeMillis());
//							numFiveMinutePeriods = numFiveMinutePeriods / (5 * 60000) + 1;
//
//							testSchedule.put("dur", numFiveMinutePeriods);
//						}
//						else
//							return;
					}
					if (testSchedule.has ("trigger")) {
						testTrigger = testSchedule.getString("trigger");
						//testSchedule.put("dur", 0);
					}
					startAdvancedTracking(cmdJson, 0, false);
				}
				else {
					JSONObject testSettings = cmdJson.getJSONObject("settings");
					if (testSettings.getInt("dur") > 0)
					{
						durMinutes = testSettings.getInt("dur");
						if (elapsed >= durMinutes*60)
							return;
//						{
//							int numFiveMinutePeriods = (int) (expires - System.currentTimeMillis());
//							numFiveMinutePeriods = numFiveMinutePeriods / (5 * 60000) + 1;
//
//							testSettings.put("dur", numFiveMinutePeriods);
//						}
//						else
//							return;
					}

					startTracking(cmdJson, 0, false);
				}
				//trackingExpires = expires;
				trackingElapsed = elapsed;

				LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "resumeTracking", "elapsed=" + elapsed + ",cmd=" + cmd);

			}

		}
		catch(Exception e)
		{
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "resumeTracking", "Exception reading stored tracking settings");
		}

	}

	public void startAdvancedTracking (JSONObject cmdJson, long starttime, boolean resetElapsed) {
		try {
			//JSONObject commands = cmdJson.getJSONObject("commands");
			JSONObject schedule = cmdJson.getJSONObject("schedule");


			if (schedule.has("dur"))
				durMinutes = schedule.getInt("dur");
			else
				durMinutes = 5;
			// If script is starting fresh (not from a trigger), elapsed time is reset to 0
			if (resetElapsed)
				trackingElapsed = 0;
			prevTrackingTime = System.currentTimeMillis();
			//numFiveMinutePeriods = (numFiveMinutePeriods + 4) / 5;
			if (durMinutes == 0)  // continuous tracking
			{
				//trackingExpires = 0;
				// store a tracking expiry date of 10 million seconds from now
				PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.TRACKING_ELAPSED, trackingElapsed).commit();
				PreferenceManager.getDefaultSharedPreferences(owner).edit().putLong(PreferenceKeys.Miscellaneous.ENGINEER_MODE_EXPIRES_TIME, System.currentTimeMillis()).commit();
			}

			if (schedule.has("trigger") && !schedule.getString("trigger").equals(""))
			{
//				testTriggerOpt = schedule.getString("opt");
//				if (testTriggerOpt.equals("once") || testTriggerOpt.equals("always"))
//					testTrigger = "travel";
//				else
//					testTrigger = "";
				testTrigger = schedule.getString("trigger");
				// Initally, when not yet travelling, just wait for travel
				if (!owner.getTravelDetector().isTravelling() || !owner.getTravelDetector().isConfirmed())
					return;
			}
			else
				testTrigger = "";


			if (schedule.has("commands")) {
				// The active test commands will be scheduled in a special
				scheduledCommands = schedule.getJSONArray("commands");
				advancedIndex = 0;
				//bTracking = true;

				// Run the normal Signal tracking as usual
				startCoverageTracking ();
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
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "startTracking", "exception", e);
		}
	}

	public void startCoverageTracking ()
	{
		bTracking = true;
		this.owner.keepAwake(true, true);
		AlarmManager alarmMgr = (AlarmManager) owner.getSystemService( Service.ALARM_SERVICE );
		Intent intent = new Intent(IntentHandler.ACTION_TRACKING_5MINUTE);
		PendingIntent alarm = PendingIntent.getBroadcast( owner, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmMgr.cancel( alarm);
		//trackingExpires = System.currentTimeMillis() + (numFiveMinutePeriods) * 5L * 60L * 1000L;

		//PreferenceManager.getDefaultSharedPreferences(owner).edit().putLong(PreferenceKeys.Miscellaneous.TRACKING_EXPIRES, trackingExpires).commit();
		long expiresTime = System.currentTimeMillis() + (durMinutes*60 - trackingElapsed) * 1000;
		PreferenceManager.getDefaultSharedPreferences(owner).edit().putLong(PreferenceKeys.Miscellaneous.ENGINEER_MODE_EXPIRES_TIME, expiresTime).commit();

		long delay = 0;
		if (durMinutes == 0)  // continuous tracking
		{
			//trackingExpires = 0;
			// store a tracking expiry date of 10 million seconds from now
			//PreferenceManager.getDefaultSharedPreferences(owner).edit().putLong(PreferenceKeys.Miscellaneous.TRACKING_EXPIRES, System.currentTimeMillis()+10000000000l).commit();
			PreferenceManager.getDefaultSharedPreferences(owner).edit().putLong(PreferenceKeys.Miscellaneous.ENGINEER_MODE_EXPIRES_TIME, System.currentTimeMillis()).commit();
		}
		LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "startTracking", "durationMinutes=" + durMinutes + ",covInterval=" + coverageInterval + ",SpeedInterval=" + speedtestInterval + ",videoInterval=" + videoInterval);

		delay = 5L * 60L * 1000L;
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, delay, alarm);
	}
	public void startTracking (JSONObject cmdJson, long starttime, boolean resetElapsed)
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
			if (resetElapsed)
				trackingElapsed = 0;
			prevTrackingTime = System.currentTimeMillis();

			if (testSettings.has("dur"))
				durMinutes = testSettings.getInt("dur");
			else
				durMinutes = 15;
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

			startCoverageTracking ();
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
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, TAG, "startTracking", "exception", e);
		}
	}
	
	public boolean isTracking ()
	{
		//if (timer != null && (trackingExpires > System.currentTimeMillis() || trackingExpires == 0))
		if (bTracking == true) // && (trackingExpires+0*280000 > System.currentTimeMillis() || trackingExpires == 0))
			return true;
		else 
		{
			bTracking = false;
			return false;
		}
	}

	public String getDriveTestTrigger ()
	{
		return testTrigger;
	}
	public void setDriveTestTrigger(String trig)
	{
		testTrigger = trig;
	}

	// A Drive test script may wait and be triggered by travel detection, or other means
	public void triggerDriveTest (String reason, boolean start)
	{
		String cmd = PreferenceManager.getDefaultSharedPreferences(owner).getString(PreferenceKeys.Miscellaneous.DRIVE_TEST_CMD, null);
		if (cmd == null)
			return;
		try {
			JSONObject cmdsJson = new JSONObject(cmd);
			JSONObject testSchedule = cmdsJson.getJSONObject("schedule");

			if (start) {
				// If drive test is to be triggered only once, disable it after starting
//				if (testTriggerOpt != null && testTriggerOpt.equals("once")) {
//					testTriggerOpt = "disabled";
//					testSchedule.put ("opt", "disabled");
//				}
				startAdvancedTracking (cmdsJson, 0, false);
			}
			else {
//				if (testTriggerOpt != null && testTriggerOpt.equals("disabled")) {
//					testTrigger = "";
//				}
				owner.getEventManager().stopTracking();
			}
		}
		catch (JSONException e)
		{
		}
	}

	public int getTestScriptIndex ()
	{
		return advancedIndex;
	}

	public boolean isAdvancedTrackingWaiting ()
	{
		//if (timer != null && (trackingExpires > System.currentTimeMillis() || trackingExpires == 0))
		if (bTracking == true) // && (trackingExpires+0*280000 > System.currentTimeMillis() || trackingExpires == 0))
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
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "cancelScheduledEvents", "");
            bTracking = false;

			if (prevTrackingTime > 0) {
				trackingElapsed += (System.currentTimeMillis() - prevTrackingTime + 500) / 1000;
				PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.TRACKING_ELAPSED, trackingElapsed).commit();
				if (durMinutes > 0 && trackingElapsed >= (durMinutes-1) * 60) {
					testTrigger = "";
				}
			}
			prevTrackingTime = System.currentTimeMillis();

			if (testTrigger.equals(""))
				trackingElapsed = 1000000;
			//trackingExpires = System.currentTimeMillis() - 10000;
			PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.TRACKING_ELAPSED, trackingElapsed).commit();
			PreferenceManager.getDefaultSharedPreferences(owner).edit().putLong(PreferenceKeys.Miscellaneous.ENGINEER_MODE_EXPIRES_TIME, 0L).commit();

            AlarmManager alarmMgr = (AlarmManager) owner.getSystemService( Service.ALARM_SERVICE );
			Intent intent = new Intent(IntentHandler.ACTION_TRACKING_5MINUTE);
			PendingIntent alarm = PendingIntent.getBroadcast( owner, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			alarmMgr.cancel(alarm);
			handler.removeCallbacks(testRunnable);

		}catch (Exception e)
		{
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "cancelScheduledEvents", "Exception", e);
		}
	}

    // every 1 minute, execute drive tests that come due
    public void runTrackingTests ()
    {
		if (trackingElapsed < durMinutes*60 || bTracking == false)
			return;

		try{
			if (bTracking && (durMinutes == 0 || trackingElapsed < durMinutes*60 + 30)) {

				handler.postDelayed(
						testRunnable = new Runnable() {
							public void run() {
								runTrackingTests();
							}
						}, 60L * 1000L);

			}
		}catch (Exception e)
		{
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "startTracking", "start:Exception", e);
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
        LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "runTrackingTests", "count=" + count);



    }

	// Queue a series of tests with delays, then detect when they complete, and repeat them again
	public void runAdvancedTrackingTests ()
	{
		if ((durMinutes > 0 && trackingElapsed >= durMinutes*60) || bTracking == false)
			return;
		if (testTrigger.equals("travel") && !owner.getTravelDetector().isTravelling())
		{
			owner.getEventManager().stopTracking();
			return;
		}

		count += 1;

		try{
			advancedWaiting = false;
			JSONObject command = scheduledCommands.getJSONObject(advancedIndex);
			String cmdtype = command.getString("mmctype");
			int loop = command.getInt("loop");

			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "runAdvancedTrackingTests", "cmdtype=" + cmdtype + " loop=" + loop);

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
			else if (cmdtype.equals("cov"))  // in case a pre-delay doesn't follow a test, treat as a post delay
			{
				cmdtype = "predelay";
				loop = 300;
			}
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
			LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "startTracking", "start:Exception", e);
		}
	}

	private long prevTrackingTime = 0;
	public void runTracking () {

		if (testTrigger.equals("travel") && !owner.getTravelDetector().isTravelling())
		{
			owner.getEventManager().stopTracking();
			return;
		}

		LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, TAG, "runTracking", "covInterval=" + coverageInterval + ",speedInterval=" + speedtestInterval + ",count=" + count + ",videoInterval=" + videoInterval);
		if (prevTrackingTime > 0)
		{
			trackingElapsed += (System.currentTimeMillis() - prevTrackingTime + 500) / 1000;
			PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.TRACKING_ELAPSED, trackingElapsed).commit();
		}
		prevTrackingTime = System.currentTimeMillis();

		if (durMinutes > 0 && trackingElapsed >= (durMinutes)*60)
		{
			cancelScheduledEvents ();
		}
		else if (coverageInterval > 0) {
			owner.getEventManager().triggerSingletonEvent(EventType.MAN_TRACKING);
		}
	}

}

package com.cortxt.app.mmccore.Services.Events;

import android.content.Intent;
import android.preference.PreferenceManager;

import com.cortxt.app.mmccore.MMCService;
import com.cortxt.app.mmccore.Services.Intents.MMCIntentHandler;
import com.cortxt.app.mmcutility.Reporters.ReportManager;
import com.cortxt.app.mmcutility.Utils.CommonIntentBundleKeysOld;
import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.Utils.PreferenceKeys;
import com.cortxt.app.mmccore.R;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;


public class EventResponse {
    private EventResponseContents d;

    public EventResponse(EventResponseContents contents) {
        this.d = contents;

    }

    public EventResponse() {
    }

    ;

    public void init() {
        if (d.extra_settings != null) {
            try {
                JSONArray array_settings = new JSONArray(d.extra_settings);
                for (int i = 0; i < array_settings.length(); i++) {
                    try {
                        JSONObject jobj = array_settings.getJSONObject(i);
                        String name = jobj.getString("name");
                        String value = jobj.getString("value");
                        if (name.equals("hide_tt_share"))
                            d.hide_tt_share = Integer.parseInt(value);
                        if (name.equals("allow_travel_fillin"))
                            d.allow_travel_fillin = Integer.parseInt(value);
                        if (name.equals("use_gcm"))
                            d.use_gcm = Integer.parseInt(value);
                        if (name.equals("use_svcmode"))
                            d.use_svcmode = Integer.parseInt(value);
                        if (name.equals("auto_speedtest"))
                            d.auto_speedtest = Integer.parseInt(value);
                        if (name.equals("speed_month_mb"))
                            d.speed_month_mb = Integer.parseInt(value);
                        if (name.equals("websocket"))
                            d.websocket = Integer.parseInt(value);
                        if (name.equals("hide_compare"))
                            d.hideCompare = Integer.parseInt(value);
                        if (name.equals("hide_map"))
                            d.hideMap = Integer.parseInt(value);
                        if (name.equals("usercovonly"))
                            d.userCovOnly = Integer.parseInt(value);
                        if (name.equals("carriercovonly"))
                            d.carrierCovOnly = Integer.parseInt(value);

                    } catch (Exception e) {
                        MMCLogger.logToFile(MMCLogger.Level.ERROR, "EventResponseContents", "exception in jsonarray " + i, d.extra_settings, e);
                    }
                }
            }
            catch (Exception e) {
                MMCLogger.logToFile(MMCLogger.Level.ERROR, "EventResponseContents", "exception in json ", d.extra_settings, e);
            }
        }
    }

    public long getTimeStamp() {
        return d.TimeStamp;
    }

    public String getData() {
        return d.Data;
    }

    public int getBytes() {
        return d.Bytes;
    }

    public boolean isResult() {
        return d.Result;
    }

    public boolean isNull() {
        if (d == null)
            return true;
        return false;
    }

    public int getSpeedTest() {
        if (d != null)
            return d.SpeedTest;
        else
            return 0;
    }

    public Integer getLevelLimit() {
        if (d.levelLimit != null)
            return d.levelLimit;
        else
            return null;
    }

    public Integer getDormant() {
        if (d.dormant != null)
            return d.dormant;
        else
            return null;
    }

    public Integer getHideRanking() {
        if (d.hideRanking != null)
            return d.hideRanking;
        else
            return null;
    }

    public Integer getHideCompare() {
        if (d.hideCompare != null)
            return d.hideCompare;
        else
            return null;
    }

    public Integer getHideMap() {
        if (d.hideMap != null)
            return d.hideMap;
        else
            return null;
    }

    public Integer getUserCovOnly() {
        if (d.userCovOnly != null)
            return d.userCovOnly;
        else
            return null;
    }

    public Integer getCarrierCovOnly() {
        if (d.carrierCovOnly != null)
            return d.carrierCovOnly;
        else
            return null;
    }

    public Integer getHideCalls() {
        if (d.hideCalls != null)
            return d.hideCalls;
        else
            return null;
    }

    public Integer getHideTweetShare() {
        if (d.hide_tt_share != null)
            return d.hide_tt_share;
        else
            return null;
    }

    public Integer getAllowTravelFillins() {
        if (d.allow_travel_fillin != null)
            return d.allow_travel_fillin;
        else
            return null;
    }

    public Integer getUseGCM() {
        if (d.use_gcm != null)
            return d.use_gcm;
        else
            return null;
    }

    public Integer getUseSvcMode() {
        if (d.use_svcmode != null)
            return d.use_svcmode;
        else
            return null;
    }

    public Integer getAutoSpeedtest() {
        if (d.auto_speedtest != null)
            return d.auto_speedtest;
        else
            return null;
    }
    public Integer getSpeedMonthMB() {
        if (d.speed_month_mb != null)
            return d.speed_month_mb;
        else
            return null;
    }

    public Integer getWebSocket() {
        if (d.websocket != null)
            return d.websocket;
        else
            return null;
    }

    public Integer getAllowBuildings() {
        if (d.allow_buildings != null)
            return d.allow_buildings;
        else
            return null;
    }

    public Integer getAllowTransit() {
        if (d.allow_transit != null)
            return d.allow_transit;
        else
            return null;
    }

    public Integer getAutoConnTests() {
        if (d.auto_conn_test != null)
            return d.auto_conn_test;
        else
            return null;
    }

    public Integer getWifiEvents() {
        if (d.allow_wifi_events != null)
            return d.allow_wifi_events;
        else
            return null;
    }

    public Integer getDropProx() {
        if (d.dropProx != null)
            return d.dropProx;
        else
            return null;
    }

    public Integer getDropPopup() {
        if (d.dropPopup != null)
            return d.dropPopup;
        else
            return null;
    }

    public Integer getPhoneScreenOn() {
        if (d.phoneScreenOn != null)
            return d.phoneScreenOn;
        else
            return null;
    }

    public Integer getPassiveSpeedTestOn() {
        if (d.passiveSpeedTest != null)
            return d.passiveSpeedTest;
        else
            return null;
    }

    public Integer getScreenOnUpdate() {
        if (d.screenOnUpdate != null)
            return d.screenOnUpdate;
        else
            return null;
    }

    public Integer getAllowConfirmation() {
        if (d.allowConfirm != null)
            return d.allowConfirm;
        else
            return null;
    }

    public Integer getDataMonitorAllow() {
        if (d.dataMonitor != null)
            return d.dataMonitor;
        else
            return null;
    }

    public Integer getAppScanSeconds() {
        if (d.appscan_sec != null)
            return d.appscan_sec;
        else
            return null;
    }

    public Boolean getNotifyIconSetting() {
        if (d.notifyAlways != null) {
            if (d.notifyAlways == 0)
                return false;
            else if (d.notifyAlways == 1)
                return true;
        }
        return null;
    }

    public String getDownloadUrl() {
        if (d.downloadurl != null)
            return d.downloadurl;
        else
            return null;
    }

    public String getVideoUrl() {
        if (d.videoUrl != null)
            return d.videoUrl;
        else
            return null;
    }

    public String getAudioUrl() {
        if (d.audio_url != null)
            return d.audio_url;
        else
            return null;
    }

    public String getWebUrl() {
        if (d.web_url != null)
            return d.web_url;
        else
            return null;
    }

    public String getYoutubeId() {
        if (d.youtube_id != null)
            return d.youtube_id;
        else
            return null;
    }

    public String getVoiceTestService() {
        if (d.voicetest_service != null)
            return d.voicetest_service;
        else
            return null;
    }

    public String getCommand() {
        if (d.commands != null && !d.commands.equals("null")) {
            return d.commands;
        } else
            return null;
    }

    public String getSpeedSizes() {
        if (d.speed_sizes != null && !d.speed_sizes.equals("null")) {
            return d.speed_sizes;
        } else
            return null;
    }

    public String getUploadUrl() {
        if (d.uploadurl != null)
            return d.uploadurl;
        else
            return null;
    }

    public String getLatencyUrl() {
        if (d.latencyurl != null)
            return d.latencyurl;
        else
            return null;
    }

    public int getVersion() {
        return d.V;
    }

    public Integer getSurvey() {
        if (d.survey_instance_id != null)
            return d.survey_instance_id;
        else
            return null;
    }

    public String getWifi() {
        return d.Wifi;
    }

    public String getType() {
        return d.__type;
    }

    ;

    public long[] getEventIds() {
        if (d.eventids != null) {
            return d.eventids;
        } else
            return null;
    }

    public void setStartTime (long time)
    {
        d.starttime = time;
    }

    public void handleEventResponse(MMCService owner, boolean commandsOnly)
    {
        if(this.getCommand() != null && this.getCommand().length() > 2) {
            Intent intent = new Intent(MMCIntentHandler.COMMAND);
            String cmds = this.getCommand().toString ();
            intent.putExtra(MMCIntentHandler.COMMAND_EXTRA, cmds);
            intent.putExtra("STARTTIME_EXTRA", d.starttime);
            owner.sendBroadcast(intent);
        }
        if (this.getSurvey() != null && this.getSurvey() > 0)
        {
            Intent intent = new Intent(MMCIntentHandler.SURVEY);
            intent.putExtra(MMCIntentHandler.SURVEY_EXTRA, this.getSurvey());
            owner.sendBroadcast(intent);
        }
        if (commandsOnly == true)
            return;
        if (this.getSpeedTest() > 0)
        {
            Intent intent = new Intent(MMCIntentHandler.SPEED_TEST);
            intent.putExtra(CommonIntentBundleKeysOld.EXTRA_SPEED_TRIGGER, 1);
            intent.putExtra(CommonIntentBundleKeysOld.EXTRA_UPDATE_UI, false);
            owner.sendBroadcast(intent);
        }
        // Server can impose upper limit on Level of Reporting
        if (this.getDormant() != null)
            owner.getUsageLimits().setDormant(this.getDormant());
        if (this.getLevelLimit() != null)
            owner.getUsageLimits().setLevelLimit(this.getLevelLimit());

        // Server can turn ranking feature on/off
        if (this.getHideRanking() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.HIDE_RANKING, this.getHideRanking()).commit();
        if (this.getHideCompare() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.HIDE_COMPARE, this.getHideCompare()).commit();
        if (this.getHideMap() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.HIDE_MAP, this.getHideMap()).commit();
        if (this.getUserCovOnly() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.USER_COV_ONLY, this.getUserCovOnly()).commit();
        if (this.getCarrierCovOnly() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.CARRIER_COV_ONLY, this.getCarrierCovOnly()).commit();
        if (this.getAllowBuildings() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.ALLOW_BUILDINGS, this.getAllowBuildings()).commit();
        if (this.getAllowTransit() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.ALLOW_TRANSIT, this.getAllowTransit()).commit();
        if (this.getAutoConnTests() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.AUTO_CONNECTION_TESTS, this.getAutoConnTests()).commit();
        if (this.getWifiEvents() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.WIFI_EVENTS, this.getWifiEvents()).commit();
        if (this.getHideCalls() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.HIDE_CALLS, this.getHideCalls()).commit();
        if (this.getHideTweetShare() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.HIDE_TWEET_SHARE, this.getHideTweetShare()).commit();
        if (this.getAllowTravelFillins() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.ALLOW_TRAVEL_FILLINS, this.getAllowTravelFillins()).commit();
        if (this.getUseGCM() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.USE_GCM, this.getUseGCM()).commit();
        if (this.getUseSvcMode() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.USE_SVCMODE, this.getUseSvcMode()).commit();

        if (this.getAutoSpeedtest() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.AUTOSPEED_SVR_ENABLE, this.getAutoSpeedtest()).commit();
        if (this.getSpeedMonthMB() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.AUTOSPEED_SVR_SIZEMB, this.getSpeedMonthMB()).commit();

        if (this.getDropProx() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.DROP_PROX, this.getDropProx()).commit();
        if (this.getAllowConfirmation() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.ALLOW_CONFIRMATION, this.getAllowConfirmation()).commit();
        if (this.getVideoUrl() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putString(PreferenceKeys.Miscellaneous.VIDEO_URL, this.getVideoUrl()).commit();
        if (this.getAudioUrl() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putString(PreferenceKeys.Miscellaneous.AUDIO_URL, this.getAudioUrl()).commit();
        if (this.getWebUrl() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putString(PreferenceKeys.Miscellaneous.WEB_URL, this.getWebUrl()).commit();
        if (this.getVoiceTestService() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putString(PreferenceKeys.Miscellaneous.VOICETEST_SERVICE, this.getVoiceTestService()).commit();
        if (this.getSpeedSizes() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putString(PreferenceKeys.Miscellaneous.SPEED_SIZES_JSON, this.getSpeedSizes()).commit();
        if (this.getWebSocket() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.WEBSOCKET, this.getWebSocket()).commit();

        if (this.getYoutubeId() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putString(PreferenceKeys.Miscellaneous.YOUTUBE_VIDEOID, this.getYoutubeId()).commit();

        Integer dropPopup = owner.getResources().getInteger(R.integer.SHOW_DROP_POPUP);
        if (dropPopup == -1 && this.getDropPopup() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.ALLOW_DROP_POPUP, this.getDropPopup()).commit();
        else
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.ALLOW_DROP_POPUP, dropPopup).commit();
        if (this.getDataMonitorAllow() != null)
            owner.manageDataMonitor(this.getDataMonitorAllow(), this.getAppScanSeconds());
        if (this.getNotifyIconSetting() != null)
        {
            boolean userChangedNotify = PreferenceManager.getDefaultSharedPreferences(owner).getBoolean(PreferenceKeys.Miscellaneous.CHANGED_NOTIFY, false);
            if (userChangedNotify == false)
            {
                boolean newNotify = this.getNotifyIconSetting();
                boolean currentNotify = PreferenceManager.getDefaultSharedPreferences(owner).getBoolean(PreferenceKeys.Miscellaneous.ICON_ALWAYS, false);
                if (newNotify != currentNotify)
                {
                    PreferenceManager.getDefaultSharedPreferences(owner).edit().putBoolean(PreferenceKeys.Miscellaneous.ICON_ALWAYS, newNotify).commit();
                    ReportManager.getInstance(owner).setIconBehavior();
                }
            }
        }

        if (this.getPhoneScreenOn() != null)
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.SERVERPHONESCREEN_ENABLE, this.getPhoneScreenOn()).commit();

        if (this.getPassiveSpeedTestOn() != null) {
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.PASSIVE_SPEEDTEST_SERVER, this.getPassiveSpeedTestOn()).commit();
            //if(this.getPassiveSpeedTestOn() == 4)
            //    eventType.setPostEventStageTime(4000);
        }

        if (this.getScreenOnUpdate() != null)
        {
            int screenOnUpdate = PreferenceManager.getDefaultSharedPreferences(owner).getInt(PreferenceKeys.Miscellaneous.SCREEN_ON_UPDATE, 0);
            PreferenceManager.getDefaultSharedPreferences(owner).edit().putInt(PreferenceKeys.Miscellaneous.SCREEN_ON_UPDATE, this.getScreenOnUpdate()).commit();
            if (this.getScreenOnUpdate() != screenOnUpdate) // change in setting?
                owner.modifyWakeLock ();
        }
        if (this.getDownloadUrl() != null && this.getUploadUrl() != null) {
            owner.getEventManager().setSpeedtestUrls(this.getDownloadUrl(), this.getUploadUrl(), this.getLatencyUrl());
        }
        else { //use our URLs
            owner.getEventManager().setSpeedtestUrls(null, null, null);
        }

        if (this.getData() != null) {
            String[] rdata = this.getData().split(",");
            HashMap<String, Integer> handset = ReportManager.getHandsetCaps(owner);
            for (int i = 1; i < rdata.length; i += 2) {
                try {
                    if (rdata[i + 1].length() > 0)
                        handset.put(rdata[i], Integer.parseInt(rdata[i + 1]));
                } catch (Exception e) {
                }
            }
            if (rdata.length > 2)
                PreferenceManager.getDefaultSharedPreferences(owner).edit().putString(PreferenceKeys.Miscellaneous.HANDSET_CAPS, this.getData()).commit();
        }
    }
}

class EventResponseContents {
	public long TimeStamp;
	public String Data;
	public int Bytes;
	public boolean Result;
	public int SpeedTest;
	public int V;
	public Integer dropProx = null, dropPopup = null;
	public Integer notifyAlways;
	public Integer levelLimit = null, hideRanking = null, allowConfirm = null, phoneScreenOn = null, dataMonitor = null, passiveSpeedTest = null;
	public Integer hideCalls = null, hide_tt_share = null;
	public Integer screenOnUpdate = null;
	public Integer allow_buildings = 0, auto_conn_test = 0, allow_wifi_events = 0, allow_transit = 0;
	public Integer survey_instance_id = null;
	public Integer appscan_sec = null;
    public Integer dormant = null;
	public String Wifi;
	public String __type;
	public String downloadurl = null, uploadurl = null, latencyurl = null;
	public String videoUrl = "", voicetest_service = "";
    public String web_url = "";
    public String audio_url = "";//"http://d1l72qawknwf5q.cloudfront.net/speedtest/test_60s.ogg";
    //public String audioUrl = "http://d1l72qawknwf5q.cloudfront.net/speedtest/wearie.mp3";
	public String commands = null;
	public String speed_sizes = null;
	public String extra_settings = null;
    //public String active_tests = null;
	public Integer allow_travel_fillin = null;
    public Integer use_gcm = 0, speed_month_mb = 0, auto_speedtest = 0, use_svcmode = 0;
    public Integer websocket = 0;
    public String youtube_id = "";
	//public long eventId = 0;
	public long[] eventids;
    public long starttime = 0;
    public Integer hideCompare = 0, hideMap = 0, userCovOnly = 0, carrierCovOnly = 0;

	public EventResponseContents(){
		
	}
}

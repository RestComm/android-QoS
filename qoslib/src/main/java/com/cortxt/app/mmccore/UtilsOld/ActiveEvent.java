package com.cortxt.app.mmccore.UtilsOld;

import com.cortxt.app.mmcutility.DataObjects.EventType;

/**
 * Created by bscheurman on 15-08-13.
 */
public enum ActiveEvent {
    SPEED_TEST (EventType.MAN_SPEEDTEST),
    CONNECT_TEST (EventType.LATENCY_TEST),
    SMS_TEST (EventType.SMS_TEST),
    VIDEO_TEST (EventType.VIDEO_TEST),
    YOUTUBE_TEST (EventType.YOUTUBE_TEST),
    WEB_TEST (EventType.WEBPAGE_TEST),
    AUDIO_TEST (EventType.AUDIO_TEST),
    VOICE_QUALITY_TEST (EventType.EVT_VQ_CALL),
    UPDATE_EVENT (EventType.COV_UPDATE),
    COVERAGE_EVENT (EventType.EVT_FILLIN);

    public EventType eventType;
    ActiveEvent(EventType _eventType)
    {
        eventType = _eventType;
    }

}

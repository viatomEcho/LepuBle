package com.lepu.blepro.ext.ventilator;

public class Event {
    private long timestamp;  // 测量时间时间戳 e.g.  0:  1970.01.01 00:00:0时间戳
    private boolean alarm;   // 0-取消告警，1-告警
    private int alarmLevel;  // 告警等级 VentilatorBleCmd.AlarmLevel
    private int eventId;     // 事件id VentilatorBleCmd.EventId

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAlarm() {
        return alarm;
    }

    public void setAlarm(boolean alarm) {
        this.alarm = alarm;
    }

    public int getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(int alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return "Event{" +
                "timestamp=" + timestamp +
                ", alarm=" + alarm +
                ", alarmLevel=" + alarmLevel +
                ", eventId=" + eventId +
                '}';
    }
}

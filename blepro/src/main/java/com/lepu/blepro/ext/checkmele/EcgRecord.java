package com.lepu.blepro.ext.checkmele;

public class EcgRecord {
    private long timestamp;  // 时间戳 秒s
    private String recordName;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private int measureMode;  // 测量方式 1：Hand-Hand，2：Hand-Chest，3：1-Lead，4：2-Lead
    private String measureModeMess;
    private boolean normal;   // true：good，false：bad

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRecordName() {
        return recordName;
    }

    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getMeasureMode() {
        return measureMode;
    }

    public void setMeasureMode(int measureMode) {
        this.measureMode = measureMode;
    }

    public String getMeasureModeMess() {
        return measureModeMess;
    }

    public void setMeasureModeMess(String measureModeMess) {
        this.measureModeMess = measureModeMess;
    }

    public boolean isNormal() {
        return normal;
    }

    public void setNormal(boolean normal) {
        this.normal = normal;
    }

    @Override
    public String toString() {
        return "EcgRecord{" +
                "timestamp=" + timestamp +
                ", recordName='" + recordName + '\'' +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                ", second=" + second +
                ", measureMode=" + measureMode +
                ", measureModeMess='" + measureModeMess + '\'' +
                ", normal=" + normal +
                '}';
    }
}

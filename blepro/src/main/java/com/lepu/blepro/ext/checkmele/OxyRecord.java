package com.lepu.blepro.ext.checkmele;

public class OxyRecord {

    private long timestamp;
    private String recordName;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private int measureMode;         // 测量模式 0：内部，1：外部
    private String measureModeMess;
    private int spo2;               // 血氧值（0-100，单位为%）
    private int pr;                 // PR值（0-255）
    private float pi;               // PI值（实际为一位小数的值，单位为%，此处使用整数表示，如12.5%则用125表示）
    private boolean normal;          // true：good，false：bad


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

    public int getSpo2() {
        return spo2;
    }

    public void setSpo2(int spo2) {
        this.spo2 = spo2;
    }

    public int getPr() {
        return pr;
    }

    public void setPr(int pr) {
        this.pr = pr;
    }

    public float getPi() {
        return pi;
    }

    public void setPi(float pi) {
        this.pi = pi;
    }

    public boolean isNormal() {
        return normal;
    }

    public void setNormal(boolean normal) {
        this.normal = normal;
    }

    @Override
    public String toString() {
        return "OxyRecord{" +
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
                ", spo2=" + spo2 +
                ", pr=" + pr +
                ", pi=" + pi +
                ", normal=" + normal +
                '}';
    }
}

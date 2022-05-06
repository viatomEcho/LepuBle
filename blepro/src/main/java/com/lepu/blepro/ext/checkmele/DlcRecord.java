package com.lepu.blepro.ext.checkmele;

public class DlcRecord {

    private long timestamp;    // 时间戳 秒s
    private String recordName;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private int hr;
    private boolean ecgNormal;
    private int spo2;
    private float pi;
    private boolean oxyNormal;

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

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public boolean isEcgNormal() {
        return ecgNormal;
    }

    public void setEcgNormal(boolean ecgNormal) {
        this.ecgNormal = ecgNormal;
    }

    public int getSpo2() {
        return spo2;
    }

    public void setSpo2(int spo2) {
        this.spo2 = spo2;
    }

    public float getPi() {
        return pi;
    }

    public void setPi(float pi) {
        this.pi = pi;
    }

    public boolean isOxyNormal() {
        return oxyNormal;
    }

    public void setOxyNormal(boolean oxyNormal) {
        this.oxyNormal = oxyNormal;
    }

    @Override
    public String toString() {
        return "DlcRecord{" +
                "timestamp=" + timestamp +
                ", recordName='" + recordName + '\'' +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                ", second=" + second +
                ", hr=" + hr +
                ", ecgNormal=" + ecgNormal +
                ", spo2=" + spo2 +
                ", pi=" + pi +
                ", oxyNormal=" + oxyNormal +
                '}';
    }
}

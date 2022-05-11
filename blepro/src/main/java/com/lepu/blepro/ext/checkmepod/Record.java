package com.lepu.blepro.ext.checkmepod;

public class Record {
    private long timestamp;
    private String recordName;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private int leadType;
    private String leadTypeMess;
    private int spo2;
    private int pr;
    private float pi;
    private float temp;

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

    public int getLeadType() {
        return leadType;
    }

    public void setLeadType(int leadType) {
        this.leadType = leadType;
    }

    public String getLeadTypeMess() {
        return leadTypeMess;
    }

    public void setLeadTypeMess(String leadTypeMess) {
        this.leadTypeMess = leadTypeMess;
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

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    @Override
    public String toString() {
        return "Record{" +
                "timestamp=" + timestamp +
                ", recordName='" + recordName + '\'' +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                ", second=" + second +
                ", leadType=" + leadType +
                ", leadTypeMess='" + leadTypeMess + '\'' +
                ", spo2=" + spo2 +
                ", pr=" + pr +
                ", pi=" + pi +
                ", temp=" + temp +
                '}';
    }
}

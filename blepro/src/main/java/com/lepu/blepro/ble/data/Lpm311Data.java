package com.lepu.blepro.ble.data;

import java.util.Arrays;

public class Lpm311Data {

    private byte[] bytes;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private double chol;
    private String cholStr;
    private double hdl;
    private String hdlStr;
    private double trig;
    private String trigStr;
    private double ldl;
    private String ldlStr;
    private double cholDivHdl;
    private String cholDivHdlStr;
    private int unit;           // 0：mmol_L，1：mg_dL
    private String user;

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
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

    public double getChol() {
        return chol;
    }

    public void setChol(double chol) {
        this.chol = chol;
    }

    public double getHdl() {
        return hdl;
    }

    public void setHdl(double hdl) {
        this.hdl = hdl;
    }

    public double getTrig() {
        return trig;
    }

    public void setTrig(double trig) {
        this.trig = trig;
    }

    public double getLdl() {
        return ldl;
    }

    public void setLdl(double ldl) {
        this.ldl = ldl;
    }

    public double getCholDivHdl() {
        return cholDivHdl;
    }

    public void setCholDivHdl(double cholDivHdl) {
        this.cholDivHdl = cholDivHdl;
    }

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCholStr() {
        return cholStr;
    }

    public void setCholStr(String cholStr) {
        this.cholStr = cholStr;
    }

    public String getHdlStr() {
        return hdlStr;
    }

    public void setHdlStr(String hdlStr) {
        this.hdlStr = hdlStr;
    }

    public String getTrigStr() {
        return trigStr;
    }

    public void setTrigStr(String trigStr) {
        this.trigStr = trigStr;
    }

    public String getLdlStr() {
        return ldlStr;
    }

    public void setLdlStr(String ldlStr) {
        this.ldlStr = ldlStr;
    }

    public String getCholDivHdlStr() {
        return cholDivHdlStr;
    }

    public void setCholDivHdlStr(String cholDivHdlStr) {
        this.cholDivHdlStr = cholDivHdlStr;
    }

    @Override
    public String toString() {
        return "Lpm311Data{" +
                "bytes=" + Arrays.toString(bytes) +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                ", second=" + second +
                ", chol=" + chol +
                ", cholStr=" + cholStr +
                ", hdl=" + hdl +
                ", hdlStr=" + hdlStr +
                ", trig=" + trig +
                ", trigStr=" + trigStr +
                ", ldl=" + ldl +
                ", ldlStr=" + ldlStr +
                ", cholDivHdl=" + cholDivHdl +
                ", cholDivHdlStr=" + cholDivHdlStr +
                ", unit=" + unit +
                ", user='" + user + '\'' +
                '}';
    }
}

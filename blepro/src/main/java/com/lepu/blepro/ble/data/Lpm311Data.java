package com.lepu.blepro.ble.data;

public class Lpm311Data {

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private double chol;
    private double hdl;
    private double trig;
    private double ldl;
    private double cholDivHdl;
    private int unit;
    private String user;

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

    @Override
    public String toString() {
        return "Lpm311Data{" +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                ", second=" + second +
                ", chol=" + chol +
                ", hdl=" + hdl +
                ", trig=" + trig +
                ", ldl=" + ldl +
                ", cholDivHdl=" + cholDivHdl +
                ", unit=" + unit +
                ", user='" + user + '\'' +
                '}';
    }
}

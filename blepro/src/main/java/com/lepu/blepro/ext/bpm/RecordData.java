package com.lepu.blepro.ext.bpm;

public class RecordData {

    private int sys;
    private int dia;
    private boolean irregularHrFlag;
    private int pr;
    private int deviceUserId;  // 用户id
    private int storeId;       // 数据序号
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    public int getSys() {
        return sys;
    }

    public void setSys(int sys) {
        this.sys = sys;
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public boolean isIrregularHrFlag() {
        return irregularHrFlag;
    }

    public void setIrregularHrFlag(boolean irregularHrFlag) {
        this.irregularHrFlag = irregularHrFlag;
    }

    public int getPr() {
        return pr;
    }

    public void setPr(int pr) {
        this.pr = pr;
    }

    public int getDeviceUserId() {
        return deviceUserId;
    }

    public void setDeviceUserId(int deviceUserId) {
        this.deviceUserId = deviceUserId;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
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

    @Override
    public String toString() {
        return "RecordData{" +
                "sys=" + sys +
                ", dia=" + dia +
                ", irregularHrFlag=" + irregularHrFlag +
                ", pr=" + pr +
                ", deviceUserId=" + deviceUserId +
                ", storeId=" + storeId +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                '}';
    }
}

package com.lepu.blepro.ext.pc80b;

import java.util.Arrays;

public class EcgFile {
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private byte[] ecgBytes;
    private int[] ecgInts;
    private float[] ecgFloats;
    private int hr;
    private float gain;
    private int result;
    private String resultMess;
    private int filterMode;

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

    public byte[] getEcgBytes() {
        return ecgBytes;
    }

    public void setEcgBytes(byte[] ecgBytes) {
        this.ecgBytes = ecgBytes;
    }

    public int[] getEcgInts() {
        return ecgInts;
    }

    public void setEcgInts(int[] ecgInts) {
        this.ecgInts = ecgInts;
    }

    public float[] getEcgFloats() {
        return ecgFloats;
    }

    public void setEcgFloats(float[] ecgFloats) {
        this.ecgFloats = ecgFloats;
    }

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public float getGain() {
        return gain;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getResultMess() {
        return resultMess;
    }

    public void setResultMess(String resultMess) {
        this.resultMess = resultMess;
    }

    public int getFilterMode() {
        return filterMode;
    }

    public void setFilterMode(int filterMode) {
        this.filterMode = filterMode;
    }

    @Override
    public String toString() {
        return "EcgFile{" +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                ", second=" + second +
                ", ecgBytes=" + Arrays.toString(ecgBytes) +
                ", ecgFloats=" + Arrays.toString(ecgFloats) +
                ", hr=" + hr +
                ", gain=" + gain +
                ", result=" + result +
                ", resultMess='" + resultMess + '\'' +
                ", filterMode=" + filterMode +
                '}';
    }
}

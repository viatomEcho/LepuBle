package com.lepu.blepro.ext.oxy;

import java.util.Arrays;

public class RtWave {

    private int spo2;
    private int pr;
    private int battery;
    private int batteryState;
    private float pi;
    private int state;
    private int len;
    private byte[] waveByte;
    private int[] wFs;

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

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getBatteryState() {
        return batteryState;
    }

    public void setBatteryState(int batteryState) {
        this.batteryState = batteryState;
    }

    public float getPi() {
        return pi;
    }

    public void setPi(float pi) {
        this.pi = pi;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public byte[] getWaveByte() {
        return waveByte;
    }

    public void setWaveByte(byte[] waveByte) {
        this.waveByte = waveByte;
    }

    public int[] getWFs() {
        return wFs;
    }

    public void setWFs(int[] wFs) {
        this.wFs = wFs;
    }

    @Override
    public String toString() {
        return "RtWave{" +
                "spo2=" + spo2 +
                ", pr=" + pr +
                ", battery=" + battery +
                ", batteryState=" + batteryState +
                ", pi=" + pi +
                ", state=" + state +
                ", len=" + len +
                ", waveByte=" + Arrays.toString(waveByte) +
                ", wFs=" + Arrays.toString(wFs) +
                '}';
    }
}

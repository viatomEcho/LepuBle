package com.lepu.blepro.ext;

public class FhrInfo {
    private String deviceName;  // 设备名称
    private int hr;             // 心率数据（心率测量范围为60~240，0表示无信号，255表示超出量程）
    private int volume;         // 音量数据（0-6）
    private int strength;       // 心音强度数据（0-2）
    private int battery;        // 电量数据（0-6）不准确，设备问题

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    @Override
    public String toString() {
        return "FhrInfo{" +
                "deviceName='" + deviceName + '\'' +
                ", hr=" + hr +
                ", volume=" + volume +
                ", strength=" + strength +
                ", battery=" + battery +
                '}';
    }
}

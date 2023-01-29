package com.lepu.blepro.ext.bp2;

public class RtStatus {
    private int deviceStatus;
    private String deviceStatusMsg;
    private int batteryStatus;
    private String batteryStatusMsg;
    private int percent;
    private float vol;

    public int getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(int deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public String getDeviceStatusMsg() {
        return deviceStatusMsg;
    }

    public void setDeviceStatusMsg(String deviceStatusMsg) {
        this.deviceStatusMsg = deviceStatusMsg;
    }

    public int getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(int batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public String getBatteryStatusMsg() {
        return batteryStatusMsg;
    }

    public void setBatteryStatusMsg(String batteryStatusMsg) {
        this.batteryStatusMsg = batteryStatusMsg;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public float getVol() {
        return vol;
    }

    public void setVol(float vol) {
        this.vol = vol;
    }

    @Override
    public String toString() {
        return "RtStatus{" +
                "deviceStatus=" + deviceStatus +
                ", deviceStatusMsg='" + deviceStatusMsg + '\'' +
                ", batteryStatus=" + batteryStatus +
                ", batteryStatusMsg=" + batteryStatusMsg +
                ", percent=" + percent +
                ", vol=" + vol +
                '}';
    }
}

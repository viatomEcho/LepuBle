package com.lepu.blepro.ext.pc303;

public class DeviceInfo {
    private int deviceId;
    private String deviceName;
    private String softwareV;
    private String hardwareV;
    private int batLevel;     // 电量等级 0-3
    private int batStatus;    // 电池状态 0：正常，1：充电中，2：已充满

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getSoftwareV() {
        return softwareV;
    }

    public void setSoftwareV(String softwareV) {
        this.softwareV = softwareV;
    }

    public String getHardwareV() {
        return hardwareV;
    }

    public void setHardwareV(String hardwareV) {
        this.hardwareV = hardwareV;
    }

    public int getBatLevel() {
        return batLevel;
    }

    public void setBatLevel(int batLevel) {
        this.batLevel = batLevel;
    }

    public int getBatStatus() {
        return batStatus;
    }

    public void setBatStatus(int batStatus) {
        this.batStatus = batStatus;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "deviceId=" + deviceId +
                ", deviceName='" + deviceName + '\'' +
                ", softwareV='" + softwareV + '\'' +
                ", hardwareV='" + hardwareV + '\'' +
                ", batLevel=" + batLevel +
                ", batStatus=" + batStatus +
                '}';
    }
}

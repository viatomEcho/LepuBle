package com.lepu.blepro.ext.pc68b;

public class DeviceInfo {
    private String softwareV;
    private String hardwareV;
    private String deviceName;

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

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "softwareV='" + softwareV + '\'' +
                ", hardwareV='" + hardwareV + '\'' +
                ", deviceName='" + deviceName + '\'' +
                '}';
    }
}

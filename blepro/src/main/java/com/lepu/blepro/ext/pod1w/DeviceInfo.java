package com.lepu.blepro.ext.pod1w;

public class DeviceInfo {
    private String deviceName;
    private String sn;
    private String softwareV;
    private String hardwareV;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
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

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "deviceName='" + deviceName + '\'' +
                ", sn='" + sn + '\'' +
                ", softwareV='" + softwareV + '\'' +
                ", hardwareV='" + hardwareV + '\'' +
                '}';
    }
}

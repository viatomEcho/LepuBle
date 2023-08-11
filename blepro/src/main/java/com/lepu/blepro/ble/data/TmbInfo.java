package com.lepu.blepro.ble.data;

public class TmbInfo {
    private String manufacturer;
    private String name;
    private String serial;
    private String hv;
    private String fv;
    private String sv;
    private int userId;
    private String deviceId;
    private int battery;

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getHv() {
        return hv;
    }

    public void setHv(String hv) {
        this.hv = hv;
    }

    public String getFv() {
        return fv;
    }

    public void setFv(String fv) {
        this.fv = fv;
    }

    public String getSv() {
        return sv;
    }

    public void setSv(String sv) {
        this.sv = sv;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    @Override
    public String toString() {
        return "TmbInfo{" +
                "manufacturer='" + manufacturer + '\'' +
                ", name='" + name + '\'' +
                ", serial='" + serial + '\'' +
                ", hv='" + hv + '\'' +
                ", fv='" + fv + '\'' +
                ", sv='" + sv + '\'' +
                ", userId=" + userId +
                ", deviceId='" + deviceId + '\'' +
                ", battery=" + battery +
                '}';
    }
}

package com.lepu.blepro.ext.aoj20a;

public class DeviceInfo {

    private int mode;
    private String modeMess;
    private int battery;
    private String version;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getModeMess() {
        return modeMess;
    }

    public void setModeMess(String modeMess) {
        this.modeMess = modeMess;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "mode=" + mode +
                ", modeMess='" + modeMess + '\'' +
                ", battery=" + battery +
                ", version='" + version + '\'' +
                '}';
    }
}

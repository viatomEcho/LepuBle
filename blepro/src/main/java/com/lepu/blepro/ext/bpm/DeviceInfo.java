package com.lepu.blepro.ext.bpm;

public class DeviceInfo {

    private int mainVersion;
    private int secondVersion;
    private String version;

    public int getMainVersion() {
        return mainVersion;
    }

    public void setMainVersion(int mainVersion) {
        this.mainVersion = mainVersion;
    }

    public int getSecondVersion() {
        return secondVersion;
    }

    public void setSecondVersion(int secondVersion) {
        this.secondVersion = secondVersion;
    }

    public String getVersion() {
        return mainVersion+"."+secondVersion;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "mainVersion=" + mainVersion +
                ", secondVersion=" + secondVersion +
                ", version='" + getVersion() + '\'' +
                '}';
    }
}

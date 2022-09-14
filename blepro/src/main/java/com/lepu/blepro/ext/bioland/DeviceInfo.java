package com.lepu.blepro.ext.bioland;

public class DeviceInfo {

    private String version;    // 版本号
    private int customerType;  // 客户代码（0：苹果，1：爱奥乐，2：海尔，3：无，4：小米，5：道通，6：KANWEI）
    private int battery;       // 电量（0-100%）
    private int deviceType;    // 设备类型（1：血压计，2：血糖仪）
    private int deviceCode;    // 设备型号
    private String sn;         // 9位

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getCustomerType() {
        return customerType;
    }

    public void setCustomerType(int customerType) {
        this.customerType = customerType;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(int deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "version='" + version + '\'' +
                ", customerType=" + customerType +
                ", battery=" + battery +
                ", deviceType=" + deviceType +
                ", deviceCode=" + deviceCode +
                ", sn='" + sn + '\'' +
                '}';
    }
}

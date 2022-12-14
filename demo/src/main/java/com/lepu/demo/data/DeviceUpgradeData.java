package com.lepu.demo.data;

public class DeviceUpgradeData {

    // 数据保存时间
    private String time;
    
    // 蓝牙名
    private String name;

    // 地址
    private String address;

    // sn
    private String sn;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    @Override
    public String toString() {
        return "DeviceUpgradeData{" +
                "\"time\":" + "\"" + time + "\"" +
                ",\"name\":" + "\"" + name + "\"" +
                ",\"address\":" + "\"" + address + "\"" +
                ",\"sn\":" + "\"" + sn + "\"" +
                "}";
    }
}

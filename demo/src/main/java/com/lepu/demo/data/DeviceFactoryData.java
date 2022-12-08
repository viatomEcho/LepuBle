package com.lepu.demo.data;

public class DeviceFactoryData {

    // 数据保存时间
    private String time;
    
    // 蓝牙名
    private String name;

    // 地址
    private String address;

    // sn
    private String sn;

    // code
    private String code;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "DeviceFactoryData{" +
                "\"time\":" + "\"" + time + "\"" +
                ",\"name\":" + "\"" + name + "\"" +
                ",\"address\":" + "\"" + address + "\"" +
                ",\"sn\":" + "\"" + sn + "\"" +
                ",\"code\":" + "\"" + code + "\"" +
                "}";
    }
}

package com.lepu.blepro.ext.bp2w;

public class Bp2Wifi {
    private int state;           // 0:断开 1:连接中 2:已连接 0xff:密码错误 0xfd:找不到SSID
    private int ssidLen;
    private String ssid;         // ssid
    private int type;            // wifi类型	0:2.4G   1:5G
    private int rssi;            // 信号强度
    private int pwdLen;
    private String pwd;
    private String macAddr;      // wifi模块mac地址
    private int ipType;          // ip类型 0动态 1静态
    private int ipLen;
    private String ipAddr;       // ip信息
    private int netmaskLen;
    private String netmaskAddr;  // 子网掩码
    private int gatewayLen;
    private String gatewayAddr;  // 网关

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getSsidLen() {
        return ssidLen;
    }

    public void setSsidLen(int ssidLen) {
        this.ssidLen = ssidLen;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getPwdLen() {
        return pwdLen;
    }

    public void setPwdLen(int pwdLen) {
        this.pwdLen = pwdLen;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getMacAddr() {
        return macAddr;
    }

    public void setMacAddr(String macAddr) {
        this.macAddr = macAddr;
    }

    public int getIpType() {
        return ipType;
    }

    public void setIpType(int ipType) {
        this.ipType = ipType;
    }

    public int getIpLen() {
        return ipLen;
    }

    public void setIpLen(int ipLen) {
        this.ipLen = ipLen;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public int getNetmaskLen() {
        return netmaskLen;
    }

    public void setNetmaskLen(int netmaskLen) {
        this.netmaskLen = netmaskLen;
    }

    public String getNetmaskAddr() {
        return netmaskAddr;
    }

    public void setNetmaskAddr(String netmaskAddr) {
        this.netmaskAddr = netmaskAddr;
    }

    public int getGatewayLen() {
        return gatewayLen;
    }

    public void setGatewayLen(int gatewayLen) {
        this.gatewayLen = gatewayLen;
    }

    public String getGatewayAddr() {
        return gatewayAddr;
    }

    public void setGatewayAddr(String gatewayAddr) {
        this.gatewayAddr = gatewayAddr;
    }

    @Override
    public String toString() {
        return "Bp2Wifi{" +
                "state=" + state +
                ", ssidLen=" + ssidLen +
                ", ssid='" + ssid + '\'' +
                ", type=" + type +
                ", rssi=" + rssi +
                ", pwdLen=" + pwdLen +
                ", pwd='" + pwd + '\'' +
                ", macAddr='" + macAddr + '\'' +
                ", ipType=" + ipType +
                ", ipLen=" + ipLen +
                ", ipAddr='" + ipAddr + '\'' +
                ", netmaskLen=" + netmaskLen +
                ", netmaskAddr='" + netmaskAddr + '\'' +
                ", gatewayLen=" + gatewayLen +
                ", gatewayAddr='" + gatewayAddr + '\'' +
                '}';
    }
}

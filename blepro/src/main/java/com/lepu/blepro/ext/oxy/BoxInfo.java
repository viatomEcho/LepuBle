package com.lepu.blepro.ext.oxy;

public class BoxInfo {

    private char hwVersion;    // 硬件版本
    private String swVersion;    // 软件版本
    private String btlVersion;    // 引导版本
    private String branchCode;    // Branch编码
    private int fileVer;       // 文件解析协议版本
    private int deviceType;        // 设备类型
    private String spcpVer;       // 蓝牙通讯协议版本
    private String curTime;         // 时间
    private int protocolMaxLen;    // 通信协议数据段最大长度
    private int snLen;  // SN长度(小于18)
    private String sn;           // 序列号

    public char getHwVersion() {
        return hwVersion;
    }

    public void setHwVersion(char hwVersion) {
        this.hwVersion = hwVersion;
    }

    public String getSwVersion() {
        return swVersion;
    }

    public void setSwVersion(String swVersion) {
        this.swVersion = swVersion;
    }

    public String getBtlVersion() {
        return btlVersion;
    }

    public void setBtlVersion(String btlVersion) {
        this.btlVersion = btlVersion;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public int getFileVer() {
        return fileVer;
    }

    public void setFileVer(int fileVer) {
        this.fileVer = fileVer;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public String getSpcpVer() {
        return spcpVer;
    }

    public void setSpcpVer(String spcpVer) {
        this.spcpVer = spcpVer;
    }

    public String getCurTime() {
        return curTime;
    }

    public void setCurTime(String curTime) {
        this.curTime = curTime;
    }

    public int getProtocolMaxLen() {
        return protocolMaxLen;
    }

    public void setProtocolMaxLen(int protocolMaxLen) {
        this.protocolMaxLen = protocolMaxLen;
    }

    public int getSnLen() {
        return snLen;
    }

    public void setSnLen(int snLen) {
        this.snLen = snLen;
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
                "hwVersion=" + hwVersion +
                ", swVersion='" + swVersion + '\'' +
                ", btlVersion='" + btlVersion + '\'' +
                ", branchCode='" + branchCode + '\'' +
                ", fileVer=" + fileVer +
                ", deviceType=" + deviceType +
                ", spcpVer='" + spcpVer + '\'' +
                ", curTime='" + curTime + '\'' +
                ", protocolMaxLen=" + protocolMaxLen +
                ", snLen=" + snLen +
                ", sn='" + sn + '\'' +
                '}';
    }

}

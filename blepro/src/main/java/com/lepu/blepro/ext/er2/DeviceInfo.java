package com.lepu.blepro.ext.er2;

public class DeviceInfo {

    private String hwVersion;    // 硬件版本
    private String swVersion;    // 软件版本
    private String btlVersion;    // 引导版本
    private String branchCode;    // Branch编码
    private String spcpVer;       // 蓝牙通讯协议版本
    private int snLen;  // SN长度(小于18)
    private String sn;           // 序列号

    public String getHwVersion() {
        return hwVersion;
    }

    public void setHwVersion(String hwVersion) {
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

    public String getSpcpVer() {
        return spcpVer;
    }

    public void setSpcpVer(String spcpVer) {
        this.spcpVer = spcpVer;
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
                ", spcpVer='" + spcpVer + '\'' +
                ", snLen=" + snLen +
                ", sn='" + sn + '\'' +
                '}';
    }

}

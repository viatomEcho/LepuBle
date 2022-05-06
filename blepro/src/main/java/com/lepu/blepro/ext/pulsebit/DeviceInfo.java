package com.lepu.blepro.ext.pulsebit;

public class DeviceInfo {

    private String region;        // 地区版本
    private String model;         // 系列版本
    private String hwVersion;    // 硬件版本
    private String swVersion;    // 软件版本
    private String lgVersion;    // 语言版本
    private String curLanguage;  // 语言版本
    private String sn;           // 序列号
    private String fileVer;       // 文件解析协议版本
    private String spcpVer;       // 蓝牙通讯协议版本
    private String branchCode;    // code码
    private String application;   //

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

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

    public String getLgVersion() {
        return lgVersion;
    }

    public void setLgVersion(String lgVersion) {
        this.lgVersion = lgVersion;
    }

    public String getCurLanguage() {
        return curLanguage;
    }

    public void setCurLanguage(String curLanguage) {
        this.curLanguage = curLanguage;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getFileVer() {
        return fileVer;
    }

    public void setFileVer(String fileVer) {
        this.fileVer = fileVer;
    }

    public String getSpcpVer() {
        return spcpVer;
    }

    public void setSpcpVer(String spcpVer) {
        this.spcpVer = spcpVer;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "region='" + region + '\'' +
                ", model='" + model + '\'' +
                ", hwVersion='" + hwVersion + '\'' +
                ", swVersion='" + swVersion + '\'' +
                ", lgVersion='" + lgVersion + '\'' +
                ", curLanguage='" + curLanguage + '\'' +
                ", sn='" + sn + '\'' +
                ", fileVer='" + fileVer + '\'' +
                ", spcpVer='" + spcpVer + '\'' +
                ", branchCode='" + branchCode + '\'' +
                ", application='" + application + '\'' +
                '}';
    }
}

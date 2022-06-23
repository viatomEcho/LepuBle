package com.lepu.blepro.ext.oxy;

public class DeviceInfo {

    private String region;        // 地区版本
    private String model;        // 系列版本
    private String hwVersion;    // 硬件版本
    private String swVersion;    // 软件版本
    private String btlVersion;   // 引导版本
    private int pedTar;          // 步数
    private String sn;           // 序列号
    private String curTime;      // 时间
    private int batteryState;    // 电池状态（0为正常使用，1为充电，2为充满）
    private String batteryValue;  // 电量（0%-100%）
    private int oxiThr;          // 血氧阈值
    private int motor;           // 强度（KidsO2、Oxylink：最低：5，低：10，中：17，高：22，最高：35；O2Ring：最低：20，低：40，中：60，高：80，最高：100，震动强度不随开关的改变而改变）
    private int mode;            // 工作模式（0：sleep模式  1：monitor模式）
    private String fileList;     // 文件列表
    private int oxiSwitch;        // 血氧开关（bit0:震动  bit1:声音）(int 0：震动关声音关 1：震动开声音关 2：震动关声音开 3：震动开声音开)
    private int hrSwitch;         // 心率开关（bit0:震动  bit1:声音）(int 0：震动关声音关 1：震动开声音关 2：震动关声音开 3：震动开声音开)
    private int hrLowThr;         // 心率震动最低阈值
    private int hrHighThr;        // 心率震动最高阈值
    private String fileVer;       // 文件解析协议版本
    private String spcpVer;       // 蓝牙通讯协议版本
    private int curState;         // 运行状态（0:准备阶段 可升级不可获取波形 1:测量就绪 可升级和获取波形 2:记录数据中 不可升级可获取波形）
    private int lightingMode;     // 亮屏模式（0：Standard模式，1：Always Off模式，2：Always On模式）
    private int lightStr;         // 屏幕亮度
    private String branchCode;    // code码
    private int spo2Switch;     // 血氧功能开关（0：关 1：开）
    private int buzzer;          // 声音强度（checkO2Plus：最低：20，低：40，中：60，高：80，最高：100）
    private int mtSwitch;        // 体动开关（0：关 1：开）
    private int mtThr;           // 体动阈值
    private int ivSwitch;        // 无效值报警开关（0：关 1：开）
    private int ivThr;           // 无效值报警告警时间阈值

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

    public String getBtlVersion() {
        return btlVersion;
    }

    public void setBtlVersion(String btlVersion) {
        this.btlVersion = btlVersion;
    }

    public int getPedTar() {
        return pedTar;
    }

    public void setPedTar(int pedTar) {
        this.pedTar = pedTar;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getCurTime() {
        return curTime;
    }

    public void setCurTime(String curTime) {
        this.curTime = curTime;
    }

    public int getBatteryState() {
        return batteryState;
    }

    public void setBatteryState(int batteryState) {
        this.batteryState = batteryState;
    }

    public String getBatteryValue() {
        return batteryValue;
    }

    public void setBatteryValue(String batteryValue) {
        this.batteryValue = batteryValue;
    }

    public int getOxiThr() {
        return oxiThr;
    }

    public void setOxiThr(int oxiThr) {
        this.oxiThr = oxiThr;
    }

    public int getMotor() {
        return motor;
    }

    public void setMotor(int motor) {
        this.motor = motor;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getFileList() {
        return fileList;
    }

    public void setFileList(String fileList) {
        this.fileList = fileList;
    }

    public int getOxiSwitch() {
        return oxiSwitch;
    }

    public void setOxiSwitch(int oxiSwitch) {
        this.oxiSwitch = oxiSwitch;
    }

    public int getHrSwitch() {
        return hrSwitch;
    }

    public void setHrSwitch(int hrSwitch) {
        this.hrSwitch = hrSwitch;
    }

    public int getHrLowThr() {
        return hrLowThr;
    }

    public void setHrLowThr(int hrLowThr) {
        this.hrLowThr = hrLowThr;
    }

    public int getHrHighThr() {
        return hrHighThr;
    }

    public void setHrHighThr(int hrHighThr) {
        this.hrHighThr = hrHighThr;
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

    public int getCurState() {
        return curState;
    }

    public void setCurState(int curState) {
        this.curState = curState;
    }

    public int getLightingMode() {
        return lightingMode;
    }

    public void setLightingMode(int lightingMode) {
        this.lightingMode = lightingMode;
    }

    public int getLightStr() {
        return lightStr;
    }

    public void setLightStr(int lightStr) {
        this.lightStr = lightStr;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public int getSpo2Switch() {
        return spo2Switch;
    }

    public void setSpo2Switch(int spo2Switch) {
        this.spo2Switch = spo2Switch;
    }

    public int getBuzzer() {
        return buzzer;
    }

    public void setBuzzer(int buzzer) {
        this.buzzer = buzzer;
    }

    public int getMtSwitch() {
        return mtSwitch;
    }

    public void setMtSwitch(int mtSwitch) {
        this.mtSwitch = mtSwitch;
    }

    public int getMtThr() {
        return mtThr;
    }

    public void setMtThr(int mtThr) {
        this.mtThr = mtThr;
    }

    public int getIvSwitch() {
        return ivSwitch;
    }

    public void setIvSwitch(int ivSwitch) {
        this.ivSwitch = ivSwitch;
    }

    public int getIvThr() {
        return ivThr;
    }

    public void setIvThr(int ivThr) {
        this.ivThr = ivThr;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "region='" + region + '\'' +
                ", model='" + model + '\'' +
                ", hwVersion='" + hwVersion + '\'' +
                ", swVersion='" + swVersion + '\'' +
                ", btlVersion='" + btlVersion + '\'' +
                ", pedTar=" + pedTar +
                ", sn='" + sn + '\'' +
                ", curTime='" + curTime + '\'' +
                ", batteryState=" + batteryState +
                ", batteryValue='" + batteryValue + '\'' +
                ", oxiThr=" + oxiThr +
                ", motor=" + motor +
                ", mode=" + mode +
                ", fileList='" + fileList + '\'' +
                ", oxiSwitch=" + oxiSwitch +
                ", hrSwitch=" + hrSwitch +
                ", hrLowThr=" + hrLowThr +
                ", hrHighThr=" + hrHighThr +
                ", fileVer='" + fileVer + '\'' +
                ", spcpVer='" + spcpVer + '\'' +
                ", curState=" + curState +
                ", lightingMode=" + lightingMode +
                ", lightStr=" + lightStr +
                ", branchCode='" + branchCode + '\'' +
                ", spo2Switch=" + spo2Switch +
                ", buzzer=" + buzzer +
                ", mtSwitch=" + mtSwitch +
                ", mtThr=" + mtThr +
                ", ivSwitch=" + ivSwitch +
                ", ivThr=" + ivThr +
                '}';
    }
}

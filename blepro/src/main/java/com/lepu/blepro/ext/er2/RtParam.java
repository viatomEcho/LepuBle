package com.lepu.blepro.ext.er2;

public class RtParam {

    private int hr;
    private boolean rSignal;     // 有无检测到R波
    private boolean poorSignal;  // 信号弱标记(测量过程中信号弱)
    private int batteryState;    // 电池状态 0：正常使用，1：充电中，2：充满，3：低电量
    private int battery;         // 0-100%
    private int recordTime;      // 记录时长 s
    private int curStatus;       // 当前状态 0：空闲待机(导联脱落)，1：测量准备(主机丢弃前段波形阶段)，2：记录中，3：分析存储中，
    //         4：已存储成功(满时间测量结束后一直停留此状态直到回空闲状态)，5：记录小于30s(记录中状态直接切换至此状态)，
    //         6：重测已达6次，进入待机，7：导联断开

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public boolean isRSignal() {
        return rSignal;
    }

    public void setRSignal(boolean rSignal) {
        this.rSignal = rSignal;
    }

    public boolean isPoorSignal() {
        return poorSignal;
    }

    public void setPoorSignal(boolean poorSignal) {
        this.poorSignal = poorSignal;
    }

    public int getBatteryState() {
        return batteryState;
    }

    public void setBatteryState(int batteryState) {
        this.batteryState = batteryState;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(int recordTime) {
        this.recordTime = recordTime;
    }

    public int getCurStatus() {
        return curStatus;
    }

    public void setCurStatus(int curStatus) {
        this.curStatus = curStatus;
    }

    @Override
    public String toString() {
        return "RtParam{" +
                "hr=" + hr +
                ", rSignal=" + rSignal +
                ", poorSignal=" + poorSignal +
                ", batteryState=" + batteryState +
                ", battery=" + battery +
                ", recordTime=" + recordTime +
                ", curStatus=" + curStatus +
                '}';
    }
}

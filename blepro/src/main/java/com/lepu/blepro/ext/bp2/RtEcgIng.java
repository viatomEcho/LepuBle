package com.lepu.blepro.ext.bp2;

public class RtEcgIng {
    private int curDuration;     // 当前测量时长 s
    private boolean poolSignal;  // 是否信号弱
    private boolean leadOff;     // 是否导联脱落
    private int hr;
    public RtEcgIng(byte[] bytes) {
        com.lepu.blepro.ble.data.Bp2DataEcgIng data = new com.lepu.blepro.ble.data.Bp2DataEcgIng(bytes);
        curDuration = data.getCurDuration();
        poolSignal = data.isPoolSignal();
        leadOff = data.isLeadOff();
        hr = data.getHr();
    }

    public int getCurDuration() {
        return curDuration;
    }

    public void setCurDuration(int curDuration) {
        this.curDuration = curDuration;
    }

    public boolean isPoolSignal() {
        return poolSignal;
    }

    public void setPoolSignal(boolean poolSignal) {
        this.poolSignal = poolSignal;
    }

    public boolean isLeadOff() {
        return leadOff;
    }

    public void setLeadOff(boolean leadOff) {
        this.leadOff = leadOff;
    }

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    @Override
    public String toString() {
        return "RtEcgIng{" +
                "curDuration=" + curDuration +
                ", poolSignal=" + poolSignal +
                ", leadOff=" + leadOff +
                ", hr=" + hr +
                '}';
    }
}

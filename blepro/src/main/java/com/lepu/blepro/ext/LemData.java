package com.lepu.blepro.ext;

public class LemData {

    private int battery;             // 1-100%
    private boolean heatMode;        // 恒温加热模式开关
    private int massageMode;         // 按摩模式 0：活力，1：动感，2：捶击，3：舒缓，4：自动
    private int massageLevel;        // 按摩力度挡位 1-15
    private int massageTime;         // 按摩时间 0：15min，1：10min，2：5min

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public boolean isHeatMode() {
        return heatMode;
    }

    public void setHeatMode(boolean heatMode) {
        this.heatMode = heatMode;
    }

    public int getMassageMode() {
        return massageMode;
    }

    public void setMassageMode(int massageMode) {
        this.massageMode = massageMode;
    }

    public int getMassageLevel() {
        return massageLevel;
    }

    public void setMassageLevel(int massageLevel) {
        this.massageLevel = massageLevel;
    }

    public int getMassageTime() {
        return massageTime;
    }

    public void setMassageTime(int massageTime) {
        this.massageTime = massageTime;
    }

    @Override
    public String toString() {
        return "LemData{" +
                "battery=" + battery +
                ", heatMode=" + heatMode +
                ", massageMode=" + massageMode +
                ", massageLevel=" + massageLevel +
                ", massageTime=" + massageTime +
                '}';
    }
}

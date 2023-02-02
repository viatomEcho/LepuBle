package com.lepu.blepro.ext.lpbp2w;

public class BpRecord {
    private long startTime;       // 测量时间戳s
    private String fileName;      // 文件名
    private int uid;              // 用户id
    private int measureMode;      // 测量模式 0：单次 1：3次
    private int sys;              // 收缩压
    private int dia;              // 舒张压
    private int mean;             // 平均压
    private int pr;               // 脉率
    private boolean isIrregular;  // 心律不齐
    private boolean isMovement;   // 动作干扰

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getMeasureMode() {
        return measureMode;
    }

    public void setMeasureMode(int measureMode) {
        this.measureMode = measureMode;
    }

    public int getSys() {
        return sys;
    }

    public void setSys(int sys) {
        this.sys = sys;
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public int getMean() {
        return mean;
    }

    public void setMean(int mean) {
        this.mean = mean;
    }

    public int getPr() {
        return pr;
    }

    public void setPr(int pr) {
        this.pr = pr;
    }

    public boolean isIrregular() {
        return isIrregular;
    }

    public void setIrregular(boolean irregular) {
        isIrregular = irregular;
    }

    public boolean isMovement() {
        return isMovement;
    }

    public void setMovement(boolean movement) {
        isMovement = movement;
    }

    @Override
    public String toString() {
        return "BpRecord{" +
                "startTime=" + startTime +
                ", fileName='" + fileName + '\'' +
                ", uid=" + uid +
                ", measureMode=" + measureMode +
                ", sys=" + sys +
                ", dia=" + dia +
                ", mean=" + mean +
                ", pr=" + pr +
                ", isIrregular=" + isIrregular +
                ", isMovement=" + isMovement +
                '}';
    }
}

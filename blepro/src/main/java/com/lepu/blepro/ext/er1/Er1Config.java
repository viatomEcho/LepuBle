package com.lepu.blepro.ext.er1;

public class Er1Config {

    private boolean vibration;  // 心率震动开关
    private int threshold1;     // 阈值1 每十秒震动一次
    private int threshold2;     // 阈值2 每二秒震动一次

    public boolean isVibration() {
        return vibration;
    }

    public void setVibration(boolean vibration) {
        this.vibration = vibration;
    }

    public int getThreshold1() {
        return threshold1;
    }

    public void setThreshold1(int threshold1) {
        this.threshold1 = threshold1;
    }

    public int getThreshold2() {
        return threshold2;
    }

    public void setThreshold2(int threshold2) {
        this.threshold2 = threshold2;
    }

    @Override
    public String toString() {
        return "Er1Config{" +
                "vibration=" + vibration +
                ", threshold1=" + threshold1 +
                ", threshold2=" + threshold2 +
                '}';
    }
}

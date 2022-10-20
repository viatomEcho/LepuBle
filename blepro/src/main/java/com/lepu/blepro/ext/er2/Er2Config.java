package com.lepu.blepro.ext.er2;

public class Er2Config {

    private boolean soundOn;    // 心跳音开关 bit0
    private int vector;         // 加速度阈值
    private int motionCount;    // 加速度检测次数
    private int motionWindows;  // 加速度检测窗口

    public boolean isSoundOn() {
        return soundOn;
    }

    public void setSoundOn(boolean soundOn) {
        this.soundOn = soundOn;
    }

    public int getVector() {
        return vector;
    }

    public void setVector(int vector) {
        this.vector = vector;
    }

    public int getMotionCount() {
        return motionCount;
    }

    public void setMotionCount(int motionCount) {
        this.motionCount = motionCount;
    }

    public int getMotionWindows() {
        return motionWindows;
    }

    public void setMotionWindows(int motionWindows) {
        this.motionWindows = motionWindows;
    }

    @Override
    public String toString() {
        return "Er2Config{" +
                "soundOn=" + soundOn +
                ", vector=" + vector +
                ", motionCount=" + motionCount +
                ", motionWindows=" + motionWindows +
                '}';
    }
}

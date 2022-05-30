package com.lepu.blepro.ext.vtm20f;

import java.util.Arrays;

public class RtWave {

    private int seqNo;            // 包序号（0-255）
    private int wave;             // 脉搏波（0-100）
    private boolean pulseSound;   // 脉搏波音标记（true：表征有脉搏音 false：表征无脉搏音）
    private boolean sensorOff;    // 导连脱落标志（true：表征导连脱落 false：表征导连链接正常）
    private boolean disturb;      // 状态标记（true：表示运动干扰 false：表示状态正常）
    private boolean lowPi;        // 灌注标记（true：表示低灌注 false：表示正常灌注）
    private int barChart;         // 棒图（0-15）

    public int getWave() {
        return wave;
    }

    public void setWave(int wave) {
        this.wave = wave;
    }

    public boolean isPulseSound() {
        return pulseSound;
    }

    public void setPulseSound(boolean pulseSound) {
        this.pulseSound = pulseSound;
    }

    public boolean isSensorOff() {
        return sensorOff;
    }

    public void setSensorOff(boolean sensorOff) {
        this.sensorOff = sensorOff;
    }

    public boolean isDisturb() {
        return disturb;
    }

    public void setDisturb(boolean disturb) {
        this.disturb = disturb;
    }

    public boolean isLowPi() {
        return lowPi;
    }

    public void setLowPi(boolean lowPi) {
        this.lowPi = lowPi;
    }

    public int getBarChart() {
        return barChart;
    }

    public void setBarChart(int barChart) {
        this.barChart = barChart;
    }

    public int getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    @Override
    public String toString() {
        return "RtWave{" +
                "seqNo=" + seqNo +
                ", wave=" + wave +
                ", pulseSound=" + pulseSound +
                ", sensorOff=" + sensorOff +
                ", disturb=" + disturb +
                ", lowPi=" + lowPi +
                ", barChart=" + barChart +
                '}';
    }
}

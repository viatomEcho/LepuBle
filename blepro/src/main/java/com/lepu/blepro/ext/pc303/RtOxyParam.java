package com.lepu.blepro.ext.pc303;

public class RtOxyParam {
    private int spo2;                  // （0-100）
    private int pr;                    // （0-511）
    private float pi;                  // （0-255）
    private boolean isProbeOff;        // 探头检测中
    private boolean isPulseSearching;  // 脉搏扫描中

    public int getSpo2() {
        return spo2;
    }

    public void setSpo2(int spo2) {
        this.spo2 = spo2;
    }

    public int getPr() {
        return pr;
    }

    public void setPr(int pr) {
        this.pr = pr;
    }

    public float getPi() {
        return pi;
    }

    public void setPi(float pi) {
        this.pi = pi;
    }

    public boolean isProbeOff() {
        return isProbeOff;
    }

    public void setProbeOff(boolean probeOff) {
        isProbeOff = probeOff;
    }

    public boolean isPulseSearching() {
        return isPulseSearching;
    }

    public void setPulseSearching(boolean pulseSearching) {
        isPulseSearching = pulseSearching;
    }

    @Override
    public String toString() {
        return "RtOxyParam{" +
                "spo2=" + spo2 +
                ", pr=" + pr +
                ", pi=" + pi +
                ", isProbeOff=" + isProbeOff +
                ", isPulseSearching=" + isPulseSearching +
                '}';
    }
}

package com.lepu.blepro.ext.pc102;

public class RtOxyParam {
    private int spo2;             // （0-100）
    private int pr;               // （0-511）
    private float pi;             // （0-255）
    private boolean isDetecting;  // 探头检测中
    private boolean isScanning;   // 脉搏扫描中

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

    public boolean isDetecting() {
        return isDetecting;
    }

    public void setDetecting(boolean detecting) {
        isDetecting = detecting;
    }

    public boolean isScanning() {
        return isScanning;
    }

    public void setScanning(boolean scanning) {
        isScanning = scanning;
    }

    @Override
    public String toString() {
        return "RtOxyParam{" +
                "spo2=" + spo2 +
                ", pr=" + pr +
                ", pi=" + pi +
                ", isDetecting=" + isDetecting +
                ", isScanning=" + isScanning +
                '}';
    }
}

package com.lepu.blepro.ext.sp20;

public class RtParam {
    private int spo2;
    private int pr;
    private float pi;
    private boolean isProbeOff;
    private boolean isPulseSearching;
    private boolean isCheckProbe;
    private int battery;

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

    public boolean isCheckProbe() {
        return isCheckProbe;
    }

    public void setCheckProbe(boolean checkProbe) {
        isCheckProbe = checkProbe;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    @Override
    public String toString() {
        return "RtParam{" +
                "spo2=" + spo2 +
                ", pr=" + pr +
                ", pi=" + pi +
                ", isProbeOff=" + isProbeOff +
                ", isPulseSearching=" + isPulseSearching +
                ", isCheckProbe=" + isCheckProbe +
                ", battery=" + battery +
                '}';
    }
}

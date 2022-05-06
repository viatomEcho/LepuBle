package com.lepu.blepro.ext.checkmele;

public class LeEcgDiagnosis {
    // Regular ECG Rhythm
    private boolean isRegular = false;
    // Unable to analyze
    private boolean isPoorSignal = false;
    // High Heart Rate
    private boolean isHighHr = false;
    // Low Heart Rate
    private boolean isLowHr = false;
    // Irregular ECG Rhythm
    private boolean isIrregular = false;
    // High QRS Value
    private boolean isHighQrs = false;
    // High ST Value
    private boolean isHighSt = false;
    // Low ST Value
    private boolean isLowSt = false;
    // Suspected Premature Beat
    private boolean isPrematureBeat = false;

    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isRegular() {
        return isRegular;
    }

    public void setRegular(boolean regular) {
        isRegular = regular;
    }

    public boolean isPoorSignal() {
        return isPoorSignal;
    }

    public void setPoorSignal(boolean poorSignal) {
        isPoorSignal = poorSignal;
    }

    public boolean isHighHr() {
        return isHighHr;
    }

    public void setHighHr(boolean highHr) {
        isHighHr = highHr;
    }

    public boolean isLowHr() {
        return isLowHr;
    }

    public void setLowHr(boolean lowHr) {
        isLowHr = lowHr;
    }

    public boolean isIrregular() {
        return isIrregular;
    }

    public void setIrregular(boolean irregular) {
        isIrregular = irregular;
    }

    public boolean isHighQrs() {
        return isHighQrs;
    }

    public void setHighQrs(boolean highQrs) {
        isHighQrs = highQrs;
    }

    public boolean isHighSt() {
        return isHighSt;
    }

    public void setHighSt(boolean highSt) {
        isHighSt = highSt;
    }

    public boolean isLowSt() {
        return isLowSt;
    }

    public void setLowSt(boolean lowSt) {
        isLowSt = lowSt;
    }

    public boolean isPrematureBeat() {
        return isPrematureBeat;
    }

    public void setPrematureBeat(boolean prematureBeat) {
        isPrematureBeat = prematureBeat;
    }

    @Override
    public String toString() {
        return "LeEcgDiagnosis{" +
                "isRegular=" + isRegular +
                ", isPoorSignal=" + isPoorSignal +
                ", isHighHr=" + isHighHr +
                ", isLowHr=" + isLowHr +
                ", isIrregular=" + isIrregular +
                ", isHighQrs=" + isHighQrs +
                ", isHighSt=" + isHighSt +
                ", isLowSt=" + isLowSt +
                ", isPrematureBeat=" + isPrematureBeat +
                ", result=" + result +
                '}';
    }
}

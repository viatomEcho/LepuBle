package com.lepu.blepro.ext.pulsebit;

public class ExEcgDiagnosis {
    // Regular ECG Rhythm (心电未见明显异常，遵循医生意见)
    private boolean isRegular = false;
    // Unable to analyze (心电信号幅度低或噪声干扰)
    private boolean isPoorSignal = false;
    // Fast Heart Rate (心率过快)
    private boolean isFastHr = false;
    // Slow Heart Rate (心率过缓)
    private boolean isSlowHr = false;
    // Irregular ECG Rhythm (疑似窦性心律失常)
    private boolean isIrregular = false;
    // Possible ventricular premature beats (疑似室性早博)
    private boolean isPvcs = false;
    // Possible heart pause (疑似心跳暂停)
    private boolean isHeartPause = false;
    // Possible Atrial fibrillation (疑似房颤)
    private boolean isFibrillation = false;
    // Wide QRS duration (QRS持续时间)
    private boolean isWideQrs = false;
    // QTc is prolonged (QTc延长)
    private boolean isProlongedQtc = false;
    // QTc is short (QTc短暂)
    private boolean isShortQtc = false;
    // ST segment elevation (ST段抬高)
    private boolean isStElevation = false;
    // ST segment depression (ST压低)
    private boolean isStDepression = false;

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

    public boolean isStDepression() {
        return isStDepression;
    }

    public void setStDepression(boolean stDepression) {
        isStDepression = stDepression;
    }

    public boolean isStElevation() {
        return isStElevation;
    }

    public void setStElevation(boolean stElevation) {
        isStElevation = stElevation;
    }

    public boolean isShortQtc() {
        return isShortQtc;
    }

    public void setShortQtc(boolean shortQtc) {
        isShortQtc = shortQtc;
    }

    public boolean isProlongedQtc() {
        return isProlongedQtc;
    }

    public void setProlongedQtc(boolean prolongedQtc) {
        isProlongedQtc = prolongedQtc;
    }

    public boolean isWideQrs() {
        return isWideQrs;
    }

    public void setWideQrs(boolean wideQrs) {
        isWideQrs = wideQrs;
    }

    public boolean isFibrillation() {
        return isFibrillation;
    }

    public void setFibrillation(boolean fibrillation) {
        isFibrillation = fibrillation;
    }

    public boolean isHeartPause() {
        return isHeartPause;
    }

    public void setHeartPause(boolean heartPause) {
        isHeartPause = heartPause;
    }

    public boolean isPvcs() {
        return isPvcs;
    }

    public void setPvcs(boolean pvcs) {
        isPvcs = pvcs;
    }

    public boolean isIrregular() {
        return isIrregular;
    }

    public void setIrregular(boolean irregular) {
        isIrregular = irregular;
    }

    public boolean isSlowHr() {
        return isSlowHr;
    }

    public void setSlowHr(boolean slowHr) {
        isSlowHr = slowHr;
    }

    public boolean isFastHr() {
        return isFastHr;
    }

    public void setFastHr(boolean fastHr) {
        isFastHr = fastHr;
    }

    public boolean isPoorSignal() {
        return isPoorSignal;
    }

    public void setPoorSignal(boolean poorSignal) {
        isPoorSignal = poorSignal;
    }

    @Override
    public String toString() {
        return "ExEcgDiagnosis{" +
                "isRegular = " + isRegular +
                ", isPoorSignal = " + isPoorSignal +
                ", isFastHr = " + isFastHr +
                ", isSlowHr = " + isSlowHr +
                ", isIrregular = " + isIrregular +
                ", isPvcs = " + isPvcs +
                ", isHeartPause = " + isHeartPause +
                ", isFibrillation = " + isFibrillation +
                ", isWideQrs = " + isWideQrs +
                ", isProlongedQtc = " + isProlongedQtc +
                ", isShortQtc = " + isShortQtc +
                ", isStElevation = " + isStElevation +
                ", isStDepression = " + isStDepression +
                ", result = " + result +
                '}';
    }
}

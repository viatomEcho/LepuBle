package com.lepu.demo.data;

public class OxyData {

    private String fileName;
    private int recordingTime;
    private int avgSpo2;
    private int avgHr;
    private int minSpo2;
    private int dropsTimes3Percent;
    private int dropsTimes4Percent;
    private int asleepTimePercent;
    private int durationTime90Percent;
    private int dropsTimes90Percent;
    private int asleepTime;
    private int o2Score;
    private long startTime;
    private int[] spo2s;
    private int[] hrs;
    private int[] motions;
    private boolean[] warningSpo2s;
    private boolean[] warningHrs;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getRecordingTime() {
        return recordingTime;
    }

    public void setRecordingTime(int recordingTime) {
        this.recordingTime = recordingTime;
    }

    public int getAvgSpo2() {
        return avgSpo2;
    }

    public void setAvgSpo2(int avgSpo2) {
        this.avgSpo2 = avgSpo2;
    }

    public int getAvgHr() {
        return avgHr;
    }

    public void setAvgHr(int avgHr) {
        this.avgHr = avgHr;
    }

    public int getMinSpo2() {
        return minSpo2;
    }

    public void setMinSpo2(int minSpo2) {
        this.minSpo2 = minSpo2;
    }

    public int getDropsTimes3Percent() {
        return dropsTimes3Percent;
    }

    public void setDropsTimes3Percent(int dropsTimes3Percent) {
        this.dropsTimes3Percent = dropsTimes3Percent;
    }

    public int getDropsTimes4Percent() {
        return dropsTimes4Percent;
    }

    public void setDropsTimes4Percent(int dropsTimes4Percent) {
        this.dropsTimes4Percent = dropsTimes4Percent;
    }

    public int getAsleepTimePercent() {
        return asleepTimePercent;
    }

    public void setAsleepTimePercent(int asleepTimePercent) {
        this.asleepTimePercent = asleepTimePercent;
    }

    public int getDurationTime90Percent() {
        return durationTime90Percent;
    }

    public void setDurationTime90Percent(int durationTime90Percent) {
        this.durationTime90Percent = durationTime90Percent;
    }

    public int getDropsTimes90Percent() {
        return dropsTimes90Percent;
    }

    public void setDropsTimes90Percent(int dropsTimes90Percent) {
        this.dropsTimes90Percent = dropsTimes90Percent;
    }

    public int getAsleepTime() {
        return asleepTime;
    }

    public void setAsleepTime(int asleepTime) {
        this.asleepTime = asleepTime;
    }

    public int getO2Score() {
        return o2Score;
    }

    public void setO2Score(int o2Score) {
        this.o2Score = o2Score;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int[] getSpo2s() {
        return spo2s;
    }

    public void setSpo2s(int[] spo2s) {
        this.spo2s = spo2s;
    }

    public int[] getHrs() {
        return hrs;
    }

    public void setHrs(int[] hrs) {
        this.hrs = hrs;
    }

    public int[] getMotions() {
        return motions;
    }

    public void setMotions(int[] motions) {
        this.motions = motions;
    }

    public boolean[] getWarningSpo2s() {
        return warningSpo2s;
    }

    public void setWarningSpo2s(boolean[] warningSpo2s) {
        this.warningSpo2s = warningSpo2s;
    }

    public boolean[] getWarningHrs() {
        return warningHrs;
    }

    public void setWarningHrs(boolean[] warningHrs) {
        this.warningHrs = warningHrs;
    }
}

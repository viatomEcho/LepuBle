package com.lepu.blepro.ext.oxy;

import java.util.ArrayList;

public class OxyFile {

    private int version;
    private int operationMode;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private int startTime;
    private int size;
    private int recordingTime;
    private int asleepTime;
    private int avgSpo2;
    private int minSpo2;
    private int dropsTimes3Percent;
    private int dropsTimes4Percent;
    private int asleepTimePercent;
    private int durationTime90Percent;
    private int dropsTimes90Percent;
    private int o2Score;
    private int stepCounter;
    private ArrayList<EachData> data = new ArrayList<>();

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getOperationMode() {
        return operationMode;
    }

    public void setOperationMode(int operationMode) {
        this.operationMode = operationMode;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getRecordingTime() {
        return recordingTime;
    }

    public void setRecordingTime(int recordingTime) {
        this.recordingTime = recordingTime;
    }

    public int getAsleepTime() {
        return asleepTime;
    }

    public void setAsleepTime(int asleepTime) {
        this.asleepTime = asleepTime;
    }

    public int getAvgSpo2() {
        return avgSpo2;
    }

    public void setAvgSpo2(int avgSpo2) {
        this.avgSpo2 = avgSpo2;
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

    public int getO2Score() {
        return o2Score;
    }

    public void setO2Score(int o2Score) {
        this.o2Score = o2Score;
    }

    public int getStepCounter() {
        return stepCounter;
    }

    public void setStepCounter(int stepCounter) {
        this.stepCounter = stepCounter;
    }

    public ArrayList<EachData> getData() {
        return data;
    }

    public void setData(ArrayList<EachData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "OxyFile{" +
                "version=" + version +
                ", operationMode=" + operationMode +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                ", second=" + second +
                ", startTime=" + startTime +
                ", size=" + size +
                ", recordingTime=" + recordingTime +
                ", asleepTime=" + asleepTime +
                ", avgSpo2=" + avgSpo2 +
                ", minSpo2=" + minSpo2 +
                ", dropsTimes3Percent=" + dropsTimes3Percent +
                ", dropsTimes4Percent=" + dropsTimes4Percent +
                ", asleepTimePercent=" + asleepTimePercent +
                ", durationTime90Percent=" + durationTime90Percent +
                ", dropsTimes90Percent=" + dropsTimes90Percent +
                ", o2Score=" + o2Score +
                ", stepCounter=" + stepCounter +
                ", data=" + data +
                '}';
    }

    public class EachData {
        private int spo2;
        private int pr;
        private int vector;

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

        public int getVector() {
            return vector;
        }

        public void setVector(int vector) {
            this.vector = vector;
        }

        @Override
        public String toString() {
            return "EachData{" +
                    "spo2=" + spo2 +
                    ", pr=" + pr +
                    ", vector=" + vector +
                    '}';
        }
    }

}

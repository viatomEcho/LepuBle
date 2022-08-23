package com.lepu.demo.data;

import java.util.Arrays;

public class EcgData {

    private long recordingTime;  // 时间戳s
    private int duration;
    private String fileName;
    private byte[] data;
    private short[] shortData;

    public long getRecordingTime() {
        return recordingTime;
    }

    public void setRecordingTime(long recordingTime) {
        this.recordingTime = recordingTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public short[] getShortData() {
        return shortData;
    }

    public void setShortData(short[] shortData) {
        this.shortData = shortData;
    }

    @Override
    public String toString() {
        return "EcgData{" +
                "recordingTime=" + recordingTime +
                ", duration=" + duration +
                ", fileName='" + fileName + '\'' +
                ", data=" + Arrays.toString(data) +
                ", shortData=" + Arrays.toString(shortData) +
                '}';
    }
}

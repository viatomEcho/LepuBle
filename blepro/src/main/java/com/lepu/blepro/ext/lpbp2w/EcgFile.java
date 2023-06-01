package com.lepu.blepro.ext.lpbp2w;

import com.lepu.blepro.ble.data.LeBp2wEcgFile;

public class EcgFile {
    private byte[] bytes;
    private String fileName;
    private int fileVersion;
    private int fileType;
    private long startTime;
    private byte[] waveData;
    private short[] waveShortData;
    private float[] waveFloatData;
    private int duration;

    public EcgFile() {

    }
    public EcgFile(byte[] bytes, String fileName) {
        this.bytes = bytes;
        this.fileName = fileName;
        LeBp2wEcgFile data = new LeBp2wEcgFile(fileName, bytes, "");
        fileVersion = data.getFileVersion();
        fileType = data.getFileType();
        startTime = data.getTimestamp();
        waveData = data.getWaveData();
        waveShortData = data.getWaveShortData();
        waveFloatData = data.getWaveFloatData();
        duration = data.getDuration();
    }
    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileVersion() {
        return fileVersion;
    }

    public void setFileVersion(int fileVersion) {
        this.fileVersion = fileVersion;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public byte[] getWaveData() {
        return waveData;
    }

    public void setWaveData(byte[] waveData) {
        this.waveData = waveData;
    }

    public short[] getWaveShortData() {
        return waveShortData;
    }

    public void setWaveShortData(short[] waveShortData) {
        this.waveShortData = waveShortData;
    }

    public float[] getWaveFloatData() {
        return waveFloatData;
    }

    public void setWaveFloatData(float[] waveFloatData) {
        this.waveFloatData = waveFloatData;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "EcgFile{" +
                "fileName='" + fileName + '\'' +
                ", fileVersion=" + fileVersion +
                ", fileType=" + fileType +
                ", startTime=" + startTime +
                ", duration=" + duration +
                '}';
    }
}

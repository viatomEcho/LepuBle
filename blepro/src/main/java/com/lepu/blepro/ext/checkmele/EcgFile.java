package com.lepu.blepro.ext.checkmele;

import java.util.Arrays;

public class EcgFile {

    private String fileName;
    private int hrsDataSize;          // 波形心率大小（byte）
    private int recordingTime;         // 记录时长 s
    private int waveDataSize;          // 波形数据大小（byte）
    private int hr;                  // 诊断结果：HR，单位为bpm
    private float st;                  // 诊断结果：ST（以ST/100存储），单位为mV(内部导联写0)
    private int qrs;                   // 诊断结果：QRS，单位为ms
    private int pvcs;                  // 诊断结果：PVCs(内部导联写0)
    private int qtc;                   // 诊断结果：QTc单位为ms
    private LeEcgDiagnosis result;      // 心电异常诊断结果
    private int measureMode;           // 测量模式
    private String measureModeMess;
    private int filterMode;            // 滤波模式（1：wide   0：normal）
    private int qt;                    // 诊断结果：QT单位为ms
    private byte[] hrsData;         // ECG心率值，从数据采样开始，采样率为1Hz，每个心率值为2byte（实际20s数据，每秒出一个心率），若出现无效心率，则心率为0
    private int[] hrsIntData;       // ECG心率值
    private byte[] waveData;        // 每个采样点2byte，原始数据
    private short[] waveShortData;  // 每个采样点2byte
    private float[] wFs;            // 转毫伏值(n*4033)/(32767*12*8)

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getHrsDataSize() {
        return hrsDataSize;
    }

    public void setHrsDataSize(int hrsDataSize) {
        this.hrsDataSize = hrsDataSize;
    }

    public int getRecordingTime() {
        return recordingTime;
    }

    public void setRecordingTime(int recordingTime) {
        this.recordingTime = recordingTime;
    }

    public int getWaveDataSize() {
        return waveDataSize;
    }

    public void setWaveDataSize(int waveDataSize) {
        this.waveDataSize = waveDataSize;
    }

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public float getSt() {
        return st;
    }

    public void setSt(float st) {
        this.st = st;
    }

    public int getQrs() {
        return qrs;
    }

    public void setQrs(int qrs) {
        this.qrs = qrs;
    }

    public int getPvcs() {
        return pvcs;
    }

    public void setPvcs(int pvcs) {
        this.pvcs = pvcs;
    }

    public int getQtc() {
        return qtc;
    }

    public void setQtc(int qtc) {
        this.qtc = qtc;
    }

    public LeEcgDiagnosis getResult() {
        return result;
    }

    public void setResult(LeEcgDiagnosis result) {
        this.result = result;
    }

    public int getMeasureMode() {
        return measureMode;
    }

    public void setMeasureMode(int measureMode) {
        this.measureMode = measureMode;
    }

    public String getMeasureModeMess() {
        return measureModeMess;
    }

    public void setMeasureModeMess(String measureModeMess) {
        this.measureModeMess = measureModeMess;
    }

    public int getFilterMode() {
        return filterMode;
    }

    public void setFilterMode(int filterMode) {
        this.filterMode = filterMode;
    }

    public int getQt() {
        return qt;
    }

    public void setQt(int qt) {
        this.qt = qt;
    }

    public byte[] getHrsData() {
        return hrsData;
    }

    public void setHrsData(byte[] hrsData) {
        this.hrsData = hrsData;
    }

    public int[] getHrsIntData() {
        return hrsIntData;
    }

    public void setHrsIntData(int[] hrsIntData) {
        this.hrsIntData = hrsIntData;
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

    public float[] getWFs() {
        return wFs;
    }

    public void setWFs(float[] wFs) {
        this.wFs = wFs;
    }

    @Override
    public String toString() {
        return "EcgFile{" +
                "fileName='" + fileName + '\'' +
                ", hrsDataSize=" + hrsDataSize +
                ", recordingTime=" + recordingTime +
                ", waveDataSize=" + waveDataSize +
                ", hr=" + hr +
                ", st=" + st +
                ", qrs=" + qrs +
                ", pvcs=" + pvcs +
                ", qtc=" + qtc +
                ", result=" + result +
                ", measureMode=" + measureMode +
                ", measureModeMess='" + measureModeMess + '\'' +
                ", filterMode=" + filterMode +
                ", qt=" + qt +
                ", hrsData=" + Arrays.toString(hrsData) +
                ", hrsIntData=" + Arrays.toString(hrsIntData) +
                ", waveData=" + Arrays.toString(waveData) +
                ", waveShortData=" + Arrays.toString(waveShortData) +
                ", wFs=" + Arrays.toString(wFs) +
                '}';
    }
}

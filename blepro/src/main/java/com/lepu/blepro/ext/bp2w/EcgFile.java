package com.lepu.blepro.ext.bp2w;

public class EcgFile {
    private int fileVersion;            // 文件版本 e.g.  0x01 :  V1
    private int fileType;               // 文件类型 1：血压；2：心电
    private int measureTime;            // 测量时间时间戳 s
    private boolean upload;             // 上传标识
    private int recordingTime;          // 记录时长 s
    private int result;
    private EcgDiagnosis diagnosis;  // 诊断结果
    private int hr;                     // 心率 单位：bpm
    private int qrs;                    // QRS 单位：ms
    private int pvcs;                   // PVC个数
    private int qtc;                    // QTc 单位：ms
    private boolean connectCable;       // 是否接入线缆
    private byte[] waveData;
    private short[] waveShortData;

    public EcgFile(byte[] bytes) {
        com.lepu.blepro.ble.data.Bp2EcgFile data = new com.lepu.blepro.ble.data.Bp2EcgFile(bytes);
        fileVersion = data.getFileVersion();
        fileType = data.getFileType();
        measureTime = data.getMeasureTime();
        upload = data.getUploadTag();
        recordingTime = data.getRecordingTime();
        result = data.getResult();
        diagnosis = new EcgDiagnosis(data.getDiagnosis().getBytes());
        hr = data.getHr();
        qrs = data.getQrs();
        pvcs = data.getPvcs();
        qtc = data.getQtc();
        connectCable = data.getConnectCable();
        waveData = data.getWaveData();
        waveShortData = data.getWaveShortData();
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

    public int getMeasureTime() {
        return measureTime;
    }

    public void setMeasureTime(int measureTime) {
        this.measureTime = measureTime;
    }

    public boolean isUpload() {
        return upload;
    }

    public void setUpload(boolean upload) {
        this.upload = upload;
    }

    public int getRecordingTime() {
        return recordingTime;
    }

    public void setRecordingTime(int recordingTime) {
        this.recordingTime = recordingTime;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public EcgDiagnosis getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(EcgDiagnosis diagnosis) {
        this.diagnosis = diagnosis;
    }

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
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

    public boolean isConnectCable() {
        return connectCable;
    }

    public void setConnectCable(boolean connectCable) {
        this.connectCable = connectCable;
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

    @Override
    public String toString() {
        return "EcgFile{" +
                "fileVersion=" + fileVersion +
                ", fileType=" + fileType +
                ", measureTime=" + measureTime +
                ", upload=" + upload +
                ", recordingTime=" + recordingTime +
                ", result=" + result +
                ", diagnosis=" + diagnosis +
                ", hr=" + hr +
                ", qrs=" + qrs +
                ", pvcs=" + pvcs +
                ", qtc=" + qtc +
                ", connectCable=" + connectCable +
                '}';
    }
}

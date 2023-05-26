package com.lepu.blepro.ext.er1;

public class Er1EcgFile {

    private int fileVersion;     // 文件版本 e.g.  0x01 :  V1
    private byte[] waveData;     // 125Hz原始波形压缩数据（差分压缩），0x7FFF(32767)为无效值
    private int recordingTime;   // 记录时长 e.g. 3600 :  3600s
    private int dataCrc;         // 文件头部+原始波形和校验
    private long magic;           // 文件标志 固定值为0xA55A0438

    public Er1EcgFile(byte[] bytes) {
        com.lepu.blepro.ble.data.Er1EcgFile data = new com.lepu.blepro.ble.data.Er1EcgFile(bytes);
        fileVersion = data.getFileVersion();
        waveData = data.getWaveData();
        recordingTime = data.getRecordingTime();
        dataCrc = data.getDataCrc();
        magic = data.getMagic();
    }

    public int getFileVersion() {
        return fileVersion;
    }

    public void setFileVersion(int fileVersion) {
        this.fileVersion = fileVersion;
    }

    public byte[] getWaveData() {
        return waveData;
    }

    public void setWaveData(byte[] waveData) {
        this.waveData = waveData;
    }

    public int getRecordingTime() {
        return recordingTime;
    }

    public void setRecordingTime(int recordingTime) {
        this.recordingTime = recordingTime;
    }

    public int getDataCrc() {
        return dataCrc;
    }

    public void setDataCrc(int dataCrc) {
        this.dataCrc = dataCrc;
    }

    public long getMagic() {
        return magic;
    }

    public void setMagic(long magic) {
        this.magic = magic;
    }
}

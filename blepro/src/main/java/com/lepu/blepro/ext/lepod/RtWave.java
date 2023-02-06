package com.lepu.blepro.ext.lepod;

public class RtWave {
    private int samplingRate;    // bit0~bit3:采样率0::250Hz 1:125Hz 2:62.5Hz
    private int compressType;    // bit4~bit7: 压缩类型 0:未压缩 1:Viatom差分压缩
    private int firstIndex;      // 数据的第一个点
    private int len;             // 采样点数
    private byte[] waveBytes;    // 压缩原始数据
    private float[] waveFloats;  // 解压后毫伏值数据，n * 0.00244140625

    public int getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    public int getCompressType() {
        return compressType;
    }

    public void setCompressType(int compressType) {
        this.compressType = compressType;
    }

    public int getFirstIndex() {
        return firstIndex;
    }

    public void setFirstIndex(int firstIndex) {
        this.firstIndex = firstIndex;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public byte[] getWaveBytes() {
        return waveBytes;
    }

    public void setWaveBytes(byte[] waveBytes) {
        this.waveBytes = waveBytes;
    }

    public float[] getWaveFloats() {
        return waveFloats;
    }

    public void setWaveFloats(float[] waveFloats) {
        this.waveFloats = waveFloats;
    }
}

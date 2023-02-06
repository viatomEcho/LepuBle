package com.lepu.blepro.ext.er3;

public class RtWave {
    private int firstIndex;     // 数据的第一个点
    private int len;            // 采样点数
    private byte[] waveBytes;   // 压缩原始数据
    private float[] waveFloats;  // 解压后毫伏值数据，n * 0.00244140625

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

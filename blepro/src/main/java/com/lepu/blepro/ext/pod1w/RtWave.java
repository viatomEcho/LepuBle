package com.lepu.blepro.ext.pod1w;

import java.util.Arrays;

public class RtWave {
    private byte[] waveData;
    private int[] waveIntData;

    public byte[] getWaveData() {
        return waveData;
    }

    public void setWaveData(byte[] waveData) {
        this.waveData = waveData;
    }

    public int[] getWaveIntData() {
        return waveIntData;
    }

    public void setWaveIntData(int[] waveIntData) {
        this.waveIntData = waveIntData;
    }

    @Override
    public String toString() {
        return "RtWave{" +
                "waveData=" + Arrays.toString(waveData) +
                ", waveIntData=" + Arrays.toString(waveIntData) +
                '}';
    }
}

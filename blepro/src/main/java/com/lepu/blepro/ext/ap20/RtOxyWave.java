package com.lepu.blepro.ext.ap20;

import java.util.Arrays;

public class RtOxyWave {
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
        return "RtOxyWave{" +
                "waveData=" + Arrays.toString(waveData) +
                ", waveIntData=" + Arrays.toString(waveIntData) +
                '}';
    }
}

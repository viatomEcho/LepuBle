package com.lepu.blepro.ext.ecn;

import java.util.Arrays;

public class RtData {
    private RtState state;
    private byte[] wave;

    public RtState getState() {
        return state;
    }

    public void setState(RtState state) {
        this.state = state;
    }

    public byte[] getWave() {
        return wave;
    }

    public void setWave(byte[] wave) {
        this.wave = wave;
    }

    @Override
    public String toString() {
        return "RtData{" +
                "state=" + state +
                ", wave=" + Arrays.toString(wave) +
                '}';
    }
}

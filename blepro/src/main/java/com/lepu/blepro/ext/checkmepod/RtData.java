package com.lepu.blepro.ext.checkmepod;

public class RtData {

    private RtParam param;
    private RtWave wave;

    public RtParam getParam() {
        return param;
    }

    public void setParam(RtParam param) {
        this.param = param;
    }

    public RtWave getWave() {
        return wave;
    }

    public void setWave(RtWave wave) {
        this.wave = wave;
    }

    @Override
    public String toString() {
        return "RtData{" +
                "param=" + param +
                ", wave=" + wave +
                '}';
    }
}

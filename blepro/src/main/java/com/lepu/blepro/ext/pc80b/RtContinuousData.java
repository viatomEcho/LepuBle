package com.lepu.blepro.ext.pc80b;

public class RtContinuousData {
    private int seqNo;
    private RtEcgData ecgData;
    private int hr;
    private boolean leadOff;
    private float gain;
    private float vol;

    public int getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public RtEcgData getEcgData() {
        return ecgData;
    }

    public void setEcgData(RtEcgData ecgData) {
        this.ecgData = ecgData;
    }

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public boolean isLeadOff() {
        return leadOff;
    }

    public void setLeadOff(boolean leadOff) {
        this.leadOff = leadOff;
    }

    public float getGain() {
        return gain;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public float getVol() {
        return vol;
    }

    public void setVol(float vol) {
        this.vol = vol;
    }

    @Override
    public String toString() {
        return "RtContinuousData{" +
                "seqNo=" + seqNo +
                ", ecgData=" + ecgData +
                ", hr=" + hr +
                ", leadOff=" + leadOff +
                ", gain=" + gain +
                ", vol=" + vol +
                '}';
    }
}

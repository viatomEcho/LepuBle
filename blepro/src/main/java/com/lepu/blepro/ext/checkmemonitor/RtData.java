package com.lepu.blepro.ext.checkmemonitor;

import java.util.Arrays;

public class RtData {

    private byte[] ecgData;
    private short[] ecgShortData;
    private float[] ecgFloatData;
    private int hr;
    private int qrs;
    private int st;
    private int pvcs;
    private boolean rWaveMark;
    private int ecgNote;
    private byte[] spo2Data;
    private int[] spo2IntData;
    private int pr;
    private int spo2;
    private float pi;
    private boolean pulseMark;
    private int spo2Note;
    private int battery;

    public byte[] getEcgData() {
        return ecgData;
    }

    public void setEcgData(byte[] ecgData) {
        this.ecgData = ecgData;
    }

    public short[] getEcgShortData() {
        return ecgShortData;
    }

    public void setEcgShortData(short[] ecgShortData) {
        this.ecgShortData = ecgShortData;
    }

    public float[] getEcgFloatData() {
        return ecgFloatData;
    }

    public void setEcgFloatData(float[] ecgFloatData) {
        this.ecgFloatData = ecgFloatData;
    }

    public byte[] getSpo2Data() {
        return spo2Data;
    }

    public void setSpo2Data(byte[] spo2Data) {
        this.spo2Data = spo2Data;
    }

    public int[] getSpo2IntData() {
        return spo2IntData;
    }

    public void setSpo2IntData(int[] spo2IntData) {
        this.spo2IntData = spo2IntData;
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

    public int getSt() {
        return st;
    }

    public void setSt(int st) {
        this.st = st;
    }

    public int getPvcs() {
        return pvcs;
    }

    public void setPvcs(int pvcs) {
        this.pvcs = pvcs;
    }

    public boolean isRWaveMark() {
        return rWaveMark;
    }

    public void setRWaveMark(boolean rWaveMark) {
        this.rWaveMark = rWaveMark;
    }

    public int getEcgNote() {
        return ecgNote;
    }

    public void setEcgNote(int ecgNote) {
        this.ecgNote = ecgNote;
    }

    public int getPr() {
        return pr;
    }

    public void setPr(int pr) {
        this.pr = pr;
    }

    public int getSpo2() {
        return spo2;
    }

    public void setSpo2(int spo2) {
        this.spo2 = spo2;
    }

    public float getPi() {
        return pi;
    }

    public void setPi(float pi) {
        this.pi = pi;
    }

    public boolean isPulseMark() {
        return pulseMark;
    }

    public void setPulseMark(boolean pulseMark) {
        this.pulseMark = pulseMark;
    }

    public int getSpo2Note() {
        return spo2Note;
    }

    public void setSpo2Note(int spo2Note) {
        this.spo2Note = spo2Note;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    @Override
    public String toString() {
        return "RtData{" +
                "ecgData=" + Arrays.toString(ecgData) +
                ", ecgShortData=" + Arrays.toString(ecgShortData) +
                ", ecgFloatData=" + Arrays.toString(ecgFloatData) +
                ", hr=" + hr +
                ", qrs=" + qrs +
                ", st=" + st +
                ", pvcs=" + pvcs +
                ", rWaveMark=" + rWaveMark +
                ", ecgNote=" + ecgNote +
                ", spo2Data=" + Arrays.toString(spo2Data) +
                ", spo2IntData=" + Arrays.toString(spo2IntData) +
                ", pr=" + pr +
                ", spo2=" + spo2 +
                ", pi=" + pi +
                ", pulseMark=" + pulseMark +
                ", spo2Note=" + spo2Note +
                ", battery=" + battery +
                '}';
    }
}

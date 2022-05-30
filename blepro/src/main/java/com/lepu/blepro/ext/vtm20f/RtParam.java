package com.lepu.blepro.ext.vtm20f;

public class RtParam {

    private int seqNo;
    private int pr;
    private int spo2;
    private float pi;

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

    public int getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    @Override
    public String toString() {
        return "RtParam{" +
                "seqNo=" + seqNo +
                ", pr=" + pr +
                ", spo2=" + spo2 +
                ", pi=" + pi +
                '}';
    }
}

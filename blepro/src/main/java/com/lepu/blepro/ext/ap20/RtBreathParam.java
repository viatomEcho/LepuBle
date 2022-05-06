package com.lepu.blepro.ext.ap20;

public class RtBreathParam {
    private int rr;
    private int sign;

    public int getRr() {
        return rr;
    }

    public void setRr(int rr) {
        this.rr = rr;
    }

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "RtBreathParam{" +
                "rr=" + rr +
                ", sign=" + sign +
                '}';
    }
}

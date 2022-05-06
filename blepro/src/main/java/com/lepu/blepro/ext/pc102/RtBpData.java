package com.lepu.blepro.ext.pc102;

public class RtBpData {
    private int sign;     // 心跳标记（0 无心跳 1 有心跳）
    private int ps;  // 当前压力值

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }

    public int getPs() {
        return ps;
    }

    public void setPs(int ps) {
        this.ps = ps;
    }

    @Override
    public String toString() {
        return "RtBpData{" +
                "sign=" + sign +
                ", ps=" + ps +
                '}';
    }
}

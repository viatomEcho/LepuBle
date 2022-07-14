package com.lepu.blepro.ble.data;

public class FhrData {

    private int hr1;  // 当前心率
    private int hr2;  // 上一次心率

    public int getHr1() {
        return hr1;
    }

    public void setHr1(int hr1) {
        this.hr1 = hr1;
    }

    public int getHr2() {
        return hr2;
    }

    public void setHr2(int hr2) {
        this.hr2 = hr2;
    }

    @Override
    public String toString() {
        return "FhrData{" +
                "hr1=" + hr1 +
                ", hr2=" + hr2 +
                '}';
    }
}

package com.lepu.blepro.ext;

public class Ad5Data {
    private String sn;
    private int hr1;
    private int hr2;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

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
        return "Ad5Data{" +
                "sn='" + sn + '\'' +
                ", hr1=" + hr1 +
                ", hr2=" + hr2 +
                '}';
    }
}

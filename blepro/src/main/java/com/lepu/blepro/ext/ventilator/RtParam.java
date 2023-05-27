package com.lepu.blepro.ext.ventilator;

public class RtParam {
    private float pressure;  // 实时压(0~40cmH2O),单位0.1cmH20,e.g.10:1cmH2O[0,400],0.5Hz
    private float ipap;      // 吸气压力(0~40cmH2O),单位0.1cmH20,e.g.10:1cmH2O[0,400],0.5Hz
    private float epap;      // 呼气压力(0~40cmH2O),单位0.1cmH20,e.g.10:1cmH2O[0,400],0.5Hz
    private int vt;          // 潮气量(0~3000mL),单位1mL,e.g.10:10mL[0,3000],0.5Hz
    private float mv;        // 分钟通气量(0~60L/min),单位0.1L/min,e.g.10:1L/min[0,600],0.5Hz
    private float leak;      // 漏气量(0~120L/min),单位0.1L/min,e.g.10:1L/min[0,1200],0.5Hz
    private int rr;          // 呼吸率(0~60),单位1bpm,e.g.10:10bpm[0,60],0.5Hz
    private float ti;        // 吸气时间(0.1-4s),单位0.1s,e.g.10:1s[1,40],0.5Hz
    private float ie;        // 呼吸比(1:50.0-3.0:1),单位0.0001,e.g.10000:1[200,30000],0.5Hz
    private int spo2;        // 血氧(70-100%),单位1%,e.g.10:10%[70,100],1Hz
    private int pr;          // 脉率(30-250bpm),单位1bpm,e.g.10:10bpm[30,250],1Hz
    private int hr;          // 心率(30-250bpm),单位1bpm,e.g.10:10bpm[30,250],1Hz

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public float getIpap() {
        return ipap;
    }

    public void setIpap(float ipap) {
        this.ipap = ipap;
    }

    public float getEpap() {
        return epap;
    }

    public void setEpap(float epap) {
        this.epap = epap;
    }

    public int getVt() {
        return vt;
    }

    public void setVt(int vt) {
        this.vt = vt;
    }

    public float getMv() {
        return mv;
    }

    public void setMv(float mv) {
        this.mv = mv;
    }

    public float getLeak() {
        return leak;
    }

    public void setLeak(float leak) {
        this.leak = leak;
    }

    public int getRr() {
        return rr;
    }

    public void setRr(int rr) {
        this.rr = rr;
    }

    public float getTi() {
        return ti;
    }

    public void setTi(float ti) {
        this.ti = ti;
    }

    public float getIe() {
        return ie;
    }

    public void setIe(float ie) {
        this.ie = ie;
    }

    public int getSpo2() {
        return spo2;
    }

    public void setSpo2(int spo2) {
        this.spo2 = spo2;
    }

    public int getPr() {
        return pr;
    }

    public void setPr(int pr) {
        this.pr = pr;
    }

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    @Override
    public String toString() {
        return "RtParam{" +
                "pressure=" + pressure +
                ", ipap=" + ipap +
                ", epap=" + epap +
                ", vt=" + vt +
                ", mv=" + mv +
                ", leak=" + leak +
                ", rr=" + rr +
                ", ti=" + ti +
                ", ie=" + ie +
                ", spo2=" + spo2 +
                ", pr=" + pr +
                ", hr=" + hr +
                '}';
    }
}

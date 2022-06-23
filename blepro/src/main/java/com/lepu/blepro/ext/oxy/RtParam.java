package com.lepu.blepro.ext.oxy;

public class RtParam {

    private int spo2;             // 血氧值
    private int pr;               // 脉率值
    private int steps;            // 步数
    private int battery;          // 电量（0-100%）
    private int batteryState;     // 充电状态（0：没有充电 1：充电中 2：充电完成）
    private int vector;           // 三轴矢量
    private float pi;               // pi值
    private int state;            // 工作状态（0：导联脱落 1：导联连上 其他：异常）
    private int countDown;        // 导联脱落倒计时（10s-0）
    private int invalidIvState;   // 无效值报警（0：未达到报警条件 1：达到报警条件 2：达到报警条件，但是盒子不报警）
    private int spo2IvState;      // 血氧报警（0：未达到报警条件 1：达到报警条件 2：达到报警条件，但是盒子不报警）
    private int hrIvState;        // 心率报警（0：未达到报警条件 1：达到报警条件 2：达到报警条件，但是盒子不报警）
    private int vectorIvState;    // 体动报警（0：未达到报警条件 1：达到报警条件 2：达到报警条件，但是盒子不报警）

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

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getBatteryState() {
        return batteryState;
    }

    public void setBatteryState(int batteryState) {
        this.batteryState = batteryState;
    }

    public int getVector() {
        return vector;
    }

    public void setVector(int vector) {
        this.vector = vector;
    }

    public float getPi() {
        return pi;
    }

    public void setPi(float pi) {
        this.pi = pi;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getCountDown() {
        return countDown;
    }

    public void setCountDown(int countDown) {
        this.countDown = countDown;
    }

    public int getInvalidIvState() {
        return invalidIvState;
    }

    public void setInvalidIvState(int invalidIvState) {
        this.invalidIvState = invalidIvState;
    }

    public int getSpo2IvState() {
        return spo2IvState;
    }

    public void setSpo2IvState(int spo2IvState) {
        this.spo2IvState = spo2IvState;
    }

    public int getHrIvState() {
        return hrIvState;
    }

    public void setHrIvState(int hrIvState) {
        this.hrIvState = hrIvState;
    }

    public int getVectorIvState() {
        return vectorIvState;
    }

    public void setVectorIvState(int vectorIvState) {
        this.vectorIvState = vectorIvState;
    }

    @Override
    public String toString() {
        return "RtParam{" +
                "spo2=" + spo2 +
                ", pr=" + pr +
                ", steps=" + steps +
                ", battery=" + battery +
                ", batteryState=" + batteryState +
                ", vector=" + vector +
                ", pi=" + pi +
                ", state=" + state +
                ", countDown=" + countDown +
                ", invalidIvState=" + invalidIvState +
                ", spo2IvState=" + spo2IvState +
                ", hrIvState=" + hrIvState +
                ", vectorIvState=" + vectorIvState +
                '}';
    }
}

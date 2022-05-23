package com.lepu.blepro.ext.sp20;

public class TempResult {

    private int result;  // 体温结果 0：正常 1：过低 2：过高
    private int unit;    // 单位 0：摄氏度℃ 1：华氏度℉
    private float temp;  // 体温值

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    @Override
    public String toString() {
        return "TempResult{" +
                "result=" + result +
                ", unit=" + unit +
                ", temp=" + temp +
                '}';
    }
}

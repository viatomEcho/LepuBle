package com.lepu.blepro.ext.pc303;

public class GluResult {
    private int unit;
    private float data;
    private int result;
    private String resultMess;

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public float getData() {
        return data;
    }

    public void setData(float data) {
        this.data = data;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getResultMess() {
        return resultMess;
    }

    public void setResultMess(String resultMess) {
        this.resultMess = resultMess;
    }

    @Override
    public String toString() {
        return "GluResult{" +
                "unit=" + unit +
                ", data=" + data +
                ", result=" + result +
                ", resultMess='" + resultMess + '\'' +
                '}';
    }
}

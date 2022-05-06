package com.lepu.blepro.ext.pc303;

public class EcgResult {
    private int hr;
    private int result;
    private String resultMess;

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
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
        return "EcgResult{" +
                "hr=" + hr +
                ", result=" + result +
                ", resultMess='" + resultMess + '\'' +
                '}';
    }
}

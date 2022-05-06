package com.lepu.blepro.ext.pc303;

public class BpResultError {
    private int errorNum;      // 错误编码号
    private String errorMess;  // 错误编码信息

    public int getErrorNum() {
        return errorNum;
    }

    public void setErrorNum(int errorNum) {
        this.errorNum = errorNum;
    }

    public String getErrorMess() {
        return errorMess;
    }

    public void setErrorMess(String errorMess) {
        this.errorMess = errorMess;
    }

    @Override
    public String toString() {
        return "BpResultError{" +
                "errorNum=" + errorNum +
                ", errorMess='" + errorMess + '\'' +
                '}';
    }
}

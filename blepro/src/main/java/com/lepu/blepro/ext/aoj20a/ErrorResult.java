package com.lepu.blepro.ext.aoj20a;

public class ErrorResult {

    private int code;
    private String codeMess;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getCodeMess() {
        return codeMess;
    }

    public void setCodeMess(String codeMess) {
        this.codeMess = codeMess;
    }

    @Override
    public String toString() {
        return "ErrorResult{" +
                "code=" + code +
                ", codeMess='" + codeMess + '\'' +
                '}';
    }
}

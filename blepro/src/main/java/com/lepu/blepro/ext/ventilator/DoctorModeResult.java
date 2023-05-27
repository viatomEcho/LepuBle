package com.lepu.blepro.ext.ventilator;

public class DoctorModeResult {
    private boolean out;
    private boolean success;
    private int errCode;

    public boolean isOut() {
        return out;
    }

    public void setOut(boolean out) {
        this.out = out;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    @Override
    public String toString() {
        return "DoctorModeResult{" +
                "out=" + out +
                ", success=" + success +
                ", errCode=" + errCode +
                '}';
    }
}

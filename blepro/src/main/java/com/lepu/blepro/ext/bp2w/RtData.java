package com.lepu.blepro.ext.bp2w;

public class RtData {

    private RtStatus status;
    private RtParam param;

    public RtStatus getStatus() {
        return status;
    }

    public void setStatus(RtStatus status) {
        this.status = status;
    }

    public RtParam getParam() {
        return param;
    }

    public void setParam(RtParam param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return "RtData{" +
                "status=" + status +
                ", param=" + param +
                '}';
    }
}

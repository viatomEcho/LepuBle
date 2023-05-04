package com.lepu.blepro.ble.cmd;

public class ResponseError {

    private int model;
    private int cmd;
    private int type;

    public int getModel() {
        return model;
    }

    public void setModel(int model) {
        this.model = model;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ResponseError{" +
                "model=" + model +
                ", cmd=" + cmd +
                ", type=" + type +
                '}';
    }
}

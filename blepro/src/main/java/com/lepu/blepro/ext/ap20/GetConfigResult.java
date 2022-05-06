package com.lepu.blepro.ext.ap20;

public class GetConfigResult {
    private int type;
    private int data;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "GetConfigResult{" +
                "type=" + type +
                ", data=" + data +
                '}';
    }
}

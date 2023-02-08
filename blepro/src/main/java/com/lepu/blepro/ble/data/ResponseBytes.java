package com.lepu.blepro.ble.data;

public class ResponseBytes {
    private int dataType;
    private byte[] data;

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}

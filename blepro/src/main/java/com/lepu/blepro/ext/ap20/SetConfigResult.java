package com.lepu.blepro.ext.ap20;

public class SetConfigResult {
    private int type;
    private boolean success;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "SetConfigResult{" +
                "type=" + type +
                ", success=" + success +
                '}';
    }
}

package com.lepu.blepro.ble.cmd.er2;


import com.lepu.blepro.utils.Convertible;

public class Er2SwitcherConfig implements Convertible {
    private int switcher;
    private int vector;
    private int motionCount;
    private int motionWindows;

    public boolean getSwitcher() {
        return switcher == 1;
    }

    public void setSwitcher(boolean state) {
        if(state) {
            switcher = 1;
        } else {
            switcher = 0;
        }
    }

    public Er2SwitcherConfig() {
    }

    public Er2SwitcherConfig(boolean state, int vector, int motionCount, int motionWindows) {
        this.switcher = state ? 1 : 0;
        this.vector = vector;
        this.motionCount = motionCount;
        this.motionWindows = motionWindows;
    }

    public int getVector() {
        return vector;
    }

    public void setVector(int vector) {
        this.vector = vector;
    }

    public int getMotionCount() {
        return motionCount;
    }

    public void setMotionCount(int motionCount) {
        this.motionCount = motionCount;
    }

    public int getMotionWindows() {
        return motionWindows;
    }

    public void setMotionWindows(int motionWindows) {
        this.motionWindows = motionWindows;
    }

    @Override
    public byte[] convert2Data() {
        byte[] data = new byte[5];
        data[0] = (byte) (switcher & 0x01);
        data[1] = (byte) (vector);
        data[2] = (byte) (motionCount);
        data[3] = (byte) (motionWindows & 0xFF);
        data[4] = (byte) ((motionWindows >> 8) & 0xFF);
        return data;
    }

    public static Er2SwitcherConfig parse(byte[] data) {
        Er2SwitcherConfig config = new Er2SwitcherConfig();

        int switcher = data[0] & 0x01;
        boolean state = (switcher == 1);
        config.setSwitcher(state);

        if(data.length == 5) {
            int vector = data[1];
            config.setVector(vector);

            int motionCount = data[2];
            config.setMotionCount(motionCount);

            int motionWindows = (data[3] & 0xFF) + ((data[4] & 0xFF) << 8);
            config.setMotionWindows(motionWindows);
        }

        return config;
    }
}

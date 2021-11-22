package com.lepu.blepro.ble.cmd;

/**
 * universal command for Viatom devices
 */
public class Bpw1BleCmd {

    public final static int MEASURE = 0x00;
    public final static int READ_FILE_LIST = 0x01;
    public final static int CLEAR_FILE_LIST = 0x02;
    public final static int GET_DEVICE_INFO = 0x04;
    public final static int FACTORY_RESET = 0x13;
    public final static int SET_TIME = 0x20;
    public final static int SET_MEASURE_TIME = 0x21;
    public final static int SET_MEASURE_INFO = 0x22;


    public static byte[] startBp() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) 0x5A;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0x02;
        cmd[3] = (byte) MEASURE;
        cmd[4] = (byte) 0x00;
        cmd[5] = (byte) 0x02;
        return cmd;
    }
    public static byte[] stopBp() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) 0x5A;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0x02;
        cmd[3] = (byte) MEASURE;
        cmd[4] = (byte) 0x01;
        cmd[5] = (byte) 0x03;
        return cmd;
    }
    public static byte[] readFileList() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) 0x5A;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0x02;
        cmd[3] = (byte) READ_FILE_LIST;
        cmd[4] = (byte) 0x00;
        cmd[5] = (byte) 0x03;
        return cmd;
    }
    public static byte[] clearFileList() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) 0x5A;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0x02;
        cmd[3] = (byte) CLEAR_FILE_LIST;
        cmd[4] = (byte) 0x00;
        cmd[5] = (byte) 0x00;
        return cmd;
    }
    public static byte[] getDeviceInfo() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) 0x5A;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0x02;
        cmd[3] = (byte) GET_DEVICE_INFO;
        cmd[4] = (byte) 0x00;
        cmd[5] = (byte) 0x06;
        return cmd;
    }
    public static byte[] factoryReset() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) 0x5A;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0x02;
        cmd[3] = (byte) FACTORY_RESET;
        cmd[4] = (byte) 0xAA;
        cmd[5] = (byte) 0xA8;
        return cmd;
    }

}

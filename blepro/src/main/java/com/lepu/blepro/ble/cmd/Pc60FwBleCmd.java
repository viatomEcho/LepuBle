package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.utils.CrcUtil;

/**
 * universal command for Viatom devices
 */
public class Pc60FwBleCmd {

    public final static int HEAD_0 = 0xAA;
    public final static int HEAD_1 = 0x55;

    public final static int TOKEN_PULSE_OXIMETER = 0x0F;
    public final static int TOKEN_DEVICE_PUBLIC_INFO = 0xF0;

    public final static int GET_DEVICE_INFO = 0x81;
    public final static int DEVICE_INFO_RESPONSE = 0x01;
    public final static int GET_DEVICE_SN = 0x82;
    public final static int DEVICE_SN_RESPONSE = 0x02;
    public final static int BATLEVEL_RESPONSE = 0x03;

    public final static int RT_PARAM = 0x01;
    public final static int RT_WAVE = 0x02;
    public final static int IR_RED_FREQ = 0x20;
    public final static int WORK_STATUS_DATA = 0x21;

    // 查询版本/名称
    public static byte[] getInfo() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_DEVICE_PUBLIC_INFO;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) GET_DEVICE_INFO;
        cmd[5] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }
    // 查询设备序列号
    public static byte[] getSn() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_DEVICE_PUBLIC_INFO;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) GET_DEVICE_SN;
        cmd[5] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }

}

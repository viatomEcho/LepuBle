package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.utils.CrcUtil;

/**
 * @author chenyongfeng
 */
public class Pc60FwBleCmd {

    public static final int HEAD_0 = 0xAA;
    public static final int HEAD_1 = 0x55;

    public static final int TOKEN_F0 = 0xF0;
    public static final int CMD_GET_DEVICE_INFO_F0 = 0x81;
    public static final int MSG_GET_DEVICE_INFO_F0 = 0x01;
    public static final int CMD_GET_DEVICE_SN = 0x82;
    public static final int MSG_GET_DEVICE_SN = 0x02;
    public static final int MSG_GET_BATTERY = 0x03;
    public static final int CMD_GET_DEVICE_MAC = 0xA1;
    public static final int MSG_GET_DEVICE_MAC = 0x21;

    public static final int TOKEN_0F = 0x0F;
    public static final int MSG_RT_PARAM = 0x01;
    public static final int MSG_RT_WAVE = 0x02;
    public static final int CMD_GET_DEVICE_INFO_0F = 0x83;
    public static final int MSG_GET_DEVICE_INFO_0F = 0x03;
    public static final int CMD_ENABLE_PARAM = 0x84;
    public static final int MSG_ENABLE_PARAM = 0x04;
    public static final int CMD_ENABLE_WAVE = 0x85;
    public static final int MSG_ENABLE_WAVE = 0x05;
    public static final int MSG_IR_RED_FREQ = 0x20;
    public static final int MSG_WORK_STATUS_DATA = 0x21;

    /**
     * 0：使能血氧参数
     * 1：使能血氧波形
     */
    public static class EnableType {
        public static final int OXY_PARAM = 0;
        public static final int OXY_WAVE = 1;
    }

    /**
     * 使能发送开关
     * @param type EnableType
     * @param enable boolean
     * @return byte数组
     */
    public static byte[] enableSwitch(int type, boolean enable) {
        byte[] cmd = new byte[7];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_0F;
        cmd[3] = (byte) 0x03;
        if (type == EnableType.OXY_PARAM) {
            cmd[4] = (byte) CMD_ENABLE_PARAM;
        } else {
            cmd[4] = (byte) CMD_ENABLE_WAVE;
        }
        if (enable) {
            cmd[5] = (byte) 0x01;
        } else {
            cmd[5] = (byte) 0x00;
        }
        cmd[6] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }

    public static byte[] getInfo0F() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_0F;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) CMD_GET_DEVICE_INFO_0F;
        cmd[5] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }
    /**
     * 查询版本/名称
     * @return byte数组
     */
    public static byte[] getInfoF0() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_F0;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) CMD_GET_DEVICE_INFO_F0;
        cmd[5] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }
    /**
     * 查询设备序列号
     * @return byte数组
     */
    public static byte[] getSn() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_F0;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) CMD_GET_DEVICE_SN;
        cmd[5] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }

    /**
     * 查询设备mac地址
     */
    public static byte[] getMac() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_F0;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) CMD_GET_DEVICE_MAC;
        cmd[5] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }

}

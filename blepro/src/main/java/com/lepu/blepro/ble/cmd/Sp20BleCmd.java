package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.ble.data.TimeData;
import com.lepu.blepro.utils.CrcUtil;

import java.util.Date;

/**
 * @author chenyongfeng
 */
public class Sp20BleCmd {

    public static final int HEAD_0 = 0xAA;
    public static final int HEAD_1 = 0x55;

    public static final int TOKEN_F0 = 0xF0;
    public static final int CMD_GET_DEVICE_INFO = 0x81;
    public static final int MSG_GET_DEVICE_INFO = 0x01;
    public static final int CMD_GET_DEVICE_SN = 0x82;
    public static final int MSG_GET_DEVICE_SN = 0x02;
    public static final int CMD_GET_BATTERY = 0x83;
    public static final int MSG_GET_BATTERY = 0x03;

    public static final int TOKEN_0F = 0x0F;
    public static final int MSG_RT_OXY_PARAM = 0x01;
    public static final int MSG_RT_OXY_WAVE = 0x02;
    public static final int CMD_ENABLE_OXY_PARAM = 0x84;
    public static final int MSG_ENABLE_OXY_PARAM = 0x04;
    public static final int CMD_ENABLE_OXY_WAVE = 0x85;
    public static final int MSG_ENABLE_OXY_WAVE = 0x05;
    public static final int CMD_SET_TIME = 0x87;
    public static final int MSG_SET_TIME = 0x07;
    public static final int CMD_GET_CONFIG = 0x91;
    public static final int MSG_GET_CONFIG = 0x11;
    public static final int CMD_SET_CONFIG = 0x92;
    public static final int MSG_SET_CONFIG = 0x12;

    public static final int TOKEN_3C = 0x3C;
    public static final int MSG_TEMP = 0x01;


    /**
     * 1：警报功能开关
     * 2：血氧过低阈值
     * 3：脉率过低阈值
     * 4：脉率过高阈值
     * 5：搏动音开关
     */
    public static class ConfigType {
        public static final int ALARM_SWITCH = 1;
        public static final int LOW_OXY_THRESHOLD = 2;
        public static final int LOW_HR_THRESHOLD = 3;
        public static final int HIGH_HR_THRESHOLD = 4;
        public static final int PULSE_BEEP = 5;
    }

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
     * @param enable boolean
     * @return byte数组
     */
    public static byte[] enableSwitch(int type, boolean enable) {
        int cmd = CMD_ENABLE_OXY_PARAM;
        switch (type) {
            case EnableType.OXY_PARAM:
                cmd = CMD_ENABLE_OXY_PARAM;
                break;
            case EnableType.OXY_WAVE:
                cmd = CMD_ENABLE_OXY_WAVE;
                break;
            default:
                break;
        }
        return getReq(TOKEN_0F, cmd, new byte[]{(byte)(enable?1:0)});
    }

    /**
     * 查询版本/名称
     * @return byte数组
     */
    public static byte[] getInfo() {
        return getReq(TOKEN_F0, CMD_GET_DEVICE_INFO, new byte[0]);
    }

    /**
     * 查询设备序列号
     * @return byte数组
     */
    public static byte[] getSn() {
        return getReq(TOKEN_F0, CMD_GET_DEVICE_SN, new byte[0]);
    }

    /**
     * 查询电池电量
     * @return byte数组
     */
    public static byte[] getBattery() {
        return getReq(TOKEN_F0, CMD_GET_BATTERY, new byte[0]);
    }

    /**
     * 设置设备时间
     * @return byte数组
     */
    public static byte[] setTime() {
        TimeData timeData = new TimeData(new Date());
        byte[] data = timeData.convert2DataBig();
        return getReq(TOKEN_0F, CMD_SET_TIME, data);
    }

    /**
     * 查询血氧警报参数设置信息
     * @param type int(1-4)
     * @return byte数组
     */
    public static byte[] getConfig(int type) {
        return getReq(TOKEN_0F, CMD_GET_CONFIG, new byte[]{(byte)type});
    }

    /**
     * 设置血氧警报参数设置信息
     * @param config Sp20Config
     * @return byte数组
     */
    public static byte[] setConfig(byte[] config) {
        return getReq(TOKEN_0F, CMD_SET_CONFIG, config);
    }

    private static byte[] getReq(int token, int sendCmd, byte[] data) {
        int len = data.length;
        byte[] cmd = new byte[6+len];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) token;
        cmd[3] = (byte) (len+2);
        cmd[4] = (byte) sendCmd;

        System.arraycopy(data, 0, cmd, 5, data.length);

        cmd[cmd.length-1] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }

}

package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.ble.data.TimeData;
import com.lepu.blepro.utils.CrcUtil;
import java.util.Date;

/**
 * @author chenyongfeng
 */
public class Ap20BleCmd {

    public static final int HEAD_0 = 0xAA;
    public static final int HEAD_1 = 0x55;

    public static final int TOKEN_F0 = 0xF0;
    public static final int CMD_GET_DEVICE_INFO = 0x81;
    public static final int MSG_GET_DEVICE_INFO = 0x01;
    public static final int CMD_GET_DEVICE_SN = 0x82;
    public static final int MSG_GET_DEVICE_SN = 0x02;
    public static final int CMD_GET_BATTERY = 0x83;
    public static final int MSG_GET_BATTERY = 0x03;
    public static final int CMD_GET_BACKLIGHT = 0x84;
    public static final int MSG_GET_BACKLIGHT = 0x04;
    public static final int CMD_SET_BACKLIGHT = 0x85;
    public static final int MSG_SET_BACKLIGHT = 0x05;

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

    public static final int TOKEN_2D = 0x2D;
    public static final int MSG_RT_BREATH_WAVE = 0x01;
    public static final int MSG_RT_BREATH_PARAM = 0x02;
    public static final int CMD_ENABLE_BREATH_WAVE = 0x83;
    public static final int MSG_ENABLE_BREATH_WAVE = 0x03;
    public static final int CMD_ENABLE_BREATH_PARAM = 0x84;
    public static final int MSG_ENABLE_BREATH_PARAM = 0x04;

    /**
     * 0：背光等级
     * 1：警报功能开关
     * 2：血氧过低阈值
     * 3：脉率过低阈值
     * 4：脉率过高阈值
     */
    public static class ConfigType {
        public static final int BACK_LIGHT = 0;
        public static final int ALARM_SWITCH = 1;
        public static final int LOW_OXY_THRESHOLD = 2;
        public static final int LOW_HR_THRESHOLD = 3;
        public static final int HIGH_HR_THRESHOLD = 4;
    }

    /**
     * 0：使能血氧参数
     * 1：使能血氧波形
     * 2：使能鼻息流参数
     * 3：使能鼻息流波形
     */
    public static class EnableType {
        public static final int OXY_PARAM = 0;
        public static final int OXY_WAVE = 1;
        public static final int BREATH_PARAM = 2;
        public static final int BREATH_WAVE = 3;
    }

    /**
     * 使能发送开关
     * @param enable boolean
     * @return byte数组
     */
    public static byte[] enableSwitch(int type, boolean enable) {
        int token = TOKEN_0F;
        int cmd = CMD_ENABLE_OXY_PARAM;
        switch (type) {
            case EnableType.OXY_PARAM:
                token = TOKEN_0F;
                cmd = CMD_ENABLE_OXY_PARAM;
                break;
            case EnableType.OXY_WAVE:
                token = TOKEN_0F;
                cmd = CMD_ENABLE_OXY_WAVE;
                break;
            case EnableType.BREATH_PARAM:
                token = TOKEN_2D;
                cmd = CMD_ENABLE_BREATH_PARAM;
                break;
            case EnableType.BREATH_WAVE:
                token = TOKEN_2D;
                cmd = CMD_ENABLE_BREATH_WAVE;
                break;
            default:
                break;
        }
        return getReq(token, cmd, new byte[]{(byte)(enable?1:0)});
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
     * 查询设备背光等级
     * @return byte数组
     */
    public static byte[] getBacklight() {
        return getReq(TOKEN_F0, CMD_GET_BACKLIGHT, new byte[0]);
    }

    /**
     * 设置设备背光等级
     * @param level int(0-5)
     * @return byte数组
     */
    public static byte[] setBacklight(int level) {
        return getReq(TOKEN_F0, CMD_SET_BACKLIGHT, new byte[]{(byte)level});
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
     * @param type int(1-4)
     * @param config int
     * @return byte数组
     */
    public static byte[] setConfig(int type, int config) {
        byte[] data = new byte[2];
        data[0] = (byte) type;
        data[1] = (byte) config;
        return getReq(TOKEN_0F, CMD_SET_CONFIG, data);
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

package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.ble.data.TimeData;
import com.lepu.blepro.utils.CrcUtil;
import java.util.Date;

/**
 * universal command for Viatom devices
 */
public class Ap20BleCmd {

    public final static int HEAD_0 = 0xAA;
    public final static int HEAD_1 = 0x55;

    public final static int TOKEN_F0 = 0xF0;
    public final static int CMD_GET_DEVICE_INFO = 0x81;
    public final static int MSG_GET_DEVICE_INFO = 0x01;
    public final static int CMD_GET_DEVICE_SN = 0x82;
    public final static int MSG_GET_DEVICE_SN = 0x02;
    public final static int CMD_GET_BATTERY = 0x83;
    public final static int MSG_GET_BATTERY = 0x03;
    public final static int CMD_GET_BACKLIGHT = 0x84;
    public final static int MSG_GET_BACKLIGHT = 0x04;
    public final static int CMD_SET_BACKLIGHT = 0x85;
    public final static int MSG_SET_BACKLIGHT = 0x05;

    public final static int TOKEN_0F = 0x0F;
    public final static int MSG_RT_BO_PARAM = 0x01;
    public final static int MSG_RT_BO_WAVE = 0x02;
    public final static int CMD_ENABLE_BO_PARAM = 0x84;
    public final static int MSG_ENABLE_BO_PARAM = 0x04;
    public final static int CMD_ENABLE_BO_WAVE = 0x85;
    public final static int MSG_ENABLE_BO_WAVE = 0x05;
    public final static int CMD_SET_TIME = 0x87;
    public final static int MSG_SET_TIME = 0x07;
    public final static int CMD_GET_CONFIG = 0x91;
    public final static int MSG_GET_CONFIG = 0x11;
    public final static int CMD_SET_CONFIG = 0x92;
    public final static int MSG_SET_CONFIG = 0x12;

    public final static int TOKEN_2D = 0x2D;
    public final static int MSG_RT_BREATH_WAVE = 0x01;
    public final static int MSG_RT_BREATH_PARAM = 0x02;
    public final static int CMD_ENABLE_BREATH_WAVE = 0x83;
    public final static int MSG_ENABLE_BREATH_WAVE = 0x03;
    public final static int CMD_ENABLE_BREATH_PARAM = 0x84;
    public final static int MSG_ENABLE_BREATH_PARAM = 0x04;

    // 查询版本/名称
    public static byte[] getInfo() {
        int len = 1;
        byte[] cmd = new byte[5+len];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_F0;
        cmd[3] = (byte) (len+1);
        cmd[4] = (byte) CMD_GET_DEVICE_INFO;
        cmd[5] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }
    // 查询设备序列号
    public static byte[] getSn() {
        int len = 1;
        byte[] cmd = new byte[5+len];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_F0;
        cmd[3] = (byte) (len+1);
        cmd[4] = (byte) CMD_GET_DEVICE_SN;
        cmd[5] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }
    // 查询电池电量
    public static byte[] getBattery() {
        int len = 1;
        byte[] cmd = new byte[5+len];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_F0;
        cmd[3] = (byte) (len+1);
        cmd[4] = (byte) CMD_GET_BATTERY;
        cmd[5] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }
    // 查询设备背光等级
    public static byte[] getBacklight() {
        int len = 1;
        byte[] cmd = new byte[5+len];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_F0;
        cmd[3] = (byte) (len+1);
        cmd[4] = (byte) CMD_GET_BACKLIGHT;
        cmd[5] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }
    // 设置设备背光等级
    public static byte[] setBacklight(int level) {
        int len = 2;
        byte[] cmd = new byte[5+len];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_F0;
        cmd[3] = (byte) (len+1);
        cmd[4] = (byte) CMD_SET_BACKLIGHT;
        cmd[5] = (byte) level;
        cmd[6] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }

    // 血氧使能参数发送
    public static byte[] enableBoParam(boolean enable) {
        int len = 2;
        byte[] cmd = new byte[5+len];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_0F;
        cmd[3] = (byte) (len+1);
        cmd[4] = (byte) CMD_ENABLE_BO_PARAM;
        cmd[5] = (byte) (enable?1:0);
        cmd[6] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }
    // 血氧使能波形发送
    public static byte[] enableBoWave(boolean enable) {
        int len = 2;
        byte[] cmd = new byte[5+len];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_0F;
        cmd[3] = (byte) (len+1);
        cmd[4] = (byte) CMD_ENABLE_BO_WAVE;
        cmd[5] = (byte) (enable?1:0);
        cmd[6] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }
    // 设置设备时间
    public static byte[] setTime() {
        TimeData timeData = new TimeData(new Date());
        byte[] data = timeData.convert2Data();
        int len = data.length + 1;
        byte[] cmd = new byte[5+len];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_0F;
        cmd[3] = (byte) (len+1);
        cmd[4] = (byte) CMD_SET_TIME;
        for(int i = 0; i < data.length; i++) {
            cmd[i + 5] = data[i];
        }
        cmd[cmd.length - 1] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }
    // 查询血氧警报参数设置信息
    public static byte[] getConfig(int type) {
        int len = 2;
        byte[] cmd = new byte[5+len];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_0F;
        cmd[3] = (byte) (len+1);
        cmd[4] = (byte) CMD_GET_CONFIG;
        cmd[5] = (byte) type;
        cmd[6] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }
    // 设置血氧警报参数设置信息
    public static byte[] setConfig(int type, int config) {
        int len = 3;
        byte[] cmd = new byte[5+len];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_0F;
        cmd[3] = (byte) (len+1);
        cmd[4] = (byte) CMD_SET_CONFIG;
        cmd[5] = (byte) type;
        cmd[6] = (byte) config;
        cmd[7] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }

    // 鼻息流使能参数发送
    public static byte[] enableBreathParam(boolean enable) {
        int len = 2;
        byte[] cmd = new byte[5+len];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_2D;
        cmd[3] = (byte) (len+1);
        cmd[4] = (byte) CMD_ENABLE_BREATH_PARAM;
        cmd[5] = (byte) (enable?1:0);
        cmd[6] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }
    // 鼻息流使能波形发送
    public static byte[] enableBreathWave(boolean enable) {
        int len = 2;
        byte[] cmd = new byte[5+len];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_2D;
        cmd[3] = (byte) (len+1);
        cmd[4] = (byte) CMD_ENABLE_BREATH_WAVE;
        cmd[5] = (byte) (enable?1:0);
        cmd[6] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }

}

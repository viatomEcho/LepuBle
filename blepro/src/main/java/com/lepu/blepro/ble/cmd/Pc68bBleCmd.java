package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.ble.data.TimeData;
import com.lepu.blepro.utils.CrcUtil;
import java.util.Date;
import static com.lepu.blepro.utils.ByteArrayKt.shortToByteArray;

/**
 * @author chenyongfeng
 */
public class Pc68bBleCmd {

    public static final int HEAD_0 = 0xAA;
    public static final int HEAD_1 = 0x55;

    public static final int TOKEN_F0 = 0xF0;
    public static final int CMD_GET_DEVICE_INFO = 0x81;
    public static final int MSG_GET_DEVICE_INFO = 0x01;
    public static final int CMD_GET_DEVICE_SN = 0x82;
    public static final int MSG_GET_DEVICE_SN = 0x02;

    public static final int TOKEN_0F = 0x0F;
    public static final int MSG_RT_PARAM = 0x01;
    public static final int MSG_RT_WAVE = 0x02;
    public static final int CMD_STATE_INFO = 0x80;
    public static final int MSG_STATE_INFO = 0x00;
    public static final int CMD_ENABLE_RT_PARAM = 0x84;
    public static final int MSG_ENABLE_RT_PARAM = 0x04;
    public static final int CMD_ENABLE_RT_WAVE = 0x85;
    public static final int MSG_ENABLE_RT_WAVE = 0x05;
    public static final int CMD_GET_TIME = 0x86;
    public static final int MSG_GET_TIME = 0x06;
    public static final int CMD_SET_TIME = 0x87;
    public static final int MSG_SET_TIME = 0x07;
    public static final int CMD_GET_FILES = 0x8A;
    public static final int MSG_GET_FILES = 0x0A;
    public static final int MSG_FILE_CONTENT = 0x0B;
    public static final int CMD_GET_OR_SET_CONFIG = 0x8C;
    public static final int MSG_GET_OR_SET_CONFIG = 0x0C;
    public static final int CMD_DELETE_FILE = 0x8D;
    public static final int MSG_DELETE_FILE = 0x0D;

    /**
     * 0：使能血氧参数
     * 1：使能血氧波形
     */
    public static class EnableType {
        public static final int RT_PARAM = 0;
        public static final int RT_WAVE = 1;
    }

    /**
     * 使能发送开关
     * @param enable boolean
     * @return byte数组
     */
    public static byte[] enableSwitch(int type, boolean enable) {
        int cmd = CMD_ENABLE_RT_PARAM;
        switch (type) {
            case EnableType.RT_PARAM:
                cmd = CMD_ENABLE_RT_PARAM;
                break;
            case EnableType.RT_WAVE:
                cmd = CMD_ENABLE_RT_WAVE;
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
     * 查询设备序列号（定制版本）
     * @return byte数组
     */
    public static byte[] getSn() {
        return getReq(TOKEN_F0, CMD_GET_DEVICE_SN, new byte[0]);
    }

    /**
     * 查询设备时间（定制版本）
     * @return byte数组
     */
    public static byte[] getTime() {
        return getReq(TOKEN_0F, CMD_GET_TIME, new byte[0]);
    }

    /**
     * 设置设备时间（定制版本）
     * @return byte数组
     */
    public static byte[] setTime() {
        TimeData timeData = new TimeData(new Date());
        byte[] data = timeData.convert2DataBig();
        return getReq(TOKEN_0F, CMD_SET_TIME, data);
    }

    /**
     * 查询血氧警报参数设置信息
     * @return byte数组
     */
    public static byte[] getConfig() {
        byte[] data = new byte[7];
        return getReq(TOKEN_0F, CMD_GET_OR_SET_CONFIG, data);
    }

    /**
     * 设置血氧警报参数设置信息
     * @param config Pc68bConfig
     * @return byte数组
     */
    public static byte[] setConfig(byte[] config) {
        byte[] data = new byte[config.length+1];
        data[0] = (byte) 0x01;
        System.arraycopy(config, 0, data, 1, config.length);
        return getReq(TOKEN_0F, CMD_GET_OR_SET_CONFIG, data);
    }

    public static byte[] getStateInfo(int interval) {
        return getReq(TOKEN_0F, CMD_STATE_INFO, new byte[]{(byte)interval});
    }

    public static byte[] getFiles(int num) {
        return getReq(TOKEN_0F, CMD_GET_FILES, shortToByteArray(num));
    }

    public static byte[] deleteFile() {
        return getReq(TOKEN_0F, CMD_DELETE_FILE, new byte[0]);
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

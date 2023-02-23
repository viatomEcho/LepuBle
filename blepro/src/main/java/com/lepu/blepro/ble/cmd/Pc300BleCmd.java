package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.ble.data.TimeData;
import com.lepu.blepro.utils.CrcUtil;
import java.util.Date;

import static com.lepu.blepro.utils.ByteArrayKt.int2ByteArray;

/**
 * @author chenyongfeng
 */
public class Pc300BleCmd {

    public static final int HEAD_0 = 0xAA;
    public static final int HEAD_1 = 0x55;

    public static final int TOKEN_0XFF = 0xFF;
    public static final int GET_DEVICE_NAME = 0x01;  // 查询产品名称
    public static final int DEVICE_INFO_2 = 0x02;    // 查询版本及电量等级
    public static final int DEVICE_INFO_4 = 0x04;    // 查询版本及电量等级
    public static final int SET_TIME = 0x03;         // 设置时间
    public static final int GET_DEVICE_ID = 0x10;    // 查询产品 ID
    public static final int SET_DEVICE_ID = 0x20;    // 设置产品 ID

    public static final int TOKEN_0X51 = 0x51;
    public static final int OXY_RT_STATE = 0x02;     // 血氧上传状态数据包
    public static final int DEVICE_INFO = 0x01;      // 查询产品版本及电量等级

    public static final int TOKEN_0XD0 = 0xD0;       // 上传PC_300SNT 关机命令信息

    public static final int TOKEN_0X40 = 0x40;
    public static final int BP_START = 0x01;         // 血压开始测量命令
    public static final int BP_STOP = 0x02;          // 血压停止测量命令
    public static final int BP_MODE = 0x03;          // 血压模式命令

    public static final int TOKEN_0X42 = 0x42;       // 血压当前值和心跳信息

    public static final int TOKEN_0X43 = 0x43;
    public static final int BP_RESULT = 0x01;        // 血压测量结果
    public static final int BP_ERROR_RESULT = 0x02;  // 血压测量出现的错误结果

    public static final int TOKEN_0X52 = 0x52;       // 血氧上传波形数据包
    public static final int DISABLE_WAVE = 0x00;     // 禁止主动发送数据
    public static final int ENABLE_WAVE = 0x01;      // 允许主动发送数据

    public static final int TOKEN_0X53 = 0x53;       // 血氧上传参数数据包
    public static final int TOKEN_0X70 = 0x70;       // 体温开始测量命令

    public static final int TOKEN_0X72 = 0x72;
    public static final int TEMP_RESULT = 0x01;      // 体温测量结果
    public static final int SET_TEMP_MODE = 0x02;  // 配置体温计参数
    public static final int GET_TEMP_MODE = 0x03;  // 查询体温计参数

    public static final int TOKEN_0X73 = 0x73;       // 血糖结果

    public static final int TOKEN_0XE0 = 0xE0;
    public static final int BS_UNIT = 0x03;          // 控制血糖显示单位(仅适用百捷)

    public static final int TOKEN_0XE3 = 0xE3;       // 设置下位机血糖仪类型
    public static final int TOKEN_0XE4 = 0xE4;       // 查询下位机当前配置的血糖仪类型
    public static final int TOKEN_0XE5 = 0xE5;       // 清除血糖历史数据

    public static final int TOKEN_0XE2 = 0xE2;       // 血糖结果(仅适用百捷)
    public static final int GLU_RESULT = 0x01;       // 血糖
    public static final int UA_RESULT = 0x02;        // 尿酸
    public static final int CHOL_RESULT = 0x03;      // 总胆固醇


    // 心电部分
    public static final int TOKEN_0X30 = 0x30;
    public static final int ECG_START = 0x01;         // 开始测量命令
    public static final int ECG_STOP = 0x02;          // 停止测量命令
    public static final int ECG_DATA_DIGIT = 0x05;    // 设置心电数据位数

    public static final int TOKEN_0X31 = 0x31;
    public static final int GET_VERSION = 0x01;       // 查询版本
    public static final int ECG_RT_STATE = 0x02;      // 查询工作状态

    public static final int TOKEN_0X32 = 0x32;        // 上传数据
    public static final int TOKEN_0X33 = 0x33;        // 上传参数
    public static final int TOKEN_0X34 = 0x34;        // 设备硬件增益

    private static byte[] getReq(int token, int sendCmd, byte[] data) {
        int len = data.length;
        byte[] cmd = new byte[6+len];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) token;
        cmd[3] = (byte) (len+2);
        cmd[4] = (byte) sendCmd;
        System.arraycopy(data, 0, cmd, 5, len);
        cmd[cmd.length-1] = CrcUtil.calCRC8Pc(cmd);
        return cmd;
    }

    public static byte[] getDeviceName() {
        return getReq(TOKEN_0XFF, GET_DEVICE_NAME, new byte[0]);
    }
    public static byte[] getDeviceInfoFf2() {
        return getReq(TOKEN_0XFF, DEVICE_INFO_2, new byte[0]);
    }
    public static byte[] getDeviceInfoFf4() {
        return getReq(TOKEN_0XFF, DEVICE_INFO_4, new byte[0]);
    }
    public static byte[] getDeviceInfo51() {
        return getReq(TOKEN_0X51, 0x01, new byte[0]);
    }
    public static byte[] getDeviceId() {
        return getReq(TOKEN_0XFF, GET_DEVICE_ID, new byte[0]);
    }
    public static byte[] setDeviceId(int id) {
        return getReq(TOKEN_0XFF, SET_DEVICE_ID, int2ByteArray(id));
    }
    public static byte[] setTime() {
        TimeData timeData = new TimeData(new Date());
        byte[] data = new byte[6];
        data[0] = (byte) (timeData.getYear()-2000);
        data[1] = (byte) timeData.getMonth();
        data[2] = (byte) timeData.getDate();
        data[3] = (byte) timeData.getHour();
        data[4] = (byte) timeData.getMinute();
        data[5] = (byte) timeData.getSecond();
        return getReq(TOKEN_0XFF, SET_TIME, data);
    }
    public static class BpMode {
        public static final int ADULT_MODE = 0;
        public static final int BABY_MODE = 1;
        public static final int CHILD_MODE = 2;
    }
    public static byte[] getBpMode() {
        return getReq(TOKEN_0X40, BP_MODE, new byte[]{(byte)0x10});
    }
    public static byte[] setBpMode(int mode) {
        return getReq(TOKEN_0X40, BP_MODE, new byte[]{(byte)mode});
    }

    public static byte[] getTempResult() {
        return getReq(TOKEN_0X72, TEMP_RESULT, new byte[0]);
    }
    public static class TempMode {
        public static final int EAR_C = 0x11;
        public static final int ADULT_HEAD_C = 0x21;
        public static final int CHILD_HEAD_C = 0x31;
        public static final int OBJECT_C = 0x41;
        public static final int EAR_F = 0x12;
        public static final int ADULT_HEAD_F = 0x22;
        public static final int CHILD_HEAD_F = 0x32;
        public static final int OBJECT_F = 0x42;
    }
    public static byte[] setTempMode(int mode) {
        return getReq(TOKEN_0X72, SET_TEMP_MODE, new byte[]{(byte)mode});
    }
    public static byte[] getTempMode() {
        return getReq(TOKEN_0X72, GET_TEMP_MODE, new byte[0]);
    }

    public static class GlucometerType {
        public static final int AI_AO_LE = 1;
        public static final int BAI_JIE = 2;
        public static final int ON_CALL_SURE_SYNC = 3;
    }
    public static byte[] setGlucometerType(int type) {
        return getReq(TOKEN_0XE3, type, new byte[]{0});
    }
    public static byte[] getGlucometerType() {
        return getReq(TOKEN_0XE4, 0x01, new byte[0]);
    }

    public static class GluUnit {
        public static final int MMOL_L = 0;
        public static final int MG_DL = 1;
    }
    public static byte[] setGluUnit(int unit) {
        return getReq(TOKEN_0XE0, BS_UNIT, new byte[]{(byte)unit});
    }
    public static byte[] deleteFile() {
        return getReq(TOKEN_0XE5, 0x01, new byte[0]);
    }

    public static byte[] getVersion() {
        return getReq(TOKEN_0X31, GET_VERSION, new byte[0]);
    }
    public static byte[] ecgRtState() {
        return getReq(TOKEN_0X31, ECG_RT_STATE, new byte[0]);
    }
    public static byte[] startEcg() {
        return getReq(TOKEN_0X30, ECG_START, new byte[0]);
    }
    public static byte[] stopEcg() {
        return getReq(TOKEN_0X30, ECG_STOP, new byte[0]);
    }
    public static byte[] ecgDataDigit(int digit) {
        return getReq(TOKEN_0X30, ECG_DATA_DIGIT, new byte[]{(byte)digit});
    }

}

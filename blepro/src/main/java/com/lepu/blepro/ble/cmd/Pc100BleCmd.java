package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.utils.CrcUtil;

/**
 * universal command for Viatom devices
 */
public class Pc100BleCmd {

    public final static int HEAD_0 = 0xAA;
    public final static int HEAD_1 = 0x55;

    public final static int TOKEN_0XFF = 0xFF;
    public final static int HAND_SHAKE = 0x01;
    public final static int GET_DEVICE_ID = 0x10;
    // 基本控制
    public final static int GET_DEVICE_INFO = 0x51;
    // 血压控制
    public final static int BP_MODULE_STATE = 0x40;
    public final static int BP_START = 0x01;
    public final static int BP_END = 0x02;
    public final static int BP_CAL_MODE = 0x04;
    public final static int BP_GET_RESULT = 0x43;
    public final static int BP_RESULT = 0x01;
    public final static int BP_RESULT_ERROR = 0x02;
    public final static int BP_GET_STATUS = 0x41;
    public final static int BP_RT_DATA = 0x42;
    public final static int BP_SET_CAL_DATA = 0x44;
    // 血氧控制
    public static final int BO_MODULE_STATE = 0x50;
    public static final int BO_START = 0x01;
    public static final int BO_END = 0x02;
    public static final int BO_GET_STATUS = 0x54;
    public static final int BO_RT_WAVE = 0x52;
    public static final int BO_RT_PARAM = 0x53;
    // 血糖控制
    public static final int BS_MODULE_STATE = 0xE0;
    public static final int BS_START = 0x01;
    public static final int BS_END = 0x02;
    public static final int BS_GET_RESULT = 0xE2;
    public static final int BS_GET_STATUS = 0xE1;
    // 体温控制
    public static final int BT_MODULE_STATE = 0x70;
    public static final int BT_START = 0x01;
    public static final int BT_END = 0x02;
    public static final int BT_GET_RESULT = 0x72;
    public static final int BT_GET_STATUS = 0x71;

    // 握手连接
    public static byte[] handShake() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_0XFF;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) HAND_SHAKE;
        cmd[5] = CrcUtil.calCRC8PC(cmd);
        return cmd;
    }

    // 设备查询
    public static byte[] getDeviceInfo() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) GET_DEVICE_INFO;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) 0x01;
        cmd[5] = CrcUtil.calCRC8PC(cmd);
        return cmd;
    }

    // 查询客户ID
    public static byte[] getDeviceId() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) TOKEN_0XFF;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) GET_DEVICE_ID;
        cmd[5] = CrcUtil.calCRC8PC(cmd);
        return cmd;
    }

    // 血压模块 state:01测量开始 02测量结束 04血压校准
    public static byte[] setBpModuleState(int state) {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) BP_MODULE_STATE;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) state;
        cmd[5] = CrcUtil.calCRC8PC(cmd);
        return cmd;
    }
    // 血压测量结果查询
    public static byte[] getBpResult() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) BP_GET_RESULT;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) 0x01;
        cmd[5] = CrcUtil.calCRC8PC(cmd);
        return cmd;
    }
    // 血压状态查询
    public static byte[] getBpStatus() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) BP_GET_STATUS;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) 0x01;
        cmd[5] = CrcUtil.calCRC8PC(cmd);
        return cmd;
    }

    // 血氧模块 state:01测量开始 02测量结束
    public static byte[] setBoModuleState(int state) {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) BO_MODULE_STATE;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) state;
        cmd[5] = CrcUtil.calCRC8PC(cmd);
        return cmd;
    }
    // 血氧状态查询
    public static byte[] getBoStatus() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) BO_GET_STATUS;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) 0x01;
        cmd[5] = CrcUtil.calCRC8PC(cmd);
        return cmd;
    }

    // 血糖模块 state:01测量开始 02测量结束
    public static byte[] setBsModuleState(int state) {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) BS_MODULE_STATE;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) state;
        cmd[5] = CrcUtil.calCRC8PC(cmd);
        return cmd;
    }
    // 血糖测量结果查询
    public static byte[] getBsResult() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) BS_GET_RESULT;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) 0x01;
        cmd[5] = CrcUtil.calCRC8PC(cmd);
        return cmd;
    }
    // 血糖状态查询
    public static byte[] getBsStatus() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) BS_GET_STATUS;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) 0x01;
        cmd[5] = CrcUtil.calCRC8PC(cmd);
        return cmd;
    }

    // 体温模块 state:01测量开始 02测量结束
    public static byte[] setBtModuleState(int state) {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) BT_MODULE_STATE;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) state;
        cmd[5] = CrcUtil.calCRC8PC(cmd);
        return cmd;
    }
    // 体温测量结果查询
    public static byte[] getBtResult() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) BT_GET_RESULT;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) 0x01;
        cmd[5] = CrcUtil.calCRC8PC(cmd);
        return cmd;
    }
    // 体温状态查询
    public static byte[] getBtStatus() {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) HEAD_0;
        cmd[1] = (byte) HEAD_1;
        cmd[2] = (byte) BT_GET_STATUS;
        cmd[3] = (byte) 0x02;
        cmd[4] = (byte) 0x01;
        cmd[5] = CrcUtil.calCRC8PC(cmd);
        return cmd;
    }

}

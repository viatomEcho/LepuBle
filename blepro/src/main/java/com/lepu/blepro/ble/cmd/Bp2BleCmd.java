package com.lepu.blepro.ble.cmd;

import static com.lepu.blepro.ble.cmd.LpBleCmd.getReq;

/**
 * @author chenyongfeng
 */
public class Bp2BleCmd {

    public static final int RT_DATA = 0x08;
    public static final int RT_STATE = 0x06;
    public static final int SWITCH_STATE = 0x09;
    public static final int GET_PHY_STATE = 0x0E;
    public static final int SET_PHY_STATE = 0x0F;

    public static final int SET_CONFIG = 0x0B;
    public static final int GET_CONFIG = 0x00;

    // 定制BP2A_Sibel
    public static final int CMD_0X40 = 0x40;

    /**
     * 0：进入血压测量
     * 1：进入心电测量
     * 2：进入历史回顾
     * 3：进入开机预备状态
     * 4：关机
     * 5：进入理疗模式
     */
    public static class SwitchState {
        public static final int ENTER_BP = 0;
        public static final int ENTER_ECG = 1;
        public static final int ENTER_HISTORY = 2;
        public static final int ENTER_ON = 3;
        public static final int ENTER_OFF = 4;
        public static final int ENTER_PHY = 5;
    }

    public static byte[] switchState(int state, byte[] key) {
        return getReq(SWITCH_STATE, new byte[]{(byte)state}, key);
    }

    public static byte[] getConfig(byte[] key) {
        return getReq(GET_CONFIG, new byte[0], key);
    }

    public static byte[] setConfig(boolean switchState, int volume, byte[] key) {
        byte[] cmd = new byte[40];
        if(switchState) {
            cmd[24] = (byte) 0x01;
        }
        cmd[26] = (byte) volume;
        return getReq(SET_CONFIG, cmd, key);
    }

    public static byte[] setConfig(byte[] data, byte[] key) {
        return getReq(SET_CONFIG, data, key);
    }

    public static byte[] getRtState(byte[] key) {
        return getReq(RT_STATE, new byte[0], key);
    }
    public static byte[] getPhyState(byte[] key) {
        return getReq(GET_PHY_STATE, new byte[0], key);
    }
    public static byte[] setPhyState(byte[] data, byte[] key) {
        return getReq(SET_PHY_STATE, data, key);
    }
    public static byte[] getRtData(byte[] key) {
        return getReq(RT_DATA, new byte[0], key);
    }

    // BP2A_Sibel定制
    public static byte[] cmd0x40(boolean key, boolean measure, byte[] encrypt) {
        byte[] data = new byte[2];
        if (key) {
            data[0] = 1;
        }
        if (measure) {
            data[1] = 1;
        }
        return getReq(CMD_0X40, data, encrypt);
    }
}

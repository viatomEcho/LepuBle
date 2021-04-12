package com.lepu.blepro.ble.cmd;

import java.util.Arrays;

/**
 * author: wujuan
 * created on: 2021/3/31 10:21
 * description:
 */
public class PC100Cmd {

    public static final int MSG_TYPE_INVALID = -1;

    public final static byte HEAD_0 = (byte) 0xAA;
    public final static byte HEAD_1 = (byte) 0x55;

    // 基本控制
    public static final int CMD_TYPE_HAND_SHAKE = 0x01;
    public static final int CMD_TYPE_GET_DEVICE_INFO = 0x02;
    public static final int CMD_TYPE_GET_DEVICE_ID = 0x03;
    public static final int CMD_TYPE_SET_IDLE = 0x04;
    public static final int CMD_TYPE_SHUT_DOWN = 0x05;

    // 血压控制
    public static final int CMD_TYPE_BP_START = 0x11;
    public static final int CMD_TYPE_BP_END = 0x12;
    public static final int CMD_TYPE_BP_GET_RESULT = 0x13;
    public static final int CMD_TYPE_BP_GET_STATE = 0x14;
    public static final int CMD_TYPE_BP_CAL_MODE = 0x15; // 校准模式
    public static final int CMD_TYPE_BP_SET_CAL_DATA = 0x16; // 发送校准后参数
    public static final int CMD_TYPE_BP_RT_DATA = 0x17; // 发送校准后参数
    // 血氧控制
    public static final int CMD_TYPE_SPO2_START = 0x20;
    public static final int CMD_TYPE_SPO2_END = 0x21;
    public static final int CMD_TYPE_SPO2_GET_STATE = 0x22;
    public static final int CMD_TYPE_SPO2_WAVE_DATA = 0x23;
    public static final int CMD_TYPE_SPO2_RT_DATA = 0x24;
    public static final int CMD_TYPE_SPO2_FINGER_OUT = 0x25;

    // 基本控制
    public static final byte TOKEN_HAND_SHAKE = (byte) 0xFF;
    public static final byte TOKEN_GET_DEVICE_INFO = (byte) 0x51;
    public static final byte TOKEN_GET_DEVICE_ID = (byte) 0xFF;
    public static final byte TOKEN_SET_IDLE = (byte) 0x00;
    public static final byte TOKEN_SHUT_DOWN = (byte) 0xD0;
    // 血压控制
    public static final byte TOKEN_BP_START = (byte) 0x40;
    public static final byte TOKEN_BP_END = (byte) 0x40;
    public static final byte TOKEN_BP_GET_RESULT = (byte) 0x43;
    public static final byte TOKEN_BP_GET_STATE = (byte) 0x41;
    public static final byte TOKEN_BP_MODULE_STATE = (byte) 0x40;
    public static final byte TOKEN_BP_CAL_MODE = (byte) 0x40;
    public static final byte TOKEN_BP_SET_CAL_DATA = (byte) 0x44;
    public static final byte TOKEN_BP_RT_DATA = (byte) 0x42; // BP实时数据
    // 血氧控制
    public static final byte TOKEN_SPO2_START = (byte) 0x50;
    public static final byte TOKEN_SPO2_END = (byte) 0x50;
    public static final byte TOKEN_SPO2_GET_STATE = (byte) 0x54;
    public static final byte TOKEN_SPO2_RT_DATA = (byte) 0x53; // SPO2实时数据
    public static final byte TOKEN_SPO2_WAVE_DATA = (byte) 0x52; // SPO2实时数据

    public static byte[] getCmd(int msgType) {
        switch(msgType) {
            case CMD_TYPE_HAND_SHAKE:
                return handshake();
            case CMD_TYPE_GET_DEVICE_INFO:
                return getDeviceInfo();
            case CMD_TYPE_GET_DEVICE_ID:
                return getDeviceId();
            case CMD_TYPE_SET_IDLE:
                return setIdle();
            case CMD_TYPE_SHUT_DOWN:
                return shutdown();
            case CMD_TYPE_BP_START:
                return startBp();
            case CMD_TYPE_BP_END:
                return stopBp();
            case CMD_TYPE_BP_GET_RESULT:
                return getBpResult();
            case CMD_TYPE_BP_GET_STATE:
                return getBpState();
            case CMD_TYPE_SPO2_START:
                return startSpo2();
            case CMD_TYPE_SPO2_END:
                return stopSpo2();
            case CMD_TYPE_SPO2_GET_STATE:
                return getSpo2State();
            default:
                return new byte[0];
        }
    }

    public static int getMsgType(byte[] response) {
        PC100Response.PcBleResponse bleResponse = new PC100Response.PcBleResponse(response);

        switch(bleResponse.getToken()) {
            case TOKEN_HAND_SHAKE:
                if(bleResponse.getType() == (byte)0x01) {
                    return CMD_TYPE_HAND_SHAKE;
                } else if(bleResponse.getType() == (byte)0x10){
                    return CMD_TYPE_GET_DEVICE_ID;
                }
            case TOKEN_GET_DEVICE_INFO:
                if(bleResponse.getType() == (byte)0x01) {
                    return CMD_TYPE_GET_DEVICE_INFO;
                } else if(bleResponse.getType() == (byte)0x02){
                    return CMD_TYPE_SPO2_FINGER_OUT;
                }
            case TOKEN_SET_IDLE:
                return CMD_TYPE_SET_IDLE;
            case TOKEN_BP_MODULE_STATE:
                if(bleResponse.getType() == (byte)0x01) {
                    return CMD_TYPE_BP_START;
                } else if(bleResponse.getType() == (byte)0x02){
                    return CMD_TYPE_BP_END;
                }
            case TOKEN_BP_GET_STATE:
                return CMD_TYPE_BP_GET_STATE;
            case TOKEN_BP_RT_DATA:
                return CMD_TYPE_BP_RT_DATA;
            case TOKEN_BP_GET_RESULT:
                return CMD_TYPE_BP_GET_RESULT;
            case TOKEN_SPO2_START:
                if(bleResponse.getType() == (byte)0x01) {
                    return CMD_TYPE_SPO2_START;
                } else if(bleResponse.getType() == (byte)0x02){
                    return CMD_TYPE_SPO2_END;
                }
            case TOKEN_SPO2_WAVE_DATA:
                return CMD_TYPE_SPO2_WAVE_DATA;
            case TOKEN_SPO2_RT_DATA:
                return CMD_TYPE_SPO2_RT_DATA;
            case TOKEN_SPO2_GET_STATE:
                return CMD_TYPE_SPO2_GET_STATE;
        }
        return MSG_TYPE_INVALID;
    }

    private static byte[] handshake() {
        int len = 2;
        byte[] cmd = new byte[4 + len];

        cmd[0] = HEAD_0;
        cmd[1] = HEAD_1;
        cmd[2] = TOKEN_HAND_SHAKE;
        cmd[3] = (byte) len;
        cmd[4] = (byte) 0x01;
        // length
        cmd[5] = PC100CrcUtil.calCRC8ByCCITT(Arrays.copyOfRange(cmd, 0, cmd.length - 1));
        return cmd;
    }

    private static byte[] getDeviceInfo() {
//			int len = 2;
//			byte[] cmd = new byte[4 + len];
//
//			cmd[0] = HEAD_0;
//			cmd[1] = HEAD_1;
//			cmd[2] = TOKEN_GET_DEVICE_INFO;
//			cmd[3] = (byte) len;
//			cmd[4] = (byte) 0x01;
//
//			cmd[5] = PC100CrcUtil.calCRC8(Arrays.copyOfRange(cmd, 0, cmd.length - 1), Table_CRC8_CCITT);

        byte[] cmd =  new byte[]{-86, 85, 81, 2, 1, 0};
        cmd[cmd.length - 1] = getCRC(cmd, cmd.length);
        return cmd;
    }

    private static byte[] getDeviceId() {
        int len = 2;
        byte[] cmd = new byte[4 + len];

        cmd[0] = HEAD_0;
        cmd[1] = HEAD_1;
        cmd[2] = TOKEN_GET_DEVICE_ID;
        cmd[3] = (byte) len;
        cmd[4] = (byte) 0x10;
        // length
        cmd[5] = PC100CrcUtil.calCRC8ByCCITT(Arrays.copyOfRange(cmd, 0, cmd.length - 1));
        return cmd;
    }

    private static byte[] setIdle() {
        int len = 2;
        byte[] cmd = new byte[4 + len];

        cmd[0] = HEAD_0;
        cmd[1] = HEAD_1;
        cmd[2] = TOKEN_SET_IDLE;
        cmd[3] = (byte) len;
        cmd[4] = (byte) 0x00;
        // length
        cmd[5] = PC100CrcUtil.calCRC8ByCCITT(Arrays.copyOfRange(cmd, 0, cmd.length - 1));
        return cmd;
    }

    private static byte[] shutdown() {
        int len = 2;
        byte[] cmd = new byte[4 + len];

        cmd[0] = HEAD_0;
        cmd[1] = HEAD_1;
        cmd[2] = TOKEN_SHUT_DOWN;
        cmd[3] = (byte) len;
        cmd[4] = (byte) 0x01;
        // length
        cmd[5] = PC100CrcUtil.calCRC8ByCCITT(Arrays.copyOfRange(cmd, 0, cmd.length - 1));
        return cmd;
    }

    private static byte[] startBp() {
//			int len = 2;
//			byte[] cmd = new byte[4 + len];
//
//			cmd[0] = HEAD_0;
//			cmd[1] = HEAD_1;
//			cmd[2] = TOKEN_BP_START;
//			cmd[3] = (byte) len;
//			cmd[4] = (byte) 0x01;
//			cmd[5] = PC100CrcUtil.calCRC8ByCCITT(Arrays.copyOfRange(cmd, 0, cmd.length - 1));

        byte[] cmd = new byte[]{-86, 85, 64, 2, 1, 41};
        return cmd;
    }

    private static byte[] stopBp() {
//			int len = 2;
//			byte[] cmd = new byte[4 + len];
//
//			cmd[0] = HEAD_0;
//			cmd[1] = HEAD_1;
//			cmd[2] = TOKEN_BP_END;
//			cmd[3] = (byte) len;
//			cmd[4] = (byte) 0x02;
//			// length
//			cmd[5] = PC100CrcUtil.calCRC8ByCCITT(Arrays.copyOfRange(cmd, 0, cmd.length - 1));

        byte[] cmd = new byte[]{-86, 85, 64, 2, 2, 0};
        cmd[cmd.length - 1] = getCRC(cmd, cmd.length);
        return cmd;
    }

    private static byte[] getBpResult() {
        int len = 2;
        byte[] cmd = new byte[4 + len];

        cmd[0] = HEAD_0;
        cmd[1] = HEAD_1;
        cmd[2] = TOKEN_BP_GET_RESULT;
        cmd[3] = (byte) len;
        cmd[4] = (byte) 0x01;
        // length
        cmd[5] = PC100CrcUtil.calCRC8ByCCITT(Arrays.copyOfRange(cmd, 0, cmd.length - 1));


        return cmd;
    }

    private static byte[] getBpState() {
//			int len = 2;
//			byte[] cmd = new byte[4 + len];
//
//			cmd[0] = HEAD_0;
//			cmd[1] = HEAD_1;
//			cmd[2] = TOKEN_BP_GET_STATE;
//			cmd[3] = (byte) len;
//			cmd[4] = (byte) 0x01;
//			// length
//			cmd[5] = PC100CrcUtil.calCRC8ByCCITT(Arrays.copyOfRange(cmd, 0, cmd.length - 1));

        byte[] cmd = new byte[]{-86, 85, 65, 2, 1, 0};
        cmd[cmd.length - 1] = getCRC(cmd, cmd.length);
        return cmd;
    }

    private static byte[] startSpo2() {
        int len = 2;
        byte[] cmd = new byte[4 + len];

        cmd[0] = HEAD_0;
        cmd[1] = HEAD_1;
        cmd[2] = TOKEN_SPO2_START;
        cmd[3] = (byte) len;
        cmd[4] = (byte) 0x01;
        // length
        cmd[5] = PC100CrcUtil.calCRC8ByCCITT(Arrays.copyOfRange(cmd, 0, cmd.length - 1));
        return cmd;
    }

    private static byte[] stopSpo2() {
        int len = 2;
        byte[] cmd = new byte[4 + len];

        cmd[0] = HEAD_0;
        cmd[1] = HEAD_1;
        cmd[2] = TOKEN_SPO2_END;
        cmd[3] = (byte) len;
        cmd[4] = (byte) 0x02;
        // length
        cmd[5] = PC100CrcUtil.calCRC8ByCCITT(Arrays.copyOfRange(cmd, 0, cmd.length - 1));
        return cmd;
    }

    private static byte[] getSpo2State() {
//			int len = 2;
//			byte[] cmd = new byte[4 + len];
//
//			cmd[0] = HEAD_0;
//			cmd[1] = HEAD_1;
//			cmd[2] = TOKEN_SPO2_GET_STATE;
//			cmd[3] = (byte) len;
//			cmd[4] = (byte) 0x01;
//			// length
//			cmd[5] = PC100CrcUtil.calCRC8ByCCITT(Arrays.copyOfRange(cmd, 0, cmd.length - 1));
        byte[] cmd = new byte[]{-86, 85, 84, 2, 1, 0};
        cmd[cmd.length - 1] = getCRC(cmd, cmd.length);
        return cmd;
    }

    public static byte getCRC(byte[] srcCRC, int temp) {
        byte[] crc_table = new byte[]{0, 94, -68, -30, 97, 63, -35, -125, -62, -100, 126, 32, -93, -3, 31, 65, -99, -61, 33, 127, -4, -94, 64, 30, 95, 1, -29, -67, 62, 96, -126, -36, 35, 125, -97, -63, 66, 28, -2, -96, -31, -65, 93, 3, -128, -34, 60, 98, -66, -32, 2, 92, -33, -127, 99, 61, 124, 34, -64, -98, 29, 67, -95, -1, 70, 24, -6, -92, 39, 121, -101, -59, -124, -38, 56, 102, -27, -69, 89, 7, -37, -123, 103, 57, -70, -28, 6, 88, 25, 71, -91, -5, 120, 38, -60, -102, 101, 59, -39, -121, 4, 90, -72, -26, -89, -7, 27, 69, -58, -104, 122, 36, -8, -90, 68, 26, -103, -57, 37, 123, 58, 100, -122, -40, 91, 5, -25, -71, -116, -46, 48, 110, -19, -77, 81, 15, 78, 16, -14, -84, 47, 113, -109, -51, 17, 79, -83, -13, 112, 46, -52, -110, -45, -115, 111, 49, -78, -20, 14, 80, -81, -15, 19, 77, -50, -112, 114, 44, 109, 51, -47, -113, 12, 82, -80, -18, 50, 108, -114, -48, 83, 13, -17, -79, -16, -82, 76, 18, -111, -49, 45, 115, -54, -108, 118, 40, -85, -11, 23, 73, 8, 86, -76, -22, 105, 55, -43, -117, 87, 9, -21, -75, 54, 104, -118, -44, -107, -53, 41, 119, -12, -86, 72, 22, -23, -73, 85, 11, -120, -42, 52, 106, 43, 117, -105, -55, 74, 20, -10, -88, 116, 42, -56, -106, 21, 75, -87, -9, -74, -24, 10, 84, -41, -119, 107, 53};
        byte Old_CRC = 0;
        int i = 0;
        for(i = 0; i < temp - 1; ++i) {
            byte New_CRC = crc_table[(Old_CRC ^ srcCRC[i]) & 255];
            Old_CRC = New_CRC;
        }

        srcCRC[i] = Old_CRC;
        return srcCRC[i];
    }
}
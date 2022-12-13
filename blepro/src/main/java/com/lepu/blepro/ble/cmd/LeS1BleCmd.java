package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.ble.data.TimeData;
import com.lepu.blepro.utils.ByteArrayKt;
import com.lepu.blepro.utils.LepuBleLog;
import java.util.Date;

/**
 * universal command for Viatom devices
 * @author wujuan
 */
public class LeS1BleCmd {

    public static final int GET_INFO = 0xE1;
    public static final int RESET = 0xE2;
    public static final int FACTORY_RESET = 0xE3;
    public static final int RT_DATA = 0x03;
    public static final int READ_FILE_LIST = 0xF1;
    public static final int READ_FILE_START = 0xF2;
    public static final int READ_FILE_DATA = 0xF3;
    public static final int READ_FILE_END = 0xF4;
    public static final int SET_TIME = 0xEC;

    public static class RunStatus {
        public static final int IDLE = 0;
        public static final int WEIGHT_MEASURING = 1;
        public static final int WEIGHT_FINISH = 2;
        public static final int ECG_PREPARE = 3;
        public static final int ECG_MEASURING = 4;
        public static final int ECG_FINISH = 5;
        public static final int ECG_ERROR_RV = 6;
        public static final int ECG_ERROR = 7;
    }
    public static class BleStatus {
        public static final int DISCONNECTED = 0;
        public static final int CONNECTED = 1;
        public static final int LOADING = 2;
    }
    public static class Unit {
        public static final int KG = 0;
        public static final int LB = 1;
        public static final int ST = 2;
        public static final int LB_ST = 3;
        public static final int JIN = 4;
    }

    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }


    public static byte[] setTime() {

        TimeData timeData = new TimeData(new Date());
        byte[] data = timeData.convert2Data();
        int cmdLength = 8 + data.length;

        byte[] cmd = new byte[cmdLength];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) SET_TIME;
        cmd[2] = (byte) ~SET_TIME;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) (data.length & 0xFF);
        cmd[6] = (byte) ((data.length >> 8) & 0xFF);

        for(int i = 0; i < data.length; i++) {
            cmd[i + 7] = data[i];
        }
        cmd[cmdLength - 1] = BleCRC.calCRC8(cmd);
        addNo();

        return cmd;
    }

    public static byte[] getRtData() {
        int len = 1;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) RT_DATA;
        cmd[2] = (byte) ~RT_DATA;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x01;
        cmd[6] = (byte) 0x00;
        // 0 -> 125hz;  1-> 62.5hz
        cmd[7] = (byte) 0x7D;
        cmd[8] = BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
    }

    public static byte[] getInfo() {
        int len = 0;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) GET_INFO;
        cmd[2] = (byte) ~GET_INFO;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0;
        cmd[6] = (byte) 0;
        cmd[7] = BleCRC.calCRC8(cmd);

        addNo();

        return cmd;
    }

    public static byte[] reset() {
        int len = 0;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) RESET;
        cmd[2] = (byte) ~RESET;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = BleCRC.calCRC8(cmd);
        addNo();

        LepuBleLog.d(ByteArrayKt.bytesToHex(cmd));
        return cmd;
    }

    public static byte[] factoryReset() {
        int len = 0;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) FACTORY_RESET;
        cmd[2] = (byte) ~FACTORY_RESET;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = BleCRC.calCRC8(cmd);
        addNo();

        LepuBleLog.d(ByteArrayKt.bytesToHex(cmd));
        return cmd;
    }

    public static byte[] getFileList() {
        int len = 0;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) READ_FILE_LIST;
        cmd[2] = (byte) ~READ_FILE_LIST;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = BleCRC.calCRC8(cmd);
        addNo();

        LepuBleLog.d(ByteArrayKt.bytesToHex(cmd));
        return cmd;
    }

    public static byte[] readFileStart(byte[] name,int offset) {
        int len = 20;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) READ_FILE_START;
        cmd[2] = (byte) ~READ_FILE_START;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x14;
        cmd[6] = (byte) 0x00;

        int l = Math.min(name.length, 16);
        System.arraycopy(name, 0, cmd, 7, l);

        cmd[23] = (byte) (offset);
        cmd[24] = (byte) (offset >> 8);
        cmd[25] = (byte) (offset >> 16);
        cmd[26] = (byte) (offset >> 24);
        cmd[27] = BleCRC.calCRC8(cmd);
        addNo();

        LepuBleLog.d(ByteArrayKt.bytesToHex(cmd));
        return cmd;
    }

    public static byte[] readFileData(int offset) {
        int len = 4;
        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) READ_FILE_DATA;
        cmd[2] = (byte) ~READ_FILE_DATA;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x04;
        cmd[6] = (byte) 0x00;
        int k;
        cmd[7] = (byte) (offset);
        cmd[8] = (byte) (offset >> 8);
        cmd[9] = (byte) (offset >> 16);
        cmd[10] = (byte) (offset >> 24);

        cmd[11] = BleCRC.calCRC8(cmd);
        addNo();
        LepuBleLog.d(ByteArrayKt.bytesToHex(cmd));
        return cmd;
    }
    public static byte[] readFileEnd() {
        int len = 0;
        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) READ_FILE_END;
        cmd[2] = (byte) ~READ_FILE_END;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = BleCRC.calCRC8(cmd);
        addNo();

        LepuBleLog.d(ByteArrayKt.bytesToHex(cmd));
        return cmd;
    }

}

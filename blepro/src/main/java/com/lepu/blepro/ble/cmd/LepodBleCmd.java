package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.ble.data.TimeData;
import com.lepu.blepro.utils.ByteArrayKt;
import com.lepu.blepro.utils.LepuBleLog;
import java.util.Date;

public class LepodBleCmd {

    public static final int GET_INFO = 0xE1;
    public static final int RESET = 0xE2;
    public static final int FACTORY_RESET = 0xE3;
    public static final int FACTORY_RESET_ALL = 0xEE;
    public static final int BURN_FACTORY_INFO = 0xEA;
    public static final int BURN_LOCK_FLASH = 0xEB;
    public static final int RT_PARAM = 0x02;
    public static final int RT_DATA = 0x03;
    public static final int GET_CONFIG = 0x00;
    public static final int SET_CONFIG = 0x04;
    public static final int STOP_ECG = 0x07;
    public static final int START_ECG = 0x08;
    public static final int READ_FILE_LIST = 0xF1;
    public static final int READ_FILE_START = 0xF2;
    public static final int READ_FILE_DATA = 0xF3;
    public static final int READ_FILE_END = 0xF4;
    public static final int SET_TIME = 0xEC;

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

    public static byte[] startEcg() {
        byte[] cmd = new byte[8];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) START_ECG;
        cmd[2] = (byte) ~START_ECG;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
    }
    public static byte[] stopEcg() {
        byte[] cmd = new byte[8];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) STOP_ECG;
        cmd[2] = (byte) ~STOP_ECG;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
    }

    /**
     * len不为0时：第一个字节：bit0~bit3:采样率0::250Hz 1:125Hz 2:62.5Hz
     *                      bit4~bit7: 压缩类型 0:未压缩 1:Viatom差分压缩
     *            预留三个字节
     *            四个字节：采样点偏移，用于预留协议补点上传，目前设备端不支持
     * @return
     */
    public static byte[] getRtData() {
        byte[] cmd = new byte[8];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) RT_DATA;
        cmd[2] = (byte) ~RT_DATA;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
    }

    public static byte[] getRtParam() {
        byte[] cmd = new byte[8];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) RT_PARAM;
        cmd[2] = (byte) ~RT_PARAM;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
    }

    public static byte[] getInfo() {
        byte[] cmd = new byte[8];
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

    public static byte[] setConfig(int mode) {
        int len = 1;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) SET_CONFIG;
        cmd[2] = (byte) ~SET_CONFIG;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) len;
        cmd[6] = (byte) 0x00;
        cmd[7] = (byte) mode;

        cmd[8] = BleCRC.calCRC8(cmd);
        addNo();

        LepuBleLog.d(ByteArrayKt.bytesToHex(cmd));
        return cmd;
    }


    public static byte[] reset() {
        byte[] cmd = new byte[8];
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

    public static byte[] factoryResetAll() {
        byte[] cmd = new byte[8];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) FACTORY_RESET_ALL;
        cmd[2] = (byte) ~FACTORY_RESET_ALL;
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
        byte[] cmd = new byte[8];
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

    public static byte[] getConfig() {
        byte[] cmd = new byte[8];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) GET_CONFIG;
        cmd[2] = (byte) ~GET_CONFIG;
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
        byte[] cmd = new byte[8];
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
        cmd[5] = (byte) len;
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
        cmd[5] = (byte) len;
        cmd[6] = (byte) 0x00;
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
        byte[] cmd = new byte[8];
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
    public static byte[] burnFactoryInfo(byte[] config) {
        int len = config.length;
        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) BURN_FACTORY_INFO;
        cmd[2] = (byte) ~BURN_FACTORY_INFO;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) (len & 0xFF);
        cmd[6] = (byte) ((len >> 8) & 0xFF);

        for(int i = 0; i < len; i++) {
            cmd[i + 7] = config[i];
        }
        cmd[cmd.length - 1] = BleCRC.calCRC8(cmd);
        addNo();

        LepuBleLog.d(ByteArrayKt.bytesToHex(cmd));
        return cmd;
    }
    public static byte[] burnLockFlash() {
        byte[] cmd = new byte[8];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) BURN_LOCK_FLASH;
        cmd[2] = (byte) ~BURN_LOCK_FLASH;
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

package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.ble.data.TimeData;
import java.util.Date;

/**
 * @author wujuan
 */
public class Er2BleCmd {

    public static final int COMMON_PKG_HEAD_LENGTH = 7;
    public static final int MIN_PKG_LENGTH = COMMON_PKG_HEAD_LENGTH + 1;

    public static final byte CMD_RETRIEVE_SWITCHER_STATE = 0x00;
    public static final byte CMD_SET_SWITCHER_STATE = 0x04;
    public static final byte CMD_RETRIEVE_DEVICE_INFO = (byte) 0xE1;
    public static final byte CMD_LOCK_FLASH = (byte) 0xEB;
    public static final byte CMD_BURN_SN_CODE = (byte) 0xEA;
    public static final byte CMD_RESET = (byte) 0xE2;
    public static final byte CMD_FACTORY_RESET_ALL = (byte) 0xEE;
    public static final byte CMD_FACTORY_RESET = (byte) 0xE3;
    public static final byte CMD_GET_BATTERY = (byte) 0xE4;
    public static final byte CMD_SET_TIME = (byte) 0xEC;
    public static final byte CMD_LIST_FILE = (byte) 0xF1;
    public static final byte CMD_GET_REAL_TIME_DATA = (byte) 0x03;

    public static final byte CMD_START_READ_FILE = (byte) 0xF2;
    public static final byte CMD_READ_FILE_CONTENT = (byte) 0xF3;
    public static final byte CMD_END_READ_FILE = (byte) 0xF4;



    public static int VECTOR = 1;
    public static int MOTION_COUNT= 1;
    public static int MOTION_WINDOWS = 1;

    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    public static byte[] setSwitcherState(boolean switchState) {
        addNo();
        SwitcherConfig config = new SwitcherConfig(switchState, VECTOR, MOTION_COUNT, MOTION_WINDOWS);

        return getReq(CMD_SET_SWITCHER_STATE, (byte)seqNo, config.convert2Data());
    }

    public static byte[] getSwitcherState() {
        addNo();
        return getReq(CMD_RETRIEVE_SWITCHER_STATE, (byte)seqNo, new byte[0]);
    }

    public static byte[] getDeviceInfo() {
        addNo();
        return getReq(CMD_RETRIEVE_DEVICE_INFO, (byte)seqNo, new byte[0]);
    }

    public static byte[] setTime() {
        TimeData timeData = new TimeData(new Date());
        addNo();
        return getReq(CMD_SET_TIME, (byte)seqNo, timeData.convert2Data());
    }

    public static byte[] listFiles() {
        addNo();
        return getReq(CMD_LIST_FILE, (byte)seqNo, new byte[0]);
    }

    public static byte[] factoryReset() {
        addNo();
        return getReq(CMD_FACTORY_RESET, (byte)seqNo, new byte[0]);
    }

    public static byte[] factoryResetAll() {
        addNo();
        return getReq(CMD_FACTORY_RESET_ALL, (byte)seqNo, new byte[0]);
    }

    public static byte[] reset() {
        addNo();
        return getReq(CMD_RESET, (byte)seqNo, new byte[0]);
    }

    public static byte[] getRtData() {
        addNo();
        return getReq(CMD_GET_REAL_TIME_DATA, (byte)seqNo, new byte[0]);
    }

    public static byte[] readFileStart(byte[] fileName,int offset) {
        byte[] data = new byte[16 + 4];
        for(int i = 0; i < 16; i++) {
            if(i < fileName.length) {
                data[i] = (byte) fileName[i];
            } else {
                data[i] = (byte) 0x00;
            }
        }

        data[data.length - 4] = (byte) (offset & 0xFF);
        data[data.length - 3] = (byte) ((offset >> 8) & 0xFF);
        data[data.length - 2] = (byte) ((offset >> 16) & 0xFF);
        data[data.length - 1] = (byte) ((offset >> 24) & 0xFF);
        return getReq(CMD_START_READ_FILE, (byte) 0x00, data);
    }

    public static byte[] readFileData(int offset) {
        byte[] offsetData = new byte[4];
        offsetData[0] = (byte) (offset & 0xFF);
        offsetData[1] = (byte) ((offset >> 8) & 0xFF);
        offsetData[2] = (byte) ((offset >> 16) & 0xFF);
        offsetData[3] = (byte) ((offset >> 24) & 0xFF);

        return getReq(CMD_READ_FILE_CONTENT, (byte) 0x00, offsetData);
    }

    public static byte[] readFileEnd() {
        return getReq(CMD_END_READ_FILE, (byte) 0x00, new byte[0]);
    }

    private static byte[] getReq(byte cmd,  byte pkgNo, byte[] data) {
        Er2RequestPkg requestPkg = new Er2RequestPkg();
        requestPkg.setCmd(cmd)
                .setPkgNo(pkgNo)
                .setData(data)
                .build();
        return requestPkg.getBuf();
    }
}

package com.lepu.blepro.ble.cmd.er2;

import com.lepu.blepro.ble.data.TimeData;

import java.util.Date;

public class Er2BleCmd {

    public final static int COMMON_PKG_HEAD_LENGTH = 7;
    public final static int MIN_PKG_LENGTH = COMMON_PKG_HEAD_LENGTH + 1;

    public final static byte CMD_RETRIEVE_SWITCHER_STATE = 0x00;
    public final static byte CMD_SET_SWITCHER_STATE = 0x04;
    public final static byte CMD_RETRIEVE_DEVICE_INFO = (byte) 0xE1;
    public final static byte CMD_LOCK_FLASH = (byte) 0xEB;
    public final static byte CMD_BURN_SN_CODE = (byte) 0xEA;
    public final static byte CMD_FACTORY_RESET_ALL = (byte) 0xEE;
    public final static byte CMD_FACTORY_RESET = (byte) 0xE3;
    public final static byte CMD_GET_BATTERY = (byte) 0xE4;
    public final static byte CMD_SET_TIME = (byte) 0xEC;
    public final static byte CMD_LIST_FILE = (byte) 0xF1;
    public final static byte CMD_GET_REAL_TIME_DATA = (byte) 0x03;

    public final static byte CMD_START_READ_FILE = (byte) 0xF2;
    public final static byte CMD_READ_FILE_CONTENT = (byte) 0xF3;
    public final static byte CMD_END_READ_FILE = (byte) 0xF4;



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
        Er2SwitcherConfig config = new Er2SwitcherConfig(switchState, VECTOR, MOTION_COUNT, MOTION_WINDOWS);

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

    public static byte[] resetAll() {
        addNo();
        return getReq(CMD_FACTORY_RESET_ALL, (byte)seqNo, new byte[0]);
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
        return getReq(CMD_START_READ_FILE, (byte) 0x00, new byte[0]);
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

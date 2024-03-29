package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.utils.CrcUtil;

import java.util.Calendar;

/**
 * @author chenyongfeng
 */
public class Bpw1BleCmd {

    public static int mCurrentCmd;

    // request
    // cmd
    public static final int MEASURE_REQUEST = 0x00;
    public static final int GET_FILE_LIST = 0x01;
    public static final int CLEAR_FILE_LIST = 0x02;
    public static final int GET_MEASURE_TIME = 0x03;
    public static final int GET_DEVICE_INFO = 0x04;
    public static final int FACTORY_RESET = 0x13;
    public static final int SET_TIME = 0x20;
    public static final int SET_MEASURE_TIME = 0x21;
    public static final int SET_TIMING_SWITCH = 0x22;


    // response
    // len
    public static final int UNIVERSAL_RESPONSE_LEN = 0x02;
    public static final int BP_DATA_LEN = 0x0A;
    public static final int MEASURE_TIME_OR_DEVICE_INFO_LEN = 0x06;

    // cmd
    public static final int RT_DATA = 0x00;
    public static final int UNIVERSAL_RESPONSE = 0x40;
    public static final int LOW_BATTERY = 0x50;
    public static final int HISTORY_FILE_NUM = 0x60;
    public static final int MEASURE_RESPONSE = 0x51;
    public static final int ERROR_RESULT = 0x52;


    public static byte[] startBp() {
        mCurrentCmd = MEASURE_REQUEST;
        byte[] cmd = new byte[6];
        cmd[0] = (byte) 0x5A;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0x02;
        cmd[3] = (byte) MEASURE_REQUEST;
        cmd[4] = (byte) 0x00;
        cmd[5] = CrcUtil.calBpw1CHK(cmd);
        return cmd;
    }
    public static byte[] stopBp() {
        mCurrentCmd = MEASURE_REQUEST;
        byte[] cmd = new byte[6];
        cmd[0] = (byte) 0x5A;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0x02;
        cmd[3] = (byte) MEASURE_REQUEST;
        cmd[4] = (byte) 0x01;
        cmd[5] = CrcUtil.calBpw1CHK(cmd);
        return cmd;
    }
    public static byte[] getFileList() {
        mCurrentCmd = GET_FILE_LIST;
        byte[] cmd = new byte[6];
        cmd[0] = (byte) 0x5A;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0x02;
        cmd[3] = (byte) GET_FILE_LIST;
        cmd[4] = (byte) 0x00;
        cmd[5] = CrcUtil.calBpw1CHK(cmd);
        return cmd;
    }
    public static byte[] clearFileList() {
        mCurrentCmd = CLEAR_FILE_LIST;
        byte[] cmd = new byte[6];
        cmd[0] = (byte) 0x5A;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0x02;
        cmd[3] = (byte) CLEAR_FILE_LIST;
        cmd[4] = (byte) 0x00;
        cmd[5] = CrcUtil.calBpw1CHK(cmd);
        return cmd;
    }
    public static byte[] getMeasureTime() {
        mCurrentCmd = GET_MEASURE_TIME;
        byte[] cmd = new byte[6];
        cmd[0] = (byte) 0x5A;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0x02;
        cmd[3] = (byte) GET_MEASURE_TIME;
        cmd[4] = (byte) 0x01;
        cmd[5] = CrcUtil.calBpw1CHK(cmd);
        return cmd;
    }
    public static byte[] getDeviceInfo() {
        mCurrentCmd = GET_DEVICE_INFO;
        byte[] cmd = new byte[6];
        cmd[0] = (byte) 0x5A;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0x02;
        cmd[3] = (byte) GET_DEVICE_INFO;
        cmd[4] = (byte) 0x00;
        cmd[5] = CrcUtil.calBpw1CHK(cmd);
        return cmd;
    }
    public static byte[] factoryReset() {
        mCurrentCmd = FACTORY_RESET;
        byte[] cmd = new byte[6];
        cmd[0] = (byte) 0x5A;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0x02;
        cmd[3] = (byte) FACTORY_RESET;
        cmd[4] = (byte) 0xAA;
        cmd[5] = CrcUtil.calBpw1CHK(cmd);
        return cmd;
    }

    public static byte[] setTime() {
        mCurrentCmd = SET_TIME;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        byte[] cmd = new byte[11];
        cmd[0] = (byte) 0x5A;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0x07;
        cmd[3] = (byte) SET_TIME;
        cmd[4] = (byte) Integer.valueOf(String.valueOf(calendar.get(Calendar.YEAR)).substring(2,4)).intValue();
        cmd[5] = (byte) (calendar.get(Calendar.MONTH) + 1);
        cmd[6] = (byte) calendar.get(Calendar.DATE);
        cmd[7] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        cmd[8] = (byte) calendar.get(Calendar.MINUTE);
        cmd[9] = (byte) calendar.get(Calendar.SECOND);
        cmd[10] = CrcUtil.calBpw1CHK(cmd);
        return cmd;
    }

    public static byte[] setMeasureTime(int startHH, int startMM, int stopHH, int stopMM, int interval, int serialNum, int totalCount) {
        mCurrentCmd = SET_MEASURE_TIME;
        byte[] cmd = new byte[11];
        cmd[0] = (byte) 0x5A;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0x07;
        cmd[3] = (byte) SET_MEASURE_TIME;
        cmd[4] = (byte) startHH;
        cmd[5] = (byte) startMM;
        cmd[6] = (byte) stopHH;
        cmd[7] = (byte) stopMM;
        cmd[8] = (byte) (serialNum << 4 | totalCount);
        cmd[9] = (byte) interval;
        cmd[10] = CrcUtil.calBpw1CHK(cmd);
        return cmd;
    }

    public static byte[] setTimingSwitch(boolean timingSwitch) {
        mCurrentCmd = SET_TIMING_SWITCH;
        byte[] cmd = new byte[11];
        cmd[0] = (byte) 0x5A;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0x07;
        cmd[3] = (byte) SET_TIMING_SWITCH;
        cmd[4] = (byte) 0x00;
        if (timingSwitch) {
            cmd[4] = (byte) 0x01;
        }
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = (byte) 0x00;
        cmd[8] = (byte) 0x00;
        cmd[9] = (byte) 0x00;
        cmd[10] = CrcUtil.calBpw1CHK(cmd);
        return cmd;
    }

}

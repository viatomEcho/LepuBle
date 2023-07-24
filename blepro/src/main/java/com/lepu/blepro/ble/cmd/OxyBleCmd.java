package com.lepu.blepro.ble.cmd;

import com.lepu.blepro.utils.LepuBleLog;
import org.json.JSONException;
import org.json.JSONObject;
import static com.lepu.blepro.utils.StringUtilsKt.makeTimeStr;

/**
 * @author wujuan
 */
public class OxyBleCmd {

    public static int OXY_CMD_INFO = 0x14;
    public static int OXY_CMD_PARA_SYNC = 0x16;
    // 有pi
    public static int OXY_CMD_RT_PARAM = 0x17;
    public static int OXY_CMD_FACTORY_RESET = 0x18;
    public static int OXY_CMD_BURN_LOCK_FLASH = 0x19;
    public static int OXY_CMD_BURN_FACTORY_INFO = 0x1A;
    // 1.4.1固件版本前没有pi
    public static int OXY_CMD_RT_WAVE = 0x1B;
    public static int OXY_CMD_READ_START = 0x03;
    public static int OXY_CMD_READ_CONTENT = 0x04;
    public static int OXY_CMD_READ_END = 0x05;
    public static int OXY_CMD_PPG_RT_DATA = 0x1C;
    public static int OXY_CMD_BOX_INFO = 0x1D;


    /*************************参数同步相关**************************************/
    /**
     * SetTIME : 设置时间
     * SetOxiThr : 设置血氧阈值
     *
     * SetOxiSwitch : 设置血氧开关
     * 设备支持声音和震动提醒：bit0:震动  bit1:声音 (int 0：震动关声音关 1：震动开声音关 2：震动关声音开 3：震动开声音开)
     * 设备只支持声音或震动提醒：bit0:震动/声音 (int 0：震动/声音关 1：震动/声音开)
     *
     * SetMotor : 设置强度（KidsO2、Oxylink：最低：5，低：10，中：17，高：22，最高：35；O2Ring：最低：20，低：40，中：60，高：80，最高：100，震动强度不随开关的改变而改变）
     * SetPedtar : 设置计步器目标提醒步数
     * SetLightingMode : 设置亮屏模式（0：Standard模式，1：Always Off模式，2：Always On模式）
     *
     * SetHRSwitch : 设置心率开关
     * 设备支持声音和震动提醒：bit0:震动  bit1:声音 (int 0：震动关声音关 1：震动开声音关 2：震动关声音开 3：震动开声音开)
     * 设备只支持声音或震动提醒：bit0:震动/声音 (int 0：震动/声音关 1：震动/声音开)
     *
     * SetHRLowThr : 设置心率震动最低阈值（30-250）
     * SetHRHighThr : 设置心率震动最高阈值（30-250）
     * SetLightStr : 设置屏幕亮度（0：低，1：中，2：高）
     * SetSpO2SW : 设置血氧功能开关（0：关 1：开）
     * SetBuzzer : 设置声音强度（checkO2Plus：最低：20，低：40，中：60，高：80，最高：100）
     * SetMtSW : 设置体动报警开关（0：关 1：开）
     * SetMtThr : 设置体动报警阈值
     * SetIvSW : 设置无效值报警开关（0：关 1：开）
     * SetIvThr	: 设置无效值报警告警时间阈值（30s - 300s，每间隔30s）
     */
    public static final String SYNC_TYPE_TIME = "SetTIME";
    public static final String SYNC_TYPE_OXI_THR = "SetOxiThr";
    public static final String SYNC_TYPE_OXI_SWITCH = "SetOxiSwitch";
    public static final String SYNC_TYPE_MOTOR = "SetMotor";
    public static final String SYNC_TYPE_PEDTAR = "SetPedtar";
    public static final String SYNC_TYPE_LIGHTING_MODE = "SetLightingMode";
    public static final String SYNC_TYPE_HR_SWITCH = "SetHRSwitch";
    public static final String SYNC_TYPE_HR_LOW_THR = "SetHRLowThr";
    public static final String SYNC_TYPE_HR_HIGH_THR = "SetHRHighThr";
    public static final String SYNC_TYPE_LIGHT_STR = "SetLightStr";
    public static final String SYNC_TYPE_SPO2SW = "SetSpO2SW";
    public static final String SYNC_TYPE_BUZZER = "SetBuzzer";
    public static final String SYNC_TYPE_MT_SW = "SetMtSW";
    public static final String SYNC_TYPE_MT_THR = "SetMtThr";
    public static final String SYNC_TYPE_IV_SW = "SetIvSW";
    public static final String SYNC_TYPE_IV_THR = "SetIvThr";
    /*************************参数同步相关**************************************/
    public static final String SYNC_TYPE_ALL_SW = "AllSW";


    /**
     * O2 系列不使用SeqNo, 仅在下载数据时作为数据偏移
     */
    public static int seqNo = 0;

    private static void addNo() {
        seqNo++;
        if (seqNo >= 65535) {
            seqNo = 0;
        }
    }

    public static byte[] getBoxInfo() {
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_BOX_INFO;
        buf[2] = (byte) ~OXY_CMD_BOX_INFO;

        buf[7] = BleCRC.calCRC8(buf);
        seqNo = 0;

        return buf;
    }

    public static byte[] getInfo() {
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_INFO;
        buf[2] = (byte) ~OXY_CMD_INFO;

        buf[7] = BleCRC.calCRC8(buf);
        seqNo = 0;

        return buf;
    }

    public static byte[] syncTime() {
        JSONObject j = new JSONObject();
        try {
            j.put(SYNC_TYPE_TIME, makeTimeStr());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sync(j);
    }

    public static byte[] updateSetting(String type, int value) {
        JSONObject j = new JSONObject();
        try {
            LepuBleLog.d("syncData type="+type+", value="+value);
            j.put(type, value+"");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sync(j);

    }
    public static byte[] updateSetting(String[] type, int[] value) {
        JSONObject j = new JSONObject();
        try {
            for (int i=0; i<type.length; i++) {
                LepuBleLog.d("syncData type=" + type + "value=" + value);
                j.put(type[i], value[i] + "");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sync(j);

    }
    private static byte[] sync(JSONObject j) {
        char[] chars = j.toString().toCharArray();
        int size = chars.length;
        byte[] buf = new byte[8 + size];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_PARA_SYNC;
        buf[2] = (byte) ~OXY_CMD_PARA_SYNC;
        buf[5] = (byte) size;
        buf[6] = (byte) (size >> 8);

        for (int i = 0; i < size; i++) {
            buf[7 + i] = (byte) chars[i];
        }

        buf[8 + size - 1] = BleCRC.calCRC8(buf);
        seqNo = 0;

        return buf;
    }

    public static byte[] getRtWave() {
        int len = 1;

        byte[] buf = new byte[8 + len];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_RT_WAVE;
        buf[2] = (byte) ~OXY_CMD_RT_WAVE;
        buf[5] = (byte) len;
        buf[6] = (byte) (len >> 8);
        // 0 -> 125hz;  1-> 62.5hz
        buf[7] = (byte) 0;

        buf[8] = BleCRC.calCRC8(buf);
        seqNo = 0;

        return buf;

    }

    public static byte[] getRtParam() {
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_RT_PARAM;
        buf[2] = (byte) ~OXY_CMD_RT_PARAM;

        buf[7] = BleCRC.calCRC8(buf);
        seqNo = 0;

        return buf;
    }


    public static byte[] getPpgRt() {
        int len = 1;

        byte[] buf = new byte[8 + len];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_PPG_RT_DATA;
        buf[2] = (byte) ~OXY_CMD_PPG_RT_DATA;
        buf[5] = (byte) len;
        buf[6] = (byte) (len >> 8);
        buf[7] = (byte) 0X00;

        buf[8] = BleCRC.calCRC8(buf);
        seqNo = 0;
        return buf;

    }

    public static byte[] readFileStart(String fileName) {
        char[] name = fileName.toCharArray();
        int len = name.length + 1;
        // filename最后一位补0
        byte[] buf = new byte[8 + len];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_READ_START;
        buf[2] = (byte) ~OXY_CMD_READ_START;
        buf[5] = (byte) len;
        buf[6] = (byte) (len >> 8);

        for (int i = 0; i < len - 1; i++) {
            buf[7 + i] = (byte) name[i];
        }

        buf[buf.length - 1] = BleCRC.calCRC8(buf);

        seqNo = 0;

        return buf;
    }

    public static byte[] readFileContent() {
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_READ_CONTENT;
        buf[2] = (byte) ~OXY_CMD_READ_CONTENT;
        buf[3] = (byte) seqNo;
        buf[4] = (byte) (seqNo >> 8);

        buf[7] = BleCRC.calCRC8(buf);

        addNo();

        return buf;
    }

    public static byte[] readFileEnd() {
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_READ_END;
        buf[2] = (byte) ~OXY_CMD_READ_END;

        buf[7] = BleCRC.calCRC8(buf);

        seqNo = 0;

        return buf;
    }

    public static byte[] factoryReset() {
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_FACTORY_RESET;
        buf[2] = (byte) ~OXY_CMD_FACTORY_RESET;

        buf[7] = BleCRC.calCRC8(buf);
        seqNo = 0;

        return buf;
    }

    public static byte[] burnFactoryInfo(byte[] data) {
        int len = data.length;
        byte[] buf = new byte[8+len];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_BURN_FACTORY_INFO;
        buf[2] = (byte) ~OXY_CMD_BURN_FACTORY_INFO;
        buf[5] = (byte) len;
        buf[6] = (byte) (len >> 8);

        System.arraycopy(data, 0, buf, 7, len);

        buf[buf.length-1] = BleCRC.calCRC8(buf);
        seqNo = 0;

        return buf;
    }


}

package com.lepu.blepro.utils;

public class Er1Decompress {

    public static final byte COM_MAX_VAL = 127;
    public static final byte COM_MIN_VAL = -127;
    public static final short COM_EXTEND_MAX_VAL = 382;
    public static final short COM_EXTEND_MIN_VAL = -382;

    public static final byte COM_RET_ORIGINAL = -128;
    public static final byte COM_RET_POSITIVE = 127;
    public static final byte COM_RET_NEGATIVE = -127;

    public static final short UNCOM_RET_INVALI = -32768;

    public byte unCompressNum;
    public int lastCompressData;

    public Er1Decompress() {
        this.unCompressNum = 0;
        this.lastCompressData = 0;
    }

    public byte getUnCompressNum() {
        return unCompressNum;
    }

    public int getLastCompressData() {
        return lastCompressData;
    }

    public short unCompressAlgECG(byte compressData) {
        int ecgData = 0;
        //标志位
        switch (unCompressNum) {
            case 0:
                if (compressData == COM_RET_ORIGINAL) {
                    unCompressNum = 1;
                    ecgData = UNCOM_RET_INVALI;
                } else if (compressData == COM_RET_POSITIVE) {        //正
                    unCompressNum = 3;
                    ecgData = UNCOM_RET_INVALI;
                } else if (compressData == COM_RET_NEGATIVE) {        //负
                    unCompressNum = 4;
                    ecgData = UNCOM_RET_INVALI;
                } else {
                    ecgData = lastCompressData + compressData;
                    lastCompressData = ecgData;
                }
                break;
            case 1:            // 原始数据字节低位
//                lastCompressData = compressData & 0xFFFF;
                lastCompressData = compressData & 0xFF;
                unCompressNum = 2;
                ecgData = UNCOM_RET_INVALI;
                break;
            case 2:            //原始数据字节高位
                ecgData = lastCompressData + (compressData << 8);
                lastCompressData = ecgData;
                unCompressNum = 0;
                break;
            case 3:
                ecgData = COM_MAX_VAL + (lastCompressData + (compressData & 0xFF));
                lastCompressData = ecgData;
                unCompressNum = 0;
                break;
            case 4:
                ecgData = COM_MIN_VAL + (lastCompressData - (compressData & 0xFF));
                lastCompressData = ecgData;
                unCompressNum = 0;
                break;
            default:
                break;
        }
        return (short) ecgData;
    }

    public static short[] unCompressAlgECG(byte[] tmpDataArray) {
        short[] tmps = new short[tmpDataArray.length];
        int len = 0;
        Er1Decompress convert = new Er1Decompress();
        for (int i = 0; i < tmpDataArray.length; i++) {
            short tmp = convert.unCompressAlgECG(tmpDataArray[i]);
            if (tmp != -32768) {
                tmps[len] = tmp == 32767 ? 0 : tmp;
                len++;
            }
        }
        short[] shortData = new short[len];
        System.arraycopy(tmps, 0, shortData, 0, shortData.length);
        return shortData;
    }

}

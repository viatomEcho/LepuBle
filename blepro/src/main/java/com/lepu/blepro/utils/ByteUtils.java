package com.lepu.blepro.utils;

public class ByteUtils {


    public static float bytesToFloat(byte[] b) {
        int accum = 0;
        accum = accum|(b[0] & 0xff) << 0;
        accum = accum|(b[1] & 0xff) << 8;
        accum = accum|(b[2] & 0xff) << 16;
        accum = accum|(b[3] & 0xff) << 24;
        return Float.intBitsToFloat(accum);
    }

    /**
     * 转无符号整数
     * @param b
     * @return int
     */
    public static int byte2UInt(byte b) {
        return b & 0xff;
    }

    /**
     * 转有符号整数（小端模式）
     * @param b1
     * @param b2
     * @return short
     */
    public static short toSignedShort(byte b1, byte b2) {
        return (short) ((b1 & 0xff) + ((b2 & 0xff) << 8));
    }

    /**
     * 转无符号整数（大端模式）
     * @param b1
     * @param b2
     * @return
     */
    public static int bytes2UIntBig(byte b1, byte b2) {
        return (((b1 & 0xff) << 8) + (b2 & 0xff));
    }

    public static int bytes2UIntBig(byte b1, byte b2, byte b3, byte b4) {
        return (((b1 & 0xff) << 24) + ((b2 & 0xff) << 16) + ((b3 & 0xff) << 8) + (b4 & 0xff));
    }

    public static float[] bytes2mvs(byte[] bytes) {
        if (bytes == null || bytes.length <2) {
            return null;
        }
        int len = bytes.length/2;
        float[] mvs = new float[len];
        for (int i=0; i<len; i++) {
            mvs[i] = byteTomV(bytes[2*i], bytes[2*i+1]);
        }

        return mvs;
    }

    private static float byteTomV(byte a, byte b) {
        if (a == (byte) 0xff && b == (byte) 0x7f) {
            return 0f;
        }

        int n = ((a & 0xFF) | (short) (b  << 8));

//        float mv = (float) (n*12.7*1800*1.03)/(10*227*4096);
//        float mv = (float) ( n * (1.0035 * 1800) / (4096 * 178.74));
        float mv = (float) (n * 0.00309);

        return mv;
    }

    public static int[] bytes2ints(byte[] bytes) {
        if (bytes == null || bytes.length <2) {
            return null;
        }
        int len = bytes.length/2;
        int[] ints = new int[len];
        for (int i=0; i<len; i++) {
            ints[i] = ((bytes[2*i] & 0xFF) | (short) (bytes[2*i+1]  << 8));
        }

        return ints;
    }
}

package com.lepu.blepro.utils;

public class ByteUtils {

    public static int byte2UInt(byte b) {
        return b & 0xff;
    }

    public static short toSignedShort(byte b1, byte b2) {
        return (short) ((b1 & 0xff) + ((b2 & 0xff) << 8));
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
        if (a == (byte) 0xff && b == (byte) 0x7f)
            return 0f;

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

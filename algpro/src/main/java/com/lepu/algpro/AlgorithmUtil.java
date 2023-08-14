package com.lepu.algpro;

/**
 * @author chenyongfeng
 */
public class AlgorithmUtil {

    // 滤波
    static {
        System.loadLibrary("online-lib");
        System.loadLibrary("offline-lib");
        // 睡眠算法
        System.loadLibrary("sleep-alg");
    }

    public static native double[] filter(double f, boolean reset);

    public static native short[] shortFilter(short[] shorts);

    // reset filter
    public static void resetFilter() {
        filter(0.0d, true);
    }

    // sleep alg
    public static native void sleepAlgInit(int timestamp);
    // 脉率，三轴值
    public static native int sleepAlgMainPro(short pr, int acc);
    public static native void sleepAlgMain(int[] pr, int[] acc);
    public static native int[] sleepAlgGetResult();

}

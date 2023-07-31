package com.lepu.blepro.utils;

/**
 * @author chenyongfeng
 */
public class AlgorithmUtil {

    // 滤波
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("offline-lib");
        // 睡眠算法
        System.loadLibrary("sleep-alg");
    }

    public static native double[] filter(double f, boolean reset);

    public static native short[] shortfilter(short[] shorts);

    // reset filter
    public static void resetFilter() {
        filter(0.0d, true);
    }

    // sleep alg
    public static native void sleep_alg_init_0_25Hz(int timestamp);
    // 脉率，三轴值
    public static native int sleep_alg_main_pro_0_25Hz(short pr, int acc);
    public static native void sleep_alg_main(int[] pr, int[] acc);
    public static native int[] sleep_alg_get_res_0_25Hz();

}

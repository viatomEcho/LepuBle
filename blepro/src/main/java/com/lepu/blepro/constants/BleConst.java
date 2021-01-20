package com.lepu.blepro.constants;


public class BleConst {
    /**
     * 蓝牙连接状态
     */
    public interface DeviceState{
        //不明
        int UNKNOWN = -1;

        //已连接
        int CONNECTED = 1;

        // 未连接
        int DISCONNECTED = 2;

        //未绑定
        int UNBOUND = 3;

        //连接中
        int CONNECTING = 4;

    }


}
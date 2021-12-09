package com.lepu.blepro.ble.cmd;

/**
 * universal command for Viatom devices
 */
public class F5ScaleBleCmd {

    // response
    public final static int WEIGHT_DATA = 0xD5;
    public final static int IMPEDANCE_DATA = 0xD6;
    public final static int UNSTABLE_DATA = 0xD0;
    public final static int OTHER_DATA = 0xD7;
    public final static int HISTORY_DATA = 0xD8;

    // request
    public final static int SET_PARAM = 0xD0;
    public final static int SET_USER_LIST = 0xD1;


}

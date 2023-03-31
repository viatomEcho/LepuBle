package com.lepu.blepro.ble.cmd;

/**
 * @author chenyongfeng
 */
public class R20BleCmd {

    private static final int TYPE_NORMAL_SEND = 0x00;
    private static final int HEAD = 0xA5;

    public static final int DEVICE_BOUND = 0x01;
    public static final int DEVICE_UNBOUND = 0x02;
    public static final int SET_USER_INFO = 0x03;
    public static final int GET_USER_INFO = 0x04;
    public static final int DOCTOR_MODE = 0x05;
    public static final int GET_WIFI_LIST = 0x11;
    public static final int SET_WIFI_CONFIG = 0x12;
    public static final int GET_WIFI_CONFIG = 0x13;
    public static final int GET_VERSION_INFO = 0x15;
    public static final int GET_SYSTEM_SETTING = 0x16;
    public static final int SET_SYSTEM_SETTING = 0x17;
    public static final int GET_MEASURE_SETTING = 0x18;
    public static final int SET_MEASURE_SETTING = 0x19;
    public static final int MASK_TEST = 0x1A;
    public static final int GET_VENTILATION_SETTING = 0x20;
    public static final int SET_VENTILATION_SETTING = 0x21;
    public static final int GET_WARNING_SETTING = 0x22;
    public static final int SET_WARNING_SETTING = 0x23;
    public static final int VENTILATION_SWITCH = 0x24;
    public static final int GET_FILE_LIST = 0x31;
    public static final int READ_FILE_START = 0x32;
    public static final int READ_FILE_DATA = 0x33;
    public static final int READ_FILE_END = 0x34;
    public static final int RT_STATE = 0x35;
    public static final int RT_PARAM = 0x36;
    public static final int EVENT = 0x37;

    public static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    private static byte[] getReq(int sendCmd, byte[] data) {
        int len = data.length;
        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) HEAD;
        cmd[1] = (byte) sendCmd;
        cmd[2] = (byte) ~sendCmd;
        cmd[3] = (byte) TYPE_NORMAL_SEND;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) len;
        cmd[6] = (byte) (len << 8);
        System.arraycopy(data, 0, cmd, 7, len);
        cmd[cmd.length-1] = BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
    }

    public static byte[] deviceBound(boolean bound) {
        if (bound) {
            return getReq(DEVICE_BOUND, new byte[0]);
        } else {
            return getReq(DEVICE_UNBOUND, new byte[0]);
        }
    }
    public static byte[] setUserInfo(byte[] data) {
        return getReq(SET_USER_INFO, data);
    }
    public static byte[] getUserInfo() {
        return getReq(GET_USER_INFO, new byte[0]);
    }
    public static byte[] doctorMode(byte[] pin, long timestamp) {
        byte[] data = new byte[10];
        System.arraycopy(pin, 0, data, 0, pin.length);
        data[6] = (byte) timestamp;
        data[7] = (byte) (timestamp >> 8);
        data[8] = (byte) (timestamp >> 16);
        data[9] = (byte) (timestamp >> 24);
        return getReq(DOCTOR_MODE, data);
    }
    public static byte[] getWifiList(int deviceNum) {
        return getReq(GET_WIFI_LIST, new byte[]{(byte)deviceNum});
    }
    public static byte[] setWifiConfig(byte[] config) {
        return getReq(SET_WIFI_CONFIG, config);
    }
    public static byte[] getWifiConfig(int option) {
        return getReq(GET_WIFI_CONFIG, new byte[]{(byte)option});
    }
    public static byte[] getVersionInfo() {
        return getReq(GET_VERSION_INFO, new byte[0]);
    }
    public static byte[] getSystemSetting() {
        return getReq(GET_SYSTEM_SETTING, new byte[0]);
    }
    public static byte[] setSystemSetting(byte[] data) {
        return getReq(SET_SYSTEM_SETTING, data);
    }
    public static byte[] getMeasureSetting() {
        return getReq(GET_MEASURE_SETTING, new byte[0]);
    }
    public static byte[] setMeasureSetting(byte[] data) {
        return getReq(SET_MEASURE_SETTING, data);
    }
    public static byte[] maskTest(boolean start) {
        int temp = 0;
        if (start) {
            temp = 1;
        }
        return getReq(MASK_TEST, new byte[]{(byte)temp,0,0,0});
    }
    public static byte[] getVentilationSetting() {
        return getReq(GET_VENTILATION_SETTING, new byte[0]);
    }
    public static byte[] setVentilationSetting(byte[] data) {
        return getReq(SET_VENTILATION_SETTING, data);
    }
    public static byte[] getWarningSetting() {
        return getReq(GET_WARNING_SETTING, new byte[0]);
    }
    public static byte[] setWarningSetting(byte[] data) {
        return getReq(SET_WARNING_SETTING, data);
    }
    public static byte[] ventilationSwitch(boolean start) {
        int temp = 0;
        if (start) {
            temp = 1;
        }
        return getReq(VENTILATION_SWITCH, new byte[]{(byte)temp,0,0,0});
    }
    public static byte[] getFileList(long startTime, int recordType) {
        byte[] data = new byte[10];
        data[0] = (byte) startTime;
        data[1] = (byte) (startTime >> 8);
        data[2] = (byte) (startTime >> 16);
        data[3] = (byte) (startTime >> 24);
        data[4] = (byte) recordType;
        return getReq(GET_FILE_LIST, data);
    }
    public static byte[] readFileStart(byte[] fileName, int offset) {
        byte[] data = new byte[36];
        System.arraycopy(fileName, 0, data, 0, fileName.length);
        data[32] = (byte) offset;
        data[33] = (byte) (offset >> 8);
        data[34] = (byte) (offset >> 16);
        data[35] = (byte) (offset >> 24);
        return getReq(READ_FILE_START, data);
    }
    public static byte[] readFileData(int offset) {
        byte[] data = new byte[4];
        data[0] = (byte) offset;
        data[1] = (byte) (offset >> 8);
        data[2] = (byte) (offset >> 16);
        data[3] = (byte) (offset >> 24);
        return getReq(READ_FILE_DATA, data);
    }
    public static byte[] readFileEnd() {
        return getReq(READ_FILE_END, new byte[0]);
    }
    public static byte[] getRtState() {
        return getReq(RT_STATE, new byte[0]);
    }
    public static byte[] getRtParam() {
        return getReq(RT_PARAM, new byte[0]);
    }

    public static class AlarmLevel {
        public static final int NONE = 0;       // 正常
        public static final int LOW = 0;        // 低,提示
        public static final int MIDDLE = 0;     // 中
        public static final int HIGH = 0;       // 高
        public static final int VERY_HIGH = 0;  // 最高,系统故障
    }

    public static class EventId {
        public static final int LINE_DISCONNECT = 1;             // 管路断开
        public static final int LINE_CLOG = 2;                   // 管路阻塞
        public static final int HIGH_AIR_LEAKAGE = 3;            // 漏气量高
        public static final int HIGH_RR = 4;                     // 呼吸频率高
        public static final int LOW_RR = 5;                      // 呼吸频率低
        public static final int LOW_TIDAL_VOLUME = 6;            // 潮气量低
        public static final int MINUTES_VTL_LOW = 7;             // 分钟通气量低
        public static final int HYPOXIC_SATURATION = 8;          // 血氧饱和度低
        public static final int HR_PR_HIGH = 9;                  // 心率/脉率高
        public static final int HR_PR_LOW = 10;                  // 心率/脉率低
        public static final int APNEA = 11;                      // 窒息
        public static final int POWER_DOWN = 12;                 // 掉电

        //涡轮
        public static final int TURBINE_NOT_CONNECT = 101;       // 涡轮HALL线没接
        public static final int TURBINE_TEMP_ERROR = 102;        // 涡轮温度超过90度
        public static final int TURBINE_LOCKED_ROTOR = 103;      // 涡轮堵转
        public static final int TURBINE_POWER_ERROR = 104;       // 涡轮电源异常

        //加热盘
        public static final int HEATING_ERROR = 201;             // 加热盘异常
        public static final int HEATING_NOT_CONNECT = 202;       // 加热盘没接/温度传感器损坏
        public static final int HEATING_TEMP_ERROR = 203;        // 加热盘温度超过75度

        //流量
        public static final int FLOW_SENSOR_ERROR = 301;         // 流量传感器异常
        public static final int PRESSURE_SENSOR_ERROR = 302;     // 压力传感器异常
        public static final int FLOW_COMM_ERROR = 303;           // 流量传感器通信异常
        public static final int VTL_FLOW_TOO_LARGE = 304;        // 流量传感器测得的流速过大
        public static final int VTL_FLOW_TOO_SMALL = 305;        // 流量传感器测得的流速过小
        public static final int VTL_PRESSURE_TOO_LARGE = 306;    // 正常通气时，压力传感器数值过大
        public static final int VTL_PRESSURE_TOO_SMALL = 307;    // 正常通气时，压力传感器数值过小
        public static final int LOW_SENSOR_PRESSURE = 308;       // 传感器压力偏低
        public static final int TEST_FLOW_TOO_LARGE = 309;       // 自检时，流量传感器流速过大
        public static final int TEST_FLOW_TOO_SMALL = 310;       // 自检时，流量传感器流速过小
        public static final int TEST_FLOW_COMM_ERROR = 311;      // 自检时，流量传感器通信异常
        public static final int TEST_PRESSURE_TOO_LARGE = 312;   // 自检时，压力传感器数值过大
        public static final int TEST_PRESSURE_TOO_SMALL = 313;   // 自检时，压力传感器数值过小
        public static final int TEMP_HUMI_ERROR = 314;           // 温/湿度传感器异常
        public static final int ATMOSPHERIC_SENSOR_ERROR = 315;  // 大气压传感器异常
        public static final int EST_PRESSURE_ERROR = 316;        // 预估压力与实际压力相差较远

        //电压
        public static final int PWR_VOLTAGE_ERROR = 401;         // 输入电压异常
        public static final int PWR_VOLTAGE_TOO_SMALL = 402;     // 电源电压低
        public static final int PWR_VOLTAGE_TOO_LARGE = 403;     // 电源电压高

        //其他
        public static final int EEPROM_ERROR = 501;              // EEPROM 只读数据异常
        public static final int RTC_ERROR = 502;                 // RTC时钟异常
        public static final int NEED_TO_CALIBRATE = 503;         // 设备需要校准
        public static final int ABNORMAL_REBOOT = 504;           // 设备异常重启

        //事件
        public static final int EVENT_OA = 601;                  // 阻塞呼吸暂事件
        public static final int EVENT_CA = 602;                  // 中枢型呼吸暂停事件
        public static final int EVENT_UA = 603;                  // 无法分类的呼吸暂停事件
        public static final int EVENT_H = 604;                   // 低通气
        public static final int EVENT_RERA = 605;                // 微觉醒
        public static final int EVENT_SNORING = 606;             // 打鼾事件
        public static final int EVENT_PB = 607;                  // 周期性呼吸事件
        public static final int EVENT_LL = 608;                  // 漏气量高事件
        public static final int EVENT_DROPOFF = 609;             // 面罩摘下
        public static final int EVENT_SPONT = 610;               // 自主呼吸占比
    }
}

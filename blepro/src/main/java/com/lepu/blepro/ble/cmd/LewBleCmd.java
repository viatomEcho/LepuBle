package com.lepu.blepro.ble.cmd;

import android.util.Log;

import com.lepu.blepro.ble.data.lew.TimeData;
import com.lepu.blepro.utils.CrcUtil;
import static com.lepu.blepro.utils.ByteUtils.int4Bytes;
import static com.lepu.blepro.utils.HexString.bytesToHex;

/**
 * @author chenyongfeng
 */
public class LewBleCmd {

    private static final int HEAD = 0xA5;
    private static final int TYPE_NORMAL_SEND = 0x00;

    /**
     * public
     */
    public static final int ECHO = 0xE0;
    public static final int GET_INFO = 0xE1;
    public static final int RESET = 0xE2;
    public static final int FACTORY_RESET = 0xE3;
    public static final int GET_BATTERY = 0xE4;
    public static final int FW_UPDATE_START = 0xE5;
    public static final int FW_UPDATE_DATA = 0xE6;
    public static final int FW_UPDATE_END = 0xE7;
    public static final int BURN_FACTORY_INFO = 0xEA;
//    public static final int SET_TIME = 0xEC;
    public static final int FACTORY_RESET_ALL = 0xEE;
    public static final int GET_FILE_LIST = 0xF1;
    public static final int READ_FILE_START = 0xF2;
    public static final int READ_FILE_DATA = 0xF3;
    public static final int READ_FILE_END = 0xF4;
    public static final int DELETE_FILE = 0xF8;
    public static final int SHUTDOWN = 0xFC;

    /**
     * private
     */
    public static final int BOUND_DEVICE = 0x01;
    public static final int UNBOUND_DEVICE = 0x02;
    public static final int FIND_DEVICE = 0x03;

    public static final int GET_SYSTEM_SETTING = 0x10;
    public static final int SET_SYSTEM_SETTING = 0x11;
    public static final int LANGUAGE_SETTING = 0x12;
    public static final int UNIT_SETTING = 0x13;
    public static final int HAND_RAISE_SETTING = 0x14;
    public static final int LR_HAND_SETTING = 0x15;

    public static final int NO_DISTURB_MODE = 0x16;
    public static final int APP_SWITCH = 0x17;
    public static final int NOTIFICATION_INFO = 0x18;
    public static final int DEVICE_MODE = 0x19;
    public static final int ALARM_CLOCK_INFO = 0x21;
    public static final int PHONE_SWITCH = 0x22;
    public static final int CALL_CONTROL = 0x23;
    public static final int MEDICINE_REMIND = 0x24;

    public static final int GET_MEASURE_SETTING = 0x30;
    public static final int SET_MEASURE_SETTING = 0x31;
    public static final int SPORT_TARGET = 0x32;
    public static final int TARGET_REMIND = 0x33;
    public static final int SITTING_REMIND = 0x34;
    public static final int HR_DETECT = 0x35;
    public static final int OXY_DETECT = 0x36;

    public static final int USER_INFO = 0x40;
    public static final int PHONE_BOOK = 0x41;
    public static final int SOS_CONTACT = 0x42;
    public static final int SET_TIME = 0x44;
    public static final int CLOCK_DIAL = 0x45;
    public static final int SECOND_SCREEN = 0x46;
    public static final int CARDS = 0x47;

    public static final int GET_SPORT_LIST = 0x50;
    public static final int GET_SLEEP_LIST = 0x52;
    public static final int GET_ECG_LIST = 0x60;
    public static final int GET_HR_LIST = 0x61;
    public static final int GET_OXY_LIST = 0x63;
    public static final int HR_THRESHOLD = 0x70;
    public static final int OXY_THRESHOLD = 0x71;

    public static final int RT_DATA = 0x80;

    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    public static byte[] boundDevice() {
        return getReq(BOUND_DEVICE, new byte[0]);
    }
    public static byte[] unBoundDevice() { return getReq(UNBOUND_DEVICE, new byte[0]); }
    public static byte[] findDevice() { return getReq(FIND_DEVICE, new byte[0]); }

    public static byte[] getSystemSetting() { return getReq(GET_SYSTEM_SETTING, new byte[0]); }
    public static byte[] setSystemSetting(byte[] setting) { return getReq(SET_SYSTEM_SETTING, setting); }
    public static byte[] getLanguageSetting() { return getReq(LANGUAGE_SETTING, new byte[0]); }
    public static class Language {
        public static final int ENGLISH = 0;       // 英语
        public static final int CHINESE = 1;       // 中文
        public static final int SPANISH = 2;       // 西班牙语
        public static final int ITALIAN = 3;       // 意大利语
        public static final int PORTUGUESE = 4;    // 葡萄牙语
        public static final int FRENCH = 5;        // 法语
        public static final int JAPANESE = 6;      // 日语
        public static final int RUSSIAN = 7;       // 俄语
        public static final int KOREAN = 8;        // 韩语
        public static final int GERMAN = 9;        // 德语
        public static final int CHINESE_RTW = 10;  // 繁体中文
        public static final int ARABIC = 11;       // 阿拉伯语
        public static final int INDONESIAN = 12;   // 印尼语
        public static final int TURKISH = 13;      // 土耳其语
        public static final int UKRAINIAN = 14;    // 乌克兰语
        public static final int HEBREW = 15;       // 希伯来语
        public static final int CZECH = 16;        // 捷克语
        public static final int GREEK = 17;        // 希腊语
        public static final int VIETNAMESE = 18;   // 越南语
        public static final int POLISH = 19;       // 波兰语
        public static final int DUTCH = 20;        // 荷兰语
        public static final int LATIN = 21;        // 拉丁语
        public static final int ROMANIAN = 22;     // 马来西亚语
        public static final int THAI = 23;         // 泰语
        public static final int DANISH = 24;       // 丹麦语
        public static final int FARSI = 25;        // 波斯语
    }
    public static byte[] setLanguageSetting(int num) { return getReq(LANGUAGE_SETTING, new byte[]{(byte)num}); }
    public static byte[] getUnitSetting() { return getReq(UNIT_SETTING, new byte[0]); }
    public static class Unit {
        // ?????
        public static final int LENGTH_KM_M = 0;
        public static final int LENGTH_FEET_INCH = 1;
        public static final int WEIGHT_KG_G = 0;
        public static final int WEIGHT_POUND = 1;
        public static final int WEIGHT_QUARTZ = 2;
        public static final int TEMP_C = 0;
        public static final int TEMP_F = 1;
    }
    public static byte[] setUnitSetting(byte[] units) { return getReq(UNIT_SETTING, units); }
    public static byte[] getHandRaiseSetting() { return getReq(HAND_RAISE_SETTING, new byte[0]); }
    public static byte[] setHandRaiseSetting(byte[] setting) { return getReq(HAND_RAISE_SETTING, setting); }
    public static byte[] getLrHandSetting() { return getReq(LR_HAND_SETTING, new byte[0]); }
    public static class Hand {
        public static final int LEFT = 0;
        public static final int RIGHT = 1;
    }
    public static byte[] setLrHandSetting(int hand) { return getReq(LR_HAND_SETTING, new byte[]{(byte)hand}); }

    public static byte[] getNoDisturbMode() { return getReq(NO_DISTURB_MODE, new byte[0]); }
    public static byte[] setNoDisturbMode(byte[] mode) { return getReq(NO_DISTURB_MODE, mode); }

    public static class AppId {
        public static final int PHONE = 0;    // 暂不用
        public static final int MESSAGE = 1;  // 暂不用
        public static final int QQ = 2;
        public static final int WECHAT = 3;
        public static final int EMAIL = 4;
        public static final int FACEBOOK = 5;
        public static final int TWITTER = 6;
        public static final int WHATSAPP = 7;
        public static final int INSTAGRAM = 8;
        public static final int SKYPE = 9;
        public static final int LINKED_IN = 10;    // 领英
        public static final int LINE = 11;
        public static final int WEIBO = 12;        // 微博
        public static final int LEPU_HEALTH = 13;  // 乐普健康
        public static final int DING_TALK = 14;    // 钉钉
        public static final int WECOM = 15;        // 企业微信
        public static final int FEISHU = 16;       // 飞书

        public static final int OTHER = 31;
    }
    public static class PhoneStatus {
        public static final int TO_BE_ANSWERED = 0;  // 待接听
        public static final int HUNG_UP = 1;         // 已挂断
        public static final int NOT_ANSWERED = 2;    // 未接听
    }
    public static byte[] getAppSwitch() { return getReq(APP_SWITCH, new byte[0]); }
    public static byte[] setAppSwitch(byte[] switches) { return getReq(APP_SWITCH, switches); }
    public static byte[] notificationInfo(byte[] info) { return getReq(NOTIFICATION_INFO, info); }
    public static byte[] getDeviceMode() { return getReq(DEVICE_MODE, new byte[0]); }
    public static class DeviceMode {
        public static final int MODE_NORMAL = 0;   // 普通模式
        public static final int MODE_MONITOR = 1;  // 监护模式
        public static final int MODE_FREE = 2;     // 省心模式
    }
    public static byte[] setDeviceMode(int mode) { return getReq(DEVICE_MODE, new byte[]{(byte)mode}); }
    public static byte[] getAlarmClock() { return getReq(ALARM_CLOCK_INFO, new byte[0]); }
    public static byte[] setAlarmClock(byte[] alarm) { return getReq(ALARM_CLOCK_INFO, alarm); }
    public static byte[] getPhoneSwitch() { return getReq(PHONE_SWITCH, new byte[0]); }
    public static byte[] setPhoneSwitch(byte[] switches) { return getReq(PHONE_SWITCH, switches); }
    public static byte[] getMedicineRemind() { return getReq(MEDICINE_REMIND, new byte[0]); }
    public static byte[] setMedicineRemind(byte[] remind) { return getReq(MEDICINE_REMIND, remind); }

    public static byte[] getMeasureSetting() { return getReq(GET_MEASURE_SETTING, new byte[0]); }
    public static byte[] setMeasureSetting(byte[] setting) { return getReq(SET_MEASURE_SETTING, setting); }
    public static byte[] getSportTarget() { return getReq(SPORT_TARGET, new byte[0]); }
    public static byte[] setSportTarget(byte[] target) { return getReq(SPORT_TARGET, target); }
    public static byte[] getTargetRemind() { return getReq(TARGET_REMIND, new byte[0]); }
    public static byte[] setTargetRemind(boolean remind) {
        if (remind) {
            return getReq(TARGET_REMIND, new byte[]{1});
        } else {
            return getReq(TARGET_REMIND, new byte[]{0});
        }
    }
    public static byte[] getSittingRemind() { return getReq(SITTING_REMIND, new byte[0]); }
    public static byte[] setSittingRemind(byte[] remind) { return getReq(SITTING_REMIND, remind); }
    public static byte[] getHrDetect() { return getReq(HR_DETECT, new byte[0]); }
    public static byte[] setHrDetect(byte[] detect) { return getReq(HR_DETECT, detect); }
    public static byte[] getOxyDetect() { return getReq(OXY_DETECT, new byte[0]); }
    public static byte[] setOxyDetect(byte[] detect) { return getReq(OXY_DETECT, detect); }
    public static byte[] getUserInfo() { return getReq(USER_INFO, new byte[0]); }
    public static class Gender {
        public static final int BOY = 0;   // 男
        public static final int GIRL = 1;  // 女
    }
    public static byte[] setUserInfo(byte[] info) { return getReq(USER_INFO, info); }
    public static byte[] getPhoneBook() { return getReq(PHONE_BOOK, new byte[0]); }
    public static byte[] setPhoneBook(byte[] book) { return getReq(PHONE_BOOK, book); }
    public static byte[] getSosContact() { return getReq(SOS_CONTACT, new byte[0]); }
    public static class RelationShip {
        public static final int FATHER = 0;       // 爸爸
        public static final int MOTHER = 1;       // 妈妈
        public static final int GRANDFATHER = 2;  // 公公
        public static final int GRANDMOTHER = 3;  // 婆婆
        public static final int GRANDPA = 4;      // 爷爷
        public static final int GRANDMA = 5;      // 奶奶
        public static final int SON = 6;          // 儿子
        public static final int DAUGHTER = 7;     // 女儿
        public static final int OTHER = 8;        // 其他
    }
    public static byte[] setSosContact(byte[] sos) { return getReq(SOS_CONTACT, sos); }
    public static class TimeFormat {
        public static final int FORMAT_12H = 0;  // 12进制
        public static final int FORMAT_24H = 1;  // 24进制
        public static final int MONTH_DAY = 0;   // 月-日
        public static final int DAY_MONTH = 1;   // 日-月
    }
    public static byte[] setTime() {
        TimeData data = new TimeData();
        Log.d("test12345", "setTime = " + bytesToHex(data.getDataBytes()));
        return getReq(SET_TIME, data.getDataBytes());
    }
    public static byte[] setTime(byte[] data) {
        return getReq(SET_TIME, data);
    }
    public static byte[] getTime() {
        return getReq(SET_TIME, new byte[0]);
    }
    public static byte[] setDialNum(int num) { return getReq(CLOCK_DIAL, new byte[]{1, (byte)num}); }
    public static byte[] getSecondScreen() { return getReq(SECOND_SCREEN, new byte[0]); }
    public static byte[] setSecondScreen(byte[] screen) { return getReq(SECOND_SCREEN, screen); }
    public static byte[] getCards() { return getReq(CARDS, new byte[0]); }
    public static class Cards {
        public static final int TARGET = 0;   // 活动目标
        public static final int HR = 1;       // 心率
        public static final int WEATHER = 2;  // 天气
        public static final int ALIPAY = 3;   // 支付宝
    }
    public static byte[] setCards(int[] cards) {
        byte[] data = new byte[cards.length];
        for (int i=0; i<data.length; i++) {
            data[i] = (byte) cards[i];
        }
        return getReq(CARDS, data);
    }

    public static class ListType {
        public static final int SPORT = 0;   // 运动数据
        public static final int ECG = 1;     // 心电数据
        public static final int HR = 2;      // 心率数据
        public static final int OXY = 3;     // 血氧数据
        public static final int SLEEP = 4;   // 睡眠数据
    }
    public static class SportType {
        public static final int NULL = 0;
        public static final int BREATHE = 1;                  // 呼吸
        public static final int CYCLING = 2;                  // 骑车或室外骑车
        public static final int CYCLING_INDOOR = 3;           // 室内骑车
        public static final int RUNNING_MACHINE = 4;          // 跑步机或室内跑步
        public static final int RUN = 5;                      // 跑步或室外跑步
        public static final int SWIM = 6;                     // 游泳
        public static final int WALK = 7;                     // 走路
        public static final int WEIGHT = 8;                   // 举重
        public static final int YOGA = 9;                     // 瑜伽
        public static final int BADMINTON = 10;               // 羽毛球
        public static final int BASKETBALL = 11;              // 篮球
        public static final int SKIP = 12;                    // 跳绳
        public static final int FREE_EXERCISE = 13;           // 自由锻炼
        public static final int FOOTBALL = 14;                // 足球
        public static final int CLIMBING = 15;                // 爬山
        public static final int PINGPONG = 16;                // 乒乓球
        public static final int BOWLING = 17;                 // 保龄球
        public static final int OPENWATER = 18;               // 开放水域
        public static final int DANCING = 19;                 // 跳舞
        public static final int DUMBBELLS = 20;               // 哑铃
        public static final int HULOHOOP = 21;                // 呼啦圈
        public static final int STAIRSMOVE = 22;              // 楼梯运动
        public static final int STEPPER = 23;                 // 踏步机
        public static final int TRIATHLON = 24;               // 铁人三项
        public static final int SITUPS = 25;                  // 仰卧起坐
        public static final int SKI = 26;                     // 滑雪
        public static final int BILLIARDS = 27;               // 台球
        public static final int ELLIPTICAL_MACHINE = 28;      // 椭圆机
        public static final int TRAIL_RUNNING = 29;           // 越野跑
        public static final int AEROBICS = 30;                // 健美操
        public static final int PILATES = 31;                 // 普拉提
        public static final int SHUTTLECOCK = 32;             // 踢毽子
        public static final int SPIN = 33;                    // 旋转
        public static final int WALKING_MACHINE = 34;         // 走步机
    }
    public static byte[] getSportList(int time) { return getReq(GET_SPORT_LIST, int4Bytes(time)); }
    public static class SleepType {
        public static final int SOBER = 0;        // 清醒
        public static final int REM = 1;          // REM，快速眼动
        public static final int LIGHT_SLEEP = 2;  // 浅睡
        public static final int DEEP_SLEEP = 3;   // 深睡
    }
    public static byte[] getSleepList(int time) { return getReq(GET_SLEEP_LIST, int4Bytes(time)); }
    public static byte[] getEcgList(int time) { return getReq(GET_ECG_LIST, int4Bytes(time)); }
    public static byte[] getHrList(int time) { return getReq(GET_HR_LIST, int4Bytes(time)); }
    public static byte[] getOxyList(int time) { return getReq(GET_OXY_LIST, int4Bytes(time)); }
    public static byte[] getHrThreshold() { return getReq(HR_THRESHOLD, new byte[0]); }
    public static byte[] setHrThreshold(byte[] threshold) { return getReq(HR_THRESHOLD, threshold); }
    public static byte[] getOxyThreshold() { return getReq(OXY_THRESHOLD, new byte[0]); }
    public static byte[] setOxyThreshold(byte[] threshold) { return getReq(OXY_THRESHOLD, threshold); }

    public static class ModuleType {
        public static final int WATCH_ECG = 0;          // 设备自身ECG
        public static final int WATCH_OXY = 1;          // 设备自身血氧
        public static final int WATCH_TEMP = 2;         // 设备自身体温
        public static final int MODULE_ER1 = 10;        // er1
        public static final int MODULE_SPO2 = 11;       // 血氧
        public static final int MODULE_TEMP = 12;       // 体温
        public static final int MODULE_CGM = 13;        // 血糖
        public static final int MODULE_BP_HOLTER = 14;  // BP holter
    }
    public static byte[] getRtData() {
        return getReq(RT_DATA, new byte[0]);
    }

    public static class BatteryState {
        public static final int NORMAL = 0;       // 正常使用
        public static final int CHARGING = 1;     // 充电中
        public static final int CHARGED = 2;      // 充满
        public static final int LOW_BATTERY = 3;  // 低电量
    }
    public static byte[] getBattery() {
        return getReq(GET_BATTERY, new byte[0]);
    }

    public static byte[] getDeviceInfo() {
        return getReq(GET_INFO, new byte[0]);
    }

    public static byte[] listFiles() {
        return getReq(GET_FILE_LIST, new byte[0]);
    }

    public static byte[] deleteFile(byte[] fileName) {
        return getReq(DELETE_FILE, fileName);
    }

    public static byte[] factoryReset() {
        return getReq(FACTORY_RESET, new byte[0]);
    }

    public static byte[] factoryResetAll() {
        return getReq(FACTORY_RESET_ALL, new byte[0]);
    }

    public static byte[] reset() {
        return getReq(RESET, new byte[0]);
    }

    public static byte[] readFileStart(byte[] fileName, int offset) {
        byte[] data = new byte[fileName.length + 4];

        System.arraycopy(fileName, 0, data, 0, fileName.length);

        data[data.length - 4] = (byte) (offset & 0xFF);
        data[data.length - 3] = (byte) ((offset >> 8) & 0xFF);
        data[data.length - 2] = (byte) ((offset >> 16) & 0xFF);
        data[data.length - 1] = (byte) ((offset >> 24) & 0xFF);
        return getReq(READ_FILE_START, data);
    }

    public static byte[] readFileData(int offset) {
        byte[] offsetData = new byte[4];
        offsetData[0] = (byte) (offset & 0xFF);
        offsetData[1] = (byte) ((offset >> 8) & 0xFF);
        offsetData[2] = (byte) ((offset >> 16) & 0xFF);
        offsetData[3] = (byte) ((offset >> 24) & 0xFF);

        return getReq(READ_FILE_DATA, offsetData);
    }

    public static byte[] readFileEnd() {
        return getReq(READ_FILE_END, new byte[0]);
    }

    public static byte[] fwUpdateStart(byte[] data) {
        return getReq(FW_UPDATE_START, data);
    }
    public static byte[] fwUpdateData(byte[] data) {
        return getReq(FW_UPDATE_DATA, data);
    }
    public static byte[] fwUpdateEnd() {
        return getReq(FW_UPDATE_END, new byte[0]);
    }

    public static byte[] burnFactoryInfo(byte[] data) {
        return getReq(BURN_FACTORY_INFO, data);
    }

    private static byte[] getReq(int sendCmd, byte[] data) {
        int len = data.length;
        byte[] cmd = new byte[8+len];

        cmd[0] = (byte) HEAD;
        cmd[1] = (byte) sendCmd;
        cmd[2] = (byte) ~sendCmd;
        cmd[3] = (byte) TYPE_NORMAL_SEND;
        cmd[4] = (byte) seqNo;
        // length
        cmd[5] = (byte) len;
        cmd[6] = (byte) (len>>8);

        System.arraycopy(data, 0, cmd, 7, data.length);

        cmd[cmd.length-1] = CrcUtil.calCRC8(cmd);

        addNo();

        return cmd;
    }
}

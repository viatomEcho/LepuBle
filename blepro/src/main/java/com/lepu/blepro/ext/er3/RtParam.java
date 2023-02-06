package com.lepu.blepro.ext.er3;

public class RtParam {
    private int hr;                     // 当前主机实时心率（bpm）
    private float temp;                 // 体温（无效值0xFFFF，e.g.2500，temp = 25.0℃）
    private int spo2;                   // 血氧（无效值0xFF）
    private float pi;                   // 0- 200，e.g.25 : PI = 2.5
    private int pr;                     // 脉率（30~250bpm）
    private int respRate;               // 呼吸率
    private int batteryStatus;          // 电池状态（0：正常使用，1：充电中，2：充满，3：低电量）
    private boolean insertEcgLeadWire;  // 心电导联线状态（false：未插入导联线，true：插入导联线）
    private int oxyStatus;              // 血氧状态（0：未接入血氧，1：血氧状态正常，2：血氧手指脱落，3：探头故障）
    private boolean insertTemp;         // 体温状态（0：未接入体温，1：体温状态正常）
    private int measureStatus;          // 测量状态（0：空闲，1：准备状态，2：正式测量状态）
    private int battery;                // 电池电量（e.g.100:100%）
    private int recordTime;             // 已记录时长（单位:second）
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private int leadType;               // 导联类型（0：LEAD_12，12导，1：LEAD_6，6导，2：LEAD_5，5导，3：LEAD_3，3导，4：LEAD_3_TEMP，3导带体温，
                                        // 5：LEAD_3_LEG，3导胸贴，6：LEAD_5_LEG，5导胸贴，7：LEAD_6_LEG，6导胸贴，0XFF：LEAD_NONSUP，不支持的导联）
    private String leadSn;              // 一次性导联的sn
    private boolean leadOffLA;
    private boolean leadOffLL;
    private boolean leadOffV1;
    private boolean leadOffV2;
    private boolean leadOffV3;
    private boolean leadOffV4;
    private boolean leadOffV5;
    private boolean leadOffV6;

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public int getSpo2() {
        return spo2;
    }

    public void setSpo2(int spo2) {
        this.spo2 = spo2;
    }

    public float getPi() {
        return pi;
    }

    public void setPi(float pi) {
        this.pi = pi;
    }

    public int getPr() {
        return pr;
    }

    public void setPr(int pr) {
        this.pr = pr;
    }

    public int getRespRate() {
        return respRate;
    }

    public void setRespRate(int respRate) {
        this.respRate = respRate;
    }

    public int getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(int batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public boolean isInsertEcgLeadWire() {
        return insertEcgLeadWire;
    }

    public void setInsertEcgLeadWire(boolean insertEcgLeadWire) {
        this.insertEcgLeadWire = insertEcgLeadWire;
    }

    public int getOxyStatus() {
        return oxyStatus;
    }

    public void setOxyStatus(int oxyStatus) {
        this.oxyStatus = oxyStatus;
    }

    public boolean isInsertTemp() {
        return insertTemp;
    }

    public void setInsertTemp(boolean insertTemp) {
        this.insertTemp = insertTemp;
    }

    public int getMeasureStatus() {
        return measureStatus;
    }

    public void setMeasureStatus(int measureStatus) {
        this.measureStatus = measureStatus;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(int recordTime) {
        this.recordTime = recordTime;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getLeadType() {
        return leadType;
    }

    public void setLeadType(int leadType) {
        this.leadType = leadType;
    }

    public String getLeadSn() {
        return leadSn;
    }

    public void setLeadSn(String leadSn) {
        this.leadSn = leadSn;
    }

    public boolean isLeadOffLA() {
        return leadOffLA;
    }

    public void setLeadOffLA(boolean leadOffLA) {
        this.leadOffLA = leadOffLA;
    }

    public boolean isLeadOffLL() {
        return leadOffLL;
    }

    public void setLeadOffLL(boolean leadOffLL) {
        this.leadOffLL = leadOffLL;
    }

    public boolean isLeadOffV1() {
        return leadOffV1;
    }

    public void setLeadOffV1(boolean leadOffV1) {
        this.leadOffV1 = leadOffV1;
    }

    public boolean isLeadOffV2() {
        return leadOffV2;
    }

    public void setLeadOffV2(boolean leadOffV2) {
        this.leadOffV2 = leadOffV2;
    }

    public boolean isLeadOffV3() {
        return leadOffV3;
    }

    public void setLeadOffV3(boolean leadOffV3) {
        this.leadOffV3 = leadOffV3;
    }

    public boolean isLeadOffV4() {
        return leadOffV4;
    }

    public void setLeadOffV4(boolean leadOffV4) {
        this.leadOffV4 = leadOffV4;
    }

    public boolean isLeadOffV5() {
        return leadOffV5;
    }

    public void setLeadOffV5(boolean leadOffV5) {
        this.leadOffV5 = leadOffV5;
    }

    public boolean isLeadOffV6() {
        return leadOffV6;
    }

    public void setLeadOffV6(boolean leadOffV6) {
        this.leadOffV6 = leadOffV6;
    }

    @Override
    public String toString() {
        return "RtParam{" +
                "hr=" + hr +
                ", temp=" + temp +
                ", spo2=" + spo2 +
                ", pi=" + pi +
                ", pr=" + pr +
                ", respRate=" + respRate +
                ", batteryStatus=" + batteryStatus +
                ", insertEcgLeadWire=" + insertEcgLeadWire +
                ", oxyStatus=" + oxyStatus +
                ", insertTemp=" + insertTemp +
                ", measureStatus=" + measureStatus +
                ", battery=" + battery +
                ", recordTime=" + recordTime +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                ", second=" + second +
                ", leadType=" + leadType +
                ", leadSn='" + leadSn + '\'' +
                ", leadOffLA=" + leadOffLA +
                ", leadOffLL=" + leadOffLL +
                ", leadOffV1=" + leadOffV1 +
                ", leadOffV2=" + leadOffV2 +
                ", leadOffV3=" + leadOffV3 +
                ", leadOffV4=" + leadOffV4 +
                ", leadOffV5=" + leadOffV5 +
                ", leadOffV6=" + leadOffV6 +
                '}';
    }
}

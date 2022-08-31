package com.lepu.blepro.ble.data;

import java.util.Arrays;

public class Lpm311Data {

    private byte[] bytes;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private double chol;
    private String cholStr;
    private double hdl;
    private String hdlStr;
    private double trig;
    private String trigStr;
    private double ldl;
    private String ldlStr;
    private double cholDivHdl;
    private String cholDivHdlStr;
    private int unit;           // 0：mmol_L，1：mg_dL
    private String user;

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
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

    public double getChol() {
        return chol;
    }

    public void setChol(double chol) {
        this.chol = chol;
    }

    public double getHdl() {
        return hdl;
    }

    public void setHdl(double hdl) {
        this.hdl = hdl;
    }

    public double getTrig() {
        return trig;
    }

    public void setTrig(double trig) {
        this.trig = trig;
    }

    public double getLdl() {
        return ldl;
    }

    public void setLdl(double ldl) {
        this.ldl = ldl;
    }

    public double getCholDivHdl() {
        return cholDivHdl;
    }

    public void setCholDivHdl(double cholDivHdl) {
        this.cholDivHdl = cholDivHdl;
    }

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCholStr() {
        return cholStr;
    }

    public void setCholStr(String cholStr) {
        this.cholStr = cholStr;
    }

    public String getHdlStr() {
        return hdlStr;
    }

    public void setHdlStr(String hdlStr) {
        this.hdlStr = hdlStr;
    }

    public String getTrigStr() {
        return trigStr;
    }

    public void setTrigStr(String trigStr) {
        this.trigStr = trigStr;
    }

    public String getLdlStr() {
        return ldlStr;
    }

    public void setLdlStr(String ldlStr) {
        this.ldlStr = ldlStr;
    }

    public String getCholDivHdlStr() {
        return cholDivHdlStr;
    }

    public void setCholDivHdlStr(String cholDivHdlStr) {
        this.cholDivHdlStr = cholDivHdlStr;
    }

    public static final Double CHOL_MG_MIN = 100.0D;
    public static final Double CHOL_MG_MAX = 500.0D;
    public static final Double HDL_MG_MIN = 15.0D;
    public static final Double HDL_MG_MAX = 100.0D;
    public static final Double TRIG_MG_MIN = 45.0D;
    public static final Double TRIG_MG_MAX = 650.0D;
    public static final Double LDL_MG_MIN = 50.0D;
    public static final Double LDL_MG_MAX = 190.0D;
    public static final Double CHOL_MMOL_MIN = 2.59D;
    public static final Double CHOL_MMOL_MAX = 12.93D;
    public static final Double HDL_MMOL_MIN = 0.39D;
    public static final Double HDL_MMOL_MAX = 2.59D;
    public static final Double TRIG_MMOL_MIN = 0.51D;
    public static final Double TRIG_MMOL_MAX = 7.34D;
    public static final Double LDL_MMOL_MIN = 1.29D;
    public static final Double LDL_MMOL_MAX = 4.91D;
    public static final Double VALUE_0 = 0.0D;
    public static final int CHOL = 0;
    public static final int HDL = 1;
    public static final int TRIG = 2;
    public static final int LDL = 3;
    public static final int CHOL_HDL = 4;
    public static final int UNIT_MMOL = 0;
    public static final int UNIT_MG = 1;
    public static String getDataStr(int unit, int dataType, double data) {
        if (unit == UNIT_MG) {
            switch (dataType) {
                case CHOL:
                    if (data < CHOL_MG_MIN) {
                        return "<100";
                    } else if (data > CHOL_MG_MAX) {
                        return ">500";
                    } else {
                        return ""+data;
                    }
                case HDL:
                    if (data < HDL_MG_MIN) {
                        return "<15";
                    } else if (data > HDL_MG_MAX) {
                        return ">100";
                    } else {
                        return ""+data;
                    }
                case TRIG:
                    if (data < TRIG_MG_MIN) {
                        return "<45";
                    } else if (data > TRIG_MG_MAX) {
                        return ">650";
                    } else {
                        return ""+data;
                    }
                case LDL:
                    if (data < LDL_MG_MIN && data >= VALUE_0) {
                        return "<50";
                    } else if (data > LDL_MG_MAX) {
                        return ">190";
                    } else if (data < VALUE_0) {
                        return "--";
                    } else {
                        return ""+data;
                    }
                case CHOL_HDL:
                    if (data == Double.MIN_VALUE) {
                        return "--";
                    } else {
                        return ""+data;
                    }
                default:
                    return "--";
            }
        } else if (unit == UNIT_MMOL) {
            switch (dataType) {
                case CHOL:
                    if (data < CHOL_MMOL_MIN) {
                        return "<2.59";
                    } else if (data > CHOL_MMOL_MAX) {
                        return ">12.93";
                    } else {
                        return ""+data;
                    }
                case HDL:
                    if (data < HDL_MMOL_MIN) {
                        return "<0.39";
                    } else if (data > HDL_MMOL_MAX) {
                        return ">2.59";
                    } else {
                        return ""+data;
                    }
                case TRIG:
                    if (data < TRIG_MMOL_MIN) {
                        return "<0.51";
                    } else if (data > TRIG_MMOL_MAX) {
                        return ">7.34";
                    } else {
                        return ""+data;
                    }
                case LDL:
                    if (data < LDL_MMOL_MIN && data >= VALUE_0) {
                        return "<1.29";
                    } else if (data > LDL_MMOL_MAX) {
                        return ">4.91";
                    } else if (data < VALUE_0) {
                        return "--";
                    } else {
                        return ""+data;
                    }
                case CHOL_HDL:
                    if (data == Double.MIN_VALUE) {
                        return "--";
                    } else {
                        return ""+data;
                    }
                default:
                    return "--";
            }
        } else {
            return "--";
        }
    }

    @Override
    public String toString() {
        return "Lpm311Data{" +
                "bytes=" + Arrays.toString(bytes) +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                ", second=" + second +
                ", chol=" + chol +
                ", cholStr=" + cholStr +
                ", hdl=" + hdl +
                ", hdlStr=" + hdlStr +
                ", trig=" + trig +
                ", trigStr=" + trigStr +
                ", ldl=" + ldl +
                ", ldlStr=" + ldlStr +
                ", cholDivHdl=" + cholDivHdl +
                ", cholDivHdlStr=" + cholDivHdlStr +
                ", unit=" + unit +
                ", user='" + user + '\'' +
                '}';
    }
}

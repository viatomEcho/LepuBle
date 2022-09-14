package com.lepu.blepro.ext;

public class PoctorM3102Data {

    private int type;        // 0：血糖，1：尿酸，3：血酮
    private boolean normal;  // 结果正常时设备保存数据，结果不正常时设备不保存数据
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int result;       // normal=false(0是低值Lo,1是高值Hi),血糖、血酮的结果四位最后一位为小数点后一位数据

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isNormal() {
        return normal;
    }

    public void setNormal(boolean normal) {
        this.normal = normal;
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

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "PoctorM3102Data{" +
                "type=" + type +
                ", normal=" + normal +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                ", result=" + result +
                '}';
    }
}

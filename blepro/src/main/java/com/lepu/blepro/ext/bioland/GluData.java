package com.lepu.blepro.ext.bioland;

public class GluData {

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int resultMg;      // 单位：mg/dl (18-Li，707-Hi)
    private float resultMmol;  // 单位：mmol/l (1.0-Li，39.3-Hi)

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

    public int getResultMg() {
        return resultMg;
    }

    public void setResultMg(int resultMg) {
        this.resultMg = resultMg;
    }

    public float getResultMmol() {
        return resultMmol;
    }

    public void setResultMmol(float resultMmol) {
        this.resultMmol = resultMmol;
    }

    @Override
    public String toString() {
        return "GluData{" +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                ", resultMg=" + resultMg +
                ", resultMmol=" + resultMmol +
                '}';
    }
}

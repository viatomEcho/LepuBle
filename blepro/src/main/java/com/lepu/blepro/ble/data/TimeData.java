package com.lepu.blepro.ble.data;

import androidx.annotation.NonNull;
import com.lepu.blepro.utils.Convertible;
import java.util.Calendar;
import java.util.Date;

/**
 * @author wujuan
 */
public class TimeData implements Convertible {

    private int year;
    private int month;
    private int date;

    private int hour;
    private int minute;
    private int second;

    public TimeData(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH) + 1;
        this.date = calendar.get(Calendar.DATE);
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
        this.second = calendar.get(Calendar.SECOND);
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

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
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

    @Override
    public byte[] convert2Data() {
        byte[] data = new byte[7];
        data[0] = (byte) (year & 0xFF);
        data[1] = (byte) ((year >> 8) & 0xFF);
        data[2] = (byte) (month & 0xFF);
        data[3] = (byte) (date & 0xFF);
        data[4] = (byte) (hour & 0xFF);
        data[5] = (byte) (minute & 0xFF);
        data[6] = (byte) (second & 0xFF);
        return data;
    }

    @NonNull
    @Override
    public String toString() {
        String string = "".concat(String.valueOf(year))
                .concat("-").concat(String.valueOf(month))
                .concat("-").concat(String.valueOf(date))
                .concat(" ").concat(String.valueOf(hour))
                .concat(":").concat(String.valueOf(minute))
                .concat(":").concat(String.valueOf(second));
        return string;
    }
}

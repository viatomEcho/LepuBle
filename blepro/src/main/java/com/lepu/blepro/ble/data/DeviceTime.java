package com.lepu.blepro.ble.data;

import androidx.annotation.NonNull;

import com.lepu.blepro.utils.Convertible;

public class DeviceTime implements Convertible {
    private int year;
    private byte month;
    private byte date;
    private byte hour;
    private byte minute;
    private byte second;

    private byte[] data;

    public DeviceTime() {
    }

    public void setData(byte[] datetime) {
        this.data = datetime;
        year = (datetime[0] & 0xFF) + ((datetime[1] & 0xFF) << 8);
        month = datetime[2];
        date = datetime[3];
        hour = datetime[4];
        minute = datetime[5];
        second = datetime[6];
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public byte getMonth() {
        return month;
    }

    public void setMonth(byte month) {
        this.month = month;
    }

    public byte getDate() {
        return date;
    }

    public void setDate(byte date) {
        this.date = date;
    }

    public byte getHour() {
        return hour;
    }

    public void setHour(byte hour) {
        this.hour = hour;
    }

    public byte getMinute() {
        return minute;
    }

    public void setMinute(byte minute) {
        this.minute = minute;
    }

    public byte getSecond() {
        return second;
    }

    public void setSecond(byte second) {
        this.second = second;
    }

    @Override
    public byte[] convert2Data() {
        byte[] data = new byte[7];
        data[0] = (byte) (year >> 8 & 0xFF);
        data[1] = (byte) (year & 0xFF);
        data[2] = month;
        data[3] = date;
        data[4] = hour;
        data[5] = minute;
        data[6] = second;
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

package com.lepu.blepro.ble.data;

import java.util.Arrays;
import java.util.List;

/**
 * @author chenyongfeng
 */
public class Lew3FileList {
    private int fileNum;
    private String[] fileNames;
    public Lew3FileList(byte[] data) {
        fileNum = (data.length-10)/44;
        fileNames = new String[fileNum];
        for(int i = 0; i < fileNum; i++) {
            byte[] tmeData = Arrays.copyOfRange(data, (i * 44) + 10, (i + 1) * 44 + 10);
            int year = (tmeData[0] & 0xFF) + ((tmeData[1] & 0xFF) << 8);
            int month = tmeData[2]&0xFF;
            int day = tmeData[3]&0xFF;
            int hour = tmeData[4]&0xFF;
            int minute = tmeData[5]&0xFF;
            int second = tmeData[6]&0xFF;

            String mon = ""+month;
            String d = ""+day;
            String h = ""+hour;
            String min = ""+minute;
            String s = ""+second;

            if (month<10) {
                mon = "0"+month;
            }
            if (day<10) {
                d = "0"+day;
            }
            if (hour<10) {
                h = "0"+hour;
            }
            if (minute<10) {
                min = "0"+minute;
            }
            if (second<10) {
                s = "0"+second;
            }

            String name = "R"+year+mon+d+h+min+s;
            fileNames[i] = name;
        }
    }

    public int getFileNum() {
        return fileNum;
    }

    public String[] getFileNames() {
        return fileNames;
    }

    public List<String> listFileName() {
        return Arrays.asList(fileNames);
    }

    @Override
    public String toString() {
        return "Lew3FileList{" +
                "fileNum=" + fileNum +
                ", fileNames=" + Arrays.toString(fileNames) +
                '}';
    }
}

package com.lepu.blepro.ext.ventilator;

import java.util.Arrays;

public class StatisticsFile {

    private String fileName;
    private int fileVersion;  //
    private int fileType;
    // reserved 8
    private int duration;  // 记录时长s
    private int usageDays;  // 使用设备的天数(0-365)
    private int moreThan4hDays;  // 每天使用时间大于4小时的天数(0-365)
    private int meanSecond;  // 平均每天的使用秒数(0-86400)
    // 单次通气参数
    private int spont;  // 自主呼吸占比 (0-100)
    private int ahiCount;  // 呼吸暂停低通气次数
    private int aiCount;  // 呼吸暂停次数
    private int hiCount;  // 低通气次数
    private int oaiCount;  // 阻塞气道呼吸暂停次数
    private int caiCount;  // 中枢性呼吸暂停次数
    private int rearCount;  // 呼吸努力相关性觉醒次数
    private int sniCount;  // 鼾声次数
    private int pbCount;  // 周期性呼吸次数
    private int takeOffCount;  // 摘下次数
    private int llTime;  // 大漏气量时间
    // reserved 4
    // 监测参数统计项
    private int[] pressure;  // 实时压
    private int[] ipap;      // 吸气压力
    private int[] epap;      // 呼气压力
    private int[] vt;        // 潮气量
    private int[] mv;        // 分钟通气量
    private int[] leak;      // 漏气量
    private int[] rr;        // 呼吸率
    private int[] ti;        // 吸气时间
    private int[] ie;        // 呼吸比
    private int[] spo2;      // 血氧
    private int[] pr;        // 脉率
    private int[] hr;        // 心率

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileVersion() {
        return fileVersion;
    }

    public void setFileVersion(int fileVersion) {
        this.fileVersion = fileVersion;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getUsageDays() {
        return usageDays;
    }

    public void setUsageDays(int usageDays) {
        this.usageDays = usageDays;
    }

    public int getMoreThan4hDays() {
        return moreThan4hDays;
    }

    public void setMoreThan4hDays(int moreThan4hDays) {
        this.moreThan4hDays = moreThan4hDays;
    }

    public int getMeanSecond() {
        return meanSecond;
    }

    public void setMeanSecond(int meanSecond) {
        this.meanSecond = meanSecond;
    }

    public int getSpont() {
        return spont;
    }

    public void setSpont(int spont) {
        this.spont = spont;
    }

    public int getAhiCount() {
        return ahiCount;
    }

    public void setAhiCount(int ahiCount) {
        this.ahiCount = ahiCount;
    }

    public int getAiCount() {
        return aiCount;
    }

    public void setAiCount(int aiCount) {
        this.aiCount = aiCount;
    }

    public int getHiCount() {
        return hiCount;
    }

    public void setHiCount(int hiCount) {
        this.hiCount = hiCount;
    }

    public int getOaiCount() {
        return oaiCount;
    }

    public void setOaiCount(int oaiCount) {
        this.oaiCount = oaiCount;
    }

    public int getCaiCount() {
        return caiCount;
    }

    public void setCaiCount(int caiCount) {
        this.caiCount = caiCount;
    }

    public int getRearCount() {
        return rearCount;
    }

    public void setRearCount(int rearCount) {
        this.rearCount = rearCount;
    }

    public int getSniCount() {
        return sniCount;
    }

    public void setSniCount(int sniCount) {
        this.sniCount = sniCount;
    }

    public int getPbCount() {
        return pbCount;
    }

    public void setPbCount(int pbCount) {
        this.pbCount = pbCount;
    }

    public int getTakeOffCount() {
        return takeOffCount;
    }

    public void setTakeOffCount(int takeOffCount) {
        this.takeOffCount = takeOffCount;
    }

    public int getLlTime() {
        return llTime;
    }

    public void setLlTime(int llTime) {
        this.llTime = llTime;
    }

    public int[] getPressure() {
        return pressure;
    }

    public void setPressure(int[] pressure) {
        this.pressure = pressure;
    }

    public int[] getIpap() {
        return ipap;
    }

    public void setIpap(int[] ipap) {
        this.ipap = ipap;
    }

    public int[] getEpap() {
        return epap;
    }

    public void setEpap(int[] epap) {
        this.epap = epap;
    }

    public int[] getVt() {
        return vt;
    }

    public void setVt(int[] vt) {
        this.vt = vt;
    }

    public int[] getMv() {
        return mv;
    }

    public void setMv(int[] mv) {
        this.mv = mv;
    }

    public int[] getLeak() {
        return leak;
    }

    public void setLeak(int[] leak) {
        this.leak = leak;
    }

    public int[] getRr() {
        return rr;
    }

    public void setRr(int[] rr) {
        this.rr = rr;
    }

    public int[] getTi() {
        return ti;
    }

    public void setTi(int[] ti) {
        this.ti = ti;
    }

    public int[] getIe() {
        return ie;
    }

    public void setIe(int[] ie) {
        this.ie = ie;
    }

    public int[] getSpo2() {
        return spo2;
    }

    public void setSpo2(int[] spo2) {
        this.spo2 = spo2;
    }

    public int[] getPr() {
        return pr;
    }

    public void setPr(int[] pr) {
        this.pr = pr;
    }

    public int[] getHr() {
        return hr;
    }

    public void setHr(int[] hr) {
        this.hr = hr;
    }

    @Override
    public String toString() {
        return "StatisticsFile{" +
                "fileName='" + fileName + '\'' +
                ", fileVersion=" + fileVersion +
                ", fileType=" + fileType +
                ", duration=" + duration +
                ", usageDays=" + usageDays +
                ", moreThan4hDays=" + moreThan4hDays +
                ", meanSecond=" + meanSecond +
                ", spont=" + spont +
                ", ahiCount=" + ahiCount +
                ", aiCount=" + aiCount +
                ", hiCount=" + hiCount +
                ", oaiCount=" + oaiCount +
                ", caiCount=" + caiCount +
                ", rearCount=" + rearCount +
                ", sniCount=" + sniCount +
                ", pbCount=" + pbCount +
                ", takeOffCount=" + takeOffCount +
                ", llTime=" + llTime +
                ", pressure=" + Arrays.toString(pressure) +
                ", ipap=" + Arrays.toString(ipap) +
                ", epap=" + Arrays.toString(epap) +
                ", vt=" + Arrays.toString(vt) +
                ", mv=" + Arrays.toString(mv) +
                ", leak=" + Arrays.toString(leak) +
                ", rr=" + Arrays.toString(rr) +
                ", ti=" + Arrays.toString(ti) +
                ", ie=" + Arrays.toString(ie) +
                ", spo2=" + Arrays.toString(spo2) +
                ", pr=" + Arrays.toString(pr) +
                ", hr=" + Arrays.toString(hr) +
                '}';
    }
}

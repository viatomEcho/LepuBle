package com.lepu.blepro.ble.data.ventilator

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.toUInt

class StatisticsFile(val fileName: String, val bytes: ByteArray) {

    var fileVersion: Int  //
    var fileType: Int
    // reserved 8
    var duration: Int  // 记录时长s
    var usageDays: Int  // 使用设备的天数(0-365)
    var moreThan4hDays: Int  // 每天使用时间大于4小时的天数(0-365)
    var meanSecond: Int  // 平均每天的使用秒数(0-86400)
    // 单次通气参数
    var spont: Int  // 自主呼吸占比 (0-100)
    var ahiCount: Int  // 呼吸暂停低通气次数
    var aiCount: Int  // 呼吸暂停次数
    var hiCount: Int  // 低通气次数
    var oaiCount: Int  // 阻塞气道呼吸暂停次数
    var caiCount: Int  // 中枢性呼吸暂停次数
    var rearCount: Int  // 呼吸努力相关性觉醒次数
    var sniCount: Int  // 鼾声次数
    var pbCount: Int  // 周期性呼吸次数
    var takeOffCount: Int  // 摘下次数
    var llTime: Int  // 大漏气量时间s
    // reserved 4
    // 监测参数统计项
    var pressure = FloatArray(5)  // 实时压(0~40cmH2O),单位0.1cmH20,e.g.10:1cmH2O[0,400]
    var ipap = FloatArray(5)      // 吸气压力(0~40cmH2O),单位0.1cmH20,e.g.10:1cmH2O[0,400]
    var epap = FloatArray(5)      // 呼气压力(0~40cmH2O),单位0.1cmH20,e.g.10:1cmH2O[0,400]
    var vt = IntArray(5)          // 潮气量(0~3000mL),单位1mL,e.g.10:10mL[0,3000]
    var mv = FloatArray(5)        // 分钟通气量(0~60L/min),单位0.1L/min,e.g.10:1L/min[0,600]
    var leak = FloatArray(5)      // 漏气量(0~120L/min),单位0.1L/min,e.g.10:1L/min[0,1200]
    var rr = IntArray(5)          // 呼吸率(0~60),单位1bpm,e.g.10:10bpm[0,60]
    var ti = FloatArray(5)        // 吸气时间(0.1-4s),单位0.1s,e.g.10:1s[1,40]
    var ie = FloatArray(5)        // 呼吸比(1:50.0-3.0:1),单位0.0001,[200,30000]
    var spo2 = IntArray(5)        // 血氧(70-100%),单位1%,e.g.10:10%[70,100]
    var pr = IntArray(5)          // 脉率(30-250bpm),单位1bpm,e.g.10:10bpm[30,250]
    var hr = IntArray(5)          // 心率(30-250bpm),单位1bpm,e.g.10:10bpm[30,250]

    init {
        var index = 0
        fileVersion = byte2UInt(bytes[index])
        index++
        fileType = byte2UInt(bytes[index])
        index++
        index += 8
        duration = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        usageDays = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        moreThan4hDays = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        meanSecond = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        spont = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        ahiCount = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        aiCount = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        hiCount = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        oaiCount = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        caiCount = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        rearCount = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        sniCount = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        pbCount = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        takeOffCount = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        llTime = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        index += 4
        for (i in pressure.indices) {
            pressure[i] = toUInt(bytes.copyOfRange(index, index+4)).div(10f)
            index += 4
        }
        for (i in ipap.indices) {
            ipap[i] = toUInt(bytes.copyOfRange(index, index+4)).div(10f)
            index += 4
        }
        for (i in epap.indices) {
            epap[i] = toUInt(bytes.copyOfRange(index, index+4)).div(10f)
            index += 4
        }
        for (i in vt.indices) {
            vt[i] = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
        }
        for (i in mv.indices) {
            mv[i] = toUInt(bytes.copyOfRange(index, index+4)).div(10f)
            index += 4
        }
        for (i in leak.indices) {
            leak[i] = toUInt(bytes.copyOfRange(index, index+4)).div(10f)
            index += 4
        }
        for (i in rr.indices) {
            rr[i] = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
        }
        for (i in ti.indices) {
            ti[i] = toUInt(bytes.copyOfRange(index, index+4)).div(10f)
            index += 4
        }
        for (i in ie.indices) {
            ie[i] = toUInt(bytes.copyOfRange(index, index+4)).div(10000f)
            index += 4
        }
        for (i in spo2.indices) {
            spo2[i] = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
        }
        for (i in pr.indices) {
            pr[i] = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
        }
        for (i in hr.indices) {
            hr[i] = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
        }
    }

    override fun toString(): String {
        return """
            StatisticsFile : 
            fileVersion : $fileVersion
            fileType : $fileType
            duration : $duration
            usageDays : $usageDays
            moreThan4hDays : $moreThan4hDays
            meanSecond : $meanSecond
            spont : $spont
            ahiCount : $ahiCount
            aiCount : $aiCount
            hiCount : $hiCount
            oaiCount : $oaiCount
            caiCount : $caiCount
            rearCount : $rearCount
            sniCount : $sniCount
            pbCount : $pbCount
            takeOffCount : $takeOffCount
            llTime : $llTime
            pressure : ${pressure.joinToString(",")}
            ipap : ${ipap.joinToString(",")}
            epap : ${epap.joinToString(",")}
            vt : ${vt.joinToString(",")}
            mv : ${mv.joinToString(",")}
            leak : ${leak.joinToString(",")}
            rr : ${rr.joinToString(",")}
            ti : ${ti.joinToString(",")}
            ie : ${ie.joinToString(",")}
            spo2 : ${spo2.joinToString(",")}
            pr : ${pr.joinToString(",")}
            hr : ${hr.joinToString(",")}
        """.trimIndent()
    }
}
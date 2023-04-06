package com.lepu.blepro.ble.data.bp3

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.int2ByteArray
import com.lepu.blepro.utils.int4ByteArray
import com.lepu.blepro.utils.toUInt

class Bp3Config() {

    var uid: Int = 0
    var uarr: Int = 0
    var usize: Int = 0
    var prevCalibZero: Int = 0          // 上一次校零adc值	e.g.	2800<=zero<=12000 128mV~550mV
    var lastCalibZero: Int = 0          // 最后一次校零adc值	e.g.	2800<=zero<=12000 128mV~550mV
    var calibSlope: Int = 0             // 校准斜率值*100	e.g.	13630<=slope<=17040 136.3LSB/mmHg-170.4LSB/mmHg
    var slopePressure: Int = 0          // 校准斜率时用的压力值
    var bpTestTargetPressure: Int = 0   // 血压测试目标打气阈值
    var beepSwitch: Boolean = false     // 蜂鸣器开关 0：关；1：开
    var voiceSwitch: Boolean = false    // 语音开关 0：关；1：开
    var timeUtc: Int = 80               // 时间时区
    var wifi4gSwitch: Boolean = false   // WiFi/4g开关

    constructor(bytes: ByteArray) : this() {
        var index = 0
        uid = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        uarr = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        usize = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        prevCalibZero = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        lastCalibZero = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        calibSlope = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        slopePressure = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        bpTestTargetPressure = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        beepSwitch = byte2UInt(bytes[index]) == 1
        index++
        voiceSwitch = byte2UInt(bytes[index]) == 1
        index++
        timeUtc = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        wifi4gSwitch = byte2UInt(bytes[index]) == 1
    }
    fun getDataBytes(): ByteArray {
        val beep = if (beepSwitch) {
            1
        } else {
            0
        }
        val voice = if (voiceSwitch) {
            1
        } else {
            0
        }
        val wifi4g = if (wifi4gSwitch) {
            1
        } else {
            0
        }
        return int4ByteArray(uid)
            .plus(int4ByteArray(uarr))
            .plus(int4ByteArray(usize))
            .plus(int4ByteArray(prevCalibZero))
            .plus(int4ByteArray(lastCalibZero))
            .plus(int4ByteArray(calibSlope))
            .plus(int2ByteArray(slopePressure))
            .plus(int2ByteArray(bpTestTargetPressure))
            .plus(beep.toByte())
            .plus(voice.toByte())
            .plus(int4ByteArray(timeUtc))
            .plus(wifi4g.toByte())
    }

    override fun toString(): String {
        return """
            Bp3Config : 
            uid : $uid
            uarr : $uarr
            usize : $usize
            prevCalibZero : $prevCalibZero
            lastCalibZero : $lastCalibZero
            calibSlope : $calibSlope
            slopePressure : $slopePressure
            bpTestTargetPressure : $bpTestTargetPressure
            beepSwitch : $beepSwitch
            voiceSwitch : $voiceSwitch
            timeUtc : $timeUtc
            wifi4gSwitch : $wifi4gSwitch
        """.trimIndent()
    }
}
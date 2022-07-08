package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.int2ByteArray
import com.lepu.blepro.utils.toUInt

class Bp2Config() {

    var bytes = byteArrayOf(0)
    var prevCalibZero: Int = 0
    var lastCalibZero: Int = 0
    var calibSlope: Int = 0
    var slopePressure: Int = 0
    var calibTicks: Int = 0
    var sleepTicks: Int = 0
    var bpTestTargetPressure: Int = 0   // 目标压力值
    var beepSwitch: Boolean = false     // 心电音开关
    var avgMeasureMode: Int = 0         // 0：x3模式关闭 1：x3模式开启（时间间隔30s） 2：时间间隔60s 3：时间间隔90s 4：时间间隔120s
    var volume: Int = 0                 // 音量大小（0关，1，2，3）

    constructor(bytes: ByteArray) : this() {
        this.bytes = bytes
        var index = 0
        prevCalibZero = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        lastCalibZero = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        calibSlope = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        slopePressure = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        calibTicks = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        sleepTicks = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        bpTestTargetPressure = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        beepSwitch = (byte2UInt(bytes[index]) and 0x01) == 1
        index++
        avgMeasureMode = byte2UInt(bytes[index])
        index++
        volume = byte2UInt(bytes[index])
    }

    fun getDataBytes(): ByteArray {
        val on = if (beepSwitch) {
            1
        } else {
            0
        }
        val data = ByteArray(22)
        return data.plus(int2ByteArray(bpTestTargetPressure))
            .plus(on.toByte())
            .plus(avgMeasureMode.toByte())
            .plus(volume.toByte())
            .plus(ByteArray(13))
    }

    override fun toString(): String {
        return """
            Bp2Config : 
            bytes : ${bytesToHex(bytes)}
            getDataBytes : ${bytesToHex(getDataBytes())}
            prevCalibZero : $prevCalibZero
            lastCalibZero : $lastCalibZero
            calibSlope : $calibSlope
            slopePressure : $slopePressure
            calibTicks : $calibTicks
            sleepTicks : $sleepTicks
            bpTestTargetPressure : $bpTestTargetPressure
            beepSwitch : $beepSwitch
            avgMeasureMode : $avgMeasureMode
            volume : $volume
        """.trimIndent()
    }
}
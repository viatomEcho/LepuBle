package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.toUInt

class Bp2wConfig {

    var prevCalibZero: Int
    var lastCalibZero: Int
    var calibSlope: Int
    var slopePressure: Int
    var calibTicks: Int
    var sleepTicks: Int
    var bpTestTargetPressure: Int
    var beepSwitch: Int
    var avgMeasureMode: Int
    var volume: Int

    constructor(bytes: ByteArray) {
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
        beepSwitch = toUInt(bytes.copyOfRange(index, index+1))
        index++
        avgMeasureMode = toUInt(bytes.copyOfRange(index, index+1))
        index++
        volume = toUInt(bytes.copyOfRange(index, index+1))
    }

    override fun toString(): String {
        return """
            Bp2wConfig
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
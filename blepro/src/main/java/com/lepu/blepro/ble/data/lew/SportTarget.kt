package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.int2ByteArray
import com.lepu.blepro.utils.int4ByteArray
import com.lepu.blepro.utils.toUInt

class SportTarget() {

    var step = 0       // 单位:步数
    var distance = 0   // 单位:⽶
    var calories = 0   // 单位:卡路⾥
    var sleep = 0      // 单位:分钟 ??
    var sportTime = 0  // 单位:分钟

    constructor(bytes: ByteArray) : this() {
        var index = 0
        step = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        distance = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        calories = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        sleep = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        sportTime = toUInt(bytes.copyOfRange(index, index+2))
    }

    fun getDataBytes(): ByteArray {
        return int4ByteArray(step)
            .plus(int4ByteArray(distance))
            .plus(int4ByteArray(calories))
            .plus(int2ByteArray(sleep))
            .plus(int2ByteArray(sportTime))
    }

    override fun toString(): String {
        return """
            SportTarget : 
            bytes : ${bytesToHex(getDataBytes())}
            step : $step
            distance : $distance
            calories : $calories
            sleep : $sleep
            sportTime : $sportTime
        """.trimIndent()
    }

}
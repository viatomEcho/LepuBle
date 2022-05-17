package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.int4ByteArray
import com.lepu.blepro.utils.toUInt

class TimeData() {

    var absTime = 0
    var offsetTime = 0
    var formatHour = 0  // LewBleCmd.TimeFormat
    var formatDay = 0   // LewBleCmd.TimeFormat

    constructor(bytes: ByteArray) : this() {
        var index = 0
        absTime = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        offsetTime = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        formatHour = byte2UInt(bytes[index])
        index++
        formatDay = byte2UInt(bytes[index])
    }

    fun getDataBytes(): ByteArray {
        return int4ByteArray(absTime)
            .plus(int4ByteArray(offsetTime))
            .plus(formatHour.toByte())
            .plus(formatDay.toByte())
    }

    override fun toString(): String {
        return """
            TimeData : 
            bytes : ${bytesToHex(getDataBytes())}
            absTime : $absTime
            offsetTime : $offsetTime
            formatHour : $formatHour
            formatDay : $formatDay
        """.trimIndent()
    }
}
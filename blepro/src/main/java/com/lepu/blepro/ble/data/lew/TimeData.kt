package com.lepu.blepro.ble.data.lew

import android.util.Log
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.int4ByteArray
import com.lepu.blepro.utils.toUInt
import java.util.*

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
        val curTime = (System.currentTimeMillis() / 1000).toInt()
        offsetTime = TimeZone.getDefault().getOffset(System.currentTimeMillis()).div(1000)
        absTime = curTime - offsetTime
        Log.d("test12345", "curTime = $curTime")
        Log.d("test12345", "offsetTime = $offsetTime")
        Log.d("test12345", "absTime = $absTime")
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
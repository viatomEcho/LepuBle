package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex

class HrDetect() {

    var switch = false  // ⾃动⼼率检测开关
    var interval = 0    // ⾃动⼼率间隔，单位分钟
    // reserved 2

    constructor(bytes: ByteArray) : this() {
        var index = 0
        switch = byte2UInt(bytes[index]) == 1
        index++
        interval = byte2UInt(bytes[index])
    }

    fun getDataBytes(): ByteArray {
        val on = if (switch) {
            1
        } else {
            0
        }
        return byteArrayOf(on.toByte())
            .plus(interval.toByte())
    }

    override fun toString(): String {
        return """
            HrDetect : 
            bytes : ${bytesToHex(getDataBytes())}
            switch : $switch
            interval : $interval
        """.trimIndent()
    }
}
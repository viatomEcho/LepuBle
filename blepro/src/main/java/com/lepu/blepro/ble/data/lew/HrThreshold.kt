package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex

class HrThreshold() {

    var switch = false
    var threshold = 0

    constructor(bytes: ByteArray) : this() {
        var index = 0
        switch = byte2UInt(bytes[index]) == 1
        index++
        threshold = byte2UInt(bytes[index])
    }

    fun getDataBytes(): ByteArray {
        val on = if (switch) {
            1
        } else {
            0
        }
        return byteArrayOf(on.toByte())
            .plus(threshold.toByte())
    }

    override fun toString(): String {
        return """
            HrThreshold : 
            bytes : ${bytesToHex(getDataBytes())}
            switch : $switch
            threshold : $threshold
        """.trimIndent()
    }

}
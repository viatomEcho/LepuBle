package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex

class HandRaiseSetting() {

    var switch = false
    var startHour = 0
    var startMin = 0
    var stopHour = 0
    var stopMin = 0

    constructor(bytes: ByteArray) : this() {
        var index = 0
        switch = byte2UInt(bytes[index]) == 1
        index++
        startHour = byte2UInt(bytes[index])
        index++
        startMin = byte2UInt(bytes[index])
        index++
        stopHour = byte2UInt(bytes[index])
        index++
        stopMin = byte2UInt(bytes[index])
    }

    fun getDataBytes(): ByteArray {
        val on = if (switch) {
            1
        } else {
            0
        }
        return byteArrayOf(on.toByte())
            .plus(startHour.toByte())
            .plus(startMin.toByte())
            .plus(stopHour.toByte())
            .plus(stopMin.toByte())
    }

    override fun toString(): String {
        return """
            HandRaiseSetting : 
            bytes : ${bytesToHex(getDataBytes())}
            switch : $switch
            startHour : $startHour
            startMin : $startMin
            stopHour : $stopHour
            stopMin : $stopMin
        """.trimIndent()
    }
}
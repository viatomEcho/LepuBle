package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex

class PhoneSwitch() {

    var call = false     // 来电
    var message = false  // 短信

    constructor(bytes: ByteArray) : this() {
        var index = 0
        call = byte2UInt(bytes[index]) == 0
        index++
        message = byte2UInt(bytes[index]) == 0
    }

    fun getDataBytes(): ByteArray {
        val onCall = if (call) {
            0
        } else {
            1
        }
        val onMess = if (message) {
            0
        } else {
            1
        }
        return byteArrayOf(onCall.toByte())
            .plus(onMess.toByte())
    }

    override fun toString(): String {
        return """
            PhoneSwitch : 
            bytes : ${bytesToHex(getDataBytes())}
            call : $call
            message : $message
        """.trimIndent()
    }
}
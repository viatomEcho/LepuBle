package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex

class Sp20Config() {

    /**
     * 2 血氧过低阈值（85-99）
     * 3 脉率过低阈值（30-99）
     * 4 脉率过高阈值（100-250）
     */

    var bytes = byteArrayOf(0)
    var type: Int = 0   // Sp20BleCmd.ConfigType
    var value: Int = 0

    constructor(bytes: ByteArray) : this() {
        this.bytes = bytes
        type = byte2UInt(bytes[0])
        value = byte2UInt(bytes[1])
    }

    fun getDataBytes(): ByteArray {
        val data = byteArrayOf(type.toByte())
        return data.plus(value.toByte())
    }

    override fun toString(): String {
        return """
            Sp20Config : 
            bytes : ${bytesToHex(bytes)}
            getDataBytes : ${bytesToHex(getDataBytes())}
            type : $type
            value : $value
        """.trimIndent()
    }

}
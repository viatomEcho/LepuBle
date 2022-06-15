package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex

class SystemSetting() {

    var language = 0                    // LewBleCmd.Language
    var unit = UnitSetting()            // LewBleCmd.Unit
    var handRaise = HandRaiseSetting()  //
    var hand = 0                        // LewBleCmd.Hand

    constructor(bytes: ByteArray) : this() {
        var index = 0
        language = byte2UInt(bytes[index])
        index++
        unit = UnitSetting(bytes.copyOfRange(index, index+4))
        index += 4
        handRaise = HandRaiseSetting(bytes.copyOfRange(index, index+5))
        index += 5
        hand = byte2UInt(bytes[index])
    }

    fun getDataBytes(): ByteArray {
        return byteArrayOf(language.toByte())
            .plus(unit.getDataBytes())
            .plus(handRaise.getDataBytes())
            .plus(hand.toByte())
    }

    override fun toString(): String {
        return """
            SystemSettings : 
            bytes : ${bytesToHex(getDataBytes())}
            language : $language
            unit : $unit
            handRaise : $handRaise
            hand : $hand
        """.trimIndent()
    }
}
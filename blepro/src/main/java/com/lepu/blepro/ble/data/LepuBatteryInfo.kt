package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.toUInt

class LepuBatteryInfo(bytes: ByteArray) {
    var state: Int
    var percent: Int
    var voltage: Int

    init {
        var index = 0
        state = byte2UInt(bytes[index])
        index++
        percent = byte2UInt(bytes[index])
        index++
        voltage = toUInt(bytes.copyOfRange(index, index+2))
    }

    override fun toString(): String {
        return """
            state : $state
            percent : $percent
            voltage : $voltage
        """.trimIndent()
    }
}
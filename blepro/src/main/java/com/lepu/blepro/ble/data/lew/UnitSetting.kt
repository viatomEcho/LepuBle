package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex

class UnitSetting() {

    var lengthUnit = 0  // 0: km/m; 1: feet/inch
    var weightUnit = 0  // 0:kg/g; 1:磅; 2：英⽯
    var tempUnit = 0    // 0:C; 1:F
    // reserve

    constructor(bytes: ByteArray) : this() {
        var index = 0
        lengthUnit = byte2UInt(bytes[index])
        index++
        weightUnit = byte2UInt(bytes[index])
        index++
        tempUnit = byte2UInt(bytes[index])
        index++
    }

    fun getDataBytes(): ByteArray {
        return byteArrayOf(lengthUnit.toByte())
            .plus(weightUnit.toByte())
            .plus(tempUnit.toByte())
            .plus(byteArrayOf(0))
    }

    override fun toString(): String {
        return """
            UnitSetting : 
            bytes : ${bytesToHex(getDataBytes())}
            lengthUnit : $lengthUnit
            weightUnit : $weightUnit
            tempUnit : $tempUnit
        """.trimIndent()
    }
}
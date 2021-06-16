package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.toInt
import com.lepu.blepro.utils.toUInt


class Bp2DataBpIng {
    var isDeflate : Boolean = false
    var pressure : Int = 0
    var isPulse : Boolean = false // is pulse wave
    var pr : Int = 0

    constructor(bytes: ByteArray) {
        this.pressure = toInt(bytes.copyOfRange(1,3))/100
        this.isPulse = bytes[3].toInt() == 0
        this.pr = toUInt(bytes.copyOfRange(4, 6))
        this.isDeflate = bytes[6].toInt() == 1
    }

    override fun toString(): String {
        return """
            Bping
            isDeflate: $isDeflate
            pressure: $pressure
            isPulse: $isPulse
            pr: $pr
        """.trimIndent()
    }
}
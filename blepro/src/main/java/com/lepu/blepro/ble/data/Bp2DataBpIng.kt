package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toInt
import com.lepu.blepro.utils.toUInt

class Bp2DataBpIng {
    var bytes: ByteArray
    var isDeflate : Boolean = false  // 是否放气
    var pressure : Int = 0           // 实时压
    var isPulse : Boolean = false    // 是否检测到脉搏波
    var pr : Int = 0

    constructor(bytes: ByteArray) {
        this.bytes = bytes
        this.pressure = toInt(bytes.copyOfRange(1,3))/100
        this.isPulse = bytes[3].toInt() == 0
        this.pr = toUInt(bytes.copyOfRange(4, 6))
        this.isDeflate = bytes[6].toInt() == 1
    }

    override fun toString(): String {
        return """
            Bp2DataBpIng : 
            bytes : ${bytesToHex(bytes)}
            isDeflate: $isDeflate
            pressure: $pressure
            isPulse: $isPulse
            pr: $pr
        """.trimIndent()
    }
}
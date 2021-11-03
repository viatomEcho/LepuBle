package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.toInt
import com.lepu.blepro.utils.toUInt

class Bp2DataEcgResult {

    var result : Int = 0
    var code : Int
    var hr : Int = 0
    var qrs : Int = 0
    var pvcs : Int = 0
    var qtc : Int = 0

    constructor(bytes: ByteArray) {
        this.result = toUInt(bytes.copyOfRange(0,4))
        this.code = toInt(bytes.copyOfRange(0,4))
        this.hr = toUInt(bytes.copyOfRange(4,6))
        this.qrs = toUInt(bytes.copyOfRange(6,8))
        this.pvcs = toUInt(bytes.copyOfRange(8,10))
        this.qtc = toUInt(bytes.copyOfRange(10,12))
    }

    override fun toString(): String {
        return """
            Ecg ecgResult
            ecgResult: $result
            hr: $hr
            qrs: $qrs
            pvcs: $pvcs
            qtc: $qtc
        """.trimIndent()
    }


}
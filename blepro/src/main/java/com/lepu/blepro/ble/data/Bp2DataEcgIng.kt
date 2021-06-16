package com.lepu.blepro.ble.data
import com.lepu.blepro.utils.toInt
import com.lepu.blepro.utils.toUInt

class Bp2DataEcgIng {
    var curDuration : Int
    var isPoolSignal : Boolean = false
    var isLeadOff : Boolean = false
    var hr : Int
    var flag: Int

    constructor(bytes: ByteArray) {
        curDuration = toUInt(bytes.copyOfRange(0,4))
        flag = toInt(bytes.copyOfRange(4, 8))
        val flag0 = flag and 0x01
        isPoolSignal = flag0 == 1
        val flag1 = (flag shr 1) and 0x01
        isLeadOff = flag1 == 1
        hr = toUInt(bytes.copyOfRange(8,10))
    }

    override fun toString(): String {
        return """
            Ecging
            current duration: $curDuration
            hr: $hr
            isPoolSignal: $isPoolSignal
            isLeadOff: $isLeadOff
            flag: $flag
        """.trimIndent()
    }
}
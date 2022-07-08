package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toInt
import com.lepu.blepro.utils.toUInt

class Bp2DataEcgIng {
    var bytes: ByteArray
    var curDuration : Int               // 当前测量时长 s
    var flag: Int
    var isPoolSignal : Boolean = false  // 是否信号弱
    var isLeadOff : Boolean = false     // 是否导联脱落
    var hr : Int

    constructor(bytes: ByteArray) {
        this.bytes = bytes
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
            Bp2DataEcgIng : 
            bytes : ${bytesToHex(bytes)}
            current duration: $curDuration
            hr: $hr
            isPoolSignal: $isPoolSignal
            isLeadOff: $isLeadOff
            flag: $flag
        """.trimIndent()
    }
}
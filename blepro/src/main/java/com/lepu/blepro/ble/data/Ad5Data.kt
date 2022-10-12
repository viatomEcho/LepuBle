package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.HexString.trimStr

class Ad5Data(val bytes: ByteArray) {

    var cmd = 0
    var sn = ""
    var hr1 = 0
    var hr2 = 0

    init {
        cmd = byte2UInt(bytes[2])
        sn = trimStr(String(bytes.copyOfRange(3, 10)))
        hr1 = byte2UInt(bytes[11])
        hr2 = byte2UInt(bytes[12])
    }

    override fun toString(): String {
        return """
            Ad5Data : 
            cmd : $cmd
            sn : $sn
            hr1 : $hr1
            hr2 : $hr2
        """.trimIndent()
    }

}
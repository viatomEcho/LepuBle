package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils

class VcominData(val bytes: ByteArray) {

    var cmd = 0
    var hr1 = 0
    var hr2 = 0

    init {
        cmd = ByteUtils.byte2UInt(bytes[2])
        hr1 = ByteUtils.byte2UInt(bytes[3])
        hr2 = ByteUtils.byte2UInt(bytes[4])
    }

    override fun toString(): String {
        return """
            VcominData : 
            cmd : $cmd
            hr1 : $hr1
            hr2 : $hr2
        """.trimIndent()
    }

}
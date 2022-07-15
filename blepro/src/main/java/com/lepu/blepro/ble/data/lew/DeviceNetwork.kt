package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex

class DeviceNetwork(val bytes: ByteArray) {

    var mode: Int
    var valid: Int
    var iccid: String

    init {
        var index = 0
        mode = byte2UInt(bytes[index])
        index++
        valid = byte2UInt(bytes[index])
        index++
        iccid = trimStr(String(bytes.copyOfRange(index, index+20)))
    }

    override fun toString(): String {
        return """
            DeviceNetwork :
            bytes : ${bytesToHex(bytes)}
            mode : $mode
            valid : $valid
            iccid : $iccid
        """.trimIndent()
    }

}
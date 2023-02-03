package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.toUInt

class Bp2BleResponse {
    class BleResponse {
        var cmd: Int
        var type: Byte
        var len: Int
        var content: ByteArray

        constructor(bytes: ByteArray) {
            cmd = byte2UInt(bytes[1])
            type = bytes[3]
            len = toUInt(bytes.copyOfRange(5, 7))
            content = if (len <= 0) ByteArray(0) else bytes.copyOfRange(7, 7 + len)
        }
    }

}
package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.HexString.trimStr

class LepuFileList(val bytes: ByteArray) {
    var size: Int
    var list = mutableListOf<String>()

    init {
        size = byte2UInt(bytes[0])
        for (i in 0 until size) {
            list.add(trimStr(String(bytes.copyOfRange(1+i*16, 16*(i+1)+1))))
        }
    }

    override fun toString(): String {
        return """
            size : $size
            list : $list
        """.trimIndent()
    }
}
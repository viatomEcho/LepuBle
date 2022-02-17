package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.int2ByteArray
import com.lepu.blepro.utils.toString
import com.lepu.blepro.utils.toUInt
import java.nio.charset.Charset

class Lew3Config() {
    var notchMask = 0
    var state: Int = 0
    var serverAddrType: Int = 0
    var len: Int = 0
    var addr: String = ""
    var port: Int = 0

    constructor(bytes: ByteArray) : this() {
        var index = 0
        notchMask = byte2UInt(bytes[index])
        index++
        state = byte2UInt(bytes[index])
        index++
        serverAddrType = byte2UInt(bytes[index])
        index++
        len = byte2UInt(bytes[index])
        index++
        addr = toString(bytes.copyOfRange(index, index+len))
        index += len
        port = toUInt(bytes.copyOfRange(index, index+2))

    }

    // 只配置server
    fun getDataBytes(): ByteArray {
        val data = byteArrayOf(state.toByte())
        return data.plus(serverAddrType.toByte())
            .plus(addr.length.toByte())
            .plus(addr.toByteArray(Charset.defaultCharset()))
            .plus(int2ByteArray(port))
    }

    override fun toString(): String {
        return """
            LeW3Config
            notchMask: $notchMask
            state: $state
            serverAddrType: $serverAddrType
            len: $len
            addr: $addr
            port: $port
        """.trimIndent()
    }
}
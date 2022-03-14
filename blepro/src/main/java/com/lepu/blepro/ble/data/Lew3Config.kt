package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.int2ByteArray
import com.lepu.blepro.utils.toString
import com.lepu.blepro.utils.toUInt
import java.nio.charset.Charset

class Lew3Config() {
    var bytes = byteArrayOf(0)
    var notchMask = 0            // 工频陷波屏蔽  0:开启 1:屏蔽
    var state: Int = 0           // 服务器当前连接状态 0:断开 1:连接中 2:已连接
    var serverAddrType: Int = 0  // 服务器地址类型  0:ipv4  1:域名形式
    var len: Int = 0             // 服务器地址长度
    var addr: String = ""        // 服务器地址 e.g. “192.168.1.33”
    var port: Int = 0            // 服务器端口号 6000

    constructor(bytes: ByteArray) : this() {
        this.bytes = bytes
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
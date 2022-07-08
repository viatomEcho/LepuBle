package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.int2ByteArray
import com.lepu.blepro.utils.toUInt
import java.nio.charset.Charset

class Bp2Server() {

    var length = 0

    var state: Int = 0         // 0:断开 1:连接中 2:已连接 0xff:服务器无法连接
    var addrType: Int = 0      // 服务器地址类型  0:ipv4  1:域名形式
    var addrLen: Int = 0
    var addr: String = ""      // 服务器地址 e.g. “192.168.1.33”
    var port: Int = 0          // 服务器端口号
    var bytes = byteArrayOf(0)

    constructor(i: Int, bytes: ByteArray) : this() {
        this.bytes = bytes
        var index = i
        state = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        addrType = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        addrLen = (bytes[index].toUInt() and 0xFFu).toInt()
        index++

        val len = index+addrLen+2
        if (len <= bytes.size) {
            addr = trimStr(String(bytes.copyOfRange(index, index+addrLen), Charset.defaultCharset()))
            index += addrLen
        } else {
            addr = trimStr(String(bytes.copyOfRange(index, index+16), Charset.defaultCharset()))
            index += 16
        }

        port = toUInt(bytes.copyOfRange(index, index+2))
        index += 2

        length = index - i
    }

    fun getDataBytes(): ByteArray {
        val data = byteArrayOf(state.toByte())
        return data.plus(addrType.toByte())
            .plus(addr.length.toByte())
            .plus(addr.toByteArray(Charset.defaultCharset()))
            .plus(int2ByteArray(port))
    }

    override fun toString(): String {
        return """
            Bp2wServer : 
            bytes : ${bytesToHex(bytes)}
            getDataBytes : ${bytesToHex(getDataBytes())}
            state : $state
            addrType : $addrType
            addrLen : $addrLen
            addr : $addr
            port : $port
        """.trimIndent()
    }
}
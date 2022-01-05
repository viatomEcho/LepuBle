package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.toString
import com.lepu.blepro.utils.toUInt

class Watch4gServer(var data: ByteArray) {
    var state: Int
    var serverAddrType: Int
    var len: Int
    var addr: String
    var port: Int

    init {
        state = data[1].toInt()
        serverAddrType = data[2].toInt()
        len = data[3].toInt()
        addr = toString(data.copyOfRange(4, 4+len))
        port = toUInt(data.copyOfRange(4+len, 6+len))

    }

    override fun toString(): String {
        val string = """
            Watch4gServer
            state: $state
            serverAddrType: $serverAddrType
            len: $len
            addr: $addr
            port: $port
        """.trimIndent()
        return string
    }
}
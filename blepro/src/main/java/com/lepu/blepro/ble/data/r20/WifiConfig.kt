package com.lepu.blepro.ble.data.r20

import com.lepu.blepro.utils.bytesToHex

class WifiConfig() {

    var bytes = byteArrayOf(0)
    var length: Int = 0
    var option: Int = 0

    lateinit var wifi: Wifi
    lateinit var server: Server

    constructor(bytes: ByteArray) : this() {
        this.bytes = bytes
        var index = 0
        option = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        wifi = Wifi(index, bytes)
        index += wifi.length
        server = Server(index, bytes)
        index += server.length
        length = index
    }

    fun getDataBytes(): ByteArray {
        val data = byteArrayOf(option.toByte())
        return data.plus(wifi.getDataBytes())
            .plus(server.getDataBytes())
    }

    override fun toString(): String {
        return """
            WifiConfig : 
            bytes : ${bytesToHex(bytes)}
            getDataBytes : ${bytesToHex(getDataBytes())}
            length : $length
            option : $option
            wifi : $wifi
            server : $server
        """.trimIndent()
    }

}
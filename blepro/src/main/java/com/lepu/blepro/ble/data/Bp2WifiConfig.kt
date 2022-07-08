package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.bytesToHex

class Bp2WifiConfig() {

    var bytes = byteArrayOf(0)
    var length: Int = 0
    var option: Int = 0

    lateinit var wifi: Bp2Wifi
    lateinit var server: Bp2Server

    constructor(bytes: ByteArray) : this() {
        this.bytes = bytes
        var index = 0
        option = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        wifi = Bp2Wifi(index, bytes)
        index += wifi.length
        server = Bp2Server(index, bytes)
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
            Bp2WifiConfig : 
            bytes : ${bytesToHex(bytes)}
            getDataBytes : ${bytesToHex(getDataBytes())}
            length : $length
            option : $option
            wifi : $wifi
            server : $server
        """.trimIndent()
    }

}
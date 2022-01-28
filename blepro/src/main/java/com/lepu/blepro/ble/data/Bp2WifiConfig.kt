package com.lepu.blepro.ble.data

class Bp2WifiConfig() {

    var length: Int = 0
    var option: Int = 0

    lateinit var wifi: Bp2Wifi
    lateinit var server: Bp2wServer

    constructor(bytes: ByteArray) : this() {
        var index = 0
        option = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        wifi = Bp2Wifi(index, bytes)
        index += wifi.length
        server = Bp2wServer(index, bytes)
        index += server.length
        length = index
    }

    fun getDataBytes(): ByteArray {
        var data = ByteArray(length)
        return data.plus(option.toByte())
            .plus(wifi.getDataBytes())
            .plus(server.getDataBytes())
    }

    override fun toString(): String {
        return """
            length : $length
            option : $option
            wifi : $wifi
            server : $server
        """.trimIndent()
    }

}
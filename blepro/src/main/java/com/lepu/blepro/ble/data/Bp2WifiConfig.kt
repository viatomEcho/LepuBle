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
        when (option) {
            1 -> {
                wifi = Bp2Wifi(index, bytes)
                index += wifi.length
            }
            2, 8 -> {
                server = Bp2Server(index, bytes)
                index += server.length
            }
            3 -> {
                wifi = Bp2Wifi(index, bytes)
                index += wifi.length
                server = Bp2Server(index, bytes)
                index += server.length
            }
        }
        length = index
    }

    fun getDataBytes(): ByteArray {
        val data = byteArrayOf(option.toByte())
        if (isWifiInitialized() && isServerInitialized()) {
            return data.plus(wifi.getDataBytes())
                .plus(server.getDataBytes())
        } else if (isWifiInitialized()) {
            return data.plus(wifi.getDataBytes())
        } else if (isServerInitialized()) {
            return data.plus(server.getDataBytes())
        }
        return data
    }

    fun isServerInitialized(): Boolean {
        return this::server.isInitialized
    }
    fun isWifiInitialized(): Boolean {
        return this::wifi.isInitialized
    }

    override fun toString(): String {
        if (isWifiInitialized() && isServerInitialized()) {
            return """
                Bp2WifiConfig : 
                bytes : ${bytesToHex(bytes)}
                getDataBytes : ${bytesToHex(getDataBytes())}
                length : $length
                option : $option
                wifi : $wifi
                server : $server
            """.trimIndent()
        } else if (isWifiInitialized()) {
            return """
                Bp2WifiConfig : 
                bytes : ${bytesToHex(bytes)}
                getDataBytes : ${bytesToHex(getDataBytes())}
                length : $length
                option : $option
                wifi : $wifi
            """.trimIndent()
        } else if (isServerInitialized()) {
            return """
                Bp2WifiConfig : 
                bytes : ${bytesToHex(bytes)}
                getDataBytes : ${bytesToHex(getDataBytes())}
                length : $length
                option : $option
                server : $server
            """.trimIndent()
        }
        return """
            Bp2WifiConfig : 
            bytes : ${bytesToHex(bytes)}
            getDataBytes : ${bytesToHex(getDataBytes())}
            length : $length
            option : $option
        """.trimIndent()
    }

}
package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.bytesToHex

class Bp2WifiDevice(val bytes: ByteArray) {

    var num: Int

    var wifiList = mutableListOf<Bp2Wifi>()

    init {
        var index = 0
        num = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        for (i in 0 until num) {
            val wifi = Bp2Wifi(index, bytes)
            wifiList.add(wifi)
            index += wifi.length

        }
    }

    override fun toString(): String {
        return """
            Bp2WifiDevice : 
            bytes : ${bytesToHex(bytes)}
            num : $num
            wifiList : $wifiList
        """.trimIndent()
    }

}
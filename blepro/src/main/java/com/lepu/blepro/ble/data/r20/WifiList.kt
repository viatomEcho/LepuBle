package com.lepu.blepro.ble.data.r20

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex

class WifiList(val bytes: ByteArray) {

    var num: Int
    var list = mutableListOf<Wifi>()

    init {
        var index = 0
        num = byte2UInt(bytes[index])
        index++
        for (i in 0 until num) {
            val wifi = Wifi(index, bytes)
            list.add(wifi)
            index += wifi.length
        }
    }

    override fun toString(): String {
        return """
            WifiList : 
            bytes : ${bytesToHex(bytes)}
            num : $num
            list : $list
        """.trimIndent()
    }

}
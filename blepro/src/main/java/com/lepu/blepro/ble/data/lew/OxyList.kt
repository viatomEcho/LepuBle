package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.DateUtil
import com.lepu.blepro.utils.DateUtil.stringFromDate
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import com.lepu.blepro.utils.toLong
import java.util.*

class OxyList(val listSize: Int, val bytes: ByteArray) {

    var items = mutableListOf<Item>()

    init {
        var index = 0
        for (i in 0 until listSize) {
            items.add(Item(bytes.copyOfRange(index+i*8, index+(i+1)*8)))
        }
    }

    override fun toString(): String {
        return """
            OxyList : 
            bytes : ${bytesToHex(bytes)}
            listSize : $listSize
            items : $items
        """.trimIndent()
    }

    class Item(val bytes: ByteArray) {
        var recordingTime: Long
        var pr: Int
        var spo2: Int
        // reserved 1
        init {
            var index = 0
            recordingTime = toLong(bytes.copyOfRange(index, index + 4)) - DateUtil.getTimeZoneOffset().div(1000)
            index += 4
            pr = toUInt(bytes.copyOfRange(index, index + 2))
            index += 2
            spo2 = byte2UInt(bytes[index])
        }
        override fun toString(): String {
            return """
            Item : 
            bytes : ${bytesToHex(bytes)}
            recordingTime : $recordingTime
            recordingTimeStr : ${stringFromDate(Date(recordingTime * 1000L), "yyyy-MM-dd HH:mm:ss")}
            pr : $pr
            spo2 : $spo2
        """.trimIndent()
        }
    }
}
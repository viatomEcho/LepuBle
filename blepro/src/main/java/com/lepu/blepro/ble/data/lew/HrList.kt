package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.DateUtil.stringFromDate
import com.lepu.blepro.utils.*
import java.util.*

class HrList(val listSize: Int, val bytes: ByteArray) {

    var items = mutableListOf<Item>()

    init {
        var index = 0
        for (i in 0 until listSize) {
            items.add(Item(bytes.copyOfRange(index+i*6, index+(i+1)*6)))
        }
    }

    override fun toString(): String {
        return """
            HrList : 
            bytes : ${bytesToHex(bytes)}
            listSize : $listSize
            items : $items
        """.trimIndent()
    }

    class Item(val bytes: ByteArray) {
        var recordingTime: Long
        var hr: Int
        init {
            var index = 0
            recordingTime = toLong(bytes.copyOfRange(index, index + 4))
            index += 4
            hr = toUInt(bytes.copyOfRange(index, index + 2))
        }
        override fun toString(): String {
            return """
            Item : 
            bytes : ${bytesToHex(bytes)}
            recordingTime : $recordingTime
            recordingTimeStr : ${stringFromDate(Date(recordingTime * 1000), "yyyy-MM-dd HH:mm:ss")}
            hr : $hr
        """.trimIndent()
        }
    }
}
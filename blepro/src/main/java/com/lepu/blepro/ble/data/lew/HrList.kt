package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.DateUtil.stringFromDate
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import java.util.*

class HrList(val bytes: ByteArray) {

    var leftSize: Int
    var currentSize: Int
    var items = mutableListOf<Item>()

    init {
        var index = 0
        leftSize = byte2UInt(bytes[index])
        index++
        currentSize = byte2UInt(bytes[index])
        index++
        for (i in 0 until currentSize) {
            items.add(Item(bytes.copyOfRange(index+i*6, index+(i+1)*6)))
        }
    }

    override fun toString(): String {
        return """
            HrList : 
            bytes : ${bytesToHex(bytes)}
            leftSize : $leftSize
            currentSize : $currentSize
            items : $items
        """.trimIndent()
    }

    class Item(val bytes: ByteArray) {
        var recordingTime: Int
        var hr: Int
        init {
            var index = 0
            recordingTime = toUInt(bytes.copyOfRange(index, index + 4))
            index += 4
            hr = toUInt(bytes.copyOfRange(index, index + 2))
        }
        override fun toString(): String {
            return """
            Item : 
            bytes : ${bytesToHex(bytes)}
            recordingTime : $recordingTime
            recordingTimeStr : ${stringFromDate(Date(recordingTime * 1000L), "yyyy-MM-dd HH:mm:ss")}
            hr : $hr
        """.trimIndent()
        }
    }
}
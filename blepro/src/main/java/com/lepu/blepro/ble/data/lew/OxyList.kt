package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt

class OxyList(val bytes: ByteArray) {

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
            items.add(Item(bytes.copyOfRange(index+i*8, index+(i+1)*8)))
        }
    }

    override fun toString(): String {
        return """
            OxyList : 
            bytes : ${bytesToHex(bytes)}
            leftSize : $leftSize
            currentSize : $currentSize
            items : $items
        """.trimIndent()
    }

    class Item(val bytes: ByteArray) {
        var recordingTime: Int
        var pr: Int
        var spo2: Int
        // reserved 1
        init {
            var index = 0
            recordingTime = toUInt(bytes.copyOfRange(index, index + 4))
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
            pr : $pr
            spo2 : $spo2
        """.trimIndent()
        }
    }
}
package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.DateUtil.stringFromDate
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import java.nio.charset.StandardCharsets
import java.util.*

class EcgList(val bytes: ByteArray) {

    var leftSize: Int
    var currentSize: Int
    var items = mutableListOf<Item>()

    init {
        var index = 0
        leftSize = byte2UInt(bytes[index])
        index++
        currentSize = byte2UInt(bytes[index])
        index++
        while (index != bytes.size) {
            val len = byte2UInt(bytes[index+4])
            items.add(Item(bytes.copyOfRange(index, index+4+1+len)))
            index += 4+1+len
        }
    }

    override fun toString(): String {
        return """
            EcgList : 
            bytes : ${bytesToHex(bytes)}
            leftSize : $leftSize
            currentSize : $currentSize
            items : $items
        """.trimIndent()
    }

    class Item(val bytes: ByteArray) {
        var recordingTime: Int
        var nameLen: Int
        var name: String
        init {
            var index = 0
            recordingTime = toUInt(bytes.copyOfRange(index, index + 4))
            index += 4
            nameLen = byte2UInt(bytes[index])
            index++
//            name = trimStr(String(bytes.copyOfRange(index, index+nameLen), StandardCharsets.US_ASCII))
            name = trimStr(String(bytes.copyOfRange(index, index+nameLen), StandardCharsets.UTF_8))
        }
        override fun toString(): String {
            return """
            Item : 
            bytes : ${bytesToHex(bytes)}
            recordingTime : $recordingTime
            recordingTimeStr : ${stringFromDate(Date(recordingTime * 1000L), "yyyy-MM-dd HH:mm:ss")}
            nameLen : $nameLen
            name : $name
        """.trimIndent()
        }
    }
}
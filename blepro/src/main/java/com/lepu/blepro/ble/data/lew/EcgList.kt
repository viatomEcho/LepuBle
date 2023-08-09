package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.DateUtil.stringFromDate
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.*
import java.nio.charset.StandardCharsets
import java.util.*

class EcgList(val listSize: Int, val bytes: ByteArray) {

    var items = mutableListOf<Item>()

    init {
        var index = 0
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
            listSize : $listSize
            items : $items
        """.trimIndent()
    }

    class Item(val bytes: ByteArray) {
        var recordingTime: Long
        var nameLen: Int
        var name: String
        init {
            var index = 0
            recordingTime = toLong(bytes.copyOfRange(index, index + 4))
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
            recordingTimeStr : ${stringFromDate(Date(recordingTime * 1000), "yyyy-MM-dd HH:mm:ss")}
            nameLen : $nameLen
            name : $name
        """.trimIndent()
        }
    }
}
package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.DateUtil.stringFromDate
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import java.util.*

class SleepList(val bytes: ByteArray) {

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
            val len = byte2UInt(bytes[index+10])
            items.add(Item(bytes.copyOfRange(index, index+len*3+11)))
            index += len*3 + 11
        }
    }

    override fun toString(): String {
        return """
            SleepList : 
            bytes : ${bytesToHex(bytes)}
            leftSize : $leftSize
            currentSize : $currentSize
            items : $items
        """.trimIndent()
    }

    class Item(val bytes: ByteArray) {
        var startTime: Int
        var stopTime: Int
        // reserved 2
        var len: Int
        var datas = mutableListOf<Sleep>()
        init {
            var index = 0
            startTime = toUInt(bytes.copyOfRange(index, index + 4))
            index += 4
            stopTime = toUInt(bytes.copyOfRange(index, index + 4))
            index += 4
            index += 2
            len = byte2UInt(bytes[index])
            index++
            for (i in 0 until len) {
                datas.add(Sleep(bytes.copyOfRange(index+i*3, index+(i+1)*3)))
            }
        }
        override fun toString(): String {
            return """
                Item : 
                bytes : ${bytesToHex(bytes)}
                startTime : $startTime
                startTimeStr : ${stringFromDate(Date(startTime * 1000L), "yyyy-MM-dd HH:mm:ss")}
                stopTime : $stopTime
                stopTimeStr : ${stringFromDate(Date(stopTime * 1000L), "yyyy-MM-dd HH:mm:ss")}
                len : $len
                datas : $datas
            """.trimIndent()
        }
    }

    class Sleep(val bytes: ByteArray) {
        var type: Int      // 睡眠阶段类型 LewBleCmd.SleepType
        var duration: Int  // 睡眠阶段时长
        init {
            var index = 0
            type = byte2UInt(bytes[index])
            index++
            duration = toUInt(bytes.copyOfRange(index, index+2))
        }
        override fun toString(): String {
            return """
                Sleep : 
                bytes : ${bytesToHex(bytes)}
                type : $type
                duration : $duration
            """.trimIndent()
        }
    }

}
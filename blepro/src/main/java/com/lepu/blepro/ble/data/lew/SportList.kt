package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.DateUtil
import com.lepu.blepro.utils.DateUtil.stringFromDate
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toLong
import com.lepu.blepro.utils.toUInt
import java.util.*

class SportList(val listSize: Int, val bytes: ByteArray) {

    var items = mutableListOf<Item>()

    init {
        var index = 0
        for (i in 0 until listSize) {
            items.add(Item(bytes.copyOfRange(index+i*27, index+(i+1)*27)))
        }
    }

    override fun toString(): String {
        return """
            SportList : 
            bytes : ${bytesToHex(bytes)}
            listSize : $listSize
            items : $items
        """.trimIndent()
    }

    class Item(val bytes: ByteArray) {
        var type: Int       // LewBleCmd.SportType
        var startTime: Long
        var stopTime: Long
        var distance: Int
        var averHr: Int
        var maxHr: Int
        var steps: Int
        var calories: Int
        var averSpeed: Int
        var maxSpeed: Int
        init {
            var index = 0
            type = byte2UInt(bytes[index])
            index++
            startTime = toLong(bytes.copyOfRange(index, index + 4)) - DateUtil.getTimeZoneOffset().div(1000)
            index += 4
            stopTime = toLong(bytes.copyOfRange(index, index + 4)) - DateUtil.getTimeZoneOffset().div(1000)
            index += 4
            distance = toUInt(bytes.copyOfRange(index, index + 4))
            index += 4
            averHr = byte2UInt(bytes[index])
            index++
            maxHr = byte2UInt(bytes[index])
            index++
            steps = toUInt(bytes.copyOfRange(index, index + 4))
            index += 4
            calories = toUInt(bytes.copyOfRange(index, index + 4))
            index += 4
            averSpeed = toUInt(bytes.copyOfRange(index, index + 2))
            index += 2
            maxSpeed = toUInt(bytes.copyOfRange(index, index + 2))
        }
        override fun toString(): String {
            return """
            Item : 
            bytes : ${bytesToHex(bytes)}
            type : $type
            startTime : $startTime
            startTimeStr : ${stringFromDate(Date(startTime * 1000L), "yyyy-MM-dd HH:mm:ss")}
            stopTime : $stopTime
            stopTimeStr : ${stringFromDate(Date(stopTime * 1000L), "yyyy-MM-dd HH:mm:ss")}
            distance : $distance
            averHr : $averHr
            maxHr : $maxHr
            steps : $steps
            calories : $calories
            averSpeed : $averSpeed
            maxSpeed : $maxSpeed
        """.trimIndent()
        }
    }
}
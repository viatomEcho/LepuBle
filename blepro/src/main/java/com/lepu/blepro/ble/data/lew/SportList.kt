package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt

class SportList(val bytes: ByteArray) {

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
            items.add(Item(bytes.copyOfRange(index+i*27, index+(i+1)*27)))
        }
    }

    override fun toString(): String {
        return """
            SportList : 
            bytes : ${bytesToHex(bytes)}
            leftSize : $leftSize
            currentSize : $currentSize
            items : $items
        """.trimIndent()
    }

    class Item(val bytes: ByteArray) {
        var type: Int       // LewBleCmd.SportType
        var startTime: Int
        var stopTime: Int
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
            startTime = toUInt(bytes.copyOfRange(index, index + 4))
            index += 4
            stopTime = toUInt(bytes.copyOfRange(index, index + 4))
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
            stopTime : $stopTime
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
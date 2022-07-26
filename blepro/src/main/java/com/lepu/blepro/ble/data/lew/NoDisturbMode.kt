package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex

class NoDisturbMode() {

    var switch = false
    var itemSize = 0
    var items = mutableListOf<Item>()  // 5

    constructor(bytes: ByteArray) : this() {
        var index = 0
        switch = byte2UInt(bytes[index]) == 1
        index++
        itemSize = byte2UInt(bytes[index])
        index++
        for (i in 0 until itemSize) {
            items.add(Item(bytes.copyOfRange(index + i*4, index + (i+1)*4)))
        }
    }

    fun getDataBytes(): ByteArray {
        val on = if (switch) {
            1
        } else {
            0
        }
        itemSize = items.size
        var itemsTemp = ByteArray(0)
        for (i in 0 until itemSize) {
            itemsTemp = itemsTemp.plus(items[i].getDataBytes())
        }
        return byteArrayOf(on.toByte())
            .plus(itemSize.toByte())
            .plus(itemsTemp)
    }

    override fun toString(): String {
        return """
            NoDisturbMode : 
            bytes : ${bytesToHex(getDataBytes())}
            switch : $switch
            itemSize : $itemSize
            items : $items
        """.trimIndent()
    }

    class Item() {
        var startHour = 0
        var startMin = 0
        var stopHour = 0
        var stopMin = 0
        constructor(bytes: ByteArray) : this() {
            var index = 0
            startHour = byte2UInt(bytes[index])
            index++
            startMin = byte2UInt(bytes[index])
            index++
            stopHour = byte2UInt(bytes[index])
            index++
            stopMin = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(startHour.toByte())
                .plus(startMin.toByte())
                .plus(stopHour.toByte())
                .plus(stopMin.toByte())
        }
        override fun toString(): String {
            return """
                Item : 
                bytes : ${bytesToHex(getDataBytes())}
                startHour : $startHour
                startMin : $startMin
                stopHour : $stopHour
                stopMin : $stopMin
            """.trimIndent()
        }
    }

}
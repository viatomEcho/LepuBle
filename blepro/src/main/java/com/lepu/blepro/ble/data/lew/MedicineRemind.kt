package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex
import java.nio.charset.StandardCharsets

class MedicineRemind() {

    var itemSize = 0
    var items = mutableListOf<Item>()

    constructor(bytes: ByteArray) : this() {
        var index = 0
        itemSize = byte2UInt(bytes[index])
        index++
        for (i in 0 until itemSize) {
            items.add(Item(bytes.copyOfRange(index + i*5, index + (i+1)*5)))
        }
    }

    fun getDataBytes(): ByteArray {
        itemSize = items.size
        val itemsTemp = ByteArray(0)
        for (i in 0 until itemSize) {
            itemsTemp.plus(items[i].getDataBytes())
        }
        return byteArrayOf(itemSize.toByte())
            .plus(itemsTemp)
    }

    override fun toString(): String {
        return """
            MedicineRemind : 
            bytes : ${bytesToHex(getDataBytes())}
            itemSize : $itemSize
            items : $items
        """.trimIndent()
    }

    class Item() {
        var type = 0              // 0:普通提醒闹钟, 1:智能提醒闹钟 ?????
        var weekRepeat = 0        // 0x7F 设备会仅闹⼀次后再将闹钟值删除
        var repeat = false
        var everySunday = false
        var everyMonday = false
        var everyTuesday = false
        var everyWednesday = false
        var everyThursday = false
        var everyFriday = false
        var everySaturday = false
        var hour = 0
        var minute = 0
        var switch = false
        var nameLen = 0
        var name = ""
        constructor(bytes: ByteArray) : this() {
            var index = 0
            type = byte2UInt(bytes[index])
            index++
            weekRepeat = byte2UInt(bytes[index])
            repeat = ((weekRepeat and 0x80) shr 7) == 1
            everySunday = (weekRepeat and 0x01) == 1
            everyMonday = ((weekRepeat and 0x02) shr 1) == 1
            everyTuesday = ((weekRepeat and 0x04) shr 2) == 1
            everyWednesday = ((weekRepeat and 0x08) shr 3) == 1
            everyThursday = ((weekRepeat and 0x10) shr 4) == 1
            everyFriday = ((weekRepeat and 0x20) shr 5) == 1
            everySaturday = ((weekRepeat and 0x40) shr 6) == 1
            index++
            hour = byte2UInt(bytes[index])
            index++
            minute = byte2UInt(bytes[index])
            index++
            switch = byte2UInt(bytes[index]) == 1
            index++
            nameLen = byte2UInt(bytes[index])
            index++
//            name = trimStr(String(bytes.copyOfRange(index, index + nameLen), StandardCharsets.US_ASCII))
            name = trimStr(String(bytes.copyOfRange(index, index + nameLen), StandardCharsets.UTF_8))
        }
        fun getDataBytes(): ByteArray {
            if (!repeat) {
                everySunday = true
                everyMonday = true
                everyTuesday = true
                everyWednesday = true
                everyThursday = true
                everyFriday = true
                everySaturday = true
            }
            if (everySunday) {
                weekRepeat = weekRepeat or 0x01
            }
            if (everyMonday) {
                weekRepeat = weekRepeat or 0x02
            }
            if (everyTuesday) {
                weekRepeat = weekRepeat or 0x04
            }
            if (everyWednesday) {
                weekRepeat = weekRepeat or 0x08
            }
            if (everyThursday) {
                weekRepeat = weekRepeat or 0x10
            }
            if (everyFriday) {
                weekRepeat = weekRepeat or 0x20
            }
            if (everySaturday) {
                weekRepeat = weekRepeat or 0x40
            }
            if (repeat) {
                weekRepeat = weekRepeat or 0x80
            }
            val on = if (switch) {
                1
            } else {
                0
            }
//            val nameData = name.toByteArray(StandardCharsets.US_ASCII)
            val nameData = name.toByteArray(StandardCharsets.UTF_8)
            nameLen = nameData.size
            return byteArrayOf(type.toByte())
                .plus(weekRepeat.toByte())
                .plus(hour.toByte())
                .plus(minute.toByte())
                .plus(on.toByte())
                .plus(nameLen.toByte())
                .plus(nameData)
        }
        override fun toString(): String {
            return """
                Item : 
                bytes : ${bytesToHex(getDataBytes())}
                type : $type
                weekRepeat : $weekRepeat
                repeat : $repeat
                everySunday : $everySunday
                everyMonday : $everyMonday
                everyTuesday : $everyTuesday
                everyWednesday : $everyWednesday
                everyThursday : $everyThursday
                everyFriday : $everyFriday
                everySaturday : $everySaturday
                hour : $hour
                minute : $minute
                switch : $switch
                nameLen : $nameLen
                name : $name
            """.trimIndent()
        }
    }
}
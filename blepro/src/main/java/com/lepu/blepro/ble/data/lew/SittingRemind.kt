package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex

class SittingRemind() {

    var switch = false
    var noonSwitch = false
    var weekRepeat = 0
    var everySunday = false
    var everyMonday = false
    var everyTuesday = false
    var everyWednesday = false
    var everyThursday = false
    var everyFriday = false
    var everySaturday = false
    var startHour = 0
    var startMin = 0
    var stopHour = 0
    var stopMin = 0

    constructor(bytes: ByteArray) : this() {
        var index = 0
        switch = byte2UInt(bytes[index]) == 1
        index++
        noonSwitch = byte2UInt(bytes[index]) == 1
        index++
        weekRepeat = byte2UInt(bytes[index])
        everySunday = (weekRepeat and 0x01) == 1
        everyMonday = ((weekRepeat and 0x02) shr 1) == 1
        everyTuesday = ((weekRepeat and 0x04) shr 2) == 1
        everyWednesday = ((weekRepeat and 0x08) shr 3) == 1
        everyThursday = ((weekRepeat and 0x10) shr 4) == 1
        everyFriday = ((weekRepeat and 0x20) shr 5) == 1
        everySaturday = ((weekRepeat and 0x40) shr 6) == 1
        index++
        startHour = byte2UInt(bytes[index])
        index++
        startMin = byte2UInt(bytes[index])
        index++
        stopHour = byte2UInt(bytes[index])
        index++
        stopMin = byte2UInt(bytes[index])
    }

    fun getDataBytes(): ByteArray {
        val on = if (switch) {
            1
        } else {
            0
        }
        val onNoon = if (noonSwitch) {
            1
        } else {
            0
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
        return byteArrayOf(on.toByte())
            .plus(onNoon.toByte())
            .plus(weekRepeat.toByte())
            .plus(startHour.toByte())
            .plus(startMin.toByte())
            .plus(stopHour.toByte())
            .plus(stopMin.toByte())
    }

    override fun toString(): String {
        return """
            SittingRemind : 
            bytes : ${bytesToHex(getDataBytes())}
            switch : $switch
            noonSwitch : $noonSwitch
            weekRepeat : $weekRepeat
            everySunday : $everySunday
            everyMonday : $everyMonday
            everyTuesday : $everyTuesday
            everyWednesday : $everyWednesday
            everyThursday : $everyThursday
            everyFriday : $everyFriday
            everySaturday : $everySaturday
            startHour : $startHour
            startMin : $startMin
            stopHour : $stopHour
            stopMin : $stopMin
        """.trimIndent()
    }

}
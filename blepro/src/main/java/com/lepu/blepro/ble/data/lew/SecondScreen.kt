package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex

class SecondScreen() {

    var medicineRemind = false  // 用药提醒
    var weather = false         // 天气
    var clock = false           // 闹钟
    var heartRate = false       // 心率值
    var spo2 = false            // 血氧值
    var peripherals = false     // 外设

    constructor(bytes: ByteArray) : this() {
        var index = 0
        medicineRemind = byte2UInt(bytes[index]) == 1
        index++
        weather = byte2UInt(bytes[index]) == 1
        index++
        clock = byte2UInt(bytes[index]) == 1
        index++
        heartRate = byte2UInt(bytes[index]) == 1
        index++
        spo2 = byte2UInt(bytes[index]) == 1
        index++
        peripherals = byte2UInt(bytes[index]) == 1
    }

    fun getDataBytes(): ByteArray {
        val medicineRemindOn = if (medicineRemind) {
            1
        } else {
            0
        }
        val weatherOn = if (weather) {
            1
        } else {
            0
        }
        val clockOn = if (clock) {
            1
        } else {
            0
        }
        val heartRateOn = if (heartRate) {
            1
        } else {
            0
        }
        val spo2On = if (spo2) {
            1
        } else {
            0
        }
        val peripheralsOn = if (peripherals) {
            1
        } else {
            0
        }
        return byteArrayOf(medicineRemindOn.toByte())
            .plus(weatherOn.toByte())
            .plus(clockOn.toByte())
            .plus(heartRateOn.toByte())
            .plus(spo2On.toByte())
            .plus(peripheralsOn.toByte())
    }

    override fun toString(): String {
        return """
            SecondScreen : 
                bytes : ${bytesToHex(getDataBytes())}
                medicineRemind : $medicineRemind
                weather : $weather
                clock : $clock
                heartRate : $heartRate
                spo2 : $spo2
                peripherals : $peripherals
            """.trimIndent()
    }

}
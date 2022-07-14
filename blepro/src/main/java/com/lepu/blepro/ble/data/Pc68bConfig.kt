package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex

class Pc68bConfig() {

    var bytes = byteArrayOf(0)
    var alert: Boolean = false        // 报警功能关闭/开启；主要包含血氧过低警报、脉率过高或过低警报
    var spo2Lo: Int = 85              // 血氧过低阈值
    var prLo: Int = 25                // 脉率过低阈值
    var prHi: Int = 100               // 脉率过高阈值
    var pulseBeep: Boolean = false    // 搏动音关闭/开启
    var sensorAlert: Boolean = false  // 脱落警示关闭/开启

    constructor(bytes: ByteArray) : this() {
        this.bytes = bytes
        var index = 0
        alert = byte2UInt(bytes[index]) == 1
        index++
        spo2Lo = byte2UInt(bytes[index])
        index++
        prLo = byte2UInt(bytes[index])
        index++
        prHi = byte2UInt(bytes[index])
        index++
        pulseBeep = byte2UInt(bytes[index]) == 1
        index++
        sensorAlert = byte2UInt(bytes[index]) == 1
    }

    fun getDataBytes(): ByteArray {
        val alertOn = if (alert) {
            1
        } else {
            0
        }
        val pulseBeepOn = if (pulseBeep) {
            1
        } else {
            0
        }
        val sensorAlertOn = if (sensorAlert) {
            1
        } else {
            0
        }
        return byteArrayOf(alertOn.toByte())
            .plus(spo2Lo.toByte())
            .plus(prLo.toByte())
            .plus(prHi.toByte())
            .plus(pulseBeepOn.toByte())
            .plus(sensorAlertOn.toByte())
    }

    override fun toString(): String {
        return """
            Pc68bConfig:
            bytes : ${bytesToHex(bytes)}
            getDataBytes : ${bytesToHex(getDataBytes())}
            alert : $alert
            spo2Lo : $spo2Lo
            prLo : $prLo
            prHi : $prHi
            pulseBeep : $pulseBeep
            sensorAlert : $sensorAlert
        """.trimIndent()
    }
}
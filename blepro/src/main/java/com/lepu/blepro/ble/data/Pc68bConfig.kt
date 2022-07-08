package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex

class Pc68bConfig() {

    var bytes = byteArrayOf(0)
    var alert: Boolean = false
    var spo2Lo: Int = 85
    var prLo: Int = 25
    var prHi: Int = 100
    var pulseBeep: Boolean = false
    var sensorAlert: Boolean = false

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
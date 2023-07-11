package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex

class Er1Config() {

    var bytes = ByteArray(0)
    var hrSwitch = false    // 心率震动开关
    var batSwitch = false   // 低电量震动开关
    var waveSwitch = false  // 波形判断失败震动开关
    var hr1 = 0             // 阈值1 每十秒震动一次
    var hr2 = 0             // 阈值2 每二秒震动一次

    constructor(bytes: ByteArray) : this() {
        this.bytes = bytes
        var index = 0
        hrSwitch = (byte2UInt(bytes[index]) and 0x01) == 1
        batSwitch = ((byte2UInt(bytes[index]) and 0x02) shr 1) == 1
        waveSwitch = ((byte2UInt(bytes[index]) and 0x04) shr 2) == 1
        index++
        hr1 = byte2UInt(bytes[index])
        index++
        hr2 = byte2UInt(bytes[index])
    }

    fun getDataBytes(): ByteArray {
        var temp = 0
        if (hrSwitch) {
            temp = temp or 0x01
        }
        if (batSwitch) {
            temp = temp or 0x02
        }
        if (waveSwitch) {
            temp = temp or 0x04
        }
        return byteArrayOf(temp.toByte())
            .plus(hr1.toByte())
            .plus(hr2.toByte())
    }

    override fun toString(): String {
        return """
            Er1Config : 
            bytes : ${bytesToHex(bytes)}
            getDataBytes : ${bytesToHex(getDataBytes())}
            hrSwitch : $hrSwitch
            batSwitch : $batSwitch
            waveSwitch : $waveSwitch
            hr1 : $hr1
            hr2 : $hr2
        """.trimIndent()
    }
}
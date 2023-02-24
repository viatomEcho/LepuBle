package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.toUInt

const val NORMAL = 0
const val CHARGING = 1
const val CHARGED = 2
const val LOW_BATTERY = 3

@ExperimentalUnsignedTypes
public class KtBleBattery constructor(val bytes: ByteArray) {
    var state: Int        // 0：正常使用 1：充电中 2：充满 3：低电量
    var stateMsg: String
    var percent: Int      // 电量百分比 100%
    var vol: Float        // 3.92V

    init {
        var index = 0
        state = byte2UInt(bytes[index])
        stateMsg = getStateMsg(state)
        index++
        percent = byte2UInt(bytes[index])
        index++
        vol = toUInt(bytes.copyOfRange(index, index+2)).toFloat() / 1000
    }

    private fun getStateMsg(state: Int): String {
        return when(state) {
            NORMAL -> "Normal"
            CHARGING -> "Charging"
            CHARGED -> "Charged"
            LOW_BATTERY -> "Low battery"
            else -> ""
        }
    }

    override fun toString(): String {

        var s = "Normal"

        when(state) {
            NORMAL -> s="Normal"
            CHARGING -> s="Charging"
            CHARGED -> s="Charged"
            LOW_BATTERY -> s="Low battery"
        }

        return """
            state: $s
            stateMsg: $stateMsg
            percent: $percent
            voltage: $vol
        """.trimIndent()
    }
}
package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.ble.cmd.LewBleCmd
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt

class BatteryInfo(val bytes: ByteArray) {

    var state: Int         // 电池状态 e.g. 0:正常使⽤ 1:充电中 2:充满 3:低电量 LewBleCmd.BatteryState
    var stateMess: String
    var percent: Int       // 电池状态 e.g. 电池电量百分⽐
    var voltage: Float     // 电池电压(mV) e.g. 3950 : 3.95V

    init {
        var index = 0
        state = byte2UInt(bytes[index])
        stateMess = getStateMess(state)
        index++
        percent = byte2UInt(bytes[index])
        index++
        voltage = toUInt(bytes.copyOfRange(index, index+2)).div(1000f)
    }

    private fun getStateMess(state: Int): String {
        return when (state) {
            LewBleCmd.BatteryState.NORMAL -> "正常使用"
            LewBleCmd.BatteryState.CHARGING -> "充电中"
            LewBleCmd.BatteryState.CHARGED -> "充满"
            LewBleCmd.BatteryState.LOW_BATTERY -> "低电量"
            else -> ""
        }
    }

    override fun toString(): String {
        return """
            BatteryInfo : 
            bytes : ${bytesToHex(bytes)}
            state : $state
            stateMess : $stateMess
            percent : $percent
            voltage : $voltage
        """.trimIndent()
    }
}
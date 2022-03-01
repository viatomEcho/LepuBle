package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.toUInt

class LepuBatteryInfo(bytes: ByteArray) {
    var state: Int    // 电池状态 e.g.   0:正常使用 1:充电中 2:充满 3:低电量
    var percent: Int  // 电池状态 e.g.	电池电量百分比
    var voltage: Float  // 电池电压(mV)	e.g.	3950 : 3.95V

    init {
        var index = 0
        state = byte2UInt(bytes[index])
        index++
        percent = byte2UInt(bytes[index])
        index++
        voltage = toUInt(bytes.copyOfRange(index, index+2)).div(1000f)
    }

    override fun toString(): String {
        return """
            state : $state
            percent : $percent
            voltage : $voltage
        """.trimIndent()
    }
}
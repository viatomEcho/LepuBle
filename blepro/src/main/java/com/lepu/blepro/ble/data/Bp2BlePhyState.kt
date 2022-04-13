package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt

class Bp2BlePhyState() {
    var bytes = byteArrayOf(0)
    var leadOff = true     // 电极状态 true电极脱落 false电极连接
    var mode = 0           // 模式:1-6代表6个理疗的模式 设置0代表退出理疗
    var intensy = 0        // 强度:0代表理疗未开始 强度有 1-15个档位
    var remainingTime = 0  // 设置理疗理疗剩余的时间 设置0则选择默认的15分钟 单位秒

    constructor(bytes: ByteArray) : this() {
        this.bytes = bytes
        var index = 0
        leadOff = byte2UInt(bytes[index]) == 0
        index++
        mode = byte2UInt(bytes[index])
        index++
        intensy = byte2UInt(bytes[index])
        index++
        remainingTime = byte2UInt(bytes[index])
    }

    fun getDataBytes(): ByteArray {
        return byteArrayOf(mode.toByte())
            .plus(intensy.toByte())
            .plus(remainingTime.toByte())
            .plus(ByteArray(8))
    }

    override fun toString(): String {
        return """
            leadOff : $leadOff
            mode : $mode
            intensy : $intensy
            remainingTime : $remainingTime
        """.trimIndent()
    }
}
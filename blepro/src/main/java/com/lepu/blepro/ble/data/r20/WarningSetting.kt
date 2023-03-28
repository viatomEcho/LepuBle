package com.lepu.blepro.ble.data.r20

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.int2ByteArray
import com.lepu.blepro.utils.toUInt

class WarningSetting() {

    var type = 0
    // reserved 3
    var warningApnea = WarningApnea()
    var warningLeak = WarningLeak()
    var warningVt = WarningVt()
    var warningVentilation = WarningVentilation()
    var warningRrHigh = WarningRrHigh()
    var warningRrLow = WarningRrLow()
    var warningSpo2Low = WarningSpo2Low()
    var warningHrHigh = WarningHrHigh()
    var warningHrLow = WarningHrLow()

    constructor(bytes: ByteArray) : this() {
        var index = 0
        warningApnea = WarningApnea(bytes.copyOfRange(index, index+1))
        index++
        warningLeak = WarningLeak(bytes.copyOfRange(index, index+1))
        index++
        warningVt = WarningVt(bytes.copyOfRange(index, index+1))
        index++
        warningVentilation = WarningVentilation(bytes.copyOfRange(index, index+1))
        index++
        warningRrHigh = WarningRrHigh(bytes.copyOfRange(index, index+1))
        index++
        warningRrLow = WarningRrLow(bytes.copyOfRange(index, index+1))
        index++
        warningSpo2Low = WarningSpo2Low(bytes.copyOfRange(index, index+1))
        index++
        warningHrHigh = WarningHrHigh(bytes.copyOfRange(index, index+1))
        index++
        warningHrLow = WarningHrLow(bytes.copyOfRange(index, index+1))
    }

    fun getDataBytes(): ByteArray {
        var data = byteArrayOf(type.toByte())
            .plus(ByteArray(3))
        when (type) {
            0 -> {
                data = data.plus(warningApnea.getDataBytes())
                    .plus(warningLeak.getDataBytes())
                    .plus(warningVt.getDataBytes())
                    .plus(warningVentilation.getDataBytes())
                    .plus(warningRrHigh.getDataBytes())
                    .plus(warningRrLow.getDataBytes())
                    .plus(warningSpo2Low.getDataBytes())
                    .plus(warningHrHigh.getDataBytes())
                    .plus(warningHrLow.getDataBytes())
            }
            1 -> {
                data = data.plus(warningApnea.getDataBytes())
            }
            2 -> {
                data = data.plus(warningLeak.getDataBytes())
            }
            3 -> {
                data = data.plus(warningVt.getDataBytes())
            }
            4 -> {
                data = data.plus(warningVentilation.getDataBytes())
            }
            5 -> {
                data = data.plus(warningRrHigh.getDataBytes())
            }
            6 -> {
                data = data.plus(warningRrLow.getDataBytes())
            }
            7 -> {
                data = data.plus(warningSpo2Low.getDataBytes())
            }
            8 -> {
                data = data.plus(warningHrHigh.getDataBytes())
            }
            9 -> {
                data = data.plus(warningHrLow.getDataBytes())
            }
        }
        return data
    }

    // 报警提示：呼吸暂停
    class WarningApnea() {
        var apnea = 0  // 0: Off 范围：10s/20s/30s；级别：高
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            apnea = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(apnea.toByte())
                .plus(ByteArray(3))
        }
    }
    // 报警提示：漏气量高
    class WarningLeak() {
        var high = 0  // 0: Off 范围：15s/30s/45s/60s； 级别：中
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            high = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(high.toByte())
                .plus(ByteArray(3))
        }
    }
    // 报警提示：潮气量低
    class WarningVt() {
        var low = 0  // 0: Off 范围：200-2000ml；步进：10ml；级别：中
        // reserved 2
        constructor(bytes: ByteArray) : this() {
            var index = 0
            low = toUInt(bytes.copyOfRange(index, index+2))
        }
        fun getDataBytes(): ByteArray {
            return int2ByteArray(low)
                .plus(ByteArray(2))
        }
    }
    // 报警提示：分钟通气量低
    class WarningVentilation() {
        var low = 0  // 0: off 范围：1-25L/min；步进：1L/min；级别：中
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            low = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(low.toByte())
                .plus(ByteArray(3))
        }
    }
    // 报警提示：呼吸频率高
    class WarningRrHigh() {
        var high = 0  // 0:Off 范围：1-60bmp；步进：1bpm；级别：中
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            high = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(high.toByte())
                .plus(ByteArray(3))
        }
    }
    // 报警提示：呼吸频率低
    class WarningRrLow() {
        var low = 0  // 0:Off 范围：1-60bmp；步进：1bpm；级别：中
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            low = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(low.toByte())
                .plus(ByteArray(3))
        }
    }
    // 报警提示：血氧饱和度低
    class WarningSpo2Low() {
        var low = 0  // 0:Off 范围：80-95%；步进：1%；级别：中
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            low = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(low.toByte())
                .plus(ByteArray(3))
        }
    }
    // 报警提示：脉率/心率高
    class WarningHrHigh() {
        var high = 0  // 0:Off 范围：100-240bmp；步进：10bpm；级别：中
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            high = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(high.toByte())
                .plus(ByteArray(3))
        }
    }
    // 报警提示：脉率/心率低
    class WarningHrLow() {
        var low = 0  // 0:Off 范围：30-70bmp；步进：5bpm；级别：中
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            low = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(low.toByte())
                .plus(ByteArray(3))
        }
    }
}
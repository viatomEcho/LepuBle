package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt

class Pf10Aw1Config() {

    var bytes = ByteArray(0)
    var type = 0
    // reserved 3
    var spo2Low = Spo2Low()          // 血氧阈值 85%-99% 步进：1%
    var prHi = PrHigh()              // 脉搏高阈值 100bpm-240bpm；步进：5bpm
    var prLow = PrLow()              // 脉搏低阈值  30bpm-60bpm；步进：5bpm
    var alarmSwitch = AlarmSwitch()  // 阈值提醒开关 0:关 1：开
    var measureMode = MeasureMode()  // 测量模式 1：点测 2：连续
    var beepSwitch = BeepSwitch()    // 蜂鸣器开关  0:关 1：开
    var language = Language()        // 语言包 0:英文 1：中文
    var bleSwitch = BleSwitch()      // 蓝牙开关 0:关 1：开
    var esMode = EsMode()            // 测量过程，定时息屏，0：常亮，1：1分钟熄屏，2：3分钟熄屏，3：5分钟熄屏
    // reserved 8

    constructor(bytes: ByteArray) : this() {
        this.bytes = bytes
        var index = 0
        spo2Low = Spo2Low(bytes.copyOfRange(index, index+1))
        index++
        prHi = PrHigh(bytes.copyOfRange(index, index+1))
        index++
        prLow = PrLow(bytes.copyOfRange(index, index+1))
        index++
        alarmSwitch = AlarmSwitch(bytes.copyOfRange(index, index+1))
        index++
        measureMode = MeasureMode(bytes.copyOfRange(index, index+1))
        index++
        beepSwitch = BeepSwitch(bytes.copyOfRange(index, index+1))
        index++
        language = Language(bytes.copyOfRange(index, index+1))
        index++
        bleSwitch = BleSwitch(bytes.copyOfRange(index, index+1))
        index++
        esMode = EsMode(bytes.copyOfRange(index, index+1))
    }

    fun getDataBytes() : ByteArray {
        var data = byteArrayOf(type.toByte())
            .plus(ByteArray(3))
        when (type) {
            0 -> {
                data = data.plus(spo2Low.getDataBytes())
                    .plus(prHi.getDataBytes())
                    .plus(prLow.getDataBytes())
                    .plus(alarmSwitch.getDataBytes())
                    .plus(measureMode.getDataBytes())
                    .plus(beepSwitch.getDataBytes())
                    .plus(language.getDataBytes())
                    .plus(bleSwitch.getDataBytes())
                    .plus(esMode.getDataBytes())
            }
            1 -> {
                data = data.plus(spo2Low.getDataBytes())
            }
            2 -> {
                data = data.plus(prHi.getDataBytes())
            }
            3 -> {
                data = data.plus(prLow.getDataBytes())
            }
            4 -> {
                data = data.plus(alarmSwitch.getDataBytes())
            }
            5 -> {
                data = data.plus(measureMode.getDataBytes())
            }
            6 -> {
                data = data.plus(beepSwitch.getDataBytes())
            }
            7 -> {
                data = data.plus(language.getDataBytes())
            }
            8 -> {
                data = data.plus(bleSwitch.getDataBytes())
            }
            9 -> {
                data = data.plus(esMode.getDataBytes())
            }
        }
        return data
    }

    // 血氧提醒阈值
    class Spo2Low() {
        var low = 85  // 85%-99% 步进：1%
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            low = byte2UInt(bytes[index])
        }
        fun getDataBytes() : ByteArray {
            return byteArrayOf(low.toByte())
                .plus(ByteArray(3))
        }
    }
    // 脉率高阈值
    class PrHigh() {
        var hi = 100  // 100bpm-240bpm；步进：5bpm
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            hi = byte2UInt(bytes[index])
        }
        fun getDataBytes() : ByteArray {
            return byteArrayOf(hi.toByte())
                .plus(ByteArray(3))
        }
    }
    // 脉率低阈值
    class PrLow() {
        var low = 30  // 30bpm-60bpm；步进：5bpm
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            low = byte2UInt(bytes[index])
        }
        fun getDataBytes() : ByteArray {
            return byteArrayOf(low.toByte())
                .plus(ByteArray(3))
        }
    }
    // 阈值提醒开关
    class AlarmSwitch() {
        var on = false  // 0:关 1：开
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            on = byte2UInt(bytes[index]) == 1
        }
        fun getDataBytes() : ByteArray {
            val alarmOn = if (on) {
                1
            } else {
                0
            }
            return byteArrayOf(alarmOn.toByte())
                .plus(ByteArray(3))
        }
    }
    // 测量模式
    class MeasureMode() {
        var mode = 2  // 1：点测 2：连续
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            mode = byte2UInt(bytes[index])
        }
        fun getDataBytes() : ByteArray {
            return byteArrayOf(mode.toByte())
                .plus(ByteArray(3))
        }
    }
    // 蜂鸣器开关
    class BeepSwitch() {
        var on = false  // 0:关 1：开
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            on = byte2UInt(bytes[index]) == 1
        }
        fun getDataBytes() : ByteArray {
            val beepOn = if (on) {
                1
            } else {
                0
            }
            return byteArrayOf(beepOn.toByte())
                .plus(ByteArray(3))
        }
    }
    // 语言包
    class Language() {
        var language = 0  // 0:英文 1：中文
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            language = byte2UInt(bytes[index])
        }
        fun getDataBytes() : ByteArray {
            return byteArrayOf(language.toByte())
                .plus(ByteArray(3))
        }
    }
    // 蓝牙开关
    class BleSwitch() {
        var on = false  // 0:关 1：开
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            on = byte2UInt(bytes[index]) == 1
        }
        fun getDataBytes() : ByteArray {
            val bleOn = if (on) {
                1
            } else {
                0
            }
            return byteArrayOf(bleOn.toByte())
                .plus(ByteArray(3))
        }
    }
    // 测量过程，定时息屏
    class EsMode() {
        var mode = 0  // 0：常亮，1：1分钟熄屏，2：3分钟熄屏，3：5分钟熄屏
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            mode = byte2UInt(bytes[index])
        }
        fun getDataBytes() : ByteArray {
            return byteArrayOf(mode.toByte())
                .plus(ByteArray(3))
        }
    }

    override fun toString(): String {
        return """
            Pf10Aw1Config : 
            spo2Low : ${spo2Low.low}
            prHi : ${prHi.hi}
            prLow : ${prLow.low}
            alarmSwitch : ${alarmSwitch.on}
            measureMode : ${measureMode.mode}
            beepSwitch : ${beepSwitch.on}
            language : ${language.language}
            bleSwitch : ${bleSwitch.on}
            esMode : ${esMode.mode}
        """.trimIndent()
    }
}
package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt

class OxyIIConfig() {

    var bytes = ByteArray(0)
    var type = 0
    // reserved 3
    var spo2Switch = Spo2Switch()            // 血氧提醒开关
    var spo2Low = Spo2Low()                  // 血氧阈值 80-95% 步进%1 默认 88%
    var hrSwitch = HrSwitch()                // 心率提醒开关
    var hrLow = HrLow()                      // 心率提醒低阈值 30-70 步进5 默认 50
    var hrHi = HrHigh()                      // 心率提醒高阈值 70-200 步进5 默认 120
    var motor = Motor()                      // 震动强(震动强度不随开关的改变而改变) KidsO2(5/10/17/22/35) Oxylink(5/10/17/22/35)  O2Ring(20/40/60/80/100)
    var buzzer = Buzzer()                    // 声音强度 (checkO2Plus：最低：20，低:40，中：60，高：80，最高：100)
    var displayMode = DisplayMode()          // 显示模式 0:Standard模式 1:Always Off模式2:Always On模式
    var brightnessMode = BrightnessMode()    // 屏幕亮度 0: 息屏 1: 低亮屏 2: 中 3:高
    var storageInterval = StorageInterval()  // 存储间隔
    // reserved 31

    constructor(bytes: ByteArray) : this() {
        this.bytes = bytes
        var index = 0
        spo2Switch = Spo2Switch(bytes.copyOfRange(index, index+1))
        hrSwitch = HrSwitch(bytes.copyOfRange(index, index+1))
        index++
        spo2Low = Spo2Low(bytes.copyOfRange(index, index+1))
        index++
        hrLow = HrLow(bytes.copyOfRange(index, index+1))
        index++
        hrHi = HrHigh(bytes.copyOfRange(index, index+1))
        index++
        motor = Motor(bytes.copyOfRange(index, index+1))
        index++
        buzzer = Buzzer(bytes.copyOfRange(index, index+1))
        index++
        displayMode = DisplayMode(bytes.copyOfRange(index, index+1))
        index++
        brightnessMode = BrightnessMode(bytes.copyOfRange(index, index+1))
        index++
        storageInterval = StorageInterval(bytes.copyOfRange(index, index+1))
    }

    fun getDataBytes() : ByteArray {
        var data = byteArrayOf(type.toByte())
            .plus(ByteArray(3))
        when (type) {
            0 -> {
                data = data.plus(spo2Switch.getDataBytes())
                    .plus(spo2Low.getDataBytes())
                    .plus(hrSwitch.getDataBytes())
                    .plus(hrLow.getDataBytes())
                    .plus(hrHi.getDataBytes())
                    .plus(motor.getDataBytes())
                    .plus(buzzer.getDataBytes())
                    .plus(displayMode.getDataBytes())
                    .plus(brightnessMode.getDataBytes())
                    .plus(storageInterval.getDataBytes())
            }
            1 -> {
                data = data.plus(spo2Switch.getDataBytes())
            }
            2 -> {
                data = data.plus(spo2Low.getDataBytes())
            }
            3 -> {
                data = data.plus(hrSwitch.getDataBytes())
            }
            4 -> {
                data = data.plus(hrLow.getDataBytes())
            }
            5 -> {
                data = data.plus(hrHi.getDataBytes())
            }
            6 -> {
                data = data.plus(motor.getDataBytes())
            }
            7 -> {
                data = data.plus(buzzer.getDataBytes())
            }
            8 -> {
                data = data.plus(displayMode.getDataBytes())
            }
            9 -> {
                data = data.plus(brightnessMode.getDataBytes())
            }
            10 -> {
                data = data.plus(storageInterval.getDataBytes())
            }
        }
        return data
    }

    // 血氧提醒阈值，bit0震动，bit1声音
    class Spo2Switch() {
        var motorOn = false
        var buzzerOn = false
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            motorOn = (byte2UInt(bytes[index]) and 0x01) == 1
            buzzerOn = ((byte2UInt(bytes[index]) and 0x02) shr 1) == 1
        }
        fun getDataBytes() : ByteArray {
            var data = 0
            if (motorOn) {
                data = data or 0x01
            }
            if (buzzerOn) {
                data = data or 0x02
            }
            return byteArrayOf(data.toByte())
                .plus(ByteArray(3))
        }
    }
    // 血氧阈值
    class Spo2Low() {
        var low = 88  // 80-95% 步进%1 默认 88%
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
    // 心率提醒阈值，bit0震动，bit1声音
    class HrSwitch() {
        var motorOn = false
        var buzzerOn = false
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            motorOn = ((byte2UInt(bytes[index]) and 0x10) shr 4) == 1
            buzzerOn = ((byte2UInt(bytes[index]) and 0x20) shr 5) == 1
        }
        fun getDataBytes() : ByteArray {
            var data = 0
            if (motorOn) {
                data = data or 0x01
            }
            if (buzzerOn) {
                data = data or 0x02
            }
            return byteArrayOf(data.toByte())
                .plus(ByteArray(3))
        }
    }
    // 心率低阈值
    class HrLow() {
        var low = 50  // 30-70 步进5 默认 50
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
    // 心率高阈值
    class HrHigh() {
        var hi = 120  // 70-200 步进5 默认 120
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
    // 震动强度
    class Motor() {
        var motor = 20  // KidsO2(5/10/17/22/35) Oxylink(5/10/17/22/35)  O2Ring(20/40/60/80/100)
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            motor = byte2UInt(bytes[index])
        }
        fun getDataBytes() : ByteArray {
            return byteArrayOf(motor.toByte())
                .plus(ByteArray(3))
        }
    }
    // 声音强度
    class Buzzer() {
        var buzzer = 20  // (checkO2Plus：最低：20，低:40，中：60，高：80，最高：100)
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            buzzer = byte2UInt(bytes[index])
        }
        fun getDataBytes() : ByteArray {
            return byteArrayOf(buzzer.toByte())
                .plus(ByteArray(3))
        }
    }
    // 显示模式
    class DisplayMode() {
        var mode = 0  // 0:Standard模式 1:Always Off模式2:Always On模式
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
    // 屏幕亮度
    class BrightnessMode() {
        var mode = 2  // 0: 息屏 1: 低亮屏 2: 中 3:高
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
    // 存储间隔
    class StorageInterval() {
        var interval = 4  // 单位秒
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            interval = byte2UInt(bytes[index])
        }
        fun getDataBytes() : ByteArray {
            return byteArrayOf(interval.toByte())
                .plus(ByteArray(3))
        }
    }

    override fun toString(): String {
        return """
            OxyIIConfig : 
            spo2Switch : ${spo2Switch.motorOn}, ${spo2Switch.buzzerOn}
            spo2Low : ${spo2Low.low}
            hrSwitch : ${hrSwitch.motorOn}, ${hrSwitch.buzzerOn}
            hrLow : ${hrLow.low}
            hrHi : ${hrHi.hi}
            motor : ${motor.motor}
            buzzer : ${buzzer.buzzer}
            displayMode : ${displayMode.mode}
            brightnessMode : ${brightnessMode.mode}
            storageInterval : ${storageInterval.interval}
        """.trimIndent()
    }
}
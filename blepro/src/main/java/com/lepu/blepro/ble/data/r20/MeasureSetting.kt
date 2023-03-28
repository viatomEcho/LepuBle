package com.lepu.blepro.ble.data.r20

import com.lepu.blepro.utils.ByteUtils.byte2UInt

class MeasureSetting() {

    var type = 0
    // reserved 3
    var humidification = Humidification()
    var pressureReduce = PressureReduce()
    var pressureRaise = PressureRaise()
    var autoStart = AutoStart()
    var autoEnd = AutoEnd()
    var preHeat = PreHeat()
    var tubeType = TubeType()
    var maskType = MaskType()

    constructor(bytes: ByteArray) : this() {
        var index = 0
        humidification = Humidification(bytes.copyOfRange(index, index+1))
        index++
        pressureReduce = PressureReduce(bytes.copyOfRange(index, index+1))
        index++
        pressureRaise = PressureRaise(bytes.copyOfRange(index, index+2))
        index += 2
        autoStart = AutoStart(bytes.copyOfRange(index, index+1))
        index++
        autoEnd = AutoEnd(bytes.copyOfRange(index, index+1))
        index++
        preHeat = PreHeat(bytes.copyOfRange(index, index+1))
        index++
        tubeType = TubeType(bytes.copyOfRange(index, index+1))
        index++
        maskType = MaskType(bytes.copyOfRange(index, index+1))
    }

    fun getDataBytes(): ByteArray {
        var data = byteArrayOf(type.toByte())
            .plus(ByteArray(3))
        when (type) {
            0 -> {
                data = data.plus(humidification.getDataBytes())
                    .plus(pressureReduce.getDataBytes())
                    .plus(pressureRaise.getDataBytes())
                    .plus(autoStart.getDataBytes())
                    .plus(autoEnd.getDataBytes())
                    .plus(preHeat.getDataBytes())
                    .plus(tubeType.getDataBytes())
                    .plus(maskType.getDataBytes())
            }
            1 -> {
                data = data.plus(humidification.getDataBytes())
            }
            2 -> {
                data = data.plus(pressureReduce.getDataBytes())
            }
            3 -> {
                data = data.plus(pressureRaise.getDataBytes())
            }
            4 -> {
                data = data.plus(autoStart.getDataBytes())
            }
            5 -> {
                data = data.plus(autoEnd.getDataBytes())
            }
            6 -> {
                data = data.plus(preHeat.getDataBytes())
            }
            7 -> {
                data = data.plus(tubeType.getDataBytes())
            }
            8 -> {
                data = data.plus(maskType.getDataBytes())
            }
        }
        return data
    }

    // 参数设置：湿化等级
    class Humidification() {
        var humidification = 0  // 湿化等级。0：关闭；1-5档；0x10：自动；
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            humidification = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(humidification.toByte())
                .plus(ByteArray(3))
        }
    }
    // 参数设置：呼吸降压。S，S/T，T该选项无效，隐藏或置灰
    class PressureReduce() {
        var lvl = 0  // 0：关闭；1-3档。CPAP，APAP模式下默认值：2
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            lvl = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(lvl.toByte())
                .plus(ByteArray(3))
        }
    }
    // 参数设置：缓慢升压
    class PressureRaise() {
        var pressure = 0f  // 默认值4，步进0.5。
                           // 范围： 3 - CPAP压力 （CPAP模式）
                           // 3 - P min（APAP模式）
                           // 3 - EPAP （双水平模式）
                           // [单水平模式起始压力≤设置压力；
                           // 双水平模式起始压力≤呼气压力]
        var delay = 0      // 延时， 0-60min，步进5min。默认15min
        // reserved 2
        constructor(bytes: ByteArray) : this() {
            var index = 0
            pressure = byte2UInt(bytes[index]).div(10f)
            index++
            delay = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(pressure.times(10).toInt().toByte())
                .plus(delay.toByte())
                .plus(ByteArray(2))
        }
    }
    // 测量设置：自动启动
    class AutoStart() {
        var on = true  // 默认开启
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            on = byte2UInt(bytes[index]) == 1
        }
        fun getDataBytes(): ByteArray {
            val temp = if (on) {
                1
            } else {
                0
            }
            return byteArrayOf(temp.toByte())
                .plus(ByteArray(3))
        }
    }
    // 测量设置：自动停止
    class AutoEnd() {
        var on = true  // 默认开启
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            on = byte2UInt(bytes[index]) == 1
        }
        fun getDataBytes(): ByteArray {
            val temp = if (on) {
                1
            } else {
                0
            }
            return byteArrayOf(temp.toByte())
                .plus(ByteArray(3))
        }
    }
    // 测量设置：预加热
    class PreHeat() {
        var on = false  // 默认关闭
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            on = byte2UInt(bytes[index]) == 1
        }
        fun getDataBytes(): ByteArray {
            val temp = if (on) {
                1
            } else {
                0
            }
            return byteArrayOf(temp.toByte())
                .plus(ByteArray(3))
        }
    }
    // 设置：管道类型
    class TubeType() {
        var tube = 0  // 1: 15mm; 2:19mm（显示为22mm）。15（单水平机型）19（双水平机型）
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            tube = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(tube.toByte())
                .plus(ByteArray(3))
        }
    }
    // 设置：面罩类型
    class MaskType() {
        var mask = 0  // 1: 口鼻罩(full face), 2: 鼻罩(nasal), 3:鼻枕(pillow)
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            mask = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(mask.toByte())
                .plus(ByteArray(3))
        }
    }
}
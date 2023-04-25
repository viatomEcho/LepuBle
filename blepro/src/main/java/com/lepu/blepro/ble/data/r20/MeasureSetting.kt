package com.lepu.blepro.ble.data.r20

import com.lepu.blepro.utils.ByteUtils.byte2UInt

class MeasureSetting() {

    var type = 0
    // reserved 3
    var humidification = Humidification()
    var pressureReduce = PressureReduce()
    var autoSwitch = AutoSwitch()
    var preHeat = PreHeat()
    var ramp = Ramp()
    var tubeType = TubeType()
    var mask = Mask()

    constructor(bytes: ByteArray) : this() {
        var index = 0
        humidification = Humidification(bytes.copyOfRange(index, index+1))
        index++
        pressureReduce = PressureReduce(bytes.copyOfRange(index, index+2))
        index += 2
        autoSwitch = AutoSwitch(bytes.copyOfRange(index, index+1))
        index++
        preHeat = PreHeat(bytes.copyOfRange(index, index+1))
        index++
        ramp = Ramp(bytes.copyOfRange(index, index+2))
        index += 2
        tubeType = TubeType(bytes.copyOfRange(index, index+1))
        index++
        mask = Mask(bytes.copyOfRange(index, index+2))
        // reserved 6
    }

    fun getDataBytes(): ByteArray {
        var data = byteArrayOf(type.toByte())
            .plus(ByteArray(3))
        when (type) {
            0 -> {
                data = data.plus(humidification.getDataBytes())
                    .plus(pressureReduce.getDataBytes())
                    .plus(autoSwitch.getDataBytes())
                    .plus(preHeat.getDataBytes())
                    .plus(ramp.getDataBytes())
                    .plus(tubeType.getDataBytes())
                    .plus(mask.getDataBytes())
            }
            1 -> {
                data = data.plus(humidification.getDataBytes())
            }
            2 -> {
                data = data.plus(pressureReduce.getDataBytes())
            }
            3 -> {
                data = data.plus(autoSwitch.getDataBytes())
            }
            4 -> {
                data = data.plus(preHeat.getDataBytes())
            }
            5 -> {
                data = data.plus(ramp.getDataBytes())
            }
            6 -> {
                data = data.plus(tubeType.getDataBytes())
            }
            7 -> {
                data = data.plus(mask.getDataBytes())
            }
        }
        return data
    }

    // 参数设置：湿化等级
    class Humidification() {
        var humidification = 0  // 湿化等级。0：关闭；1-5档；0xff：自动；
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
    // 参数设置：呼吸压力释放
    class PressureReduce() {
        var ipr = 2  // 吸气压力释放 0：关闭；1-3档。CPAP，APAP模式下默认值：2
        var epr = 2  // 呼气压力释放 0：关闭；1-3档。CPAP，APAP模式下默认值：2
        // reserved 2
        constructor(bytes: ByteArray) : this() {
            var index = 0
            ipr = byte2UInt(bytes[index])
            index++
            epr = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(ipr.toByte())
                .plus(epr.toByte())
                .plus(ByteArray(2))
        }
    }
    // 测量设置：自动启停
    class AutoSwitch() {
        var autoStart = true  // 自动启动，默认开启
        var autoEnd = true    // 自动停止，默认开启
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            autoStart = (byte2UInt(bytes[index]) and 0x01) == 1
            autoEnd = ((byte2UInt(bytes[index]) and 0x02) shr 1) == 1
        }
        fun getDataBytes(): ByteArray {
            var temp = 0
            if (autoStart) {
                temp = temp or 0x01
            }
            if (autoEnd) {
                temp = temp or 0x02
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
    //参数设置：缓冲压力、时间
    class Ramp() {
        var pressure = 4f   // 缓冲压力 默认值40，步进5。单位：0.1cmH2O
        var time = 15  // 缓冲时间 延时， 0-60min，步进5min。默认15min. 0xff 自动
        // reserved 2
        constructor(bytes: ByteArray) : this() {
            var index = 0
            pressure = byte2UInt(bytes[index]).div(10f)
            index++
            time = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(pressure.times(10).toInt().toByte())
                .plus(time.toByte())
                .plus(ByteArray(2))
        }
    }
    // 设置：管道类型
    class TubeType() {
        var type = 1  // 0: 15mm; 1:19mm（显示为22mm）。15（单水平机型）19（双水平机型）
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            type = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(type.toByte())
                .plus(ByteArray(3))
        }
    }
    // 设置：面罩
    class Mask() {
        var type = 0        // 0: 口鼻罩(full face), 1: 鼻罩(nasal), 2:鼻枕(pillow)
        var pressure = 10f  // 面罩佩戴匹配测试压力 默认值:100   步长:10 范围:60-180   单位0.1cmH2O
        // reserved 2
        constructor(bytes: ByteArray) : this() {
            var index = 0
            type = byte2UInt(bytes[index])
            index++
            pressure = byte2UInt(bytes[index]).div(10f)
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(type.toByte())
                .plus(pressure.times(10).toInt().toByte())
                .plus(ByteArray(2))
        }
    }
}
package com.lepu.blepro.ble.data.ventilator

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.int2ByteArray
import com.lepu.blepro.utils.toUInt

class VentilationSetting() {

    var type = 0
    // reserved 3
    var ventilationMode = VentilationMode()
    var cpapPressure = CpapPressure()
    var apapPressureMax = ApapPressureMax()
    var apapPressureMin = ApapPressureMin()
    var pressureExhale = PressureExhale()
    var pressureInhale = PressureInhale()
    var inhaleDuration = InhaleDuration()
    var respiratoryRate = RespiratoryRate()
    var pressureRaiseDuration = PressureRaiseDuration()
    var exhaleSensitive = ExhaleSensitive()
    var inhaleSensitive = InhaleSensitive()

    constructor(bytes: ByteArray) : this() {
        var index = 0
        ventilationMode = VentilationMode(bytes.copyOfRange(index, index+1))
        index++
        cpapPressure = CpapPressure(bytes.copyOfRange(index, index+1))
        index++
        apapPressureMax = ApapPressureMax(bytes.copyOfRange(index, index+1))
        index++
        apapPressureMin = ApapPressureMin(bytes.copyOfRange(index, index+1))
        index++
        pressureInhale = PressureInhale(bytes.copyOfRange(index, index+1))
        index++
        pressureExhale = PressureExhale(bytes.copyOfRange(index, index+1))
        index++
        inhaleDuration = InhaleDuration(bytes.copyOfRange(index, index+1))
        index++
        respiratoryRate = RespiratoryRate(bytes.copyOfRange(index, index+1))
        index++
        pressureRaiseDuration = PressureRaiseDuration(bytes.copyOfRange(index, index+2))
        index += 2
        inhaleSensitive = InhaleSensitive(bytes.copyOfRange(index, index+1))
        index++
        exhaleSensitive = ExhaleSensitive(bytes.copyOfRange(index, index+1))
    }

    fun getDataBytes(): ByteArray {
        var data = byteArrayOf(type.toByte())
            .plus(ByteArray(3))
        when (type) {
            0 -> {
                data = data.plus(ventilationMode.getDataBytes())
                    .plus(cpapPressure.getDataBytes())
                    .plus(apapPressureMax.getDataBytes())
                    .plus(apapPressureMin.getDataBytes())
                    .plus(pressureInhale.getDataBytes())
                    .plus(pressureExhale.getDataBytes())
                    .plus(inhaleDuration.getDataBytes())
                    .plus(respiratoryRate.getDataBytes())
                    .plus(pressureRaiseDuration.getDataBytes())
                    .plus(inhaleSensitive.getDataBytes())
                    .plus(exhaleSensitive.getDataBytes())
            }
            1 -> {
                data = data.plus(ventilationMode.getDataBytes())
            }
            2 -> {
                data = data.plus(cpapPressure.getDataBytes())
            }
            3 -> {
                data = data.plus(apapPressureMax.getDataBytes())
            }
            4 -> {
                data = data.plus(apapPressureMin.getDataBytes())
            }
            5 -> {
                data = data.plus(pressureInhale.getDataBytes())
            }
            6 -> {
                data = data.plus(pressureExhale.getDataBytes())
            }
            7 -> {
                data = data.plus(inhaleDuration.getDataBytes())
            }
            8 -> {
                data = data.plus(respiratoryRate.getDataBytes())
            }
            9 -> {
                data = data.plus(pressureRaiseDuration.getDataBytes())
            }
            10 -> {
                data = data.plus(inhaleSensitive.getDataBytes())
            }
            11 -> {
                data = data.plus(exhaleSensitive.getDataBytes())
            }
        }
        return data
    }
    class VentilationMode() {
        var mode = 0  // 0:CPAP  1:APAP  2:S   3:S/T   4:T
        constructor(bytes: ByteArray) : this() {
            var index = 0
            mode = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(mode.toByte())
                .plus(ByteArray(3))
        }
    }
    // 通气控制：压力
    class CpapPressure() {
        var pressure = 6f  // CPAP模式压力   默认值:60  步长:5 范围:40-200  单位0.1cmH2O
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            pressure = byte2UInt(bytes[index]).div(10f)
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(pressure.times(10).toInt().toByte())
                .plus(ByteArray(3))
        }
    }
    // 通气控制：最大压力
    class ApapPressureMax() {
        var max = 12f  // APAP模式压力最大值Pmax 默认值:120 步长:5 范围:Pmin-200  单位0.1cmH2O
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            max = byte2UInt(bytes[index]).div(10f)
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(max.times(10).toInt().toByte())
                .plus(ByteArray(3))
        }
    }
    // 通气控制：最小压力
    class ApapPressureMin() {
        var min = 4f  // APAP模式压力最小值Pmin 默认值:40  步长:5 范围:40-Pmax   单位0.1cmH2O
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            min = byte2UInt(bytes[index]).div(10f)
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(min.times(10).toInt().toByte())
                .plus(ByteArray(3))
        }
    }
    // 通气控制：吸气压力
    class PressureInhale() {
        var inhale = 10f  // 吸气压力 默认值:100  步长:5 范围:40-250   单位0.1cmH2O
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            inhale = byte2UInt(bytes[index]).div(10f)
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(inhale.times(10).toInt().toByte())
                .plus(ByteArray(3))
        }
    }
    // 通气控制：呼气压力
    class PressureExhale() {
        var exhale = 6f  // 呼气压力 默认值:60   步长:5 范围:40-250   单位0.1cmH2O
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            exhale = byte2UInt(bytes[index]).div(10f)
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(exhale.times(10).toInt().toByte())
                .plus(ByteArray(3))
        }
    }
    // 通气控制：吸气时间
    class InhaleDuration() {
        var duration = 1f  // 吸气时间 默认值:10   步长:1 范围:3-40     单位0.1s
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            duration = byte2UInt(bytes[index]).div(10f)
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(duration.times(10).toInt().toByte())
                .plus(ByteArray(3))
        }
    }
    // 通气控制：呼吸频率
    class RespiratoryRate() {
        var rate = 5  // 呼吸频率。范围：5-30。 单位 bpm
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            rate = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(rate.toByte())
                .plus(ByteArray(3))
        }
    }
    // 通气控制：压力上升时间
    class PressureRaiseDuration() {
        var duration = 100  // 压力上升时间。范围：100-900ms，步进：50ms
        // reserved 2
        constructor(bytes: ByteArray) : this() {
            var index = 0
            duration = toUInt(bytes.copyOfRange(index, index+2))
        }
        fun getDataBytes(): ByteArray {
            return int2ByteArray(duration)
                .plus(ByteArray(2))
        }
    }
    // 通气控制：吸气触发灵敏度
    class InhaleSensitive() {
        var sentive = 3  // 吸气触发灵敏度Inspiratory Trigger 默认值:3档  范围:0-5档   0:自动档
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            sentive = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(sentive.toByte())
                .plus(ByteArray(3))
        }
    }
    // 通气控制：呼气触发灵敏度
    class ExhaleSensitive() {
        var sentive = 3  // 呼气触发灵敏度Expiratory Trigger  默认值:3档  范围:0-5档   0:自动档
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            sentive = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(sentive.toByte())
                .plus(ByteArray(3))
        }
    }
}
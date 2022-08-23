package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.bytes2UIntBig
import com.lepu.blepro.utils.byteToPointHex
import com.lepu.blepro.utils.toUInt

class Pc68bBleResponse{

    @ExperimentalUnsignedTypes
    class BleResponse(var bytes: ByteArray) {
        var token: Int
        var len: Int
        var type: Int
        var content: ByteArray  // 内容

        init {
            token = byte2UInt(bytes[2])
            len = byte2UInt(bytes[3])
            type = byte2UInt(bytes[4])
            content = bytes.copyOfRange(5, bytes.size-1)
        }

    }

    @ExperimentalUnsignedTypes
    class DeviceInfo(var bytes: ByteArray) {
        var softwareV: String   // 软件版本
        var hardwareV: String   // 硬件版本
        var deviceName: String  // 设备名称

        init {
            var index = 0
            softwareV = byteToPointHex(bytes[index]) + "." + byteToPointHex(bytes[index+1])
            index += 2
            hardwareV = byteToPointHex(bytes[index])
            index++
            deviceName = com.lepu.blepro.utils.toString(bytes.copyOfRange(index, bytes.size))
        }

        override fun toString(): String {
            return """
                softwareV : $softwareV
                hardwareV : $hardwareV
                deviceName : $deviceName
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class RtParam(var bytes: ByteArray) {
        var spo2: Int
        var pr: Int
        var pi: Float
        var isProbeOff: Boolean        // 探头脱落，手指未接入
        var isPulseSearching: Boolean  // 脉搏检测
        var isCheckProbe: Boolean      // 探头故障或使用不当
        var vol: Float                 // 电量等级（0-32）0-3.2V
        var battery: Int               // 电量等级（0-3）

        init {
            var index = 0
            spo2 = byte2UInt(bytes[index])
            index++
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pi = byte2UInt(bytes[index]).div(10f)
            index++
            isProbeOff = ((bytes[index].toInt() and 0x02) shr 1) == 1
            isPulseSearching = ((bytes[index].toInt() and 0x04) shr 2) == 1
            isCheckProbe = ((bytes[index].toInt() and 0x08) shr 3) == 1
            index++
            vol = (bytes[index].toInt() and 0x1F).div(10f)
            battery = (bytes[index].toInt() and 0xC0) shr 6
        }
        override fun toString(): String {
            return """
                spo2 : $spo2
                pr : $pr
                pi : $pi
                isProbeOff : $isProbeOff
                isPulseSearching : $isPulseSearching
                isCheckProbe : $isCheckProbe
                vol : $vol
                battery : $battery
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class RtWave(var byteArray: ByteArray) {
        val waveData: ByteArray
        val waveIntData: IntArray
        init {
            waveData = byteArray.copyOfRange(0, 5).toList().asSequence().map { (it.toInt() and 0x7f).toByte() }.toList().toByteArray()
            waveIntData =  byteArray.copyOfRange(0, 5).toList().asSequence().map { (it.toInt() and 0x7f)}.toList().toIntArray()
        }
    }

    @ExperimentalUnsignedTypes
    class StatusInfo(val bytes: ByteArray) {
        var interval: Int              // 时间间隔
        var isProbeOff: Boolean        // 探头脱落，手指未接入
        var isPulseSearching: Boolean  // 脉搏检测
        var isCheckProbe: Boolean      // 探头故障或使用不当
        var vol: Float                 // 电池电量（0-32）0-3.2V
        var battery: Int               // 电池电量等级（0-3）
        var enableRtWave: Boolean      // 上行主动发送波形状态
        var enableRtParam: Boolean     // 上行主动发送参数状态

        init {
            var index = 0
            interval = byte2UInt(bytes[index])
            index++
            isProbeOff = ((bytes[index].toInt() and 0x02) shr 1) == 1
            isPulseSearching = ((bytes[index].toInt() and 0x04) shr 2) == 1
            isCheckProbe = ((bytes[index].toInt() and 0x08) shr 3) == 1
            index++
            vol = (bytes[index].toInt() and 0x1F).div(10f)
            battery = (bytes[index].toInt() and 0xC0) shr 6
            enableRtWave = ((bytes[index].toInt() and 0x20) shr 5) == 1
            index++
            enableRtParam = (bytes[index].toInt() and 0x01) == 1
        }

        override fun toString(): String {
            return """
                interval : $interval
                isProbeOff : $isProbeOff
                isPulseSearching : $isPulseSearching
                isCheckProbe : $isCheckProbe
                vol : $vol
                battery : $battery
                enableRtWave : $enableRtWave
                enableRtParam : $enableRtParam
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class DeviceTime(val bytes: ByteArray) {
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var second: Int

        init {
            var index = 0
            year = bytes2UIntBig(bytes[index], bytes[index+1])
            index += 2
            month = byte2UInt(bytes[index])
            index++
            day = byte2UInt(bytes[index])
            index++
            hour = byte2UInt(bytes[index])
            index++
            minute = byte2UInt(bytes[index])
            index++
            second = byte2UInt(bytes[index])
        }

        override fun toString(): String {
            return """
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
                second : $second
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class Record(val bytes: ByteArray, val name: String) {
        var spo2 : IntArray
        var hr : IntArray

        init {
            val len = bytes.size.div(2)
            spo2 = IntArray(len)
            hr = IntArray(len)
            for (i in 0 until len) {
                spo2[i] = byte2UInt(bytes[i*2])
                hr[i] = byte2UInt(bytes[i*2+1])
            }
        }

        override fun toString(): String {
            return """
                name : $name
                spo2 : $spo2
                hr : $hr
            """.trimIndent()
        }
    }

}


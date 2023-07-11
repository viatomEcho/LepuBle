package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.byteToPointStr
import com.lepu.blepro.utils.toUInt
import kotlinx.android.parcel.Parcelize

class PC60FwBleResponse{
    class PC60FwResponse(var bytes: ByteArray) {
        var token: Int
        var length: Int
        var type: Int
        var content: ByteArray

        init {
            token = (bytes[2].toUInt() and 0xFFu).toInt()
            length = (bytes[3].toUInt() and 0xFFu).toInt()
            type = (bytes[4].toUInt() and 0xFFu).toInt()
            content = if (length <= 0) ByteArray(0) else bytes.copyOfRange(5, bytes.size - 1)
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class DeviceInfo constructor(var bytes: ByteArray) : Parcelable {
        var softwareV: String   // 固件版本
        var hardwareV: String   // 硬件版本
        var deviceName: String  // 设备名称

        init {
            var index = 0
            softwareV = byteToPointStr(bytes[index]) + "." + byteToPointStr(bytes[index+1])
            index += 2
            hardwareV = byteToPointStr(bytes[index])
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
    @Parcelize
    @ExperimentalUnsignedTypes
    class DeviceInfo0F constructor(var bytes: ByteArray) : Parcelable {
        var softwareV: String   // 固件版本
        var hardwareV: String   // 硬件版本
        var deviceName: String  // 设备名称

        init {
            var index = 0
            softwareV = byteToPointStr(bytes[index])
            index++
            hardwareV = byteToPointStr(bytes[index])
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
    @Parcelize
    class RtDataParam constructor(var byteArray: ByteArray) : Parcelable {
        var spo2: Int
        var pr: Int
        var pi: Int
        var isProbeOff: Boolean        // 探头脱落，手指未接入
        var isPulseSearching: Boolean  // 脉搏检测
        var battery: Int               // 电量等级 0-3
        init {
            spo2 = byte2UInt(byteArray[0])
            pr = toUInt(byteArray.copyOfRange(1, 3))
            pi = byte2UInt(byteArray[3])
            isProbeOff = ((byteArray[4].toInt() and 0x02) shr 1) == 1
            isPulseSearching = ((byteArray[4].toInt() and 0x04) shr 2) == 1
            battery = (byte2UInt(byteArray[5]) and 0xC0) shr 6
        }
        override fun toString(): String {
            return """
                spo2 : $spo2
                pr : $pr
                pi : $pi
                isProbeOff : $isProbeOff
                isPulseSearching : $isPulseSearching
                battery : $battery
            """.trimIndent()
        }

    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class RtDataWave(var byteArray: ByteArray) : Parcelable {
        val waveData: ByteArray
        val waveIntData: IntArray
        val waveIntReData: IntArray  // 倒置数据
        init {
            waveData = byteArray.copyOfRange(0, 5).toList().asSequence().map { (it.toInt() and 0x7f).toByte() }.toList().toByteArray()
            waveIntData = byteArray.copyOfRange(0, 5).toList().asSequence().map { (it.toInt() and 0x7f)}.toList().toIntArray()
            waveIntReData = byteArray.copyOfRange(0, 5).toList().asSequence().map { (127-(it.toInt() and 0x7f))}.toList().toIntArray()
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class Battery constructor(var byteArray: ByteArray) : Parcelable {
        var batteryLevel: Byte  // 电量等级（0-3）
        init {
            batteryLevel  = byteArray[0]
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class WorkingStatus constructor(var bytes: ByteArray) : Parcelable {
        var mode: Int   // 模式（1：点测 2：连续 3：菜单）
        var step: Int   // 状态（0：idle 1：准备阶段 2：正在测量 3：播报血氧结果 4：脉率分析 5：点测完成）
        var para1: Int
        var para2: Int

        init {
            var index = 0
            mode = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            step = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            para1 = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            para2 = (bytes[index].toUInt() and 0xFFu).toInt()
        }

        override fun toString(): String {
            return """
                WorkingStatus
                mode : $mode
                step : $step
                para1 : $para1
                para2 : $para2
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class OriginalData(val bytes: ByteArray) {
        var redFrq: Int  // 0-600Khz
        var irFrq: Int   // 0-600Khz
        init {
            var index = 0
            if (bytes.size >= 8) {
                irFrq = toUInt(bytes.copyOfRange(index, index+4))
                index += 4
                redFrq = toUInt(bytes.copyOfRange(index, index+4))
            } else {
                redFrq = toUInt(bytes.copyOfRange(index, index+2))
                index += 2
                irFrq = toUInt(bytes.copyOfRange(index, index+2))
            }
        }

        override fun toString(): String {
            return """
                redFrq : $redFrq
                irFrq : $irFrq
            """.trimIndent()
        }
    }

}


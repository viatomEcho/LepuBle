package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.bytes2UIntBig
import com.lepu.blepro.utils.byteToPointHex
import com.lepu.blepro.utils.toUInt
import kotlinx.android.parcel.Parcelize

class Sp20BleResponse{

    @ExperimentalUnsignedTypes
    @Parcelize
    class Sp20Response constructor(var bytes: ByteArray) : Parcelable {
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

    @Parcelize
    @ExperimentalUnsignedTypes
    class DeviceInfo constructor(var bytes: ByteArray) : Parcelable {
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
    @Parcelize
    class RtParam constructor(var bytes: ByteArray) : Parcelable {
        var spo2: Int
        var pr: Int
        var pi: Int
        var isProbeOff: Boolean        // 探头脱落，手指未接入
        var isPulseSearching: Boolean  // 脉搏检测
        var battery: Int               // 电量等级（0-3）
        init {
            var index = 0
            spo2 = byte2UInt(bytes[index])
            index++
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pi = byte2UInt(bytes[index])
            index++
            isProbeOff = ((bytes[index].toInt() and 0x02) shr 1) == 1
            isPulseSearching = ((bytes[index].toInt() and 0x04) shr 2) == 1
            index++
            battery = (bytes[index].toInt() and 0xC0) shr 6
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
    class RtWave(var byteArray: ByteArray) : Parcelable {
        val waveData: ByteArray
        val waveIntData: IntArray
        init {
            waveData = byteArray.copyOfRange(0, 5).toList().asSequence().map { (it.toInt() and 0x7f).toByte() }.toList().toByteArray()
            waveIntData =  byteArray.copyOfRange(0, 5).toList().asSequence().map { (it.toInt() and 0x7f)}.toList().toIntArray()
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class TempData constructor(val bytes: ByteArray) : Parcelable {
        var result: Int   // 体温结果 0：正常 1：过低 2：过高
        var unit: Int     // 单位 0：摄氏度℃ 1：华氏度℉
        var value: Float  // 体温值
        init {
            result = (bytes[0].toInt() and 0x06) shr 1
            unit = bytes[0].toInt() and 0x01
            value = bytes2UIntBig(bytes[1], bytes[2]).div(100f)
        }

        override fun toString(): String {
            return """
                result : $result
                unit : $unit
                value : $value
            """.trimIndent()
        }
    }

}


package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.utils.byteToPointHex
import com.lepu.blepro.utils.toUInt
import kotlinx.android.parcel.Parcelize

class Ap20BleResponse{

    @ExperimentalUnsignedTypes
    @Parcelize
    class Ap20Response constructor(var bytes: ByteArray) : Parcelable {
        var token: Int
        var len: Int
        var type: Int
        var content: ByteArray  // 内容

        init {
            token = (bytes[2].toUInt() and 0xFFu).toInt()
            len = toUInt(bytes.copyOfRange(3, 4))
            type = (bytes[4].toUInt() and 0xFFu).toInt()
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
    class RtBoParam constructor(var bytes: ByteArray) : Parcelable {
        var spo2: Int
        var pr: Int
        var pi: Int
        var isProbeOff: Boolean        // 探头脱落，手指未接入
        var isPulseSearching: Boolean  // 脉搏检测
        var isCheckProbe: Boolean      // 探头故障或使用不当
        var battery: Int               // 电量等级（0-3）
        init {
            var index = 0
            spo2 = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pi = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            isProbeOff = ((bytes[index].toInt() and 0x02) shr 1) == 1
            isPulseSearching = ((bytes[index].toInt() and 0x04) shr 2) == 1
            isCheckProbe = ((bytes[index].toInt() and 0x08) shr 3) == 1
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
                isCheckProbe : $isCheckProbe
                battery : $battery
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class RtBoWave(var byteArray: ByteArray) : Parcelable {
        val waveData: ByteArray
        val waveIntData: IntArray
        init {
            waveData = byteArray.copyOfRange(0, 5).toList().asSequence().map { (it.toInt() and 0x7f).toByte() }.toList().toByteArray()
            waveIntData =  byteArray.copyOfRange(0, 5).toList().asSequence().map { (it.toInt() and 0x7f)}.toList().toIntArray()
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class RtBreathParam constructor(var bytes: ByteArray) : Parcelable {
        var rr: Int          // 呼吸率（6-60，单位bpm，0是无效值）
        var sign: Int      // 鼻息流脱落标记（0：呼吸信号正常 1：无呼吸信号）
        init {
            var index = 0
            rr = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            sign = bytes[index].toInt() and 0x01
        }
        override fun toString(): String {
            return """
                rr : $rr
                sign : $sign
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class RtBreathWave(var byteArray: ByteArray) : Parcelable {
        val flowBytes: ByteArray
        val flowInt: Int           // 呼吸波形数据（0-4095）
        val snoreBytes: ByteArray
        val snoreInt: Int          // 鼾声波形数据（0-4095）
        init {
            var index = 0
            flowBytes = byteArray.copyOfRange(index, index+2)
            flowInt = toUInt(flowBytes)
            index += 2
            snoreBytes = byteArray.copyOfRange(index, index+2)
            snoreInt = toUInt(snoreBytes)
        }

        override fun toString(): String {
            return """
                flowInt : $flowInt
                snoreInt : $snoreInt
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class ConfigInfo constructor(var bytes: ByteArray) : Parcelable {
        var type: Int  // 0：背光等级（0-5） 1：警报开关（0：off 1：on） 2：血氧过低阈值（85-99） 3：脉率过低阈值（30-99） 4：脉率过高阈值（100-250）
        var data: Int
        init {
            type = (bytes[0].toUInt() and 0xFFu).toInt()
            data = (bytes[1].toUInt() and 0xFFu).toInt()
        }
        override fun toString(): String {
            return """
                type : $type
                data : $data
            """.trimIndent()
        }

    }

}


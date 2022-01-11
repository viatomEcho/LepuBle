package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.utils.ByteUtils.toSignedShort
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import kotlinx.android.parcel.Parcelize

class Ap20BleResponse{

    @ExperimentalUnsignedTypes
    @Parcelize
    class Ap10Response constructor(var bytes: ByteArray) : Parcelable {
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
            softwareV = bytesToHex(bytes.copyOfRange(index, index + 2))
            index += 2
            hardwareV = bytesToHex(byteArrayOf(bytes[index]))
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
        var status: Int
        var battery: Int
        init {
            var index = 0
            spo2 = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pi = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            status = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            battery = (bytes[index].toInt() and 0xC0) shr 6
        }
        override fun toString(): String {
            return """
                spo2 : $spo2
                pr : $pr
                pi : $pi
                status : $status
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
        var rr: Int
        var singleFlag: Int
        init {
            var index = 0
            rr = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            singleFlag = bytes[index].toInt() and 0x01
        }
        override fun toString(): String {
            return """
                rr : $rr
                singleFlag : $singleFlag
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class RtBreathWave(var byteArray: ByteArray) : Parcelable {
        val flow: Int
        val snore: Int
        init {
            var index = 0
            flow = toUInt(byteArray.copyOfRange(index, index+2))
            index += 2
            snore = toUInt(byteArray.copyOfRange(index, index+2))
        }

        fun intToArray(data: Int): IntArray {
            var array = IntArray(1)
            array[0] = data
            return array
        }

        override fun toString(): String {
            return """
                flow : $flow
                snore : $snore
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class ConfigInfo constructor(var bytes: ByteArray) : Parcelable {
        var type: Int
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


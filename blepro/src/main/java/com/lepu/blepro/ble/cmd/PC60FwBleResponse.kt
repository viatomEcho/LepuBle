package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.utils.ByteUtils.toSignedShort
import com.lepu.blepro.utils.bytesToHex
import kotlinx.android.parcel.Parcelize


const val HEAD_0 = 0xAA.toByte()
const val HEAD_1 = 0x55.toByte()
const val TOKEN_EPI_F0 = 0xF0.toByte() // (EPI -> Equipment Public information)
const val TOKEN_PO_0F = 0x0F.toByte() // (PO -> Pulse Oximeter)
class PC60FwBleResponse{
    class PC60FwResponse(var bytes: ByteArray) {

        companion object {
            const val TYPE_SPO2_PARAM = 0x01.toByte()
            const val TYPE_SPO2_WAVE = 0x02.toByte()
            const val TYPE_DEVICE_INFO = 0x01.toByte()
            const val TYPE_DEVICE_SN = 0x02.toByte()
            const val TYPE_BATTERY_LEVEL = 0x03.toByte()
            const val TYPE_WORKING_STATUS = 0x21.toByte()
        }

        val token: Byte = bytes[2]
        val length: Int = bytes[3].toInt()
        val type: Byte = bytes[4]
        val content: ByteArray
        private var valid: Boolean = true

        init {
            content = if (length == 0) ByteArray(0) else bytes.copyOfRange(5, bytes.size - 1)
            if (bytes[0] != HEAD_0) {
                valid = false
            } else if (bytes[1] != HEAD_1) {
                valid = false
            } else if ((token != TOKEN_EPI_F0) or (token != TOKEN_PO_0F)) {
                valid = false
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other is PC60FwResponse) {
                return this.bytes.contentEquals(other.bytes)
            }
            return false
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
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class RtDataParam constructor(var byteArray: ByteArray) : Parcelable {
        var spo2: Byte
        var pr: Short
        var pi: Short
        init {
            spo2 =  byteArray[0]
            pr = toSignedShort(byteArray[1], byteArray[2])
            pi = (byteArray[3].toInt() and 0xff).toShort()
        }


    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class RtDataWave(var byteArray: ByteArray) : Parcelable {
        val waveData: ByteArray
        val waveIntData: IntArray
        init {
            waveData = byteArray.copyOfRange(0, 5).toList().asSequence().map { (it.toInt() and 0x7f).toByte() }.toList().toByteArray()
            waveIntData =  byteArray.copyOfRange(0, 5).toList().asSequence().map { (it.toInt() and 0x7f)}.toList().toIntArray()
        }


    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class Battery constructor(var byteArray: ByteArray) : Parcelable {
        var batteryLevel: Byte
        init {
            batteryLevel  = byteArray[0]
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class WorkingStatus constructor(var bytes: ByteArray) : Parcelable {
        var mode: Int
        var step: Int
        var para1: Int
        var para2: Int

        // 模式 : 0x01点测 0x02连续 0x03菜单
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
    }

}


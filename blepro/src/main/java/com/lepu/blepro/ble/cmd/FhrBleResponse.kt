package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.utils.*
import kotlinx.android.parcel.Parcelize

object FhrBleResponse {

    @ExperimentalUnsignedTypes
    @Parcelize
    class FhrResponse constructor(var bytes: ByteArray) : Parcelable {
        var len: Int
        var cmd: Int
        var content: ByteArray  // 内容

        init {
            len = ((bytes[2].toUInt() and 0x0Fu).toInt() shl 8) + ((bytes[3].toUInt() and 0xFFu).toInt())
            cmd = (bytes[4].toUInt() and 0xFFu).toInt()
            content = bytes.copyOfRange(5, bytes.size - 2)
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class DeviceInfo constructor(var bytes: ByteArray) : Parcelable {
        var deviceName: String  // 设备名称
        var hr: Int             // 心率数据（心率测量范围为60~240，0表示无信号，255表示超出量程）
        var volume: Int         // 音量数据（0-6）
        var strength: Int       // 心音强度数据（0-2）
        var battery: Int        // 电量数据（0-6）不准确，设备问题

        init {
            val data = FhrResponse(bytes)
            deviceName = toString(data.content.copyOfRange(1, 5))
            hr = (data.content[7].toUInt() and 0xFFu).toInt()
            volume = (data.content[8].toUInt() and 0xFFu).toInt()
            strength = (data.content[9].toUInt() and 0xFFu).toInt()
            battery = (data.content[10].toUInt() and 0xFFu).toInt()
        }

        override fun toString(): String {
            return """
                deviceName : $deviceName
                hr : $hr
                volume : $volume
                strength : $strength
                battery : $battery
            """.trimIndent()
        }
    }

}
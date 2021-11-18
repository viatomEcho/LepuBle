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
            len = ((bytes[2].toInt() and 0x0F) shl 8) + ((bytes[3].toUInt() and 0xFFu).toInt())
            cmd = (bytes[4].toUInt() and 0xFFu).toInt()
            content = bytes.copyOfRange(5, bytes.size - 2)
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class DeviceInfo constructor(var bytes: ByteArray) : Parcelable {
        var deviceName: String  // 设备名称
        var hr: Int             // 心率数据（心率测量范围为60~240，0表示无信号，255表示超出量程）
        var volume: Int         // 音量数据
        var strength: Int       // 心音强度数据
        var batLevel: Int       // 电量数据

        init {
            deviceName = toString(bytes.copyOfRange(1, 5))
            hr = (bytes[7].toUInt() and 0xFFu).toInt()
            volume = (bytes[8].toUInt() and 0xFFu).toInt()
            strength = (bytes[9].toUInt() and 0xFFu).toInt()
            batLevel = (bytes[10].toUInt() and 0xFFu).toInt()
        }
    }

}
package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.utils.*
import kotlinx.android.parcel.Parcelize

object Bpw1BleResponse {

    @ExperimentalUnsignedTypes
    @Parcelize
    class Bpw1Response constructor(var bytes: ByteArray) : Parcelable {
        var head: Byte // 数据头
        var type: Byte // 设备类型
        var len: Int
        var content: ByteArray

        init {
            head = bytes[0]
            type = bytes[1]
            len = (bytes[2].toUInt() and 0xFFu).toInt()
            content = bytes.copyOfRange(3, bytes.size-1)
        }

    }

}
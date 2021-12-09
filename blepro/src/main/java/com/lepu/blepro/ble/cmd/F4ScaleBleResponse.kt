package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

object F4ScaleBleResponse {

    @ExperimentalUnsignedTypes
    @Parcelize
    class F4ScaleResponse constructor(var bytes: ByteArray) : Parcelable {
        var packageNo: Int
        var len: Int
        var seqNo: Int
        var cmd: Int
        var content: ByteArray  // 内容

        init {
            packageNo = (bytes[0].toUInt() and 0xFFu).toInt()
            len = (bytes[1].toUInt() and 0xFFu).toInt()
            seqNo = (bytes[2].toUInt() and 0xFFu).toInt()
            cmd = (bytes[3].toUInt() and 0xFFu).toInt()
            content = bytes.copyOfRange(4, 17)
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class WeightData constructor(var bytes: ByteArray) : Parcelable {
        var state: Int
        var weightG: Int
        var hr: Int

        init {
            state = (bytes[0].toUInt() and 0xFFu).toInt()
            weightG = ((bytes[2].toUInt() and 0x03u).toInt() shl 16) + ((bytes[3].toUInt() and 0xFFu).toInt() shl 8) + (bytes[4].toUInt() and 0xFFu).toInt()
            hr = (bytes[5].toUInt() and 0xFFu).toInt()
        }

        override fun toString(): String {
            return """
                state = $state
                weightG = $weightG
                hr = $hr
            """
        }
    }

}
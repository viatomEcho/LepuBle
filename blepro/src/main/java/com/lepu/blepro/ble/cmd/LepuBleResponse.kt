package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.utils.toUInt
import kotlinx.android.parcel.Parcelize

class LepuBleResponse {

    @ExperimentalUnsignedTypes
    @Parcelize
    class BleResponse constructor(var bytes: ByteArray) : Parcelable {
        var cmd: Int
        var pkgType: Byte
        var pkgNo: Int
        var len: Int
        var content: ByteArray

        init {
            cmd = (bytes[1].toUInt() and 0xFFu).toInt()
            pkgType = bytes[3]
            pkgNo = (bytes[4].toUInt() and 0xFFu).toInt()
            len = toUInt(bytes.copyOfRange(5, 7))
            content = bytes.copyOfRange(7, 7+len)
        }
    }

}
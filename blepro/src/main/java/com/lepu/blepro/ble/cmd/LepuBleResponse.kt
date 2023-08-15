package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.utils.ByteUtils
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import kotlinx.android.parcel.Parcelize

class LepuBleResponse {

    @ExperimentalUnsignedTypes
    @Parcelize
    class BleResponse constructor(var bytes: ByteArray) : Parcelable {
        var cmd: Int
        var pkgType: Int
        var pkgNo: Int
        var len: Int
        var content: ByteArray

        init {
            cmd = (bytes[1].toUInt() and 0xFFu).toInt()
            pkgType = (bytes[3].toUInt() and 0xFFu).toInt()
            pkgNo = (bytes[4].toUInt() and 0xFFu).toInt()
            len = toUInt(bytes.copyOfRange(5, 7))
            content = bytes.copyOfRange(7, 7+len)
        }
    }

    @ExperimentalUnsignedTypes
    class EncryptInfo(val bytes: ByteArray) {
        var type: Int       // 0：未加密，1：AES加密，2：MD5 加密
        var len: Int        // 加密密钥长度
        // reserved 2
        var key: ByteArray  // 加密通讯密钥AES key
        init {
            var index = 0
            type = ByteUtils.byte2UInt(bytes[index])
            index++
            len = ByteUtils.byte2UInt(bytes[index])
            index++
            index += 2
            key = bytes.copyOfRange(index, index+len)
        }
        override fun toString(): String {
            return """
                EncryptInfo : 
                type : $type
                len : $len
                key : ${bytesToHex(key)}
            """.trimIndent()
        }
    }

}
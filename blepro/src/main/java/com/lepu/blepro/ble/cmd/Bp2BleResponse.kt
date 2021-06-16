package com.lepu.blepro.ble.cmd

import com.lepu.blepro.ble.cmd.BleCRC.calCRC8
import com.lepu.blepro.utils.toUInt

class Bp2BleResponse {
    class BleResponse {
        companion object {
            const val TYPE_SEND = 0x00.toByte()
            const val TYPE_RESPONSE = 0x01.toByte()

            const val FILE_NOT_EXIST = 0xE0.toByte()
            const val FILE_OPEN_ERROR = 0xE1.toByte()
            const val FILE_READ_ERROR = 0xE2.toByte()
            const val FILE_WRITE_ERROR = 0xE3.toByte()

            const val ERROR_CMD = 0xFC.toByte()
            const val ERROR_CMD_NOT_SUPPORT = 0xFD.toByte()

            const val ERROR_COMMON = 0xFF.toByte()
        }

        var cmd: Byte
        var type: Byte
        var len: Int
        var content: ByteArray

        constructor(bytes: ByteArray) {
            cmd = bytes[1]
            type = bytes[3]
            len = toUInt(bytes.copyOfRange(5, 7))
            content = if (len == 0) ByteArray(0) else bytes.copyOfRange(7, 7 + len)
        }
    }

//    @ExperimentalUnsignedTypes
//    private fun bp2(bytes: ByteArray?): ByteArray? {
//        val bytesLeft: ByteArray? = bytes
//
//        if (bytes == null || bytes.size < 8) {
//            return bytes
//        }
//
//        loop@ for (i in 0 until bytes.size-7) {
//            if (bytes[i] != 0xA5.toByte()) {
//                continue@loop
//            }
//
//            // need content length
//            val len = toUInt(bytes.copyOfRange(i + 5, i + 7))
//            if (i+8+len > bytes.size) {
//                return bytes.copyOfRange(i, bytes.size)
//            }
//
//            val temp: ByteArray = bytes.copyOfRange(i, i + 8 + len)
//            if (temp.last() == calCRC8(temp)) {
//                val bleResponse = BleResponse(temp)
//                onResponseReceived(temp)
//                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i + 8 + len, bytes.size)
//                return bp2(tempBytes)
//            }
//        }
//        return bytesLeft
//    }
}
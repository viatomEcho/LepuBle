package com.lepu.blepro.ble.cmd

import android.util.Log

object PC100Response {

    class PcBleResponse(var bytes: ByteArray) {
        val token: Byte = bytes[2]
        private val length: Int = bytes[3].toInt()
        val type: Byte = bytes[4]
        val content: ByteArray = if (length == 1) ByteArray(0) else bytes.copyOfRange(5, bytes.size - 1)

        override fun equals(other: Any?): Boolean {
            if(this === other) return true
            if(other is PcBleResponse) {
                return this.bytes.contentEquals(other.bytes)
            }
            return false
        }

    }


    data class PcStatus(val byteArray: ByteArray) {
        val status = byteArray[5]
        val swVer: String
            get() {
                val swByte = byteArray[6]
                val preVer = (swByte.toInt() and 0xf0 shr 4)
                val sufVer = (swByte.toInt() and 0x0f)
                return "${preVer}.${sufVer}"
            }

        val hwVer: String
            get() {
                val swByte = byteArray[7]
                val preVer = (swByte.toInt() and 0xf0 shr 4)
                val sufVer = (swByte.toInt() and 0x0f)
                return "${preVer}.${sufVer}"
            }
    }


    data class PcBpData(val byteArray: ByteArray) {
        val bpValue = (byteArray[5].toInt() and 0x0f shl 8) + (byteArray[6].toInt() and 0xff)
        val flag: Boolean = ((byteArray[5].toInt() and 0x10 shr 4) and 0x01) == 0x01

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PcBpData

            if (!byteArray.contentEquals(other.byteArray)) return false
            if (bpValue != other.bpValue) return false
            if (flag != other.flag) return false

            return true
        }

        override fun hashCode(): Int {
            var result = byteArray.contentHashCode()
            result = 31 * result + bpValue
            result = 31 * result + flag.hashCode()
            return result
        }
    }

   data class PcDeviceInfo (val byteArray: ByteArray) {

        val swVer: String
            get() {
                val swByte = byteArray[5]
                val preVer = (swByte.toInt() and 0xf0 shr 4)
                val sufVer = (swByte.toInt() and 0x0f)
                return "${preVer}.${sufVer}"
            }

        val hwVer: String
            get() {
                val swByte = byteArray[6]
                val preVer = (swByte.toInt() and 0xf0 shr 4)
                val sufVer = (swByte.toInt() and 0x0f)
                return "${preVer}.${sufVer}"
            }


    }

}
package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.toSignedShort

data class PC100Spo2Param(val byteArray: ByteArray, val isOxyfit: Boolean = false) {


    val spo2: Byte = if (isOxyfit) byteArray[0] else byteArray[5]
    val pr: Short = if (isOxyfit) toSignedShort(byteArray[1], byteArray[2]).toShort() else toSignedShort(byteArray[6], byteArray[7]).toShort()
    val pi: Short = if (isOxyfit) (byteArray[10].toInt() and 0x7f).toShort() else (byteArray[8].toInt() and 0x7f).toShort()
    val status: Byte = if (isOxyfit) byteArray[11] else byteArray[9]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PC100Spo2Param

        if (!byteArray.contentEquals(other.byteArray)) return false
        if (spo2 != other.spo2) return false
        if (pr != other.pr) return false
        if (pi != other.pi) return false
        if (status != other.status) return false

        return true
    }

    override fun hashCode(): Int {
        var result = byteArray.contentHashCode()
        result = 31 * result + spo2
        result = 31 * result + pr
        result = 31 * result + pi
        result = 31 * result + status
        return result
    }
}
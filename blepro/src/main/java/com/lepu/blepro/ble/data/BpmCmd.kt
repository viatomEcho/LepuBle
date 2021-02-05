package com.lepu.blepro.ble.data

data class BpmCmd(val byteArray: ByteArray)  {

    val length = byteArray[3].toInt()
    val type = byteArray[4]
    val data = byteArray.copyOfRange(5, byteArray.size)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BpmCmd

        if (!byteArray.contentEquals(other.byteArray)) return false
        if (length != other.length) return false
        if (type != other.type) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = byteArray.contentHashCode()
        result = 31 * result + length
        result = 31 * result + type
        result = 31 * result + data.contentHashCode()
        return result
    }
}

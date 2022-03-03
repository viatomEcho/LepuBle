package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.toUInt

class LeBp2wUserList() {

    var fileVersion: Int = 0
    var fileType: Int = 0
    lateinit var listContent: ByteArray
    var userList = mutableListOf<LeBp2wUserInfo>()

    constructor(bytes: ByteArray) : this() {
        var index = 0
        fileVersion = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        fileType = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        index += 8
        listContent = bytes.copyOfRange(index, bytes.size)
        index = 0
        while (index != listContent.size) {
            val len = toUInt(listContent.copyOfRange(index, index+2))
            userList.add(LeBp2wUserInfo(listContent.copyOfRange(index, index+len)))
            index += len
        }
    }

    fun getDataBytes(): ByteArray {
        var data = byteArrayOf(fileVersion.toByte())
        data = data.plus(fileType.toByte())
            .plus(ByteArray(8))
        for (user in userList) {
            data = data.plus(user.getDataBytes())
        }
        return data
    }

    override fun toString(): String {
        return """
            fileVersion : $fileVersion
            fileType : $fileType
            userList : $userList
        """.trimIndent()
    }
}
package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.int2ByteArray
import com.lepu.blepro.utils.toUInt
import java.nio.charset.StandardCharsets

class PhoneBook() {

    var leftSize = 0
    var currentSize = 0
    var items = mutableListOf<Item>()

    constructor(bytes: ByteArray) : this() {
        var index = 0
        leftSize = byte2UInt(bytes[index])
        index++
        currentSize = byte2UInt(bytes[index])
        index++
        for (i in 0 until currentSize) {
            val nameLen = byte2UInt(bytes[index+2])
            val phoneLen = byte2UInt(bytes[index+3+nameLen])
            val len = 4 + nameLen + phoneLen
            items.add(Item(bytes.copyOfRange(index, index+len)))
            index += len
        }
    }

    fun getDataBytes(): ByteArray {
        currentSize = items.size
        val itemsTemp = ByteArray(0)
        for (i in 0 until currentSize) {
            itemsTemp.plus(items[i].getDataBytes())
        }
        return byteArrayOf(leftSize.toByte())
            .plus(currentSize.toByte())
            .plus(itemsTemp)
    }

    override fun toString(): String {
        return """
            PhoneBook : 
            bytes : ${bytesToHex(getDataBytes())}
            leftSize : $leftSize
            currentSize : $currentSize
            items : $items
        """.trimIndent()
    }

    class Item() {
        var id = 0
        var nameLen = 0
        var name = ""
        var phoneLen = 0
        var phone = ""
        constructor(bytes: ByteArray) : this() {
            var index = 0
            id = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            nameLen = byte2UInt(bytes[index])
            index++
//            name = trimStr(String(bytes.copyOfRange(index, index+nameLen), StandardCharsets.US_ASCII))
            name = trimStr(String(bytes.copyOfRange(index, index+nameLen), StandardCharsets.UTF_8))
            index += nameLen
            phoneLen = byte2UInt(bytes[index])
            index++
            phone = trimStr(String(bytes.copyOfRange(index, index+phoneLen), StandardCharsets.US_ASCII))
//            phone = trimStr(String(bytes.copyOfRange(index, index+phoneLen), StandardCharsets.UTF_8))
        }
        fun getDataBytes(): ByteArray {
//            val nameTemp = name.toByteArray(StandardCharsets.US_ASCII)
            val nameTemp = name.toByteArray(StandardCharsets.UTF_8)
            nameLen = nameTemp.size
            val phoneTemp = phone.toByteArray(StandardCharsets.US_ASCII)
//            val phoneTemp = phone.toByteArray(StandardCharsets.UTF_8)
            phoneLen = phoneTemp.size
            return int2ByteArray(id)
                .plus(nameLen.toByte())
                .plus(nameTemp)
                .plus(phoneLen.toByte())
                .plus(phoneTemp)
        }
        override fun toString(): String {
            return """
                Item : 
                bytes : ${bytesToHex(getDataBytes())}
                id : $id
                nameLen : $nameLen
                name : $name
                phoneLen : $phoneLen
                phone : $phone
            """.trimIndent()
        }
    }

}
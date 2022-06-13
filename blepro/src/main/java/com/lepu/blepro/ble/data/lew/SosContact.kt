package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex
import java.nio.charset.StandardCharsets

class SosContact() {

    var switch = false
    var itemSize = 0
    var items = mutableListOf<Item>()

    constructor(bytes: ByteArray) : this() {
        var index = 0
        switch = byte2UInt(bytes[index]) == 1
        index++
        itemSize = byte2UInt(bytes[index])
        index++
        for (i in 0 until itemSize) {
            val nameLen = byte2UInt(bytes[index+1])
            val phoneLen = byte2UInt(bytes[index+2+nameLen])
            val len = 4 + nameLen + phoneLen
            items.add(Item(bytes.copyOfRange(index, index+len)))
            index += len
        }
    }

    fun getDataBytes(): ByteArray {
        val on = if (switch) {
            1
        } else {
            0
        }
        itemSize = items.size
        val itemsTemp = ByteArray(0)
        for (i in 0 until itemSize) {
            itemsTemp.plus(items[i].getDataBytes())
        }
        return byteArrayOf(on.toByte())
            .plus(itemSize.toByte())
            .plus(itemsTemp)
    }

    override fun toString(): String {
        return """
            SosContact : 
            bytes : ${bytesToHex(getDataBytes())}
            switch : $switch
            itemSize : $itemSize
            items : $items
        """.trimIndent()
    }

    class Item() {
        var default = false  // 是否为默认拨打紧急联系人，只有一个。1：是，0：否
        var nameLen = 0      // 姓名长度，最长45
        var name = ""
        var phoneLen = 0     // 电话长度，最长16
        var phone = ""
        var relation = 0     // 关系 LewBleCmd.RelationShip
        constructor(bytes: ByteArray) : this() {
            var index = 0
            default = byte2UInt(bytes[index]) == 1
            index++
            nameLen = byte2UInt(bytes[index])
            index++
//            name = trimStr(String(bytes.copyOfRange(index, index+nameLen), StandardCharsets.US_ASCII))
            name = trimStr(String(bytes.copyOfRange(index, index + nameLen), StandardCharsets.UTF_8))
            index += nameLen
            phoneLen = byte2UInt(bytes[index])
            index++
            phone = trimStr(String(bytes.copyOfRange(index, index + phoneLen), StandardCharsets.US_ASCII))
//            phone = trimStr(String(bytes.copyOfRange(index, index+phoneLen), StandardCharsets.UTF_8))
            index += phoneLen
            relation = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            val on = if (default) {
                1
            } else {
                0
            }
//        val nameTemp = name.toByteArray(StandardCharsets.US_ASCII)
            val nameTemp = name.toByteArray(StandardCharsets.UTF_8)
            nameLen = nameTemp.size
            val phoneTemp = phone.toByteArray(StandardCharsets.US_ASCII)
//        val phoneTemp = phone.toByteArray(StandardCharsets.UTF_8)
            phoneLen = phoneTemp.size
            return byteArrayOf(on.toByte())
                .plus(nameLen.toByte())
                .plus(nameTemp)
                .plus(phoneLen.toByte())
                .plus(phoneTemp)
                .plus(relation.toByte())
        }
        override fun toString(): String {
            return """
                Item : 
                bytes : ${bytesToHex(getDataBytes())}
                default : $default
                nameLen : $nameLen
                name : $name
                phoneLen : $phoneLen
                phone : $phone
                relation : $relation
            """.trimIndent()
        }
    }

}
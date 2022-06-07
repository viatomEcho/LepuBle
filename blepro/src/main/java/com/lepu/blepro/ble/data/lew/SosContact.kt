package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex
import java.nio.charset.StandardCharsets

class SosContact() {

    var switch = false
    var nameLen = 0
    var name = ""
    var phoneLen = 0
    var phone = ""

    constructor(bytes: ByteArray) : this() {
        var index = 0
        switch = byte2UInt(bytes[index]) == 1
        index++
        nameLen = byte2UInt(bytes[index])
        index++
//        name = trimStr(String(bytes.copyOfRange(index, index+nameLen), StandardCharsets.US_ASCII))
        name = trimStr(String(bytes.copyOfRange(index, index+nameLen), StandardCharsets.UTF_8))
        index += nameLen
        phoneLen = byte2UInt(bytes[index])
        index++
        phone = trimStr(String(bytes.copyOfRange(index, index+phoneLen), StandardCharsets.US_ASCII))
//        phone = trimStr(String(bytes.copyOfRange(index, index+phoneLen), StandardCharsets.UTF_8))
    }

    fun getDataBytes(): ByteArray {
        val on = if (switch) {
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
    }

    override fun toString(): String {
        return """
            SosContact : 
            bytes : ${bytesToHex(getDataBytes())}
            switch : $switch
            nameLen : $nameLen
            name : $name
            phoneLen : $phoneLen
            phone : $phone
        """.trimIndent()
    }

}
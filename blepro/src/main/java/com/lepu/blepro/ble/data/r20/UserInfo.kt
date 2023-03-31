package com.lepu.blepro.ble.data.r20

import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.HexString.trimStr
import java.nio.charset.StandardCharsets

class UserInfo() {
    var aid: Int = 0                // 主账户id
    var uid: Int = 0                // 用户id
    var fName: String = ""          // 姓,utf8格式
    var name: String = ""           // 名,utf8格式
    var birthday: String = "0-0-0"  // 生日 "1997-01-01"
    var height: Int = 0             // 身高 cm (init 170cm -> cmdSend 1700)
    var weight: Float = 0f          // 体重 kg (init 75.5kg -> cmdSend 755)
    var gender: Int = 0             // 性别 0：男 1：女
    // reserved 16
    var bytes = ByteArray(0)

    constructor(bytes: ByteArray) : this() {
        this.bytes = bytes
        var index = 0
        aid = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        uid = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
//        fName = trimStr(String(bytes.copyOfRange(index, index+32), StandardCharsets.US_ASCII))
        fName = trimStr(String(bytes.copyOfRange(index, index + 32), StandardCharsets.UTF_8))
        index += 32
//        name = trimStr(String(bytes.copyOfRange(index, index+32), StandardCharsets.US_ASCII))
        name = trimStr(String(bytes.copyOfRange(index, index + 32), StandardCharsets.UTF_8))
        index += 32
        birthday = "" + toUInt(bytes.copyOfRange(index, index+2)) + "-" + bytes[index+2].toInt() + "-" + bytes[index+3].toInt()
        index += 4
        height = toUInt(bytes.copyOfRange(index, index+2)).div(10)
        index += 2
        weight = toUInt(bytes.copyOfRange(index, index+2)).div(10f)
        index += 2
        gender = byte2UInt(bytes[index])
    }

    fun getDataBytes(): ByteArray {
        val fNameByteArray = ByteArray(32)
//        val fNameData = fName.toByteArray(StandardCharsets.US_ASCII)
        val fNameData = fName.toByteArray(StandardCharsets.UTF_8)
        if (fNameData.size > 32) {
            System.arraycopy(fNameData, 0, fNameByteArray, 0, 32)
        } else {
            System.arraycopy(fNameData, 0, fNameByteArray, 0, fNameData.size)
        }
        val nameByteArray = ByteArray(32)
//        val nameData = name.toByteArray(StandardCharsets.US_ASCII)
        val nameData = name.toByteArray(StandardCharsets.UTF_8)
        if (nameData.size > 32) {
            System.arraycopy(nameData, 0, nameByteArray, 0, 32)
        } else {
            System.arraycopy(nameData, 0, nameByteArray, 0, nameData.size)
        }
        return int4ByteArray(aid)
            .plus(int4ByteArray(uid))
            .plus(fNameByteArray)
            .plus(nameByteArray)
            .plus(int2ByteArray(birthday.split("-")[0].toInt()))
            .plus(birthday.split("-")[1].toInt().toByte())
            .plus(birthday.split("-")[2].toInt().toByte())
            .plus(int2ByteArray(height*10))
            .plus(int2ByteArray((weight*10).toInt()))
            .plus(gender.toByte())
            .plus(ByteArray(16))
    }

    override fun toString(): String {
        return """
            UserInfo : 
            bytes : ${bytesToHex(bytes)}
            getDataBytes : ${bytesToHex(getDataBytes())}
            aid : $aid
            uid : $uid
            fName : $fName
            name : $name
            birthday : $birthday
            height : $height
            weight : $weight
            gender : $gender
        """.trimIndent()
    }
}
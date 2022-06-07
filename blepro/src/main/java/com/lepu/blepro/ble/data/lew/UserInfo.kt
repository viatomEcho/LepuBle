package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.int2ByteArray
import com.lepu.blepro.utils.int4ByteArray
import com.lepu.blepro.utils.toUInt
import java.nio.charset.Charset

class UserInfo() {

    var aid: Int = 0                // 主账户id
    var uid: Int = 0                // 用户id
    var fName: String = ""          // 姓
    var name: String = ""           // 名
    var birthday: String = "0-0-0"  // 生日 "1997-01-01"
    var height: Int = 0             // 身高 cm (init 170cm -> cmdSend 1700)
    var weight: Float = 0f          // 体重 kg (init 75.5kg -> cmdSend 755)
    var gender: Int = 0             // 性别 0：男 1：女 LewBleCmd.Gender

    constructor(bytes: ByteArray) : this() {
        var index = 0
        aid = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        uid = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        fName = trimStr(String(bytes.copyOfRange(index, index+32), Charset.defaultCharset()))
        index += 32
        name = trimStr(String(bytes.copyOfRange(index, index+32), Charset.defaultCharset()))
        index += 32
        birthday = "" + toUInt(bytes.copyOfRange(index, index+2)) + "-" + bytes[index+2].toInt() + "-" + bytes[index+3].toInt()
        index += 4
        height = toUInt(bytes.copyOfRange(index, index+2)).div(10)
        index += 2
        weight = toUInt(bytes.copyOfRange(index, index+2)).div(10f)
        index += 2
        gender = (bytes[index].toUInt() and 0xFFu).toInt()
    }

    fun getDataBytes(): ByteArray {
        val fNameByteArray = ByteArray(32)
        val fNameData = fName.toByteArray(Charset.defaultCharset())
        System.arraycopy(fNameData, 0, fNameByteArray, 0, fNameData.size)
        val nameByteArray = ByteArray(32)
        val nameData = name.toByteArray(Charset.defaultCharset())
        System.arraycopy(nameData, 0, nameByteArray, 0, nameData.size)

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
            .plus(ByteArray(11))
    }

    override fun toString(): String {
        return """
            UserInfo : 
            bytes : ${bytesToHex(getDataBytes())}
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
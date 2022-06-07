package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.int2ByteArray
import com.lepu.blepro.utils.int4ByteArray
import com.lepu.blepro.utils.toUInt
import java.nio.charset.StandardCharsets

/**
 * bp2wifi用户信息
 * 必传参数：aid, uid, fName, name, birthday, height, weight, gender, icon(width, height, data)
 */
class LeBp2wUserInfo() {

    var len: Int = 0
    var aid: Int = 0                // 主账户id
    var uid: Int = 0                // 用户id
    var fName: String = ""          // 姓
    var name: String = ""           // 名
    var birthday: String = "0-0-0"  // 生日 "1997-01-01"
    var height: Int = 0             // 身高 cm (init 170cm -> cmdSend 1700)
    var weight: Float = 0f          // 体重 kg (init 75.5kg -> cmdSend 755)
    var gender: Int = 0             // 性别 0：男 1：女
    lateinit var icon: Icon

    constructor(bytes: ByteArray) : this() {
        var index = 0
        len = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        aid = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        uid = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
//        fName = trimStr(String(bytes.copyOfRange(index, index+32), StandardCharsets.US_ASCII))
        fName = trimStr(String(bytes.copyOfRange(index, index+32), StandardCharsets.UTF_8))
        index += 32
//        name = trimStr(String(bytes.copyOfRange(index, index+32), StandardCharsets.US_ASCII))
        name = trimStr(String(bytes.copyOfRange(index, index+32), StandardCharsets.UTF_8))
        index += 32
        birthday = "" + toUInt(bytes.copyOfRange(index, index+2)) + "-" + bytes[index+2].toInt() + "-" + bytes[index+3].toInt()
        index += 4
        height = toUInt(bytes.copyOfRange(index, index+2)).div(10)
        index += 2
        weight = toUInt(bytes.copyOfRange(index, index+2)).div(10f)
        index += 2
        gender = (bytes[index].toUInt() and 0xFFu).toInt()
        index += 12
        icon = Icon(bytes.copyOfRange(index, bytes.size))
    }

    class Icon() {
        var type: Int = 0             // 格式 0：bmp
        var width: Int = 0            // 宽
        var height: Int = 0           // 高
        var iconLen: Int = 0          // 图标内容长度
        lateinit var icon: ByteArray  // 图标内容

        constructor(bytes: ByteArray) : this() {
            var index = 0
            type = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            width = toUInt(bytes.copyOfRange(index, index + 2))
            index += 2
            height = toUInt(bytes.copyOfRange(index, index + 2))
            index += 2
            iconLen = toUInt(bytes.copyOfRange(index, index + 2))
            index += 2
            icon = bytes.copyOfRange(index, index + iconLen)
        }

        fun getDataBytes(): ByteArray {
            val data = byteArrayOf(type.toByte())
            return data.plus(int2ByteArray(width))
                .plus(int2ByteArray(height))
                .plus(int2ByteArray(icon.size))
                .plus(icon)
        }

        override fun toString(): String {
            return """
                type : $type
                width : $width
                height : $height
                iconLen : $iconLen
                icon : ${bytesToHex(icon)}
            """.trimIndent()
        }
    }

    fun getDataBytes(): ByteArray {
        len = 94 + icon.getDataBytes().size
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

        val data = int2ByteArray(len)
        return data.plus(int4ByteArray(aid))
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
            .plus(icon.getDataBytes())
    }

    override fun toString(): String {
        return """
            len : $len
            aid : $aid
            uid : $uid
            fName : $fName
            name : $name
            birthday : $birthday
            height : $height
            weight : $weight
            gender : $gender
            icon : $icon
        """.trimIndent()
    }

}
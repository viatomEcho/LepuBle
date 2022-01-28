package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.int2ByteArray
import com.lepu.blepro.utils.int4ByteArray
import com.lepu.blepro.utils.toUInt
import java.nio.charset.Charset

/**
 * bp2wifi用户信息
 * 必传参数：aid, uid, fName, name, birthday, height, weight, gender, icon(width, height, data)
 */
class Bp2wUserInfo() {

    var len: Int = 0
    var aid: Int = 0            // 主账户id
    var uid: Int = 0            // 用户id
    lateinit var fName: String  // 姓
    lateinit var name: String   // 名
    var birthday: Int = 0       // 生日
    var height: Int = 0         // 身高 cm
    var weight: Int = 0         // 体重 kg
    var gender: Int = 0         // 性别 0：男 1：女
    lateinit var icon: Icon

    constructor(bytes: ByteArray) : this() {
        var index = 0
        len = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        aid = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        uid = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        fName = trimStr(String(bytes.copyOfRange(index, index+32), Charset.defaultCharset()))
        index += 32
        name = trimStr(String(bytes.copyOfRange(index, index+32), Charset.defaultCharset()))
        index += 32
        birthday = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        height = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        weight = toUInt(bytes.copyOfRange(index, index+2))
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
    }

    fun getDataBytes(): ByteArray {
        len = 94 + icon.getDataBytes().size
        val fNameByteArray = ByteArray(32)
        val fNameData = fName.toByteArray(Charset.defaultCharset())
        System.arraycopy(fNameData, 0, fNameByteArray, 0, fNameData.size)
        val nameByteArray = ByteArray(32)
        val nameData = name.toByteArray(Charset.defaultCharset())
        System.arraycopy(nameData, 0, nameByteArray, 0, nameData.size)

        val data = int2ByteArray(len)
        return data.plus(int4ByteArray(aid))
            .plus(int4ByteArray(uid))
            .plus(fNameByteArray)
            .plus(nameByteArray)
            .plus(int4ByteArray(birthday))
            .plus(int2ByteArray(height))
            .plus(int2ByteArray(weight))
            .plus(gender.toByte())
            .plus(ByteArray(11))
            .plus(icon.getDataBytes())
    }

}
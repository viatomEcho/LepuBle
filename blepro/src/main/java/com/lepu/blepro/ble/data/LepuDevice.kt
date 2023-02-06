package com.lepu.blepro.ble.data

import android.os.Parcelable
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import kotlinx.android.parcel.Parcelize

@ExperimentalUnsignedTypes
@Parcelize
class LepuDevice constructor(var bytes: ByteArray) : Parcelable {
    var hwV: Char            // 硬件版本
    var fwV: String          // 固件版本
    var btlV: String         // 引导版本
    var branchCode: String   // Branch编码
    var fileV: Int           // 文件系统版本
    // reserve 2
    var deviceType: Int      // 设备类型
    var protocolV: String    // 协议版本
    var curTime: String      // 时间
    var protocolMaxLen: Int  // 通信协议数据段最大长度
    // reserve 4
    var snLen: Int           // SN长度(小于18)
    var sn: String           // SN号


    // reserve 4

    init {
        hwV = bytes[0].toChar()
        fwV = "${bytes[4].toUInt()}.${bytes[3].toUInt()}.${bytes[2].toUInt()}.${bytes[1].toUInt()}"
        btlV = "${bytes[8].toUInt()}.${bytes[7].toUInt()}.${bytes[6].toUInt()}.${bytes[5].toUInt()}"
        branchCode = String(bytes.copyOfRange(9, 17))
        fileV = (bytes[17].toUInt() and 0xFFu).toInt()
        deviceType = toUInt(bytes.copyOfRange(20, 22))
        protocolV = "${bytes[22].toUInt()}.${bytes[23].toUInt()}"
        val year = toUInt(bytes.copyOfRange(24, 26))
        val month = (bytes[26].toUInt() and 0xFFu).toInt()
        val day = (bytes[27].toUInt() and 0xFFu).toInt()
        val hour = (bytes[28].toUInt() and 0xFFu).toInt()
        val min = (bytes[29].toUInt() and 0xFFu).toInt()
        val second = (bytes[30].toUInt() and 0xFFu).toInt()
        curTime = "$year/$month/$day $hour:$min:$second"
        protocolMaxLen = toUInt(bytes.copyOfRange(21, 23))
        snLen = (bytes[37].toUInt() and 0xFFu).toInt()
        if (snLen > 18) {
            snLen = 18
        }
        sn = trimStr(String(bytes.copyOfRange(38, 38+ snLen)))
    }

    override fun toString(): String {
        return """
            LepuDevice :
            bytes : ${bytesToHex(bytes)}
            hwV : $hwV
            fwV : $fwV
            btlV : $btlV
            branchCode : $branchCode
            fileV : $fileV
            deviceType : $deviceType
            protocolV : $protocolV
            curTime : $curTime
            protocolMaxLen : $protocolMaxLen
            snLen : $snLen
            sn : $sn
        """.trimIndent()
    }
}
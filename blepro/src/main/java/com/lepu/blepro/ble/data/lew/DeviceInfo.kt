package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.ble.cmd.LewBleCmd
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.DateUtil.getSecondTimestamp
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.getTimeString
import com.lepu.blepro.utils.toUInt

class DeviceInfo(val bytes: ByteArray) {

    var hwV: Char            // 硬件版本
    var fwV: String          // 固件版本
    var btlV: String         // 引导版本
//    var branchCode: String   // Branch编码
    var fileV: Int           // 文件系统版本
    // reserve 2
//    var deviceType: Int      // 设备类型
    var protocolV: String    // 协议版本
    var year: Int
    var month: Int
    var day: Int
    var hour: Int
    var minute: Int
    var second: Int
    var curTime: Long        // 时间戳s
    var protocolMaxLen: Int  // 通信协议数据段最大长度
    // reserve 4
    var snLen: Int           // SN长度(小于18)
    var sn: String           // SN号
    var deviceMode: Int      // 设备模式 LewBleCmd.DeviceMode
    var deviceModeMess: String
    // reserve 3

    init {
        var index = 0
        hwV = bytes[index].toChar()
        index++
        fwV = "${byte2UInt(bytes[index+3])}.${byte2UInt(bytes[index+2])}.${byte2UInt(bytes[index+1])}.${byte2UInt(bytes[index])}"
        index += 4
        btlV = "${byte2UInt(bytes[index+3])}.${byte2UInt(bytes[index+2])}.${byte2UInt(bytes[index+1])}.${byte2UInt(bytes[index])}"
        index += 4
//        branchCode = trimStr(String(bytes.copyOfRange(index, index+8)))
        index += 8
        fileV = byte2UInt(bytes[index])
        index++
        // reserve 2
        index += 2
//        deviceType = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        protocolV = "${byte2UInt(bytes[index+1])}.${byte2UInt(bytes[index])}"
        index += 2
        year = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        month = byte2UInt(bytes[index])
        index++
        day = byte2UInt(bytes[index])
        index++
        hour = byte2UInt(bytes[index])
        index++
        minute = byte2UInt(bytes[index])
        index++
        second = byte2UInt(bytes[index])
        curTime = getSecondTimestamp(getTimeString(year, month, day, hour, minute, second))
        index++
        protocolMaxLen = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        // reserve 4
        index += 4
        snLen = byte2UInt(bytes[index])
        index++
        sn = trimStr(String(bytes.copyOfRange(index, index+snLen)))
        index += 18
        deviceMode = byte2UInt(bytes[index])
        deviceModeMess = getModeMess(deviceMode)
    }

    private fun getModeMess(mode: Int): String {
        return when (mode) {
            LewBleCmd.DeviceMode.MODE_NORMAL -> "普通模式"
//            LewBleCmd.DeviceMode.MODE_MONITOR -> "监护模式"
            LewBleCmd.DeviceMode.MODE_FREE -> "省心模式"
            else -> ""
        }
    }

    override fun toString(): String {
        return """
            DeviceInfo : 
            bytes : ${bytesToHex(bytes)}
            hwV : $hwV
            fwV : $fwV
            btlV : $btlV
            fileV : $fileV
            protocolV : $protocolV
            year : $year
            month : $month
            day : $day
            hour : $hour
            minute : $minute
            second : $second
            curTime : $curTime
            protocolMaxLen : $protocolMaxLen
            snLen : $snLen
            sn : $sn
            deviceMode : $deviceMode
        """.trimIndent()
    }

}
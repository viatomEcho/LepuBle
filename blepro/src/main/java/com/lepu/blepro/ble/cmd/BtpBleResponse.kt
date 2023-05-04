package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.HexString.trimStr
import java.util.*

object BtpBleResponse {

    @ExperimentalUnsignedTypes
    class BleResponse (val bytes: ByteArray) {
        var cmd: Int
        var pkgType: Int
        var pkgNo: Int
        var len: Int
        var content: ByteArray

        init {
            cmd = byte2UInt(bytes[1])
            pkgType = byte2UInt(bytes[3])
            pkgNo = byte2UInt(bytes[4])
            len = toUInt(bytes.copyOfRange(5, 7))
            content = bytes.copyOfRange(7, 7+len)
        }
    }

    @ExperimentalUnsignedTypes
    class ConfigInfo(val bytes: ByteArray) {

        var hrSwitch: Boolean     // 心率提醒开关
        var lightSwitch: Boolean  // 工作时灯开关
        var tempSwitch: Boolean   // 温度提醒开关
        var hrLowThr: Int         // 心率提醒最低阈值，30-110，5递增，默认80
        var hrHighThr: Int        // 心率提醒最高阈值，120-220，5递增，默认150
        var tempUnit: Int         // 温度单位，0摄氏度，1华氏度
        var tempLowThr: Int       // 温度提醒最低阈值，30-34℃（86-93℉），1递增，默认34℃（93℉）
        var tempHighThr: Int      // 温度提醒最高阈值，37-40℃（98-104℉），1递增，默认37℃（98℉）
        var utcTime: Int          // 时区，默认8时区，80
        // reserved 8

        init {
            var index = 0
            hrSwitch = (byte2UInt(bytes[index]) and 0x01) == 1
            lightSwitch = ((byte2UInt(bytes[index]) and 0x02) shr 1) == 1
            tempSwitch = ((byte2UInt(bytes[index]) and 0x04) shr 2) == 1
            index++
            hrLowThr = byte2UInt(bytes[index])
            index++
            hrHighThr = byte2UInt(bytes[index])
            index++
            tempUnit = byte2UInt(bytes[index])
            index++
            tempLowThr = toUInt(bytes.copyOfRange(index, index+2)).div(100)
            index += 2
            tempHighThr = toUInt(bytes.copyOfRange(index, index+2)).div(100)
            index += 2
            utcTime = byte2UInt(bytes[index])
        }

        override fun toString(): String {
            return """
            BtpConfig : 
            bytes : ${bytesToHex(bytes)}
            hrSwitch : $hrSwitch
            lightSwitch : $lightSwitch
            tempSwitch : $tempSwitch
            hrLowThr : $hrLowThr
            hrHighThr : $hrHighThr
            tempUnit : $tempUnit
            tempLowThr : $tempLowThr
            tempHighThr : $tempHighThr
            utcTime : $utcTime
        """.trimIndent()
        }

    }

    @ExperimentalUnsignedTypes
    class RtData(val bytes: ByteArray) {
        var hr: Int
        var level: Int          // 0-100
        var isWearing: Boolean  // 穿戴状态
        var hrStatus: Int       // 心率状态，0正常，1心率低异常，2心率高异常
        var tempStatus: Int     // 体温状态，0正常，3温度低异常，4温度高异常
        var temp: Float         // 单位摄氏度
        // reserved 3

        init {
            var index = 0
            hr = byte2UInt(bytes[index])
            index++
            level = byte2UInt(bytes[index])
            index++
            isWearing = byte2UInt(bytes[index]) == 1
            index++
            hrStatus = byte2UInt(bytes[index])
            index++
            tempStatus = byte2UInt(bytes[index])
            index++
            temp = toUInt(bytes.copyOfRange(index, index+2)).div(100f)
        }

        override fun toString(): String {
            return """
                RtData : 
                bytes : ${bytesToHex(bytes)}
                hr : $hr
                level : $level
                isWearing : $isWearing
                hrStatus : $hrStatus
                tempStatus : $tempStatus
                temp : $temp
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class FileList(val bytes: ByteArray) {
        var fileSize: Int
        var fileNames = mutableListOf<String>()
        init {
            var index = 0
            fileSize = byte2UInt(bytes[index])
            index++
            for (i in 0 until fileSize) {
                fileNames.add(trimStr(String(bytes.copyOfRange(index, index+16))))
                index += 16
            }
        }

        override fun toString(): String {
            return """
                FileList : 
                fileSize : $fileSize
                fileNames : $fileNames
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class BtpFile(val bytes: ByteArray) {
        var fileVersion: Int  // 文件版本，0x01：V1
        var fileType: Int     // 文件类型
        // reserved 8
        var magic: Int        // 幻数
        var timestamp: Long   // 测量时间戳s
        var duration: Int     // 测量时长，单位：分钟
        // reserved 4
        var pointDatas = mutableListOf<PointData>()
        init {
            var index = 0
            fileVersion = byte2UInt(bytes[index])
            index++
            fileType = byte2UInt(bytes[index])
            index++
            index += 8
            magic = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            timestamp = toLong(bytes.copyOfRange(index, index+4)) - DateUtil.getTimeZoneOffset().div(1000)
            index += 4
            duration = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            index += 4
            val len = (bytes.size - index).div(12)
            for (i in 0 until len) {
                pointDatas.add(PointData(bytes.copyOfRange(index, index+12)))
                index += 12
            }
        }
        override fun toString(): String {
            return """
                BtpFile : 
                bytes : ${bytesToHex(bytes)}
                fileVersion : $fileVersion
                fileType : $fileType
                magic : $magic
                timestamp : $timestamp
                duration : $duration
                pointDatas : $pointDatas
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class PointData(val bytes: ByteArray) {
        var timestamp: Long  // 每一分钟的时间点（可获取指定时间段，预留）s
        var hrEvent: Int     // 心率事件
        var hr: Int          // 心率
        var tempEvent: Int   // 温度事件
        var temp: Float      // 温度，单位：摄氏度
        // reserved 3
        init {
            var index = 0
            timestamp = toLong(bytes.copyOfRange(index, index+4)) - DateUtil.getTimeZoneOffset().div(1000)
            index += 4
            hrEvent = byte2UInt(bytes[index])
            index++
            hr = byte2UInt(bytes[index])
            index++
            tempEvent = byte2UInt(bytes[index])
            index++
            temp = toUInt(bytes.copyOfRange(index, index+2)).div(100f)
        }
        override fun toString(): String {
            return """
                PointData : 
                timestamp : $timestamp
                hrEvent : $hrEvent
                hr : $hr
                tempEvent : $tempEvent
                temp : $temp
            """.trimIndent()
        }
    }

}
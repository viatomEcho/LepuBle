package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.DateUtil
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.toLong
import com.lepu.blepro.utils.toUInt
import java.util.*

object Pf10Aw1BleResponse {

    class FileList(val bytes: ByteArray) {
        var size: Int
        var fileNames = mutableListOf<String>()
        init {
            var index = 0
            size = byte2UInt(bytes[index])
            index++
            for (i in 0 until size) {
                fileNames.add(trimStr(String(bytes.copyOfRange(index, index + 16))))
                index += 16
            }
        }
        override fun toString(): String {
            return """
                FileList : 
                size : $size
                fileNames : $fileNames
            """.trimIndent()
        }
    }

    class BleFile(val bytes: ByteArray) {
        var fileVersion: Int                 // 文件版本
        var fileType: Int                    // 文件类型，3：血氧
        // reserved 6
        var deviceModel: Int                 // 设备型号，4：源动血氧产品
        var spo2List = mutableListOf<Int>()
        var prList = mutableListOf<Int>()
        var checkSum: Long                   // 文件头部+数据点和校验
        var magic: Long                      // 文件标志 固定值为0xDA5A1248
        var startTime: Long                  // 开始测量时间
        var size: Int                        // 记录点数
        var interval: Int                    // 存储间隔
        var channelType: Int                 // 通道类型，0：CHANNAL_SPO2_PR，1：CHANNEL_SPO2_PR_MOTION
        var channelBytes: Int                // 单个通道字节数
        // reserved 29
        init {
            var index = 0
            fileVersion = byte2UInt(bytes[index])
            index++
            fileType = byte2UInt(bytes[index])
            index++
            index += 6
            deviceModel = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            val len = bytes.size - 10 - 48
            for (i in 0 until len.div(2)) {
                spo2List.add(byte2UInt(bytes[index]))
                prList.add(byte2UInt(bytes[index+1]))
                index += 2
            }
            checkSum = toLong(bytes.copyOfRange(index, index+4))
            index += 4
            magic = toLong(bytes.copyOfRange(index, index+4))
            index += 4
//            val rawOffset = DateUtil.getTimeZoneOffset().div(1000)
//            val defaultTime = toLong(bytes.copyOfRange(index, index+4))
//            startTime = defaultTime - rawOffset
            startTime = toLong(bytes.copyOfRange(index, index+4))
            index += 4
            size = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            interval = byte2UInt(bytes[index])
            index++
            channelType = byte2UInt(bytes[index])
            index++
            channelBytes = byte2UInt(bytes[index])
        }
        override fun toString(): String {
            return """
                BleFile : 
                fileVersion : $fileVersion
                fileType : $fileType
                deviceModel : $deviceModel
                checkSum : $checkSum
                magic : $magic
                startTime : $startTime
                startTime : ${DateUtil.stringFromDate(Date(startTime.times(1000)), "yyyy-MM-dd HH:mm:ss")}
                size : $size
                interval : $interval
                channelType : $channelType
                channelBytes : $channelBytes
                spo2List : ${spo2List.joinToString(",")}
                prList : ${prList.joinToString(",")}
            """.trimIndent()
        }
    }

    class RtParam(val bytes: ByteArray) {
        var spo2: Int
        var pr: Int
        var pi: Float
        var probeOff: Boolean  // 手指未接入
        var batLevel: Int      // 电量等级，0-3
        init {
            var index = 0
            spo2 = byte2UInt(bytes[index])
            index++
            pr = byte2UInt(bytes[index])
            index++
            pi = byte2UInt(bytes[index]).div(10f)
            index++
            probeOff = ((byte2UInt(bytes[index]) and 0x02) shr 1) == 1
            index++
            batLevel = (byte2UInt(bytes[index]) and 0xC0) shr 6
        }
        override fun toString(): String {
            return """
                RtParam : 
                spo2 : $spo2
                pr : $pr
                pi : $pi
                probeOff : $probeOff
                batLevel : $batLevel
            """.trimIndent()
        }
    }

    class RtWave(val bytes: ByteArray) {
        var waveData: ByteArray
        var waveIntData: IntArray
        var waveIntReData: IntArray  // 倒置数据
        init {
            waveData = bytes.copyOfRange(0, 5).toList().asSequence().map { (it.toInt() and 0x7f).toByte() }.toList().toByteArray()
            waveIntData = bytes.copyOfRange(0, 5).toList().asSequence().map { (it.toInt() and 0x7f)}.toList().toIntArray()
            waveIntReData = bytes.copyOfRange(0, 5).toList().asSequence().map { (127-(it.toInt() and 0x7f))}.toList().toIntArray()
        }
    }

    class WorkingStatus(val bytes: ByteArray) {
        var mode: Int   // 模式（1：点测 2：连续 3：菜单）
        var step: Int   // 状态（0：idle 1：准备阶段 2：正在测量 3：播报血氧结果 4：脉率分析 5：点测完成）
        var para1: Int
        var para2: Int
        init {
            var index = 0
            mode = byte2UInt(bytes[index])
            index++
            step = byte2UInt(bytes[index])
            index++
            para1 = byte2UInt(bytes[index])
            index++
            para2 = byte2UInt(bytes[index])
        }
        override fun toString(): String {
            return """
                WorkingStatus : 
                mode : $mode
                step : $step
                para1 : $para1
                para2 : $para2
            """.trimIndent()
        }
    }
}
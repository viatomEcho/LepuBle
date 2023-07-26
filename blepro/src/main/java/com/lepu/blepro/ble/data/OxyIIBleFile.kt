package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils
import com.lepu.blepro.utils.DateUtil
import com.lepu.blepro.utils.toLong
import com.lepu.blepro.utils.toUInt
import java.util.*

class OxyIIBleFile(val bytes: ByteArray) {
    var fileVersion: Int                        // 文件版本
    var fileType: Int                           // 文件类型，3：血氧
    // reserved 6
    var deviceModel: Int                        // 设备型号，4：源动血氧产品
    var spo2List = mutableListOf<Int>()
    var prList = mutableListOf<Int>()
    var motionList = mutableListOf<Int>()       // 体动
    var remindHrs = mutableListOf<Boolean>()    // 心率提醒标志位
    var remindsSpo2 = mutableListOf<Boolean>()  // 血氧提醒标志位
    var checkSum: Long                          // 文件头部+数据点和校验
    var magic: Long                             // 文件标志 固定值为0xDA5A1248
    var startTime: Long                         // 开始测量时间
    var size: Int                               // 记录点数
    var interval: Int                           // 存储间隔
    var channelType: Int                        // 通道类型，0：CHANNAL_SPO2_PR，1：CHANNEL_SPO2_PR_MOTION
    var channelBytes: Int                       // 单个通道字节数
    // reserved 13
    var asleepTime: Int                         // 睡着时间，Reserved for total asleep time future
    var avgSpo2: Int                            // 平均血氧，Average blood oxygen saturation
    var minSpo2: Int                            // 最低血氧，Minimum blood oxygen saturation
    var dropsTimes3Percent: Int                 // 3%drops，drops below baseline - 3
    var dropsTimes4Percent: Int                 // 4%drops，drops below baseline - 4
    var percentLessThan90: Int                  // <90%占总时间百分比，单位%，T90 = (<90% duration time) / (total recording time) *100%
    var durationTime90Percent: Int              // <90%持续时间，单位s，Duration time when SpO2 lower than 90%
    var dropsTimes90Percent: Int                // <90%跌落次数，Reserved for drop times when SpO2 lower than 90%
    var o2Score: Int                            // O2得分，Range: 0~100 For range 0~10, should be (O2 Score) / 10
    var stepCounter: Int                        // 计步结果，Total steps
    var avgHr: Int                              // 平均心率
    init {
        var index = 0
        fileVersion = ByteUtils.byte2UInt(bytes[index])
        index++
        fileType = ByteUtils.byte2UInt(bytes[index])
        index++
        index += 6
        deviceModel = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        val len = bytes.size - 10 - 48
        for (i in 0 until len.div(3)) {
            spo2List.add(ByteUtils.byte2UInt(bytes[index]))
            index++
            prList.add(ByteUtils.byte2UInt(bytes[index]))
            index++
            motionList.add((ByteUtils.byte2UInt(bytes[index]) and 0xFC) shr 2)
            remindHrs.add(((ByteUtils.byte2UInt(bytes[index]) and 0x02) shr 1) == 1)
            remindsSpo2.add((ByteUtils.byte2UInt(bytes[index]) and 0x01) == 1)
            index++
        }
        checkSum = toLong(bytes.copyOfRange(index, index+4))
        index += 4
        magic = toLong(bytes.copyOfRange(index, index+4))
        index += 4
        val rawOffset = DateUtil.getTimeZoneOffset().div(1000)
        val defaultTime = toLong(bytes.copyOfRange(index, index+4))
        startTime = defaultTime - rawOffset
        index += 4
        size = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        interval = ByteUtils.byte2UInt(bytes[index])
        index++
        channelType = ByteUtils.byte2UInt(bytes[index])
        index++
        channelBytes = ByteUtils.byte2UInt(bytes[index])
        index++
        index += 13
        asleepTime = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        avgSpo2 = ByteUtils.byte2UInt(bytes[index])
        index++
        minSpo2 = ByteUtils.byte2UInt(bytes[index])
        index++
        dropsTimes3Percent = ByteUtils.byte2UInt(bytes[index])
        index++
        dropsTimes4Percent = ByteUtils.byte2UInt(bytes[index])
        index++
        percentLessThan90 = ByteUtils.byte2UInt(bytes[index])
        index++
        durationTime90Percent = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        dropsTimes90Percent = ByteUtils.byte2UInt(bytes[index])
        index++
        o2Score = ByteUtils.byte2UInt(bytes[index])
        index++
        stepCounter = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        avgHr = ByteUtils.byte2UInt(bytes[index])
    }
    override fun toString(): String {
        return """
        OxyIIBleFile : 
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
        asleepTime : $asleepTime
        avgSpo2 : $avgSpo2
        minSpo2 : $minSpo2
        dropsTimes3Percent : $dropsTimes3Percent
        dropsTimes4Percent : $dropsTimes4Percent
        percentLessThan90 : $percentLessThan90
        durationTime90Percent : $durationTime90Percent
        dropsTimes90Percent : $dropsTimes90Percent
        o2Score : $o2Score
        stepCounter : $stepCounter
        avgHr : $avgHr
        spo2List : ${spo2List.joinToString(",")}
        prList : ${prList.joinToString(",")}
        motionList : ${motionList.joinToString(",")}
        remindHrs : ${remindHrs.joinToString(",")}
        remindsSpo2 : ${remindsSpo2.joinToString(",")}
        """.trimIndent()
    }
}
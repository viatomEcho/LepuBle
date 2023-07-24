package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.toSignedShort
import java.util.*

object OxyIIBleResponse {

    class RtParam(val bytes: ByteArray) {
        var duration: Int        // 记录时长 单位秒
        var runStatus: Int       // 运行状态 0：准备阶段 1：测量准备阶段 2：测量中 3：测量结束
        var sensorState: Int     // 传感器状态 0：正常状态，1：导联脱落，未放手指，2：探头拔出，3：传感器或探头故障
        var spo2: Int
        var pi: Float
        var pr: Int
        var flag: Int            // 标志参数 bit0：脉搏音标志
        var motion: Int          // 体动
        var batteryState: Int    // 电池状态 0：正常使用，1：充电中，2：充满，3：低电量
        var batteryPercent: Int  // 电池电量百分比
        // reserved 6
        init {
            var index = 0
            duration = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            runStatus = byte2UInt(bytes[index])
            index++
            sensorState = byte2UInt(bytes[index])
            index++
            spo2 = byte2UInt(bytes[index])
            index++
            pi = byte2UInt(bytes[index]).div(10f)
            index++
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            flag = byte2UInt(bytes[index])
            index++
            motion = byte2UInt(bytes[index])
            index++
            batteryState = byte2UInt(bytes[index])
            index++
            batteryPercent = byte2UInt(bytes[index])
        }
        override fun toString(): String {
            return """
                RtParam :
                duration : $duration
                runStatus : $runStatus
                sensorState : $sensorState
                spo2 : $spo2
                pi : $pi
                pr : $pr
                flag : $flag
                motion : $motion
                batteryState : $batteryState
                batteryPercent : $batteryPercent
            """.trimIndent()
        }
    }

    class RtWave(val bytes: ByteArray) {
        var offset: Int
        var size: Int
        var wave: ByteArray
        var waveInt: IntArray
        init {
            var index = 0
            offset = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            size = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            wave = bytes.copyOfRange(index, index+size)
            waveInt = IntArray(size)
            for (i in 0 until size) {
                var temp = byte2UInt(wave[i])
                // 处理毛刺
                if (temp == 156 || temp == 246) {
                    if (i==0) {
                        if ((i+1) < size)
                            temp = byte2UInt(wave[i+1])
                    } else if (i == size-1) {
                        temp = byte2UInt(wave[i-1])
                    } else {
                        if ((i+1) < size)
                            temp = (byte2UInt(wave[i-1]) + byte2UInt(wave[i+1]))/2
                    }
                }
                waveInt[i] = temp
            }
        }
        override fun toString(): String {
            return """
                RtWave : 
                offset : $offset
                size : $size
                wave : ${bytesToHex(wave)}
                waveInt : ${waveInt.joinToString(",")}
            """.trimIndent()
        }
    }

    class RtData(val bytes: ByteArray) {
        var param: RtParam
        var wave: RtWave
        init {
            var index = 0
            param = RtParam(bytes.copyOfRange(index, index+20))
            index += 20
            wave = RtWave(bytes.copyOfRange(index, bytes.size))
        }
        override fun toString(): String {
            return """
                RtData : 
                param : $param
                wave : $wave
            """.trimIndent()
        }
    }

    class RtPpg(val bytes: ByteArray) {
        var size: Int
        var irArray = mutableListOf<Short>()
        var redArray = mutableListOf<Short>()
        var motionArray = mutableListOf<Int>()
        init {
            var index = 0
            size = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            for (i in 0 until size) {
                irArray.add(toSignedShort(bytes[index], bytes[index+1]))
                index += 2
                redArray.add(toSignedShort(bytes[index], bytes[index+1]))
                index += 2
                motionArray.add(byte2UInt(bytes[index]))
                index++
            }
        }
        override fun toString(): String {
            return """
                RtPpg : 
                size : $size
                irArray : ${irArray.joinToString(",")}
                redArray : ${redArray.joinToString(",")}
                motionArray : ${motionArray.joinToString(",")}
            """.trimIndent()
        }
    }

    class FileList(val bytes: ByteArray) {
        var size: Int
        var fileNames = mutableListOf<String>()
        init {
            var index = 0
            size = byte2UInt(bytes[index])
            index++
            for (i in 0 until size) {
                fileNames.add(HexString.trimStr(String(bytes.copyOfRange(index, index + 16))))
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
        var fileVersion: Int                        // 文件版本
        var fileType: Int                           // 文件类型，3：血氧
        // reserved 6
        var deviceModel: Int                        // 设备型号，4：源动血氧产品
        var spo2List = mutableListOf<Int>()
        var prList = mutableListOf<Int>()
        var motionList = mutableListOf<Int>()       // 体动
        var remindHrs = mutableListOf<Boolean>()    // 心率提醒标志位
        var remindsSpo2 = mutableListOf<Boolean>()  // 血氧提醒标志位
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
        var checkSum: Long                          // 文件头部+数据点和校验
        var magic: Long                             // 文件标志 固定值为0xDA5A1248
        var startTime: Long                         // 开始测量时间
        var size: Int                               // 记录点数
        var interval: Int                           // 存储间隔
        var channelType: Int                        // 通道类型，0：CHANNAL_SPO2_PR，1：CHANNEL_SPO2_PR_MOTION
        var channelBytes: Int                       // 单个通道字节数
        // reserved 13
        init {
            var index = 0
            fileVersion = byte2UInt(bytes[index])
            index++
            fileType = byte2UInt(bytes[index])
            index++
            index += 6
            deviceModel = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            val len = bytes.size - 10 - 16 - 48
            for (i in 0 until len.div(5)) {
                spo2List.add(byte2UInt(bytes[index]))
                index++
                prList.add(byte2UInt(bytes[index]))
                index++
                motionList.add(byte2UInt(bytes[index]))
                index++
                remindHrs.add(byte2UInt(bytes[index]) == 1)
                index++
                remindsSpo2.add(byte2UInt(bytes[index]) == 1)
                index++
            }
            asleepTime = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            avgSpo2 = byte2UInt(bytes[index])
            index++
            minSpo2 = byte2UInt(bytes[index])
            index++
            dropsTimes3Percent = byte2UInt(bytes[index])
            index++
            dropsTimes4Percent = byte2UInt(bytes[index])
            index++
            percentLessThan90 = byte2UInt(bytes[index])
            index++
            durationTime90Percent = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            dropsTimes90Percent = byte2UInt(bytes[index])
            index++
            o2Score = byte2UInt(bytes[index])
            index++
            stepCounter = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            avgHr = byte2UInt(bytes[index])
            index++
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
                motionList : ${motionList.joinToString(",")}
                remindHrs : ${remindHrs.joinToString(",")}
                remindsSpo2 : ${remindsSpo2.joinToString(",")}
            """.trimIndent()
        }
    }
}
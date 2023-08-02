package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.DateUtil.getSecondTimestamp
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.getTimeString
import com.lepu.blepro.utils.toUInt

class OxyBleFile(val bytes: ByteArray) {

    var version: Int
    var operationMode: Int          // Operation Mode, 0 for Sleep Mode, 1 for Minitor Mode
    var year: Int
    var month: Int
    var day: Int
    var hour: Int
    var minute: Int
    var second: Int
    var startTime: Long             // timestamp s
    var size: Int                   // Total bytes of this data file package
    var recordingTime: Int          // Total recording time
    var asleepTime: Int             // Reserved for total asleep time future
    var avgSpo2: Int                // Average blood oxygen saturation
    var minSpo2: Int                // Minimum blood oxygen saturation
    var dropsTimes3Percent: Int     // drops below baseline - 3
    var dropsTimes4Percent: Int     // drops below baseline - 4
    var asleepTimePercent: Int      // T90 = (<90% duration time) / (total recording time) *100%
    var durationTime90Percent: Int  // Duration time when SpO2 lower than 90%
    var dropsTimes90Percent: Int    // Reserved for drop times when SpO2 lower than 90%
    var o2Score: Int                // Range: 0~100 For range 0~10, should be (O2 Score) / 10
    var stepCounter: Int            // Total steps
    // reserved 10
    var data = mutableListOf<EachData>()

    init {
        var index = 0
        version = byte2UInt(bytes[index])
        index++
        operationMode = byte2UInt(bytes[index])
        index++
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
        startTime = getSecondTimestamp(getTimeString(year, month, day, hour, minute, second))
        index++
        size = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        recordingTime = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
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
        asleepTimePercent = byte2UInt(bytes[index])
        index++
        durationTime90Percent = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        dropsTimes90Percent = byte2UInt(bytes[index])
        index++
        o2Score = byte2UInt(bytes[index])
        index++
        stepCounter = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        index += 10
        val len = (bytes.size - index).div(5)
        for (i in 0 until len) {
            data.add(EachData(bytes.copyOfRange(index+i*5, index+(i+1)*5)))
        }
    }

    override fun toString(): String {
        return """
            OxyBleFile : 
            version : $version
            operationMode : $operationMode
            year : $year
            month : $month
            day : $day
            hour : $hour
            minute : $minute
            second : $second
            startTime : $startTime
            startTime : ${getTimeString(year, month, day, hour, minute, second)}
            size : $size
            recordingTime : $recordingTime
            asleepTime : $asleepTime
            avgSpo2 : $avgSpo2
            minSpo2 : $minSpo2
            dropsTimes3Percent : $dropsTimes3Percent
            dropsTimes4Percent : $dropsTimes4Percent
            asleepTimePercent : $asleepTimePercent
            durationTime90Percent : $durationTime90Percent
            dropsTimes90Percent : $dropsTimes90Percent
            o2Score : $o2Score
            stepCounter : $stepCounter
            data.size : ${data.size}
            data : $data
        """.trimIndent()
    }

    class EachData(val bytes: ByteArray) {
        var spo2: Int
        var pr: Int
        var vector: Int                  // 加速度值，体动
        var warningSignSpo2: Boolean     // "低血氧告警"标记
        var warningSignPr: Boolean       // "脉率告警"标记
        var warningSignVector: Boolean   // "体动告警"标记
        var warningSignInvalid: Boolean  // "无效值告警"标记
        init {
            var index = 0
            spo2 = byte2UInt(bytes[index])
            index++
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            vector = byte2UInt(bytes[index])
            index++
            warningSignSpo2 = (byte2UInt(bytes[index]) and 0x80) == 1
            warningSignPr = (byte2UInt(bytes[index]) and 0x40) == 1
            warningSignVector = (byte2UInt(bytes[index]) and 0x20) == 1
            warningSignInvalid = (byte2UInt(bytes[index]) and 0x10) == 1
        }
        override fun toString(): String {
            return """
                EachData : 
                bytes : ${bytesToHex(bytes)}
                spo2 : $spo2
                pr : $pr
                vector : $vector
                warningSignSpo2 : $warningSignSpo2
                warningSignPr : $warningSignPr
                warningSignVector : $warningSignVector
                warningSignInvalid : $warningSignInvalid
            """.trimIndent()
        }
    }
}
package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.DateUtil.getSecondTimestamp
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt

class O2OxyFile(val bytes: ByteArray) {

    var version: Int
    var operationMode: Int
    var year: Int
    var month: Int
    var day: Int
    var hour: Int
    var minute: Int
    var second: Int
    var startTime: Int
    var size: Int
    var recordingTime: Int
    var asleepTime: Int
    var avgSpo2: Int
    var minSpo2: Int
    var dropsTimes3Percent: Int
    var dropsTimes4Percent: Int
    var asleepTimePercent: Int
    var durationTime90Percent: Int
    var dropsTimes90Percent: Int
    var o2Score: Int
    var stepCounter: Int
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
        startTime = getSecondTimestamp(getTimeString()).toInt()
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

    private fun getTimeString(): String {
        val monthStr = if (month < 10) {
            "0$month"
        } else {
            "$month"
        }
        val dayStr = if (day < 10) {
            "0$day"
        } else {
            "$day"
        }
        val hourStr = if (hour < 10) {
            "0$hour"
        } else {
            "$hour"
        }
        val minuteStr = if (minute < 10) {
            "0$minute"
        } else {
            "$minute"
        }
        val secondStr = if (second < 10) {
            "0$second"
        } else {
            "$second"
        }
        return "$year$monthStr$dayStr$hourStr$minuteStr$secondStr"
    }

    override fun toString(): String {
        return """
            O2OxyFile : 
            version : $version
            operationMode : $operationMode
            startTime : $startTime
            startTime : ${getTimeString()}
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
        var vector: Int
        // reserved 1
        init {
            var index = 0
            spo2 = byte2UInt(bytes[index])
            index++
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            vector = byte2UInt(bytes[index])
        }
        override fun toString(): String {
            return """
                EachData : 
                bytes : ${bytesToHex(bytes)}
                spo2 : $spo2
                pr : $pr
                vector : $vector
            """.trimIndent()
        }
    }
}
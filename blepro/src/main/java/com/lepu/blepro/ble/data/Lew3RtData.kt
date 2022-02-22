package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.toSignedShort
import com.lepu.blepro.utils.toUInt

class Lew3RtData(bytes: ByteArray) {

    var content: ByteArray
    var param: RtParam
    var wave: RtWave

    init {
        content = bytes
        var index = 0
        param = RtParam(bytes.copyOfRange(index, index+20))
        index += 20
        wave = RtWave(bytes.copyOfRange(index, bytes.size))
    }

    class RtParam(bytes: ByteArray) {
        var hr: Int
        var isrFlag: Boolean
        var batteryState: Int
        var batteryPercent: Int
        var recordTime: Int = 0
        var runStatus: Int
        // reserved 11

        init {
            var index = 0
            hr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            isrFlag = (byte2UInt(bytes[index]) and 0x01) == 1
            batteryState = (byte2UInt(bytes[index]) and 0xC0) ushr 5
            index++
            batteryPercent = byte2UInt(bytes[index])
            index++
            recordTime = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            runStatus = byte2UInt(bytes[index])
        }

        override fun toString(): String {
            return """
                hr : $hr
                isrFlag : $isrFlag
                batteryState : $batteryState
                batteryPercent : $batteryPercent
                recordTime : $recordTime
                runStatus : $runStatus
            """.trimIndent()
        }
    }

    class RtWave(bytes: ByteArray) {
        var samplingNum: Int
        var waveData: ByteArray
        var wFs: FloatArray

        init {
            var index = 0
            samplingNum = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            waveData = bytes.copyOfRange(index, bytes.size)
            wFs = FloatArray(samplingNum)
            for (i in 0 until samplingNum) {
                wFs[i] = byteTomV(waveData[2 * i], waveData[2 * i + 1])
            }
        }

        private fun byteTomV(a: Byte, b: Byte): Float {
            if (a == 0xFF.toByte() && b == 0x7F.toByte()) {
                return 0f
            }
            val n = toSignedShort(a, b)
            return n * 1.div(345f)
        }
    }

}
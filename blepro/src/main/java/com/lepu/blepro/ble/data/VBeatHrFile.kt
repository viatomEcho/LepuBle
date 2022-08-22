package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt

class VBeatHrFile(val bytes: ByteArray) {

    var fileVersion: Int     // 文件版本 e.g.  0x01 :  V1
    // reserved 9
    var hrList = mutableListOf<PointData>()
    var recordingTime: Int   // 记录时长 e.g. 3600 :  3600s
    // reserved 12
    var magic: Int           // 文件标志 固定值为0xA55A0438

    init {
        var index = 0
        fileVersion = byte2UInt(bytes[index])
        index++
        index += 9
        val len = (bytes.size - index - 20).div(3)
        for (i in 0 until len) {
            hrList.add(PointData(bytes.copyOfRange(index+i*3, index+(i+1)*3)))
        }
        index = bytes.size - 20
        recordingTime = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        index += 12
        magic = toUInt(bytes.copyOfRange(index, index+4))
    }

    override fun toString(): String {
        return """
            VBeatHrFile : 
            bytes : ${bytesToHex(bytes)}
            fileVersion : $fileVersion
            hrList : $hrList
            recordingTime : $recordingTime
            magic : $magic
        """.trimIndent()
    }

    class PointData(val bytes: ByteArray) {
        var hr: Int             // 心率（有效范围30-250）
        var motion: Int         // 加速度值，压缩至量程为0-255
        var vibration: Boolean  // 震动标记	bit7：0无震动 1震动 bit0-6:预留
        init {
            var index = 0
            hr = byte2UInt(bytes[index])
            index++
            motion = byte2UInt(bytes[index])
            index++
            vibration = ((byte2UInt(bytes[index]) and 0x80) shr 7) == 1
        }
        override fun toString(): String {
            return """
                PointData : 
                bytes : ${bytesToHex(bytes)}
                hr : $hr
                motion : $motion
                vibration : $vibration
            """.trimIndent()
        }
    }

}
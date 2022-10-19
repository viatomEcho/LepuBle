package com.lepu.blepro.ble.data

import com.lepu.blepro.ble.cmd.Er3BleResponse
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.toUInt

/**
 * Wxxxxxxxxxxxxxx：心电波形存储文件
 */
class Er3WaveFile(val bytes: ByteArray) {

    var fileVersion: Int     // 文件版本 e.g.  0x01 :  V1，固定为0x01
    var fileType: Int        // 文件类型
    var leadType: Int        // 导联类型，leadType（0：LEAD_12，12导，1：LEAD_6，6导，2：LEAD_5，5导，3：LEAD_3，3导，4：LEAD_3_TEMP，3导带体温，
                             // 5：LEAD_3_LEG，3导胸贴，6：LEAD_5_LEG，5导胸贴，7：LEAD_6_LEG，6导胸贴，0XFF：LEAD_NONSUP，不支持的导联）
    // reserved 7

    var wave: ByteArray
    var waveInts: IntArray
    var waveMvs: FloatArray

    var recordingTime: Int   // 记录时长 e.g. 3600 :  3600s
    var dataCrc: Int         // 文件头部+原始波形和校验
    // reserved 10
    var magic: Int           // 文件标志 固定值为0xA55A0438

    init {
        var index = 0
        fileVersion = byte2UInt(bytes[index])
        index++
        fileType = byte2UInt(bytes[index])
        index++
        leadType = byte2UInt(bytes[index])
        index++
        index += 7
        val len = bytes.size-10-20
        wave = bytes.copyOfRange(index, index+len)
        waveInts = Er3BleResponse.getIntsFromWaveBytes(wave, leadType)
        waveMvs = Er3BleResponse.getMvsFromWaveBytes(wave, leadType)
        index += len
        recordingTime = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        dataCrc = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        index += 10
        magic = toUInt(bytes.copyOfRange(index, index+4))
    }

    override fun toString(): String {
        return """
            Er3WaveFile : 
            fileVersion : $fileVersion
            fileType : $fileType
            leadType : $leadType
            recordingTime : $recordingTime
            dataCrc : $dataCrc
            magic : $magic
        """.trimIndent()
    }
}
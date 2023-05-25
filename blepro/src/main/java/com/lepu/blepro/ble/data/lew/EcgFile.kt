package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.toLong
import com.lepu.blepro.utils.toUInt

class EcgFile(val bytes: ByteArray) {

    var fileVersion: Int     // 文件版本 e.g.  0x01 :  V1
    // reserved 9
    var waveData: ByteArray  // 250Hz原始波形压缩数据（差分压缩），0x7FFF(32767)为无效值
    var recordingTime: Int   // 记录时长 e.g. 3600 :  3600s
    var dataCrc: Int         // 文件头部+原始波形和校验
    // reserved 10
    var magic: Long          // 文件标志 固定值为0xA55A0438 无符号数

    init {
        var index = 0
        fileVersion = byte2UInt(bytes[index])
        index++
        index += 9
        val len = bytes.size-10-20
        waveData = bytes.copyOfRange(index, index+len)
        index += len
        recordingTime = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        dataCrc = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        index += 10
        magic = toLong(bytes.copyOfRange(index, index+4))
    }

    override fun toString(): String {
        return """
            EcgFile : 
            fileVersion : $fileVersion
            recordingTime : $recordingTime
            dataCrc : $dataCrc
            magic : $magic
            waveData.size : ${waveData.size}
        """.trimIndent()
    }

}
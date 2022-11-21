package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.Er3Decompress
import com.lepu.blepro.utils.toUInt

/**
 * Wxxxxxxxxxxxxxx：心电波形存储文件
 * app下载数据调用
 */
class Er3WaveFile {

    var fileVersion = 0     // 文件版本 e.g.  0x01 :  V1，固定为0x01
    var fileType = 0        // 文件类型
    var leadType = 0        // 导联类型，leadType（0：LEAD_12，12导，1：LEAD_6，6导，2：LEAD_5，5导，3：LEAD_3，3导，4：LEAD_3_TEMP，3导带体温，
    // 5：LEAD_3_LEG，3导胸贴，6：LEAD_5_LEG，5导胸贴，7：LEAD_6_LEG，6导胸贴，0XFF：LEAD_NONSUP，不支持的导联）
    // reserved 7

    var recordingTime = 0   // 记录时长 e.g. 3600 :  3600s
    var dataCrc = 0         // 文件头部+原始波形和校验
    // reserved 10
    var magic = 0           // 文件标志 固定值为0xA55A0438

    /**
     * 分块解压文件数据
     */
    fun parseIntsFromWaveBytes(wave: ByteArray, leadType: Int, decompress: Er3Decompress) : IntArray {
        // decompress
        val decompressData = mutableListOf<Int>()
        for (b in wave) {
            val tmp = decompress.Decompress(b)
            if (tmp != null) {
                for (i in tmp) {
                    if (i == 32767) {  // 导联脱落，基线处理
                        decompressData.add(0)
                    } else {
                        decompressData.add(i)
                    }
                }
            }
        }

        val oriInts = decompressData.toIntArray()
        var waveInts = IntArray(0)
        val tmpFs = mutableListOf<Int>()

        when(leadType) {
            0 -> {  // LEAD_12
                val lead_size = 8
                waveInts = oriInts
            }
            1, 7 -> {  // LEAD_6, LEAD_6_LEG
                val lead_size = 4
                for (i in oriInts.indices step lead_size) {
                    tmpFs.add(0)
                    tmpFs.add(oriInts[i+1])
                    tmpFs.add(oriInts[i+2])
                    tmpFs.add(oriInts[i+3])
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(oriInts[i])
                }
                waveInts = tmpFs.toIntArray()
            }
            2 -> {  // LEAD_5
                val lead_size = 4
                for (i in oriInts.indices step lead_size) {
                    tmpFs.add(0)
                    tmpFs.add(oriInts[i+1])
                    tmpFs.add(oriInts[i+2])
                    tmpFs.add(oriInts[i+3])
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                }
                waveInts = tmpFs.toIntArray()
            }
            3 -> {  // LEAD_3
                val lead_size = 4
                for (i in oriInts.indices step lead_size) {
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(oriInts[i+2])
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                }
                waveInts = tmpFs.toIntArray()
            }
            4 -> {  // LEAD_3_TEMP
                val lead_size = 4
                for (i in oriInts.indices step lead_size) {
                    tmpFs.add(0)
                    tmpFs.add(oriInts[i+1])
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                }
                waveInts = tmpFs.toIntArray()
            }
            5 -> {  // LEAD_3_LEG
                val lead_size = 4
                for (i in oriInts.indices step lead_size) {
                    tmpFs.add(0)
                    tmpFs.add(oriInts[i+1])
                    tmpFs.add(oriInts[i+2])
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                }
                waveInts = tmpFs.toIntArray()
            }
            6 -> {  // LEAD_5_LEG
                val lead_size = 4
                for (i in oriInts.indices step lead_size) {
                    tmpFs.add(0)
                    tmpFs.add(oriInts[i+1])
                    tmpFs.add(oriInts[i+2])
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(oriInts[i])
                }
                waveInts = tmpFs.toIntArray()
            }
        }
        return waveInts
    }

    /**
     * 解析文件头部
     */
    fun parseHeadData(bytes: ByteArray) {
        var index = 0
        fileVersion = byte2UInt(bytes[index])
        index++
        fileType = byte2UInt(bytes[index])
        index++
        leadType = byte2UInt(bytes[index])
    }

    /**
     * 解析文件尾部
     */
    fun parseEndData(bytes: ByteArray) {
        var index = 0
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
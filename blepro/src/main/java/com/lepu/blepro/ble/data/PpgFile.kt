package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.int4Bytes
import com.lepu.blepro.utils.HexString.trimStr
import java.util.*

class PpgFile() {

    val DATA_TYPE_UNDEFINED = 0
    val DATA_TYPE_IR = 1
    val DATA_TYPE_RED = 2
    val DEVICE_TYPE_UNDEFINED = 0
    val DEVICE_TYPE_O2RING = 1
    val DEVICE_TYPE_PC60FW = 2

    var fileVersion = 0                     // 文件版本，0x01：V1。一个字节
    var fileType = 7                        // 文件类型。一个字节
    // reserved 8
    var configSize = 128                    // 描述信息长度，默认（128字节）。两个字节
    var sampleTime = 0L                     // 采样时间unix时间戳，精确到s。四个字节
    var sampleRate = 150                    // 采样率。两个字节
    var sampleBytes = 4                     // 设备采样点字节数
    var sampleSize = 0                      // 采样个数，采样个数 = 采样时长（秒）* 采样率。四个字节
    var leadSize = 1                        // 通道数量。1个字节。（正式上线应该会只选择一个通道的数据，考虑到扩展以及研发阶段，协议支持多通道数据。协议定义最多支持4个通道）
    var leadConfig = arrayOf(0, 0, 0, 0)    // 采样通道列表，依次为采样点通道，0：未定义，1：红外，2：红光。4个字节
    var waveConfig = arrayOf(0, 0, 0, 0)    // 采样光谱波长，4个通道。8个字节。
    var accuracy = 0xffff                   // 采样精度。≥2字节缩放为2字节（0xffff），1字节保持不变（0x00ff）。两个字节
    var maxValue = 0xffff                   // 最大值。（ppg采样值为无符号位数据）。两个字节
    var baseline = 0                        // 基线 0x00。两个字节
    var deviceType = 0                      // 设备类型，只做存储维护，0：未定义，1：o2ring，2：pc60fw。1个字节
    var sn: String = ""                     // 设备sn号。32个字节
    // 预留64字节。 描述信息共计128字节
    var sampleIntsData = IntArray(0)    // 采样点数据，一个采样点两个字节

    constructor(bytes: ByteArray) : this() {
        var index = 0
        fileVersion = byte2UInt(bytes[index])
        index++
        fileType = byte2UInt(bytes[index])
        index++
        index += 8
        configSize = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        sampleTime = toLong(bytes.copyOfRange(index, index+4))
        index += 4
        sampleRate = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        sampleSize = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        leadSize = byte2UInt(bytes[index])
        index++
        leadConfig[0] = byte2UInt(bytes[index])
        leadConfig[1] = byte2UInt(bytes[index+1])
        leadConfig[2] = byte2UInt(bytes[index+2])
        leadConfig[3] = byte2UInt(bytes[index+3])
        index += 4
        waveConfig[0] = toUInt(bytes.copyOfRange(index, index+2))
        waveConfig[1] = toUInt(bytes.copyOfRange(index+2, index+4))
        waveConfig[2] = toUInt(bytes.copyOfRange(index+4, index+6))
        waveConfig[3] = toUInt(bytes.copyOfRange(index+6, index+8))
        index += 8
        accuracy = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        maxValue = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        baseline = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        deviceType = byte2UInt(bytes[index])
        index++
        sn = trimStr(String(bytes.copyOfRange(index, index+32)))
        index += 32
        index += 64
        val len = (bytes.size - 10 - 128).div(2)
        sampleIntsData = IntArray(len)
        for (i in 0 until len) {
            sampleIntsData[i] = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
        }
    }

    fun getDataBytes(): ByteArray {
        sampleSize = sampleIntsData.size/leadSize
        // 目前只针对红外和红光两种情况，默认波长固定
        // 红外940，红光660
        for (i in 0 until leadSize) {
            when (leadConfig[i]) {
                1 -> waveConfig[i] = 940
                2 -> waveConfig[i] = 660
            }
        }
        // 最大值目前只针对PPG数值，先固定两种情况
        if (sampleBytes > 1) {
            accuracy = 0xffff
            maxValue = 0xffff
        } else {
            accuracy = 0x00ff
            maxValue = 0x00ff
        }
        val tempSn = ByteArray(32)
        System.arraycopy(sn.toByteArray(), 0, tempSn, 0, sn.toByteArray().size)
        var data = byteArrayOf(fileVersion.toByte())
            .plus(fileType.toByte())
            .plus(ByteArray(8))
            .plus(int2ByteArray(configSize))
            .plus(int4Bytes(sampleTime))
            .plus(int2ByteArray(sampleRate))
            .plus(int4ByteArray(sampleSize))
            .plus(leadSize.toByte())
        for (lead in leadConfig) {
            data = data.plus(lead.toByte())
        }
        for (wave in waveConfig) {
            data = data.plus(int2ByteArray(wave))
        }
        data = data.plus(int2ByteArray(accuracy))
            .plus(int2ByteArray(maxValue))
            .plus(int2ByteArray(baseline))
            .plus(deviceType.toByte())
            .plus(tempSn)
            .plus(ByteArray(64))
        val acc = if (sampleBytes > 2) {
            256
        } else {
            1
        }
        for (sample in sampleIntsData) {
            data = data.plus(int2ByteArray(sample/acc))
        }
        return data
    }

    override fun toString(): String {
        return """
            PpgFile : 
            fileVersion : $fileVersion
            fileType : $fileType
            configSize : $configSize
            sampleTime : $sampleTime
            sampleTime : ${DateUtil.stringFromDate(Date(sampleTime.times(1000)), "yyyy-MM-dd HH:mm:ss")}
            sampleRate : $sampleRate
            sampleSize : $sampleSize
            leadSize : $leadSize
            leadConfig : ${leadConfig.joinToString(",")}
            waveConfig : ${waveConfig.joinToString(",")}
            accuracy : $accuracy
            maxValue : $maxValue
            baseline : $baseline
            deviceType : $deviceType
            sn : $sn
            sampleIntsData : ${sampleIntsData.joinToString(",")}
        """.trimIndent()
    }

}
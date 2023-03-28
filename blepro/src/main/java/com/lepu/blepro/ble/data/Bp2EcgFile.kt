package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.toSignedShort
import com.lepu.blepro.utils.DateUtil
import com.lepu.blepro.utils.DateUtil.stringFromDate
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import java.util.*

class Bp2EcgFile(val bytes: ByteArray) {

    var fileVersion: Int            // 文件版本 e.g.  0x01 :  V1
    var fileType: Int               // 文件类型 1：血压；2：心电
    var measureTime: Int            // 测量时间时间戳 s
    // reserved 3
    var uploadTag: Boolean          // 上传标识
    var recordingTime: Int          // 记录时长 s
    // reserved 2
    var result: Int
    var diagnosis: Bp2EcgDiagnosis  // 诊断结果
    var hr: Int                     // 心率 单位：bpm
    var qrs: Int                    // QRS 单位：ms
    var pvcs: Int                   // PVC个数
    var qtc: Int                    // QTc 单位：ms
    var connectCable: Boolean       // 是否接入线缆
    // reserved 19
    var waveData: ByteArray
    var waveShortData: ShortArray

    init {
        var index = 0
        fileVersion = byte2UInt(bytes[index])
        index++
        fileType = byte2UInt(bytes[index])
        index++
        val rawOffset = DateUtil.getTimeZoneOffset().div(1000)
        val defaultTime = toUInt(bytes.copyOfRange(index, index+4))
        measureTime = defaultTime - rawOffset
        index += 4
        index += 3
        uploadTag = (byte2UInt(bytes[index]) and 0x01) == 1
        index++
        recordingTime = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        index += 2
        result = toUInt(bytes.copyOfRange(index, index+4))
        diagnosis = Bp2EcgDiagnosis(bytes.copyOfRange(index, index+4))
        index += 4
        hr = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        qrs = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        pvcs = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        qtc = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        connectCable = byte2UInt(bytes[index]) == 1
        index++
        index += 19
        waveData = bytes.copyOfRange(index, bytes.size)
        waveShortData = ShortArray(waveData.size.div(2))
        for (i in waveShortData.indices) {
            waveShortData[i] = toSignedShort(waveData[2*i], waveData[2*i+1])
        }
    }

    override fun toString(): String {
        return """
            Bp2EcgFile : 
            fileVersion : $fileVersion
            fileType : $fileType
            measureTime : $measureTime
            measureTime : ${stringFromDate(Date(measureTime*1000L), "yyyy-MM-dd HH:mm:ss")}
            uploadTag : $uploadTag
            recordingTime : $recordingTime
            result : $result
            diagnosis : $diagnosis
            hr : $hr
            qrs : $qrs
            pvcs : $pvcs
            qtc : $qtc
            connectCable : $connectCable
            waveData : ${bytesToHex(waveData)}
            waveShortData : $waveShortData
        """.trimIndent()
    }

}
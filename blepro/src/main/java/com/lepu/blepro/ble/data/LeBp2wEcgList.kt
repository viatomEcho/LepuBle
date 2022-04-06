package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.DateUtil.stringFromDate
import com.lepu.blepro.utils.toUInt
import java.util.*

const val ECG_RECORD_LENGTH = 46

class LeBp2wEcgList(var bytes: ByteArray) {

    var fileVersion: Int
    var fileType: Int
    var listContent: ByteArray
    var ecgFileList = mutableListOf<EcgRecord>()

    init {
        var index = 0
        fileVersion = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        fileType = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        index += 8
        listContent = bytes.copyOfRange(index, bytes.size)
        val num = listContent.size.div(ECG_RECORD_LENGTH)
        for (i in 0 until num) {
            ecgFileList.add(EcgRecord(listContent.copyOfRange(ECG_RECORD_LENGTH*i, ECG_RECORD_LENGTH*(i+1))))
        }

    }

    data class EcgRecord(val bytes: ByteArray) {
        var time: Long          // 测量时间戳s
        var fileName: String    // 文件名
        var uid: Int            // 用户id
        var mode: Int           // 测量模式
        var recordingTime: Int  // 记录时长 单位s
        var result: Int         // 诊断结果
        var hr: Int             // 单位bpm
        var qrs: Int            // 单位ms
        var pvcs: Int           // 单位个
        var qtc: Int            // 单位ms

        init {
            var index = 0
            val rawOffset = TimeZone.getDefault().rawOffset.div(1000)
            val defaultTime = toUInt(bytes.copyOfRange(index, index+4)).toLong()
            time = defaultTime - rawOffset
            fileName = stringFromDate(Date(time*1000), "yyyyMMddHHmmss")
            index += 4
            uid = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            mode = (bytes[index].toUInt() and 0xFFu).toInt()
            index += 2
            recordingTime = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            result = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            hr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            qrs = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pvcs = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            qtc = toUInt(bytes.copyOfRange(index, index+2))
        }

        override fun toString(): String {
            return """
                time : $time
                fileName : $fileName
                uid : $uid
                mode : $mode
                recordingTime : $recordingTime
                result : $result
                hr : $hr
                qrs : $qrs
                pvcs : $pvcs
                qtc : $qtc
            """.trimIndent()
        }
    }

    override fun toString(): String {
        return """
            fileVersion : $fileVersion
            fileType : $fileType
            ecgFileList : $ecgFileList
        """.trimIndent()
    }
}
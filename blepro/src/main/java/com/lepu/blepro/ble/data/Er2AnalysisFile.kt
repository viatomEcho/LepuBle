package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.toSignedShort
import com.lepu.blepro.utils.toUInt

class Er2AnalysisFile(val bytes: ByteArray) {

    var fileVersion: Int     // 文件版本 e.g.  0x01 :  V1
    // reserved 9
    var recordingTime: Int   // 记录时长 e.g. 3600 :  3600s
    // reserved 66
    var resultList = mutableListOf<AnalysisResult>()

    init {
        var index = 0
        fileVersion = byte2UInt(bytes[index])
        index++
        index += 9
        recordingTime = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        index += 66
        val len = (bytes.size - index).div(32)
        for (i in 0 until len) {
            resultList.add(AnalysisResult(bytes.copyOfRange(index+i*32, index+(i+1)*32)))
        }
    }

    override fun toString(): String {
        return """
            Er2AnalysisFile : 
            fileVersion : $fileVersion
            recordingTime : $recordingTime
            resultList : $resultList
        """.trimIndent()
    }

    class AnalysisResult(val bytes: ByteArray) {
        var result: Er2EcgDiagnosis
        var hr: Int
        var qrs: Int
        var pvcs: Int
        var qtc: Int
        var st: Short
        // reserved 18
        init {
            var index = 0
            result = Er2EcgDiagnosis(bytes.copyOfRange(index, index+4))
            index += 4
            hr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            qrs = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pvcs = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            qtc = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            st = toSignedShort(bytes[index], bytes[index+1])
        }
        override fun toString(): String {
            return """
                AnalysisResult : 
                result : $result
                hr : $hr
                qrs : $qrs
                pvcs : $pvcs
                qtc : $qtc
                st : $st
            """.trimIndent()
        }
    }

}
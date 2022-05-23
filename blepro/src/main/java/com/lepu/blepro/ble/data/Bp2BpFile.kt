package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.DateUtil.stringFromDate
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import java.util.*

class Bp2BpFile(val bytes: ByteArray) {

    var fileVersion: Int       // 文件版本 e.g.  0x01 :  V1
    var fileType: Int          // 文件类型 1：血压；2：心电
    var measureTime: Int       // 测量时间时间戳s
    // reserved 4
    var statusCode: Int        // 状态码
    var sys: Int               // 收缩压
    var dia: Int               // 舒张压
    var mean: Int              // 平均压
    var pr: Int                // 心率
    var result: Int            // 诊断结果 bit0:心率不齐
    // reserved 19

    init {
        var index = 0
        fileVersion = byte2UInt(bytes[index])
        index++
        fileType = byte2UInt(bytes[index])
        index++
        val rawOffset = TimeZone.getDefault().rawOffset.div(1000)
        val defaultTime = toUInt(bytes.copyOfRange(index, index+4))
        measureTime = defaultTime - rawOffset
        index += 4
        index += 4
        statusCode = byte2UInt(bytes[index])
        index++
        sys = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        dia = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        mean = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        pr = byte2UInt(bytes[index])
        index++
        result = byte2UInt(bytes[index])
    }

    override fun toString(): String {
        return """
            Bp2BpFile : 
            bytes : ${bytesToHex(bytes)}
            fileVersion : $fileVersion
            fileType : $fileType
            measureTime : $measureTime
            measureTime : ${stringFromDate(Date(measureTime * 1000L), "yyyy-MM-dd HH:mm:ss")}
            statusCode : $statusCode
            sys : $sys
            dia : $dia
            mean : $mean
            pr : $pr
            result : $result
        """.trimIndent()
    }

}
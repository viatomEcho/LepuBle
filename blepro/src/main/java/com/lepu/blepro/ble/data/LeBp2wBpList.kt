package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.DateUtil.stringFromDate
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import java.util.*

const val BP_RECORD_LENGTH = 37

class LeBp2wBpList(var bytes: ByteArray) {

    var fileVersion: Int
    var fileType: Int
    var listContent: ByteArray
    var bpFileList = mutableListOf<BpRecord>()

    init {
        var index = 0
        fileVersion = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        fileType = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        index += 8
        listContent = bytes.copyOfRange(index, bytes.size)
        val num = listContent.size.div(BP_RECORD_LENGTH)
        for (i in 0 until num) {
            bpFileList.add(BpRecord(listContent.copyOfRange(BP_RECORD_LENGTH*i, BP_RECORD_LENGTH*(i+1))))
        }
    }

    override fun toString(): String {
        return """
            LeBp2wBpList : 
            bytes : ${bytesToHex(bytes)}
            fileVersion : $fileVersion
            fileType : $fileType
            bpFileList : $bpFileList
        """.trimIndent()
    }

    class BpRecord(val bytes: ByteArray) {
        var time: Long            // 测量时间戳s
        var fileName: String      // 文件名
        var uid: Int              // 用户id
        var mode: Int             // 测量模式 0：单次 1：3次
        var interval: Int         // 测量间隔 单位s 非单次测量模式有效
        var status: Int           // 状态码
        var sys: Int              // 收缩压
        var dia: Int              // 舒张压
        var mean: Int             // 平均压
        var pr: Int               // 脉率
        var result: Int           // 诊断结果 bit0：心律不齐 bit1：动作干扰
        var isIrregular: Boolean  // 心律不齐
        var isMovement: Boolean   // 动作干扰
        var level: Int            // 血压等级，协议没有具体数值含义

        init {
            var index = 0
            val rawOffset = TimeZone.getDefault().rawOffset.div(1000)
            val defaultTime = toUInt(bytes.copyOfRange(index, index+4)).toLong()
            time = defaultTime - rawOffset
            fileName = stringFromDate(Date(time * 1000), "yyyyMMddHHmmss")
            index += 4
            uid = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            mode = (bytes[index].toUInt() and 0xFFu).toInt()
            index += 2
            interval = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            status = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            sys = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            dia = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            mean = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pr = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            result = (bytes[index].toUInt() and 0xFFu).toInt()
            isIrregular = result and 0x01 == 1
            isMovement = (result and 0x02 shr 1) == 1
            index++
            level = (bytes[index].toUInt() and 0xFFu).toInt()
        }

        override fun toString(): String {
            return """
                BpRecord : 
                bytes : ${bytesToHex(bytes)}
                time : $time
                fileName : $fileName
                uid : $uid
                mode : $mode
                interval : $interval
                status : $status
                sys : $sys
                dia : $dia
                mean : $mean
                pr : $pr
                result : $result
                isIrregular : $isIrregular
                isMovement : $isMovement
                level : $level
            """.trimIndent()
        }
    }

}
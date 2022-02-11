package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.DateUtil.stringFromDate
import com.lepu.blepro.utils.toUInt
import java.util.*

const val BP_RECORD_LENGTH = 37

class Bp2wBpList(bytes: ByteArray) {

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

    data class BpRecord(val bytes: ByteArray) {
        var time: Int         // 测量时间
        var fileName: String  // 文件名
        var uid: Int          // 用户id
        var mode: Int         // 测量模式 0：单次 1：3次
        var interval: Int     // 测量间隔 单位s 非单次测量模式有效
        var status: Int       // 状态码
        var sys: Int          // 收缩压
        var dia: Int          // 舒张压
        var mean: Int         // 平均压
        var pr: Int           // 脉率
        var result: Int       // 诊断结果 0：心律不齐 1：动作干扰
        var level: Int        // 血压等级

        init {
            var index = 0
            time = toUInt(bytes.copyOfRange(index, index+4))
            fileName = stringFromDate(Date(time.toLong()), "yyyyMMddHHmmss")
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
            index++
            level = (bytes[index].toUInt() and 0xFFu).toInt()
        }
    }

}
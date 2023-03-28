package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.HexString.trimStr

object R20BleResponse {

    @ExperimentalUnsignedTypes
    class BleResponse (val bytes: ByteArray) {
        var cmd: Int
        var pkgType: Int
        var pkgNo: Int
        var len: Int
        var content: ByteArray

        init {
            cmd = byte2UInt(bytes[1])
            pkgType = byte2UInt(bytes[3])
            pkgNo = byte2UInt(bytes[4])
            len = toUInt(bytes.copyOfRange(5, 7))
            content = bytes.copyOfRange(7, 7+len)
        }
    }

    @ExperimentalUnsignedTypes
    class DoctorModeResult(val bytes: ByteArray) {
        var success: Boolean
        var errCode: Int      // 1：设备处于医生模式；2：设备处于医生模式（BLE）；3：设备处于医生模式（Socket）; 4:密码错误
        init {
            var index = 0
            success = byte2UInt(bytes[index]) == 1
            index++
            errCode = byte2UInt(bytes[index])
        }
        override fun toString(): String {
            return """
                DoctorModeResult :
                success : $success
                errCode : $errCode
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class VersionInfo(val bytes: ByteArray) {
        var hwV: String   // 硬件版本 e.g. 'A' : A版
        var fwV: String   // 固件版本 e.g. 0x00010200 : V1.2.0
        var blV: String   // 引导版本 e.g. 0x00010200 : V1.2.0
        var bleV: String  // 蓝牙驱动版本 e.g. 0x00010200 : V1.2.0
        var algV: String  // 算法版本
        // reserved 7
        init {
            var index = 0
            hwV = trimStr(String(bytes.copyOfRange(index, index+1)))
            index++
            fwV = "${bytes[index+3]}.${bytes[index+2]}.${bytes[index+1]}.${bytes[index]}"
            index += 4
            blV = "${bytes[index+3]}.${bytes[index+2]}.${bytes[index+1]}.${bytes[index]}"
            index += 4
            bleV = "${bytes[index+3]}.${bytes[index+2]}.${bytes[index+1]}.${bytes[index]}"
            index += 4
            algV = "${bytes[index+3]}.${bytes[index+2]}.${bytes[index+1]}.${bytes[index]}"
        }
        override fun toString(): String {
            return """
                VersionInfo :
                hwV : $hwV
                fwV : $fwV
                blV : $blV
                bleV : $bleV
                algV : $algV
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class MaskTestResult(val bytes: ByteArray) {
        var status: Int  // 0:未在测试状态；1：测试中；2：测试结束
        var leak: Int    // 实时漏气量。 单位L/min
        var result: Int  // 0:测试未完成；1：不合适；2：合适
        // reserved 3
        init {
            var index = 0
            status = byte2UInt(bytes[index])
            index++
            leak = byte2UInt(bytes[index])
            index++
            result = byte2UInt(bytes[index])
        }
        override fun toString(): String {
            return """
                MaskTestResult : 
                status : $status
                leak : $leak
                result : $result
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class RecordList(val bytes: ByteArray) {
        var startTime: Long  // 列表起始时间s
        var type: Int        // 1:当天统计；2:单次统计
        // reserved 5
        var size: Int        // 记录size
        var list = mutableListOf<Record>()
        init {
            var index = 0
            startTime = toLong(bytes.copyOfRange(index, index+4))
            index += 4
            type = byte2UInt(bytes[index])
            index++
            index += 5
            size = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            for (i in 0 until size) {
                list.add(Record(bytes.copyOfRange(index, index+10)))
                index += 10
            }
        }
        override fun toString(): String {
            return """
                RecordList : 
                startTime : $startTime
                type : $type
                size : $size
                list : $list
            """.trimIndent()
        }
    }
    @ExperimentalUnsignedTypes
    class Record(val bytes: ByteArray) {
        var measureTime: Long  // 记录时间，对应出文件名然后下载s
        var updateTime: Long   // 此记录更新时间s
        // reserved 2
        init {
            var index = 0
            measureTime = toLong(bytes.copyOfRange(index, index+4))
            index += 4
            updateTime = toLong(bytes.copyOfRange(index, index+4))
        }
        override fun toString(): String {
            return """
                Record : 
                measureTime : $measureTime
                updateTime : $updateTime
            """.trimIndent()
        }
    }
}
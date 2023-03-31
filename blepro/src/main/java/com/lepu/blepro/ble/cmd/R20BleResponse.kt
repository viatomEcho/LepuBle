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

    @ExperimentalUnsignedTypes
    class RtState(val bytes: ByteArray) {
        var ventilationMode: Int  // 通气模式 0:CPAP  1:APAP  2:S   3:S/T   4:T
        var deviceMode: Int       // 0:普通模式；1：设备端医生模式；2：蓝牙医生模式；3：socket端医生模式
        var standard: Int         // CE/FDA, 0:CE;1:FDA
        // reserved 5
        init {
            var index = 0
            ventilationMode = byte2UInt(bytes[index])
            index++
            deviceMode = byte2UInt(bytes[index])
            index++
            standard = byte2UInt(bytes[index])
        }
        override fun toString(): String {
            return """
                RtState : 
                ventilationMode : $ventilationMode
                deviceMode : $deviceMode
                standard : $standard
            """.trimIndent()
        }
    }
    @ExperimentalUnsignedTypes
    class RtParam(val bytes: ByteArray) {
        var pressure: Float  // 实时压(0~40cmH2O),单位0.1cmH20,e.g.10:1cmH2O[0,400],0.5Hz
        var ipap: Float      // 吸气压力(0~40cmH2O),单位0.1cmH20,e.g.10:1cmH2O[0,400],0.5Hz
        var epap: Float      // 呼气压力(0~40cmH2O),单位0.1cmH20,e.g.10:1cmH2O[0,400],0.5Hz
        var vt: Int          // 潮气量(0~3000mL),单位1mL,e.g.10:10mL[0,3000],0.5Hz
        var mv: Float        // 分钟通气量(0~60L/min),单位0.1L/min,e.g.10:1L/min[0,600],0.5Hz
        var leak: Float      // 漏气量(0~120L/min),单位0.1L/min,e.g.10:1L/min[0,1200],0.5Hz
        var rr: Int          // 呼吸率(0~60),单位1bpm,e.g.10:10bpm[0,60],0.5Hz
        var ti: Float        // 吸气时间(0.1-4s),单位0.1s,e.g.10:1s[1,40],0.5Hz
        var ie: Int          // 呼吸比(1:50.0-3.0:1),单位0.0001,e.g.10:1:49.75[0,30000],0.5Hz ??????
        var spo2: Int        // 血氧(70-100%),单位1%,e.g.10:10%[70,100],1Hz
        var pr: Int          // 脉率(30-250bpm),单位1bpm,e.g.10:10bpm[30,250],1Hz
        var hr: Int          // 心率(30-250bpm),单位1bpm,e.g.10:10bpm[30,250],1Hz
        // reserved 8
        init {
            var index = 0
            pressure = toUInt(bytes.copyOfRange(index, index+2)).div(10f)
            index += 2
            ipap = toUInt(bytes.copyOfRange(index, index+2)).div(10f)
            index += 2
            epap = toUInt(bytes.copyOfRange(index, index+2)).div(10f)
            index += 2
            vt = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            mv = toUInt(bytes.copyOfRange(index, index+2)).div(10f)
            index += 2
            leak = toUInt(bytes.copyOfRange(index, index+2)).div(10f)
            index += 2
            rr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            ti = toUInt(bytes.copyOfRange(index, index+2)).div(10f)
            index += 2
            ie = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            spo2 = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            hr = toUInt(bytes.copyOfRange(index, index+2))
        }
        override fun toString(): String {
            return """
                RtParam : 
                pressure : $pressure
                ipap : $ipap
                epap : $epap
                vt : $vt
                mv : $mv
                leak : $leak
                rr : $rr
                ti : $ti
                ie : $ie
                spo2 : $spo2
                pr : $pr
                hr : $hr
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class Event(val bytes: ByteArray) {
        var timestamp: Long  // 测量时间时间戳 e.g.  0:  1970.01.01 00:00:0时间戳
        var alarm: Boolean   // 0-取消告警，1-告警
        var alarmLevel: Int  // 告警等级 R20BleCmd.AlarmLevel
        var eventId: Int     // 事件id R20BleCmd.EventId
        // reserved 1
        init {
            var index = 0
            timestamp = toLong(bytes.copyOfRange(index, index+4))
            index += 4
            alarm = byte2UInt(bytes[index]) == 1
            index++
            alarmLevel = byte2UInt(bytes[index])
            index++
            eventId = toUInt(bytes.copyOfRange(index, index+2))
        }
        override fun toString(): String {
            return """
                Event : 
                timestamp : $timestamp
                alarm : $alarm
                alarmLevel : $alarmLevel
                eventId : $eventId
            """.trimIndent()
        }
    }
}
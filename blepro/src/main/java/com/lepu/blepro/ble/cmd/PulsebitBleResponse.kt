package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import com.lepu.blepro.utils.toUIntTest
import org.json.JSONObject

class PulsebitBleResponse{

    @ExperimentalUnsignedTypes
    class BleResponse(bytes: ByteArray) {
        var no: Int
        var len: Int
        var state: Boolean
        var content: ByteArray

        init {
            state = bytes[1].toInt() == 0x00
            no = toUInt(bytes.copyOfRange(3, 5))
            len = bytes.size - 8
            content = bytes.copyOfRange(7, 7 + len)
        }
    }

    @ExperimentalUnsignedTypes
    class DeviceInfo(val bytes: ByteArray) {
        var infoStr: JSONObject
        var region: String       // 地区版本
        var model: String        // 系列版本
        var hwVersion: String    // 硬件版本
        var swVersion: String    // 软件版本
        var lgVersion: String    // 语言版本
        var curLanguage: String    // 语言版本
        var sn: String           // 序列号
        var fileVer:String       // 文件解析协议版本
        var spcpVer:String       // 蓝牙通讯协议版本
        var branchCode:String    // code码
        var application:String    // code码

        init {
            infoStr = JSONObject(String(bytes))
//            try {
//                var infoStr = JSONObject(String(bytes))
//            } catch (e: JSONException) {
//                LogUtils.d(String(bytes))
//            }
            region = infoStrGetString("Region")
            model = infoStrGetString("Model")
            hwVersion = infoStrGetString("HardwareVer")
            swVersion = infoStrGetString("SoftwareVer")
            lgVersion = infoStrGetString("LanguageVer")
            curLanguage = infoStrGetString("CurLanguage")
            sn = infoStrGetString("SN")
            fileVer = infoStrGetString("FileVer")
            spcpVer = infoStrGetString("SPCPVer")
            branchCode = infoStrGetString("BranchCode")
            application = infoStrGetString("Application")
        }

        private fun infoStrGetString(key: String): String {
            return if (infoStr.has(key)) {
                infoStr.getString(key)
            } else {
                ""
            }
        }

        override fun toString(): String {
            return """
                DeviceInfo : 
                infoStr = $infoStr
                region = $region
                model = $model
                hwVersion = $hwVersion
                swVersion = $swVersion
                lgVersion = $lgVersion
                curLanguage = $curLanguage
                sn = $sn
                fileVer = $fileVer
                spcpVer = $spcpVer
                branchCode = $branchCode
                application = $application
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class EcgFile(val fileName: String, val fileSize: Int, val bytes: ByteArray) {
        var hrsDataSize: Int     // 波形心率大小（byte）
        var waveDataSize: UInt   // 波形数据大小（byte）
        var hr: Int              // 诊断结果：HR，单位为bpm
        var st: Int              // 诊断结果：ST（以ST/100存储），单位为mV(内部导联写0)
        var qrs: Int             // 诊断结果：QRS，单位为ms
        var pvcs: Int            // 诊断结果：PVCs(内部导联写0)
        var qtc: Int             // 诊断结果：QTc单位为ms
        var result: UInt
        var resultMess: String   // 心电异常诊断结果
        var measureMode: Int     // 测量模式
        var filterMode: Int      // 滤波模式（1：wide   0：normal）
        var qt: Int              // 诊断结果：QT单位为ms
        var hrsData: ByteArray   // ECG心率值，从数据采样开始，采样率为1Hz，每个心率值为2byte（实际20s数据，每秒出一个心率），若出现无效心率，则心率为0
        var waveData: ByteArray  // 按照压缩算法之后的ECG数据内容，每个采样点2byte

        init {
            var index = 0
            hrsDataSize = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            waveDataSize = toUIntTest(bytes.copyOfRange(index, index+4))
            index += 4
            hr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            st = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            qrs = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pvcs = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            qtc = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            result = toUIntTest(bytes.copyOfRange(index, index+4))
            resultMess = getMess(result)
            index += 4
            measureMode = byte2UInt(bytes[index])
            index++
            filterMode = byte2UInt(bytes[index])
            index++
            qt = toUInt(bytes.copyOfRange(index, index+2))
            hrsData = bytes.copyOfRange(44, 44+hrsDataSize)
//            waveData = bytes.copyOfRange(44+hrsDataSize, 44+hrsDataSize+waveDataSize)
            waveData = bytes.copyOfRange(44+hrsDataSize, bytes.size)
        }

        fun getMess(result: UInt): String {
            return when(result) {
                0x00000000u -> "Regular ECG Rhythm(除异常情况之外)"
                0xFFFFFFFFu -> "Unable to analyze(波形质量差，或者导联一直脱落等算法无法分析的情况)"
                0x00000001u -> "Fast Heart Rate(HR>100bpm)"
                0x00000002u -> "Slow Heart Rate(HR<50bpm)"
                0x00000004u -> "Irregular ECG Rhythm(RR间期不规则)"
                0x00000008u -> "Possible ventricular premature beats(PVC)"
                0x00000010u -> "Possible heart pause(停搏)"
                0x00000020u -> "Possible Atrial fibrillation"
                0x00000040u -> "Wide QRS duration(QRS>120ms)"
                0x00000080u -> "QTc is prolonged(QTc>450ms)"
                0x00000100u -> "QTc is short(QTc<300ms)"
                0x00000200u -> "ST segment elevation(ST>+0.2mV)"
                0x00000400u -> "ST segment depression(ST<-0.2mV)"
                else -> ""
            }
        }

        override fun toString(): String {
            return """
                hrsDataSize : $hrsDataSize
                waveDataSize : $waveDataSize
                hr : $hr
                st : $st
                qrs : $qrs
                pvcs : $pvcs
                qtc : $qtc
                result : $result
                resultMess : $resultMess
                measureMode : $measureMode
                filterMode : $filterMode
                qt : $qt
                hrsData : ${bytesToHex(hrsData)}
                waveData : ${bytesToHex(waveData)}
            """.trimIndent()
        }

    }

    @ExperimentalUnsignedTypes
    class FileList(val bytes: ByteArray) {
        var size: Int
        var list = mutableSetOf<Record>()

        init {
            size = bytes.size.div(17)
            for (i in 0 until size) {
                list.add(Record(bytes.copyOfRange(i*17, (i+1)*17)))
            }
        }

        override fun toString(): String {
            return """
                size : $size
                list : $list
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class Record(val bytes: ByteArray) {
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var second: Int
        var hr: Int
        var user: Int

        init {
            var index = 0
            year = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            month = byte2UInt(bytes[index])
            index++
            day = byte2UInt(bytes[index])
            index++
            hour = byte2UInt(bytes[index])
            index++
            minute = byte2UInt(bytes[index])
            index++
            second = byte2UInt(bytes[index])
            index++
            hr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            user = byte2UInt(bytes[15])
        }

        fun getTimeString(): String {
            val monthStr = if (month < 9) {
                "0$month"
            } else {
                "$month"
            }
            val dayStr = if (day < 9) {
                "0$day"
            } else {
                "$day"
            }
            val hourStr = if (hour < 9) {
                "0$hour"
            } else {
                "$hour"
            }
            val minuteStr = if (minute < 9) {
                "0$minute"
            } else {
                "$minute"
            }
            val secondStr = if (second < 9) {
                "0$second"
            } else {
                "$second"
            }
            return "$year$monthStr$dayStr$hourStr$minuteStr$secondStr"
        }

        override fun toString(): String {
            return """
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
                second : $second
                hr : $hr
                user : $user
            """.trimIndent()
        }
    }

}
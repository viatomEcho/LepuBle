package com.lepu.blepro.ble.cmd

import com.lepu.blepro.ble.data.CheckmeLeEcgDiagnosis
import com.lepu.blepro.utils.ByteUtils.*
import com.lepu.blepro.utils.DateUtil.getSecondTimestamp
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.bytesToSignedShort
import com.lepu.blepro.utils.getTimeString
import com.lepu.blepro.utils.toUInt
import org.json.JSONObject
import java.util.*

object CheckmeLeBleResponse{

    @ExperimentalUnsignedTypes
    class BleResponse(val bytes: ByteArray) {
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
        var swVersion: String    // 固件版本
        var lgVersion: String    // 语言包版本
        var curLanguage: String  // 当前语言版本
        var sn: String           // 序列号
        var fileVer:String       // 文件解析协议版本
        var spcpVer:String       // 蓝牙通讯协议版本
        var application:String   //

        init {
            val data = String(bytes)
            infoStr = if (data.contains("{") && data.contains("}")) {
                JSONObject(data)
            } else {
                JSONObject()
            }
            region = infoStrGetString("Region")
            model = infoStrGetString("Model")
            hwVersion = infoStrGetString("HardwareVer")
            swVersion = infoStrGetString("SoftwareVer")
            lgVersion = infoStrGetString("LanguageVer")
            curLanguage = infoStrGetString("CurLanguage")
            sn = infoStrGetString("SN")
            fileVer = infoStrGetString("FileVer")
            spcpVer = infoStrGetString("SPCPVer")
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
                application = $application
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class ListContent(val type: Int, val content: ByteArray) {

    }

    @ExperimentalUnsignedTypes
    class DlcList(val bytes: ByteArray) {
        var size: Int
        var list = mutableListOf<DlcRecord>()
        init {
            size = bytes.size.div(17)
            for (i in 0 until size) {
                list.add(DlcRecord(bytes.copyOfRange(i*17, (i+1)*17)))
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
    class DlcRecord(val bytes: ByteArray) {
        var timestamp: Long     // 时间戳 秒s
        var recordName: String
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var second: Int
        var hr: Int
        var ecgNormal: Boolean  // true：笑脸，false：哭脸
        var spo2: Int
        var pi: Float
        var oxyNormal: Boolean  // true：笑脸，false：哭脸
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
            ecgNormal = byte2UInt(bytes[index]) == 0
            index++
            spo2 = byte2UInt(bytes[index])
            index++
            pi = byte2UInt(bytes[index]).div(10f)
            index++
            oxyNormal = byte2UInt(bytes[index]) == 0
            recordName = getTimeString(year, month, day, hour, minute, second)
            timestamp = getSecondTimestamp(recordName)
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
                ecgNormal : $ecgNormal
                spo2 : $spo2
                pi : $pi
                oxyNormal : $oxyNormal
                recordName : $recordName
                timestamp : $timestamp
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class TempList(val bytes: ByteArray) {
        var size: Int
        var list = mutableListOf<TempRecord>()
        init {
            size = bytes.size.div(11)
            for (i in 0 until size) {
                list.add(TempRecord(bytes.copyOfRange(i*11, (i+1)*11)))
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
    class TempRecord(val bytes: ByteArray) {
        var timestamp: Long  // 时间戳 秒s
        var recordName: String
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var second: Int
        var temp: Float      // 单位摄氏度
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
            temp = bytesToFloat(bytes.copyOfRange(index, index+4))
            recordName = getTimeString(year, month, day, hour, minute, second)
            timestamp = getSecondTimestamp(recordName)
        }
        override fun toString(): String {
            return """
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
                second : $second
                temp : $temp
                recordName : $recordName
                timestamp : $timestamp
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class OxyList(val bytes: ByteArray) {
        var size: Int
        var list = mutableListOf<OxyRecord>()
        init {
            size = bytes.size.div(12)
            for (i in 0 until size) {
                list.add(OxyRecord(bytes.copyOfRange(i*12, (i+1)*12)))
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
    class OxyRecord(val bytes: ByteArray) {
        var timestamp: Long          // 时间戳 秒s
        var recordName: String
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var second: Int
        var measureMode: Int         // 测量模式 0：内部，1：外部
        var measureModeMess: String
        var spo2: Int                // 血氧值（0-100，单位为%）
        var pr: Int                  // PR值（0-255）
        var pi: Float                // PI值（实际为一位小数的值，单位为%，此处使用整数表示，如12.5%则用125表示）（0-25.5%）
        var normal: Boolean          // true：good，false：bad
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
            measureMode = byte2UInt(bytes[index])
            measureModeMess = getModeMess(1, measureMode)
            index++
            spo2 = byte2UInt(bytes[index])
            index++
            pr = byte2UInt(bytes[index])
            index++
            pi = byte2UInt(bytes[index]).div(10f)
            index++
            normal = byte2UInt(bytes[index]) == 0
            recordName = getTimeString(year, month, day, hour, minute, second)
            timestamp = getSecondTimestamp(recordName)
        }
        override fun toString(): String {
            return """
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
                second : $second
                measureMode : $measureMode
                spo2 : $spo2
                pr : $pr
                pi : $pi
                normal : $normal
                recordName : $recordName
                timestamp : $timestamp
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class EcgList(val bytes: ByteArray) {
        var size: Int
        var list = mutableListOf<EcgRecord>()
        init {
            size = bytes.size.div(10)
            for (i in 0 until size) {
                list.add(EcgRecord(bytes.copyOfRange(i*10, (i+1)*10)))
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
    class EcgRecord(val bytes: ByteArray) {
        var timestamp: Long   // 时间戳 秒s
        var recordName: String
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var second: Int
        var measureMode: Int  // 测量方式 1：Hand-Hand，2：Hand-Chest，3：1-Lead，4：2-Lead
        var measureModeMess: String
        var normal: Boolean   // true：good，false：bad

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
            measureMode = byte2UInt(bytes[index])
            measureModeMess = getModeMess(0, measureMode)
            index++
            normal = byte2UInt(bytes[index]) == 0
            index++
            recordName = getTimeString(year, month, day, hour, minute, second)
            timestamp = getSecondTimestamp(recordName)
        }
        override fun toString(): String {
            return """
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
                second : $second
                measureMode : $measureMode
                measureModeMess : $measureModeMess
                normal : $normal
                recordName : $recordName
                timestamp : $timestamp
            """.trimIndent()
        }
    }
    @ExperimentalUnsignedTypes
    class EcgFile(val fileName: String, val fileSize: Int, val bytes: ByteArray) {
        var hrsDataSize: Int                  // 波形心率大小（byte）
        var recordingTime: Int                // 记录时长 s
        var waveDataSize: Int                 // 波形数据大小（byte）
        var hr: Int                           // HR，单位为bpm
        var st: Float                         // ST（以ST/100存储），单位为mV(内部导联写0)
        var qrs: Int                          // QRS，单位为ms
        var pvcs: Int                         // PVCs(内部导联写0)
        var qtc: Int                          // QTc单位为ms
        var result: Int
        var diagnosis: CheckmeLeEcgDiagnosis  // 诊断结果
        var measureMode: Int                  // 测量模式 1：Hand-Hand，2：Hand-Chest，3：1-Lead，4：2-Lead
        var measureModeMess: String
        var filterMode: Int                   // 滤波模式（1：wide   0：normal）
        var qt: Int                           // QT单位为ms
        var hrsData: ByteArray                // ECG心率值，从数据采样开始，采样率为1Hz，每个心率值为2byte（实际20s数据，每秒出一个心率），若出现无效心率，则心率为0
        var hrsIntData: IntArray              // ECG心率值
        var waveData: ByteArray               // 每个采样点2byte，原始数据
        var waveShortData: ShortArray         // 每个采样点2byte
        var wFs: FloatArray                   // 转毫伏值(n*4033)/(32767*12*8)
        init {
            var index = 0
            hrsDataSize = toUInt(bytes.copyOfRange(index, index+2))
            recordingTime = hrsDataSize/2
            index += 2
            waveDataSize = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            hr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            st = bytesToSignedShort(bytes[index], bytes[index+1]).div(100f)
            index += 2
            qrs = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pvcs = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            qtc = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            result = byte2UInt(bytes[index])
            diagnosis = CheckmeLeEcgDiagnosis(bytes[index])
            index++
            measureMode = byte2UInt(bytes[index])
            measureModeMess = getModeMess(0, measureMode)
            index++
            filterMode = byte2UInt(bytes[index])
            index++
            qt = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            hrsData = bytes.copyOfRange(index, index+hrsDataSize)
            hrsIntData = IntArray(hrsData.size/2)
            for (i in hrsIntData.indices) {
                hrsIntData[i] = toUInt(hrsData.copyOfRange(i*2, i*2+2))
            }
            val tempSize = index+hrsDataSize+waveDataSize
            waveData = if (tempSize > bytes.size) {
                bytes.copyOfRange(index+hrsDataSize, bytes.size)
            } else {
                bytes.copyOfRange(index + hrsDataSize, tempSize)
            }
            val len = waveData.size/2
            val tempData = ShortArray(len)
            waveShortData = ShortArray(waveData.size)
            wFs = FloatArray(waveData.size)
            for (i in 0 until len) {
                tempData[i] = toSignedShort(waveData[i*2], waveData[i*2+1])
                if (i != 0) {
                    waveShortData[i*2-1] = (tempData[i-1] + tempData[i]).div(2).toShort()
                    waveShortData[i*2] = tempData[i]
                } else {
                    waveShortData[i*2] = tempData[i]
                }
            }
            for (i in waveShortData.indices) {
                wFs[i] = (waveShortData[i] * 4033) / (32767 * 12 * 8f)
            }
        }
        override fun toString(): String {
            return """
                fileSize : $fileSize
                bytes.size : ${bytes.size}
                hrsDataSize : $hrsDataSize
                recordingTime : $recordingTime
                waveDataSize : $waveDataSize
                hr : $hr
                st : $st
                qrs : $qrs
                pvcs : $pvcs
                qtc : $qtc
                result : $result
                diagnosis : $diagnosis
                measureMode : $measureMode
                measureModeMess : $measureModeMess
                filterMode : $filterMode
                qt : $qt
                hrsData : ${bytesToHex(hrsData)}
                hrsIntData : ${Arrays.toString(hrsIntData)}
                waveData : ${bytesToHex(waveData)}
            """.trimIndent()
        }
    }

    fun getModeMess(type: Int, mode: Int): String {
        // 0:ecg, 1:oxy
        return if (type == 1) {
            when (mode) {
                0 -> "Internal"
                1 -> "External"
                else -> ""
            }
        } else {
            when (mode) {
                1 -> "Internal Lead I"
                2 -> "Internal Lead II"
                3 -> "External Lead I"
                4 -> "External Lead II"
                else -> ""
            }
        }
    }

}
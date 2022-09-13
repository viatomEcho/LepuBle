package com.lepu.blepro.ble.cmd

import com.lepu.blepro.ble.data.ExEcgDiagnosis
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.toSignedShort
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.bytesToSignedShort
import com.lepu.blepro.utils.getTimeString
import com.lepu.blepro.utils.toUInt
import org.json.JSONObject
import java.util.*

class PulsebitBleResponse{

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
        var swVersion: String    // 软件版本
        var lgVersion: String    // 语言包版本
        var curLanguage: String  // 当前语言版本
        var sn: String           // 序列号
        var fileVer:String       // 文件解析协议版本
        var spcpVer:String       // 蓝牙通讯协议版本
        var branchCode:String    // code码
        var application:String   //

        init {
            val data = String(bytes)
            infoStr = if (data.contains("{")) {
                JSONObject(data)
            } else {
                JSONObject()
            }
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
        var hrsDataSize: Int              // 波形心率大小（byte）
        var recordingTime: Int            // 记录时长 s
        var waveDataSize: Int             // 波形数据大小（byte）
        var hr: Int                       // HR，单位为bpm
        var st: Float                     // ST（以ST/100存储），单位为mV(内部导联写0)
        var qrs: Int                      // QRS，单位为ms
        var pvcs: Int                     // PVCs(内部导联写0)
        var qtc: Int                      // QTc单位为ms
        var result: Int
        var diagnosis: ExEcgDiagnosis     // 诊断结果
        var measureMode: Int              // 测量模式 1：内部导联I，2：内部导联II，3：外部导联I，4：外部导联II
        var filterMode: Int               // 滤波模式（1：wide   0：normal）
        var qt: Int                       // QT单位为ms
        var hrsData: ByteArray            // ECG心率值，从数据采样开始，采样率为1Hz，每个心率值为2byte（实际20s数据，每秒出一个心率），若出现无效心率，则心率为0
        var hrsIntData: IntArray          // ECG心率值
        var waveData: ByteArray           // 每个采样点2byte，原始数据
        var waveShortData: ShortArray     // 每个采样点2byte
        var wFs: FloatArray               // 转毫伏值(n*4033)/(32767*12*8)

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
            result = toUInt(bytes.copyOfRange(index, index+4))
            diagnosis = ExEcgDiagnosis(bytes.copyOfRange(index, index+4))
            index += 4
            measureMode = byte2UInt(bytes[index])
            index++
            filterMode = byte2UInt(bytes[index])
            index++
            qt = toUInt(bytes.copyOfRange(index, index+2))
            hrsData = bytes.copyOfRange(44, 44+hrsDataSize)
            hrsIntData = IntArray(hrsData.size/2)
            for (i in hrsIntData.indices) {
                hrsIntData[i] = toUInt(hrsData.copyOfRange(i*2, i*2+2))
            }
            val tempSize = 44+hrsDataSize+waveDataSize
            waveData = if (tempSize > bytes.size) {
                bytes.copyOfRange(44+hrsDataSize, bytes.size)
            } else {
                bytes.copyOfRange(44 + hrsDataSize, tempSize)
            }
            val len = waveData.size/2
            waveShortData = ShortArray(len)
            wFs = FloatArray(len)
            for (i in 0 until len) {
                waveShortData[i] = toSignedShort(waveData[i*2], waveData[i*2+1])
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
                filterMode : $filterMode
                qt : $qt
                hrsData : ${bytesToHex(hrsData)}
                hrsIntData : ${Arrays.toString(hrsIntData)}
                waveData : ${bytesToHex(waveData)}
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class FileList(val bytes: ByteArray) {
        var size: Int
        var list = mutableListOf<Record>()

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
        var recordName: String
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
            recordName = getTimeString(year, month, day, hour, minute, second)
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
                recordName : $recordName
            """.trimIndent()
        }
    }

}
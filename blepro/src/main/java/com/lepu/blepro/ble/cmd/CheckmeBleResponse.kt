package com.lepu.blepro.ble.cmd

import android.util.Log
import com.lepu.blepro.ble.cmd.CheckmeLeBleResponse.getModeMess
import com.lepu.blepro.ble.data.CheckmeEcgDiagnosis
import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.toSignedShort
import com.lepu.blepro.utils.HexString.trimStr
import org.json.JSONObject
import java.util.*

class CheckmeBleResponse{

    @ExperimentalUnsignedTypes
    class BleResponse(val bytes: ByteArray) {
        var head: Int
        var no: Int
        var len: Int
        var state: Boolean
        var content: ByteArray

        init {
            head = byte2UInt(bytes[0])
            state = if (head == 0x55) {
                bytes[1].toInt() == 0x00
            } else {
                true
            }
            no = if (head == 0x55) {
                toUInt(bytes.copyOfRange(3, 5))
            } else {
                0
            }
            len = if (head == 0x55) {
                bytes.size - 8
            } else {
                bytes.size - 4
            }
            content = if (head == 0x55) {
                bytes.copyOfRange(7, 7 + len)
            } else {
                bytes.copyOfRange(4, 4 + len)
            }
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
        var branchCode:String    // code码
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
    class ListContent(val userId: Int, val type: Int, val content: ByteArray) {

    }
    @ExperimentalUnsignedTypes
    class FileContent(val fileName: String, val userId: Int, val type: Int, val content: ByteArray) {

    }

    class UserList(val bytes: ByteArray) {
        var size: Int
        var list = mutableListOf<UserInfo>()
        init {
            size = bytes.size.div(27)
            for (i in 0 until size) {
                list.add(UserInfo(bytes.copyOfRange(i*27, (i+1)*27)))
            }
        }
        override fun toString(): String {
            return """
                UserList : 
                size : $size
                list : $list
            """.trimIndent()
        }
    }
    class UserInfo(val bytes: ByteArray) {
        var id: Int
        var name: String
        var icon: Int
        var sex: Int       // 0：男，1：女
        var year: Int
        var month: Int
        var day: Int
        var weight: Float  // kg, 20kg-160kg
        var height: Int    // cm, 60cm-240cm
        init {
            var index = 0
            id = byte2UInt(bytes[index])
            index++
            name = trimStr(String(bytes.copyOfRange(index, index+16)))
            index += 16
            icon = byte2UInt(bytes[index])
            index++
            sex = byte2UInt(bytes[index])
            index++
            year = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            month = byte2UInt(bytes[index])
            index++
            day = byte2UInt(bytes[index])
            index++
            weight = toUInt(bytes.copyOfRange(index, index+2)).div(10f)
            index += 2
            height = toUInt(bytes.copyOfRange(index, index+2))
        }
        override fun toString(): String {
            return """
                UserInfo : 
                id : $id
                name : $name
                icon : $icon
                sex : $sex
                year : $year
                month : $month
                day : $day
                weight : $weight
                height : $height
            """.trimIndent()
        }
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
                DlcList : 
                size : $size
                list : $list
            """.trimIndent()
        }
    }
    @ExperimentalUnsignedTypes
    class DlcRecord(val bytes: ByteArray) {
        var timestamp: Long
        var recordName: String
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var second: Int
        var hr: Int
        var ecgResult: Int   // ECG笑脸/哭脸(0是笑脸，1是哭脸，别的值不显示)
        var spo2: Int
        var pi: Float
        var spo2Result: Int  // SPO2笑脸/哭脸(0是笑脸，1是哭脸，别的值不显示)
        var bpi: Int         // 0xFF表示无效值
        var dia: Int         // 舒张压，0xFF表示无效值
        var sys: Int         // 收缩压，0xFF表示无效值
        var bpiResult: Int   // BPI笑脸/哭脸(0是笑脸，1是哭脸，别的值不显示)
        var voice: Boolean   // 是否包含语音
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
            ecgResult = byte2UInt(bytes[index])
            index++
            spo2 = byte2UInt(bytes[index])
            index++
            pi = byte2UInt(bytes[index]).div(10f)
            index++
            spo2Result = byte2UInt(bytes[index])
            index++
            when (val flag = byte2UInt(bytes[index])) {
                0x00 -> {
                    bpi = 100
                    sys = 0xFF
                    dia = 0xFF
                }
                0xFF -> {
                    bpi = 0xFF
                    sys = 0xFF
                    dia = 0xFF
                }
                else -> {
                    bpi = 0xFF
                    dia = flag
                    sys = byte2UInt(bytes[index+1])
                }
            }
            Log.d("DlcRecord", "byte2UInt(bytes[index]) : ${byte2UInt(bytes[index])}")
            Log.d("DlcRecord", "byte2UInt(bytes[index+1]) : ${byte2UInt(bytes[index+1])}")
            index += 2
            bpiResult = byte2UInt(bytes[index])
            index++
            voice = byte2UInt(bytes[index]) == 1
            recordName = getTimeString(year, month, day, hour, minute, second)
            timestamp = DateUtil.getSecondTimestamp(recordName)
        }
        override fun toString(): String {
            return """
                DlcRecord : 
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
                second : $second
                hr : $hr
                ecgResult : $ecgResult
                spo2 : $spo2
                pi : $pi
                spo2Result : $spo2Result
                bpi : $bpi
                dia : $dia
                sys : $sys
                bpiResult : $bpiResult
                voice : $voice
                recordName : $recordName
                timestamp : $timestamp
            """.trimIndent()
        }
    }

    class BpcalList(val bytes: ByteArray) {
        var size: Int
        var list = mutableListOf<BpcalRecord>()
        init {
            size = bytes.size.div(12)
            for (i in 0 until size) {
                list.add(BpcalRecord(bytes.copyOfRange(i*12, (i+1)*12)))
            }
        }
        override fun toString(): String {
            return """
                BpcalList : 
                size : $size
                list : $list
            """.trimIndent()
        }
    }
    class BpcalRecord(val bytes: ByteArray) {
        var id: Int
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var second: Int
        var bpi: Int
        var dia: Int
        var sys: Int
        init {
            var index = 0
            id = byte2UInt(bytes[index])
            index++
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
            bpi = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            dia = byte2UInt(bytes[index])
            index++
            sys = byte2UInt(bytes[index])
        }
        override fun toString(): String {
            return """
                BpcalRecord : 
                id : $id
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
                second : $second
                bpi : $bpi
                dia : $dia
                sys : $sys
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
                EcgList : 
                size : $size
                list : $list
            """.trimIndent()
        }
    }
    @ExperimentalUnsignedTypes
    class EcgRecord(val bytes: ByteArray) {
        var timestamp: Long         // 时间戳 秒s
        var recordName: String
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var second: Int
        var measureMode: Int         // 测量方式 1：Hand-Hand，2：Hand-Chest，3：1-Lead，4：2-Lead
        var measureModeMess: String
        var result: Int              // 笑脸/哭脸(0是笑脸，1是哭脸，别的值不显示)
        var voice: Boolean           // 是否包含语音
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
            result = byte2UInt(bytes[index])
            index++
            voice = byte2UInt(bytes[index]) == 1
            recordName = getTimeString(year, month, day, hour, minute, second)
            timestamp = DateUtil.getSecondTimestamp(recordName)
        }
        override fun toString(): String {
            return """
                EcgRecord : 
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
                second : $second
                measureMode : $measureMode
                measureModeMess : $measureModeMess
                result : $result
                voice : $voice
                recordName : $recordName
                timestamp : $timestamp
            """.trimIndent()
        }
    }
    @ExperimentalUnsignedTypes
    class EcgFile(val bytes: ByteArray) {
        var hrsDataSize: Int                  // 波形心率大小（byte）
        var recordingTime: Int                // 记录时长 s
        var waveDataSize: Int                 // 波形数据大小（byte）
        var hr: Int                           // HR，单位为bpm
        var st: Float                         // ST（以ST/100存储），单位为mV(内部导联写0)
        var qrs: Int                          // QRS，单位为ms
        var pvcs: Int                         // PVCs(内部导联写0)
        var qtc: Int                          // QTc单位为ms
        var result: Int
        var diagnosis: CheckmeEcgDiagnosis  // 诊断结果
        var measureMode: Int                  // 测量模式 1：Hand-Hand，2：Hand-Chest，3：1-Lead，4：2-Lead
        var measureModeMess: String
        var filterMode: Int                   // 滤波模式（1：wide，0：normal）
        var qt: Int                           // QT单位为ms
        var hrsData: ByteArray                // ECG心率值，从数据采样开始，采样率为1Hz，每个心率值为2byte（实际20s数据，每秒出一个心率），若出现无效心率，则心率为0
        var hrsIntData: IntArray              // ECG心率值
        var waveData: ByteArray               // 每个采样点2byte，原始数据
        var waveShortData: ShortArray         // 每个采样点2byte，未解压，250HZ
        var waveDecompress: ShortArray        // 解压数据，500HZ
        var wFs: FloatArray                   // 转毫伏值(n*4033)/(32767*12*8)，250HZ
        var wFsDecompress: FloatArray         // 转毫伏值(n*4033)/(32767*12*8)，500HZ
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
            diagnosis = CheckmeEcgDiagnosis(bytes[index])
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
            waveShortData = ShortArray(len)
            waveDecompress = ShortArray(waveData.size)
            wFs = FloatArray(waveShortData.size)
            wFsDecompress = FloatArray(waveData.size)
            for (i in 0 until len) {
                waveShortData[i] = toSignedShort(waveData[i*2], waveData[i*2+1])
                if (i != 0) {
                    waveDecompress[i*2-1] = (waveShortData[i-1] + waveShortData[i]).div(2).toShort()
                    waveDecompress[i*2] = waveShortData[i]
                } else {
                    waveDecompress[i*2] = waveShortData[i]
                }
            }
            for (i in waveShortData.indices) {
                wFs[i] = (waveShortData[i] * 4033) / (32767 * 12 * 8f)
            }
            for (i in waveDecompress.indices) {
                wFsDecompress[i] = (waveDecompress[i] * 4033) / (32767 * 12 * 8f)
            }
        }
        override fun toString(): String {
            return """
                EcgFile : 
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
                OxyList : 
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
        var result: Int              // 笑脸/哭脸(0是笑脸，1是哭脸，别的值不显示)
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
            result = byte2UInt(bytes[index])
            recordName = getTimeString(year, month, day, hour, minute, second)
            timestamp = DateUtil.getSecondTimestamp(recordName)
        }
        override fun toString(): String {
            return """
                OxyRecord : 
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
                result : $result
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
                TempList : 
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
        var measureMode: Int  // 测量方式(0：体温，1：物体温度)
        var temp: Float       // 单位摄氏度
        var result: Int       // 笑脸/哭脸(0是笑脸，1是哭脸，别的值不显示)
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
            index++
            temp = toUInt(bytes.copyOfRange(index, index + 2)).div(10f)
            index += 2
            result = byte2UInt(bytes[index])
            recordName = getTimeString(year, month, day, hour, minute, second)
            timestamp = DateUtil.getSecondTimestamp(recordName)
        }
        override fun toString(): String {
            return """
                TempRecord : 
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
                second : $second
                measureMode : $measureMode
                temp : $temp
                result : $result
                recordName : $recordName
                timestamp : $timestamp
            """.trimIndent()
        }
    }

    class SlmList(val bytes: ByteArray) {
        var size: Int
        var list = mutableListOf<SlmRecord>()
        init {
            size = bytes.size.div(18)
            for (i in 0 until size) {
                list.add(SlmRecord(bytes.copyOfRange(i*18, (i+1)*18)))
            }
        }
        override fun toString(): String {
            return """
                SlmList : 
                size : $size
                list : $list
            """.trimIndent()
        }
    }
    class SlmRecord(val bytes: ByteArray) {
        var timestamp: Long     // 时间戳 秒s
        var recordName: String
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var second: Int
        var duration: Int
        var lowSpo2Time: Int
        var lowSpo2Count: Int
        var lowSpo2: Int
        var avgSpo2: Int
        var result: Int     // 笑脸/哭脸(0是笑脸，1是哭脸，别的值不显示)
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
            duration = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            lowSpo2Time = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            lowSpo2Count = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            lowSpo2 = byte2UInt(bytes[index])
            index++
            avgSpo2 = byte2UInt(bytes[index])
            index++
            result = byte2UInt(bytes[index])
            recordName = getTimeString(year, month, day, hour, minute, second)
            timestamp = DateUtil.getSecondTimestamp(recordName)
        }
        override fun toString(): String {
            return """
                SlmRecord : 
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
                second : $second
                duration : $duration
                lowSpo2Time : $lowSpo2Time
                lowSpo2Count : $lowSpo2Count
                lowSpo2 : $lowSpo2
                avgSpo2 : $avgSpo2
                result : $result
                timestamp : $timestamp
                recordName : $recordName
            """.trimIndent()
        }
    }
    class SlmFile(val bytes: ByteArray) {
        var spo2List = mutableListOf<Int>()
        var hrList = mutableListOf<Int>()
        init {
            var len = bytes.size.div(2)
            for (i in 0 until len) {
                spo2List.add(byte2UInt(bytes[i*2]))
                hrList.add(byte2UInt(bytes[i*2+1]))
            }
        }
        override fun toString(): String {
            return """
                SlmFile : 
                spo2List : $spo2List
                hrList : $hrList
            """.trimIndent()
        }
    }

    class PedList(val bytes: ByteArray) {
        var size: Int
        var list = mutableListOf<PedRecord>()
        init {
            size = bytes.size.div(29)
            for (i in 0 until size) {
                list.add(PedRecord(bytes.copyOfRange(i*29, (i+1)*29)))
            }
        }
        override fun toString(): String {
            return """
                PedList : 
                size : $size
                list : $list
            """.trimIndent()
        }
    }
    class PedRecord(val bytes: ByteArray) {
        var timestamp: Long     // 时间戳 秒s
        var recordName: String
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var second: Int
        var step: Long          // 步数
        var distance: Double    // 距离(单位km)
        var avgSpeed: Double    // 平均速度(单位km/h)
        var calorie: Double     // 卡路里(单位kcal)
        var fat: Float          // 脂肪(单位g)
        var duration: Int       // 运动时长(分钟)
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
            step = toLong(bytes.copyOfRange(index, index+4))
            index += 4
            distance = toLong(bytes.copyOfRange(index, index+4)).div(100.0)
            index += 4
            avgSpeed = toLong(bytes.copyOfRange(index, index+4)).div(10.0)
            index += 4
            calorie = toLong(bytes.copyOfRange(index, index+4)).div(100.0)
            index += 4
            fat = toUInt(bytes.copyOfRange(index, index+2)).div(100f)
            index += 2
            duration = toUInt(bytes.copyOfRange(index, index+2))
            recordName = getTimeString(year, month, day, hour, minute, second)
            timestamp = DateUtil.getSecondTimestamp(recordName)
        }
        override fun toString(): String {
            return """
                PedRecord : 
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
                second : $second
                step : $step
                distance : $distance
                avgSpeed : $avgSpeed
                calorie : $calorie
                fat : $fat
                duration : $duration
                recordName : $recordName
                timestamp : $timestamp
            """.trimIndent()
        }
    }

    class BpList(val bytes: ByteArray) {
        var size: Int
        var list = mutableListOf<BpRecord>()
        init {
            size = bytes.size.div(11)
            for (i in 0 until size) {
                list.add(BpRecord(bytes.copyOfRange(i*11, (i+1)*11)))
            }
        }
        override fun toString(): String {
            return """
                BpList : 
                size : $size
                list : $list
            """.trimIndent()
        }
    }
    class BpRecord(val bytes: ByteArray) {
        var timestamp: Long     // 时间戳 秒s
        var recordName: String
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var second: Int
        var sys: Int            // 收缩压
        var dia: Int            // 舒张压
        var pr: Int
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
            sys = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            dia = byte2UInt(bytes[index])
            index++
            pr = byte2UInt(bytes[index])
            recordName = getTimeString(year, month, day, hour, minute, second)
            timestamp = DateUtil.getSecondTimestamp(recordName)
        }
        override fun toString(): String {
            return """
                BpRecord : 
                timestamp : $timestamp
                recordName : $recordName
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
                second : $second
                sys : $sys
                dia : $dia
                pr : $pr
            """.trimIndent()
        }
    }

    class GluList(val bytes: ByteArray) {
        var size: Int
        var list = mutableListOf<GluRecord>()
        init {
            size = bytes.size.div(32)
            for (i in 0 until size) {
                list.add(GluRecord(bytes.copyOfRange(i*32, (i+1)*32)))
            }
        }
        override fun toString(): String {
            return """
                GluList : 
                size : $size
                list : $list
            """.trimIndent()
        }
    }
    class GluRecord(val bytes: ByteArray) {
        var timestamp: Long     // 时间戳 秒s
        var recordName: String
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var second: Int
        var glu: Int            // 血糖值, 单位mg/dL
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
            glu = toUInt(bytes.copyOfRange(index, index+2))
            recordName = getTimeString(year, month, day, hour, minute, second)
            timestamp = DateUtil.getSecondTimestamp(recordName)
        }
        override fun toString(): String {
            return """
                GluRecord : 
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
                second : $second
                glu : $glu
                recordName : $recordName
                timestamp : $timestamp
            """.trimIndent()
        }
    }

    class RtData(val bytes: ByteArray) {
        var ecgWave: ByteArray
        var ecgwIs: ShortArray
        var ecgwFs: FloatArray
        var hr: Int
        var qrs: Int
        var st: Int
        var pvcs: Int
        var mark: Int
        var ecgNote: Int
        var spo2Wave: ByteArray
        var spo2wIs: IntArray
        var pr: Int
        var spo2: Int
        var pi: Float
        var pulseSound: Int
        var spo2Note: Int
        var battery: Int         // 0-100
        init {
            var index = 0
            ecgWave = bytes.copyOfRange(index, index+10)
            val len = ecgWave.size/2
            ecgwIs = ShortArray(len)
            ecgwFs = FloatArray(len)
            for (i in ecgwFs.indices) {
                ecgwIs[i] = toSignedShort(ecgWave[2 * i], ecgWave[2 * i + 1])
                ecgwFs[i] = ((ecgwIs[i]*4033)/(32767*12f))*1.05f
            }

            index += 10
            hr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            qrs = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            st = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pvcs = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            mark = toUInt(bytes.copyOfRange(index, index+1))
            index++
            ecgNote = toUInt(bytes.copyOfRange(index, index+1))
            index++

            index++
            spo2Wave = bytes.copyOfRange(index, index+10)
            spo2wIs = IntArray(spo2Wave.size/2)
            for (i in spo2wIs.indices) {
                spo2wIs[i] = toUInt(spo2Wave.copyOfRange(2 * i, 2 * i + 2))
            }

            index += 10
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            spo2 = toUInt(bytes.copyOfRange(index, index+1))
            index++
            pi = toUInt(bytes.copyOfRange(index, index+1)).div(10f)
            index++
            pulseSound = toUInt(bytes.copyOfRange(index, index+1))
            index++
            spo2Note = toUInt(bytes.copyOfRange(index, index+1))
            index++
            index++
            battery = toUInt(bytes.copyOfRange(index, index+1))
        }
        override fun toString(): String {
            return """
                RtData : 
                ecgWave: ${Arrays.toString(ecgWave)}
                ecgwIs: ${Arrays.toString(ecgwIs)}
                ecgwFs: ${Arrays.toString(ecgwFs)}
                hr: $hr
                qrs: $qrs
                st: $st
                pvcs: $pvcs
                mark: $mark
                ecgNote: $ecgNote
                spo2Wave: ${Arrays.toString(spo2Wave)}
                spo2wIs: ${Arrays.toString(spo2wIs)}
                pr: $pr
                spo2: $spo2
                pi: $pi
                pulseSound: $pulseSound
                spo2Note: $spo2Note
                battery: $battery
            """.trimIndent()
        }
    }
}
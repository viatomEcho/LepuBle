package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.toSignedShort
import org.json.JSONObject
import java.util.*

class CheckmePodBleResponse{

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
    class DeviceInfo (val bytes: ByteArray) {
        var infoStr: JSONObject
        var region: String       // 地区版本
        var model: String        // 系列版本
        var hwVersion: String    // 硬件版本
        var swVersion: String    // 软件版本
        var lgVersion: String    // 语言版本
        var curLanguage: String  // 语言版本
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
    class RtData (var bytes: ByteArray) {
        var param: RtParam
        var wave: RtWave

        init {
            var index = 0
            param = RtParam(bytes.copyOfRange(index, index+20))
            index += 20
            wave = RtWave(bytes.copyOfRange(index, bytes.size))
        }

        override fun toString(): String {
            return """
                param : $param
                wave : $wave
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class RtParam (var bytes: ByteArray) {
        var pr: Int               // 脉率值
        var spo2: Int             // 血氧值
        var pi: Float             // pi值
        var temp: Float           // 温度值 ℃
        var oxyState: Int         // 血氧探头（0：未接入血氧电缆 1：未接入手指 2：接入手指）
        var tempState: Int        // 体温探头（0：未接入 1：接入）
        var batteryState: Int     // 充电状态（0：没有充电 1：充电中 2：充电完成 3：低电量）
        var battery: Int          // 电量（0-100%）
        var runStatus: Int        // 运行状态（0：空闲 1：测量准备 2：测量中）

        init {
            var index = 0
            pr =  toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            spo2 = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pi = byte2UInt(bytes[index]).div(10f)
            index++
            temp = toUInt(bytes.copyOfRange(index, index+2)).div(10f)
            index += 2
            oxyState = byte2UInt(bytes[index]) and 0x03
            tempState = (byte2UInt(bytes[index]) and 0x0C) shr 2
            index++
            batteryState = byte2UInt(bytes[index])
            index++
            battery = byte2UInt(bytes[index])
            index++
            runStatus = byte2UInt(bytes[index])
        }

        override fun toString(): String {
            return """
                RtParam : 
                pr = $pr
                spo2 = $spo2
                pi = $pi
                temp = $temp
                oxyState = $oxyState
                tempState = $tempState
                batteryState = $batteryState
                battery = $battery
                runStatus = $runStatus
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class RtWave (var bytes: ByteArray) {
        var len: Int
        var waveByte: ByteArray
        var wFs: IntArray

        init {
            var index = 0
            len = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            waveByte = bytes.copyOfRange(index, bytes.size)
            if (len != waveByte.size.div(2)) {
                len = waveByte.size.div(2)
            }
            wFs = IntArray(len)
            for (i in 0 until len) {
                wFs[i] = toSignedShort(waveByte[i*2], waveByte[i*2+1]).toInt()
            }
        }
        override fun toString(): String {
            return """
                RtWave : 
                len = $len
                waveByte = ${bytesToHex(waveByte)}
                wFs = ${Arrays.toString(wFs)}
            """.trimIndent()
        }

    }

    @ExperimentalUnsignedTypes
    class OxiTFile(val name: String, val size: Int) {
        var fileName: String
        var fileSize: Int
        var fileContent: ByteArray
        var index: Int  // 标识当前下载index

        init {
            fileName = name
            fileSize = size
            fileContent = ByteArray(size)
            index = 0
        }

        fun addContent(bytes: ByteArray) {
            if (index >= fileSize) {
                LepuBleLog.d("index > fileSize. 文件下载完成")
                return
            } else {
                System.arraycopy(bytes, 0, fileContent, index, bytes.size)
                index += bytes.size
            }
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
        var timestamp: Long   // 时间戳 秒s
        var recordName: String
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var second: Int
        var leadType: Int         // 导联方式 0：内部，1：外部
        var leadTypeMess: String
        var spo2: Int
        var pr: Int
        var pi: Float
        var temp: Float    // 温度值 ℃

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
            leadType = byte2UInt(bytes[index])
            leadTypeMess = getTypeMess(leadType)
            index++
            spo2 = byte2UInt(bytes[index])
            index++
            pr = byte2UInt(bytes[index])
            index++
            pi = byte2UInt(bytes[index]).div(10f)
            index++
            temp = toUInt(bytes.copyOfRange(index, index+2)).div(10f)
            recordName = getTimeString(year, month, day, hour, minute, second)
            timestamp = DateUtil.getSecondTimestamp(recordName)
        }

        private fun getTypeMess(type: Int): String {
            return when (type) {
                0 -> "Internal"
                1 -> "External"
                else -> ""
            }
        }

        override fun toString(): String {
            return """
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
                second : $second
                leadType : $leadType
                leadTypeMess : $leadTypeMess
                spo2 : $spo2
                pr : $pr
                pi : $pi
                temp : $temp
                recordName : $recordName
                timestamp : $timestamp
            """.trimIndent()
        }
    }
}
package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.utils.ByteUtils
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.toUInt
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject

class OxyBleResponse{

    @ExperimentalUnsignedTypes
    class OxyResponse(bytes: ByteArray) {
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

    @Parcelize
    class OxyInfo (val bytes: ByteArray) : Parcelable {
        var region: String       // 地区版本
        var model: String        // 系列版本
        var hwVersion: String    // 硬件版本
        var swVersion: String    // 软件版本
        var btlVersion: String   // 引导版本
        var pedTar: Int          // 步数
        var sn: String           // 序列号
        var curTime: String      // 时间
        var batteryState: Int    // 电池状态（0为正常使用，1为充电，2为充满）
        var batteryValue:String  // 电量（0%-100%）
        var oxiThr: Int          // 血氧阈值
        var motor: Int           // 强度（KidsO2、Oxylink：最低：5，低：10，中：17，高：22，最高：35；O2Ring：最低：20，低：40，中：60，高：80，最高：100，震动强度不随开关的改变而改变）
        var mode: Int            // 工作模式（0：sleep模式  1：monitor模式）
        var fileList: String     // 文件列表
        var oxiSwitch:Int        // 血氧开关
        var hrSwitch:Int         // 心率开关
        var hrLowThr:Int         // 心率震动最低阈值
        var hrHighThr:Int        // 心率震动最高阈值
        var fileVer:String       // 文件解析协议版本
        var spcpVer:String       // 蓝牙通讯协议版本
        var curState:Int         // 运行状态（0:准备阶段 可升级不可获取波形 1:测量就绪 可升级和获取波形 2:记录数据中 不可升级可获取波形）
        var lightingMode:Int     // 亮屏模式（0：Standard模式，1：Always Off模式，2：Always On模式）
        var lightStr:Int         // 屏幕亮度
        var branchCode:String    // code码
        init {
            var infoStr = JSONObject(String(bytes))
//            try {
//                var infoStr = JSONObject(String(bytes))
//            } catch (e: JSONException) {
//                LogUtils.d(String(bytes))
//            }
            region = infoStr.getString("Region")
            model = infoStr.getString("Model")
            hwVersion = infoStr.getString("HardwareVer")
            swVersion = infoStr.getString("SoftwareVer")
            btlVersion = infoStr.getString("BootloaderVer")
            pedTar = infoStr.getInt("CurPedtar")
            sn = infoStr.getString("SN")
            curTime = infoStr.getString("CurTIME")
            batteryState = infoStr.getInt("CurBatState")
            batteryValue = infoStr.getString("CurBAT")
            oxiSwitch = infoStr.getInt("OxiSwitch")
            oxiThr = infoStr.getInt("CurOxiThr")
            motor = infoStr.getInt("CurMotor")
            mode = infoStr.getInt("CurMode")
            fileList = infoStr.getString("FileList")
            hrSwitch = infoStr.getInt("HRSwitch")
            hrLowThr = infoStr.getInt("HRLowThr")
            hrHighThr = infoStr.getInt("HRHighThr")
            fileVer = infoStr.getString("FileVer")
            spcpVer = infoStr.getString("SPCPVer")
            curState = infoStr.getInt("CurState")
            lightingMode = if (infoStr.has("LightingMode")) {
                infoStr.getInt("LightingMode")
            } else {
                0
            }
            lightStr = if (infoStr.has("LightStr")) {
                infoStr.getInt("LightStr")
            } else {
                0
            }
            branchCode = infoStr.getString("BranchCode")

        }

        override fun toString(): String {
            return """
                OxyInfo : 
                region = $region
                model = $model
                hwVersion = $hwVersion
                swVersion = $swVersion
                btlVersion = $btlVersion
                pedTar = $pedTar
                sn = $sn
                curTime = $curTime
                batteryState = $batteryState
                batteryValue = $batteryValue
                oxiSwitch = $oxiSwitch
                oxiThr = $oxiThr
                motor = $motor
                mode = $mode
                fileList = $fileList
                hrSwitch = $hrSwitch
                hrLowThr = $hrLowThr
                hrHighThr = $hrHighThr
                fileVer = $fileVer
                spcpVer = $spcpVer
                curState = $curState
                lightingMode = $lightingMode
                lightStr = $lightStr
                branchCode = $branchCode
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class RtParam constructor(var bytes: ByteArray) : Parcelable {
        var content: ByteArray = bytes
        var spo2: Int             // 血氧值
        var pr: Int               // 脉率值
        var steps: Int            // 步数
        var battery: Int          // 电量（0-100%）
        var batteryState: Int     // 充电状态（0：没有充电 1：充电中 2：充电完成）
        var vector: Int           // 三轴矢量
        var pi: Int               // pi值
        var state: Int            // 工作状态（0：导联脱落 1：导联连上 其他：异常）

        init {
            spo2 = bytes[0].toUInt().toInt()
            pr =  (bytes[1].toUInt().toInt() and 0xFF) or (bytes[2].toUInt().toInt() and 0xFF shl 8)
            steps = bytes[3].toUInt().toInt() and 0xFF or (bytes[4].toUInt().toInt() and 0xFF shl 8) or (bytes[5].toUInt().toInt() and 0xFF shl 16) or (bytes[6].toUInt().toInt() and 0xFF shl 24)
            battery = bytes[7].toUInt().toInt()
            batteryState = bytes[8].toUInt().toInt()
            vector = bytes[9].toUInt().toInt()
            pi = bytes[10].toUInt().toInt() and 0xFF
            state = bytes[11].toUInt().toInt()
        }

        override fun toString(): String {
            return """
                RtParam : 
                spo2 = $spo2
                pr = $pr
                steps = $steps
                battery = $battery
                batteryState = $batteryState
                vector = $vector
                pi = $pi
                state = $state
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class RtWave constructor(var bytes: ByteArray) : Parcelable {
        var content: ByteArray = bytes
        var spo2: Int
        var pr: Int
        var battery: Int
        var batteryState: Int
        var pi: Int
        var state: Int
        var len: Int
        var waveByte: ByteArray
        var wFs: IntArray? = null
        var wByte: ByteArray? = null

        init {
            spo2 = bytes[0].toUInt().toInt()
            pr = toUInt(bytes.copyOfRange(1, 3))
            battery = bytes[3].toUInt().toInt()
            batteryState = bytes[4].toUInt().toInt()
            pi = ByteUtils.byte2UInt(bytes[5])
            state = bytes[6].toUInt().toInt()
            len = toUInt(bytes.copyOfRange(10, 12))
            waveByte = bytes.copyOfRange(12, 12 + len)
            wFs = IntArray(len)
            wByte = ByteArray(len)
            for (i in 0 until len) {
                var temp = ByteUtils.byte2UInt(waveByte[i])
                if (temp == 156) {
                    if (i==0) {
                        if ((i+1) < len)
                          temp = ByteUtils.byte2UInt(waveByte[i+1])
                    } else if (i == len-1) {
                        temp = ByteUtils.byte2UInt(waveByte[i-1])
                    } else {
                        if ((i+1) < len)
                            temp = (ByteUtils.byte2UInt(waveByte[i-1]) + ByteUtils.byte2UInt(waveByte[i+1]))/2
                    }
                }

                wFs!![i] = temp
                wByte!![i] = (100 - temp/2).toByte()
            }
        }
        override fun toString(): String {
            return """
                RtParam : 
                spo2 = $spo2
                pr = $pr
                battery = $battery
                batteryState = $batteryState
                pi = $pi
                state = $state
                len = $len
            """.trimIndent()
        }

    }

    @Parcelize
    class OxyFile(val model: Int, val name: String, val size: Int, private val userId: String) : Parcelable  {
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
                DownloadHelper.writeFile(model, userId, fileName, "dat", bytes )

                index += bytes.size

            }
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class PPGData constructor(var bytes: ByteArray) : Parcelable {
        var len: Int
        var rawDataBytes: ByteArray

        var rawDataArray: Array<PpgRawData?>
        var irArray: Array<Int?>
        var irByteArray: Array<ByteArray?>
        var redArray: Array<Int?>
        var redByteArray: Array<ByteArray?>
        var motionArray: Array<Int?>

        init {
            len = toUInt(bytes.copyOfRange(0, 2))
            rawDataBytes =  bytes.copyOfRange(2, bytes.size)
            rawDataArray = arrayOfNulls(len)
            irArray = arrayOfNulls(len)
            irByteArray = arrayOfNulls(len)
            redArray = arrayOfNulls(len)
            redByteArray = arrayOfNulls(len)
            motionArray = arrayOfNulls(len)
            for (i in 0  until len ){
                if (bytes.size < (i * 9) + 11) break

                PpgRawData(bytes.copyOfRange(2 + (i * 9), (i * 9) + 11 )).let {
                    rawDataArray[i] = it

                    irArray[i] = it.ir
                    irByteArray[i] = it.irBytes
                    redArray[i] = it.red
                    redByteArray[i] = it.redBytes
                    motionArray[i] = it.motion
                }
            }

        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class PpgRawData(var bytes: ByteArray): Parcelable {
        var ir : Int
        var irBytes: ByteArray
        var red : Int
        var redBytes : ByteArray
        var motion : Int
        init {
            ir = toUInt(bytes.copyOfRange(0, 4))
            irBytes = bytes.copyOfRange(0, 4)
            red = toUInt(bytes.copyOfRange(4, 8))
            redBytes = bytes.copyOfRange(4, 8)
            motion = bytes[8].toUInt().toInt()
        }

    }


}
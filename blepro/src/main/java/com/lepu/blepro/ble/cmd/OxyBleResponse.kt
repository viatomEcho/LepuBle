package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.add
import com.lepu.blepro.utils.toUInt
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject

class OxyBleResponse{

    @ExperimentalUnsignedTypes
    class OxyResponse(val bytes: ByteArray) {
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
        var infoStr: JSONObject
        var region: String       // 地区版本
        var model: String        // 系列版本
        var hwVersion: String    // 硬件版本
        var swVersion: String    // 固件版本
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
        var oxiSwitch:Int        // 血氧开关（bit0:震动  bit1:声音）(int 0：震动关声音关 1：震动开声音关 2：震动关声音开 3：震动开声音开)
        var hrSwitch:Int         // 心率开关（bit0:震动  bit1:声音）(int 0：震动关声音关 1：震动开声音关 2：震动关声音开 3：震动开声音开)
        var hrLowThr:Int         // 心率震动最低阈值
        var hrHighThr:Int        // 心率震动最高阈值
        var fileVer:String       // 文件解析协议版本
        var spcpVer:String       // 蓝牙通讯协议版本
        var curState:Int         // 运行状态（0:准备阶段 可升级不可获取波形 1:测量就绪 可升级和获取波形 2:记录数据中 不可升级可获取波形）
        var lightingMode:Int     // 亮屏模式（0：Standard模式，1：Always Off模式，2：Always On模式）
        var lightStr:Int         // 屏幕亮度
        var branchCode:String    // code码
        var spo2Switch : Int     // 血氧功能开关（0：关 1：开）
        var buzzer: Int          // 声音强度（checkO2Plus：最低：20，低：40，中：60，高：80，最高：100）
        var mtSwitch: Int        // 体动开关（0：关 1：开）
        var mtThr: Int           // 体动阈值
        var ivSwitch: Int        // 无效值报警开关（0：关 1：开）
        var ivThr: Int           // 无效值报警告警时间阈值

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
            btlVersion = infoStrGetString("BootloaderVer")
            pedTar = infoStrGetInt("CurPedtar")
            sn = infoStrGetString("SN")
            curTime = infoStrGetString("CurTIME")
            batteryState = infoStrGetInt("CurBatState")
            batteryValue = infoStrGetString("CurBAT")
            oxiSwitch = infoStrGetInt("OxiSwitch")
            oxiThr = infoStrGetInt("CurOxiThr")
            motor = infoStrGetInt("CurMotor")
            mode = infoStrGetInt("CurMode")
            fileList = infoStrGetString("FileList")
            hrSwitch = infoStrGetInt("HRSwitch")
            hrLowThr = infoStrGetInt("HRLowThr")
            hrHighThr = infoStrGetInt("HRHighThr")
            fileVer = infoStrGetString("FileVer")
            spcpVer = infoStrGetString("SPCPVer")
            curState = infoStrGetInt("CurState")
            lightingMode = infoStrGetInt("LightingMode")
            lightStr = infoStrGetInt("LightStr")
            branchCode = infoStrGetString("BranchCode")
            spo2Switch = infoStrGetInt("SpO2SW")
            buzzer = infoStrGetInt("CurBuzzer")
            mtSwitch = infoStrGetInt("MtSW")
            mtThr = infoStrGetInt("MtThr")
            ivSwitch = infoStrGetInt("IvSW")
            ivThr = infoStrGetInt("IvThr")

        }

        private fun infoStrGetInt(key: String): Int {
            return if (infoStr.has(key)) {
                infoStr.getInt(key)
            } else {
                0
            }
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
                OxyInfo : 
                infoStr = $infoStr
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
                spo2Switch = $spo2Switch
                buzzer = $buzzer
                mtSwitch = $mtSwitch
                mtThr = $mtThr
                ivSwitch = $ivSwitch
                ivThr = $ivThr
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class RtParam constructor(var bytes: ByteArray) : Parcelable {
        var spo2: Int             // 血氧值
        var pr: Int               // 脉率值
        var steps: Int            // 步数
        var battery: Int          // 电量（0-100%）
        var batteryState: Int     // 充电状态（0：没有充电 1：充电中 2：充电完成）
        var vector: Int           // 三轴矢量，体动
        var pi: Int               // pi值
        var state: Int            // 工作状态（0：导联脱落 1：导联连上 其他：异常）
        var countDown: Int        // 导联脱落倒计时（10s-0）
        var invalidIvState: Int   // 无效值报警（0：未达到报警条件 1：达到报警条件 2：达到报警条件，但是盒子不报警）
        var spo2IvState: Int      // 血氧报警（0：未达到报警条件 1：达到报警条件 2：达到报警条件，但是盒子不报警）
        var hrIvState: Int        // 心率报警（0：未达到报警条件 1：达到报警条件 2：达到报警条件，但是盒子不报警）
        var vectorIvState: Int    // 体动报警（0：未达到报警条件 1：达到报警条件 2：达到报警条件，但是盒子不报警）

        init {
            var index = 0
            spo2 = byte2UInt(bytes[index])
            index++
            pr =  toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            steps = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            battery = byte2UInt(bytes[index])
            index++
            batteryState = byte2UInt(bytes[index])
            index++
            vector = byte2UInt(bytes[index])
            index++
            pi = byte2UInt(bytes[index])
            index++
            state = byte2UInt(bytes[index]) and 0x01
            countDown = (byte2UInt(bytes[index]) and 0xF0) shr 4
            index++
            if (bytes.size > index) {
                invalidIvState = byte2UInt(bytes[index]) and 0x03
                spo2IvState = (byte2UInt(bytes[index]) and 0x0C) shr 2
                hrIvState = (byte2UInt(bytes[index]) and 0x30) shr 4
                vectorIvState = (byte2UInt(bytes[index]) and 0xC0) shr 6
            } else {
                invalidIvState = 0
                spo2IvState = 0
                hrIvState = 0
                vectorIvState = 0
            }

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
                countDown = $countDown
                invalidIvState = $invalidIvState
                spo2IvState = $spo2IvState
                hrIvState = $hrIvState
                vectorIvState = $vectorIvState
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class RtWave constructor(var bytes: ByteArray) : Parcelable {
        var spo2: Int
        var pr: Int
        var battery: Int         // 电量（0-100%）
        var batteryState: Int    // 充电状态（0：没有充电 1：充电中 2：充电完成）
        var pi: Int
        var state: Int           // 工作状态（0：导联脱落 1：导联连上 其他：异常）
        var len: Int
        var waveByte: ByteArray
        var wFs: IntArray
        var wByte: ByteArray

        init {
            spo2 = byte2UInt(bytes[0])
            pr = toUInt(bytes.copyOfRange(1, 3))
            battery = byte2UInt(bytes[3])
            batteryState = byte2UInt(bytes[4])
            pi = byte2UInt(bytes[5])
            state = byte2UInt(bytes[6])
            len = toUInt(bytes.copyOfRange(10, 12))
            waveByte = bytes.copyOfRange(12, 12 + len)
            wFs = IntArray(len)
            wByte = ByteArray(len)
            for (i in 0 until len) {
                var temp = byte2UInt(waveByte[i])
                // 处理毛刺
                if (temp == 156 || temp == 246) {
                    if (i==0) {
                        if ((i+1) < len)
                          temp = byte2UInt(waveByte[i+1])
                    } else if (i == len-1) {
                        temp = byte2UInt(waveByte[i-1])
                    } else {
                        if ((i+1) < len)
                            temp = (byte2UInt(waveByte[i-1]) + byte2UInt(waveByte[i+1]))/2
                    }
                }

                wFs[i] = temp
                wByte[i] = (100 - temp/2).toByte()
            }
        }
        override fun toString(): String {
            return """
                RtWave : 
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
            if ((index+bytes.size) <= fileSize) {
                System.arraycopy(bytes, 0, fileContent, index, bytes.size)
//                DownloadHelper.writeFile(model, userId, fileName, "dat", bytes)
            }
            index += bytes.size
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class PPGData constructor(var bytes: ByteArray) : Parcelable {
        var len: Int
        var rawDataBytes: ByteArray

        var rawDataArray = mutableListOf<PpgRawData>()
        var irRedArray: IntArray
        var irRedByteArray = ByteArray(0)
        var irArray : IntArray
        var irByteArray = ByteArray(0)
        var redArray : IntArray
        var redByteArray = ByteArray(0)
        var motionArray : IntArray

        init {
            len = toUInt(bytes.copyOfRange(0, 2))
            rawDataBytes = bytes.copyOfRange(2, bytes.size)
            irRedArray = IntArray(len*2)
            irArray = IntArray(len)
            redArray = IntArray(len)
            motionArray = IntArray(len)
            for (i in 0 until len){
                if (bytes.size < (i * 9) + 11) break

                PpgRawData(bytes.copyOfRange(2 + (i * 9), (i * 9) + 11)).let {
                    rawDataArray.add(it)
                    irRedArray[i*2] = it.ir
                    irRedArray[i*2+1] = it.red
                    irRedByteArray = irRedByteArray.plus(it.irBytes)
                    irRedByteArray = irRedByteArray.plus(it.redBytes)
                    irArray[i] = it.ir
                    irByteArray = add(irByteArray, it.irBytes)
                    redArray[i] = it.red
                    redByteArray = add(redByteArray, it.redBytes)
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
            motion = byte2UInt(bytes[8])
        }
    }


}
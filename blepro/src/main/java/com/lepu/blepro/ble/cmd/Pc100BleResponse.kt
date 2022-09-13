package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.utils.*
import kotlinx.android.parcel.Parcelize

object Pc100BleResponse {

    @ExperimentalUnsignedTypes
    @Parcelize
    class Pc100Response constructor(var bytes: ByteArray) : Parcelable {
        var cmd: Int
        var len: Int
        var type: Int
        var content: ByteArray  // 内容

        init {
            cmd = (bytes[2].toUInt() and 0xFFu).toInt()
            len = toUInt(bytes.copyOfRange(3, 4))
            type = (bytes[4].toUInt() and 0xFFu).toInt()
            content = bytes.copyOfRange(5, 5 + len - 2)
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class DeviceInfo constructor(var bytes: ByteArray) : Parcelable {
        var softwareV: String  // 软件版本
        var hardwareV: String  // 硬件版本
        var batLevel: Int      // 电量等级
        var batStatus: Int     // 充电状态

        // 电量等级 : 级数越高，电量越多（0-3）
        // 充电状态 : 00：为没有充电，01：表示充电中，10：表示充电完成，11：保留
        init {
            var index = 0
            softwareV = byteToPointHex(bytes[index])
            if (bytes.size == 3) {
                index++
                hardwareV = byteToPointHex(bytes[index])
                index++
                batLevel = bytes[index].toInt() and 0x07
                batStatus = (bytes[index].toInt() and 0x30) shr 4
            } else {
                hardwareV = ""
                batLevel = 0
                batStatus = 0
            }
        }

        override fun toString(): String {
            return """
                DeviceInfo
                softwareV : $softwareV
                hardwareV : $hardwareV
                batLevel : $batLevel
                batStatus : $batStatus
            """.trimIndent()
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class BpResult constructor(var bytes: ByteArray) : Parcelable {
        var sys: Int            // 收缩压
        var result: Int         // 心率结果（0：心率正常 1：心率不齐）
        var resultMess: String  // 心率结果
        var map: Int            // 平均压
        var dia: Int            // 舒张压
        var plus: Int           // 脉率

        init {
            sys = ((bytes[0].toInt() and 0x7F) shl 8) + ((bytes[1].toUInt() and 0xFFu).toInt())
            result = (bytes[0].toInt() and 0x80) shr 7
            resultMess = if (result == 0) "HR normal" else "HR irregular"
            map = (bytes[2].toUInt() and 0xFFu).toInt()
            dia = (bytes[3].toUInt() and 0xFFu).toInt()
            plus = (bytes[4].toUInt() and 0xFFu).toInt()
        }

        override fun toString(): String {
            return """
                BpResult
                sys : $sys
                result : $result
                resultMess : $resultMess
                map : $map
                dia : $dia
                plus : $plus
            """.trimIndent()
        }
    }
    @Parcelize
    @ExperimentalUnsignedTypes
    class BpResultError constructor(var bytes: ByteArray) : Parcelable {
        var errorType: Int     // 错误编码类型
        var errorNum: Int      // 错误编码号
        var errorMess: String  // 错误编码信息

        init {
            errorType = (bytes[0].toInt() and 0x80) shr 7
            errorNum = bytes[0].toInt() and 0x0F
            errorMess = getErrorMess(errorType, errorNum)
        }

        private fun getErrorMess(errorType: Int, errorNum: Int) : String {
            /*if (errorType == 0) {
                when(errorNum) {
                    0 -> return "测量不到有效脉搏"
                    1 -> return "7秒内打气不上30mmHg，气袋没有绑好"
                    2 -> return "测量结果数值有误"
                    3 -> return "气袋压力超过295mmHg，进入超压保护"
                    4 -> return "干预过多，测量中移动，说话等"
                    15 -> return "电量过低，血压测量停止"
                }
            } else if (errorType == 1) {*/
                when(errorNum) {
                    1 -> return "Fail to inflate pressure to 30mmHg within 7 seconds(The cuff is not well-wrapped)"
                    2 -> return "Cuff pressure is over 295mmHg(Overpressure protection)"
                    3 -> return "No valid pulse is detected"
                    4 -> return "Excessive motion artifact"
                    5 -> return "The measurement fails"
                    6 -> return "Air leakage"
                    15 -> return "Low battery"
                }
//            }
            return ""
        }

        override fun toString(): String {
            return """
                BpResultError
                errorType : $errorType
                errorNum : $errorNum
                errorMess : $errorMess
            """.trimIndent()
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class BpStatus constructor(var bytes: ByteArray) : Parcelable {
        var status: Int         // 状态
        var statusMess: String

        // status：测量状态，包括以下3种状态。
        // 0x00：测量结束，
        // 0x01：模块忙或测量正在进行中，
        // 0xFF：测量模块故障或未接入。
        init {
            var index = 0
            status = (bytes[index].toUInt() and 0xFFu).toInt()
            statusMess = getStateMess(status)
        }

        override fun toString(): String {
            return """
                BpStatus
                status : $status
                statusMess : $statusMess
            """.trimIndent()
        }
    }

    fun getStateMess(state: Int) : String {
        when(state) {
            0x00 -> return "测量结束"
            0x01 -> return "模块忙或测量正在进行中"
            0xFF -> return "测量模块故障或未接入"
        }
        return ""
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class RtBpData constructor(var bytes: ByteArray) : Parcelable {
        var sign: Int     // 心跳标记（0 无心跳 1 有心跳）
        var psValue: Int  // 当前压力值
        init {
            sign = (bytes[0].toInt() and 0x10) shr 4
            psValue = ((bytes[0].toInt() and 0x0F) shl 8) + ((bytes[1].toUInt() and 0xFFu).toInt())
        }

        override fun toString(): String {
            return """
                RtBpData
                sign : $sign
                psValue : $psValue
            """.trimIndent()
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class BoStatus constructor(var bytes: ByteArray) : Parcelable {
        var status: Int         // 状态
        var statusMess: String  // 状态
        var sw_ver: String      // 血氧软件版本号
        var hw_ver: String      // 血氧硬件版本号

        // status：测量状态，包括以下3种状态。
        // 0x00：测量结束，
        // 0x01：模块忙或测量正在进行中，
        // 0xFF：测量模块故障或未接入。
        init {
            var index = 0
            status = (bytes[index].toUInt() and 0xFFu).toInt()
            statusMess = getStateMess(status)
            index++
            sw_ver = byteToPointHex(bytes[index])
            index++
            hw_ver = byteToPointHex(bytes[index])
        }

        override fun toString(): String {
            return """
                BoStatus
                status : $status
                statusMess : $statusMess
                sw_ver : $sw_ver
                hw_ver : $hw_ver
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class RtBoWave(val bytes: ByteArray) {
        val waveData: ByteArray
        val waveIntData: IntArray
        init {
            waveData = bytes.copyOfRange(0, bytes.size).toList().asSequence().map { (it.toInt() and 0x7f).toByte() }.toList().toByteArray()
            waveIntData =  bytes.copyOfRange(0, bytes.size).toList().asSequence().map { (it.toInt() and 0x7f)}.toList().toIntArray()
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class RtBoParam constructor(var bytes: ByteArray) : Parcelable {
        var spo2: Int             // （0-100）
        var pr: Int               // （0-511）
        var pi: Int               // （0-255）
        var isDetecting: Boolean  // 探头检测中
        var isScanning: Boolean   // 脉搏扫描中

        init {
            var index = 0
            spo2 = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pi = (bytes[index].toUInt() and 0xFFu).toInt()
            index++

            isDetecting = ((bytes[index].toInt() and 0x02) shr 1) == 1
            isScanning = ((bytes[index].toInt() and 0x04) shr 2) == 1

        }

        override fun toString(): String {
            return """
                RtBoParam
                spo2 : $spo2
                pr : $pr
                pi : $pi
                isDetecting : $isDetecting
                isScanning : $isScanning
            """.trimIndent()
        }

    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class BsResult constructor(var bytes: ByteArray) : Parcelable {
        var type: Int  // 结果类型
        var unit: Int  // 血糖单位
        var data: Int  // 血糖值

        // 结果类型
        // “0x4x” 血糖结果正常
        // “0x5x” 血糖结果偏低
        // “0x6x” 血糖结果偏高
        // 血糖单位
        // “0xX1”表示单位为mg/dL
        // “0xX2”表示单位为mmol/L
        init {
            var index = 0
            type = (bytes[index].toInt() and 0x80) shr 4
            unit = bytes[index].toInt() and 0x0F
            index++
            data = toUInt(bytes.copyOfRange(index, index+2))
        }
    }
    @Parcelize
    @ExperimentalUnsignedTypes
    class BsStatus constructor(var bytes: ByteArray) : Parcelable {
        var status: Int     // 状态
        var sw_ver: String  // 血糖软件版本号
        var hw_ver: String  // 血糖硬件版本号

        // status：测量状态，包括以下3种状态。
        // 0x00：测量结束，
        // 0x01：模块忙或测量正在进行中，
        // 0xFF：测量模块故障或未接入。
        init {
            var index = 0
            status = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            sw_ver = byteToPointHex(bytes[index])
            index++
            hw_ver = byteToPointHex(bytes[index])
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class BtResult constructor(var bytes: ByteArray) : Parcelable {
        var status: Int  // 状态
        var data: Int    // 体温值

        // 状态
        // 00：待机状态（开机默认）
        // 01：正在测量状态（跟踪）
        // 10：测量结束
        // 11：测量超时（暂定1分钟未出最终结果）
        init {
            var index = 0
            status = (bytes[index].toInt() and 0x60) shr 5
            index++
            data = ((bytes[index].toInt() and 0x0F) shl 8) + ((bytes[2].toUInt() and 0xFFu).toInt())
        }
    }
    @Parcelize
    @ExperimentalUnsignedTypes
    class BtStatus constructor(var bytes: ByteArray) : Parcelable {
        var status: Int     // 状态
        var sw_ver: String  // 体温软件版本号
        var hw_ver: String  // 体温硬件版本号

        // status：测量状态，包括以下3种状态。
        // 0x00：测量结束，
        // 0x01：模块忙或测量正在进行中，
        // 0xFF：测量模块故障或未接入。
        init {
            var index = 0
            status = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            sw_ver = byteToPointHex(bytes[index])
            index++
            hw_ver = byteToPointHex(bytes[index])
        }
    }

}
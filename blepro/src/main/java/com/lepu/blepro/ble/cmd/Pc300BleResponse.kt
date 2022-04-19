package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.ByteUtils.*
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import java.util.*

object Pc300BleResponse {

    @ExperimentalUnsignedTypes
    class BleResponse(val bytes: ByteArray) {
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

    @ExperimentalUnsignedTypes
    class DeviceInfo(val bytes: ByteArray) {
        var softwareV: String  // 软件版本
        var hardwareV: String  // 硬件版本
        var batLevel: Int      // 电量等级 级数越高，电量越多（0-3）

        init {
            var index = 0
            if (bytes.size == 4) {
                softwareV = "" + ((byte2UInt(bytes[index]) and 0xF0) shr 4) + "." + (byte2UInt(bytes[index]) and 0x0F) + "." + ((byte2UInt(bytes[index+1]) and 0xF0) shr 4) + "." + (byte2UInt(bytes[index+1]) and 0x0F)
                index += 2
            } else {
                softwareV = "" + ((byte2UInt(bytes[index]) and 0xF0) shr 4) + "." + (byte2UInt(bytes[index]) and 0x0F)
                index++
            }
            hardwareV = "" + ((byte2UInt(bytes[index]) and 0xF0) shr 4) + "." + (byte2UInt(bytes[index]) and 0x0F)
            index++
            batLevel = if (bytes.size < 3) {
                0
            } else {
                byte2UInt(bytes[index])
            }
        }

        override fun toString(): String {
            return """
                DeviceInfo
                softwareV : $softwareV
                hardwareV : $hardwareV
                batLevel : $batLevel
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class RtBpData(val bytes: ByteArray) {
        var psValue: Int   // 当前压力
        var sign: Boolean  // 心跳信息 true有心跳，false无心跳
        var mode: Int      // 血压模式（仅对KRK血压模块）
        init {
            psValue = ((byte2UInt(bytes[0]) and 0x0F) shl 8) + byte2UInt(bytes[1])
            sign = ((byte2UInt(bytes[0]) and 0x10) shr 4) == 1
            mode = (byte2UInt(bytes[0]) and 0xC0) shr 6
        }

        override fun toString(): String {
            return """
                RtBpData
                psValue : $psValue
                sign : $sign
                mode : $mode
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class BpResult(val bytes: ByteArray) {
        var sys: Int            // 收缩压
        var result: Int         // 心率结果（0：心率正常 1：心率不齐）
        var resultMess: String  // 心率结果
        var map: Int            // 平均压
        var dia: Int            // 舒张压
        var plus: Int           // 脉率

        init {
            var index = 0
            sys = ((byte2UInt(bytes[index]) and 0x7F) shl 8) + byte2UInt(bytes[index+1])
            result = (byte2UInt(bytes[index]) and 0x80) shr 7
            resultMess = if (result == 0) "心率正常" else "心率不齐"
            index += 2
            map = byte2UInt(bytes[index])
            index++
            dia = byte2UInt(bytes[index])
            index++
            plus = byte2UInt(bytes[index])
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

    @ExperimentalUnsignedTypes
    class BpResultError(val bytes: ByteArray) {
        var errorType: Int     // 错误编码类型 0(景新浩) 1(KRK血压)
        var errorNum: Int      // 错误编码号
        var errorMess: String  // 错误编码信息

        init {
            errorType = (byte2UInt(bytes[0]) and 0x80) shr 7
            errorNum = byte2UInt(bytes[0]) and 0x7F
            errorMess = getErrorMess(errorType, errorNum)
        }

        private fun getErrorMess(errorType: Int, errorNum: Int) : String {
            if (errorType == 0) {
                when(errorNum) {
                    1 -> return "7S内打气不上30mmHg(气袋没绑好)"
                    2 -> return "气袋压力超过295mmHg，进入超压保护"
                    3 -> return "测量不到有效的脉搏"
                    4 -> return "干预过多（测量中移动、说话等）"
                    5 -> return "测量结果数值有误"
                    6 -> return "漏气"
                    14 -> return "电量过低，暂停使用"
                }
            } else if (errorType == 1) {
                when(errorNum) {
                    1 -> return "7S内打气不上30mmHg(气袋没绑好)"
                    2 -> return "气袋压力超过295mmHg，进入超压保护"
                    3 -> return "测量不到有效的脉搏"
                    4 -> return "干预过多（测量中移动、说话等）"
                    5 -> return "测量结果数值有误"
                    6 -> return "漏气"
                    7 -> return "自检失败，可能是传感器或A/D采样出错"
                    8 -> return "气压错误，可能是阀门无法正常打开"
                    9 -> return "信号饱和，由于运动或其他原因使信号幅度太大"
                    10 -> return "在漏气检测中，发现系统气路漏气"
                    11 -> return "开机后，充气泵、A/D采样、压力传感器出错，或者软件运行中指针出错"
                    12 -> return "某次测量超过规定时间，成人袖带压超过200mmHg时为120秒，未超过时为90秒，新生儿为90秒"
                    14 -> return "电量过低，暂停使用"
                }
            }
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

    @ExperimentalUnsignedTypes
    class RtOxyWave(val bytes: ByteArray) {
        val waveData: ByteArray
        val waveIntData: IntArray
        init {
            waveData = bytes.copyOfRange(0, bytes.size).toList().asSequence().map { (it.toInt() and 0x7f).toByte() }.toList().toByteArray()
            waveIntData =  bytes.copyOfRange(0, bytes.size).toList().asSequence().map { (it.toInt() and 0x7f)}.toList().toIntArray()
        }
    }

    @ExperimentalUnsignedTypes
    class RtOxyParam(val bytes: ByteArray) {
        var spo2: Int                  // （0-100）0代表无效值
        var pr: Int                    // （0-511）0代表无效值
        var pi: Float                  // （0-255）0代表无效值
        var isProbeOff: Boolean        // 探头脱落，手指未接入
        var isPulseSearching: Boolean  // 脉搏检测

        init {
            var index = 0
            spo2 = byte2UInt(bytes[index])
            index++
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pi = byte2UInt(bytes[index]).div(10f)
            index++
            isProbeOff = ((byte2UInt(bytes[index]) and 0x02) shr 1) == 1
            isPulseSearching = ((byte2UInt(bytes[index]) and 0x04) shr 2) == 1
        }

        override fun toString(): String {
            return """
                RtOxyParam
                spo2 : $spo2
                pr : $pr
                pi : $pi
                isProbeOff : $isProbeOff
                isPulseSearching : $isPulseSearching
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class RtOxyState(val bytes: ByteArray) {
        var enableRtState: Boolean  // 上行使能发送状态
        var isProbeOff: Boolean     // 探头脱落 （手指未插入）
        var isCheckProbe: Boolean   // 检查探头 (探头故障或使用不当)

        init {
            enableRtState = ((byte2UInt(bytes[0]) and 0x20) shr 5) == 1
            isProbeOff = ((byte2UInt(bytes[0]) and 0x08) shr 3) == 1
            isCheckProbe = ((byte2UInt(bytes[0]) and 0x40) shr 2) == 1
        }

        override fun toString(): String {
            return """
                RtOxyState
                enableRtState : $enableRtState
                isProbeOff : $isProbeOff
                isCheckProbe : $isCheckProbe
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class TempResult(val bytes: ByteArray) {
        var state: Int           // 状态
        var isProbeOff: Boolean  // 探头脱落
        var temp: Float          // 体温值  T=30.00+(dat/100), 单位为摄氏度（C），相应的温度范围为30.00-43.00度（正常范围为32—43度，当其两字节为0x0520（1312）时表示被测量温度过高提醒用户停止测量并将感温棒移离被测物）

        // 状态
        // 00：待机状态（开机默认）
        // 01：正在测量状态（跟踪）
        // 10：测量结束
        // 11：测量超时（暂定1分钟未出最终结果）
        init {
            var index = 0
            state = (byte2UInt(bytes[index]) and 0x60) shr 5
            isProbeOff = ((byte2UInt(bytes[index]) and 0x10) shr 4) == 1
            temp = if (bytes.size > 2) {
                index++
                30+bytes2UIntBig(bytes[index], bytes[index + 1]).div(100f)
            } else {
                0f
            }
        }

        override fun toString(): String {
            return """
                TempResult
                state : $state
                isProbeOff : $isProbeOff
                temp : $temp
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class BsResult(val bytes: ByteArray) {
        var type: Int  // 结果类型
        var unit: Int  // 血糖单位
        var data: Int  // 血糖值

        // 结果类型
        // 0 血糖结果正常
        // 1 血糖结果偏低
        // 2 血糖结果偏高
        // 血糖单位
        // 1 单位为mg/dL
        // 0 单位为mmol/L
        init {
            var index = 0
            type = (byte2UInt(bytes[index]) and 0x30) shr 4
            unit = byte2UInt(bytes[index]) and 0x01
            index++
            data = if (unit == 1) {
                bytes2UIntBig(bytes[index], bytes[index+1])
            } else {
                bytesToHex(bytes.copyOfRange(index, index+2)).toInt().div(10)
            }
        }

        override fun toString(): String {
            return """
                type : $type
                unit : $unit
                data : $data
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class RtEcgState(val bytes: ByteArray) {
        var state: Int        // 测量状态 0待机中，1测量中
        var connect: Boolean  // ecg连接状态 false没连上，true已连接

        init {
            state = (byte2UInt(bytes[0]) and 0x80) shr 7
            connect = ((byte2UInt(bytes[0]) and 0x40) shr 6) == 1
        }

        override fun toString(): String {
            return """
                RtEcgState
                state : $state
                connect : $connect
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class RtEcgWave(val bytes: ByteArray) {
        var seqNo: Int
//        var digit: Int
//        var sign: Boolean
        var waveData: ByteArray
        var waveShortData: ShortArray  // 0—4095
        var wFs: FloatArray
        var isProbeOff: Boolean

        init {
            var index = 0
            seqNo = byte2UInt(bytes[index])
            index++
            waveData = bytes.copyOfRange(index, index+25*2)
            index += 50
            val len = waveData.size/2
            waveShortData = ShortArray(len)
            wFs = FloatArray(len)
            for (i in 0 until len) {
                waveShortData[i] = (((byte2UInt(bytes[i*2]) and 0x0F) shl 8) + byte2UInt(bytes[i*2+1])).toShort()
                wFs[i] = (waveShortData[i]-2048) * 1.div(394f)
            }
            isProbeOff = ((byte2UInt(bytes[index]) and 0x80) shr 7) == 1
        }

        override fun toString(): String {
            return """
                seqNo : $seqNo
                waveData : ${bytesToHex(waveData)}
                waveShortData : ${Arrays.toString(waveShortData)}
                wFs : ${Arrays.toString(wFs)}
                isProbeOff : $isProbeOff
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class RtEcgResult(val bytes: ByteArray) {
        var result: Int
        var resultMess: String
        var hr: Int

        init {
            var index = 0
            result = byte2UInt(bytes[index])
            resultMess = getMess(result)
            index++
            hr = byte2UInt(bytes[index+1])
        }

        fun getMess(result: Int): String {
            return when(result) {
                0x00 -> "波形未见异常"
                0x01 -> "波形疑似心跳稍快请注意休息"
                0x02 -> "波形疑似心跳过快请注意休息"
                0x03 -> "波形疑似阵发性心跳过快请咨询医生"
                0x04 -> "波形疑似心跳稍缓请注意休息"
                0x05 -> "波形疑似心跳过缓请注意休息"
                0x06 -> "波形疑似偶发心跳间期缩短请咨询医生"
                0x07 -> "波形疑似心跳间期不规则请咨询医生"
                0x08 -> "波形疑似心跳稍快伴有偶发心跳间期缩短请咨询医生"
                0x09 -> "波形疑似心跳稍缓伴有偶发心跳间期缩短请咨询医生"
                0x0A -> "波形疑似心跳稍缓伴有心跳间期不规则请咨询医生"
                0x0B -> "波形有漂移请重新测量"
                0x0C -> "波形疑似心跳过快伴有波形漂移请咨询医生"
                0x0D -> "波形疑似心跳过缓伴有波形漂移请咨询医生"
                0x0E -> "波形疑似偶发心跳间期缩短伴有波形漂移请咨询医生"
                0x0F -> "波形疑似心跳间期不规则伴有波形漂移请咨询医生"
                0xFF -> "信号较差请重新测量"
                else -> ""
            }
        }

        override fun toString(): String {
            return """
                RtEcgResult
                result : $result
                resultMess : $resultMess
                hr : $hr
            """.trimIndent()
        }
    }

}
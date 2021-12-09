package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.ble.data.PC80DataController
import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.HexString.trimStr
import kotlinx.android.parcel.Parcelize

object PC80BleResponse {

    @ExperimentalUnsignedTypes
    @Parcelize
    class PC80Response constructor(var bytes: ByteArray) : Parcelable {
        var cmd: Int
        var len: Int
        var content: ByteArray  // 数据域

        init {
            cmd = (bytes[1].toUInt() and 0xFFu).toInt()
            len = toUInt(bytes.copyOfRange(2, 3))
            content = bytes.copyOfRange(3, 3 + len)
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class DeviceInfo constructor(var bytes: ByteArray, var len: Int) : Parcelable {
        var softwareV: String   // 软件版本
        var hardwareV: String   // 硬件版本
        var algorithmV: String  // 算法版本
        var sn: String

        // 使用BCD码格式
        // 0xAB : Va.b
        // 0x12 : V1.2
        // 0x5,0x6,0x3,'B',0x0,0x1 : V5.6.3.B01 len=8  V5.6.3.0 len=6
        init {
            if (len == 8) {
                softwareV = "" + bytes[0].toInt() +
                        "." + bytes[1].toInt() +
                        "." + bytes[2].toInt() +
                        "." + toString(byteArrayOf(bytes[3])) + bytes[4].toInt() + bytes[5].toInt()
                hardwareV = bytesToHex(byteArrayOf(bytes[len-2]))
                algorithmV = bytesToHex(byteArrayOf(bytes[len-1]))
            } else {
                softwareV = "" + bytes[0].toInt() +
                        "." + bytes[1].toInt() +
                        "." + bytes[2].toInt() +
                        "." + bytes[3].toInt()
                hardwareV = bytesToHex(byteArrayOf(bytes[len-2]))
                algorithmV = bytesToHex(byteArrayOf(bytes[len-1]))
            }
            sn = "XBN00NK03553"
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class TransSet constructor(var bytes: ByteArray) : Parcelable {
        var deviceType: Int     // 设备型号
        var deviceName: String  // 设备型号字符串
        var filterMode: Int     // 滤波模式
        var transType: Int      // 传输类型
        var sn: String          // 产品序列号

        // 滤波模式 : 0普通, 1增强
        // 传输类型 : 0实时, 1非实时
        init {
            var index = 0
            deviceType = (bytes[index].toUInt() and 0xFFu).toInt()
            deviceName = getDeviceName(deviceType)
            index++
            filterMode = (bytes[index].toInt() and 0x80) shr 7
            transType = (bytes[index].toInt() and 0x01)
            index++
            sn = trimStr(toString(bytes.copyOfRange(index, index+12)))
            /*sn = "" + (bytes[2].toUInt() and 0xFFu).toInt() + "-" + (bytes[3].toUInt() and 0xFFu).toInt() + "-" + (bytes[4].toUInt() and 0xFFu).toInt() +
                    "-" + (bytes[5].toUInt() and 0xFFu).toInt() + "-" + (bytes[6].toUInt() and 0xFFu).toInt() + "-" + (bytes[7].toUInt() and 0xFFu).toInt() +
                    "-" + (bytes[8].toUInt() and 0xFFu).toInt() + "-" + (bytes[9].toUInt() and 0xFFu).toInt() + "-" + (bytes[10].toUInt() and 0xFFu).toInt() +
                    "-" + (bytes[11].toUInt() and 0xFFu).toInt() + "-" + (bytes[12].toUInt() and 0xFFu).toInt() + "-" + (bytes[13].toUInt() and 0xFFu).toInt()
            sn2 = toString(bytes.copyOfRange(index, index+12))
            sn3 = trimStr(toString(bytes.copyOfRange(index, index+12)))
            sn4 = bytesToHex(bytes.copyOfRange(index, index+12))*/
        }
    }
    fun getDeviceName(int: Int) : String {
        return when(int) {
            0x0A -> "PC-80A"
            0x0B -> "PC-80B"
            0x80 -> "PC-80B(UW)"
            else -> ""
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class RtTrackData constructor(var bytes: ByteArray) : Parcelable {
        var seqNo: Int     // 帧号
        var gain: Float    // 波形幅值系数
        var channel: Int   // 当前使用的通道
        var measure: Int   // 当前测量模式
        var stage: Int     // 当前测量阶段
        var leadOff: Int   // 电极脱落标记
        var dataType: Int  // ECG数据结构类型
        var data: RtData   // ECG数据

        // 帧号 : 0-255
        // 波形幅值系数 : x1/2(000)，x1(001)，x2(010)，x4(100)
        // 当前使用的通道 : 00：正在检测测量通道，01：内部通道测量，10：外部通道测量
        // 当前测量模式 : 00：正在检测测量模式，01：快速测量模式，10：连续测量模式
        // 当前测量阶段
        // 0000：正在检测通道，
        // 0001：正在准备测量，
        // 0010：测量进行中，
        // 0011：开始分析，
        // 0100：报告测量结果，
        // 0101：跟踪停止
        // 电极脱落标记 : 1代表当前电极脱落，0为不脱落
        // ECG数据结构类型
        // 0(Structure-0)：表示ECG数据部分为空值，即不包含任何有效的数据
        // 1(Structure-1)：表示ECG数据部分为25个ECG数据采样点的值
        // 2(Structure-2)：表示ECG数据部分为ECG测量结果
        init {
            seqNo = (bytes[0].toUInt() and 0xFFu).toInt()
            gain = getGain((bytes[2].toInt() and 0xF0) shr 4)
            channel = (bytes[3].toInt() and 0xC0) shr 6
            measure = (bytes[3].toInt() and 0x30) shr 4
            stage = bytes[3].toInt() and 0x0F
            leadOff = (bytes[5].toInt() and 0x80) shr 7
            dataType = bytes[5].toInt() and 0x07
            data = RtData(bytes.copyOfRange(6, bytes.size), dataType)
        }
    }
    @Parcelize
    @ExperimentalUnsignedTypes
    class RtData constructor(var bytes: ByteArray, var dataType: Int) : Parcelable {
        var ecgData: RtEcgData? = null  // ECG采样数据
        var result: RtResult? = null    // ECG测量结果
        init {
            if (dataType == 1) {
                ecgData = RtEcgData(bytes)
            } else if (dataType == 2) {
                result = RtResult(bytes)
            }
        }
    }
    @Parcelize
    @ExperimentalUnsignedTypes
    class RtEcgData constructor(var bytes: ByteArray) : Parcelable {
        var len: Int
        var ecg: ByteArray
        var wFs : FloatArray

        // ecg : 每个采样数据占两个字节，低字节在前，只有低12位是有效的ECG波形数据，采样点范围(0，4095)基线值2048，采样点对应的电压范围(0mV，3300mV)
        init {
            len = bytes.size
            ecg = bytes
            wFs = FloatArray(len/2)
            for (i in 0 until (len/2)) {
                wFs!![i] = PC80DataController.byteTomV(bytes[2 * i], bytes[2 * i + 1])
            }
        }
    }
    @Parcelize
    @ExperimentalUnsignedTypes
    class RtResult constructor(var bytes: ByteArray) : Parcelable {
        var year: String
        var month: String
        var day: String
        var hour: String
        var minute: String
        var second: String
        var hr: Int
        var result: Int
        var resultMess: String

        // 时间和日期使用BCD码格式，测量结果使用16进制
        init {
            var index = 0
            year = bytesToHex(bytes.copyOfRange(index, index + 2))
            index += 2
            month = bytesToHex(byteArrayOf(bytes[index]))
            index++
            day = bytesToHex(byteArrayOf(bytes[index]))
            index++
            hour = bytesToHex(byteArrayOf(bytes[index]))
            index++
            minute = bytesToHex(byteArrayOf(bytes[index]))
            index++
            second = bytesToHex(byteArrayOf(bytes[index]))
            index++
            hr = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            result = (bytes[index].toUInt() and 0xFFu).toInt()
            resultMess = getResult(result)
        }
    }
    fun getGain(int: Int) : Float {
        return when(int) {
            1 -> 1f
            2 -> 2f
            4 -> 4f
            else -> 0.5f
        }
    }
    fun getResult(int: Int) : String {
        when(int) {
            0 -> return "节律无异常"
            1 -> return "疑似心跳稍快，请注意休息"
            2 -> return "疑似心跳过快，请注意休息"
            3 -> return "疑似阵发性心跳过快 请咨询医生"
            4 -> return "疑似心跳稍缓 请注意休息"
            5 -> return "疑似心跳过缓 请注意休息"
            6 -> return "疑似心跳间期缩短 请咨询医生"
            7 -> return "疑似心跳间期不规则 请咨询医生"
            8 -> return "疑似心跳稍快伴有心跳间期缩短 请咨询医生"
            9 -> return "疑似心跳稍缓伴有心跳间期缩短 请咨询医生"
            10 -> return "疑似心跳稍缓伴有心跳间期不规则 请咨询医生"
            11 -> return "波形有漂移"
            12 -> return "疑似心跳过快伴有波形漂移 请咨询医生"
            13 -> return "疑似心跳过缓伴有波形漂移 请咨询医生"
            14 -> return "疑似心跳间期缩短伴有波形漂移 请咨询医生"
            15 -> return "疑似心跳间期不规则伴有波形漂移 请咨询医生"
            16 -> return "信号较差，请重新测量"
            else -> return ""
        }
    }

    class RtRecordData(val size: Int, var index: Int) {
        var seqNo: Int
        var fileSize: Int
        var content: ByteArray

        init {
            seqNo = 0
            fileSize = size
            content = ByteArray(size)
            index = index
        }

        fun addContent(bytes: ByteArray) {
            seqNo = (bytes[0].toUInt() and 0xFFu).toInt()
            var data = bytes.copyOfRange(1, bytes.size)
            if (index >= fileSize) {
                return // 已下载完成
            } else {
                System.arraycopy(data, 0, content, index, data.size)

                index += data.size
            }
            LepuBleLog.d("er1File, bytes size = ${bytes.size}, index = $index")
        }
    }

    const val CRCLen = 2
    const val Length = 4
    const val section0Len = 76
    const val section1Len = 32
    const val section2Len = 30
    const val section3Len = 28
    const val section6Len = 9024
    const val section9Len = (88+512)
    @Parcelize
    @ExperimentalUnsignedTypes
    class ScpEcgFile(var bytes: ByteArray) : Parcelable {
        var crc: Int            // 全部记录的校验CRC值
        var len: Int            // 全部记录的长度
        var section0: Section0  // 数据段指针
        var section1: Section1  // 头信息
        var section2: Section2  // Huffman表段
        var section3: Section3  // 导联信息段
        var section6: Section6  // 采样数据段
        var section9: Section9  // 自定义段

        init {
            crc = toUInt(bytes.copyOfRange(0, CRCLen))
            len = toUInt(bytes.copyOfRange(CRCLen, CRCLen + Length))
            section0 = Section0(bytes.copyOfRange(CRCLen + Length, CRCLen + Length + section0Len))
            section1 = Section1(bytes.copyOfRange(CRCLen + Length + section0Len, CRCLen + Length + section0Len + section1Len))
            section2 = Section2(bytes.copyOfRange(CRCLen + Length + section0Len + section1Len, CRCLen + Length + section0Len + section1Len + section2Len))
            section3 = Section3(bytes.copyOfRange(CRCLen + Length + section0Len + section1Len + section2Len, CRCLen + Length + section0Len + section1Len + section2Len + section3Len))
            section6 = Section6(bytes.copyOfRange(CRCLen + Length + section0Len + section1Len + section2Len + section3Len, CRCLen + Length + section0Len + section1Len + section2Len + section3Len + section6Len))
            section9 = Section9(bytes.copyOfRange(CRCLen + Length + section0Len + section1Len + section2Len + section3Len + section6Len, bytes.size))
        }
    }
    @Parcelize
    @ExperimentalUnsignedTypes
    class Section0(var bytes: ByteArray) : Parcelable {
        var crc: Int          // 整段的检验CRC值
        var sectionId: Int    // 段号
        var sectionLen: Int   // 整段数据长
        var sectionV: Int     // 段版本号
        var protocolV: Int    // 协议版本号
        var reserved: String  // 'S','C','P','E','C','G'

        init {
            var index = 0
            crc = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            sectionId = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            sectionLen = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            sectionV = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            protocolV = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            reserved = toString(bytes.copyOfRange(index, index+6))
        }
    }
    @Parcelize
    @ExperimentalUnsignedTypes
    class Section1(var bytes: ByteArray) : Parcelable {
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var second: Int

        init {
            year = toUInt(bytes.copyOfRange(19, 21))
            month = (bytes[21].toUInt() and 0xFFu).toInt()
            day = (bytes[22].toUInt() and 0xFFu).toInt()
            hour = (bytes[26].toUInt() and 0xFFu).toInt()
            minute = (bytes[27].toUInt() and 0xFFu).toInt()
            second = (bytes[28].toUInt() and 0xFFu).toInt()
        }
    }
    @Parcelize
    @ExperimentalUnsignedTypes
    class Section2(var bytes: ByteArray) : Parcelable {
    }
    @Parcelize
    @ExperimentalUnsignedTypes
    class Section3(var bytes: ByteArray) : Parcelable {
    }
    @Parcelize
    @ExperimentalUnsignedTypes
    class Section6(var bytes: ByteArray) : Parcelable {
        var avm: Int            // 1幅值单位 = ? nv 即(3300000/4096=805)
        var si: Int             // 采样间隔(6666)us 即150Hz
        var du: Int             // 差分类型(0)
        var bimodal: Int        // 是否双峰压缩(0)
        var leadLen: Int        // 1导联(9000) 3导联(27000)
        var ecgData: RtEcgData  // ECG数据

        init {
            var index = 16
            avm = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            si = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            du = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            bimodal = (bytes[index].toUInt() and 0xFFu).toInt()
            index++
            leadLen = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            ecgData = RtEcgData(bytes.copyOfRange(index, bytes.size))
        }
    }
    @Parcelize
    @ExperimentalUnsignedTypes
    class Section9(var bytes: ByteArray) : Parcelable {
        var hr: Int             // 平均HR
        var gain: Float         // 增益
        var result: Int         // 分析结果
        var resultMess: String  // 分析结果提示
        var filterMode: Int     // 滤波模式

        init {
            hr = toUInt(bytes.copyOfRange(19, 21))
            gain = getGain((bytes[24].toUInt() and 0xFFu).toInt())
            result = (bytes[28].toUInt() and 0xFFu).toInt()
            resultMess = getResult(result)
            filterMode = (bytes[87].toUInt() and 0xFFu).toInt()
        }
    }

}
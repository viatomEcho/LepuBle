package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.ByteUtils
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.toUInt

object Lew3BleResponse {

    @ExperimentalUnsignedTypes
    class BleResponse(val bytes: ByteArray) {
        var cmd: Int
        var pkgType: Byte
        var pkgNo: Int
        var len: Int
        var content: ByteArray

        init {
            cmd = (bytes[1].toUInt() and 0xFFu).toInt()
            pkgType = bytes[3]
            pkgNo = (bytes[4].toUInt() and 0xFFu).toInt()
            len = toUInt(bytes.copyOfRange(5, 7))
            content = bytes.copyOfRange(7, 7 + len)
        }
    }

    @ExperimentalUnsignedTypes
    class RtData(val bytes: ByteArray) {
        var param: RtParam
        var wave: RtWave

        init {
            var index = 0
            param = RtParam(bytes.copyOfRange(index, index+20))
            index += 20
            wave = RtWave(bytes.copyOfRange(index, bytes.size))
        }
    }

    @ExperimentalUnsignedTypes
    class RtParam(val bytes: ByteArray) {
        var hr: Int               // 实时心率bpm
        var isrFlag: Boolean      // R波标记（有无检测到R波）
        var batteryState: Int     // 电池状态（0:正常使用 1:充电中 2:充满 3:低电量）
        var batteryPercent: Int   // 电池电量（单位%）
        var recordTime: Int = 0   // 记录时长（单位s）
        var runStatus: Int        // 运行状态（0：空闲待机，导联脱落 1：测量准备 2：记录中 3：分析存储中 4：已存储成功 5：记录小于30s 7：导联断开）
        var runStatusMsg: String  // 运行状态
        // reserved 11

        init {
            var index = 0
            hr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            isrFlag = (byte2UInt(bytes[index]) and 0x01) == 1
            batteryState = (byte2UInt(bytes[index]) and 0xC0) ushr 5
            index++
            batteryPercent = byte2UInt(bytes[index])
            index++
            recordTime = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            runStatus = byte2UInt(bytes[index])
            runStatusMsg = getStatusMsg(runStatus)
        }

        private fun getStatusMsg(status: Int): String {
            return when (status) {
                0 -> "空闲待机(导联脱落)"
                1 -> "测量准备(主机丢弃前段波形阶段)"
                2 -> "记录中"
                3 -> "分析存储中"
                4 -> "已存储成功(满时间测量结束后一直停留此状态直到回空闲状态)"
                5 -> "记录小于30s(记录中状态直接切换至此状态)"
                6 -> "重测已达6次，进入待机"
                7 -> "导联断开"
                else -> ""
            }
        }

        override fun toString(): String {
            return """
                hr : $hr
                isrFlag : $isrFlag
                batteryState : $batteryState
                batteryPercent : $batteryPercent
                recordTime : $recordTime
                runStatus : $runStatus
            """.trimIndent()
        }
    }
    @ExperimentalUnsignedTypes
    class RtWave(val bytes: ByteArray) {
        var samplingNum: Int     // 采样点数
        var waveData: ByteArray  // 原始数据
        var wFs: FloatArray      // 毫伏值数据

        init {
            var index = 0
            samplingNum = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            waveData = bytes.copyOfRange(index, bytes.size)

            if (samplingNum != waveData.size.div(2)) {
                samplingNum = waveData.size.div(2)
            }

            wFs = FloatArray(samplingNum)
            for (i in 0 until samplingNum) {
                wFs[i] = byteTomV(waveData[2 * i], waveData[2 * i + 1])
            }
        }

        private fun byteTomV(a: Byte, b: Byte): Float {
            if (a == 0xFF.toByte() && b == 0x7F.toByte()) {
                return 0f
            }
            val n = ByteUtils.toSignedShort(a, b)
            return n * 1.div(345f)
        }
    }

    @ExperimentalUnsignedTypes
    class EcgFile(val model: Int, val name: String, val size: Int) {
        var fileName: String
        var fileSize: Int
        var content: ByteArray
        var index: Int // 标识当前下载index

        init {
            fileName = name
            fileSize = size
            content = ByteArray(size)
            index = 0
        }

        fun addContent(bytes: ByteArray) {
            if (index >= fileSize) {
                return // 已下载完成
            } else {
                System.arraycopy(bytes, 0, content, index, bytes.size)

                index += bytes.size
            }
            LepuBleLog.d("LeW3File,bytes size = ${bytes.size}, index = $index")
        }
    }

    @ExperimentalUnsignedTypes
    class FileList(val bytes: ByteArray) {
        var size: Int
        var list = mutableListOf<String>()

        init {
            size = (bytes.size-10).div(44)
            for (i in 0 until size) {
                val fileName = getFileName(bytes.copyOfRange(10+i*44, 10+(i+1)*44))
                list.add(fileName)
            }
        }

        private fun getFileName(bytes: ByteArray): String {
            val year = toUInt(bytes.copyOfRange(0, 2))
            val month = byte2UInt(bytes[2])
            val day = byte2UInt(bytes[3])
            val hour = byte2UInt(bytes[4])
            val min = byte2UInt(bytes[5])
            val second = byte2UInt(bytes[6])
            var mon = ""+month
            if (month<10) {
                mon = "0$month"
            }
            var d = ""+day
            if (day<10) {
                d = "0$day"
            }
            var h = ""+hour
            if (hour<10) {
                h = "0$hour"
            }
            var m = ""+min
            if (min<10) {
                m = "0$min"
            }
            var sec = ""+second
            if (second<10) {
                sec = "0$second"
            }
            return "R$year$mon$d$h$m$sec"
        }

        override fun toString(): String {
            return """
                size : $size
                list : $list
            """.trimIndent()
        }
    }

}
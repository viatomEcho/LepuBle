package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import kotlinx.android.parcel.Parcelize

object Bpw1BleResponse {

    @ExperimentalUnsignedTypes
    @Parcelize
    class Bpw1Response constructor(var bytes: ByteArray) : Parcelable {
        var head: Byte          // 数据头
        var type: Int           // 设备类型
        var len: Int            // 长度
        var content: ByteArray  // 内容

        init {
            head = bytes[0]
            type = (bytes[1].toUInt() and 0xFFu).toInt()
            len = (bytes[2].toUInt() and 0xFFu).toInt()
            content = bytes.copyOfRange(3, bytes.size-1)
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class DeviceInfo constructor(var bytes: ByteArray) : Parcelable {
        var battery: Int
        var timingSwitch: Boolean
        var fileListSize: Int

        init {
            battery = (bytes[0].toUInt() and 0xFFu).toInt()
            timingSwitch = ((bytes[1].toInt() and 0xF0) shr 4) != 0
            fileListSize = (bytes[4].toUInt() and 0xFFu).toInt()
        }

        override fun toString(): String {
            val string = """
            DeviceInfo 
            battery: $battery 
            timingSwitch: $timingSwitch 
            fileListSize: $fileListSize
            """.trimIndent()
            return string
        }
    }
    @ExperimentalUnsignedTypes
    @Parcelize
    class MeasureTime constructor(var bytes: ByteArray) : Parcelable {
        var startHH: Int
        var startMM: Int
        var stopHH: Int
        var stopMM: Int
        var serialNum: Int
        var totalNum: Int
        var interval: Int

        init {
            startHH = (bytes[0].toUInt() and 0xFFu).toInt()
            startMM = (bytes[1].toUInt() and 0xFFu).toInt()
            stopHH = (bytes[2].toUInt() and 0xFFu).toInt()
            stopMM = (bytes[3].toUInt() and 0xFFu).toInt()
            serialNum = (bytes[4].toInt() and 0xF0) shr 4
            totalNum = (bytes[4].toInt() and 0x0F)
            interval = (bytes[5].toUInt() and 0xFFu).toInt()
        }

        override fun toString(): String {
            val string = """
            MeasureTime 
            serialNum: $serialNum 
            totalNum: $totalNum
            time: $startHH : $startMM -  $stopHH : $stopMM
            """.trimIndent()
            return string
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class RtData constructor(var bytes: ByteArray) : Parcelable {
        var pressure: Int
        init {
            pressure = (bytes[1].toUInt() and 0xFFu).toInt()
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class BpData constructor(var bytes: ByteArray) : Parcelable {
        var year: String
        var month: String
        var day: String
        var hour: String
        var minute: String
        var sys: Int
        var dia: Int
        var pul: Int
        var fg: Int
        init {
            year = (bytes[0].toUInt() and 0xFFu).toInt().toString()
            month = (bytes[1].toUInt() and 0xFFu).toInt().toString()
            day = (bytes[2].toUInt() and 0xFFu).toInt().toString()
            hour = (bytes[3].toUInt() and 0xFFu).toInt().toString()
            minute = (bytes[4].toUInt() and 0xFFu).toInt().toString()
            sys = (byte2UInt(bytes[6]) and 0xFF) or (byte2UInt(bytes[5]) and 0xFF shl 8)
            dia = (bytes[7].toUInt() and 0xFFu).toInt()
            pul = (bytes[8].toUInt() and 0xFFu).toInt()
            fg = (bytes[9].toUInt() and 0xFFu).toInt()
        }

        override fun toString(): String {
            val string = """
            BpData 
            date: $year $month $day $hour $minute 
            sys: $sys 
            dia: $dia 
            pul: $pul 
            fg: $fg 
            """.trimIndent()
            return string
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class ErrorResult constructor(var bytes: ByteArray) : Parcelable {
        var type: Int
        var result: String

        init {
            type = (bytes[1].toUInt() and 0xFFu).toInt()
            result = getResult(type)
        }
    }

    fun getResult(type: Int): String {
        return when(type) {
            0 -> "设备错误"
            1 -> "检测不到脉搏"
            2 -> "请保持安静"
            3 -> "佩带过紧"
            4 -> "佩带过松"
            else -> ""
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class Bpw1FileList constructor(val size: Int) : Parcelable {
        var listSize: Int
        var fileList: Array<BpData?>
        var index: Int // 标识当前下载index

        init {
            listSize = size
            fileList = arrayOfNulls(listSize)
            index = 0
        }

        fun addFile(data: BpData) {
            if (index >= listSize) {
                return // 已下载完成
            } else {
                fileList[index] = data
                index++
            }
            LepuBleLog.d("Bpw1FileList, size = ${size}, index = $index")
        }

        override fun toString(): String {
            var temp = ""
            for (file in fileList)
                temp += "date: " + file?.year + " " + file?.month +  " "  + file?.day + " " + file?.hour +  " "  + file?.minute + " sys: " + file?.sys + " dia: " + file?.dia + " pul: " + file?.pul + " fg: " + file?.fg + "\n"
            val string = """
            Bpw1FileList 
            size: $listSize
            $temp
        """.trimIndent()
            return string
        }
    }

}
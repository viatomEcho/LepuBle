package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.bytes2UIntBig

object Aoj20aBleResponse {

    @ExperimentalUnsignedTypes
    class BleResponse (val bytes: ByteArray) {
        var head: Int
        var deviceType: Int
        var cmd: Int
        var len: Int
        var content: ByteArray  // 内容

        init {
            var index = 0
            head = byte2UInt(bytes[index])
            index++
            deviceType = byte2UInt(bytes[index])
            index++
            cmd = byte2UInt(bytes[index])
            index++
            len = byte2UInt(bytes[index])
            if (cmd == Aoj20aBleCmd.MSG_TEMP_MEASURE) {
                len = 3
            } else if (cmd == Aoj20aBleCmd.MSG_ERROR_CODE) {
                len = 1
            }
            index++
            content = bytes.copyOfRange(index, index+len)
        }
    }

    @ExperimentalUnsignedTypes
    class TempRtData (val bytes: ByteArray) {
        var temp: Float      // 测温数据
        var mode: Int        // 测温模式
        var modeMsg: String

        init {
            var index = 0
            temp = bytes2UIntBig(bytes[index], bytes[index+1]).div(100f)
            index += 2
            mode = byte2UInt(bytes[index])
            modeMsg = getModeMsg(mode)
        }

        override fun toString(): String {
            return """
                temp : $temp
                mode : $mode
                modeMsg : $modeMsg
            """.trimIndent()
        }
    }

    private fun getModeMsg(mode: Int): String {
        return when(mode) {
            0x01 -> "Adult frontal temperature"
            0x02 -> "Children's frontal temperature"
            0x03 -> "Ear temperature"
            0x04 -> "Material temperature"
            else -> ""
        }
    }

    @ExperimentalUnsignedTypes
    class TempRecord(val bytes: ByteArray) {
        var num: Int     // 历史数据序号
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var temp: Float  // 测温数据 ℃

        init {
            var index = 0
            num = byte2UInt(bytes[index])
            index++
            year = byte2UInt(bytes[index]) + 2000
            index++
            month = byte2UInt(bytes[index])
            index++
            day = byte2UInt(bytes[index])
            index++
            hour = byte2UInt(bytes[index])
            index++
            minute = byte2UInt(bytes[index])
            index++
            temp = bytes2UIntBig(bytes[index], bytes[index+1]).div(100f)
        }

        override fun toString(): String {
            return """
                num : $num
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
                temp : $temp
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class DeviceData(val bytes: ByteArray) {
        var mode: Int           // 测温模式
        var modeMsg: String
        var battery: Int        // 电量数值 1-10 (10:100%)
        var version: Int        // 主机版本号
        var versionMsg: String

        init {
            var index = 0
            mode = byte2UInt(bytes[index])
            modeMsg = getModeMsg(mode)
            index++
            battery = (byte2UInt(bytes[index]) and 0xF0) shr 4
            index++
            version = byte2UInt(bytes[index])
            versionMsg = getVersionMsg(version.toString())
        }

        private fun getVersionMsg(mode: String): String {
            var v = ""
            for (i in mode.indices) {
                v += if (i == mode.length-1) {
                    mode[i]
                } else {
                    mode[i]+"."
                }
            }
            return v
        }

        override fun toString(): String {
            return """
                mode : $mode
                modeMsg : $modeMsg
                battery : $battery
                version : $version
                versionMsg : $versionMsg
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class ErrorMsg(val bytes: ByteArray) {
        var code: Int
        var codeMsg: String

        init {
            code = byte2UInt(bytes[0])
            codeMsg = getCodeMsg(code)
        }

        private fun getCodeMsg(code: Int): String {
            return when(code) {
                0xe1 -> "Ambient temperature > 40℃ or < 10℃"
                0xe2 -> "Material temperature < 0℃"
                0xe3 -> "Material temperature > 100℃"
                0xe4 -> "Human body temperature < 32℃"
                0xe5 -> "Human body temperature > 42.9℃"
                else -> ""
            }
        }

        override fun toString(): String {
            return """
                code : $code
                codeMsg : $codeMsg
            """.trimIndent()
        }
    }

}
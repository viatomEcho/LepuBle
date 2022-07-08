package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex

object LemBleResponse {

    @ExperimentalUnsignedTypes
    class BleResponse(val bytes: ByteArray) {
        var cmd: Int
        var len: Int
        var content: ByteArray  // 内容

        init {
            cmd = byte2UInt(bytes[5])
            len = byte2UInt(bytes[6])
            content = bytes.copyOfRange(7, 7+len)
        }
    }

    @ExperimentalUnsignedTypes
    class DeviceInfo(val bytes: ByteArray) {
//        var deviceSwitch: Boolean
        var battery: Int             // 1-100%
        var heatMode: Boolean        // 恒温加热模式开关
        var massageMode: Int         // 按摩模式 0：活力，1：动感，2：捶击，3：舒缓，4：自动
        var massageModeMess: String
        var massageLevel: Int        // 按摩力度挡位 1-15
        var massageTime: Int         // 按摩时间 0：15min，1：10min，2：5min
        var massageTimeMess: String

        init {
            var index = 0
//            deviceSwitch = byte2UInt(bytes[index]) == 1
            index++
            battery = byte2UInt(bytes[index])
            index++
            heatMode = byte2UInt(bytes[index]) == 1
            index++
            massageMode = byte2UInt(bytes[index])
            massageModeMess = getModeMess(massageMode)
            index++
            massageLevel = byte2UInt(bytes[index])
            index++
            massageTime = byte2UInt(bytes[index])
            massageTimeMess = getTimeMess(massageTime)
        }

        private fun getModeMess(mode: Int): String {
            return when (mode) {
                LemBleCmd.MassageMode.VITALITY -> "活力模式"
                LemBleCmd.MassageMode.DYNAMIC -> "动感模式"
                LemBleCmd.MassageMode.HAMMERING -> "捶击模式"
                LemBleCmd.MassageMode.SOOTHING -> "舒缓模式"
                LemBleCmd.MassageMode.AUTOMATIC -> "自动模式"
                else -> ""
            }
        }
        private fun getTimeMess(mode: Int): String {
            return when (mode) {
                LemBleCmd.MassageTime.MIN_15 -> "15min"
                LemBleCmd.MassageTime.MIN_10 -> "10min"
                LemBleCmd.MassageTime.MIN_5 -> "5min"
                else -> ""
            }
        }

        override fun toString(): String {
            return """
                DeviceInfo : 
                bytes : ${bytesToHex(bytes)}
                battery : $battery
                heatMode : $heatMode
                massageMode : $massageMode
                massageModeMess : $massageModeMess
                massageLevel : $massageLevel
                massageTime : $massageTime
                massageTimeMess : $massageTimeMess
            """.trimIndent()
        }
    }

}
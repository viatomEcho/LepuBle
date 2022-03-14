package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.bytes2UIntBig

object Vtm20fBleResponse {

    const val RT_PARAM = 0x55
    const val RT_WAVE= 0x56

    @ExperimentalUnsignedTypes
    class BleResponse (val bytes: ByteArray) {
        var head: Int
        var len: Int
        var cmd: Int
        var content: ByteArray  // 内容

        init {
            var index = 0
            head = byte2UInt(bytes[index])
            index++
            len = byte2UInt(bytes[index])
            index++
            cmd = byte2UInt(bytes[index])
            index++
            content = bytes.copyOfRange(index, bytes.size - 1)
        }
    }

    @ExperimentalUnsignedTypes
    class RtParam (val bytes: ByteArray) {
        var pr: Int     // 脉率数据（0-300，0x1FF为无效值）
        var spo2: Int   // 血氧值（0-100，0x7F为无效值）
        var pi: Float   // 灌注指数（0-20）
        var seqNo: Int  // 包序号（0-255）

        init {
            var index = 0
            pr = bytes2UIntBig(bytes[index], bytes[index+1])
            index += 2
            spo2 = byte2UInt(bytes[index])
            index++
            pi = bytes2UIntBig(bytes[index], bytes[index+1]).div(1000f)
            index +=2
            seqNo = byte2UInt(bytes[index])
        }

        override fun toString(): String {
            return """
                pr : $pr
                spo2 : $spo2
                pi : $pi
                seqNo : $seqNo
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class RtWave (val bytes: ByteArray) {
        var wave: Int             // 脉搏波
        var pulseSound: Boolean   // 脉搏波音标记（true：表征有脉搏音 false：表征无脉搏音）
        var isSensorOff: Boolean  // 导连脱落标志（true：表征导连脱落 false：表征导连链接正常）
        var isDisturb: Boolean    // 状态标记（true：表示运动干扰 false：表示状态正常）
        var isLowPi: Boolean      // 灌注标记（true：表示低灌注 false：表示正常灌注）
        var barChart: Int         // 棒图（0-15）
        var seqNo: Int            // 包序号（0-255）

        init {
            var index = 0
            wave = byte2UInt(bytes[index])
            index++
            pulseSound = (byte2UInt(bytes[index]) and 0x01) == 1
            isSensorOff = ((byte2UInt(bytes[index]) and 0x02) shr 1) == 1
            isDisturb = ((byte2UInt(bytes[index]) and 0x04) shr 2) == 1
            isLowPi = ((byte2UInt(bytes[index]) and 0x08) shr 3) == 1
            index++
            barChart = byte2UInt(bytes[index])
            index++
            seqNo = byte2UInt(bytes[index])
        }

        override fun toString(): String {
            return """
                wave : $wave
                pulseSound : $pulseSound
                isSensorOff : $isSensorOff
                isDisturb : $isDisturb
                isLowPi : $isLowPi
                barChart : $barChart
                seqNo : $seqNo
            """.trimIndent()
        }
    }

}
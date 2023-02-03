package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt

object Vtm01BleResponse {

    @ExperimentalUnsignedTypes
    class BleResponse (val bytes: ByteArray) {
        var cmd: Int
        var type: Int
        var len: Int
        var content: ByteArray

        init {
            cmd = byte2UInt(bytes[1])
            type = byte2UInt(bytes[3])
            len = toUInt(bytes.copyOfRange(5, 7))
            content = if (len <= 0) ByteArray(0) else bytes.copyOfRange(7, 7 + len)
        }
    }

    @ExperimentalUnsignedTypes
    class RtParam (val bytes: ByteArray) {
        var spo2: Int   // 血氧值（77-99）
        var pr: Int     // 脉率数据（30-250）
        var pi: Float   // 灌注指数（0- 200 e.g. 25 : PI = 2.5）
        var probeState: Int  // 探头状态 0:未检测到手指 1:正常测量 2:探头故障
        // reversed 7

        init {
            var index = 0
            spo2 = byte2UInt(bytes[index])
            index++
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pi = byte2UInt(bytes[index]).div(10f)
            index ++
            probeState = byte2UInt(bytes[index])
        }

        override fun toString(): String {
            return """
                RtParam : 
                spo2 : $spo2
                pr : $pr
                pi : $pi
                probeState : $probeState
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class RtData (val bytes: ByteArray) {
        var param: RtParam     // 参数，12字节
        var len: Int           // 波形数据长度
        var wave: ByteArray    // 0-200分辨率(125Hz)值为255时表示脉搏音标记
        var waveInt: IntArray  //

        init {
            var index = 0
            param = RtParam(bytes.copyOfRange(index, index+12))
            index += 12
            len = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            wave = bytes.copyOfRange(index, index+len)
            waveInt = IntArray(len)
            for (i in wave.indices) {
                var temp = byte2UInt(wave[i])
                // 处理毛刺
                if (temp == 255) {
                    if (i==0) {
                        if ((i+1) < len)
                            temp = byte2UInt(wave[i+1])
                    } else if (i == len-1) {
                        temp = byte2UInt(wave[i-1])
                    } else {
                        if ((i+1) < len)
                            temp = (byte2UInt(wave[i-1]) + byte2UInt(wave[i+1]))/2
                    }
                }
                waveInt[i] = temp
            }
        }

        override fun toString(): String {
            return """
                RtData : 
                param : $param
                len : $len
                wave : ${bytesToHex(wave)}
                waveInt : ${waveInt.joinToString(",")}
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class OriginalData (val bytes: ByteArray) {
        var len: Int
        var irRedBytes: ByteArray
        var irRedDatas = mutableListOf<IrRedData>()
        var irByteArray = mutableListOf<ByteArray>()
        var irIntArray: IntArray
        var redByteArray = mutableListOf<ByteArray>()
        var redIntArray: IntArray

        init {
            var index = 0
            len = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            irRedBytes = bytes.copyOfRange(index, bytes.size)
            irIntArray = IntArray(len)
            redIntArray = IntArray(len)
            for (i in 0 until len) {
                IrRedData(bytes.copyOfRange(index, index+8)).let {
                    irRedDatas.add(it)
                    irByteArray.add(it.irBytes)
                    irIntArray[i] = it.ir
                    redByteArray.add(it.redBytes)
                    redIntArray[i] = it.red
                }
                index += 8
            }
        }

        override fun toString(): String {
            return """
                OriginalData :
                len : $len
                irRedBytes : ${bytesToHex(irRedBytes)}
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class IrRedData (val bytes: ByteArray) {
        var ir: Int
        var irBytes: ByteArray
        var red: Int
        var redBytes: ByteArray
        init {
            var index = 0
            ir = toUInt(bytes.copyOfRange(index, index+4))
            irBytes = bytes.copyOfRange(index, index+4)
            index += 4
            red = toUInt(bytes.copyOfRange(index, index+4))
            redBytes = bytes.copyOfRange(index, index+4)
        }
    }

}
package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.toSignedShort
import java.util.*

object OxyIIBleResponse {

    class RtParam(val bytes: ByteArray) {
        var duration: Int        // 记录时长 单位秒
        var runStatus: Int       // 运行状态 0：准备阶段 1：测量准备阶段 2：测量中 3：测量结束
        var sensorState: Int     // 传感器状态 0：正常状态，1：导联脱落，未放手指，2：探头拔出，3：传感器或探头故障
        var spo2: Int
        var pi: Float
        var pr: Int
        var flag: Int            // 标志参数 bit0：脉搏音标志
        var motion: Int          // 体动
        var batteryState: Int    // 电池状态 0：正常使用，1：充电中，2：充满，3：低电量
        var batteryPercent: Int  // 电池电量百分比
        // reserved 6
        init {
            var index = 0
            duration = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            runStatus = byte2UInt(bytes[index])
            index++
            sensorState = byte2UInt(bytes[index])
            index++
            spo2 = byte2UInt(bytes[index])
            index++
            pi = byte2UInt(bytes[index]).div(10f)
            index++
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            flag = byte2UInt(bytes[index])
            index++
            motion = byte2UInt(bytes[index])
            index++
            batteryState = byte2UInt(bytes[index])
            index++
            batteryPercent = byte2UInt(bytes[index])
        }
        override fun toString(): String {
            return """
                RtParam :
                duration : $duration
                runStatus : $runStatus
                sensorState : $sensorState
                spo2 : $spo2
                pi : $pi
                pr : $pr
                flag : $flag
                motion : $motion
                batteryState : $batteryState
                batteryPercent : $batteryPercent
            """.trimIndent()
        }
    }

    class RtWave(val bytes: ByteArray) {
        var offset: Int
        var size: Int
        var wave: ByteArray
        var waveInt: IntArray
        init {
            var index = 0
            offset = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            size = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            wave = bytes.copyOfRange(index, index+size)
            waveInt = IntArray(size)
            for (i in 0 until size) {
                var temp = byte2UInt(wave[i])
                // 脉搏音标记-100
                if (temp == 156) {
                    if (i==0) {
                        if ((i+1) < size)
                            temp = byte2UInt(wave[i+1])
                    } else if (i == size-1) {
                        temp = byte2UInt(wave[i-1])
                    } else {
                        if ((i+1) < size)
                            temp = (byte2UInt(wave[i-1]) + byte2UInt(wave[i+1]))/2
                    }
                }
                waveInt[i] = temp
            }
        }
        override fun toString(): String {
            return """
                RtWave : 
                offset : $offset
                size : $size
                wave : ${bytesToHex(wave)}
                waveInt : ${waveInt.joinToString(",")}
            """.trimIndent()
        }
    }

    class RtData(val bytes: ByteArray) {
        var param: RtParam
        var wave: RtWave
        init {
            var index = 0
            param = RtParam(bytes.copyOfRange(index, index+20))
            index += 20
            wave = RtWave(bytes.copyOfRange(index, bytes.size))
        }
        override fun toString(): String {
            return """
                RtData : 
                param : $param
                wave : $wave
            """.trimIndent()
        }
    }

    class RtPpg(val bytes: ByteArray) {
        var size: Int
        var irArray = mutableListOf<Short>()
        var redArray = mutableListOf<Short>()
        var motionArray = mutableListOf<Int>()
        init {
            var index = 0
            size = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            for (i in 0 until size) {
                irArray.add(toSignedShort(bytes[index], bytes[index+1]))
                index += 2
                redArray.add(toSignedShort(bytes[index], bytes[index+1]))
                index += 2
                motionArray.add(byte2UInt(bytes[index]))
                index++
            }
        }
        override fun toString(): String {
            return """
                RtPpg : 
                size : $size
                irArray : ${irArray.joinToString(",")}
                redArray : ${redArray.joinToString(",")}
                motionArray : ${motionArray.joinToString(",")}
            """.trimIndent()
        }
    }

    class FileList(val type: Int, val bytes: ByteArray) {
        var size: Int
        var fileNames = mutableListOf<String>()
        init {
            var index = 0
            size = byte2UInt(bytes[index])
            index++
            for (i in 0 until size) {
                fileNames.add(HexString.trimStr(String(bytes.copyOfRange(index, index + 16))))
                index += 16
            }
        }
        override fun toString(): String {
            return """
                FileList : 
                size : $size
                fileNames : $fileNames
            """.trimIndent()
        }
    }

    class BleFile(val fileType: Int, val content: ByteArray) {

    }
}
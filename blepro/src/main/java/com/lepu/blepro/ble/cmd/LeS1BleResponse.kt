package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.ble.data.Er1DataController
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.bytes2UIntBig
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import kotlinx.android.parcel.Parcelize
import java.util.*

object LeS1BleResponse {

    
    @Parcelize
    class BleResponse constructor(var bytes: ByteArray) : Parcelable {
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


    @Parcelize
    class RtData constructor(var bytes: ByteArray) : Parcelable {
        var param: RtParam
        var scaleData: ScaleData
        var wave: RtWave

        init {
            var index = 0
            param = RtParam(bytes.copyOfRange(index, index+16))
            index += 16
            scaleData = ScaleData(bytes.copyOfRange(index, index+11))
            index += 11
            wave = RtWave(bytes.copyOfRange(index, bytes.size))
        }

        override fun toString(): String {
            return """
                RtData : 
                bytes : ${bytesToHex(bytes)}
                param : $param
                scaleData : $scaleData
                wave : $wave
            """.trimIndent()
        }
    }

    @Parcelize
    class RtParam constructor(var bytes: ByteArray) : Parcelable {
        var runStatus: Int
        var hr: Int
        var recordTime: Int
        var leadOff: Boolean
        var bleStatus: Int
        // reserve 7

        init {
            var index = 0
            runStatus = byte2UInt(bytes[index])
            index++
            hr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            recordTime = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            leadOff = byte2UInt(bytes[index]) == 0
            index++
            bleStatus = byte2UInt(bytes[index])
        }

        override fun toString(): String {
            return """
                RtParam :
                runStatus : $runStatus
                hr : $hr
                recordTime : $recordTime
                leadOff : $leadOff
                bleStatus : $bleStatus
            """.trimIndent()
        }

    }

    @Parcelize
    class ScaleData constructor(var bytes: ByteArray) : Parcelable {
        var stable: Boolean
        var unit: Int
        var precision: Int
        var weight: Float
        var resistance: Int
        var crc: Int

        init {
            var index = 0
            index += 2
            stable = byte2UInt(bytes[index]) == 0xAA
            index++
            unit = getUnit(byte2UInt(bytes[index]) and 0x1F)
            precision = (byte2UInt(bytes[index]) and 0xF0) shr 4
            index++
            weight = bytes2UIntBig(bytes[index], bytes[index+1]).div(10f*precision)
            index += 2
            resistance = bytes2UIntBig(bytes[index], bytes[index+1], bytes[index+2], bytes[index+3])
            index += 4
            crc = byte2UInt(bytes[index])
        }

        private fun getUnit(type: Int): Int {
            if (type and 0x01 == 0x01) {
                return LeS1BleCmd.Unit.KG
            }
            if (type and 0x02 == 0x02) {
                return LeS1BleCmd.Unit.LB
            }
            if (type and 0x04 == 0x04) {
                return LeS1BleCmd.Unit.ST
            }
            if (type and 0x08 == 0x08) {
                return LeS1BleCmd.Unit.LB_ST
            }
            if (type and 0x10 == 0x10) {
                return LeS1BleCmd.Unit.JIN
            }
            return 0
        }

        override fun toString(): String {
            return """
                ScaleData :
                bytes : ${bytesToHex(bytes)}
                stable : $stable
                unit : $unit
                precision : $precision
                weight : $weight
                resistance : $resistance
                crc : $crc
            """.trimIndent()
        }
    }

    @Parcelize
    class RtWave constructor(var bytes: ByteArray) : Parcelable {
        var len: Int
        var wave: ByteArray
        var wFs : FloatArray? = null

        init {
            len = toUInt(bytes.copyOfRange(0, 2))
            wave = bytes.copyOfRange(2, bytes.size)

            if (len != wave.size.div(2)) {
                len = wave.size.div(2)
            }

            wFs = FloatArray(len)
            for (i in 0 until len) {
                wFs!![i] = Er1DataController.byteTomV(wave[2 * i], wave[2 * i + 1])
            }
        }
    }

    class EcgResult(val bytes: ByteArray) {
        var recordingTime: Int
        // reserved 2
        var result: Int
        var resultMess: String
        var hr: Int
        var qrs: Int
        var pvcs: Int
        var qtc: Int
        // reserved 20

        init {
            var index = 0
            recordingTime = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            // reserved 2
            index += 2
            result = toUInt(bytes.copyOfRange(index, index+4))
            resultMess = ""
            index += 4
            hr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            qrs = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pvcs = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            qtc = toUInt(bytes.copyOfRange(index, index+2))
        }

        override fun toString(): String {
            return """
                EcgResult :
                bytes : ${bytesToHex(bytes)}
                recordingTime : $recordingTime
                result : $result
                resultMess : $resultMess
                hr : $hr
                qrs : $qrs
                pvcs : $pvcs
                qtc : $qtc
            """.trimIndent()
        }
    }

    class BleFile(val bytes: ByteArray) {
        var hasWeight: Boolean
        var hasEcg: Boolean
        var scaleData: ScaleData? = null
        var ecgResult: EcgResult? = null
        var ecgData: ByteArray? = null
        var ecgIntData: IntArray? = null

        init {
            var index = 10
            hasWeight = (byte2UInt(bytes[index]) and 0x01) == 1
            hasEcg = ((byte2UInt(bytes[index]) and 0x02) shr 1) == 1
            index++
            if (hasWeight) {
                scaleData = ScaleData(bytes.copyOfRange(index, index + 11))
                index += 11
            }
            if (hasEcg) {
                ecgResult = EcgResult(bytes.copyOfRange(index, index + 38))
                index += 38
                ecgData = bytes.copyOfRange(index, bytes.size)
                val len = ecgData!!.size.div(2)
                ecgIntData = IntArray(len)
                for (i in 0 until len) {
                    ecgIntData!![i] = toUInt(ecgData!!.copyOfRange(i*2, (i+1)*2))
                }
            }
        }

        override fun toString(): String {
            return """
                BleFile :
                hasWeight : $hasWeight
                hasEcg : $hasEcg
                scaleData : $scaleData
                ecgResult : $ecgResult
                ecgData : ${Arrays.toString(ecgData)}
                ecgIntData : ${Arrays.toString(ecgIntData)}
            """.trimIndent()
        }
    }

    
    @Parcelize
    class FileList constructor(var bytes: ByteArray) : Parcelable {
        var size: Int
        var fileList = mutableListOf<String>()

        init {
            size = bytes[0].toUInt().toInt()
            for (i in  0 until size) {
                fileList.add(trimStr(String(bytes.copyOfRange(1 + i * 16, 17 + i * 16))))
            }
        }

        override fun toString(): String {
            return """
                FileList :
                size : $size
                fileList : $fileList
            """.trimIndent()
        }
    }
}
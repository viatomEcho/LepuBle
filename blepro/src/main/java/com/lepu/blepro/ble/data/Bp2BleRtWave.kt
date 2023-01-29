package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.toSignedShort
import com.lepu.blepro.utils.toUInt

class Bp2BleRtWave {

    var waveDataType : Int
    var waveData : ByteArray
    var waveformSize : Int
    var waveform : ByteArray
    var waveShorts: ShortArray
    var waveFloats: FloatArray

    @ExperimentalUnsignedTypes
    constructor(bytes : ByteArray) {
        waveDataType = bytes[0].toInt()
        waveData = bytes.copyOfRange(1, 21)
        waveformSize = toUInt(bytes.copyOfRange(21, 23))
        waveform = bytes.copyOfRange(23, bytes.size)
        val len = waveform.size/2
        waveShorts = ShortArray(len)
        waveFloats = FloatArray(len)
        var temp: Short
        for (i in 0 until len) {
            temp = toSignedShort(waveform[2 * i], waveform[2 * i + 1])
            waveShorts[i] = if (temp == 32767.toShort()) {
                0
            } else {
                temp
            }
            waveFloats[i] = waveShorts[i] * 0.003098f
        }
    }

    override fun toString(): String {
        return """
            Bp2BleRtWave : 
            waveDataType : $waveDataType
            waveformSize : $waveformSize
        """.trimIndent()
    }
}

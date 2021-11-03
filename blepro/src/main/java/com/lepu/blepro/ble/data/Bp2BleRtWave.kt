package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.toUInt


class Bp2BleRtWave {

    var waveDataType : Int
    var waveData : ByteArray
    var waveformSize : Int
    var waveform : ByteArray?


    @ExperimentalUnsignedTypes
    constructor(bytes : ByteArray) {
        waveDataType = bytes[0].toInt()
        waveData = bytes.copyOfRange(1, 21)
        waveformSize = toUInt(bytes.copyOfRange(21, 23))
        waveform = bytes.copyOfRange(23, bytes.size)
    }
}

package com.lepu.blepro.ble.data

class Bp2BleRtData {

    var rtState : Bp2BleRtState
    var rtWave : Bp2BleRtWave

    constructor(bytes: ByteArray) {
        rtState = Bp2BleRtState(bytes.copyOfRange(0, 9))
        rtWave = Bp2BleRtWave(bytes.copyOfRange(9, bytes.size))
    }
}
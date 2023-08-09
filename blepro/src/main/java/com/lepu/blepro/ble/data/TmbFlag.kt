package com.lepu.blepro.ble.data

class TmbFlag() {

    var unit = 0  // 0:mmHg 1:kPa
    var pr = 0  // 0:not support 1:support
    var id = 0  //
    var utc = 0  //
    var bodyMovement = 0  //
    var cuffFit = 0  //
    var irregularPulse = 0  //
    var measurementPosition = 0  //
    var timeZone = 0  //
    var timeStamp = 0  //

    constructor(bytes: ByteArray) : this() {

    }

}
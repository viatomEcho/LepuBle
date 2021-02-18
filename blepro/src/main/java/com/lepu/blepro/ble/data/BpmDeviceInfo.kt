package com.lepu.blepro.ble.data

import java.util.*

class BpmDeviceInfo {
    var name: String
    var mainVersion: Int
    var secondVersion: Int
    var lastDate: Date

    constructor(bytes: ByteArray, deviceName: String) {
        this.name = deviceName
        mainVersion = bytes[5].toInt()
        secondVersion = bytes[6].toInt()

        val c = Calendar.getInstance()
        c.set(bytes[7].toInt() + 2000, bytes[8].toInt() - 1, bytes[9].toInt())
        this.lastDate = c.time
    }

    fun getFwVersion() = "${mainVersion}.${secondVersion}"
}
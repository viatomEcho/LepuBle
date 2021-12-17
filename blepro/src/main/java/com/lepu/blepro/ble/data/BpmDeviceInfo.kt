package com.lepu.blepro.ble.data

import java.text.SimpleDateFormat
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

    override fun toString(): String {
        val format = SimpleDateFormat("HH:mm:ss MMM dd, yyyy", Locale.getDefault())
        val dateStr = format.format(lastDate)
        return """
            BpmDeviceInfo:
            mainVersion: $mainVersion
            secondVersion: $secondVersion
            lastDate: $dateStr
        """.trimIndent()
    }

}
package com.lepu.blepro.ble.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@ExperimentalUnsignedTypes
@Parcelize
class Pc300DeviceInfo : Parcelable {
    var deviceId = 0
    var deviceName = ""
    var softwareV = ""
    var hardwareV = ""
    var batLevel = 0

    override fun toString(): String {
        return """
            Pc300DeviceInfo : 
            deviceId : $deviceId
            deviceName : $deviceName
            softwareV : $softwareV
            hardwareV : $hardwareV
            batLevel : $batLevel
        """.trimIndent()
    }
}
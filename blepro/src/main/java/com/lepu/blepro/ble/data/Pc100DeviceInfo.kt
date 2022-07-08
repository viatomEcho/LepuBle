package com.lepu.blepro.ble.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@ExperimentalUnsignedTypes
@Parcelize
class Pc100DeviceInfo : Parcelable {
    var sn = ""
    var deviceId = 0
    var deviceName = ""
    var softwareV = ""
    var hardwareV = ""
    var batLevel = 0
    var batStatus = 0

    override fun toString(): String {
        return """
            Pc100DeviceInfo : 
            sn : $sn
            deviceId : $deviceId
            deviceName : $deviceName
            softwareV : $softwareV
            hardwareV : $hardwareV
            batLevel : $batLevel
            batStatus : $batStatus
        """.trimIndent()
    }
}
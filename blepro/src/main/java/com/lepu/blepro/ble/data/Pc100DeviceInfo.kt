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
    var batLevel = 0    // 电量等级 0-3
    var batStatus = 0   // 0：为没有充电，1：表示充电中，2：表示充电完成，3：保留

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
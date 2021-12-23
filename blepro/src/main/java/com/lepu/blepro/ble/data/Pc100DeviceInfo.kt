package com.lepu.blepro.ble.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@ExperimentalUnsignedTypes
@Parcelize
class Pc100DeviceInfo : Parcelable {
    var sn: String? = null
    var deviceId: Int? = null
    var deviceName: String? = null
    var softwareV: String? = null
    var hardwareV: String? = null
    var batLevel: Int? = null
    var batStatus: Int? = null

    override fun toString(): String {
        return """
            Pc100DeviceInfo
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
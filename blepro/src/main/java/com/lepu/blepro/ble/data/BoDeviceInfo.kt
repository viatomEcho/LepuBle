package com.lepu.blepro.ble.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@ExperimentalUnsignedTypes
@Parcelize
class BoDeviceInfo : Parcelable {
    var deviceName = ""
    var sn = ""
    var softwareV = ""
    var hardwareV = ""
    var branchCode = ""

    override fun toString(): String {
        return """
            deviceName : $deviceName
            sn : $sn
            softwareV : $softwareV
            hardwareV : $hardwareV
            branchCode : $branchCode
        """.trimIndent()
    }
}
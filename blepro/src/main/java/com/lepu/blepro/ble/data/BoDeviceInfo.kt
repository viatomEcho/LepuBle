package com.lepu.blepro.ble.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@ExperimentalUnsignedTypes
@Parcelize
class BoDeviceInfo : Parcelable {
    var deviceName: String? = null
    var sn: String? = null
    var softwareV: String? = null
    var hardwareV: String? = null
    var branchCode: String? = null

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
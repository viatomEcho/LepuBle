package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

object F4ScaleBleResponse {

    @ExperimentalUnsignedTypes
    @Parcelize
    class F4ScaleResponse constructor(var bytes: ByteArray) : Parcelable {
        var packageNo: Int
        var len: Int
        var seqNo: Int
        var cmd: Int
        var content: ByteArray  // 内容

        init {
            packageNo = (bytes[0].toUInt() and 0xFFu).toInt()
            len = (bytes[1].toUInt() and 0xFFu).toInt()
            seqNo = (bytes[2].toUInt() and 0xFFu).toInt()
            cmd = (bytes[3].toUInt() and 0xFFu).toInt()
            content = bytes.copyOfRange(4, 17)
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class BasicData constructor(var bytes: ByteArray) : Parcelable {
        var kgScaleDivision: Int        // 分度值kg公斤
        var lbScaleDivision: Int        // 分度值lb英镑
        var isSupportHR: Boolean        // 支持心率
        var isSupportBalance: Boolean   // 支持平衡
        var isSupportFocus: Boolean     // 支持重心
        var isSupportOTA: Boolean       // 支持OTA
        var isSupportOffline: Boolean   // 支持离线
        var isSupportUserData: Boolean  // 支持用户数据
        var mUserCount: Int             // 支持用户数量

        var battery: Int
        var algType: Int

        var isImp: Boolean
        var isBalance: Boolean
        var isHR: Boolean

        init {
            kgScaleDivision = (bytes[4].toUInt() and 0x07u).toInt()
            lbScaleDivision = (bytes[4].toUInt() and 0x38u).toInt() shr 3
            isSupportHR = (bytes[4].toUInt() and 0x40u).toInt() shr 6 != 0
            isSupportBalance = (bytes[4].toUInt() and 0x80u).toInt() shr 7 != 0
            isSupportFocus = (bytes[3].toUInt() and 0x01u).toInt() != 0
            isSupportOTA = (bytes[3].toUInt() and 0x02u).toInt() shr 1 != 0
            isSupportOffline = (bytes[3].toUInt() and 0x04u).toInt() shr 2 != 0
            isSupportUserData = (bytes[3].toUInt() and 0x10u).toInt() shr 4 != 0
            mUserCount = ((bytes[2].toUInt() and 0x03u).toInt() shl 3) + (bytes[3].toUInt() and 0xE0u).toInt() shr 5

            battery = (bytes[5].toUInt() and 0xFFu).toInt()
            algType = (bytes[6].toUInt() and 0xFFu).toInt()
            isImp = (bytes[7].toUInt() and 0x01u).toInt() != 0
            isBalance = (bytes[7].toUInt() and 0x01u).toInt() shr 1 != 0
            isHR = (bytes[7].toUInt() and 0x01u).toInt() shr 2 != 0

        }

        override fun toString(): String {
            return """
                kgScaleDivision = $kgScaleDivision
                lbScaleDivision = $lbScaleDivision
                isSupportHR = $isSupportHR
                isSupportBalance = $isSupportBalance
                isSupportFocus = $isSupportFocus
                isSupportOTA = $isSupportOTA
                isSupportOffline = $isSupportOffline
                isSupportUserData = $isSupportUserData
                mUserCount = $mUserCount
                battery = $battery
                algType = $algType
                isImp = $isImp
                isBalance = $isBalance
                isHR = $isHR
            """.trimIndent()
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class StableData constructor(var bytes: ByteArray) : Parcelable {
        var state: Int
        var weightG: Int
        var hr: Int

        init {
            state = (bytes[0].toUInt() and 0xFFu).toInt()
            weightG = ((bytes[2].toUInt() and 0x03u).toInt() shl 16) + ((bytes[3].toUInt() and 0xFFu).toInt() shl 8) + (bytes[4].toUInt() and 0xFFu).toInt()
            hr = (bytes[5].toUInt() and 0xFFu).toInt()
        }

        override fun toString(): String {
            return """
                state = $state
                weightG = $weightG
                hr = $hr
            """.trimIndent()
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class WeightData constructor(var bytes: ByteArray) : Parcelable {
        var weightG: Int
        var hr: Int
        var imp: Int

        init {
            weightG = ((bytes[1].toUInt() and 0x03u).toInt() shl 16) + ((bytes[2].toUInt() and 0xFFu).toInt() shl 8) + (bytes[3].toUInt() and 0xFFu).toInt()
            hr = (bytes[4].toUInt() and 0xFFu).toInt()
            imp = ((bytes[5].toUInt() and 0xFFu).toInt() shl 8) + (bytes[6].toUInt() and 0xFFu).toInt()
        }

        override fun toString(): String {
            return """
                weightG = $weightG
                hr = $hr
                imp = $imp
            """.trimIndent()
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class HistoryData constructor(var bytes: ByteArray) : Parcelable {
        var time: Long
        var weightData: WeightData

        init {
            time = (((bytes[0].toUInt() and 0xFFu).toInt() shl 24) +
                    ((bytes[1].toUInt() and 0xFFu).toInt() shl 16) +
                    ((bytes[2].toUInt() and 0xFFu).toInt() shl 8) +
                    (bytes[3].toUInt() and 0xFFu).toInt()).toLong()
            weightData = WeightData(bytes.copyOfRange(4, 11))
        }

        override fun toString(): String {
            return """
                time = $time
                weightData = $weightData
            """.trimIndent()
        }
    }

}
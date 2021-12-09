package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.utils.*
import kotlinx.android.parcel.Parcelize

object F5ScaleBleResponse {

    @ExperimentalUnsignedTypes
    @Parcelize
    class F5ScaleResponse constructor(var bytes: ByteArray) : Parcelable {
        var cmd: Int
        var content: ByteArray  // 内容

        init {
            cmd = (bytes[18].toUInt() and 0xFFu).toInt()
            content = bytes.copyOfRange(2, 17)
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class WeightData constructor(var bytes: ByteArray) : Parcelable {
        var weightG: Int               // 体重g
        var kgScaleDivision: Int       // 分度值kg公斤
        var lbScaleDivision: Int       // 分度值lb英镑
        var isElectrode8: Boolean      // true 八电极 false 四电极
        var isSupportHR: Boolean       // 支持心率
        var isSupportBalance: Boolean  // 支持平衡
        var isSupportFocus: Boolean    // 支持重心
        var isStable: Boolean          // 稳定

        init {
            weightG = ((bytes[1].toUInt() and 0x03u).toInt() shl 16) + ((bytes[2].toUInt() and 0xFFu).toInt() shl 8) + (bytes[3].toUInt() and 0xFFu).toInt()
            kgScaleDivision = (bytes[1].toUInt() and 0x1Cu).toInt() shr 2
            lbScaleDivision = (bytes[1].toUInt() and 0xE0u).toInt() shr 5
            isElectrode8 = (bytes[0].toUInt() and 0x01u).toInt() != 0
            isSupportHR = ((bytes[0].toUInt() and 0x02u).toInt() shr 1) != 0
            isSupportBalance = ((bytes[0].toUInt() and 0x04u).toInt() shr 2) != 0
            isSupportFocus = ((bytes[0].toUInt() and 0x08u).toInt() shr 3) != 0
            isStable = ((bytes[0].toUInt() and 0x80u).toInt() shr 7) != 0
        }

        override fun toString(): String {
            return """
                weightG = $weightG
                kgScaleDivision = $kgScaleDivision
                lbScaleDivision = $lbScaleDivision
                isElectrode8 = $isElectrode8
                isSupportHR = $isSupportHR
                isSupportBalance = $isSupportBalance
                isSupportFocus = $isSupportFocus
                isStable = $isStable
            """
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class ImpedanceData constructor(var bytes: ByteArray) : Parcelable {
        var impNum: Int
        var packageNo: Int
        var imp: Int

        init {
            impNum = (bytes[0].toUInt() and 0xFFu).toInt()
            packageNo = (bytes[1].toUInt() and 0xFFu).toInt()
            imp = toUInt(bytes.copyOfRange(2, 4))
        }

        override fun toString(): String {
            return """
                impNum = $impNum
                packageNo = $packageNo
                imp = $imp
            """
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class StableData constructor(var bytes: ByteArray) : Parcelable {
        var isStable: Boolean
        var leftWeightKG: Int

        init {
            isStable = (bytes[0].toUInt() and 0xFFu).toInt() != 0
            leftWeightKG = toUInt(bytes.copyOfRange(1, 3))
        }

        override fun toString(): String {
            return """
                isStable = $isStable
                leftWeightKG = $leftWeightKG
            """
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class HrData constructor(var bytes: ByteArray) : Parcelable {
        var dataType: Int
        var hr: Int

        init {
            dataType = (bytes[0].toUInt() and 0xFFu).toInt()
            hr = (bytes[1].toUInt() and 0xFFu).toInt()
        }

        override fun toString(): String {
            return """
                dataType = $dataType
                hr = $hr
            """
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class HistoryData constructor(var bytes: ByteArray) : Parcelable {
        var time: Int
        var weightData: WeightData
        var leftBalanceKG: Int
        var hr: Int
        var impNum: Int
        var imp: Int

        init {
            time = toUInt(bytes.copyOfRange(1, 5))
            weightData = WeightData(bytes.copyOfRange(5, 9))
            leftBalanceKG = toUInt(bytes.copyOfRange(9, 11))
            hr = (bytes[11].toUInt() and 0xFFu).toInt()
            impNum = (bytes[12].toUInt() and 0xFFu).toInt()
            imp = toUInt(bytes.copyOfRange(13, 15))
        }

        override fun toString(): String {
            return """
                time = $time
                weightData = $weightData
                leftBalanceKG = $leftBalanceKG
                hr = $hr
                impNum = $impNum
                imp = $imp
            """
        }
    }

}
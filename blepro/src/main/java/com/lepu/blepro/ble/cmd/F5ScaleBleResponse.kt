package com.lepu.blepro.ble.cmd

import android.os.Parcelable
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
        var weightJin: Double          // 体重斤
        var weightKG: Double           // 体重kg
        var weightLB: Double           // 体重磅lb
        var weightST: Int              // 体重英石st
        var weightSTLB: Double         // 体重st:lb
        var kgScaleDivision: Int       // 分度值kg公斤
        var lbScaleDivision: Int       // 分度值lb英镑
        var isElectrode8: Boolean      // true 八电极 false 四电极
        var isSupportHR: Boolean       // 支持心率
        var isSupportBalance: Boolean  // 支持平衡
        var isSupportFocus: Boolean    // 支持重心
        var isStable: Boolean          // 稳定

        init {
            weightG = ((bytes[1].toUInt() and 0x03u).toInt() shl 16) + ((bytes[2].toUInt() and 0xFFu).toInt() shl 8) + (bytes[3].toUInt() and 0xFFu).toInt()

            weightJin = weightG.div(500.0)
            weightKG = weightG.div(1000.0)
            weightLB = weightG*0.0022046
            weightST = (weightLB/14).toInt()
            weightSTLB = weightLB - weightST*14

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
                weightJin = $weightJin
                weightKG = $weightKG
                weightLB = $weightLB
                weightST = $weightST
                kgScaleDivision = $kgScaleDivision
                lbScaleDivision = $lbScaleDivision
                isElectrode8 = $isElectrode8
                isSupportHR = $isSupportHR
                isSupportBalance = $isSupportBalance
                isSupportFocus = $isSupportFocus
                isStable = $isStable
            """.trimIndent()
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class ImpedanceData constructor(var bytes: ByteArray) : Parcelable {
        var impNum: Int
        var packageNo: Int
        var imp: Double

        init {
            impNum = (bytes[0].toUInt() and 0xFFu).toInt()
            packageNo = (bytes[1].toUInt() and 0xFFu).toInt()
            imp = (((bytes[2].toUInt() and 0xFFu).toInt() shl 8) + (bytes[3].toUInt() and 0xFFu).toInt()).toDouble()
        }

        override fun toString(): String {
            return """
                impNum = $impNum
                packageNo = $packageNo
                imp = $imp
            """.trimIndent()
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class StableData constructor(var bytes: ByteArray) : Parcelable {
        var isStable: Boolean
        var leftWeightKG: Int

        init {
            isStable = (bytes[0].toUInt() and 0xFFu).toInt() != 0
            leftWeightKG = ((bytes[1].toUInt() and 0xFFu).toInt() shl 8) + (bytes[2].toUInt() and 0xFFu).toInt()
        }

        override fun toString(): String {
            return """
                isStable = $isStable
                leftWeightKG = $leftWeightKG
            """.trimIndent()
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
            """.trimIndent()
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class HistoryData constructor(var bytes: ByteArray) : Parcelable {
        var time: Long
        var weightData: WeightData
        var leftBalanceKG: Int
        var hr: Int
        var impNum: Int
        var imp: Double

        init {
            time = (((bytes[1].toUInt() and 0xFFu).toInt() shl 24) +
                    ((bytes[2].toUInt() and 0xFFu).toInt() shl 16) +
                    ((bytes[3].toUInt() and 0xFFu).toInt() shl 8) +
                    (bytes[4].toUInt() and 0xFFu).toInt()).toLong()
            weightData = WeightData(bytes.copyOfRange(5, 9))
            leftBalanceKG = ((bytes[9].toUInt() and 0xFFu).toInt() shl 8) + (bytes[10].toUInt() and 0xFFu).toInt()
            hr = (bytes[11].toUInt() and 0xFFu).toInt()
            impNum = (bytes[12].toUInt() and 0xFFu).toInt()
            imp = (((bytes[13].toUInt() and 0xFFu).toInt() shl 8) + (bytes[14].toUInt() and 0xFFu).toInt()).toDouble()
        }

        override fun toString(): String {
            return """
                time = $time
                weightData = $weightData
                leftBalanceKG = $leftBalanceKG
                hr = $hr
                impNum = $impNum
                imp = $imp
            """.trimIndent()
        }
    }

}
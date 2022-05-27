package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.utils.ByteUtils.byte2UInt
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
        var unit: Int

        init {
            packageNo = (bytes[0].toUInt() and 0xFFu).toInt()
            len = (bytes[1].toUInt() and 0xFFu).toInt()
            seqNo = (bytes[2].toUInt() and 0xFFu).toInt()
            cmd = (bytes[3].toUInt() and 0xFFu).toInt()
            content = bytes.copyOfRange(4, bytes.size-1)
            unit = byte2UInt(bytes[bytes.size-1]) and 0xE0 shr 5
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class BasicData constructor(var bytes: ByteArray) : Parcelable {
        var kgScaleDivision: Int        // 分度值kg公斤 (0:0.01, 1:0.02, 2:0.05, 3:0.1, 4:0.2)
        var lbScaleDivision: Int        // 分度值lb英镑
        var isSupportHr: Boolean        // 支持心率
        var isSupportBalance: Boolean   // 支持平衡
        var isSupportFocus: Boolean     // 支持重心
        var isSupportOta: Boolean       // 支持OTA
        var isSupportOffline: Boolean   // 支持离线
        var isSupportUserData: Boolean  // 支持用户数据
        var mUserCount: Int             // 支持用户数量
        var isSupportBattery: Int       // 支持电量
        var isSupportKg: Boolean        // 支持kg
        var isSupportLb: Boolean        // 支持lb
        var isSupportSt: Boolean        // 支持st
        var isSupportJin: Boolean       // 支持jin
        var electrode: Int              // 电极 (0:四电极, 1:八电极)
        var frequency: Int              // 0:单频, 1:双频, 2:三频
        var impType: Int                // 0:原始阻抗, 1:阻抗除以10
        var impCount: Int               // 0:6阻抗, 1:5阻抗

        var battery: Int                // 0-100
        var algType: Int                // 算法类型

        var isImp: Boolean
        var isBalance: Boolean
        var isHr: Boolean

        var imp: Int                    // 0:5kHz, 1:20kHz, 2:50kHz, 3:100kHz, 4:200kHz, 5:250kHz

        init {
            kgScaleDivision = (bytes[4].toUInt() and 0x07u).toInt()
            lbScaleDivision = (bytes[4].toUInt() and 0x38u).toInt() shr 3
            isSupportHr = (bytes[4].toUInt() and 0x40u).toInt() shr 6 != 0
            isSupportBalance = (bytes[4].toUInt() and 0x80u).toInt() shr 7 != 0
            isSupportFocus = (bytes[3].toUInt() and 0x01u).toInt() != 0
            isSupportOta = (bytes[3].toUInt() and 0x02u).toInt() shr 1 != 0
            isSupportOffline = (bytes[3].toUInt() and 0x04u).toInt() shr 2 != 0
            isSupportUserData = (bytes[3].toUInt() and 0x10u).toInt() shr 4 != 0
            mUserCount = ((bytes[2].toUInt() and 0x03u).toInt() shl 3) + ((bytes[3].toUInt() and 0xE0u).toInt() shr 5)
            isSupportBattery = (bytes[2].toUInt() and 0x0Cu).toInt() shr 2
            isSupportKg = (bytes[2].toUInt() and 0x10u).toInt() shr 4 != 0
            isSupportLb = (bytes[2].toUInt() and 0x20u).toInt() shr 5 != 0
            isSupportSt = (bytes[2].toUInt() and 0x40u).toInt() shr 6 != 0
            isSupportJin = (bytes[2].toUInt() and 0x80u).toInt() shr 7 != 0
            electrode = (bytes[1].toUInt() and 0x01u).toInt()
            frequency = (bytes[1].toUInt() and 0x0Eu).toInt() shr 1
            impType = (bytes[1].toUInt() and 0x10u).toInt() shr 4
            impCount = (bytes[1].toUInt() and 0x20u).toInt() shr 5

            battery = (bytes[5].toUInt() and 0xFFu).toInt()
            algType = (bytes[6].toUInt() and 0xFFu).toInt()
            isImp = (bytes[7].toUInt() and 0x01u).toInt() != 0
            isBalance = (bytes[7].toUInt() and 0x02u).toInt() shr 1 != 0
            isHr = (bytes[7].toUInt() and 0x03u).toInt() shr 2 != 0

            imp = (bytes[8].toUInt() and 0xFFu).toInt()
        }

        override fun toString(): String {
            return """
                kgScaleDivision = $kgScaleDivision
                lbScaleDivision = $lbScaleDivision
                isSupportHr = $isSupportHr
                isSupportBalance = $isSupportBalance
                isSupportFocus = $isSupportFocus
                isSupportOta = $isSupportOta
                isSupportOffline = $isSupportOffline
                isSupportUserData = $isSupportUserData
                mUserCount = $mUserCount
                isSupportBattery = $isSupportBattery
                isSupportKg = $isSupportKg
                isSupportLb = $isSupportLb
                isSupportSt = $isSupportSt
                isSupportJin = $isSupportJin
                electrode = $electrode
                frequency = $frequency
                impType = $impType
                impCount = $impCount
                battery = $battery
                algType = $algType
                isImp = $isImp
                isBalance = $isBalance
                isHr = $isHr
                imp = $imp
            """.trimIndent()
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class StableData constructor(var bytes: ByteArray) : Parcelable {
        var state: Int    // 测量状态 (0:预留, 1:测量体重中, 2:测量阻抗中4电极, 3:测量阻抗中8电极, 4:测量心率中, 5:测量平衡中)
        var weightG: Int
        var weightJin: Double          // 体重斤
        var weightKg: Double           // 体重kg
        var weightLb: Double           // 体重磅lb
        var weightSt: Int              // 体重英石st
        var weightStLb: Double         // 体重st:lb
        var algType: Int               // 算法类型
        var hr: Int

        init {
            state = (bytes[0].toUInt() and 0xFFu).toInt()
            weightG = ((bytes[2].toUInt() and 0x03u).toInt() shl 16) + ((bytes[3].toUInt() and 0xFFu).toInt() shl 8) + (bytes[4].toUInt() and 0xFFu).toInt()

            weightJin = weightG.div(500.0)
            weightKg = weightG.div(1000.0)
            weightLb = weightG*0.0022046
            weightSt = (weightLb/14).toInt()
            weightStLb = weightLb - weightSt*14

            algType = (bytes[1].toUInt() and 0xFFu).toInt()
            hr = (bytes[5].toUInt() and 0xFFu).toInt()
        }

        override fun toString(): String {
            return """
                state = $state
                weightG = $weightG
                weightJin = $weightJin
                weightKg = $weightKg
                weightLb = $weightLb
                weightSt = $weightSt
                weightStLb = $weightStLb
                algType = $algType
                hr = $hr
            """.trimIndent()
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class WeightData constructor(var bytes: ByteArray) : Parcelable {
        var weightG: Int
        var weightJin: Double          // 体重斤
        var weightKg: Double           // 体重kg
        var weightLb: Double           // 体重磅lb
        var weightSt: Int              // 体重英石st
        var weightStLb: Double         // 体重st:lb
        var algType: Int               // 算法类型
        var hr: Int                    // 心率
        var bodyImp = 0                // 躯干阻抗
        var leftHandImp = 0            // 左手阻抗
        var rightHandImp = 0           // 右手阻抗
        var leftLegImp = 0             // 左脚阻抗
        var rightLegImp = 0            // 右脚阻抗
        var bodyImp2 = 0                // 躯干阻抗
        var leftHandImp2 = 0            // 左手阻抗
        var rightHandImp2 = 0           // 右手阻抗
        var leftLegImp2 = 0             // 左脚阻抗
        var rightLegImp2 = 0            // 右脚阻抗

        init {
            weightG = ((bytes[1].toUInt() and 0x03u).toInt() shl 16) + ((bytes[2].toUInt() and 0xFFu).toInt() shl 8) + (bytes[3].toUInt() and 0xFFu).toInt()

            weightJin = weightG.div(500.0)
            weightKg = weightG.div(1000.0)
            weightLb = weightG*0.0022046
            weightSt = (weightLb/14).toInt()
            weightStLb = weightLb - weightSt*14

            algType = (bytes[0].toUInt() and 0xFFu).toInt()
            hr = (bytes[4].toUInt() and 0xFFu).toInt()
            bodyImp = ((bytes[5].toUInt() and 0xFFu).toInt() shl 8) + (bytes[6].toUInt() and 0xFFu).toInt()
            leftHandImp = ((bytes[7].toUInt() and 0xFFu).toInt() shl 8) + (bytes[8].toUInt() and 0xFFu).toInt()
            rightHandImp = ((bytes[9].toUInt() and 0xFFu).toInt() shl 8) + (bytes[10].toUInt() and 0xFFu).toInt()
            leftLegImp = ((bytes[11].toUInt() and 0xFFu).toInt() shl 8) + (bytes[12].toUInt() and 0xFFu).toInt()
            rightLegImp = ((bytes[13].toUInt() and 0xFFu).toInt() shl 8) + (bytes[14].toUInt() and 0xFFu).toInt()
            if (bytes.size > 15) {
                bodyImp2 = ((bytes[15].toUInt() and 0xFFu).toInt() shl 8) + (bytes[16].toUInt() and 0xFFu).toInt()
                leftHandImp2 = ((bytes[17].toUInt() and 0xFFu).toInt() shl 8) + (bytes[18].toUInt() and 0xFFu).toInt()
                rightHandImp2 = ((bytes[19].toUInt() and 0xFFu).toInt() shl 8) + (bytes[20].toUInt() and 0xFFu).toInt()
                leftLegImp2 = ((bytes[21].toUInt() and 0xFFu).toInt() shl 8) + (bytes[22].toUInt() and 0xFFu).toInt()
                rightLegImp2 = ((bytes[23].toUInt() and 0xFFu).toInt() shl 8) + (bytes[24].toUInt() and 0xFFu).toInt()
            }
        }

        override fun toString(): String {
            return """
                weightG = $weightG
                weightJin = $weightJin
                weightKg = $weightKg
                weightLb = $weightLb
                weightSt = $weightSt
                weightStLb = $weightStLb
                algType = $algType
                hr = $hr
                bodyImp = $bodyImp
                leftHandImp = $leftHandImp
                rightHandImp = $rightHandImp
                leftLegImp = $leftLegImp
                rightLegImp = $rightLegImp
                bodyImp = $bodyImp
                leftHandImp = $leftHandImp
                rightHandImp = $rightHandImp
                leftLegImp = $leftLegImp
                rightLegImp = $rightLegImp
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